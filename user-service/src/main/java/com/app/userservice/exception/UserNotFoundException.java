package com.app.userservice.exception;

import java.util.UUID;

/**
 * Exception thrown when a requested user is not found in the database
 */
public class UserNotFoundException extends RuntimeException {
    
    public UserNotFoundException(String message) {
        super(message);
    }
    
    public UserNotFoundException(UUID userId) {
        super("User not found with id: " + userId);
    }
}
