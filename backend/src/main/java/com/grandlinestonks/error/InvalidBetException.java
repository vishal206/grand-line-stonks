package com.grandlinestonks.error;

public class InvalidBetException extends RuntimeException {

    public InvalidBetException(String detail) {
        super("invalid bet: " + detail);
    }
}
