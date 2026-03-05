# User Service Microservice

Microservicio de gestión de usuarios desarrollado con Spring Boot 3.2, Java 21 y PostgreSQL.

## 📋 Tecnologías

- **Java**: 21
- **Spring Boot**: 3.2.1
- **Base de datos**: PostgreSQL 18.2
- **Build Tool**: Maven
- **Containerization**: Docker & Docker Compose

## 🏗️ Estructura del Proyecto

```
user-service/
├── src/
│   └── main/
│       ├── java/com/app/userservice/
│       │   ├── controller/      # REST Controllers
│       │   ├── service/         # Business Logic
│       │   ├── repository/      # Data Access Layer
│       │   ├── model/           # Entity & Profile classes
│       │   ├── dto/             # Data Transfer Objects
│       │   ├── mapper/          # Entity-DTO converters
│       │   └── exception/       # Custom exceptions & handlers
│       └── resources/
│           ├── application.properties
│           └── application-docker.properties
├── Dockerfile
├── docker-compose.yml
├── init.sql
└── pom.xml
```

## 🚀 Ejecutar con Docker (Recomendado)

### Prerequisitos
- Docker
- Docker Compose

### Pasos

1. **Clonar el repositorio** (o crear los archivos)

2. **Construir y ejecutar con Docker Compose**:
```bash
docker-compose up --build
```

Esto levantará:
- PostgreSQL en el puerto 5432
- User Service en el puerto 8080

3. **Verificar que está corriendo**:
```bash
curl http://localhost:8080/actuator/health
```

## 💻 Ejecutar Localmente (Sin Docker)

### Prerequisitos
- Java 21
- Maven 3.9+
- PostgreSQL 16 instalado y corriendo

### Pasos

1. **Crear la base de datos**:
```sql
CREATE DATABASE user_db;
```

2. **Ejecutar el script init.sql** en tu base de datos

3. **Configurar application.properties**:
Edita `src/main/resources/application.properties` con tus credenciales:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/user_db
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña
```

4. **Compilar y ejecutar**:
```bash
mvn clean install
mvn spring-boot:run
```

## 📡 API Endpoints

### Base URL: `http://localhost:8080/api/users`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/users` | Crear un usuario |
| GET | `/api/users` | Obtener todos los usuarios |
| GET | `/api/users/{id}` | Obtener usuario por ID |
| GET | `/api/users/email/{email}` | Obtener usuario por email |
| PUT | `/api/users/{id}` | Actualizar usuario |
| DELETE | `/api/users/{id}` | Eliminar usuario |
| GET | `/api/users/exists?email={email}` | Verificar si email existe |

### Ejemplos de Uso

#### 1. Crear Usuario
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Carlos",
    "lastName": "García",
    "email": "carlos@example.com",
    "isAdmin": false,
    "profile": {
      "avatarUrl": "https://example.com/avatar.jpg",
      "bio": "Backend developer",
      "phone": "+573001234567",
      "config": {
        "theme": "dark"
      }
    }
  }'
```

#### 2. Obtener Todos los Usuarios
```bash
curl http://localhost:8080/api/users
```

#### 3. Obtener Usuario por ID
```bash
curl http://localhost:8080/api/users/1
```

#### 4. Obtener Usuario por Email
```bash
curl http://localhost:8080/api/users/email/carlos@example.com
```

#### 5. Actualizar Usuario
```bash
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Carlos Alberto",
    "profile": {
      "bio": "Senior Backend Developer",
      "phone": "+573001234567",
      "config": {
        "theme": "light"
      }
    }
  }'
```

#### 6. Eliminar Usuario
```bash
curl -X DELETE http://localhost:8080/api/users/1
```

#### 7. Verificar si Email Existe
```bash
curl http://localhost:8080/api/users/exists?email=carlos@example.com
```

## 📊 Estructura de la Base de Datos

### Tabla: users

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | UUID | Primary key |
| first_name | VARCHAR(30) | Nombre |
| last_name | VARCHAR(30) | Apellido |
| email | VARCHAR(100) | Email único |
| is_admin | BOOLEAN | Si es administrador |
| profile | JSONB | Perfil del usuario |
| created_at | TIMESTAMPTZ | Fecha de creación |

### Estructura del Profile (JSONB)
```json
{
  "avatarUrl": "string",
  "bio": "string",
  "phone": "string",
  "config": {
    "theme": "string"
  }
}
```

## 🔧 Configuración

### Variables de Entorno (Docker)

En `docker-compose.yml` puedes modificar:

- `DB_HOST`: Host de la base de datos
- `DB_PORT`: Puerto de PostgreSQL
- `DB_NAME`: Nombre de la base de datos
- `DB_USERNAME`: Usuario de PostgreSQL
- `DB_PASSWORD`: Contraseña de PostgreSQL

## 📝 Notas Importantes

1. **Profile es opcional**: Al crear un usuario, el campo profile puede ser null o tener cualquier combinación de sus campos internos.

2. **Validaciones**: 
   - Email debe ser único
   - firstName y lastName son requeridos (máximo 30 caracteres)
   - Email debe ser válido (máximo 100 caracteres)

3. **Actualizaciones parciales**: En el endpoint PUT, solo se actualizan los campos que se envían en el request.

4. **Manejo de errores**: La API retorna errores estructurados con:
   - timestamp
   - status
   - error
   - message
   - path
   - validationErrors (si aplica)

## 🧪 Testing

Para ejecutar los tests (cuando los agregues):
```bash
mvn test
```

## 🏗️ Build para Producción

### Construcción del JAR
```bash
mvn clean package -DskipTests
```

El JAR estará en `target/user-service-1.0.0.jar`

### Construcción de la Imagen Docker
```bash
docker build -t user-service:1.0.0 .
```

## 📦 Deployment

### Docker
```bash
docker run -d \
  -p 8080:8080 \
  -e DB_HOST=your_db_host \
  -e DB_NAME=your_db_name \
  -e DB_USERNAME=your_username \
  -e DB_PASSWORD=your_password \
  --name user-service \
  user-service:1.0.0
```

## 🐛 Troubleshooting

### El servicio no se conecta a la base de datos
- Verifica que PostgreSQL está corriendo
- Verifica las credenciales en application.properties
- Verifica que la base de datos existe

### Puerto 8080 ya está en uso
Cambia el puerto en `application.properties`:
```properties
server.port=8081
```

### Error con JSONB
Asegúrate de tener la dependencia `hypersistence-utils-hibernate-63` en el pom.xml

## 📄 Licencia

Este proyecto es parte de un sistema de microservicios y es de uso interno.
