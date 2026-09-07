package com.smartparking.model.enums;

/**
 * Real-time operational availability status of an individual parking bay.
 */
public enum SlotStatus {
    AVAILABLE("Available"),
    RESERVED("Reserved"),
    OCCUPIED("Occupied"),
    MAINTENANCE("Under Maintenance");

    private final String displayName;

    SlotStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
