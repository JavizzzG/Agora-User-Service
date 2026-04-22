package com.app.userservice.controller;

import com.app.userservice.dto.GoogleCreateUserRequest;
import com.app.userservice.service.GoogleUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GoogleUserController.class)
@DisplayName("GoogleUserController Tests")
class GoogleUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GoogleUserService googleUserService;

    @Test
    @DisplayName("Should register Google user and return created UUID")
    void registerGoogleUser_Returns201_WithUuid() throws Exception {
        UUID createdId = UUID.randomUUID();
        GoogleCreateUserRequest request = GoogleCreateUserRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@gmail.com")
                .avatarUrl("https://example.com/avatar.jpg")
                .build();

        when(googleUserService.createGoogleUser(any(GoogleCreateUserRequest.class))).thenReturn(createdId);

        mockMvc.perform(post("/internal/users/register/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("\"" + createdId + "\""));
    }

    @Test
    @DisplayName("Should return 400 when Google register payload is invalid")
    void registerGoogleUser_Returns400_WhenInvalidRequest() throws Exception {
        GoogleCreateUserRequest invalidRequest = new GoogleCreateUserRequest();

        mockMvc.perform(post("/internal/users/register/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
