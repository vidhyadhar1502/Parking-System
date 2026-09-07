package com.smartparking.service;

/**
 * Exception thrown when authentication or credential verification fails.
 * Designed to provide generic, secure error messages that prevent account enumeration.
 */
public class AuthenticationException extends ServiceException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
