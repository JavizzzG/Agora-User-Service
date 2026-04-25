package com.app.userservice.validation;

import com.app.userservice.model.UserProfile;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValidUserProfileValidator Tests")
class ValidUserProfileValidatorTest {

    private ValidUserProfileValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder;

    @BeforeEach
    void setUp() {
        validator = new ValidUserProfileValidator();
        lenient().when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        lenient().when(violationBuilder.addPropertyNode(anyString())).thenReturn(nodeBuilder);
        lenient().when(nodeBuilder.addConstraintViolation()).thenReturn(context);
    }

    @Test
    @DisplayName("Should accept null profile")
    void isValid_ReturnsTrue_WhenProfileIsNull() {
        assertThat(validator.isValid(null, context)).isTrue();
    }

    @Test
    @DisplayName("Should accept valid profile")
    void isValid_ReturnsTrue_WhenProfileIsValid() {
        UserProfile profile = UserProfile.builder()
                .avatarUrl("https://example.com/avatar.jpg")
                .bio("Software engineer")
                .config(UserProfile.UserConfig.builder()
                        .theme("dark")
                        .newSubmission(true)
                        .newGrading(true)
                        .submissionAlert(true)
                        .sendEmailNotification(true)
                        .agenticMode(true)
                        .retroStyle("detailed")
                        .exigencyLevel("moderated")
                        .weeklyReport(true)
                        .build())
                .build();

        assertThat(validator.isValid(profile, context)).isTrue();
        verify(context).disableDefaultConstraintViolation();
    }

    @Test
    @DisplayName("Should reject invalid avatar URL")
    void isValid_ReturnsFalse_WhenAvatarUrlIsInvalid() {
        UserProfile profile = UserProfile.builder()
                .avatarUrl("javascript:alert(1)")
                .build();

        boolean valid = validator.isValid(profile, context);

        assertThat(valid).isFalse();
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(context).buildConstraintViolationWithTemplate(messageCaptor.capture());
        assertThat(messageCaptor.getValue()).contains("Avatar URL");
    }

    @Test
    @DisplayName("Should reject dangerous bio content")
    void isValid_ReturnsFalse_WhenBioContainsScript() {
        UserProfile profile = UserProfile.builder()
                .bio("Hello <script>alert('xss')</script>")
                .build();

        boolean valid = validator.isValid(profile, context);

        assertThat(valid).isFalse();
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(context).buildConstraintViolationWithTemplate(messageCaptor.capture());
        assertThat(messageCaptor.getValue()).contains("dangerous content");
    }

    @Test
    @DisplayName("Should reject bio with null bytes")
    void isValid_ReturnsFalse_WhenBioContainsNullByte() {
        UserProfile profile = UserProfile.builder()
                .bio("hello\0world")
                .build();

        boolean valid = validator.isValid(profile, context);

        assertThat(valid).isFalse();
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(context).buildConstraintViolationWithTemplate(messageCaptor.capture());
        assertThat(messageCaptor.getValue()).contains("invalid characters");
    }

    @Test
    @DisplayName("Should reject unsupported theme")
    void isValid_ReturnsFalse_WhenThemeIsInvalid() {
        UserProfile profile = UserProfile.builder()
                .config(UserProfile.UserConfig.builder().theme("neon").build())
                .build();

        boolean valid = validator.isValid(profile, context);

        assertThat(valid).isFalse();
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(context).buildConstraintViolationWithTemplate(messageCaptor.capture());
        assertThat(messageCaptor.getValue()).contains("Theme must be one of");
    }

    @Test
    @DisplayName("Should accept allowed themes case-insensitively")
    void isValid_ReturnsTrue_WhenThemeIsUppercaseButAllowed() {
        UserProfile profile = UserProfile.builder()
                .config(UserProfile.UserConfig.builder().theme("DARK").build())
                .build();

        assertThat(validator.isValid(profile, context)).isTrue();
    }

    @Test
    @DisplayName("Should reject unsupported retro style")
    void isValid_ReturnsFalse_WhenRetroStyleIsInvalid() {
        UserProfile profile = UserProfile.builder()
                .config(UserProfile.UserConfig.builder().retroStyle("summary").build())
                .build();

        boolean valid = validator.isValid(profile, context);

        assertThat(valid).isFalse();
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(context).buildConstraintViolationWithTemplate(messageCaptor.capture());
        assertThat(messageCaptor.getValue()).contains("Retro style must be one of");
    }

    @Test
    @DisplayName("Should accept allowed retro style and exigency level case-insensitively")
    void isValid_ReturnsTrue_WhenAiConfigValuesAreUppercaseButAllowed() {
        UserProfile profile = UserProfile.builder()
                .config(UserProfile.UserConfig.builder()
                        .retroStyle("FULL")
                        .exigencyLevel("STRICT")
                        .build())
                .build();

        assertThat(validator.isValid(profile, context)).isTrue();
    }

    @Test
    @DisplayName("Should reject unsupported exigency level")
    void isValid_ReturnsFalse_WhenExigencyLevelIsInvalid() {
        UserProfile profile = UserProfile.builder()
                .config(UserProfile.UserConfig.builder().exigencyLevel("hardcore").build())
                .build();

        boolean valid = validator.isValid(profile, context);

        assertThat(valid).isFalse();
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(context).buildConstraintViolationWithTemplate(messageCaptor.capture());
        assertThat(messageCaptor.getValue()).contains("Exigency level must be one of");
    }
}
