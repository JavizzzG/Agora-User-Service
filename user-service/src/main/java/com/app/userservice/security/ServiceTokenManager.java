package com.app.userservice.security;

import com.app.userservice.client.AuthenticateService;
import com.app.userservice.dto.AuthenticateCredentialsResponse;
import com.app.userservice.exception.AuthServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Gestor centralizado de tokens de servicio.
 *
 * RESPONSABILIDADES:
 * 1. Obtener token de auth-service al iniciar
 * 2. Almacenar token de forma segura en memoria
 * 3. Renovar token automáticamente antes de expirar
 * 4. Proveer token válido a otros componentes
 *
 * SEGURIDAD:
 * - Token en MEMORIA (no en disco)
 * - Thread-safe (ReadWriteLock)
 * - Auto-renovación (Scheduled task)
 *
 * USO:
 * @Autowired
 * private ServiceTokenManager tokenManager;
 *
 * String token = tokenManager.getToken();
 */
@Component
@Slf4j
public class ServiceTokenManager {

    private final AuthenticateService authenticateService;

    // ALMACENAMIENTO DEL TOKEN (en memoria)
    private volatile String currentToken;
    // ↑ volatile: garantiza visibilidad entre threads

    private volatile Instant tokenExpiresAt;
    // ↑ Cuándo expira el token

    // THREAD SAFETY: ReadWriteLock
    // Permite múltiples lecturas simultáneas pero solo una escritura
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public ServiceTokenManager(@Lazy AuthenticateService authenticateService) {
        this.authenticateService = authenticateService;
    }

    /**
     * Inicializar: obtener token al arrancar la aplicación.
     *
     * @PostConstruct: Se ejecuta DESPUÉS de inyectar dependencias,
     * ANTES de que la app esté lista para recibir requests.
     *
     * Si falla aquí, la app NO arranca (fail-fast).
     */
    @PostConstruct
    public void initialize() {
        log.info("Initializing ServiceTokenManager...");

        try {
            refreshToken();
            log.info("ServiceTokenManager initialized successfully");

        } catch (Exception e) {
            log.error("CRITICAL: Failed to obtain initial service token. Application may not function correctly.", e);
            // Decisión: ¿Dejar que la app arranque sin token o fallar?
            // Opción A: Lanzar exception (app no arranca)
            // throw new RuntimeException("Failed to initialize service token", e);

            // Opción B: Continuar (app arranca, pero requests fallarán)
            // (actual)
        }
    }

    /**
     * Renovar token automáticamente cada 50 minutos.
     *
     * @Scheduled: Spring ejecuta esto automáticamente
     *
     * POR QUÉ 50 MINUTOS:
     * - Token expira en 60 minutos (3600 segundos)
     * - Renovamos 10 minutos ANTES (buffer de seguridad)
     * - Si la renovación falla, aún tenemos 10 min para reintentar
     *
     * ALTERNATIVA INTELIGENTE:
     * Calcular basado en expires_in dinámicamente
     */
    @Scheduled(fixedRate = 50 * 60 * 1000)  // 50 minutos en milisegundos
    // ↑ fixedRate: ejecuta cada X tiempo desde el INICIO de la ejecución anterior
    public void renewTokenScheduled() {
        log.info("Scheduled token renewal triggered");

        try {
            refreshToken();

        } catch (Exception e) {
            log.error("Failed to renew service token. Will retry in next cycle.", e);
            // No lanzar exception: el scheduler debe continuar
        }
    }

    /**
     * Obtener el token actual.
     *
     * THREAD-SAFE: Usa ReadLock (múltiples threads pueden leer simultáneamente)
     *
     * @return Token JWT válido
     */
    public String getToken() {
        boolean shouldRefresh;

        lock.readLock().lock();
        try {
            shouldRefresh = currentToken == null || tokenExpiresAt == null || tokenExpiresAt.isBefore(Instant.now().plusSeconds(5 * 60));

        } finally {
            lock.readLock().unlock();
        }

        if (shouldRefresh) {
            log.warn("Service token is missing or close to expiration, refreshing now");
            refreshToken();
        }

        lock.readLock().lock();
        try {
            if (currentToken == null || currentToken.isBlank()) {
                throw new AuthServiceException("Service token is not available");
            }
            return currentToken;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Refrescar token llamando a auth-service.
     *
     * THREAD-SAFE: Usa WriteLock (solo un thread puede escribir)
     */
    private void refreshToken() {
        log.info("Refreshing service token...");

        lock.writeLock().lock();  // ← Adquirir lock de escritura
        try {
            // Llamar a auth-service
            ResponseEntity<AuthenticateCredentialsResponse> response =
                    authenticateService.authService();

            if (response.getBody() == null) {
                throw new AuthServiceException("Auth service returned empty response");
            }

            AuthenticateCredentialsResponse tokenResponse = response.getBody();

            // Validar respuesta
            if (tokenResponse.getAccessToken() == null || tokenResponse.getAccessToken().isEmpty()) {
                throw new AuthServiceException("Auth service returned invalid token");
            }

            // Guardar token
            this.currentToken = tokenResponse.getAccessToken();

            // Calcular cuándo expira
            int expiresInSeconds = parseExpiresIn(tokenResponse.getExpiresIn());
            this.tokenExpiresAt = Instant.now().plusSeconds(expiresInSeconds);

            log.info("Service token refreshed successfully. Expires at: {}", tokenExpiresAt);

        } finally {
            lock.writeLock().unlock();  // ← SIEMPRE liberar lock
        }
    }

    /**
     * Parsear expires_in (puede ser String o int según auth-service).
     */
    private int parseExpiresIn(String expiresIn) {
        try {
            return Integer.parseInt(expiresIn);
        } catch (RuntimeException e) {
            log.warn("Failed to parse expires_in: '{}', using default 3600", expiresIn);
            return 3600;  // Default: 1 hora
        }
    }

    /**
     * Verificar si hay un token válido disponible.
     *
     * Útil para health checks.
     */
    public boolean hasValidToken() {
        lock.readLock().lock();
        try {
            if (currentToken == null || tokenExpiresAt == null) {
                return false;
            }

            // Verificar que no haya expirado
            return tokenExpiresAt.isAfter(Instant.now());

        } finally {
            lock.readLock().unlock();
        }
    }
}
