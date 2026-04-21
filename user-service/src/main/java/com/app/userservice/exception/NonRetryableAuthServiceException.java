package com.app.userservice.exception;

public class NonRetryableAuthServiceException extends AuthServiceException {

    public NonRetryableAuthServiceException(String message) {
        super(message);
    }

    public NonRetryableAuthServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
