package com.grandlinestonks.portfolio;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.grandlinestonks.TestcontainersConfiguration;
import com.grandlinestonks.auth.dto.AuthResponse;
import com.grandlinestonks.auth.dto.SignupRequest;
import com.grandlinestonks.market.dto.BetRequest;
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
class PortfolioApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void positionsAndTransactionsArePaginated() {
        String token = signup();
        MarketResponse market = openMarket(token);
        for (int i = 0; i < 3; i++) {
            bet(token, market.id(), market.outcomes().get(i % 2).id(), new BigDecimal("10.0000"));
        }

        JsonNode positions = getJson(token, "/api/me/positions?page=0&size=1");
        assertThat(positions.get("content")).hasSize(1);
        assertThat(positions.get("page").get("totalElements").asLong()).isEqualTo(2);
        assertThat(positions.get("content").get(0).get("question").asText())
                .isEqualTo(market.question());

        JsonNode transactions = getJson(token, "/api/me/transactions?page=0&size=2");
        assertThat(transactions.get("content")).hasSize(2);
        assertThat(transactions.get("page").get("totalElements").asLong()).isEqualTo(4);
        assertThat(transactions.get("content").get(0).get("type").asText()).isEqualTo("BET");
    }

    @Test
    void marketListIsPaginated() {
        String token = signup();
        for (int i = 0; i < 3; i++) {
            openMarket(token);
        }
        JsonNode page = getJson(token, "/api/markets?status=OPEN&page=0&size=2");
        assertThat(page.get("content").size()).isEqualTo(2);
        assertThat(page.get("page").get("totalElements").asLong()).isGreaterThanOrEqualTo(3);
    }

    @Test
    void openApiDocsAreServedWithoutAuth() {
        ResponseEntity<String> response = restTemplate.getForEntity("/v3/api-docs", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("/api/markets/{id}/bets");
    }

    private String signup() {
        String username = "folio_" + UUID.randomUUID().toString().substring(0, 12).replace("-", "");
        return restTemplate.postForEntity(
                        "/api/auth/signup", new SignupRequest(username, "password123"), AuthResponse.class)
                .getBody().token();
    }

    private MarketResponse openMarket(String token) {
        MarketResponse market = restTemplate.exchange(
                "/api/markets", HttpMethod.POST,
                new HttpEntity<>(new CreateMarketRequest(
                        "Will the next island have a coating engineer?",
                        List.of("Yes", "No"), new BigDecimal("100")), bearer(token)),
                MarketResponse.class).getBody();
        return restTemplate.exchange(
                "/api/markets/" + market.id() + "/open", HttpMethod.POST,
                new HttpEntity<>(bearer(token)), MarketResponse.class).getBody();
    }

    private void bet(String token, Long marketId, Long outcomeId, BigDecimal amount) {
        restTemplate.exchange(
                "/api/markets/" + marketId + "/bets", HttpMethod.POST,
                new HttpEntity<>(new BetRequest(outcomeId, amount, UUID.randomUUID().toString()),
                        bearer(token)),
                String.class);
    }

    private JsonNode getJson(String token, String path) {
        return restTemplate.exchange(
                path, HttpMethod.GET, new HttpEntity<>(bearer(token)), JsonNode.class).getBody();
    }

    private HttpHeaders bearer(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
