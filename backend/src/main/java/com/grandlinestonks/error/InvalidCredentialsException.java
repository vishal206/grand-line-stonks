package com.grandlinestonks.error;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("invalid username or password");
    }
}
