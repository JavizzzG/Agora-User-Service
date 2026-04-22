package com.app.userservice.client;

import com.app.userservice.dto.AuthenticateCredentialsResponse;
import com.app.userservice.exception.AuthServiceException;
import com.app.userservice.exception.NonRetryableAuthServiceException;
import com.app.userservice.exception.RetryableAuthServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticateService Tests")
class AuthenticateServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private AuthenticateService authenticateService;

    @BeforeEach
    void setUp() {
        authenticateService = new AuthenticateService(restTemplate);
        ReflectionTestUtils.setField(authenticateService, "authServiceUrl", "http://auth-service:8080");
        ReflectionTestUtils.setField(authenticateService, "authServiceAuthenticateService", "/internal/service/token");
        ReflectionTestUtils.setField(authenticateService, "authServiceCredentials", "user_service:secret123");
    }

    @Test
    @DisplayName("Should authenticate service when auth-service returns 200")
    void authService_ReturnsToken_WhenSuccess() {
        AuthenticateCredentialsResponse tokenResponse = AuthenticateCredentialsResponse.builder()
                .accessToken("service-token")
                .expiresIn("3600")
                .tokenType("Bearer")
                .build();

        when(restTemplate.postForEntity(
                eq("http://auth-service:8080/internal/service/token"),
                any(),
                eq(AuthenticateCredentialsResponse.class))
        ).thenReturn(ResponseEntity.ok(tokenResponse));

        ResponseEntity<AuthenticateCredentialsResponse> response = authenticateService.authService();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccessToken()).isEqualTo("service-token");
    }

    @Test
    @DisplayName("Should throw retryable exception on unexpected status")
    void authService_ThrowsRetryable_WhenUnexpectedStatus() {
        when(restTemplate.postForEntity(any(String.class), any(), eq(AuthenticateCredentialsResponse.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).build());

        assertThatThrownBy(() -> authenticateService.authService())
                .isInstanceOf(RetryableAuthServiceException.class);
    }

    @Test
    @DisplayName("Should throw auth service exception when credentials format is invalid")
    void authService_ThrowsAuthServiceException_WhenInvalidCredentialsFormat() {
        ReflectionTestUtils.setField(authenticateService, "authServiceCredentials", "invalid-format");

        assertThatThrownBy(() -> authenticateService.authService())
                .isInstanceOf(AuthServiceException.class)
                .hasMessageContaining("Invalid service credentials format");
    }

    @Test
    @DisplayName("Should throw non-retryable exception on 4xx error")
    void authService_ThrowsNonRetryable_WhenClientError() {
        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                null,
                "invalid credentials".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );
        when(restTemplate.postForEntity(any(String.class), any(), eq(AuthenticateCredentialsResponse.class)))
                .thenThrow(exception);

        assertThatThrownBy(() -> authenticateService.authService())
                .isInstanceOf(NonRetryableAuthServiceException.class);
    }

    @Test
    @DisplayName("Should throw retryable exception on 5xx error")
    void authService_ThrowsRetryable_WhenServerError() {
        HttpServerErrorException exception = HttpServerErrorException.create(
                HttpStatus.BAD_GATEWAY,
                "Bad Gateway",
                null,
                "auth service down".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );
        when(restTemplate.postForEntity(any(String.class), any(), eq(AuthenticateCredentialsResponse.class)))
                .thenThrow(exception);

        assertThatThrownBy(() -> authenticateService.authService())
                .isInstanceOf(RetryableAuthServiceException.class);
    }

    @Test
    @DisplayName("Should throw retryable exception on network error")
    void authService_ThrowsRetryable_WhenNetworkError() {
        when(restTemplate.postForEntity(any(String.class), any(), eq(AuthenticateCredentialsResponse.class)))
                .thenThrow(new ResourceAccessException("Connection timeout"));

        assertThatThrownBy(() -> authenticateService.authService())
                .isInstanceOf(RetryableAuthServiceException.class);
    }

    @Test
    @DisplayName("Should throw retryable exception on generic rest error")
    void authService_ThrowsRetryable_WhenRestClientError() {
        when(restTemplate.postForEntity(any(String.class), any(), eq(AuthenticateCredentialsResponse.class)))
                .thenThrow(new RestClientException("Unknown error"));

        assertThatThrownBy(() -> authenticateService.authService())
                .isInstanceOf(RetryableAuthServiceException.class);
    }

    @Test
    @DisplayName("Should throw auth service exception in recover method")
    void recoverAuthService_ThrowsAuthServiceException() {
        RetryableAuthServiceException cause = new RetryableAuthServiceException("Retries exhausted");

        assertThatThrownBy(() -> authenticateService.recoverAuthService(cause))
                .isInstanceOf(AuthServiceException.class);
    }
}
