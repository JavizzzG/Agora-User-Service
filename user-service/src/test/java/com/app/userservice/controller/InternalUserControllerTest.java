package com.app.userservice.controller;

import com.app.userservice.dto.InternalAiProfileResponse;
import com.app.userservice.dto.InternalUserSummaryResponse;
import com.app.userservice.dto.InternalUsersBatchRequest;
import com.app.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalUserController.class)
@DisplayName("InternalUserController Tests")
class InternalUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("Should return ai profile by user id")
    void getAiProfile_Returns200() throws Exception {
        UUID userId = UUID.randomUUID();
        InternalAiProfileResponse response = InternalAiProfileResponse.builder()
                .userId(userId)
                .agenticMode(true)
                .retroStyle("detailed")
                .exigencyLevel("moderated")
                .weeklyReport(false)
                .sendEmailNotification(true)
                .build();

        when(userService.getInternalAiProfile(userId)).thenReturn(response);

        mockMvc.perform(get("/internal/users/{userId}/ai-profile", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.agenticMode").value(true));
    }

    @Test
    @DisplayName("Should return user summaries for batch request")
    void getBatchSummary_Returns200() throws Exception {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();

        InternalUsersBatchRequest request = new InternalUsersBatchRequest(List.of(firstId, secondId));
        List<InternalUserSummaryResponse> response = List.of(
                InternalUserSummaryResponse.builder()
                        .id(firstId)
                        .firstName("John")
                        .lastName("Doe")
                        .fullName("John Doe")
                        .avatarUrl("https://cdn/john.png")
                        .build(),
                InternalUserSummaryResponse.builder()
                        .id(secondId)
                        .firstName("Jane")
                        .lastName("Smith")
                        .fullName("Jane Smith")
                        .avatarUrl("https://cdn/jane.png")
                        .build()
        );

        when(userService.getInternalUserSummaries(request.getUserIds())).thenReturn(response);

        mockMvc.perform(post("/internal/users/batch-summary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(firstId.toString()))
                .andExpect(jsonPath("$[0].fullName").value("John Doe"))
                .andExpect(jsonPath("$[1].id").value(secondId.toString()));
    }

    @Test
    @DisplayName("Should return 400 when batch request is empty")
    void getBatchSummary_Returns400_WhenIdsAreEmpty() throws Exception {
        InternalUsersBatchRequest request = new InternalUsersBatchRequest(List.of());

        mockMvc.perform(post("/internal/users/batch-summary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
