package com.grandlinestonks.error;

public class UsernameTakenException extends RuntimeException {

    public UsernameTakenException(String username) {
        super("username already taken: " + username);
    }
}
