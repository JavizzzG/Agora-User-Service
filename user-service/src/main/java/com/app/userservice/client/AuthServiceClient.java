package com.app.userservice.client;

import com.app.userservice.dto.AuthCredentialsRequest;
import com.app.userservice.exception.AuthServiceException;
import com.app.userservice.exception.NonRetryableAuthServiceException;
import com.app.userservice.exception.RetryableAuthServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
@Slf4j
public class AuthServiceClient {

    private final RestTemplate restTemplate;

    // Inyectar el RestTemplate específico para auth-service
    public AuthServiceClient(@Qualifier("authServiceRestTemplate") RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }

    @Value("${auth.service.url}")
    private String authServiceUrl;

    @Value("${auth.service.credentials.endpoint}")
    private String authServiceCredentialsEndpoint;

    @Retryable(
            retryFor = RetryableAuthServiceException.class,
            maxAttemptsExpression = "${auth.service.retry.max-attempts:3}",
            backoff = @Backoff(
                    delayExpression = "${auth.service.retry.initial-delay-ms:300}",
                    multiplierExpression = "${auth.service.retry.multiplier:2.0}",
                    maxDelayExpression = "${auth.service.retry.max-delay-ms:2000}"
            )
    )
    public void registerCredentials(UUID user_id, String identifier, String password, String credential_type){
        log.info("Registering credentials for user identified by: {}", identifier);

        AuthCredentialsRequest request = AuthCredentialsRequest.builder()
                .user_id(user_id)
                .identifier(identifier)
                .password(password)
                .credential_type(credential_type)
                .build();

        String url = authServiceUrl + authServiceCredentialsEndpoint;

        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(
                    url,
                    request,
                    Void.class
            );

            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED){
                log.info("Successfully registered credentials for user identified by: {}", identifier);
            }else{
                log.error("Error response from auth service: {}", response.getStatusCode());
                throw new RetryableAuthServiceException(
                        "Failed to register credentials: unexpected status " + response.getStatusCode()
                );
            }

        } catch (HttpClientErrorException e){
            log.error("Client error registering credentials for user identified by {}: {} - {}", identifier, e.getStatusCode(), e.getResponseBodyAsString());
            throw new NonRetryableAuthServiceException("Auth service rejected credentials: " + e.getMessage(), e);

        } catch (HttpServerErrorException e){
            log.error("Server error registering credentials for user identified by {}: {} - {}", identifier, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RetryableAuthServiceException("Auth service error: " + e.getMessage(), e);

        } catch (ResourceAccessException e){
            log.error("Network error registering credentials for user identified by {}: {}", identifier, e.getMessage());
            throw new RetryableAuthServiceException("Failed to reach auth service: " + e.getMessage(), e);

        } catch (RestClientException e){
            log.error("Error communicating with auth service for user identified by {}: {}", identifier, e.getMessage());
            throw new RetryableAuthServiceException("Failed to communicate with auth service: " + e.getMessage(), e);
        }
    }

    @Recover
    public void recoverRegisterCredentials(
            RetryableAuthServiceException ex,
            UUID userId,
            String identifier,
            String password,
            String credentialType
    ) {
        throw new AuthServiceException(
                "Failed to register credentials in auth-service after retries for identifier: " + identifier,
                ex
        );
    }

}
