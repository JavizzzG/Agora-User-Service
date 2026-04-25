package com.app.userservice.service;

import com.app.userservice.client.AuthServiceClient;
import com.app.userservice.dto.CreateUserRequest;
import com.app.userservice.dto.UpdateUserRequest;
import com.app.userservice.dto.UserResponse;
import com.app.userservice.exception.DuplicateEmailException;
import com.app.userservice.exception.UserNotFoundException;
import com.app.userservice.mapper.UserMapper;
import com.app.userservice.model.User;
import com.app.userservice.model.UserProfile;
import com.app.userservice.repository.UserRepository;
import com.app.userservice.util.DataSanitizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private DataSanitizer dataSanitizer;

    @Mock
    private AuthServiceClient authServiceClient;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private CreateUserRequest createRequest;
    private UpdateUserRequest updateRequest;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        
        // Create test user
        testUser = User.builder()
                .id(userId)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .isAdmin(false)
                .profile(UserProfile.builder()
                        .bio("Test bio")
                        .phone("+1234567890")
                        .config(UserProfile.UserConfig.builder()
                                .theme("dark")
                                .newSubmission(true)
                                .newGrading(false)
                                .submissionAlert(true)
                                .sendEmailNotification(true)
                                .agenticMode(false)
                                .retroStyle("detailed")
                                .exigencyLevel("moderated")
                                .weeklyReport(true)
                                .build())
                        .build())
                .build();

        // Create test requests
        createRequest = CreateUserRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("password123")
                .isAdmin(false)
                .profile(UserProfile.builder()
                        .bio("Test bio")
                        .phone("+1234567890")
                        .config(UserProfile.UserConfig.builder()
                                .theme("dark")
                                .newSubmission(true)
                                .newGrading(false)
                                .submissionAlert(true)
                                .sendEmailNotification(true)
                                .agenticMode(false)
                                .retroStyle("detailed")
                                .exigencyLevel("moderated")
                                .weeklyReport(true)
                                .build())
                        .build())
                .build();

        updateRequest = UpdateUserRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .isAdmin(true)
                .profile(UserProfile.builder()
                        .bio("Updated bio")
                        .phone("+0987654321")
                        .config(UserProfile.UserConfig.builder()
                                .theme("LIGHT")
                                .newSubmission(false)
                                .newGrading(true)
                                .submissionAlert(false)
                                .sendEmailNotification(false)
                                .agenticMode(true)
                                .retroStyle("FULL")
                                .exigencyLevel("STRICT")
                                .weeklyReport(false)
                                .build())
                        .build())
                .build();

        lenient().when(dataSanitizer.sanitizeName(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(dataSanitizer.sanitizeEmail(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(dataSanitizer.sanitizeBio(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(dataSanitizer.sanitizePhone(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(dataSanitizer.sanitizeUrl(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(dataSanitizer.sanitizeTheme(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(dataSanitizer.sanitizeRetroStyle(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(dataSanitizer.sanitizeExigencyLevel(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Should create user successfully when email is unique")
    void createUser_Success_WhenEmailIsUnique() {
        // Given
        when(userRepository.existsByEmail(createRequest.getEmail())).thenReturn(false);
        when(userMapper.toEntity(createRequest)).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(createUserResponse());

        // When
        UserResponse result = userService.createUser(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(createRequest.getEmail());
        
        verify(userRepository).existsByEmail(createRequest.getEmail());
        verify(userMapper).toEntity(createRequest);
        verify(userRepository).save(any(User.class));
        verify(authServiceClient).registerCredentials(userId, createRequest.getEmail(), "password123", "password");
        verify(userMapper).toResponse(testUser);
        verify(dataSanitizer).sanitizeName("John");
        verify(dataSanitizer).sanitizeName("Doe");
        verify(dataSanitizer).sanitizeEmail("john.doe@example.com");
    }

    @Test
    @DisplayName("Should throw DuplicateEmailException when email already exists")
    void createUser_ThrowsException_WhenEmailExists() {
        // Given
        when(userRepository.existsByEmail(createRequest.getEmail())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(createRequest))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining(createRequest.getEmail());

        verify(userRepository).existsByEmail(createRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
        verify(authServiceClient, never()).registerCredentials(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should get user by ID successfully")
    void getUserById_Success_WhenUserExists() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userMapper.toResponse(testUser)).thenReturn(createUserResponse());

        // When
        UserResponse result = userService.getUserById(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getEmail()).isEqualTo(testUser.getEmail());

        verify(userRepository).findById(userId);
        verify(userMapper).toResponse(testUser);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user ID doesn't exist")
    void getUserById_ThrowsException_WhenUserNotFound() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(userId.toString());

        verify(userRepository).findById(userId);
        verify(userMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Should get user by email successfully")
    void getUserByEmail_Success_WhenUserExists() {
        // Given
        String email = "john.doe@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(userMapper.toResponse(testUser)).thenReturn(createUserResponse());

        // When
        UserResponse result = userService.getUserByEmail(email);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);

        verify(userRepository).findByEmail(email);
        verify(userMapper).toResponse(testUser);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when email doesn't exist")
    void getUserByEmail_ThrowsException_WhenUserNotFound() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserByEmail(email))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(email);

        verify(userRepository).findByEmail(email);
        verify(userMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Should update user successfully")
    void updateUser_Success_WhenUserExists() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(updateRequest.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(createUserResponse());

        // When
        UserResponse result = userService.updateUser(userId, updateRequest);

        // Then
        assertThat(result).isNotNull();

        verify(userRepository).findById(userId);
        verify(userRepository).existsByEmail(updateRequest.getEmail());
        verify(userMapper).updateEntityFromRequest(testUser, updateRequest);
        verify(userRepository).save(testUser);
        verify(userMapper).toResponse(testUser);
        
        // Verify sanitization
        verify(dataSanitizer).sanitizeName("Jane");
        verify(dataSanitizer).sanitizeName("Smith");
        verify(dataSanitizer).sanitizeEmail("jane.smith@example.com");
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when updating non-existent user")
    void updateUser_ThrowsException_WhenUserNotFound() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(userId, updateRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(userId.toString());

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DuplicateEmailException when updating to existing email")
    void updateUser_ThrowsException_WhenEmailExists() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(updateRequest.getEmail())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(userId, updateRequest))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining(updateRequest.getEmail());

        verify(userRepository).findById(userId);
        verify(userRepository).existsByEmail(updateRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should delete user successfully")
    void deleteUser_Success_WhenUserExists() {
        // Given
        when(userRepository.existsById(userId)).thenReturn(true);

        // When
        userService.deleteUser(userId);

        // Then
        verify(userRepository).existsById(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when deleting non-existent user")
    void deleteUser_ThrowsException_WhenUserNotFound() {
        // Given
        when(userRepository.existsById(userId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(userId.toString());

        verify(userRepository).existsById(userId);
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should check if user exists by email")
    void existsByEmail_ReturnsCorrectValue() {
        // Given
        String email = "test@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // When
        boolean result = userService.existsByEmail(email);

        // Then
        assertThat(result).isTrue();
        verify(userRepository).existsByEmail(email);
    }

    @Test
    @DisplayName("Should sanitize request data during creation")
    void createUser_SanitizesRequestData() {
        // Given
        when(userRepository.existsByEmail(createRequest.getEmail())).thenReturn(false);
        when(userMapper.toEntity(createRequest)).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(createUserResponse());

        // When
        userService.createUser(createRequest);

        // Then - Verify sanitization methods were called
        verify(dataSanitizer).sanitizeName("John");
        verify(dataSanitizer).sanitizeName("Doe");
        verify(dataSanitizer).sanitizeEmail("john.doe@example.com");
        verify(dataSanitizer).sanitizeBio("Test bio");
        verify(dataSanitizer).sanitizePhone("+1234567890");
        verify(dataSanitizer).sanitizeTheme("dark");
        verify(dataSanitizer).sanitizeRetroStyle("detailed");
        verify(dataSanitizer).sanitizeExigencyLevel("moderated");
        assertThat(createRequest.getProfile().getConfig().getNewSubmission()).isTrue();
        assertThat(createRequest.getProfile().getConfig().getNewGrading()).isFalse();
        assertThat(createRequest.getProfile().getConfig().getSubmissionAlert()).isTrue();
        assertThat(createRequest.getProfile().getConfig().getSendEmailNotification()).isTrue();
        assertThat(createRequest.getProfile().getConfig().getAgenticMode()).isFalse();
        assertThat(createRequest.getProfile().getConfig().getWeeklyReport()).isTrue();
    }

    @Test
    @DisplayName("Should sanitize request data during update")
    void updateUser_SanitizesRequestData() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(updateRequest.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(createUserResponse());

        // When
        userService.updateUser(userId, updateRequest);

        // Then - Verify sanitization methods were called
        verify(dataSanitizer).sanitizeName("Jane");
        verify(dataSanitizer).sanitizeName("Smith");
        verify(dataSanitizer).sanitizeEmail("jane.smith@example.com");
        verify(dataSanitizer).sanitizeBio("Updated bio");
        verify(dataSanitizer).sanitizePhone("+0987654321");
        verify(dataSanitizer).sanitizeTheme("LIGHT");
        verify(dataSanitizer).sanitizeRetroStyle("FULL");
        verify(dataSanitizer).sanitizeExigencyLevel("STRICT");
        assertThat(updateRequest.getProfile().getConfig().getNewSubmission()).isFalse();
        assertThat(updateRequest.getProfile().getConfig().getNewGrading()).isTrue();
        assertThat(updateRequest.getProfile().getConfig().getSubmissionAlert()).isFalse();
        assertThat(updateRequest.getProfile().getConfig().getSendEmailNotification()).isFalse();
        assertThat(updateRequest.getProfile().getConfig().getAgenticMode()).isTrue();
        assertThat(updateRequest.getProfile().getConfig().getWeeklyReport()).isFalse();
    }

    private UserResponse createUserResponse() {
        return UserResponse.builder()
                .id(userId)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .isAdmin(false)
                .profile(UserProfile.builder()
                        .bio("Test bio")
                        .phone("+1234567890")
                        .config(UserProfile.UserConfig.builder()
                                .theme("dark")
                                .newSubmission(true)
                                .newGrading(false)
                                .submissionAlert(true)
                                .sendEmailNotification(true)
                                .agenticMode(false)
                                .retroStyle("detailed")
                                .exigencyLevel("moderated")
                                .weeklyReport(true)
                                .build())
                        .build())
                .build();
    }
}
