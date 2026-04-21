package com.app.userservice.exception;

public class RetryableAuthServiceException extends AuthServiceException {

    public RetryableAuthServiceException(String message) {
        super(message);
    }

    public RetryableAuthServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
