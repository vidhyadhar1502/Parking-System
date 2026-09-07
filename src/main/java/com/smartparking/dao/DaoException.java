package com.smartparking.dao;

/**
 * Standard unchecked exception for persistence and data access operations.
 * Wraps checked SQLExceptions to avoid leaking database internals into presentation and business layers.
 */
public class DaoException extends RuntimeException {

    public DaoException(String message) {
        super(message);
    }

    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }

    public DaoException(Throwable cause) {
        super(cause);
    }
}
