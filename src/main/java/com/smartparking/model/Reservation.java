package com.smartparking.model;

import com.smartparking.model.enums.ReservationStatus;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing a parking space reservation or active parking session.
 */
public class Reservation {
    private Integer reservationId;
    private String reservationCode;
    private Integer userId;
    private Integer slotId;
    private LocalDateTime reservationTime;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private ReservationStatus status;
    private String qrCodeFilePath;
    private LocalDateTime createdAt;

    public Reservation() {
        this.status = ReservationStatus.CONFIRMED;
        this.reservationTime = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

    public Reservation(Integer reservationId, String reservationCode, Integer userId, 
                       Integer slotId, LocalDateTime reservationTime, 
                       LocalDateTime checkInTime, LocalDateTime checkOutTime, 
                       ReservationStatus status, String qrCodeFilePath, 
                       LocalDateTime createdAt) {
        this.reservationId = reservationId;
        this.reservationCode = reservationCode;
        this.userId = userId;
        this.slotId = slotId;
        this.reservationTime = reservationTime != null ? reservationTime : LocalDateTime.now();
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.status = status != null ? status : ReservationStatus.CONFIRMED;
        this.qrCodeFilePath = qrCodeFilePath;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public Integer getReservationId() {
        return reservationId;
    }

    public void setReservationId(Integer reservationId) {
        this.reservationId = reservationId;
    }

    public String getReservationCode() {
        return reservationCode;
    }

    public void setReservationCode(String reservationCode) {
        this.reservationCode = reservationCode;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getSlotId() {
        return slotId;
    }

    public void setSlotId(Integer slotId) {
        this.slotId = slotId;
    }

    public LocalDateTime getReservationTime() {
        return reservationTime;
    }

    public void setReservationTime(LocalDateTime reservationTime) {
        this.reservationTime = reservationTime;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalDateTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public String getQrCodeFilePath() {
        return qrCodeFilePath;
    }

    public void setQrCodeFilePath(String qrCodeFilePath) {
        this.qrCodeFilePath = qrCodeFilePath;
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
        Reservation that = (Reservation) o;
        return Objects.equals(reservationId, that.reservationId) && 
               Objects.equals(reservationCode, that.reservationCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservationId, reservationCode);
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "reservationId=" + reservationId +
                ", reservationCode='" + reservationCode + '\'' +
                ", userId=" + userId +
                ", slotId=" + slotId +
                ", reservationTime=" + reservationTime +
                ", status=" + status +
                '}';
    }
}
