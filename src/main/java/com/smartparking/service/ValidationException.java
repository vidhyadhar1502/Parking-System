package com.smartparking.service;

/**
 * Exception thrown when business validation constraints (e.g. data formatting,
 * length requirements, uniqueness violations) are not met.
 */
public class ValidationException extends ServiceException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
