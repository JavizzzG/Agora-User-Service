package com.app.userservice.controller;

import com.app.userservice.dto.CreateUserRequest;
import com.app.userservice.dto.UpdateUserRequest;
import com.app.userservice.dto.UserResponse;
import com.app.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@DisplayName("UserController Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserResponse testUserResponse;
    private CreateUserRequest createRequest;
    private UpdateUserRequest updateRequest;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        testUserResponse = UserResponse.builder()
                .id(userId)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .isAdmin(false)
                .build();

        createRequest = CreateUserRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("password123")
                .isAdmin(false)
                .build();

        updateRequest = UpdateUserRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .isAdmin(true)
                .build();
    }

    @Test
    @DisplayName("Should create user and return 201 status")
    void createUser_Returns201_WhenValidRequest() throws Exception {
        // Given
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(testUserResponse);

        // When & Then
        mockMvc.perform(post("/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.isAdmin").value(false));
    }

    @Test
    @DisplayName("Should return 400 when creating user with invalid data")
    void createUser_Returns400_WhenInvalidRequest() throws Exception {
        // Given - Invalid request (missing required fields)
        CreateUserRequest invalidRequest = new CreateUserRequest();

        // When & Then
        mockMvc.perform(post("/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get user by ID and return 200 status")
    void getUserById_Returns200_WhenUserExists() throws Exception {
        // Given
        when(userService.getUserById(userId)).thenReturn(testUserResponse);

        // When & Then
        mockMvc.perform(get("/users/get-user")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    @DisplayName("Should get user by email and return 200 status")
    void getUserByEmail_Returns200_WhenUserExists() throws Exception {
        // Given
        String email = "john.doe@example.com";
        when(userService.getUserByEmail(email)).thenReturn(testUserResponse);

        // When & Then
        mockMvc.perform(get("/users/email/{email}", email))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    @DisplayName("Should update user and return 200 status")
    void updateUser_Returns200_WhenValidRequest() throws Exception {
        // Given
        UserResponse updatedResponse = UserResponse.builder()
                .id(userId)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .isAdmin(true)
                .build();

        when(userService.updateUser(eq(userId), any(UpdateUserRequest.class))).thenReturn(updatedResponse);

        // When & Then
        mockMvc.perform(put("/users/update-user")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.email").value("jane.smith@example.com"))
                .andExpect(jsonPath("$.isAdmin").value(true));
    }

    @Test
    @DisplayName("Should return 400 when updating user with invalid data")
    void updateUser_Returns400_WhenInvalidRequest() throws Exception {
        // Given - Invalid request
        UpdateUserRequest invalidRequest = new UpdateUserRequest();
        invalidRequest.setEmail("invalid-email"); // Invalid email format

        // When & Then
        mockMvc.perform(put("/users/update-user")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should delete user and return 204 status")
    void deleteUser_Returns204_WhenUserExists() throws Exception {
        // When & Then
        mockMvc.perform(delete("/users/delete-user")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("Should check if user exists by email and return true")
    void existsByEmail_ReturnsTrue_WhenUserExists() throws Exception {
        // Given
        String email = "john.doe@example.com";
        when(userService.existsByEmail(email)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/users/exists/{email}", email))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    @DisplayName("Should check if user exists by email and return false")
    void existsByEmail_ReturnsFalse_WhenUserDoesNotExist() throws Exception {
        // Given
        String email = "nonexistent@example.com";
        when(userService.existsByEmail(email)).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/users/exists/{email}", email))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(false));
    }

    @Test
    @DisplayName("Should handle invalid UUID format in user ID")
    void getUserById_Returns400_WhenInvalidUUID() throws Exception {
        // When & Then
        mockMvc.perform(get("/users/get-user")
                        .header("X-User-Id", "invalid-uuid"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should handle empty request body")
    void createUser_Returns400_WhenEmptyRequestBody() throws Exception {
        // When & Then
        mockMvc.perform(post("/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle malformed JSON")
    void createUser_Returns400_WhenMalformedJSON() throws Exception {
        // When & Then
        mockMvc.perform(post("/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should validate email format in create request")
    void createUser_Returns400_WhenInvalidEmail() throws Exception {
        // Given
        CreateUserRequest requestWithInvalidEmail = CreateUserRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("invalid-email")
                .password("password123")
                .build();

        // When & Then
        mockMvc.perform(post("/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithInvalidEmail)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should validate name length in create request")
    void createUser_Returns400_WhenNameTooLong() throws Exception {
        // Given
        String longName = "a".repeat(31); // Exceeds max length of 30
        CreateUserRequest requestWithLongName = CreateUserRequest.builder()
                .firstName(longName)
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("password123")
                .build();

        // When & Then
        mockMvc.perform(post("/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithLongName)))
                .andExpect(status().isBadRequest());
    }
}
