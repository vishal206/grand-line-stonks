package com.grandlinestonks.error;

public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(Long accountId) {
        super("insufficient balance on account " + accountId);
    }
}
