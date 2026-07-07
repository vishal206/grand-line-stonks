package com.grandlinestonks.market;

import static org.assertj.core.api.Assertions.assertThat;

import com.grandlinestonks.TestcontainersConfiguration;
import com.grandlinestonks.account.Account;
import com.grandlinestonks.account.AccountRepository;
import com.grandlinestonks.auth.dto.AuthResponse;
import com.grandlinestonks.auth.dto.MeResponse;
import com.grandlinestonks.auth.dto.SignupRequest;
import com.grandlinestonks.ledger.LedgerService;
import com.grandlinestonks.market.dto.BetRequest;
import com.grandlinestonks.market.dto.BetResponse;
import com.grandlinestonks.market.dto.CreateMarketRequest;
import com.grandlinestonks.market.dto.MarketResponse;
import com.grandlinestonks.market.dto.ResolveRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class SettlementIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MarketRepository marketRepository;

    @Autowired
    private LedgerService ledgerService;

    @Test
    void fullLifecyclePaysWinnersExactlyOncePerShare() {
        String owner = signup();
        String winner = signup();
        String loser = signup();
        MarketResponse market = open(owner, createMarket(owner).id());
        Long yesOutcome = market.outcomes().get(0).id();
        Long noOutcome = market.outcomes().get(1).id();

        BetResponse winningBet = bet(winner, market.id(), yesOutcome, new BigDecimal("60.0000"));
        BetResponse losingBet = bet(loser, market.id(), noOutcome, new BigDecimal("40.0000"));

        post(owner, "/api/markets/" + market.id() + "/close", null);
        ResponseEntity<MarketResponse> resolved = post(
                owner, "/api/markets/" + market.id() + "/resolve", new ResolveRequest(yesOutcome));
        assertThat(resolved.getBody().status()).isEqualTo(MarketStatus.RESOLVED);
        assertThat(resolved.getBody().winningOutcomeId()).isEqualTo(yesOutcome);

        ResponseEntity<MarketResponse> settled =
                post(owner, "/api/markets/" + market.id() + "/settle", null);
        assertThat(settled.getBody().status()).isEqualTo(MarketStatus.SETTLED);

        assertThat(me(winner).balance()).isEqualByComparingTo(
                new BigDecimal("10000.0000")
                        .subtract(new BigDecimal("60.0000"))
                        .add(winningBet.shares()));
        assertThat(me(loser).balance())
                .isEqualByComparingTo(new BigDecimal("10000.0000").subtract(new BigDecimal("40.0000")));

        Market persisted = marketRepository.findById(market.id()).orElseThrow();
        Account marketMaker =
                accountRepository.findById(persisted.getMarketMakerAccountId()).orElseThrow();
        assertThat(marketMaker.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(losingBet.shares()).isGreaterThan(BigDecimal.ZERO);
        assertSystemConsistent();
    }

    @Test
    void settlingTwiceIsRejected() {
        String owner = signup();
        MarketResponse market = open(owner, createMarket(owner).id());
        bet(owner, market.id(), market.outcomes().get(0).id(), new BigDecimal("10.0000"));
        post(owner, "/api/markets/" + market.id() + "/close", null);
        post(owner, "/api/markets/" + market.id() + "/resolve",
                new ResolveRequest(market.outcomes().get(0).id()));
        ResponseEntity<MarketResponse> first =
                post(owner, "/api/markets/" + market.id() + "/settle", null);
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<MarketResponse> second =
                post(owner, "/api/markets/" + market.id() + "/settle", null);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void resolvingAnOpenMarketIsRejected() {
        String owner = signup();
        MarketResponse market = open(owner, createMarket(owner).id());
        ResponseEntity<MarketResponse> response = post(
                owner, "/api/markets/" + market.id() + "/resolve",
                new ResolveRequest(market.outcomes().get(0).id()));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void cancelRefundsAllStakes() {
        String owner = signup();
        String bettor = signup();
        MarketResponse market = open(owner, createMarket(owner).id());
        bet(bettor, market.id(), market.outcomes().get(0).id(), new BigDecimal("123.4567"));
        bet(bettor, market.id(), market.outcomes().get(1).id(), new BigDecimal("76.5433"));

        ResponseEntity<MarketResponse> cancelled =
                post(owner, "/api/markets/" + market.id() + "/cancel", null);
        assertThat(cancelled.getBody().status()).isEqualTo(MarketStatus.CANCELLED);

        assertThat(me(bettor).balance()).isEqualByComparingTo("10000.0000");
        Market persisted = marketRepository.findById(market.id()).orElseThrow();
        assertThat(accountRepository.findById(persisted.getMarketMakerAccountId())
                .orElseThrow().getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertSystemConsistent();
    }

    @Test
    void bettingOnSettledOrCancelledMarketIsRejected() {
        String owner = signup();
        MarketResponse market = open(owner, createMarket(owner).id());
        post(owner, "/api/markets/" + market.id() + "/cancel", null);
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/markets/" + market.id() + "/bets", HttpMethod.POST,
                new HttpEntity<>(new BetRequest(market.outcomes().get(0).id(),
                        new BigDecimal("10"), UUID.randomUUID().toString()), bearer(owner)),
                String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    private void assertSystemConsistent() {
        BigDecimal total = BigDecimal.ZERO;
        for (Account account : accountRepository.findAll()) {
            assertThat(ledgerService.recomputeBalance(account.getId()))
                    .isEqualByComparingTo(account.getBalance());
            total = total.add(account.getBalance());
        }
        assertThat(total).isEqualByComparingTo(BigDecimal.ZERO);
    }

    private String signup() {
        String username = "settle_" + UUID.randomUUID().toString().substring(0, 12).replace("-", "");
        return restTemplate.postForEntity(
                        "/api/auth/signup", new SignupRequest(username, "password123"), AuthResponse.class)
                .getBody().token();
    }

    private MarketResponse createMarket(String token) {
        return restTemplate.exchange(
                "/api/markets", HttpMethod.POST,
                new HttpEntity<>(new CreateMarketRequest(
                        "Will the giant kraken surface near the reverse mountain?",
                        List.of("Yes", "No"), new BigDecimal("100")), bearer(token)),
                MarketResponse.class).getBody();
    }

    private MarketResponse open(String token, Long marketId) {
        return restTemplate.exchange(
                "/api/markets/" + marketId + "/open", HttpMethod.POST,
                new HttpEntity<>(bearer(token)), MarketResponse.class).getBody();
    }

    private BetResponse bet(String token, Long marketId, Long outcomeId, BigDecimal amount) {
        return restTemplate.exchange(
                "/api/markets/" + marketId + "/bets", HttpMethod.POST,
                new HttpEntity<>(new BetRequest(outcomeId, amount, UUID.randomUUID().toString()),
                        bearer(token)),
                BetResponse.class).getBody();
    }

    private <T> ResponseEntity<MarketResponse> post(String token, String path, T body) {
        return restTemplate.exchange(
                path, HttpMethod.POST, new HttpEntity<>(body, bearer(token)), MarketResponse.class);
    }

    private MeResponse me(String token) {
        return restTemplate.exchange(
                "/api/me", HttpMethod.GET, new HttpEntity<>(bearer(token)), MeResponse.class).getBody();
    }

    private HttpHeaders bearer(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
