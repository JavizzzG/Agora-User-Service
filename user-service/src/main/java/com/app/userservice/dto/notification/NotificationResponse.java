package com.app.userservice.dto.notification;

/**
 * NotificationResponse
 * ─────────────────────────────────────────────────────────────
 * Mapea la respuesta JSON del gateway:
 *   { "success": true, "message": "Notificación enviada a ..." }
 */
public class NotificationResponse {

    private boolean success;
    private String message;

    public NotificationResponse() {}

    public boolean isSuccess()           { return success; }
    public void setSuccess(boolean s)    { this.success = s; }

    public String getMessage()           { return message; }
    public void setMessage(String m)     { this.message = m; }

    @Override
    public String toString() {
        return "NotificationResponse{success=" + success + ", message='" + message + "'}";
    }
}