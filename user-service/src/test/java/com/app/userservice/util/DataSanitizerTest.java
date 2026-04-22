package com.app.userservice.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("DataSanitizer Tests")
class DataSanitizerTest {

    private DataSanitizer dataSanitizer;

    @BeforeEach
    void setUp() {
        dataSanitizer = new DataSanitizer();
    }

    // ============================================================
    // BASIC SANITIZATION TESTS
    // ============================================================

    @Test
    @DisplayName("Should sanitize text by trimming and normalizing spaces")
    void sanitizeText_TrimsAndNormalizesSpaces() {
        // Given
        String input = "  John   Doe  ";

        // When
        String result = dataSanitizer.sanitizeText(input);

        // Then
        assertThat(result).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should handle null in sanitizeText")
    void sanitizeText_ReturnsNull_WhenInputIsNull() {
        // When
        String result = dataSanitizer.sanitizeText(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should handle empty string in sanitizeText")
    void sanitizeText_ReturnsEmpty_WhenInputIsEmpty() {
        // When
        String result = dataSanitizer.sanitizeText("");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should sanitize name by removing control characters")
    void sanitizeName_RemovesControlCharacters() {
        // Given
        String input = "John\nDoe";

        // When
        String result = dataSanitizer.sanitizeName(input);

        // Then
        assertThat(result).isEqualTo("JohnDoe");
    }

    @Test
    @DisplayName("Should preserve capitalization in names")
    void sanitizeName_PreservesCapitalization() {
        // Given
        String[] names = {
                "McDonald",
                "van Gogh",
                "O'Brien",
                "Jean-Pierre"
        };

        // When & Then
        for (String name : names) {
            assertThat(dataSanitizer.sanitizeName(name)).isEqualTo(name);
        }
    }

    @Test
    @DisplayName("Should handle null in sanitizeName")
    void sanitizeName_ReturnsNull_WhenInputIsNull() {
        // When
        String result = dataSanitizer.sanitizeName(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should normalize email to lowercase")
    void sanitizeEmail_ConvertsToLowercase() {
        // Given
        String input = "John.Doe@EXAMPLE.COM";

        // When
        String result = dataSanitizer.sanitizeEmail(input);

        // Then
        assertThat(result).isEqualTo("john.doe@example.com");
    }

    @Test
    @DisplayName("Should trim email whitespace")
    void sanitizeEmail_TrimsWhitespace() {
        // Given
        String input = "  john.doe@example.com  ";

        // When
        String result = dataSanitizer.sanitizeEmail(input);

        // Then
        assertThat(result).isEqualTo("john.doe@example.com");
    }

    @Test
    @DisplayName("Should handle null in sanitizeEmail")
    void sanitizeEmail_ReturnsNull_WhenInputIsNull() {
        // When
        String result = dataSanitizer.sanitizeEmail(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should format phone numbers correctly")
    void sanitizePhone_FormatsPhoneNumbers() {
        // Given
        String[] phones = {
                "+57 (300) 123-4567",
                "+1-202-555-1234",
                "+44 20 7946 0958"
        };

        String[] expected = {
                "+57-3001234567",
                "+1-2025551234",
                "+44-2079460958"
        };

        // When & Then
        for (int i = 0; i < phones.length; i++) {
            assertThat(dataSanitizer.sanitizePhone(phones[i]))
                    .isEqualTo(expected[i]);
        }
    }

    @Test
    @DisplayName("Should handle phone without separator")
    void sanitizePhone_ReturnsAsIs_WhenNoSeparator() {
        // Given
        String phone = "+573001234567";

        // When
        String result = dataSanitizer.sanitizePhone(phone);

        // Then
        assertThat(result).isEqualTo(phone);
    }

    @Test
    @DisplayName("Should handle null in sanitizePhone")
    void sanitizePhone_ReturnsNull_WhenInputIsNull() {
        // When
        String result = dataSanitizer.sanitizePhone(null);

        // Then
        assertThat(result).isNull();
    }

    // ============================================================
    // URL HANDLING TESTS
    // ============================================================

    @Test
    @DisplayName("Should trim URL whitespace")
    void sanitizeUrl_TrimsWhitespace() {
        // Given
        String input = "  https://example.com/avatar.jpg  ";

        // When
        String result = dataSanitizer.sanitizeUrl(input);

        // Then
        assertThat(result).isEqualTo("https://example.com/avatar.jpg");
    }

    @Test
    @DisplayName("Should handle null in sanitizeUrl")
    void sanitizeUrl_ReturnsNull_WhenInputIsNull() {
        // When
        String result = dataSanitizer.sanitizeUrl(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should normalize URL by adding https://")
    void normalizeUrl_AddsHttpsWhenMissing() {
        // Given
        String input = "example.com/avatar.jpg";

        // When
        String result = dataSanitizer.normalizeUrl(input);

        // Then
        assertThat(result).isEqualTo("https://example.com/avatar.jpg");
    }

    @Test
    @DisplayName("Should not modify URL when protocol exists")
    void normalizeUrl_KeepsProtocol_WhenExists() {
        // Given
        String[] urls = {
                "https://example.com/avatar.jpg",
                "http://example.com/avatar.jpg"
        };

        // When & Then
        for (String url : urls) {
            assertThat(dataSanitizer.normalizeUrl(url)).isEqualTo(url);
        }
    }

    // ============================================================
    // HTML SANITIZATION TESTS
    // ============================================================

    @Test
    @DisplayName("Should sanitize HTML by removing dangerous tags")
    void sanitizeHtml_RemovesDangerousTags() {
        // Given
        String input = "<b>Hello</b><script>alert('xss')</script>";

        // When
        String result = dataSanitizer.sanitizeHtml(input);

        // Then
        assertThat(result).isEqualTo("<b>Hello</b>");
    }

    @Test
    @DisplayName("Should allow safe HTML formatting")
    void sanitizeHtml_AllowsSafeFormatting() {
        // Given
        String input = "<p><b>Bold text</b> and <i>italic text</i></p>";

        // When
        String result = dataSanitizer.sanitizeHtml(input);

        // Then
        assertThat(result).isEqualTo("<p><b>Bold text</b> and <i>italic text</i></p>");
    }

    @Test
    @DisplayName("Should handle null in sanitizeHtml")
    void sanitizeHtml_ReturnsNull_WhenInputIsNull() {
        // When
        String result = dataSanitizer.sanitizeHtml(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should strip all HTML in strict mode")
    void sanitizeHtmlStrict_StripsAllHtml() {
        // Given
        String input = "<b>Hello</b> <script>alert('xss')</script> world";

        // When
        String result = dataSanitizer.sanitizeHtmlStrict(input);

        // Then
        assertThat(result).isEqualTo("Hello alert('xss') world");
    }

    // ============================================================
    // OUTPUT ESCAPING TESTS
    // ============================================================

    @Test
    @DisplayName("Should escape HTML for output")
    void escapeHtmlForOutput_EscapesHtml() {
        // Given
        String input = "<script>alert('xss')</script>";

        // When
        String result = dataSanitizer.escapeHtmlForOutput(input);

        // Then
        assertThat(result).isEqualTo("&lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;");
    }

    @Test
    @DisplayName("Should handle null in escapeHtmlForOutput")
    void escapeHtmlForOutput_ReturnsNull_WhenInputIsNull() {
        // When
        String result = dataSanitizer.escapeHtmlForOutput(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should escape HTML using Apache Commons")
    void escapeHtmlApache_EscapesHtml() {
        // Given
        String input = "<script>alert('xss')</script>";

        // When
        String result = dataSanitizer.escapeHtmlApache(input);

        // Then
        assertThat(result).isEqualTo("&lt;script&gt;alert('xss')&lt;/script&gt;");
    }

    // ============================================================
    // CONTROL CHARACTER REMOVAL TESTS
    // ============================================================

    @Test
    @DisplayName("Should remove control characters")
    void removeControlCharacters_RemovesControlChars() {
        // Given
        String input = "John\nDoe\r\n\tTest";

        // When
        String result = dataSanitizer.removeControlCharacters(input);

        // Then
        assertThat(result).isEqualTo("JohnDoeTest");
    }

    @Test
    @DisplayName("Should handle null in removeControlCharacters")
    void removeControlCharacters_ReturnsNull_WhenInputIsNull() {
        // When
        String result = dataSanitizer.removeControlCharacters(null);

        // Then
        assertThat(result).isNull();
    }

    // ============================================================
    // DOMAIN-SPECIFIC SANITIZATION TESTS
    // ============================================================

    @Test
    @DisplayName("Should sanitize theme values")
    void sanitizeTheme_NormalizesTheme() {
        // Given
        String[] inputs = {
                "dark",
                "DARK",
                "Dark",
                "light",
                "auto",
                "invalid",
                null,
                ""
        };

        String[] expected = {
                "dark",
                "dark",
                "dark",
                "light",
                "auto",
                "light", // fallback
                "light", // fallback for null
                "light"  // fallback for empty
        };

        // When & Then
        for (int i = 0; i < inputs.length; i++) {
            assertThat(dataSanitizer.sanitizeTheme(inputs[i]))
                    .as("Input: %s", inputs[i])
                    .isEqualTo(expected[i]);
        }
    }

    @Test
    @DisplayName("Should sanitize bio text")
    void sanitizeBio_RemovesControlCharacters() {
        // Given
        String input = "  Software developer\nwith experience  ";

        // When
        String result = dataSanitizer.sanitizeBio(input);

        // Then
        assertThat(result).isEqualTo("Software developerwith experience");
    }

    @Test
    @DisplayName("Should handle null in sanitizeBio")
    void sanitizeBio_ReturnsNull_WhenInputIsNull() {
        // When
        String result = dataSanitizer.sanitizeBio(null);

        // Then
        assertThat(result).isNull();
    }

    // ============================================================
    // SECURITY TESTS
    // ============================================================

    @Test
    @DisplayName("Should handle XSS attempts in HTML sanitization")
    void sanitizeHtml_HandlesXssAttempts() {
        // Given
        String[] xssAttempts = {
                "<script>alert('xss')</script>",
                "<img src=x onerror=alert(1)>",
                "javascript:alert('xss')",
                "<svg onload=alert(1)>"
        };

        // When & Then
        for (String xss : xssAttempts) {
            String result = dataSanitizer.sanitizeHtml(xss);
            assertThat(result).doesNotContain("<script>")
                           .doesNotContain("onerror")
                           .doesNotContain("onload");
        }
    }

    @Test
    @DisplayName("Should handle null byte injection")
    void sanitizeName_HandlesNullByteInjection() {
        // Given
        String input = "John\0Doe";

        // When
        String result = dataSanitizer.sanitizeName(input);

        // Then
        assertThat(result).doesNotContain("\0");
    }

    @Test
    @DisplayName("Should preserve safe characters in names")
    void sanitizeName_PreservesSafeCharacters() {
        // Given
        String[] safeNames = {
                "José María",
                "Jean-Pierre O'Connor",
                "François-Marie d'Artagnan",
                "Hans-Christian Müller"
        };

        // When & Then
        for (String name : safeNames) {
            assertThat(dataSanitizer.sanitizeName(name)).isEqualTo(name);
        }
    }

    @Test
    @DisplayName("Should handle complex phone number formats")
    void sanitizePhone_HandlesComplexFormats() {
        // Given
        String[] phones = {
                "+1 (555) 123-4567",
                "+44 20 7946 0958",
                "+33 1 42 86 83 26",
                "+61 2 9876 5432"
        };

        // When & Then
        for (String phone : phones) {
            String result = dataSanitizer.sanitizePhone(phone);
            assertThat(result).matches("^\\+[0-9]+-[0-9]+$");
        }
    }

    @Test
    @DisplayName("Should handle edge cases consistently")
    void sanitizeMethods_HandleEdgeCases() {
        // Given
        String[] edgeCases = {null, "", " ", "   "};

        // When & Then
        for (String edgeCase : edgeCases) {
            assertThat(dataSanitizer.sanitizeText(edgeCase)).isIn(null, "", " ");
            assertThat(dataSanitizer.sanitizeName(edgeCase)).isIn(null, "", " ");
            assertThat(dataSanitizer.sanitizeEmail(edgeCase)).isIn(null, "", " ");
            assertThat(dataSanitizer.sanitizePhone(edgeCase)).isIn(null, "", " ");
            assertThat(dataSanitizer.sanitizeUrl(edgeCase)).isIn(null, "", " ");
            assertThat(dataSanitizer.sanitizeBio(edgeCase)).isIn(null, "", " ");
        }
    }
}
