package com.app.userservice.mapper;

import com.app.userservice.dto.CreateUserRequest;
import com.app.userservice.dto.UpdateUserRequest;
import com.app.userservice.dto.UserResponse;
import com.app.userservice.model.User;
import org.springframework.stereotype.Component;

/**
 * Mapper class to convert between User entity and DTOs.
 * Handles the transformation logic to keep controllers and services clean.
 */
@Component
public class UserMapper {
    
    /**
     * Convert User entity to UserResponse DTO
     * @param user the user entity
     * @return UserResponse DTO
     */
    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .isAdmin(user.getIsAdmin())
                .profile(user.getProfile())
                .createdAt(user.getCreatedAt())
                .build();
    }
    
    /**
     * Convert CreateUserRequest DTO to User entity
     * @param request the create user request
     * @return User entity
     */
    public User toEntity(CreateUserRequest request) {
        if (request == null) {
            return null;
        }
        
        return User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .isAdmin(request.getIsAdmin())
                .profile(request.getProfile())
                .build();
    }
    
    /**
     * Update existing user entity with data from UpdateUserRequest.
     * Only updates non-null fields from the request.
     * @param user the existing user entity
     * @param request the update request
     */
    public void updateEntityFromRequest(User user, UpdateUserRequest request) {
        if (request == null) {
            return;
        }
        
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        
        if (request.getIsAdmin() != null) {
            user.setIsAdmin(request.getIsAdmin());
        }
        
        if (request.getProfile() != null) {
            user.setProfile(request.getProfile());
        }
    }
}
