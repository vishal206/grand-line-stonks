package com.grandlinestonks.error;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> details = new HashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(error -> details.put(error.getField(), error.getDefaultMessage()));
        return ApiError.of("VALIDATION_FAILED", "request validation failed", details);
    }

    @ExceptionHandler(UsernameTakenException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleUsernameTaken(UsernameTakenException e) {
        return ApiError.of("USERNAME_TAKEN", e.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handleInvalidCredentials(InvalidCredentialsException e) {
        return ApiError.of("INVALID_CREDENTIALS", e.getMessage());
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ApiError handleInsufficientBalance(InsufficientBalanceException e) {
        return ApiError.of("INSUFFICIENT_BALANCE", e.getMessage());
    }

    @ExceptionHandler(AccountNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleAccountNotFound(AccountNotFoundException e) {
        return ApiError.of("ACCOUNT_NOT_FOUND", e.getMessage());
    }

    @ExceptionHandler(InvalidTransferException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInvalidTransfer(InvalidTransferException e) {
        return ApiError.of("INVALID_TRANSFER", e.getMessage());
    }

    @ExceptionHandler(UnbalancedTransactionException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleUnbalancedTransaction(UnbalancedTransactionException e) {
        return ApiError.of("UNBALANCED_TRANSACTION", e.getMessage());
    }

    @ExceptionHandler(MarketNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleMarketNotFound(MarketNotFoundException e) {
        return ApiError.of("MARKET_NOT_FOUND", e.getMessage());
    }

    @ExceptionHandler(IllegalMarketTransitionException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleIllegalTransition(IllegalMarketTransitionException e) {
        return ApiError.of("ILLEGAL_MARKET_TRANSITION", e.getMessage());
    }

    @ExceptionHandler(InvalidBetException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ApiError handleInvalidBet(InvalidBetException e) {
        return ApiError.of("INVALID_BET", e.getMessage());
    }

    @ExceptionHandler(NotMarketOwnerException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleNotMarketOwner(NotMarketOwnerException e) {
        return ApiError.of("NOT_MARKET_OWNER", e.getMessage());
    }
}
