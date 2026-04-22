package com.app.userservice.exception;

import com.app.userservice.dto.ErrorResponse;
import com.app.userservice.dto.CreateUserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private MethodArgumentNotValidException buildValidationException(List<FieldError> fieldErrors) {
        try {
            Method method = GlobalExceptionHandlerTest.class
                    .getDeclaredMethod("validationTarget", CreateUserRequest.class);
            MethodParameter parameter = new MethodParameter(method, 0);
            BindingResult bindingResult = new BeanPropertyBindingResult(
                    new CreateUserRequest(),
                    "createUserRequest"
            );
            fieldErrors.forEach(bindingResult::addError);
            return new MethodArgumentNotValidException(parameter, bindingResult);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Failed to build MethodArgumentNotValidException for test", e);
        }
    }

    @SuppressWarnings("unused")
    private void validationTarget(@Valid CreateUserRequest request) {
        // Only used to build a MethodParameter for MethodArgumentNotValidException in tests.
    }

    @Test
    @DisplayName("Should handle UserNotFoundException correctly")
    void handleUserNotFoundException_Returns404_WhenUserNotFound() {
        // Given
        String errorMessage = "User not found with id: 123e4567-e89b-12d3-a456-426614174000";
        String requestUri = "/api/users/123e4567-e89b-12d3-a456-426614174000";
        
        UserNotFoundException exception = new UserNotFoundException(errorMessage);
        when(request.getRequestURI()).thenReturn(requestUri);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleUserNotFoundException(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        
        ErrorResponse error = response.getBody();
        assertThat(error).isNotNull();
        assertThat(error.getStatus()).isEqualTo(404);
        assertThat(error.getError()).isEqualTo("Not Found");
        assertThat(error.getMessage()).isEqualTo(errorMessage);
        assertThat(error.getPath()).isEqualTo(requestUri);
        assertThat(error.getValidationErrors()).isNull();
    }

    @Test
    @DisplayName("Should handle DuplicateEmailException correctly")
    void handleDuplicateEmailException_Returns409_WhenEmailIsDuplicate() {
        // Given
        String errorMessage = "User with email 'test@example.com' already exists";
        String requestUri = "/api/users";
        
        DuplicateEmailException exception = new DuplicateEmailException("test@example.com");
        when(request.getRequestURI()).thenReturn(requestUri);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDuplicateEmailException(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        
        ErrorResponse error = response.getBody();
        assertThat(error).isNotNull();
        assertThat(error.getStatus()).isEqualTo(409);
        assertThat(error.getError()).isEqualTo("Conflict");
        assertThat(error.getMessage()).isEqualTo(errorMessage);
        assertThat(error.getPath()).isEqualTo(requestUri);
        assertThat(error.getValidationErrors()).isNull();
    }

    @Test
    @DisplayName("Should handle validation errors correctly")
    void handleValidationException_Returns400_WhenValidationFails() {
        // Given
        String requestUri = "/api/users";
        List<FieldError> fieldErrors = Arrays.asList(
                new FieldError("createUserRequest", "firstName", "John", false, null, null, "First name is required"),
                new FieldError("createUserRequest", "email", "invalid-email", false, null, null, "Email must be valid")
        );

        MethodArgumentNotValidException validationException = buildValidationException(fieldErrors);
        when(request.getRequestURI()).thenReturn(requestUri);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(validationException, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        ErrorResponse error = response.getBody();
        assertThat(error).isNotNull();
        assertThat(error.getStatus()).isEqualTo(400);
        assertThat(error.getError()).isEqualTo("Validation Failed");
        assertThat(error.getMessage()).isEqualTo("Invalid input data");
        assertThat(error.getPath()).isEqualTo(requestUri);
        
        assertThat(error.getValidationErrors()).isNotNull();
        assertThat(error.getValidationErrors()).hasSize(2);
        assertThat(error.getValidationErrors()).containsExactlyInAnyOrder(
                "First name is required",
                "Email must be valid"
        );
    }

    @Test
    @DisplayName("Should handle empty validation errors list")
    void handleValidationException_HandlesEmptyValidationErrors() {
        // Given
        String requestUri = "/api/users";
        
        MethodArgumentNotValidException validationException = buildValidationException(Arrays.asList());
        when(request.getRequestURI()).thenReturn(requestUri);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(validationException, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        ErrorResponse error = response.getBody();
        assertThat(error).isNotNull();
        assertThat(error.getValidationErrors()).isNotNull();
        assertThat(error.getValidationErrors()).isEmpty();
    }

    @Test
    @DisplayName("Should handle generic exceptions correctly")
    void handleGenericException_Returns500_WhenUnexpectedError() {
        // Given
        String requestUri = "/api/users";
        Exception exception = new RuntimeException("Unexpected database error");
        when(request.getRequestURI()).thenReturn(requestUri);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        
        ErrorResponse error = response.getBody();
        assertThat(error).isNotNull();
        assertThat(error.getStatus()).isEqualTo(500);
        assertThat(error.getError()).isEqualTo("Internal Server Error");
        assertThat(error.getMessage()).isEqualTo("An unexpected error occurred");
        assertThat(error.getPath()).isEqualTo(requestUri);
        assertThat(error.getValidationErrors()).isNull();
    }

    @Test
    @DisplayName("Should handle null request URI gracefully")
    void handleExceptions_HandlesNullRequestUri() {
        // Given
        UserNotFoundException exception = new UserNotFoundException("User not found");
        when(request.getRequestURI()).thenReturn(null);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleUserNotFoundException(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        
        ErrorResponse error = response.getBody();
        assertThat(error).isNotNull();
        assertThat(error.getPath()).isNull();
    }

    @Test
    @DisplayName("Should handle empty request URI gracefully")
    void handleExceptions_HandlesEmptyRequestUri() {
        // Given
        UserNotFoundException exception = new UserNotFoundException("User not found");
        when(request.getRequestURI()).thenReturn("");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleUserNotFoundException(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        
        ErrorResponse error = response.getBody();
        assertThat(error).isNotNull();
        assertThat(error.getPath()).isEqualTo("");
    }

    @Test
    @DisplayName("Should create proper ErrorResponse structure")
    void handleExceptions_CreatesProperErrorResponseStructure() {
        // Given
        String errorMessage = "User with email 'test@example.com' already exists";
        String requestUri = "/api/users";
        
        DuplicateEmailException exception = new DuplicateEmailException("test@example.com");
        when(request.getRequestURI()).thenReturn(requestUri);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDuplicateEmailException(exception, request);

        // Then
        ErrorResponse error = response.getBody();
        assertThat(error).isNotNull();
        
        // Verify all required fields are present
        assertThat(error.getStatus()).isNotNull();
        assertThat(error.getError()).isNotNull();
        assertThat(error.getMessage()).isNotNull();
        assertThat(error.getPath()).isNotNull();
        
        // Verify timestamp is present (automatically set by ErrorResponse)
        assertThat(error.getTimestamp()).isNotNull();
        
        // Verify the structure can be serialized to JSON
        assertThatCode(() -> objectMapper.writeValueAsString(error)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle multiple validation errors properly")
    void handleValidationException_HandlesMultipleValidationErrors() {
        // Given
        String requestUri = "/api/users";
        List<FieldError> fieldErrors = Arrays.asList(
                new FieldError("createUserRequest", "firstName", null, false, null, null, "First name is required"),
                new FieldError("createUserRequest", "lastName", null, false, null, null, "Last name is required"),
                new FieldError("createUserRequest", "email", null, false, null, null, "Email is required"),
                new FieldError("createUserRequest", "password", null, false, null, null, "Password is required")
        );

        MethodArgumentNotValidException validationException = buildValidationException(fieldErrors);
        when(request.getRequestURI()).thenReturn(requestUri);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(validationException, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        ErrorResponse error = response.getBody();
        assertThat(error).isNotNull();
        assertThat(error.getValidationErrors()).hasSize(4);
        assertThat(error.getValidationErrors()).containsExactlyInAnyOrder(
                "First name is required",
                "Last name is required",
                "Email is required",
                "Password is required"
        );
    }

    @Test
    @DisplayName("Should handle null field error messages")
    void handleValidationException_HandlesNullFieldErrorMessages() {
        // Given
        String requestUri = "/api/users";
        List<FieldError> fieldErrors = Arrays.asList(
                new FieldError("createUserRequest", "firstName", null, false, null, null, null),
                new FieldError("createUserRequest", "email", null, false, null, null, "Email must be valid")
        );

        MethodArgumentNotValidException validationException = buildValidationException(fieldErrors);
        when(request.getRequestURI()).thenReturn(requestUri);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(validationException, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        ErrorResponse error = response.getBody();
        assertThat(error).isNotNull();
        assertThat(error.getValidationErrors()).hasSize(2);
        assertThat(error.getValidationErrors()).contains(null, "Email must be valid");
    }

    @Test
    @DisplayName("Should handle UserNotFoundException with UUID")
    void handleUserNotFoundException_HandlesUuidInMessage() {
        // Given
        UUID userId = UUID.randomUUID();
        String errorMessage = "User not found with id: " + userId;
        String requestUri = "/api/users/" + userId;
        
        UserNotFoundException exception = new UserNotFoundException(userId);
        when(request.getRequestURI()).thenReturn(requestUri);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleUserNotFoundException(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        
        ErrorResponse error = response.getBody();
        assertThat(error).isNotNull();
        assertThat(error.getMessage()).isEqualTo(errorMessage);
        assertThat(error.getPath()).isEqualTo(requestUri);
    }

    @Test
    @DisplayName("Should handle DuplicateEmailException with email")
    void handleDuplicateEmailException_HandlesEmailInMessage() {
        // Given
        String email = "test@example.com";
        String errorMessage = "User with email '" + email + "' already exists";
        String requestUri = "/api/users";
        
        DuplicateEmailException exception = new DuplicateEmailException(email);
        when(request.getRequestURI()).thenReturn(requestUri);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDuplicateEmailException(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        
        ErrorResponse error = response.getBody();
        assertThat(error).isNotNull();
        assertThat(error.getMessage()).isEqualTo(errorMessage);
        assertThat(error.getPath()).isEqualTo(requestUri);
    }

    @Test
    @DisplayName("Should return 503 for generic auth-service failures")
    void handleAuthServiceException_Returns503() {
        // Given
        String requestUri = "/users/create";
        AuthServiceException exception = new AuthServiceException("Auth service unavailable");
        when(request.getRequestURI()).thenReturn(requestUri);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuthServiceException(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(503);
    }

    @Test
    @DisplayName("Should return 502 for non-retryable auth-service failures")
    void handleNonRetryableAuthServiceException_Returns502() {
        // Given
        String requestUri = "/users/create";
        NonRetryableAuthServiceException exception = new NonRetryableAuthServiceException("Invalid auth payload");
        when(request.getRequestURI()).thenReturn(requestUri);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNonRetryableAuthServiceException(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(502);
    }
}
