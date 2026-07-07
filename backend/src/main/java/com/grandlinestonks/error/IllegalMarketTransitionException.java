package com.grandlinestonks.error;

public class IllegalMarketTransitionException extends RuntimeException {

    public IllegalMarketTransitionException(String detail) {
        super("illegal market transition: " + detail);
    }
}
