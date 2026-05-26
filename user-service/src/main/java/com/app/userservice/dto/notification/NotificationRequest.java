package com.app.userservice.dto.notification;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * NotificationRequest
 * ─────────────────────────────────────────────────────────────
 * DTO que representa el body del POST /notifications/send.
 *
 * El campo `type` determina qué template usa el gateway y qué
 * campos son obligatorios. Jackson serializa esto a JSON.
 *
 * @JsonInclude(NON_NULL): los campos null no se incluyen en el JSON,
 * así el gateway solo recibe los campos relevantes para cada tipo.
 *
 * Uso con los factory methods (recomendado):
 *   NotificationRequest req = NotificationRequest.welcome("user@mail.com", "Juan");
 *
 * Uso manual:
 *   NotificationRequest req = new NotificationRequest();
 *   req.setType("welcome");
 *   req.setTo("user@mail.com");
 *   req.setUsername("Juan");
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationRequest {

    // Tipo de notificación — determina el template y los campos requeridos
    private String type;

    // Campo presente en TODOS los tipos
    private String to;

    // Campos opcionales según el tipo
    private String username;

    @JsonProperty("recovery_token")
    private String recoveryToken;

    @JsonProperty("verification_token")
    private String verificationToken;

    @JsonProperty("expires_in_minutes")
    private Integer expiresInMinutes;

    private String ip;
    private String device;
    private String timestamp;
    private String reason;

    // ─── Constructores ───────────────────────────────────────────────────────────

    public NotificationRequest() {}

    // ─── Factory methods (uno por tipo de notificación) ───────────────────────
    // Garantizan que siempre se pasen los campos correctos para cada tipo.

    public static NotificationRequest welcome(String to, String username) {
        NotificationRequest r = new NotificationRequest();
        r.type     = "welcome";
        r.to       = to;
        r.username = username;
        return r;
    }

    public static NotificationRequest passwordRecovery(String to, String username, String recoveryToken) {
        NotificationRequest r = new NotificationRequest();
        r.type          = "password-recovery";
        r.to            = to;
        r.username      = username;
        r.recoveryToken = recoveryToken;
        return r;
    }

    public static NotificationRequest emailVerification(String to, String username, String verificationToken) {
        NotificationRequest r = new NotificationRequest();
        r.type                = "email-verification";
        r.to                  = to;
        r.username            = username;
        r.verificationToken   = verificationToken;
        r.expiresInMinutes    = 60; // Default
        return r;
    }

    public static NotificationRequest emailVerification(
            String to, String username, String verificationToken, int expiresInMinutes) {
        NotificationRequest r = emailVerification(to, username, verificationToken);
        r.expiresInMinutes = expiresInMinutes;
        return r;
    }

    public static NotificationRequest loginAlert(String to, String username, String ip, String device) {
        NotificationRequest r = new NotificationRequest();
        r.type      = "login-alert";
        r.to        = to;
        r.username  = username;
        r.ip        = ip;
        r.device    = device;
        r.timestamp = Instant.now().toString(); // ISO 8601 automático
        return r;
    }

    public static NotificationRequest accountDeactivated(String to, String username, String reason) {
        NotificationRequest r = new NotificationRequest();
        r.type     = "account-deactivated";
        r.to       = to;
        r.username = username;
        r.reason   = reason;
        return r;
    }

    // ─── Getters y Setters ───────────────────────────────────────────────────────

    public String getType()                  { return type; }
    public void setType(String type)         { this.type = type; }

    public String getTo()                    { return to; }
    public void setTo(String to)             { this.to = to; }

    public String getUsername()              { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRecoveryToken()                     { return recoveryToken; }
    public void setRecoveryToken(String recoveryToken)   { this.recoveryToken = recoveryToken; }

    public String getVerificationToken()                        { return verificationToken; }
    public void setVerificationToken(String verificationToken)  { this.verificationToken = verificationToken; }

    public Integer getExpiresInMinutes()                        { return expiresInMinutes; }
    public void setExpiresInMinutes(Integer expiresInMinutes)   { this.expiresInMinutes = expiresInMinutes; }

    public String getIp()                { return ip; }
    public void setIp(String ip)         { this.ip = ip; }

    public String getDevice()                { return device; }
    public void setDevice(String device)     { this.device = device; }

    public String getTimestamp()                 { return timestamp; }
    public void setTimestamp(String timestamp)   { this.timestamp = timestamp; }

    public String getReason()                { return reason; }
    public void setReason(String reason)     { this.reason = reason; }
}