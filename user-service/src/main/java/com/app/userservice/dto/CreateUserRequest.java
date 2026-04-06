package com.app.userservice.dto;

import com.app.userservice.model.UserProfile;
import com.app.userservice.validation.NoSpecialCharacters;
import com.app.userservice.validation.ValidUserProfile;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new user.
 *
 * VALIDATION STRATEGY:
 * 1. Bean Validation annotations for simple rules (@NotBlank, @Size, @Email)
 * 2. Custom validators for complex rules (@NoSpecialCharacters, @ValidUserProfile)
 * 3. Service layer applies sanitization AFTER validation passes
 *
 * VALIDATION FLOW:
 * Request → @Valid in Controller → Bean Validation → Custom Validators → Service Sanitization → DB
 *
 * WHY THIS APPROACH:
 * ✅ Declarative validation (easy to read)
 * ✅ Fails fast (bad data rejected at controller)
 * ✅ Clear error messages for API consumers
 * ✅ Sanitization happens only after validation (cleaner)
 *
 * @author Your Name
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    /**
     * User's first name.
     *
     * VALIDATION RULES:
     * - Required (not null, not empty, not blank)
     * - Min 2 characters (prevents typos like "A")
     * - Max 30 characters (database constraint)
     * - No dangerous special characters (XSS prevention)
     * - Allows: letters, spaces, hyphens, apostrophes, accents
     *
     * EXAMPLES:
     * ✅ "John"
     * ✅ "María"
     * ✅ "Jean-Pierre"
     * ✅ "O'Brien"
     * ❌ "A" (too short)
     * ❌ "<script>" (dangerous)
     * ❌ "John123" (numbers not allowed in names)
     */
    @NotBlank(message = "First name is required")
    @Size(max = 30, message = "First name must be less than 30 characters")
    @NoSpecialCharacters(
            allowSpaces = true,     // First nama
            allowHyphens = true,      // Allow "Jean-Pierre"
            allowApostrophes = true,  // Allow "O'Brien"
            allowAccents = true,      // Allow "José"
            message = "First name contains invalid characters"
    )
    private String firstName;

    /**
     * User's last name.
     *
     * VALIDATION RULES: Same as firstName
     *
     * NOTE: We allow spaces in lastName for compound surnames
     * Example: "van der Sar", "De la Cruz"
     */
    @NotBlank(message = "Last name is required")
    @Size(max = 30, message = "Last name must be less than 30 characters")
    @NoSpecialCharacters(
            allowSpaces = true,       // Allow "van der Sar"
            allowHyphens = true,      // Allow "García-López"
            allowApostrophes = true,  // Allow "O'Neill"
            allowAccents = true,      // Allow "Müller"
            message = "Last name contains invalid characters"
    )
    private String lastName;

    /**
     * User's email address.
     *
     * VALIDATION RULES:
     * - Required
     * - Must be valid email format (Bean Validation regex)
     * - Max 100 characters (database constraint)
     *
     * SANITIZATION (in Service):
     * - Converted to lowercase (RFC 5321 allows)
     * - Trimmed
     *
     * EXAMPLES:
     * ✅ "john@example.com"
     * ✅ "user+tag@domain.co.uk"
     * ❌ "notanemail" (invalid format)
     * ❌ "a@b" (too short, but technically valid - consider business rules)
     *
     * NOTE: @Email uses a regex. For stricter validation, consider
     * Apache Commons EmailValidator or custom validator.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;


    /*
        User password

        Just verify if the password is present and nothing more

        password is in the dto but isn't in the entity



     */

    @NotBlank(message= "Password is required")
    private String password;

    /**
     * Whether user is an administrator.
     *
     * SECURITY NOTE:
     * This field should probably be set server-side, not accepted from client.
     * Consider removing from DTO and setting in Service based on business rules.
     *
     * DEFAULT: false (regular user)
     */
    @Builder.Default
    private Boolean isAdmin = false;

    /**
     * User's profile information (stored as JSONB).
     *
     * VALIDATION:
     * - Uses custom @ValidUserProfile validator
     * - Validates: avatarUrl, bio, phone, config.theme
     * - All fields optional within profile
     *
     * SANITIZATION (in Service):
     * - URLs normalized (optional: add https://)
     * - Phone formatted to E.164
     * - Bio cleaned (optional: HTML sanitization)
     * - Theme normalized to lowercase
     *
     * EXAMPLE:
     * {
     *   "avatarUrl": "https://example.com/avatar.jpg",
     *   "bio": "Software developer",
     *   "phone": "+573001234567",
     *   "config": {
     *     "theme": "dark"
     *   }
     * }
     */
    @ValidUserProfile
    private UserProfile profile;
}
