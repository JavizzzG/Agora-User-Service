package com.app.userservice.client;

import com.app.userservice.dto.AuthenticateCredentialsRequest;
import com.app.userservice.dto.AuthenticateCredentialsResponse;
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

@Component
@Slf4j
public class AuthenticateService {

    private final RestTemplate restTemplate;

    public AuthenticateService(@Qualifier("restTemplate") RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }

    @Value("${auth.service.url}")
    private String authServiceUrl;

    @Value("${auth.service.authenticate.service}")
    private String authServiceAuthenticateService;

    @Value("${auth.service.credentials}")
    private String authServiceCredentials;

    @Retryable(
            retryFor = RetryableAuthServiceException.class,
            maxAttemptsExpression = "${auth.service.retry.max-attempts:3}",
            backoff = @Backoff(
                    delayExpression = "${auth.service.retry.initial-delay-ms:300}",
                    multiplierExpression = "${auth.service.retry.multiplier:2.0}",
                    maxDelayExpression = "${auth.service.retry.max-delay-ms:2000}"
            )
    )
    public ResponseEntity<AuthenticateCredentialsResponse> authService(){
        log.info("Doing the authentication of user service");

        String[] serviceArray = authServiceCredentials.split(":");
        if (serviceArray.length != 2) {
            log.error("Invalid service credentials format");
            throw new AuthServiceException("Invalid service credentials format");
        }

        String service_id = serviceArray[0];
        String service_secret = serviceArray[1];

        AuthenticateCredentialsRequest request = AuthenticateCredentialsRequest.builder()
                .service_id(service_id)
                .service_secret(service_secret)
                .build();

        String url = authServiceUrl + authServiceAuthenticateService;

        try {
            ResponseEntity<AuthenticateCredentialsResponse> response = restTemplate.postForEntity(
                    url,
                    request,
                    AuthenticateCredentialsResponse.class
                    // access_token -> String
                    // expires_in -> int
                    // token_type -> String
            );

            if (response.getStatusCode() == HttpStatus.OK){
                log.info("Successfully authenticated service");
                return response;
            }else{
                log.error("Error response from auth service: {}", response.getStatusCode());
                throw new RetryableAuthServiceException("Failed to connect: unexpected status " + response.getStatusCode());
            }

        } catch (HttpClientErrorException e){
            log.error("Client error registering the connection: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new NonRetryableAuthServiceException("Auth service rejected connection: " + e.getMessage(), e);

        } catch (HttpServerErrorException e){
            log.error("Server error authenticating service: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RetryableAuthServiceException("Auth service error: " + e.getMessage(), e);

        } catch (ResourceAccessException e){
            log.error("Network error authenticating service: {}", e.getMessage());
            throw new RetryableAuthServiceException("Failed to reach auth service: " + e.getMessage(), e);

        } catch (RestClientException e){
            log.error("Error communicating with auth service for user service: {}", e.getMessage());
            throw new RetryableAuthServiceException("Failed to communicate with auth service: " + e.getMessage(), e);
        }
    }

    @Recover
    public ResponseEntity<AuthenticateCredentialsResponse> recoverAuthService(RetryableAuthServiceException ex) {
        throw new AuthServiceException("Unable to authenticate user-service against auth-service after retries", ex);
    }

}
