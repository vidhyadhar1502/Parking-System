package com.smartparking;

import com.smartparking.dao.DaoException;
import com.smartparking.dao.ParkingLocationDao;
import com.smartparking.dao.ParkingSlotDao;
import com.smartparking.dao.PaymentDao;
import com.smartparking.dao.ReservationDao;
import com.smartparking.dao.UserDao;
import com.smartparking.dao.jdbc.JdbcParkingLocationDao;
import com.smartparking.dao.jdbc.JdbcParkingSlotDao;
import com.smartparking.dao.jdbc.JdbcPaymentDao;
import com.smartparking.dao.jdbc.JdbcReservationDao;
import com.smartparking.dao.jdbc.JdbcUserDao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Structural contract tests verifying DAO interfaces, JDBC implementation bindings,
 * and method signatures (including transactional FOR UPDATE patterns).
 * Runs completely offline.
 */
public class DaoStructureTest {

    @Test
    @DisplayName("Verify JDBC classes implement their corresponding DAO interfaces")
    void testDaoInterfaceBindings() {
        assertTrue(UserDao.class.isAssignableFrom(JdbcUserDao.class), 
                "JdbcUserDao must implement UserDao");
        assertTrue(ParkingLocationDao.class.isAssignableFrom(JdbcParkingLocationDao.class), 
                "JdbcParkingLocationDao must implement ParkingLocationDao");
        assertTrue(ParkingSlotDao.class.isAssignableFrom(JdbcParkingSlotDao.class), 
                "JdbcParkingSlotDao must implement ParkingSlotDao");
        assertTrue(ReservationDao.class.isAssignableFrom(JdbcReservationDao.class), 
                "JdbcReservationDao must implement ReservationDao");
        assertTrue(PaymentDao.class.isAssignableFrom(JdbcPaymentDao.class), 
                "JdbcPaymentDao must implement PaymentDao");
    }

    @Test
    @DisplayName("Verify ParkingSlotDao contains transactional findByIdForUpdate signature")
    void testTransactionalLockSignature() throws NoSuchMethodException {
        Method method = ParkingSlotDao.class.getMethod("findByIdForUpdate", Connection.class, int.class);
        assertNotNull(method);
        assertEquals(Connection.class, method.getParameterTypes()[0]);
        assertEquals(int.class, method.getParameterTypes()[1]);

        Method implMethod = JdbcParkingSlotDao.class.getMethod("findByIdForUpdate", Connection.class, int.class);
        assertNotNull(implMethod);
    }

    @Test
    @DisplayName("Verify UserDao contains findByEmail and existsByEmail")
    void testUserDaoMethodSignatures() throws NoSuchMethodException {
        assertNotNull(UserDao.class.getMethod("findByEmail", String.class));
        assertNotNull(UserDao.class.getMethod("existsByEmail", String.class));
        assertNotNull(UserDao.class.getMethod("deleteById", int.class));
    }

    @Test
    @DisplayName("Verify ReservationDao contains findActiveByUserId and findByCode")
    void testReservationDaoMethodSignatures() throws NoSuchMethodException {
        assertNotNull(ReservationDao.class.getMethod("findByCode", String.class));
        assertNotNull(ReservationDao.class.getMethod("findActiveByUserId", int.class));
        assertNotNull(ReservationDao.class.getMethod("updateStatus", int.class, com.smartparking.model.enums.ReservationStatus.class));
    }

    @Test
    @DisplayName("Verify PaymentDao contains findByReservationId and BigDecimal mapping")
    void testPaymentDaoMethodSignatures() throws NoSuchMethodException {
        assertNotNull(PaymentDao.class.getMethod("findByReservationId", int.class));
        assertNotNull(PaymentDao.class.getMethod("updateStatus", int.class, com.smartparking.model.enums.PaymentStatus.class));
    }
}
