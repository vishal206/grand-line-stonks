package com.grandlinestonks.market;

import static org.assertj.core.api.Assertions.assertThat;

import com.grandlinestonks.TestcontainersConfiguration;
import com.grandlinestonks.account.AccountRepository;
import com.grandlinestonks.auth.dto.AuthResponse;
import com.grandlinestonks.auth.dto.MeResponse;
import com.grandlinestonks.auth.dto.SignupRequest;
import com.grandlinestonks.market.dto.BetRequest;
import com.grandlinestonks.market.dto.BetResponse;
import com.grandlinestonks.market.dto.CreateMarketRequest;
import com.grandlinestonks.market.dto.MarketResponse;
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
class MarketLifecycleIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MarketRepository marketRepository;

    @Test
    void createOpenAndBetFlow() {
        String token = signup();
        MarketResponse draft = createMarket(token);
        assertThat(draft.status()).isEqualTo(MarketStatus.DRAFT);
        assertThat(draft.outcomes()).hasSize(2);
        assertThat(draft.outcomes().get(0).price()).isEqualByComparingTo("0.5");

        MarketResponse open = open(token, draft.id());
        assertThat(open.status()).isEqualTo(MarketStatus.OPEN);
        Market persisted = marketRepository.findById(open.id()).orElseThrow();
        BigDecimal subsidy = accountRepository
                .findById(persisted.getMarketMakerAccountId()).orElseThrow().getBalance();
        assertThat(subsidy).isEqualByComparingTo(
                new LmsrMarketMaker(new BigDecimal("100")).maxLoss(2));

        BigDecimal balanceBefore = me(token).balance();
        ResponseEntity<BetResponse> bet = placeBet(
                token, draft.id(), open.outcomes().get(0).id(),
                new BigDecimal("50.0000"), UUID.randomUUID().toString());
        assertThat(bet.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(bet.getBody().shares()).isGreaterThan(BigDecimal.ZERO);
        assertThat(me(token).balance())
                .isEqualByComparingTo(balanceBefore.subtract(new BigDecimal("50.0000")));

        MarketResponse afterBet = getMarket(token, draft.id());
        assertThat(afterBet.outcomes().get(0).price())
                .isGreaterThan(afterBet.outcomes().get(1).price());

        ResponseEntity<List> history = restTemplate.exchange(
                "/api/markets/" + draft.id() + "/history", HttpMethod.GET,
                new HttpEntity<>(bearer(token)), List.class);
        assertThat(history.getBody()).hasSize(1);
    }

    @Test
    void betRetryWithSameIdempotencyKeyDoesNotDoubleBet() {
        String token = signup();
        MarketResponse market = open(token, createMarket(token).id());
        String key = UUID.randomUUID().toString();
        Long outcomeId = market.outcomes().get(0).id();

        BetResponse first = placeBet(token, market.id(), outcomeId,
                new BigDecimal("25.0000"), key).getBody();
        BetResponse retry = placeBet(token, market.id(), outcomeId,
                new BigDecimal("25.0000"), key).getBody();

        assertThat(retry.betId()).isEqualTo(first.betId());
        assertThat(retry.transactionId()).isEqualTo(first.transactionId());
        assertThat(me(token).balance()).isEqualByComparingTo("9975.0000");
    }

    @Test
    void betOnDraftMarketIsRejected() {
        String token = signup();
        MarketResponse draft = createMarket(token);
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/markets/" + draft.id() + "/bets", HttpMethod.POST,
                new HttpEntity<>(new BetRequest(
                        draft.outcomes().get(0).id(), new BigDecimal("10"), UUID.randomUUID().toString()),
                        bearer(token)),
                String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void openingTwiceIsRejected() {
        String token = signup();
        MarketResponse market = createMarket(token);
        open(token, market.id());
        ResponseEntity<String> second = restTemplate.exchange(
                "/api/markets/" + market.id() + "/open", HttpMethod.POST,
                new HttpEntity<>(bearer(token)), String.class);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void openingSomeoneElsesMarketIsForbidden() {
        String owner = signup();
        String intruder = signup();
        MarketResponse market = createMarket(owner);
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/markets/" + market.id() + "/open", HttpMethod.POST,
                new HttpEntity<>(bearer(intruder)), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void betOnForeignOutcomeIsRejected() {
        String token = signup();
        MarketResponse first = open(token, createMarket(token).id());
        MarketResponse second = open(token, createMarket(token).id());
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/markets/" + first.id() + "/bets", HttpMethod.POST,
                new HttpEntity<>(new BetRequest(
                        second.outcomes().get(0).id(), new BigDecimal("10"), UUID.randomUUID().toString()),
                        bearer(token)),
                String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    private String signup() {
        String username = "market_" + UUID.randomUUID().toString().substring(0, 12).replace("-", "");
        return restTemplate.postForEntity(
                        "/api/auth/signup", new SignupRequest(username, "password123"), AuthResponse.class)
                .getBody().token();
    }

    private MarketResponse createMarket(String token) {
        return restTemplate.exchange(
                "/api/markets", HttpMethod.POST,
                new HttpEntity<>(new CreateMarketRequest(
                        "Will the crew reach the final island this year?",
                        List.of("Yes", "No"), new BigDecimal("100")), bearer(token)),
                MarketResponse.class).getBody();
    }

    private MarketResponse open(String token, Long marketId) {
        return restTemplate.exchange(
                "/api/markets/" + marketId + "/open", HttpMethod.POST,
                new HttpEntity<>(bearer(token)), MarketResponse.class).getBody();
    }

    private MarketResponse getMarket(String token, Long marketId) {
        return restTemplate.exchange(
                "/api/markets/" + marketId, HttpMethod.GET,
                new HttpEntity<>(bearer(token)), MarketResponse.class).getBody();
    }

    private ResponseEntity<BetResponse> placeBet(
            String token, Long marketId, Long outcomeId, BigDecimal amount, String key) {
        return restTemplate.exchange(
                "/api/markets/" + marketId + "/bets", HttpMethod.POST,
                new HttpEntity<>(new BetRequest(outcomeId, amount, key), bearer(token)),
                BetResponse.class);
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
