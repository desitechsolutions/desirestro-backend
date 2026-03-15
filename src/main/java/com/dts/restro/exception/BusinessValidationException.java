package com.dts.restro.exception;

/**
 * Thrown when a business rule is violated (e.g. closing a table that has unpaid bills).
 */
public class BusinessValidationException extends RuntimeException {

    public BusinessValidationException(String message) {
        super(message);
    }

    public BusinessValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
