package com.app.userservice.service;

import com.app.userservice.dto.GoogleCreateUserRequest;
import com.app.userservice.dto.GoogleCreateUserResponse;
import com.app.userservice.model.User;
import com.app.userservice.repository.UserRepository;
import com.app.userservice.util.DataSanitizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GoogleUserService Tests")
class GoogleUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DataSanitizer dataSanitizer;

    @InjectMocks
    private GoogleUserService googleUserService;

    private GoogleCreateUserRequest request;

    @BeforeEach
    void setUp() {
        request = GoogleCreateUserRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@gmail.com")
                .avatarUrl("https://example.com/avatar.jpg")
                .build();

        lenient().when(dataSanitizer.sanitizeName(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(dataSanitizer.sanitizeEmail(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(dataSanitizer.sanitizeUrl(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Should create Google user and return UUID")
    void createGoogleUser_ReturnsUuid_WhenEmailIsUnique() {
        GoogleCreateUserResponse userId = new GoogleCreateUserResponse(UUID.randomUUID());
        User savedUser = User.builder()
                .id(userId.getId())
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@gmail.com")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        GoogleCreateUserResponse result = googleUserService.createGoogleUser(request);

        assertThat(result).isEqualTo(userId);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User createdUser = userCaptor.getValue();

        assertThat(createdUser.getFirstName()).isEqualTo("John");
        assertThat(createdUser.getLastName()).isEqualTo("Doe");
        assertThat(createdUser.getEmail()).isEqualTo("john.doe@gmail.com");
        assertThat(createdUser.getProfile()).isNotNull();
        assertThat(createdUser.getProfile().getAvatarUrl()).isEqualTo("https://example.com/avatar.jpg");
        assertThat(createdUser.getIsAdmin()).isFalse();

        verify(dataSanitizer).sanitizeName("John");
        verify(dataSanitizer).sanitizeName("Doe");
        verify(dataSanitizer).sanitizeEmail("john.doe@gmail.com");
        verify(dataSanitizer).sanitizeUrl("https://example.com/avatar.jpg");
    }

    @Test
    @DisplayName("Should return existing UUID when email already exists")
    void createGoogleUser_ReturnsExistingUuid_WhenEmailExists() {
        UUID existingId = UUID.randomUUID();
        User existingUser = User.builder()
                .id(existingId)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@gmail.com")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);
        when(userRepository.findByEmail(request.getEmail())).thenReturn(java.util.Optional.of(existingUser));

        GoogleCreateUserResponse result = googleUserService.createGoogleUser(request);

        assertThat(result.getId()).isEqualTo(existingId);
        verify(userRepository).findByEmail(request.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }
}
