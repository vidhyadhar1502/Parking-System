package com.smartparking.util;

/**
 * Exception thrown when scene or view navigation fails due to missing resources,
 * FXML initialization faults, or unauthorized access attempts.
 */
public class NavigationException extends RuntimeException {

    public NavigationException(String message) {
        super(message);
    }

    public NavigationException(String message, Throwable cause) {
        super(message, cause);
    }
}
