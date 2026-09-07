package com.smartparking.model.enums;

/**
 * Classification of parking bay geometry, capabilities, and vehicle suitability.
 */
public enum SlotType {
    STANDARD("Standard Vehicle"),
    COMPACT("Compact / Two-Wheeler"),
    EV_CHARGING("EV Charging Station"),
    HANDICAPPED("Accessible / Disabled");

    private final String displayName;

    SlotType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
