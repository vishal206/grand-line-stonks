package com.grandlinestonks.transfer;

import static org.assertj.core.api.Assertions.assertThat;

import com.grandlinestonks.TestcontainersConfiguration;
import com.grandlinestonks.auth.dto.AuthResponse;
import com.grandlinestonks.auth.dto.MeResponse;
import com.grandlinestonks.auth.dto.SignupRequest;
import com.grandlinestonks.transfer.dto.TransferRequest;
import com.grandlinestonks.transfer.dto.TransferResponse;
import java.math.BigDecimal;
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
class TransferApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void transferIsIdempotentAcrossRetries() {
        String sender = "sender_" + UUID.randomUUID().toString().substring(0, 8);
        String receiver = "receiver_" + UUID.randomUUID().toString().substring(0, 8);
        String senderToken = signup(sender);
        String receiverToken = signup(receiver);
        String key = UUID.randomUUID().toString();
        TransferRequest request = new TransferRequest(receiver, new BigDecimal("250.0000"), key);

        ResponseEntity<TransferResponse> first = restTemplate.exchange(
                "/api/transfers", HttpMethod.POST,
                new HttpEntity<>(request, bearer(senderToken)), TransferResponse.class);
        ResponseEntity<TransferResponse> retry = restTemplate.exchange(
                "/api/transfers", HttpMethod.POST,
                new HttpEntity<>(request, bearer(senderToken)), TransferResponse.class);

        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(retry.getBody().transactionId()).isEqualTo(first.getBody().transactionId());
        assertThat(me(senderToken).balance()).isEqualByComparingTo("9750.0000");
        assertThat(me(receiverToken).balance()).isEqualByComparingTo("10250.0000");
    }

    @Test
    void transferBeyondBalanceIsRejected() {
        String sender = "sender_" + UUID.randomUUID().toString().substring(0, 8);
        String receiver = "receiver_" + UUID.randomUUID().toString().substring(0, 8);
        String senderToken = signup(sender);
        signup(receiver);
        TransferRequest request = new TransferRequest(
                receiver, new BigDecimal("10000.0001"), UUID.randomUUID().toString());

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/transfers", HttpMethod.POST,
                new HttpEntity<>(request, bearer(senderToken)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(me(senderToken).balance()).isEqualByComparingTo("10000.0000");
    }

    @Test
    void selfTransferIsRejected() {
        String sender = "sender_" + UUID.randomUUID().toString().substring(0, 8);
        String senderToken = signup(sender);
        TransferRequest request = new TransferRequest(
                sender, new BigDecimal("1.0000"), UUID.randomUUID().toString());

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/transfers", HttpMethod.POST,
                new HttpEntity<>(request, bearer(senderToken)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private String signup(String username) {
        return restTemplate.postForEntity(
                        "/api/auth/signup", new SignupRequest(username, "password123"), AuthResponse.class)
                .getBody().token();
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
