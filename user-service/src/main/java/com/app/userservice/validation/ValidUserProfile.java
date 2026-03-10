package com.app.userservice.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation for UserProfile JSONB object.
 *
 * This validates the entire UserProfile object including:
 * - avatarUrl: Must be valid HTTP/HTTPS URL
 * - bio: Max 500 chars, no dangerous HTML
 * - phone: Must be in E.164 format (if present)
 * - config.theme: Must be one of: light, dark, auto
 *
 * WHY VALIDATE THE WHOLE OBJECT:
 * UserProfile is stored as JSONB, so we can't use individual @Valid on fields.
 * We need to validate the entire object before it goes to the database.
 *
 * EXAMPLE VALID PROFILE:
 * {
 *   "avatarUrl": "https://example.com/avatar.jpg",
 *   "bio": "Software developer from Colombia",
 *   "phone": "+573001234567",
 *   "config": {
 *     "theme": "dark"
 *   }
 * }
 *
 * EXAMPLE INVALID PROFILE:
 * {
 *   "avatarUrl": "not a url",                    ❌
 *   "bio": "<script>alert('xss')</script>",      ❌
 *   "phone": "123456",                           ❌
 *   "config": {
 *     "theme": "rainbow"                         ❌
 *   }
 * }
 *
 * @author Your Name
 * @since 1.0.0
 */
@Documented
@Constraint(validatedBy = ValidUserProfileValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUserProfile {

    /**
     * Error message when validation fails
     */
    String message() default "Invalid user profile data";

    /**
     * Validation groups
     */
    Class<?>[] groups() default {};

    /**
     * Payload for metadata
     */
    Class<? extends Payload>[] payload() default {};
}