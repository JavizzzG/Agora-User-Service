package com.app.userservice.dto;

import com.app.userservice.model.UserProfile;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing user.
 * All fields are optional to allow partial updates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    
    @Size(max = 30, message = "First name must not exceed 30 characters")
    private String firstName;
    
    @Size(max = 30, message = "Last name must not exceed 30 characters")
    private String lastName;
    
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;
    
    private Boolean isAdmin;
    
    /**
     * User profile information to update
     */
    private UserProfile profile;
}
