package com.app.userservice.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ValidPhoneValidator Tests")
class ValidPhoneValidatorTest {

    private ValidPhoneValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ValidPhoneValidator();
    }

    @Test
    @DisplayName("Should accept null and empty values")
    void isValid_ReturnsTrue_ForNullOrEmpty() {
        assertThat(validator.isValid(null, null)).isTrue();
        assertThat(validator.isValid("", null)).isTrue();
    }

    @Test
    @DisplayName("Should accept valid international format numbers")
    void isValid_ReturnsTrue_ForValidPhones() {
        ConstraintValidatorContext context = null;

        assertThat(validator.isValid("+57 3001234567", context)).isTrue();
        assertThat(validator.isValid("+1-2025551234", context)).isTrue();
        assertThat(validator.isValid("+44 2079460958", context)).isTrue();
    }

    @Test
    @DisplayName("Should reject phones with invalid format")
    void isValid_ReturnsFalse_ForInvalidPhones() {
        ConstraintValidatorContext context = null;

        assertThat(validator.isValid("3001234567", context)).isFalse();
        assertThat(validator.isValid("+573001234567", context)).isFalse();
        assertThat(validator.isValid("+57-123", context)).isFalse();
        assertThat(validator.isValid("+AA-1234567", context)).isFalse();
    }
}
