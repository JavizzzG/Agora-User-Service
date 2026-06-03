package com.app.userservice.util;

import org.apache.commons.text.StringEscapeUtils;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

/**
 * Professional data sanitization utility.
 *
 * PHILOSOPHY:
 * 1. "Validate, don't mutate" - Only modify when universally safe
 * 2. Use libraries for security-critical operations (HTML, XSS)
 * 3. Document all modifications clearly
 * 4. Separate sanitization from business logic
 *
 * WHEN TO USE MANUAL vs LIBRARY:
 *
 * MANUAL (Simple operations):
 * ✅ trim()              - Remove whitespace
 * ✅ toLowerCase()       - Normalize emails (RFC allows)
 * ✅ replaceAll("\\s")   - Remove formatting from phone numbers
 * ✅ Control character removal
 *
 * LIBRARY (Security operations):
 * ✅ HTML sanitization   - OWASP Java HTML Sanitizer
 * ✅ URL validation      - Apache Commons Validator
 * ✅ HTML escaping       - Apache Commons Text / Spring HtmlUtils
 * ✅ Complex parsing     - Jsoup, Jackson, etc.
 *
 * DEPENDENCIES REQUIRED:
 * - com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer
 * - org.apache.commons:commons-text
 * - org.springframework:spring-web (included in spring-boot-starter-web)
 *
 * @author Your Name
 * @since 1.0.0
 */
@Component
public class DataSanitizer {

    // OWASP HTML Sanitizer policy
    // Allows safe formatting (bold, italic, links) but blocks dangerous HTML
    private final PolicyFactory htmlPolicy = Sanitizers.FORMATTING
            .and(Sanitizers.LINKS)
            .and(Sanitizers.BLOCKS);

    // ============================================================
    // BASIC SANITIZATION (Manual - Simple and Safe)
    // ============================================================

    /**
     * Basic text sanitization - ONLY trim and normalize spaces.
     *
     * WHAT IT DOES:
     * - Removes leading/trailing whitespace
     * - Replaces multiple spaces with single space
     *
     * WHAT IT DOESN'T DO:
     * - Change capitalization (preserves "McDonald", "van Gogh")
     * - Remove user content
     * - Modify special characters
     *
     * USE FOR: Almost any text input
     *
     * EXAMPLE:
     * "  John   Doe  " → "John Doe"
     */
    public String sanitizeText(String input) {
        if (input == null) {
            return null;
        }

        return input.trim()
                .replaceAll(" +", " ");  // Multiple spaces → single space
    }

