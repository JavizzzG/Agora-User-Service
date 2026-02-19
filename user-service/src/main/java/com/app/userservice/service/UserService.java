package com.app.userservice.service;

import com.app.userservice.dto.CreateUserRequest;
import com.app.userservice.dto.UpdateUserRequest;
import com.app.userservice.dto.UserResponse;
import com.app.userservice.exception.DuplicateEmailException;
import com.app.userservice.exception.UserNotFoundException;
import com.app.userservice.mapper.UserMapper;
import com.app.userservice.model.User;
import com.app.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for user-related business logic.
 * Handles all operations related to user management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    
    /**
     * Create a new user
     * @param request the user creation request
     * @return the created user response
     * @throws DuplicateEmailException if email already exists
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating new user with email: {}", request.getEmail());
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Attempted to create user with duplicate email: {}", request.getEmail());
            throw new DuplicateEmailException(request.getEmail());
        }
        
        // Convert DTO to entity and save
        User user = userMapper.toEntity(request);
        User savedUser = userRepository.save(user);
        
        log.info("Successfully created user with id: {}", savedUser.getId());
        return userMapper.toResponse(savedUser);
    }
    
    /**
     * Get a user by ID
     * @param userId the user ID
     * @return the user response
     * @throws UserNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        log.info("Fetching user with id: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        return userMapper.toResponse(user);
    }
    
    /**
     * Get a user by email
     * @param email the user email
     * @return the user response
     * @throws UserNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        log.info("Fetching user with email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        
        return userMapper.toResponse(user);
    }
    
    /**
     * Get all users
     * @return list of all users
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.info("Fetching all users");
        
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Update an existing user
     * @param userId the user ID to update
     * @param request the update request
     * @return the updated user response
     * @throws UserNotFoundException if user not found
     * @throws DuplicateEmailException if new email already exists
     */
    @Transactional
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        log.info("Updating user with id: {}", userId);
        
        // Find existing user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        // Check if email is being changed and if new email already exists
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                log.warn("Attempted to update user with duplicate email: {}", request.getEmail());
                throw new DuplicateEmailException(request.getEmail());
            }
        }
        
        // Update user fields
        userMapper.updateEntityFromRequest(user, request);
        User updatedUser = userRepository.save(user);
        
        log.info("Successfully updated user with id: {}", userId);
        return userMapper.toResponse(updatedUser);
    }
    
    /**
     * Delete a user by ID
     * @param userId the user ID to delete
     * @throws UserNotFoundException if user not found
     */
    @Transactional
    public void deleteUser(UUID userId) {
        log.info("Deleting user with id: {}", userId);
        
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        
        userRepository.deleteById(userId);
        log.info("Successfully deleted user with id: {}", userId);
    }
    
    /**
     * Check if a user exists by email
     * @param email the email to check
     * @return true if user exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
