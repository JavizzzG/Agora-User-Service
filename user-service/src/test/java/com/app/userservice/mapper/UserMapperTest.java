package com.app.userservice.mapper;

import com.app.userservice.dto.CreateUserRequest;
import com.app.userservice.dto.UpdateUserRequest;
import com.app.userservice.dto.UserResponse;
import com.app.userservice.model.User;
import com.app.userservice.model.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserMapper Tests")
class UserMapperTest {

    private UserMapper userMapper;
    private User testUser;
    private CreateUserRequest createRequest;
    private UpdateUserRequest updateRequest;
    private UUID userId;
    private OffsetDateTime createdAt;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
        userId = UUID.randomUUID();
        createdAt = OffsetDateTime.now();

        // Create test user
        testUser = User.builder()
                .id(userId)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .isAdmin(false)
                .profile(UserProfile.builder()
                        .bio("Software developer")
                        .phone("+1234567890")
                        .avatarUrl("https://example.com/avatar.jpg")
                        .config(UserProfile.UserConfig.builder()
                                .theme("dark")
                                .build())
                        .build())
                .createdAt(createdAt)
                .build();

        // Create test requests
        createRequest = CreateUserRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("password123")
                .isAdmin(false)
                .profile(UserProfile.builder()
                        .bio("Software developer")
                        .phone("+1234567890")
                        .avatarUrl("https://example.com/avatar.jpg")
                        .config(UserProfile.UserConfig.builder()
                                .theme("dark")
                                .build())
                        .build())
                .build();

