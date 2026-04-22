package com.app.userservice.repository;

import com.app.userservice.model.User;
import com.app.userservice.model.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        
        testUser = User.builder()
                .id(userId)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .isAdmin(false)
                .profile(null)
                .build();

        entityManager.persistAndFlush(testUser);
        entityManager.clear();
    }

    @Test
    @DisplayName("Should find user by email successfully")
    void findByEmail_ReturnsUser_WhenEmailExists() {
        // When
        Optional<User> result = userRepository.findByEmail("john.doe@example.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("john.doe@example.com");
        assertThat(result.get().getFirstName()).isEqualTo("John");
        assertThat(result.get().getLastName()).isEqualTo("Doe");
        assertThat(result.get().getId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should return empty when email doesn't exist")
    void findByEmail_ReturnsEmpty_WhenEmailDoesNotExist() {
        // When
        Optional<User> result = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return true when email exists")
    void existsByEmail_ReturnsTrue_WhenEmailExists() {
        // When
        boolean result = userRepository.existsByEmail("john.doe@example.com");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false when email doesn't exist")
    void existsByEmail_ReturnsFalse_WhenEmailDoesNotExist() {
        // When
        boolean result = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should find user by ID successfully")
    void findById_ReturnsUser_WhenUserExists() {
        // When
        Optional<User> result = userRepository.findById(userId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(userId);
        assertThat(result.get().getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    @DisplayName("Should return empty when user ID doesn't exist")
    void findById_ReturnsEmpty_WhenUserDoesNotExist() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        Optional<User> result = userRepository.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should save user successfully")
    void save_ReturnsSavedUser_WhenValidUser() {
        // Given
        User newUser = User.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .isAdmin(true)
                .profile(null)
                .build();

        // When
        User savedUser = userRepository.save(newUser);

        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getFirstName()).isEqualTo("Jane");
        assertThat(savedUser.getLastName()).isEqualTo("Smith");
        assertThat(savedUser.getEmail()).isEqualTo("jane.smith@example.com");
        assertThat(savedUser.getIsAdmin()).isTrue();
        assertThat(savedUser.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should update user successfully")
    void save_UpdatesUser_WhenUserExists() {
        // Given
        testUser.setFirstName("Updated Name");
        testUser.setEmail("updated@example.com");

        // When
        User updatedUser = userRepository.save(testUser);

        // Then
        assertThat(updatedUser.getFirstName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(updatedUser.getId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should delete user successfully")
    void deleteById_DeletesUser_WhenUserExists() {
        // Given
        assertThat(userRepository.existsById(userId)).isTrue();

        // When
        userRepository.deleteById(userId);

        // Then
        assertThat(userRepository.existsById(userId)).isFalse();
    }

    @Test
    @DisplayName("Should find all users successfully")
    void findAll_ReturnsAllUsers_WhenUsersExist() {
        // Given
        User secondUser = User.builder()
                .firstName("Alice")
                .lastName("Johnson")
                .email("alice.johnson@example.com")
                .isAdmin(false)
                .build();
        entityManager.persistAndFlush(secondUser);
        entityManager.clear();

        // When
        Iterable<User> users = userRepository.findAll();

        // Then
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getEmail)
                .containsExactlyInAnyOrder("john.doe@example.com", "alice.johnson@example.com");
    }

    @Test
    @DisplayName("Should handle case-insensitive email search")
    void findByEmail_HandlesCaseInsensitiveEmail() {
        // Given - Insert user with lowercase email
        User userWithLowercaseEmail = User.builder()
                .firstName("Bob")
                .lastName("Wilson")
                .email("bob.wilson@example.com")
                .isAdmin(false)
                .build();
        entityManager.persistAndFlush(userWithLowercaseEmail);
        entityManager.clear();

        // When - Search with uppercase email
        Optional<User> result = userRepository.findByEmail("BOB.WILSON@EXAMPLE.COM");

        // Then - Should not find due to case sensitivity (depends on database collation)
        // This test documents the current behavior - adjust expectation based on your DB setup
        assertThat(result).isEmpty(); // PostgreSQL is case-sensitive by default
    }

    @Test
    @DisplayName("Should handle email with leading/trailing spaces")
    void findByEmail_HandlesEmailWithSpaces() {
        // Given - Email with spaces should be stored as-is
        User userWithSpaces = User.builder()
                .firstName("Charlie")
                .lastName("Brown")
                .email(" charlie.brown@example.com ")
                .isAdmin(false)
                .build();
        entityManager.persistAndFlush(userWithSpaces);
        entityManager.clear();

        // When
        Optional<User> result = userRepository.findByEmail(" charlie.brown@example.com ");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(" charlie.brown@example.com ");
    }

    @Test
    @DisplayName("Should enforce unique email constraint")
    void save_ThrowsException_WhenEmailIsDuplicate() {
        // Given
        User duplicateUser = User.builder()
                .firstName("Duplicate")
                .lastName("User")
                .email("john.doe@example.com") // Same email as testUser
                .isAdmin(false)
                .build();

        // When & Then
        // Note: This test may behave differently depending on JPA configuration
        // Some databases throw exceptions on flush, others on commit
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> {
            entityManager.persistAndFlush(duplicateUser);
        });
    }

    @Test
    @DisplayName("Should handle null email in existsByEmail")
    void existsByEmail_ReturnsFalse_WhenEmailIsNull() {
        // When
        boolean result = userRepository.existsByEmail(null);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should handle empty string in existsByEmail")
    void existsByEmail_ReturnsFalse_WhenEmailIsEmpty() {
        // When
        boolean result = userRepository.existsByEmail("");

        // Then
        assertThat(result).isFalse();
    }
}
