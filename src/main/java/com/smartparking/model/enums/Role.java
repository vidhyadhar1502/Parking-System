package com.smartparking.model.enums;

/**
 * Represents the role of an authenticated user within the system.
 */
public enum Role {
    CUSTOMER("Customer / Driver"),
    ADMIN("System Administrator");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
