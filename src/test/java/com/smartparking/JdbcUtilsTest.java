package com.smartparking;

import com.smartparking.dao.DaoException;
import com.smartparking.model.enums.PaymentMethod;
import com.smartparking.model.enums.PaymentStatus;
import com.smartparking.model.enums.ReservationStatus;
import com.smartparking.model.enums.Role;
import com.smartparking.model.enums.SlotStatus;
import com.smartparking.model.enums.SlotType;
import com.smartparking.util.JdbcUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying JdbcUtils null safety, type conversions, and exception wrapping.
 * Runs completely offline without requiring a live database connection.
 */
public class JdbcUtilsTest {

    @Test
    @DisplayName("Verify Timestamp and LocalDateTime bi-directional conversions")
    void testTimestampConversions() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 6, 14, 30, 0);
        Timestamp ts = JdbcUtils.toTimestamp(now);
        assertNotNull(ts);
        assertEquals(Timestamp.valueOf(now), ts);

        LocalDateTime roundTrip = JdbcUtils.toLocalDateTime(ts);
        assertEquals(now, roundTrip);

        assertNull(JdbcUtils.toTimestamp(null));
        assertNull(JdbcUtils.toLocalDateTime(null));
    }

    @Test
    @DisplayName("Verify safe Enum parsing with valid, invalid, and null values")
    void testSafeEnumParsing() {
        assertEquals(Role.ADMIN, JdbcUtils.parseEnum(Role.class, "ADMIN", Role.CUSTOMER));
        assertEquals(Role.CUSTOMER, JdbcUtils.parseEnum(Role.class, "CUSTOMER", Role.ADMIN));
        
        // Fallback for null or blank
        assertEquals(Role.CUSTOMER, JdbcUtils.parseEnum(Role.class, null, Role.CUSTOMER));
        assertEquals(Role.CUSTOMER, JdbcUtils.parseEnum(Role.class, "   ", Role.CUSTOMER));

        // Fallback for unrecognized value
        assertEquals(SlotStatus.AVAILABLE, JdbcUtils.parseEnum(SlotStatus.class, "UNKNOWN_STATUS", SlotStatus.AVAILABLE));
        assertEquals(SlotType.STANDARD, JdbcUtils.parseEnum(SlotType.class, "SUPER_SLOT", SlotType.STANDARD));
        assertEquals(ReservationStatus.CONFIRMED, JdbcUtils.parseEnum(ReservationStatus.class, "INVALID", ReservationStatus.CONFIRMED));
        assertEquals(PaymentStatus.PENDING, JdbcUtils.parseEnum(PaymentStatus.class, "VOID", PaymentStatus.PENDING));
        assertEquals(PaymentMethod.CARD, JdbcUtils.parseEnum(PaymentMethod.class, "CRYPTO", PaymentMethod.CARD));
    }

    @Test
    @DisplayName("Verify DaoException constructors and exception wrapping")
    void testDaoException() {
        DaoException ex1 = new DaoException("Custom database error");
        assertEquals("Custom database error", ex1.getMessage());

        Throwable cause = new IllegalArgumentException("Root failure");
        DaoException ex2 = new DaoException("Wrapped error", cause);
        assertEquals("Wrapped error", ex2.getMessage());
        assertEquals(cause, ex2.getCause());

        DaoException ex3 = new DaoException(cause);
        assertEquals(cause, ex3.getCause());
    }
}
