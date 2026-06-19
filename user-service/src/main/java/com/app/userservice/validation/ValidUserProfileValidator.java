package com.app.userservice.validation;

import com.app.userservice.model.UserProfile;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.validator.routines.UrlValidator;

import java.util.Arrays;
import java.util.List;

/**
 * Validator implementation for {@link ValidUserProfile} annotation.
 *
 * USES APACHE COMMONS VALIDATOR for URLs instead of manual regex.
 * This is a professional approach that handles edge cases correctly.
 *
 * VALIDATION STRATEGY:
 * 1. Each field is validated independently
 * 2. Specific error messages for each field
 * 3. Allows partial profiles (all fields optional)
 * 4. Uses libraries for complex validation (URLs)
 *
 * DEPENDENCIES REQUIRED:
 * - org.apache.commons:commons-validator (for URL validation)
 *
 * @author Mama Fajis
 * @since 1.0.0
 */
public class ValidUserProfileValidator implements ConstraintValidator<ValidUserProfile, UserProfile> {

    // Allowed theme values
    private static final List<String> ALLOWED_THEMES = Arrays.asList("light", "dark", "auto");
    private static final List<String> ALLOWED_RETRO_STYLES = Arrays.asList("brief", "detailed", "full");
    private static final List<String> ALLOWED_EXIGENCY_LEVELS = Arrays.asList("flexible", "moderated", "strict");
    private static final List<String> ALLOWED_LANGUAGES = Arrays.asList("es", "en", "fr", "pt", "de", "it");

    // Business rules
    private static final int MAX_BIO_LENGTH = 500;
    private static final int MAX_AVATAR_URL_LENGTH = 2048;

    // URL validator from Apache Commons Validator
    // Only allow http and https protocols for security
    private static final UrlValidator URL_VALIDATOR = new UrlValidator(
            new String[]{"http", "https"},
            UrlValidator.ALLOW_LOCAL_URLS  // Allow localhost for development
    );

    @Override
    public boolean isValid(UserProfile profile, ConstraintValidatorContext context) {
        // Null profile is valid (profile is optional)
        if (profile == null) {
            return true;
        }

        // Disable default violation message
        context.disableDefaultConstraintViolation();

        boolean isValid = true;

        // Validate avatarUrl if present
        if (profile.getAvatarUrl() != null && !profile.getAvatarUrl().isEmpty()) {
            isValid &= validateAvatarUrl(profile.getAvatarUrl(), context);
        }

        // Validate bio if present
        if (profile.getBio() != null && !profile.getBio().isEmpty()) {
            isValid &= validateBio(profile.getBio(), context);
        }

        // Validate config if present
        if (profile.getConfig() != null) {
            isValid &= validateConfig(profile.getConfig(), context);
        }

        return isValid;
    }

    /**
     * Validate avatar URL using Apache Commons Validator.
     *
     * WHY USE LIBRARY:
     * URLs are complex. Manual regex can miss edge cases like:
     * - javascript:alert('xss')
     * - http://user:pass@host (credentials in URL)
     * - Internationalized domain names
     *
     * Apache Commons Validator handles all of this correctly.
     */
    private boolean validateAvatarUrl(String url, ConstraintValidatorContext context) {
        // Check length first (cheap operation)
        if (url.length() > MAX_AVATAR_URL_LENGTH) {
            context.buildConstraintViolationWithTemplate(
                            "Avatar URL is too long (max " + MAX_AVATAR_URL_LENGTH + " characters)"
                    )
                    .addPropertyNode("avatarUrl")
                    .addConstraintViolation();
            return false;
        }

        // Use Apache Commons Validator for robust URL validation
        if (!URL_VALIDATOR.isValid(url)) {
            context.buildConstraintViolationWithTemplate(
                            "Avatar URL is not a valid HTTP/HTTPS URL"
                    )
                    .addPropertyNode("avatarUrl")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

    /**
     * Validate bio text.
     *
     * SECURITY NOTE: We DON'T strip HTML here.
     * Reasons:
     * 1. Validate, don't mutate (philosophy)
     * 2. HTML sanitization should use OWASP library, not regex
     * 3. Sanitization happens in Service layer, not validation layer
     *
     * We only check for:
     * - Length limits
     * - Null bytes (security issue)
     * - Obviously dangerous patterns
     */
    private boolean validateBio(String bio, ConstraintValidatorContext context) {
        // Check length
        if (bio.length() > MAX_BIO_LENGTH) {
            context.buildConstraintViolationWithTemplate(
                            "Bio is too long (max " + MAX_BIO_LENGTH + " characters)"
                    )
                    .addPropertyNode("bio")
                    .addConstraintViolation();
            return false;
        }

        // Check for null bytes (serious security issue)
        if (bio.contains("\0")) {
            context.buildConstraintViolationWithTemplate(
                            "Bio contains invalid characters"
                    )
                    .addPropertyNode("bio")
                    .addConstraintViolation();
            return false;
        }

        // Check for obvious script injection attempts
        // Note: This is NOT comprehensive XSS prevention!
        // Full sanitization should be done with OWASP Java HTML Sanitizer
        String lowerBio = bio.toLowerCase();
        if (lowerBio.contains("<script") || lowerBio.contains("javascript:")) {
            context.buildConstraintViolationWithTemplate(
                            "Bio contains potentially dangerous content"
                    )
                    .addPropertyNode("bio")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

    /**
     * Validate config object.
     */
    private boolean validateConfig(UserProfile.UserConfig config, ConstraintValidatorContext context) {
        boolean isValid = true;

        // Validate theme if present
        if (config.getTheme() != null && !config.getTheme().isEmpty()) {
            String theme = config.getTheme().toLowerCase();

            if (!ALLOWED_THEMES.contains(theme)) {
                context.buildConstraintViolationWithTemplate(
                                "Theme must be one of: " + String.join(", ", ALLOWED_THEMES)
                        )
                        .addPropertyNode("config.theme")
                        .addConstraintViolation();
                isValid = false;
            }
        }

        if (config.getRetroStyle() != null && !config.getRetroStyle().isEmpty()) {
            String retroStyle = config.getRetroStyle().toLowerCase();

            if (!ALLOWED_RETRO_STYLES.contains(retroStyle)) {
                context.buildConstraintViolationWithTemplate(
                                "Retro style must be one of: " + String.join(", ", ALLOWED_RETRO_STYLES)
                        )
                        .addPropertyNode("config.retroStyle")
                        .addConstraintViolation();
                isValid = false;
            }
        }

        if (config.getExigencyLevel() != null && !config.getExigencyLevel().isEmpty()) {
            String exigencyLevel = config.getExigencyLevel().toLowerCase();

            if (!ALLOWED_EXIGENCY_LEVELS.contains(exigencyLevel)) {
                context.buildConstraintViolationWithTemplate(
                                "Exigency level must be one of: " + String.join(", ", ALLOWED_EXIGENCY_LEVELS)
                        )
                        .addPropertyNode("config.exigencyLevel")
                        .addConstraintViolation();
                isValid = false;
            }
        }

        if (config.getLanguage() != null && !config.getLanguage().isEmpty()) {
            String language = config.getLanguage().toLowerCase();

            if (!ALLOWED_LANGUAGES.contains(language)) {
                context.buildConstraintViolationWithTemplate(
                                "Language must be one of: " + String.join(", ", ALLOWED_LANGUAGES)
                        )
                        .addPropertyNode("config.language")
                        .addConstraintViolation();
                isValid = false;
            }
        }

        return isValid;
    }
}
