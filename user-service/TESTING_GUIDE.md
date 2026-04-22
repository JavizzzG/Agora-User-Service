# Guía de Pruebas Unitarias - User Service

Esta guía explica cómo ejecutar y entender las pruebas unitarias creadas para el User Service.

## 📁 Estructura de Pruebas

```
src/test/java/com/app/userservice/
├── BaseTest.java                    # Clase base de configuración
├── controller/
│   └── UserControllerTest.java      # Pruebas del controlador REST
├── service/
│   └── UserServiceTest.java         # Pruebas de lógica de negocio
├── repository/
│   └── UserRepositoryTest.java       # Pruebas de acceso a datos
├── mapper/
│   └── UserMapperTest.java           # Pruebas de mapeo DTO/Entity
├── validation/
│   └── NoSpecialCharactersValidatorTest.java # Pruebas de validación
├── util/
│   └── DataSanitizerTest.java       # Pruebas de utilidades
└── exception/
    └── GlobalExceptionHandlerTest.java # Pruebas de manejo de excepciones
```

## 🛠️ Tecnologías de Pruebas

- **JUnit 5**: Framework principal de pruebas
- **Mockito**: Para crear mocks y spies
- **AssertJ**: Para aserciones más legibles
- **Spring Boot Test**: Para pruebas de integración
- **TestContainers**: Para pruebas con base de datos real (opcional)

## 🚀 Ejecutar Pruebas

### Todas las Pruebas
```bash
mvn test
```

### Pruebas Específicas
```bash
# Pruebas de servicio
mvn test -Dtest=UserServiceTest

# Pruebas de controlador
mvn test -Dtest=UserControllerTest

# Pruebas de repositorio
mvn test -Dtest=UserRepositoryTest
```

### Pruebas con Cobertura
```bash
mvn clean test jacoco:report
```

## 📊 Tipos de Pruebas Creadas

### 1. Pruebas Unitarias Puras
- **UserServiceTest**: Prueba la lógica de negocio con mocks
- **UserMapperTest**: Prueba conversiones DTO/Entity
- **DataSanitizerTest**: Prueba utilidades de sanitización
- **NoSpecialCharactersValidatorTest**: Prueba validadores personalizados

### 2. Pruebas de Integración
- **UserRepositoryTest**: Prueba con H2 database
- **UserControllerTest**: Prueba endpoints REST con MockMvc

### 3. Pruebas de Exception Handling
- **GlobalExceptionHandlerTest**: Prueba respuestas de error

## 🔍 Convenciones de Nombres

### Métodos de Prueba
```java
@Test
@DisplayName("Should create user successfully when email is unique")
void createUser_Success_WhenEmailIsUnique() {
    // Given - Arrange: Configurar el escenario
    // When  - Act: Ejecutar la acción
    // Then  - Assert: Verificar el resultado
}
```

### Estructura AAA
- **Arrange**: Preparar datos y mocks
- **Act**: Ejecutar el método bajo prueba
- **Assert**: Verificar los resultados

## 📝 Ejemplos de Pruebas

### Prueba de Servicio con Mocks
```java
@Test
@DisplayName("Should create user successfully when email is unique")
void createUser_Success_WhenEmailIsUnique() {
    // Given
    when(userRepository.existsByEmail(createRequest.getEmail())).thenReturn(false);
    when(userMapper.toEntity(createRequest)).thenReturn(testUser);
    when(userRepository.save(any(User.class))).thenReturn(testUser);
    when(userMapper.toResponse(testUser)).thenReturn(createUserResponse());

    // When
    UserResponse result = userService.createUser(createRequest);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getEmail()).isEqualTo(createRequest.getEmail());
    
    verify(userRepository).existsByEmail(createRequest.getEmail());
    verify(userRepository).save(any(User.class));
}
```

### Prueba de Controlador REST
```java
@Test
@DisplayName("Should create user and return 201 status")
void createUser_Returns201_WhenValidRequest() throws Exception {
    // Given
    when(userService.createUser(any(CreateUserRequest.class))).thenReturn(testUserResponse);

    // When & Then
    mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("john.doe@example.com"));
}
```

### Prueba de Repositorio con Base de Datos
```java
@Test
@DisplayName("Should find user by email successfully")
void findByEmail_ReturnsUser_WhenEmailExists() {
    // When
    Optional<User> result = userRepository.findByEmail("john.doe@example.com");

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getEmail()).isEqualTo("john.doe@example.com");
}
```

## 🎯 Mejores Prácticas Aplicadas

### 1. Tests Independientes
- Cada prueba es independiente de otras
- No comparten estado entre pruebas
- Usan `@BeforeEach` para configuración

### 2. Mocks Efectivos
- Solo se mockean las dependencias externas
- Se verifican las interacciones importantes
- Se usan argument matchers apropiados

### 3. Aserciones Claras
- Usan AssertJ para legibilidad
- Verifican múltiples aspectos del resultado
- Incluyen mensajes descriptivos

### 4. Cobertura de Casos
- Casos felices (happy path)
- Casos límite (edge cases)
- Casos de error (error cases)
- Valores nulos y vacíos

## 📋 Configuración de Tests

### application-test.yml
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: password
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
```

### Dependencias de Prueba
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

## 🔧 Herramientas Útiles

### IDE Support
- **IntelliJ IDEA**: Run tests con clic derecho
- **VS Code**: Extension Java Test Runner
- **Eclipse**: JUnit integration

### Comandos Maven
```bash
# Ejecutar pruebas específicas
mvn test -Dtest="*Test"

# Saltar pruebas
mvn install -DskipTests

# Ejecutar con perfil de test
mvn test -Ptest

# Generar reportes
mvn surefire-report:report
```

## 📈 Métricas de Calidad

### Cobertura Esperada
- **Servicios**: >90%
- **Controladores**: >85%
- **Repositorios**: >80%
- **Utilidades**: >95%
- **Validadores**: >95%

### Métricas de Mutación
- Pruebas robustas contra cambios de código
- Verificación de lógica condicional
- Detección de tests falsos positivos

## 🚨 Errores Comunes y Soluciones

### 1. Mock no configurado
```java
// ❌ Error
when(userRepository.save(any())).thenReturn(user);

// ✅ Solución
when(userRepository.save(any(User.class))).thenReturn(user);
```

### 2. Verificación incorrecta
```java
// ❌ Error
verify(userRepository).save(user);

// ✅ Solución
verify(userRepository).save(any(User.class));
```

### 3. Aserciones incompletas
```java
// ❌ Error
assertThat(result).isNotNull();

// ✅ Solución
assertThat(result)
    .isNotNull()
    .extracting(UserResponse::getEmail)
    .isEqualTo(expectedEmail);
```

## 📚 Recursos Adicionales

### Documentación
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://site.mockito.org/)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [Spring Boot Testing](https://spring.io/guides/gs/testing-web/)

### Tutoriales Recomendados
- "Effective Unit Testing" por Lasse Koskela
- "Testing Spring Boot Applications" por Spring.io
- "Mockito Cookbook" por Alex Ruiz

## 🔄 Mantenimiento de Pruebas

### Actualización Regular
- Mantener tests actualizados con código
- Refactorizar tests cuando sea necesario
- Agregar tests para nuevo código

### Revisión por Pares
- Revisar calidad de tests
- Verificar cobertura adecuada
- Asegurar mantenibilidad

---

**Nota**: Esta guía es un punto de partida. Ajusta las convenciones y prácticas según las necesidades específicas de tu equipo y proyecto.
