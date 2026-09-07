package com.smartparking.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing a physical parking structure, campus deck, or lot facility.
 */
public class ParkingLocation {
    private Integer locationId;
    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private int totalCapacity;
    private BigDecimal hourlyRate;
    private boolean isActive;
    private LocalDateTime createdAt;

    public ParkingLocation() {
        this.isActive = true;
        this.hourlyRate = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
    }

    public ParkingLocation(Integer locationId, String name, String address, 
                           BigDecimal latitude, BigDecimal longitude, 
                           int totalCapacity, BigDecimal hourlyRate, 
                           boolean isActive, LocalDateTime createdAt) {
        this.locationId = locationId;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.totalCapacity = totalCapacity;
        this.hourlyRate = hourlyRate != null ? hourlyRate : BigDecimal.ZERO;
        this.isActive = isActive;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public Integer getLocationId() {
        return locationId;
    }

    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public int getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(int totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParkingLocation that = (ParkingLocation) o;
        return Objects.equals(locationId, that.locationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locationId);
    }

    @Override
    public String toString() {
        return "ParkingLocation{" +
                "locationId=" + locationId +
                ", name='" + name + '\'' +
                ", totalCapacity=" + totalCapacity +
                ", hourlyRate=" + hourlyRate +
                ", isActive=" + isActive +
                '}';
    }
}
