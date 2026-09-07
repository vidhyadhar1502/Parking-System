package com.smartparking.service;

/**
 * Base unchecked exception for business and domain service operations.
 * Decouples presentation and controller layers from persistence/database exceptions.
 */
public class ServiceException extends RuntimeException {

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceException(Throwable cause) {
        super(cause);
    }
}
