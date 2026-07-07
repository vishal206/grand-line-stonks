package com.grandlinestonks.error;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(String detail) {
        super("account not found: " + detail);
    }
}
