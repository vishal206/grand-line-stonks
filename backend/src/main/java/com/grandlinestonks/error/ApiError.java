package com.grandlinestonks.error;

import java.time.Instant;
import java.util.Map;

public record ApiError(String code, String message, Map<String, String> details, Instant timestamp) {

    public static ApiError of(String code, String message) {
        return new ApiError(code, message, Map.of(), Instant.now());
    }

    public static ApiError of(String code, String message, Map<String, String> details) {
        return new ApiError(code, message, details, Instant.now());
    }
}