        updateRequest = UpdateUserRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .isAdmin(true)
                .profile(UserProfile.builder()
                        .bio("Senior developer")
                        .phone("+0987654321")
                        .avatarUrl("https://example.com/new-avatar.jpg")
                        .config(UserProfile.UserConfig.builder()
                                .theme("light")
                                .build())
                        .build())
                .build();
    }

    // ============================================================
    // toResponse Tests
    // ============================================================

    @Test
    @DisplayName("Should convert User entity to UserResponse DTO")
    void toResponse_ConvertsUserToResponse_WhenUserIsValid() {
        // When
        UserResponse result = userMapper.toResponse(testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(result.getIsAdmin()).isFalse();
        assertThat(result.getCreatedAt()).isEqualTo(createdAt);
        
        assertThat(result.getProfile()).isNotNull();
        assertThat(result.getProfile().getBio()).isEqualTo("Software developer");
        assertThat(result.getProfile().getPhone()).isEqualTo("+1234567890");
        assertThat(result.getProfile().getAvatarUrl()).isEqualTo("https://example.com/avatar.jpg");
        assertThat(result.getProfile().getConfig().getTheme()).isEqualTo("dark");
    }

    @Test
    @DisplayName("Should return null when converting null User to UserResponse")
    void toResponse_ReturnsNull_WhenUserIsNull() {
        // When
        UserResponse result = userMapper.toResponse(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should handle User with null profile")
    void toResponse_HandlesNullProfile() {
        // Given
        testUser.setProfile(null);

        // When
        UserResponse result = userMapper.toResponse(testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getProfile()).isNull();
    }

    @Test
    @DisplayName("Should handle User with null profile config")
    void toResponse_HandlesNullProfileConfig() {
        // Given
        testUser.getProfile().setConfig(null);

        // When
        UserResponse result = userMapper.toResponse(testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProfile()).isNotNull();
        assertThat(result.getProfile().getConfig()).isNull();
    }

    // ============================================================
    // toEntity Tests
    // ============================================================

    @Test
    @DisplayName("Should convert CreateUserRequest to User entity")
    void toEntity_ConvertsCreateRequestToUser_WhenRequestIsValid() {
        // When
        User result = userMapper.toEntity(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(result.getIsAdmin()).isFalse();
        assertThat(result.getId()).isNull(); // ID should be generated by @PrePersist
        assertThat(result.getCreatedAt()).isNull(); // CreatedAt should be set by @PrePersist
        
        assertThat(result.getProfile()).isNotNull();
        assertThat(result.getProfile().getBio()).isEqualTo("Software developer");
        assertThat(result.getProfile().getPhone()).isEqualTo("+1234567890");
        assertThat(result.getProfile().getAvatarUrl()).isEqualTo("https://example.com/avatar.jpg");
        assertThat(result.getProfile().getConfig().getTheme()).isEqualTo("dark");
    }

    @Test
    @DisplayName("Should return null when converting null CreateUserRequest to User")
    void toEntity_ReturnsNull_WhenRequestIsNull() {
        // When
        User result = userMapper.toEntity(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should handle CreateUserRequest with null profile")
    void toEntity_HandlesNullProfile() {
        // Given
        createRequest.setProfile(null);

        // When
        User result = userMapper.toEntity(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getProfile()).isNull();
    }

    @Test
    @DisplayName("Should handle CreateUserRequest with default isAdmin")
    void toEntity_HandlesDefaultIsAdmin() {
        // Given
        createRequest.setIsAdmin(null);

        // When
        User result = userMapper.toEntity(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsAdmin()).isNull();
    }

    // ============================================================
    // updateEntityFromRequest Tests
    // ============================================================

    @Test
    @DisplayName("Should update User entity from UpdateUserRequest")
    void updateEntityFromRequest_UpdatesUser_WhenRequestIsValid() {
        // When
        userMapper.updateEntityFromRequest(testUser, updateRequest);

        // Then
        assertThat(testUser.getFirstName()).isEqualTo("Jane");
        assertThat(testUser.getLastName()).isEqualTo("Smith");
        assertThat(testUser.getEmail()).isEqualTo("jane.smith@example.com");
        assertThat(testUser.getIsAdmin()).isTrue();
        
        assertThat(testUser.getProfile()).isNotNull();
        assertThat(testUser.getProfile().getBio()).isEqualTo("Senior developer");
        assertThat(testUser.getProfile().getPhone()).isEqualTo("+0987654321");
        assertThat(testUser.getProfile().getAvatarUrl()).isEqualTo("https://example.com/new-avatar.jpg");
        assertThat(testUser.getProfile().getConfig().getTheme()).isEqualTo("light");
    }

    @Test
    @DisplayName("Should not update User when UpdateUserRequest is null")
    void updateEntityFromRequest_DoesNothing_WhenRequestIsNull() {
        // Given
        String originalFirstName = testUser.getFirstName();
        String originalLastName = testUser.getLastName();
        String originalEmail = testUser.getEmail();

        // When
        userMapper.updateEntityFromRequest(testUser, null);

        // Then
        assertThat(testUser.getFirstName()).isEqualTo(originalFirstName);
        assertThat(testUser.getLastName()).isEqualTo(originalLastName);
        assertThat(testUser.getEmail()).isEqualTo(originalEmail);
    }

    @Test
    @DisplayName("Should only update non-null fields")
    void updateEntityFromRequest_UpdatesOnlyNonNullFields() {
        // Given - Partial update request
        UpdateUserRequest partialUpdate = UpdateUserRequest.builder()
                .firstName("UpdatedFirstName")
                .email("updated@example.com")
                // lastName, isAdmin, profile are null
                .build();

        // When
        userMapper.updateEntityFromRequest(testUser, partialUpdate);

        // Then
        assertThat(testUser.getFirstName()).isEqualTo("UpdatedFirstName");
        assertThat(testUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(testUser.getLastName()).isEqualTo("Doe"); // Unchanged
        assertThat(testUser.getIsAdmin()).isFalse(); // Unchanged
        assertThat(testUser.getProfile().getBio()).isEqualTo("Software developer"); // Unchanged
    }

    @Test
    @DisplayName("Should handle UpdateUserRequest with null profile")
    void updateEntityFromRequest_UpdatesProfileToNull_WhenProfileIsNull() {
        // Given
        assertThat(testUser.getProfile()).isNotNull();

        UpdateUserRequest requestWithNullProfile = UpdateUserRequest.builder()
                .firstName("Updated")
                .profile(null)
                .build();

        // When
        userMapper.updateEntityFromRequest(testUser, requestWithNullProfile);

        // Then
        assertThat(testUser.getFirstName()).isEqualTo("Updated");
        assertThat(testUser.getProfile()).isNotNull();
    }

    @Test
    @DisplayName("Should set profile config to null when request config is null")
    void updateEntityFromRequest_SetsProfileConfigToNull_WhenConfigIsNull() {
        // Given
        assertThat(testUser.getProfile().getConfig()).isNotNull();

        UpdateUserRequest requestWithNullConfig = UpdateUserRequest.builder()
                .firstName("Updated")
                .profile(UserProfile.builder()
                        .bio("Updated bio")
                        .config(null)
                        .build())
                .build();

        // When
        userMapper.updateEntityFromRequest(testUser, requestWithNullConfig);

        // Then
        assertThat(testUser.getFirstName()).isEqualTo("Updated");
        assertThat(testUser.getProfile().getBio()).isEqualTo("Updated bio");
        assertThat(testUser.getProfile().getConfig()).isNull();
    }

    // ============================================================
    // Edge Cases Tests
    // ============================================================

    @Test
    @DisplayName("Should handle empty strings in update")
    void updateEntityFromRequest_HandlesEmptyStrings() {
        // Given
        UpdateUserRequest requestWithEmptyStrings = UpdateUserRequest.builder()
                .firstName("")
                .lastName("")
                .email("")
                .build();

        // When
        userMapper.updateEntityFromRequest(testUser, requestWithEmptyStrings);

        // Then
        assertThat(testUser.getFirstName()).isEqualTo("");
        assertThat(testUser.getLastName()).isEqualTo("");
        assertThat(testUser.getEmail()).isEqualTo("");
    }

    @Test
    @DisplayName("Should handle User with all null fields in toResponse")
    void toResponse_HandlesAllNullFields() {
        // Given
        User userWithNulls = User.builder()
                .id(null)
                .firstName(null)
                .lastName(null)
                .email(null)
                .isAdmin(null)
                .profile(null)
                .createdAt(null)
                .build();

        // When
        UserResponse result = userMapper.toResponse(userWithNulls);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull();
        assertThat(result.getFirstName()).isNull();
        assertThat(result.getLastName()).isNull();
        assertThat(result.getEmail()).isNull();
        assertThat(result.getIsAdmin()).isNull();
        assertThat(result.getProfile()).isNull();
        assertThat(result.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("Should handle CreateUserRequest with all null fields")
    void toEntity_HandlesAllNullFields() {
        // Given
        CreateUserRequest requestWithNulls = CreateUserRequest.builder()
                .firstName(null)
                .lastName(null)
                .email(null)
                .password(null)
                .isAdmin(null)
                .profile(null)
                .build();

        // When
        User result = userMapper.toEntity(requestWithNulls);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isNull();
        assertThat(result.getLastName()).isNull();
        assertThat(result.getEmail()).isNull();
        assertThat(result.getIsAdmin()).isNull();
        assertThat(result.getProfile()).isNull();
    }

    @Test
    @DisplayName("Should handle complex nested profile structures")
    void toResponse_HandlesComplexProfileStructures() {
        // Given
        UserProfile complexProfile = UserProfile.builder()
                .bio("Complex bio with multiple words")
                .phone("+1-555-123-4567")
                .avatarUrl("https://cdn.example.com/avatars/user123.jpg")
                .config(UserProfile.UserConfig.builder()
                        .theme("auto")
                        .build())
                .build();

        testUser.setProfile(complexProfile);

        // When
        UserResponse result = userMapper.toResponse(testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProfile()).isNotNull();
        assertThat(result.getProfile().getBio()).isEqualTo(complexProfile.getBio());
        assertThat(result.getProfile().getPhone()).isEqualTo(complexProfile.getPhone());
        assertThat(result.getProfile().getAvatarUrl()).isEqualTo(complexProfile.getAvatarUrl());
        assertThat(result.getProfile().getConfig().getTheme()).isEqualTo(complexProfile.getConfig().getTheme());
    }

    @Test
    @DisplayName("Should preserve ID and createdAt during update")
    void updateEntityFromRequest_PreservesIdAndCreatedAt() {
        // Given
        UUID originalId = testUser.getId();
        OffsetDateTime originalCreatedAt = testUser.getCreatedAt();

        // When
        userMapper.updateEntityFromRequest(testUser, updateRequest);

        // Then
        assertThat(testUser.getId()).isEqualTo(originalId);
        assertThat(testUser.getCreatedAt()).isEqualTo(originalCreatedAt);
    }

    @Test
    @DisplayName("Should handle boolean field updates correctly")
    void updateEntityFromRequest_HandlesBooleanUpdates() {
        // Given
        assertThat(testUser.getIsAdmin()).isFalse();

        // When - Update to true
        UpdateUserRequest makeAdminRequest = UpdateUserRequest.builder()
                .isAdmin(true)
                .build();

        userMapper.updateEntityFromRequest(testUser, makeAdminRequest);

        // Then
        assertThat(testUser.getIsAdmin()).isTrue();

        // When - Update to false
        UpdateUserRequest removeAdminRequest = UpdateUserRequest.builder()
                .isAdmin(false)
                .build();

        userMapper.updateEntityFromRequest(testUser, removeAdminRequest);

        // Then
        assertThat(testUser.getIsAdmin()).isFalse();
    }
}
