package com.smartparking.model.enums;

/**
 * Lifecycle stage of a parking slot pre-booking or live parking session.
 */
public enum ReservationStatus {
    CONFIRMED("Confirmed"),
    CHECKED_IN("Checked In"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String displayName;

    ReservationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
