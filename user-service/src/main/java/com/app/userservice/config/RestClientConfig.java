package com.app.userservice.config;

import com.app.userservice.security.ServiceTokenManager;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    private final ServiceTokenManager tokenManager;

    /**
     * RestTemplate GENERAL (sin token).
     *
     * Usado para:
     * - Llamadas externas que no requieren auth
     * - Futuros clientes HTTP
     */
    @Bean(name = "restTemplate")
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * RestTemplate para AUTH-SERVICE (con token automático).
     *
     * Usado solo en AuthServiceClient.
     */
    @Bean(name = "authServiceRestTemplate")
    public RestTemplate authServiceRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .interceptors(Collections.singletonList(
                        new ServiceTokenInterceptor(tokenManager)
                ))
                .build();
    }

    /**
     * Interceptor que agrega SERVICE_TOKEN automáticamente.
     */
    private static class ServiceTokenInterceptor implements ClientHttpRequestInterceptor {

        private final ServiceTokenManager tokenManager;

        public ServiceTokenInterceptor(ServiceTokenManager tokenManager) {
            this.tokenManager = tokenManager;
        }

        @Override
        public ClientHttpResponse intercept(
                HttpRequest request,
                byte[] body,
                ClientHttpRequestExecution execution
        ) throws IOException {

            String token = tokenManager.getToken();

            if (token != null && !token.isEmpty()) {
                request.getHeaders().setBearerAuth(token);
            }

            return execution.execute(request, body);
        }
    }
}
