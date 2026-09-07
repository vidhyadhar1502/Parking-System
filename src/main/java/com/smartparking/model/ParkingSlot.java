package com.smartparking.model;

import com.smartparking.model.enums.SlotStatus;
import com.smartparking.model.enums.SlotType;
import java.util.Objects;

/**
 * Entity representing an individual demarcated parking bay within a facility.
 */
public class ParkingSlot {
    private Integer slotId;
    private Integer locationId;
    private String slotNumber;
    private int floorLevel;
    private SlotType slotType;
    private SlotStatus status;

    public ParkingSlot() {
        this.floorLevel = 1;
        this.slotType = SlotType.STANDARD;
        this.status = SlotStatus.AVAILABLE;
    }

    public ParkingSlot(Integer slotId, Integer locationId, String slotNumber, 
                       int floorLevel, SlotType slotType, SlotStatus status) {
        this.slotId = slotId;
        this.locationId = locationId;
        this.slotNumber = slotNumber;
        this.floorLevel = floorLevel;
        this.slotType = slotType != null ? slotType : SlotType.STANDARD;
        this.status = status != null ? status : SlotStatus.AVAILABLE;
    }

    public Integer getSlotId() {
        return slotId;
    }

    public void setSlotId(Integer slotId) {
        this.slotId = slotId;
    }

    public Integer getLocationId() {
        return locationId;
    }

    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }

    public String getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(String slotNumber) {
        this.slotNumber = slotNumber;
    }

    public int getFloorLevel() {
        return floorLevel;
    }

    public void setFloorLevel(int floorLevel) {
        this.floorLevel = floorLevel;
    }

    public SlotType getSlotType() {
        return slotType;
    }

    public void setSlotType(SlotType slotType) {
        this.slotType = slotType;
    }

    public SlotStatus getStatus() {
        return status;
    }

    public void setStatus(SlotStatus status) {
        this.status = status;
    }

    public boolean isAvailable() {
        return this.status == SlotStatus.AVAILABLE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParkingSlot that = (ParkingSlot) o;
        return Objects.equals(slotId, that.slotId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(slotId);
    }

    @Override
    public String toString() {
        return "ParkingSlot{" +
                "slotId=" + slotId +
                ", locationId=" + locationId +
                ", slotNumber='" + slotNumber + '\'' +
                ", floorLevel=" + floorLevel +
                ", slotType=" + slotType +
                ", status=" + status +
                '}';
    }
}
