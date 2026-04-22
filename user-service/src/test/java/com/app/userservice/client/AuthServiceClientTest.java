package com.app.userservice.client;

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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceClient Tests")
class AuthServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    private AuthServiceClient authServiceClient;

    @BeforeEach
    void setUp() {
        authServiceClient = new AuthServiceClient(restTemplate);
        ReflectionTestUtils.setField(authServiceClient, "authServiceUrl", "http://auth-service:8080");
        ReflectionTestUtils.setField(authServiceClient, "authServiceCredentialsEndpoint", "/internal/credentials");
    }

    @Test
    @DisplayName("Should register credentials when auth-service returns 201")
    void registerCredentials_Success_WhenCreated() {
        when(restTemplate.postForEntity(
                eq("http://auth-service:8080/internal/credentials"),
                any(),
                eq(Void.class))
        ).thenReturn(ResponseEntity.status(HttpStatus.CREATED).build());

        assertThatCode(() -> authServiceClient.registerCredentials(
                UUID.randomUUID(), "user@example.com", "password123", "password"
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw retryable exception on unexpected status code")
    void registerCredentials_ThrowsRetryable_WhenUnexpectedStatus() {
        when(restTemplate.postForEntity(any(String.class), any(), eq(Void.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.ACCEPTED).build());

        assertThatThrownBy(() -> authServiceClient.registerCredentials(
                UUID.randomUUID(), "user@example.com", "password123", "password"
        )).isInstanceOf(RetryableAuthServiceException.class);
    }

    @Test
    @DisplayName("Should throw non-retryable exception on 4xx error")
    void registerCredentials_ThrowsNonRetryable_WhenClientError() {
        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                null,
                "invalid".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );
        when(restTemplate.postForEntity(any(String.class), any(), eq(Void.class))).thenThrow(exception);

        assertThatThrownBy(() -> authServiceClient.registerCredentials(
                UUID.randomUUID(), "user@example.com", "password123", "password"
        )).isInstanceOf(NonRetryableAuthServiceException.class);
    }

    @Test
    @DisplayName("Should throw retryable exception on 5xx error")
    void registerCredentials_ThrowsRetryable_WhenServerError() {
        HttpServerErrorException exception = HttpServerErrorException.create(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Service Unavailable",
                null,
                "downstream error".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );
        when(restTemplate.postForEntity(any(String.class), any(), eq(Void.class))).thenThrow(exception);

        assertThatThrownBy(() -> authServiceClient.registerCredentials(
                UUID.randomUUID(), "user@example.com", "password123", "password"
        )).isInstanceOf(RetryableAuthServiceException.class);
    }

    @Test
    @DisplayName("Should throw retryable exception on network errors")
    void registerCredentials_ThrowsRetryable_WhenNetworkError() {
        when(restTemplate.postForEntity(any(String.class), any(), eq(Void.class)))
                .thenThrow(new ResourceAccessException("Connection timeout"));

        assertThatThrownBy(() -> authServiceClient.registerCredentials(
                UUID.randomUUID(), "user@example.com", "password123", "password"
        )).isInstanceOf(RetryableAuthServiceException.class);
    }

    @Test
    @DisplayName("Should throw retryable exception on generic rest client errors")
    void registerCredentials_ThrowsRetryable_WhenRestClientError() {
        when(restTemplate.postForEntity(any(String.class), any(), eq(Void.class)))
                .thenThrow(new RestClientException("Unknown client error"));

        assertThatThrownBy(() -> authServiceClient.registerCredentials(
                UUID.randomUUID(), "user@example.com", "password123", "password"
        )).isInstanceOf(RetryableAuthServiceException.class);
    }

    @Test
    @DisplayName("Should throw auth service exception in recover method")
    void recoverRegisterCredentials_ThrowsAuthServiceException() {
        RetryableAuthServiceException cause = new RetryableAuthServiceException("Retry exhausted");

        assertThatThrownBy(() -> authServiceClient.recoverRegisterCredentials(
                cause,
                UUID.randomUUID(),
                "user@example.com",
                "password123",
                "password"
        )).isInstanceOf(AuthServiceException.class);
    }
}
