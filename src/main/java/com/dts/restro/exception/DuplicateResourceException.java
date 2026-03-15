package com.dts.restro.exception;

/**
 * Thrown when a create/update operation would result in a duplicate unique resource
 * (e.g. duplicate username, duplicate table number, duplicate KOT number).
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
