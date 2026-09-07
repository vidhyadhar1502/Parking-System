package com.smartparking.service;

/**
 * Application-level registry providing singleton service instances across JavaFX controllers.
 * Decouples controllers from direct service instantiation and enables clean test mocking.
 */
public final class ServiceRegistry {

    private static AuthService authService;
    private static UserService userService;

    private ServiceRegistry() {
        // Prevent instantiation
    }

    /**
     * Retrieves or initializes the shared {@link AuthService} instance.
     */
    public static synchronized AuthService getAuthService() {
        if (authService == null) {
            authService = new AuthService();
        }
        return authService;
    }

    /**
     * Sets or overrides the {@link AuthService} instance (used in unit tests).
     */
    public static synchronized void setAuthService(AuthService service) {
        authService = service;
    }

    /**
     * Retrieves or initializes the shared {@link UserService} instance.
     */
    public static synchronized UserService getUserService() {
        if (userService == null) {
            userService = new UserService();
        }
        return userService;
    }

    /**
     * Sets or overrides the {@link UserService} instance (used in unit tests).
     */
    public static synchronized void setUserService(UserService service) {
        userService = service;
    }

    /**
     * Resets registered services to default null state.
     */
    public static synchronized void reset() {
        authService = null;
        userService = null;
    }
}
