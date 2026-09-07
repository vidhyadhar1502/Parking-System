package com.smartparking;

import com.smartparking.model.ParkingLocation;
import com.smartparking.model.ParkingSlot;
import com.smartparking.model.User;
import com.smartparking.model.enums.Role;
import com.smartparking.model.enums.SlotStatus;
import com.smartparking.model.enums.SlotType;
import com.smartparking.util.AppSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test verifying Phase 1 foundation POJOs, Enums, and AppSession state logic.
 */
public class ModelAndConfigTest {

    @BeforeEach
    void setUp() {
        AppSession.getInstance().logout();
    }

    @Test
    @DisplayName("Verify User entity creation and role checks")
    void testUserEntity() {
        User user = new User(
                1, 
                "Dr. Sarah Jenkins", 
                "admin@campus.edu", 
                "$2a$10$2YDLk6yCPn0JTdje0BthDe/ScvhOBZu8xlstWGJlnp7IVyh8V9jry", 
                "+91 98401 23456", 
                Role.ADMIN, 
                "TN 07 CE 0001", 
                LocalDateTime.now()
        );

        assertNotNull(user);
        assertEquals(1, user.getUserId());
        assertEquals("Dr. Sarah Jenkins", user.getFullName());
        assertTrue(user.isAdmin());
        assertEquals(Role.ADMIN, user.getRole());
    }

    @Test
    @DisplayName("Verify ParkingLocation entity creation")
    void testParkingLocationEntity() {
        ParkingLocation loc = new ParkingLocation(
                1,
                "CEG Main Academic Deck",
                "College of Engineering Guindy, Chennai",
                new BigDecimal("13.01090000"),
                new BigDecimal("80.23550000"),
                16,
                new BigDecimal("30.00"),
                true,
                LocalDateTime.now()
        );

        assertEquals(1, loc.getLocationId());
        assertEquals("CEG Main Academic Deck", loc.getName());
        assertEquals(16, loc.getTotalCapacity());
        assertEquals(new BigDecimal("30.00"), loc.getHourlyRate());
        assertTrue(loc.isActive());
    }

    @Test
    @DisplayName("Verify ParkingSlot entity creation and availability")
    void testParkingSlotEntity() {
        ParkingSlot slot = new ParkingSlot(
                1,
                1,
                "A-101",
                1,
                SlotType.HANDICAPPED,
                SlotStatus.AVAILABLE
        );

        assertEquals("A-101", slot.getSlotNumber());
        assertEquals(SlotType.HANDICAPPED, slot.getSlotType());
        assertEquals(SlotStatus.AVAILABLE, slot.getStatus());
        assertTrue(slot.isAvailable());

        slot.setStatus(SlotStatus.OCCUPIED);
        assertFalse(slot.isAvailable());
    }

    @Test
    @DisplayName("Verify AppSession lifecycle (Login, Roles, Logout)")
    void testAppSessionLifecycle() {
        AppSession session = AppSession.getInstance();
        assertFalse(session.isLoggedIn());
        assertNull(session.getCurrentUser());

        User admin = new User(1, "Admin User", "admin@campus.edu", "hash", "+91 98400 11223", Role.ADMIN, "TN 01 AA 1111", LocalDateTime.now());
        session.login(admin);

        assertTrue(session.isLoggedIn());
        assertTrue(session.isAdmin());
        assertFalse(session.isCustomer());
        assertEquals("admin@campus.edu", session.getCurrentUser().getEmail());

        session.logout();
        assertFalse(session.isLoggedIn());
        assertNull(session.getCurrentUser());
    }
}
