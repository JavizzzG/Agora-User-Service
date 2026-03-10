package com.app.userservice.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation to ensure a string contains only safe characters.
 *
 * USE CASE: Names, usernames, titles - where you want to prevent injection attacks
 * but allow reasonable input.
 *
 * PHILOSOPHY: Validate, don't mutate. This validator REJECTS invalid input,
 * it doesn't try to "fix" it.
 *
 * EXAMPLES:
 * - "John Doe" ✅ (if allowSpaces = true)
 * - "O'Brien" ✅ (if allowApostrophes = true)
 * - "Jean-Pierre" ✅ (if allowHyphens = true)
 * - "José María" ✅ (allows accented characters by default)
 * - "<script>" ❌ (dangerous HTML)
 * - "DROP TABLE" ✅ (this is just text, SQL injection is prevented by JPA)
 *
 * @author Your Name
 * @since 1.0.0
 */
@Documented
@Constraint(validatedBy = NoSpecialCharactersValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoSpecialCharacters {

    /**
     * Error message when validation fails
     */
    String message() default "Field contains invalid characters";

    /**
     * Validation groups (for advanced use cases)
     */
    Class<?>[] groups() default {};

    /**
     * Payload for metadata (for advanced use cases)
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * Allow spaces in the string
     * Default: true (most names have spaces)
     */
    boolean allowSpaces() default true;

    /**
     * Allow hyphens (-)
     * Default: true (for names like "Jean-Pierre", "Mary-Ann")
     */
    boolean allowHyphens() default true;

    /**
     * Allow apostrophes (')
     * Default: true (for names like "O'Brien", "D'Angelo")
     */
    boolean allowApostrophes() default true;

    /**
     * Allow accented characters (é, ñ, ü, etc.)
     * Default: true (for international names)
     */
    boolean allowAccents() default true;
}