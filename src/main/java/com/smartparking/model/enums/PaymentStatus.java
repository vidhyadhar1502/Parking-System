package com.smartparking.model.enums;

/**
 * Settlement state of parking tariff transactions.
 */
public enum PaymentStatus {
    PENDING("Pending"),
    PAID("Paid"),
    WAIVED("Waived");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
