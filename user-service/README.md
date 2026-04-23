# User Service

Microservicio de usuarios construido con Spring Boot 3.2.1 y Java 21.
Gestiona datos de usuario, perfil JSONB, validación/sanitización de entrada e integración con `auth-service` para registro de credenciales.

## Documentación

- Guía técnica completa: [docs/SERVICE_DOCUMENTATION.md](/home/javizzz/Documents/Agora/Agora-User-Service/user-service/docs/SERVICE_DOCUMENTATION.md)
- Referencia de API: [docs/API_REFERENCE.md](/home/javizzz/Documents/Agora/Agora-User-Service/user-service/docs/API_REFERENCE.md)
- Guía de pruebas: [TESTING_GUIDE.md](/home/javizzz/Documents/Agora/Agora-User-Service/user-service/TESTING_GUIDE.md)
- Review técnico previo: [SERVICE_REVIEW.md](/home/javizzz/Documents/Agora/Agora-User-Service/user-service/SERVICE_REVIEW.md)

## Stack

- Java 21
- Spring Boot Web, Data JPA, Validation, Actuator
- PostgreSQL (producción/desarrollo), H2 (tests)
- Spring Retry + AOP
- Lombok
- Hypersistence Utils para JSONB

## Ejecución local

1. Configura `src/main/resources/application.properties` o variables de entorno equivalentes.
2. Levanta PostgreSQL con el esquema de `init.sql`.
3. Ejecuta:

```bash
./mvnw spring-boot:run
```

El servicio arranca por defecto en `http://localhost:8080`.

## Ejecución con Docker Compose

1. Ajusta variables en `.env`.
2. Asegura que exista la red externa `agora-network`.
3. Ejecuta:

```bash
docker compose up --build
```

El servicio expone el puerto definido por `SERVER_PORT` (mapeado hacia el `8080` interno).

## Health check

- `GET /actuator/health`
- `GET /actuator/info`

## Variables de entorno principales

- `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD` (perfil `prod`)
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` (perfil `docker`)
- `AUTH_SERVICE_URL`
- `AUTH_SERVICE_CREDENTIALS_ENDPOINT`
- `AUTH_SERVICE_AUTHENTICATE_SERVICE`
- `AUTH_SERVICE_CREDENTIALS`
- `AUTH_SERVICE_RETRY_MAX_ATTEMPTS`
- `AUTH_SERVICE_RETRY_INITIAL_DELAY_MS`
- `AUTH_SERVICE_RETRY_MAX_DELAY_MS`
- `AUTH_SERVICE_RETRY_MULTIPLIER`

## Notas operativas

- El servicio depende de `auth-service` para:
  - obtener token de servicio (`/internal/service/token`)
  - registrar credenciales de usuario (`/internal/credentials`)
- Si `auth-service` falla, se usan reintentos automáticos con backoff exponencial.
- Errores de integración externa se traducen a `502`/`503` según tipo de fallo.
