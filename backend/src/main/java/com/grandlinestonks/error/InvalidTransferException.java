package com.grandlinestonks.error;

public class InvalidTransferException extends RuntimeException {

    public InvalidTransferException(String detail) {
        super("invalid transfer: " + detail);
    }
}
