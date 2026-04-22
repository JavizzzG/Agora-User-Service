package com.app.userservice.security;

import com.app.userservice.client.AuthenticateService;
import com.app.userservice.dto.AuthenticateCredentialsResponse;
import com.app.userservice.exception.AuthServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ServiceTokenManager Tests")
class ServiceTokenManagerTest {

    @Mock
    private AuthenticateService authenticateService;

    private ServiceTokenManager tokenManager;

    @BeforeEach
    void setUp() {
        tokenManager = new ServiceTokenManager(authenticateService);
    }

    @Test
    @DisplayName("Should return in-memory token when it is still valid")
    void getToken_ReturnsCurrentToken_WhenStillValid() {
        ReflectionTestUtils.setField(tokenManager, "currentToken", "cached-token");
        ReflectionTestUtils.setField(tokenManager, "tokenExpiresAt", Instant.now().plusSeconds(1200));

        String token = tokenManager.getToken();

        assertThat(token).isEqualTo("cached-token");
        verifyNoInteractions(authenticateService);
    }

    @Test
    @DisplayName("Should refresh token on demand when token is missing")
    void getToken_RefreshesWhenMissing() {
        when(authenticateService.authService()).thenReturn(successTokenResponse("new-token", "3600"));

        String token = tokenManager.getToken();

        assertThat(token).isEqualTo("new-token");
        verify(authenticateService).authService();
    }

    @Test
    @DisplayName("Should throw when auth-service returns empty response body")
    void getToken_Throws_WhenResponseBodyIsNull() {
        when(authenticateService.authService()).thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        assertThatThrownBy(() -> tokenManager.getToken())
                .isInstanceOf(AuthServiceException.class)
                .hasMessageContaining("empty response");
    }

    @Test
    @DisplayName("Should throw when auth-service returns blank token")
    void getToken_Throws_WhenTokenIsBlank() {
        AuthenticateCredentialsResponse body = AuthenticateCredentialsResponse.builder()
                .accessToken(" ")
                .expiresIn("3600")
                .tokenType("Bearer")
                .build();
        when(authenticateService.authService()).thenReturn(ResponseEntity.ok(body));

        assertThatThrownBy(() -> tokenManager.getToken())
                .isInstanceOf(AuthServiceException.class)
                .hasMessageContaining("Service token is not available");
    }

    @Test
    @DisplayName("Should default expires_in to 3600 when response is invalid")
    void getToken_UsesDefaultExpiry_WhenExpiresInIsInvalid() {
        when(authenticateService.authService()).thenReturn(successTokenResponse("token", "invalid-value"));

        tokenManager.getToken();

        Instant tokenExpiresAt = (Instant) ReflectionTestUtils.getField(tokenManager, "tokenExpiresAt");
        assertThat(tokenExpiresAt).isNotNull();
        assertThat(tokenExpiresAt).isAfter(Instant.now().plusSeconds(3500));
    }

    @Test
    @DisplayName("Should return false in hasValidToken when token is absent")
    void hasValidToken_ReturnsFalse_WhenNoToken() {
        assertThat(tokenManager.hasValidToken()).isFalse();
    }

    @Test
    @DisplayName("Should return true in hasValidToken when token is valid")
    void hasValidToken_ReturnsTrue_WhenTokenIsValid() {
        ReflectionTestUtils.setField(tokenManager, "currentToken", "cached-token");
        ReflectionTestUtils.setField(tokenManager, "tokenExpiresAt", Instant.now().plusSeconds(300));

        assertThat(tokenManager.hasValidToken()).isTrue();
    }

    @Test
    @DisplayName("Initialize should not throw even if refresh fails")
    void initialize_DoesNotThrow_WhenRefreshFails() {
        when(authenticateService.authService()).thenThrow(new RuntimeException("Auth service down"));

        assertThatCode(() -> tokenManager.initialize()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Scheduled renew should not throw even if refresh fails")
    void renewTokenScheduled_DoesNotThrow_WhenRefreshFails() {
        when(authenticateService.authService()).thenThrow(new RuntimeException("Auth service down"));

        assertThatCode(() -> tokenManager.renewTokenScheduled()).doesNotThrowAnyException();
    }

    private ResponseEntity<AuthenticateCredentialsResponse> successTokenResponse(String token, String expiresIn) {
        AuthenticateCredentialsResponse body = AuthenticateCredentialsResponse.builder()
                .accessToken(token)
                .expiresIn(expiresIn)
                .tokenType("Bearer")
                .build();
        return ResponseEntity.ok(body);
    }
}
