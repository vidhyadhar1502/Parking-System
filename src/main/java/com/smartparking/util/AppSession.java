package com.smartparking.util;

import com.smartparking.model.User;
import com.smartparking.model.enums.Role;

/**
 * Thread-safe global session state tracker for the current authenticated user.
 */
public class AppSession {

    private static volatile AppSession instance;
    private static final Object LOCK = new Object();

    private User currentUser;

    private AppSession() {
        // Private constructor
    }

    public static AppSession getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new AppSession();
                }
            }
        }
        return instance;
    }

    /**
     * Authenticates and registers a user session.
     *
     * @param user the authenticated User entity
     */
    public synchronized void login(User user) {
        this.currentUser = user;
    }

    /**
     * Clears the current active session state upon sign-out.
     */
    public synchronized void logout() {
        this.currentUser = null;
    }

    /**
     * Checks if a user is currently signed in.
     */
    public synchronized boolean isLoggedIn() {
        return this.currentUser != null;
    }

    /**
     * Returns the currently authenticated user.
     */
    public synchronized User getCurrentUser() {
        return this.currentUser;
    }

    /**
     * Checks if the active user has administrative privileges.
     */
    public synchronized boolean isAdmin() {
        return this.currentUser != null && this.currentUser.getRole() == Role.ADMIN;
    }

    /**
     * Checks if the active user is a standard customer/driver.
     */
    public synchronized boolean isCustomer() {
        return this.currentUser != null && this.currentUser.getRole() == Role.CUSTOMER;
    }
}
