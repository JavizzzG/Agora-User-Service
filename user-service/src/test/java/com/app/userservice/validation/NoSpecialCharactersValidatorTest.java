package com.app.userservice.validation;

import com.app.userservice.validation.NoSpecialCharacters;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("NoSpecialCharactersValidator Tests")
class NoSpecialCharactersValidatorTest {

    private NoSpecialCharactersValidator validator;
    private NoSpecialCharacters annotation;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new NoSpecialCharactersValidator();
        
        // Create mock annotation with default values
        annotation = new NoSpecialCharacters() {
            @Override
            public boolean allowSpaces() {
                return true;
            }

            @Override
            public boolean allowHyphens() {
                return true;
            }

            @Override
            public boolean allowApostrophes() {
                return true;
            }

            @Override
            public boolean allowAccents() {
                return true;
            }

            @Override
            public String message() {
                return "Invalid characters";
            }

            @Override
            public Class<?>[] groups() {
                return new Class[0];
            }

            @Override
            public Class<? extends jakarta.validation.Payload>[] payload() {
                return new Class[0];
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return NoSpecialCharacters.class;
            }
        };

        validator.initialize(annotation);
    }

    @Test
    @DisplayName("Should accept null values")
    void isValid_ReturnsTrue_WhenValueIsNull() {
        // When
        boolean result = validator.isValid(null, context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should accept empty strings")
    void isValid_ReturnsTrue_WhenValueIsEmpty() {
        // When
        boolean result = validator.isValid("", context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should accept valid names with spaces")
    void isValid_ReturnsTrue_WhenValidNameWithSpaces() {
        // Given
        String[] validNames = {
                "John Doe",
                "Jane Smith",
                "van der Sar",
                "De la Cruz"
        };

        // When & Then
        for (String name : validNames) {
            assertThat(validator.isValid(name, context))
                    .as("Should accept: %s", name)
                    .isTrue();
        }
    }

    @Test
    @DisplayName("Should accept valid names with hyphens")
    void isValid_ReturnsTrue_WhenValidNameWithHyphens() {
        // Given
        String[] validNames = {
                "Jean-Pierre",
                "García-López",
                "Mary-Jane"
        };

        // When & Then
        for (String name : validNames) {
            assertThat(validator.isValid(name, context))
                    .as("Should accept: %s", name)
                    .isTrue();
        }
    }

    @Test
    @DisplayName("Should accept valid names with apostrophes")
    void isValid_ReturnsTrue_WhenValidNameWithApostrophes() {
        // Given
        String[] validNames = {
                "O'Brien",
                "O'Neill",
                "d'Artagnan"
        };

        // When & Then
        for (String name : validNames) {
            assertThat(validator.isValid(name, context))
                    .as("Should accept: %s", name)
                    .isTrue();
        }
    }

    @Test
    @DisplayName("Should accept valid names with accents")
    void isValid_ReturnsTrue_WhenValidNameWithAccents() {
        // Given
        String[] validNames = {
                "José",
                "María",
                "Müller",
                "François",
                "Ørjan"
        };

        // When & Then
        for (String name : validNames) {
            assertThat(validator.isValid(name, context))
                    .as("Should accept: %s", name)
                    .isTrue();
        }
    }

    @Test
    @DisplayName("Should accept simple names")
    void isValid_ReturnsTrue_WhenSimpleName() {
        // Given
        String[] validNames = {
                "John",
                "Mary",
                "Alice",
                "Bob"
        };

        // When & Then
        for (String name : validNames) {
            assertThat(validator.isValid(name, context))
                    .as("Should accept: %s", name)
                    .isTrue();
        }
    }

    @Test
    @DisplayName("Should reject names with HTML tags")
    void isValid_ReturnsFalse_WhenContainsHtmlTags() {
        // Given
        String[] invalidNames = {
                "<script>alert('xss')</script>",
                "John<b>Doe</b>",
                "<img src=x onerror=alert(1)>",
                "Jane&Smith"
        };

        // When & Then
        for (String name : invalidNames) {
            assertThat(validator.isValid(name, context))
                    .as("Should reject: %s", name)
                    .isFalse();
        }
    }

    @Test
    @DisplayName("Should reject names with null bytes")
    void isValid_ReturnsFalse_WhenContainsNullBytes() {
        // Given
        String invalidName = "John\0Doe";

        // When
        boolean result = validator.isValid(invalidName, context);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should reject names with control characters")
    void isValid_ReturnsFalse_WhenContainsControlCharacters() {
        // Given
        String[] invalidNames = {
                "John\nDoe",
                "Jane\rSmith",
                "Alice\tBrown",
                "Bob\u0001Test"
        };

        // When & Then
        for (String name : invalidNames) {
            assertThat(validator.isValid(name, context))
                    .as("Should reject: %s", name)
                    .isFalse();
        }
    }

    @Test
    @DisplayName("Should reject names with script-related characters")
    void isValid_ReturnsFalse_WhenContainsScriptCharacters() {
        // Given
        String[] invalidNames = {
                "John{Doe}",
                "Jane[Smith]",
                "Alice\\Brown",
                "Bob|Test"
        };

        // When & Then
        for (String name : invalidNames) {
            assertThat(validator.isValid(name, context))
                    .as("Should reject: %s", name)
                    .isFalse();
        }
    }

    @Test
    @DisplayName("Should reject names with quotes")
    void isValid_ReturnsFalse_WhenContainsQuotes() {
        // Given
        String[] invalidNames = {
                "John\"Doe\"",
                "Jane\"Smith"
        };

        // When & Then
        for (String name : invalidNames) {
            assertThat(validator.isValid(name, context))
                    .as("Should reject: %s", name)
                    .isFalse();
        }
    }

    @Test
    @DisplayName("Should reject names with numbers")
    void isValid_ReturnsFalse_WhenContainsNumbers() {
        // Given
        String[] invalidNames = {
                "John123",
                "Jane Doe 2",
                "Alice3rd"
        };

        // When & Then
        for (String name : invalidNames) {
            assertThat(validator.isValid(name, context))
                    .as("Should reject: %s", name)
                    .isFalse();
        }
    }

    @Test
    @DisplayName("Should reject names with special characters")
    void isValid_ReturnsFalse_WhenContainsSpecialCharacters() {
        // Given
        String[] invalidNames = {
                "John@Doe",
                "Jane#Smith",
                "Alice$Brown",
                "Bob%Test",
                "Mary&Jane",
                "David*Smith",
                "Tom+Brown",
                "Jerry=Doe",
                "Mike!Test",
                "Chris?Smith",
                "Steve.Brown"
        };

        // When & Then
        for (String name : invalidNames) {
            assertThat(validator.isValid(name, context))
                    .as("Should reject: %s", name)
                    .isFalse();
        }
    }

    @Test
    @DisplayName("Should work with different configuration - no spaces")
    void isValid_WithNoSpacesConfiguration() {
        // Given - Create validator that doesn't allow spaces
        NoSpecialCharactersValidator noSpacesValidator = new NoSpecialCharactersValidator();
        NoSpecialCharacters noSpacesAnnotation = new NoSpecialCharacters() {
            @Override
            public boolean allowSpaces() { return false; }
            @Override
            public boolean allowHyphens() { return true; }
            @Override
            public boolean allowApostrophes() { return true; }
            @Override
            public boolean allowAccents() { return true; }
            @Override
            public String message() { return "No spaces allowed"; }
            @Override
            public Class<?>[] groups() { return new Class[0]; }
            @Override
            public Class<? extends jakarta.validation.Payload>[] payload() { return new Class[0]; }
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() { return NoSpecialCharacters.class; }
        };
        noSpacesValidator.initialize(noSpacesAnnotation);

        // When & Then
        assertThat(noSpacesValidator.isValid("JohnDoe", context)).isTrue();
        assertThat(noSpacesValidator.isValid("John Doe", context)).isFalse();
        assertThat(noSpacesValidator.isValid("Jean-Pierre", context)).isTrue();
    }

    @Test
    @DisplayName("Should work with different configuration - no hyphens")
    void isValid_WithNoHyphensConfiguration() {
        // Given - Create validator that doesn't allow hyphens
        NoSpecialCharactersValidator noHyphensValidator = new NoSpecialCharactersValidator();
        NoSpecialCharacters noHyphensAnnotation = new NoSpecialCharacters() {
            @Override
            public boolean allowSpaces() { return true; }
            @Override
            public boolean allowHyphens() { return false; }
            @Override
            public boolean allowApostrophes() { return true; }
            @Override
            public boolean allowAccents() { return true; }
            @Override
            public String message() { return "No hyphens allowed"; }
            @Override
            public Class<?>[] groups() { return new Class[0]; }
            @Override
            public Class<? extends jakarta.validation.Payload>[] payload() { return new Class[0]; }
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() { return NoSpecialCharacters.class; }
        };
        noHyphensValidator.initialize(noHyphensAnnotation);

        // When & Then
        assertThat(noHyphensValidator.isValid("John Doe", context)).isTrue();
        assertThat(noHyphensValidator.isValid("Jean-Pierre", context)).isFalse();
        assertThat(noHyphensValidator.isValid("O'Brien", context)).isTrue();
    }

    @Test
    @DisplayName("Should work with different configuration - no apostrophes")
    void isValid_WithNoApostrophesConfiguration() {
        // Given - Create validator that doesn't allow apostrophes
        NoSpecialCharactersValidator noApostrophesValidator = new NoSpecialCharactersValidator();
        NoSpecialCharacters noApostrophesAnnotation = new NoSpecialCharacters() {
            @Override
            public boolean allowSpaces() { return true; }
            @Override
            public boolean allowHyphens() { return true; }
            @Override
            public boolean allowApostrophes() { return false; }
            @Override
            public boolean allowAccents() { return true; }
            @Override
            public String message() { return "No apostrophes allowed"; }
            @Override
            public Class<?>[] groups() { return new Class[0]; }
            @Override
            public Class<? extends jakarta.validation.Payload>[] payload() { return new Class[0]; }
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() { return NoSpecialCharacters.class; }
        };
        noApostrophesValidator.initialize(noApostrophesAnnotation);

        // When & Then
        assertThat(noApostrophesValidator.isValid("John Doe", context)).isTrue();
        assertThat(noApostrophesValidator.isValid("Jean-Pierre", context)).isTrue();
        assertThat(noApostrophesValidator.isValid("O'Brien", context)).isFalse();
    }

    @Test
    @DisplayName("Should work with different configuration - no accents")
    void isValid_WithNoAccentsConfiguration() {
        // Given - Create validator that doesn't allow accents
        NoSpecialCharactersValidator noAccentsValidator = new NoSpecialCharactersValidator();
        NoSpecialCharacters noAccentsAnnotation = new NoSpecialCharacters() {
            @Override
            public boolean allowSpaces() { return true; }
            @Override
            public boolean allowHyphens() { return true; }
            @Override
            public boolean allowApostrophes() { return true; }
            @Override
            public boolean allowAccents() { return false; }
            @Override
            public String message() { return "No accents allowed"; }
            @Override
            public Class<?>[] groups() { return new Class[0]; }
            @Override
            public Class<? extends jakarta.validation.Payload>[] payload() { return new Class[0]; }
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() { return NoSpecialCharacters.class; }
        };
        noAccentsValidator.initialize(noAccentsAnnotation);

        // When & Then
        assertThat(noAccentsValidator.isValid("John Doe", context)).isTrue();
        assertThat(noAccentsValidator.isValid("Jean-Pierre", context)).isTrue();
        assertThat(noAccentsValidator.isValid("O'Brien", context)).isTrue();
        assertThat(noAccentsValidator.isValid("José", context)).isFalse();
        assertThat(noAccentsValidator.isValid("María", context)).isFalse();
    }

    @Test
    @DisplayName("Should handle complex valid names")
    void isValid_ReturnsTrue_WhenComplexValidNames() {
        // Given
        String[] complexValidNames = {
                "Jean-Pierre O'Connor",
                "María García López",
                "François-Marie d'Artagnan",
                "Hans-Christian Müller"
        };

        // When & Then
        for (String name : complexValidNames) {
            assertThat(validator.isValid(name, context))
                    .as("Should accept: %s", name)
                    .isTrue();
        }
    }

    @Test
    @DisplayName("Should reject SQL injection attempts")
    void isValid_ReturnsFalse_WhenSqlInjectionAttempts() {
        // Given
        String[] sqlInjectionAttempts = {
                "Robert'); DROP TABLE users;--",
                "John' OR '1'='1",
                "Alice\"; DELETE FROM users; --"
        };

        // When & Then
        for (String attempt : sqlInjectionAttempts) {
            assertThat(validator.isValid(attempt, context))
                    .as("Should reject SQL injection: %s", attempt)
                    .isFalse();
        }
    }
}
