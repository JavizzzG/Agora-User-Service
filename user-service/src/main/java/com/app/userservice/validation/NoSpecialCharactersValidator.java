package com.app.userservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for {@link NoSpecialCharacters} annotation.
 *
 * SECURITY NOTE: This validator prevents DANGEROUS characters, not ALL special chars.
 * It's designed to allow legitimate international names while blocking XSS attempts.
 *
 * WHAT IT BLOCKS:
 * - HTML tags: <, >, &
 * - Null bytes: \0
 * - Control characters: \n, \r, \t, etc.
 * - Script-related: {, }, [, ], \, |
 *
 * WHAT IT ALLOWS (by default):
 * - Letters: a-z, A-Z
 * - Accented: é, ñ, ü, ø, etc.
 * - Spaces (configurable)
 * - Hyphens: - (configurable)
 * - Apostrophes: ' (configurable)
 *
 * EXAMPLES:
 * ✅ "José María"
 * ✅ "O'Brien"
 * ✅ "Jean-Pierre"
 * ✅ "Müller"
 * ✅ "van der Sar" (spaces allowed)
 * ❌ "<script>alert('xss')</script>"
 * ❌ "Robert'); DROP TABLE users;--"
 * ❌ "Name\0WithNullByte"
 *
 * @author Your Name
 * @since 1.0.0
 */
public class NoSpecialCharactersValidator implements ConstraintValidator<NoSpecialCharacters, String> {

    private boolean allowSpaces;
    private boolean allowHyphens;
    private boolean allowApostrophes;
    private boolean allowAccents;

    @Override
    public void initialize(NoSpecialCharacters annotation) {
        this.allowSpaces = annotation.allowSpaces();
        this.allowHyphens = annotation.allowHyphens();
        this.allowApostrophes = annotation.allowApostrophes();
        this.allowAccents = annotation.allowAccents();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Null and empty strings are valid (use @NotBlank for required fields)
        if (value == null || value.isEmpty()) {
            return true;
        }

        // Check for dangerous characters first
        if (containsDangerousCharacters(value)) {
            return false;
        }

        // Build regex pattern based on configuration
        StringBuilder pattern = new StringBuilder("^[a-zA-Z");

        // Add accented characters if allowed
        if (allowAccents) {
            // Unicode ranges for common accented characters
            pattern.append("\\p{L}"); // All Unicode letters (includes accents)
        }

        if (allowSpaces) {
            pattern.append(" ");
        }

        if (allowHyphens) {
            pattern.append("\\-");
        }

        if (allowApostrophes) {
            pattern.append("'");
        }

        pattern.append("]+$");

        return value.matches(pattern.toString());
    }

    /**
     * Check for characters that are ALWAYS dangerous regardless of context.
     *
     * These characters have no legitimate use in names/titles:
     * - HTML/XML special chars: <, >, &
     * - Null bytes: \0
     * - Control characters: \n, \r, \t, etc.
     * - Script delimiters: {, }, [, ], \, |
     * - Quotes: " (single quotes ' are allowed for O'Brien)
     *
     * @param value the string to check
     * @return true if dangerous characters found
     */
    private boolean containsDangerousCharacters(String value) {
        // Check for null bytes (serious security issue)
        if (value.contains("\0")) {
            return true;
        }

        // Check for control characters (except space)
        if (value.matches(".*\\p{Cntrl}.*")) {
            return true;
        }

        // Check for HTML/XML special characters
        if (value.matches(".*[<>&\"].*")) {
            return true;
        }

        // Check for script-related characters
        if (value.matches(".*[{}\\[\\]\\\\|].*")) {
            return true;
        }

        return false;
    }
}
