package com.smartparking.model;

import com.smartparking.model.enums.Role;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing a registered user (Customer or Admin) in the system.
 */
public class User {
    private Integer userId;
    private String fullName;
    private String email;
    private String passwordHash;
    private String phoneNumber;
    private Role role;
    private String vehicleNumber;
    private LocalDateTime createdAt;

    public User() {
        this.role = Role.CUSTOMER;
        this.createdAt = LocalDateTime.now();
    }

    public User(Integer userId, String fullName, String email, String passwordHash, 
                String phoneNumber, Role role, String vehicleNumber, LocalDateTime createdAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phoneNumber = phoneNumber;
        this.role = role != null ? role : Role.CUSTOMER;
        this.vehicleNumber = vehicleNumber;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId) && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, email);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", vehicleNumber='" + vehicleNumber + '\'' +
                '}';
    }
}
