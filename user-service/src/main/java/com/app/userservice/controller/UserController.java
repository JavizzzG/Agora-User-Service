package com.app.userservice.controller;

import com.app.userservice.dto.AuthenticateCredentialsResponse;
import com.app.userservice.dto.CreateUserRequest;
import com.app.userservice.dto.UpdateUserRequest;
import com.app.userservice.dto.UserResponse;
import com.app.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for user management endpoints.
 * Provides CRUD operations for users.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    /**
     * Create a new user
     * POST /api/users
     * @param request the user creation request
     * @return the created user with 201 status
     */
    @PostMapping("/create")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }
    
    /**
     * Get a user by ID
     * GET /api/users/{id}
     * @param id the user ID
     * @return the user details
     */
    @GetMapping("/get-user")
    public ResponseEntity<UserResponse> getUserById(@RequestHeader("X-User-Id") UUID id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get a user by email
     * GET /api/users/email/{email}
     * @param email the user email
     * @return the user details
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        UserResponse response = userService.getUserByEmail(email);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update an existing user
     * PUT /api/users/{id}
     * @param id the user ID to update
     * @param request the update request
     * @return the updated user
     */
    @PutMapping("/update-user")
    public ResponseEntity<UserResponse> updateUser(
            @RequestHeader("X-User-Id") UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete a user
     * DELETE /api/users/{id}
     * @param id the user ID to delete
     * @return 204 No Content on success
     */
    @DeleteMapping("/delete-user")
    public ResponseEntity<Void> deleteUser(@RequestHeader("X-User-Id") UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Check if a user exists by email
     * GET /api/users/exists?email={email}
     * @param email the email to check
     * @return true if exists, false otherwise
     */
    @GetMapping("/exists/{email}")
    public ResponseEntity<Boolean> existsByEmail(@PathVariable String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }
}
