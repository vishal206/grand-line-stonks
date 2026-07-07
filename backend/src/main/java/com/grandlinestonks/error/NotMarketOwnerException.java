package com.grandlinestonks.error;

public class NotMarketOwnerException extends RuntimeException {

    public NotMarketOwnerException(Long marketId) {
        super("only the market creator may manage market " + marketId);
    }
}
