package com.app.userservice.exception;

/**
 * NotificationException
 * ─────────────────────────────────────────────────────────────
 * Excepción lanzada cuando el gateway de notificaciones falla.
 *
 * Por qué una excepción propia:
 *   Permite que quien llame capture específicamente errores de
 *   notificaciones y decida si reintenta, loguea o ignora,
 *   sin mezclarlo con otras RuntimeException del sistema.
 *
 * Ejemplo de manejo:
 *   try {
 *       notificationClient.sendWelcome(to, username);
 *   } catch (NotificationException e) {
 *       log.warn("No se pudo enviar el email de bienvenida, continuando...", e);
 *       // El registro del usuario continúa aunque el email falle
 *   }
 */
public class NotificationException extends RuntimeException {

    public NotificationException(String message) {
        super(message);
    }

    public NotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
