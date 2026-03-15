package com.dts.restro.exception;

/**
 * Thrown when an ingredient's available stock falls below the quantity required
 * to fulfil a Kitchen Order Ticket.
 */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }
}
