package com.app.userservice.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation for international phone numbers.
 *
 * EXPECTED FORMAT: E.164 international format
 * - Must start with +
 * - Followed by country code (1-3 digits)
 * - Followed by national number (up to 15 digits total)
 *
 * EXAMPLES OF VALID NUMBERS:
 * ✅ "+573001234567" (Colombia)
 * ✅ "+12025551234" (USA)
 * ✅ "+442071234567" (UK)
 * ✅ "+61412345678" (Australia)
 *
 * ALSO ACCEPTS (will be sanitized by DataSanitizer):
 * ✅ "+57 300 123 4567" (spaces)
 * ✅ "+1-202-555-1234" (hyphens)
 * ✅ "+44 (20) 7123 4567" (parentheses)
 *
 * INVALID:
 * ❌ "3001234567" (missing country code)
 * ❌ "+57" (too short)
 * ❌ "+12345678901234567890" (too long)
 * ❌ "phone" (not a number)
 *
 * WHY E.164:
 * - International standard (ITU-T)
 * - Unambiguous (no guessing country)
 * - Compatible with SMS/WhatsApp/Telegram
 * - Future-proof for international users
 *
 * NOTE: This validator works AFTER sanitization.
 * DataSanitizer will remove spaces, hyphens, parentheses first.
 *
 * @author Your Name
 * @since 1.0.0
 */
@Documented
@Constraint(validatedBy = ValidPhoneValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhone {

    /**
     * Error message when validation fails
     */
    String message() default "Phone number must be in international format (e.g., +573001234567)";

    /**
     * Validation groups
     */
    Class<?>[] groups() default {};

    /**
     * Payload for metadata
     */
    Class<? extends Payload>[] payload() default {};
}