package com.grandlinestonks.error;

public class MarketNotFoundException extends RuntimeException {

    public MarketNotFoundException(String detail) {
        super("market not found: " + detail);
    }
}
