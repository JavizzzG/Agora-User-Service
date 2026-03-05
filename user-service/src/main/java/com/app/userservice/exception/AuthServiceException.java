package com.app.userservice.exception;

//Extends RuntimeException because if the service have an error Springboot do a rollback
public class AuthServiceException extends RuntimeException{

    //Method that just show a message if the service have an error
    public AuthServiceException (String message){
        super(message);
    }

    //Method that show a message and the error when the service have an error
    public AuthServiceException (String message, Throwable cause){
        super (message, cause);
    }

}
