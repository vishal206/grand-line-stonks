package com.grandlinestonks.error;

public class UnbalancedTransactionException extends RuntimeException {

    public UnbalancedTransactionException(String detail) {
        super("ledger transaction rejected: " + detail);
    }
}
