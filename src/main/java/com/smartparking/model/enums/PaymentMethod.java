package com.smartparking.model.enums;

/**
 * Payment channel utilized to settle parking charges.
 */
public enum PaymentMethod {
    CARD("Debit / Credit Card"),
    CASH("Cash Settlement"),
    UPI_SIMULATED("UPI / QR Payment");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
