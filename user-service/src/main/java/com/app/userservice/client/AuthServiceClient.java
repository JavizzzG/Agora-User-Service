package com.app.userservice.client;

import com.app.userservice.dto.AuthCredentialsRequest;
import com.app.userservice.exception.AuthServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthServiceClient {

    private final RestTemplate restTemplate;

    @Value("${auth.service.url}")
    private String authServiceUrl;

    @Value("${auth.service.credentials.endpoint}")
    private String authServiceCredentialsEndpoint;

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
                throw new AuthServiceException("Failed to register credentials: unexpected status " + response.getStatusCode());
            }

        } catch (HttpClientErrorException e){
            log.error("Client error registering credentials for user identified by {}: {} - {}", identifier, e.getStatusCode(), e.getResponseBodyAsString());
            throw new AuthServiceException("Auth service rejected credentials: " + e.getMessage(), e);

        } catch (HttpServerErrorException e){
            log.error("Server error registering credentials for user identified by {}: {} - {}", identifier, e.getStatusCode(), e.getResponseBodyAsString());
            throw new AuthServiceException("Auth service error: " + e.getMessage(), e);

        } catch (Exception e){
            log.error("Error communicating with auth service for user identified by {}: {}", identifier, e.getMessage());
            throw new AuthServiceException("Failed to communicate with auth service: " + e.getMessage(), e);
        }
    }


}
