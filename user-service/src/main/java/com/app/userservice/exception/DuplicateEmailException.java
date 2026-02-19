package com.app.userservice.exception;

/**
 * Exception thrown when trying to create/update a user with an email that already exists
 */
public class DuplicateEmailException extends RuntimeException {
    
    public DuplicateEmailException(String email) {
        super("User with email '" + email + "' already exists");
    }
}
