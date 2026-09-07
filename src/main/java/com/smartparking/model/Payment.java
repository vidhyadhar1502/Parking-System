package com.smartparking.model;

import com.smartparking.model.enums.PaymentMethod;
import com.smartparking.model.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing a billing record and financial settlement for a parking reservation.
 */
public class Payment {
    private Integer paymentId;
    private Integer reservationId;
    private BigDecimal durationHours;
    private BigDecimal totalAmount;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private LocalDateTime paidAt;

    public Payment() {
        this.durationHours = BigDecimal.ZERO;
        this.totalAmount = BigDecimal.ZERO;
        this.paymentStatus = PaymentStatus.PENDING;
        this.paymentMethod = PaymentMethod.CARD;
        this.paidAt = LocalDateTime.now();
    }

    public Payment(Integer paymentId, Integer reservationId, BigDecimal durationHours, 
                   BigDecimal totalAmount, PaymentStatus paymentStatus, 
                   PaymentMethod paymentMethod, LocalDateTime paidAt) {
        this.paymentId = paymentId;
        this.reservationId = reservationId;
        this.durationHours = durationHours != null ? durationHours : BigDecimal.ZERO;
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
        this.paymentStatus = paymentStatus != null ? paymentStatus : PaymentStatus.PENDING;
        this.paymentMethod = paymentMethod != null ? paymentMethod : PaymentMethod.CARD;
        this.paidAt = paidAt != null ? paidAt : LocalDateTime.now();
    }

    public Integer getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Integer paymentId) {
        this.paymentId = paymentId;
    }

    public Integer getReservationId() {
        return reservationId;
    }

    public void setReservationId(Integer reservationId) {
        this.reservationId = reservationId;
    }

    public BigDecimal getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(BigDecimal durationHours) {
        this.durationHours = durationHours;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public boolean isPaid() {
        return this.paymentStatus == PaymentStatus.PAID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return Objects.equals(paymentId, payment.paymentId) && 
               Objects.equals(reservationId, payment.reservationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentId, reservationId);
    }

    @Override
    public String toString() {
        return "Payment{" +
                "paymentId=" + paymentId +
                ", reservationId=" + reservationId +
                ", durationHours=" + durationHours +
                ", totalAmount=" + totalAmount +
                ", paymentStatus=" + paymentStatus +
                ", paymentMethod=" + paymentMethod +
                '}';
    }
}
