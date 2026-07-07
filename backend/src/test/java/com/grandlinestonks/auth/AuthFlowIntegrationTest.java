package com.grandlinestonks.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.grandlinestonks.TestcontainersConfiguration;
import com.grandlinestonks.auth.dto.AuthResponse;
import com.grandlinestonks.auth.dto.LoginRequest;
import com.grandlinestonks.auth.dto.MeResponse;
import com.grandlinestonks.auth.dto.SignupRequest;
import com.grandlinestonks.common.Berries;
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
class AuthFlowIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void signupLoginAndMeFlow() {
        ResponseEntity<AuthResponse> signup = restTemplate.postForEntity(
                "/api/auth/signup", new SignupRequest("luffy", "gomugomu123"), AuthResponse.class);
        assertThat(signup.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(signup.getBody().token()).isNotBlank();

        ResponseEntity<AuthResponse> login = restTemplate.postForEntity(
                "/api/auth/login", new LoginRequest("luffy", "gomugomu123"), AuthResponse.class);
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<MeResponse> me = restTemplate.exchange(
                "/api/me", HttpMethod.GET, withBearer(login.getBody().token()), MeResponse.class);
        assertThat(me.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(me.getBody().username()).isEqualTo("luffy");
        assertThat(me.getBody().balance()).isEqualByComparingTo(Berries.STARTING_BERRIES);
    }

    @Test
    void duplicateSignupIsRejected() {
        restTemplate.postForEntity(
                "/api/auth/signup", new SignupRequest("zoro", "santoryu123"), AuthResponse.class);
        ResponseEntity<String> second = restTemplate.postForEntity(
                "/api/auth/signup", new SignupRequest("zoro", "santoryu123"), String.class);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void wrongPasswordIsRejected() {
        restTemplate.postForEntity(
                "/api/auth/signup", new SignupRequest("nami", "mikan12345"), AuthResponse.class);
        ResponseEntity<String> login = restTemplate.postForEntity(
                "/api/auth/login", new LoginRequest("nami", "wrongpassword"), String.class);
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void meWithoutTokenIsUnauthorized() {
        ResponseEntity<String> me = restTemplate.getForEntity("/api/me", String.class);
        assertThat(me.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void invalidSignupInputIsBadRequest() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/signup", new SignupRequest("x", "short"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private HttpEntity<Void> withBearer(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(headers);
    }
}