    /**
     * Sanitize name fields (firstName, lastName).
     *
     * PHILOSOPHY: Validate, DON'T mutate.
     * We DON'T capitalize because:
     * - "McDonald" → "Mcdonald" ❌
     * - "van Gogh" → "Van Gogh" ❌
     * - "O'Brien" stays "O'Brien" ✅
     *
     * WHAT IT DOES:
     * - Trim whitespace
     * - Remove control characters (security)
     * - Preserve user's capitalization
     *
     * USE FOR: firstName, lastName, displayName
     *
     * WHY MANUAL: Simple operation, no edge cases
     */
    public String sanitizeName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }

        // Trim
        String sanitized = name.trim();

        // Remove only truly dangerous characters (null bytes, control chars)
        // Keep accents, hyphens, apostrophes - they're valid in names
        sanitized = sanitized.replaceAll("\\p{Cntrl}", "");

        // Normalize multiple spaces
        sanitized = sanitized.replaceAll(" +", " ");

        return sanitized;
    }

    /**
     * Sanitize email - normalize to lowercase.
     *
     * WHY LOWERCASE IS SAFE:
     * RFC 5321 Section 2.3.11: Email addresses are case-insensitive
     * "John@Example.COM" === "john@example.com"
     *
     * WHAT IT DOES:
     * - Convert to lowercase
     * - Trim whitespace
     *
     * USE FOR: All email fields
     *
     * WHY MANUAL: Simple, universally safe operation
     */
    public String sanitizeEmail(String email) {
        if (email == null || email.isEmpty()) {
            return email;
        }

        return email.trim().toLowerCase();
    }

    /**
     * Sanitize phone number to E.164 format.
     *
     * WHAT IT DOES:
     * - Removes spaces, hyphens, parentheses
     * - Keeps only + and digits
     *
     * EXAMPLES:
     * "+57 (300) 123-4567" → "+573001234567"
     * "+1-202-555-1234"    → "+12025551234"
     *
     * USE FOR: Phone numbers (after validation)
     *
     * WHY MANUAL: Phone formatting is domain-specific
     */
    public String sanitizePhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return phone;
        }

        String sanitized = phone.trim();
        String[] array = sanitized.split("[-\\s]", 2);
        
        // Handle case where no space/hyphen found
        if (array.length == 1) {
            return sanitized; // Return as-is if no separator
        }
        
        String countryCode = array[0].replaceAll("[^0-9]", "");
        String number = array[1].replaceAll("[^0-9]", "");

        sanitized = "+" + countryCode + "-" + number;

        return sanitized;
    }

    // ============================================================
    // URL HANDLING (Business Logic - NOT Security)
    // ============================================================

    /**
     * Basic URL sanitization - ONLY trim.
     *
     * IMPORTANT: This does NOT add https:// automatically.
     * That's a business decision, not sanitization.
     *
     * If you WANT to auto-add protocol, use normalizeUrl() instead.
     *
     * USE FOR: When you want to reject URLs without protocol
     */
    public String sanitizeUrl(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }

        return url.trim();
    }

    /**
     * Normalize URL by adding https:// if missing.
     *
     * ⚠️ WARNING: This is BUSINESS LOGIC, not security sanitization.
     * Only use this if your business requirements allow auto-correction.
     *
     * USE CASES:
     * ✅ Social media app (better UX)
     * ✅ Content management (help users)
     * ❌ Banking app (reject invalid, don't guess)
     * ❌ API contracts (strict validation)
     *
     * DECISION: Talk to your team/PM about this behavior
     */
    public String normalizeUrl(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }

        String normalized = url.trim();

        // Only add https:// if there's no protocol
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "https://" + normalized;
        }

        return normalized;
    }

    // ============================================================
    // HTML SANITIZATION (LIBRARY - Security Critical)
    // ============================================================

    /**
     * Sanitize HTML using OWASP Java HTML Sanitizer.
     *
     * ⭐ THIS IS THE PROFESSIONAL WAY TO HANDLE HTML
     *
     * WHY USE LIBRARY:
     * ❌ Regex for XSS prevention is NEVER secure
     * ❌ You can't think of all edge cases: <sCrIpT>, <script , etc.
     * ✅ OWASP library is tested by security experts
     * ✅ Updated with new attack vectors
     * ✅ Allows safe HTML (formatting) while blocking dangerous HTML
     *
     * WHAT IT ALLOWS:
     * ✅ <b>, <i>, <u> (formatting)
     * ✅ <a href="..."> (links)
     * ✅ <p>, <div>, <h1-h6> (structure)
     *
     * WHAT IT BLOCKS:
     * ❌ <script> (XSS)
     * ❌ <iframe> (embedding)
     * ❌ javascript: in links
     * ❌ onerror, onload, etc.
     *
     * USE FOR: User-generated rich content (bio, comments, posts)
     *
     * EXAMPLE:
     * Input:  "<b>Hello</b><script>alert('xss')</script>"
     * Output: "<b>Hello</b>"
     *
     * DEPENDENCY REQUIRED:
     * com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer:20220608.1
     */
    public String sanitizeHtml(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }

        // Use OWASP policy to sanitize
        return htmlPolicy.sanitize(html);
    }

    /**
     * Strict HTML sanitization - allows NO HTML at all.
     *
     * USE FOR: Fields that should never contain HTML (names, titles)
     *
     * EXAMPLE:
     * Input:  "<b>Hello</b> world"
     * Output: "Hello world"
     */
    public String sanitizeHtmlStrict(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }

        // Strip ALL HTML tags
        return html.replaceAll("<[^>]*>", "")
                .trim();
    }

    // ============================================================
    // OUTPUT ESCAPING (For Responses - NOT for Storage)
    // ============================================================

    /**
     * Escape HTML for output (use in responses, NOT for storage).
     *
     * IMPORTANT: This is for OUTPUT, not INPUT.
     *
     * WHEN TO USE:
     * ✅ Generating HTML responses
     * ✅ Embedding user data in HTML templates
     * ❌ Storing in database (store original, escape on output)
     *
     * BETTER APPROACH:
     * Let your frontend framework handle this:
     * - React: {userInput} automatically escapes
     * - Vue: {{ userInput }} automatically escapes
     * - Thymeleaf: th:text="${userInput}" automatically escapes
     *
     * EXAMPLE:
     * Input:  "<script>alert('xss')</script>"
     * Output: "&lt;script&gt;alert('xss')&lt;/script&gt;"
     *
     * WHY USE LIBRARY:
     * ✅ Handles all edge cases (', ", &, <, >)
     * ✅ Tested and maintained
     * ❌ Manual escaping always misses something
     */
    public String escapeHtmlForOutput(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Option 1: Spring's built-in (lighter, good enough)
        return HtmlUtils.htmlEscape(input);

        // Option 2: Apache Commons Text (more comprehensive)
        // return StringEscapeUtils.escapeHtml4(input);
    }

    /**
     * Escape HTML using Apache Commons Text.
     * More comprehensive than Spring's HtmlUtils.
     *
     * USE FOR: Complex escaping scenarios
     */
    public String escapeHtmlApache(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        return StringEscapeUtils.escapeHtml4(input);
    }

    // ============================================================
    // CONTROL CHARACTER REMOVAL (Security)
    // ============================================================

    /**
     * Remove control characters (security).
     *
     * WHAT ARE CONTROL CHARACTERS:
     * - Null byte: \0
     * - Newline: \n
     * - Carriage return: \r
     * - Tab: \t
     * - etc.
     *
     * WHY REMOVE:
     * ✅ Null bytes can break some systems
     * ✅ Prevent log injection attacks
     * ✅ Normalize whitespace
     *
     * USE FOR: Any user input before storage
     *
     * WHY MANUAL: Simple regex, no edge cases
     */
    public String removeControlCharacters(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Remove all control characters except space
        return input.replaceAll("\\p{Cntrl}", "");
    }

    // ============================================================
    // DOMAIN-SPECIFIC SANITIZATION
    // ============================================================

    /**
     * Sanitize theme value (domain-specific).
     *
     * This is an example of domain-specific sanitization.
     * We KNOW the valid values, so we can normalize.
     *
     * WHAT IT DOES:
     * - Converts to lowercase
     * - Returns default if invalid
     *
     * USE FOR: Enum-like values where you have a closed set
     */
    public String sanitizeTheme(String theme) {
        if (theme == null || theme.isEmpty()) {
            return "light"; // Default
        }

        String normalized = theme.trim().toLowerCase();

        // Only allow known values
        return switch (normalized) {
            case "dark", "light", "auto" -> normalized;
            default -> "light"; // Safe fallback
        };
    }

    /**
     * Sanitize AI feedback style.
     */
    public String sanitizeRetroStyle(String retroStyle) {
        if (retroStyle == null || retroStyle.isEmpty()) {
            return "detailed"; // Default
        }

        String normalized = retroStyle.trim().toLowerCase();

        return switch (normalized) {
            case "brief", "detailed", "full" -> normalized;
            default -> "detailed"; // Safe fallback
        };
    }

    /**
     * Sanitize AI grading exigency level.
     */
    public String sanitizeExigencyLevel(String exigencyLevel) {
        if (exigencyLevel == null || exigencyLevel.isEmpty()) {
            return "moderated"; // Default
        }

        String normalized = exigencyLevel.trim().toLowerCase();

        return switch (normalized) {
            case "flexible", "moderated", "strict" -> normalized;
            default -> "moderated"; // Safe fallback
        };
    }

    /**
     * Sanitize language value.
     */
    public String sanitizeLanguage(String language) {
        if (language == null || language.isEmpty()) {
            return "en"; // Default
        }

        String normalized = language.trim().toLowerCase();

        return switch (normalized) {
            case "es", "en", "fr", "pt", "de", "it" -> normalized;
            default -> "en"; // Safe fallback
        };
    }

    /**
     * Sanitize bio text.
     *
     * APPROACH: Minimal sanitization, rely on validation.
     *
     * WHAT IT DOES:
     * - Trim
     * - Remove control characters
     * - Remove null bytes
     *
     * WHAT IT DOESN'T DO:
     * - Strip HTML (use sanitizeHtml() if needed)
     * - Modify content
     * - Capitalize or reformat
     *
     * DECISION POINT: Does your app allow HTML in bio?
     * YES → Use sanitizeHtml() instead
     * NO  → Use this + validation to reject HTML
     */
    public String sanitizeBio(String bio) {
        if (bio == null || bio.isEmpty()) {
            return bio;
        }

        String sanitized = bio.trim();

        // Remove control characters (security)
        sanitized = removeControlCharacters(sanitized);

        return sanitized;
    }
}
