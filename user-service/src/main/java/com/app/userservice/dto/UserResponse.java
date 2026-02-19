package com.app.userservice.dto;

import com.app.userservice.model.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO for user responses.
 * Contains all user information to be sent to clients.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean isAdmin;
    private UserProfile profile;
    private OffsetDateTime createdAt;
}
