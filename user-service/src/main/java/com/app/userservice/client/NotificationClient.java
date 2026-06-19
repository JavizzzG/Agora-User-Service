package com.app.userservice.client;

import com.app.userservice.dto.notification.NotificationRequest;
import com.app.userservice.dto.notification.NotificationResponse;
import com.app.userservice.exception.NotificationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * NotificationClient
 * ─────────────────────────────────────────────────────────────
 * Cliente HTTP que se comunica con el Agora Notification Gateway.
 *
 * Por qué existe:
 *   Centraliza toda la lógica de llamado al servicio de notificaciones.
 *   Cualquier otro servicio que necesite enviar emails solo importa
 *   este cliente — no necesita conocer la URL ni armar el request.
 *
 * Cómo funciona:
 *   Usa el HttpClient nativo de Java 11+ (sin dependencias extra).
 *   Lee la URL base desde application.properties (o .env vía Docker).
 *   Lanza NotificationException si el gateway responde con error.
 *
 * Cómo inyectarlo:
 *   @Autowired
 *   private NotificationClient notificationClient;
 */
@Service
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    // URL base del notification gateway.
    // En Docker: http://notification-gateway:3002
    // En local:  http://localhost:3002
    // Se configura en application.properties como: notification.service.url
    @Value("${notification.service.url}")
    private String notificationServiceUrl;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public NotificationClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        // HttpClient con timeout de 10 segundos por request
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    // ─── Método principal ───────────────────────────────────────────────────────

    /**
     * Envía cualquier tipo de notificación al gateway.
     *
     * @param request Objeto con `type` y los campos del email (to, username, etc.)
     * @throws NotificationException si el gateway responde con error o hay fallo de red
     */
    public NotificationResponse send(NotificationRequest request) {
        try {
            String body = objectMapper.writeValueAsString(request);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(notificationServiceUrl + "/notifications/" + request.getType()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    httpRequest,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("[NotificationClient] Notificación '{}' enviada a {}",
                        request.getType(), request.getTo());
                return objectMapper.readValue(response.body(), NotificationResponse.class);
            } else {
                log.error("[NotificationClient] Error del gateway: {} — {}",
                        response.statusCode(), response.body());
                throw new NotificationException(
                        "El gateway respondió con error " + response.statusCode() + ": " + response.body()
                );
            }

        } catch (NotificationException e) {
            throw e; // Re-lanzar sin envolver
        } catch (Exception e) {
            log.error("[NotificationClient] Fallo de red o serialización: {}", e.getMessage(), e);
            throw new NotificationException("No se pudo conectar al servicio de notificaciones: " + e.getMessage(), e);
        }
    }

    // ─── Métodos de conveniencia (uno por tipo de notificación) ────────────────
    // Evitan que quien llame tenga que construir el request a mano.

    /** Envía email de bienvenida al registrarse */
    public void sendWelcome(String to, String username) {
        send(NotificationRequest.welcome(to, username));
    }

    /** Envía email de recuperación de contraseña */
    public void sendPasswordRecovery(String to, String username, String recoveryToken) {
        send(NotificationRequest.passwordRecovery(to, username, recoveryToken));
    }

    /** Envía email de verificación de cuenta */
    public void sendEmailVerification(String to, String username, String verificationToken) {
        send(NotificationRequest.emailVerification(to, username, verificationToken));
    }

    /** Envía email de alerta de inicio de sesión */
    public void sendLoginAlert(String to, String username, String ip, String device) {
        send(NotificationRequest.loginAlert(to, username, ip, device));
    }

    /** Envía email de cuenta desactivada */
    public void sendAccountDeactivated(String to, String username, String reason) {
        send(NotificationRequest.accountDeactivated(to, username, reason));
    }

    @PostConstruct
    public void init() {
        log.info("[NotificationClient] URL configurada: '{}'", notificationServiceUrl);
    }
}

