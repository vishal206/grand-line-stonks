package com.grandlinestonks.error;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

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

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleUnreadableRequest(Exception e) {
        return ApiError.of("MALFORMED_REQUEST", "request could not be parsed");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNoResource(NoResourceFoundException e) {
        return ApiError.of("NOT_FOUND", "no such resource");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleUnexpected(Exception e) {
        log.error("unhandled exception", e);
        return ApiError.of("INTERNAL_ERROR", "an unexpected error occurred");
    }
}
