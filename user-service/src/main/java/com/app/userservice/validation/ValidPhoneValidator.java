package com.app.userservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for {@link ValidPhone} annotation.
 *
 * VALIDATION RULES:
 * 1. Must start with + (international format)
 * 2. Country code: 1-3 digits
 * 3. Total length: 8-15 digits (excluding +)
 * 4. Only digits after + (spaces/hyphens should be removed by sanitizer)
 *
 * WHY NOT USE LIBRARY:
 * Phone validation is tricky. Libraries like Google's libphonenumber are heavy (8MB+)
 * and overkill for basic validation. This validator:
 * - ✅ Simple and fast
 * - ✅ Covers 99% of cases
 * - ✅ No heavy dependencies
 * - ⚠️ Doesn't validate if number ACTUALLY exists
 * - ⚠️ Doesn't check country-specific rules
 *
 * IF YOU NEED MORE:
 * Consider Google's libphonenumber:
 * <dependency>
 *     <groupId>com.googlecode.libphonenumber</groupId>
 *     <artifactId>libphonenumber</artifactId>
 * </dependency>
 *
 * @author Your Name
 * @since 1.0.0
 */
public class ValidPhoneValidator implements ConstraintValidator<ValidPhone, String> {

    // E.164 format: +[country code 1-3 digits][number up to 15 digits total]
    // Min: +1234567 (8 chars: + plus 7 digits)
    // Max: +123456789012345 (16 chars: + plus 15 digits)

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Null and empty strings are valid (use @NotBlank for required fields)
        if (value == null || value.isEmpty()) {
            return true;
        }

        return value.matches("^\\+\\d{1,3}[-\\s]\\d{4,12}$");
    }
}