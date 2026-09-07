package com.smartparking.dao.jdbc;

import com.smartparking.config.DatabaseConfig;
import com.smartparking.dao.DaoException;
import com.smartparking.dao.PaymentDao;
import com.smartparking.model.Payment;
import com.smartparking.model.enums.PaymentMethod;
import com.smartparking.model.enums.PaymentStatus;
import com.smartparking.util.JdbcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link PaymentDao} using HikariCP and PreparedStatements.
 * Enforces BigDecimal precision for all financial calculations.
 */
public class JdbcPaymentDao implements PaymentDao {

    private static final Logger logger = LoggerFactory.getLogger(JdbcPaymentDao.class);

    private static final String SELECT_COLUMNS = 
            "payment_id, reservation_id, duration_hours, total_amount, payment_status, payment_method, paid_at";

    private final DataSource dataSource;

    public JdbcPaymentDao() {
        this(DatabaseConfig.getDataSource());
    }

    public JdbcPaymentDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    @Override
    public Optional<Payment> findById(int paymentId) {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM payments WHERE payment_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paymentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error finding payment by id: {}", paymentId, e);
            throw new DaoException("Failed to find payment with id: " + paymentId, e);
        }
    }

    @Override
    public Optional<Payment> findByReservationId(int reservationId) {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM payments WHERE reservation_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error finding payment for reservation id: {}", reservationId, e);
            throw new DaoException("Failed to find payment for reservation id: " + reservationId, e);
        }
    }

    @Override
    public List<Payment> findAll() {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM payments ORDER BY payment_id DESC";
        List<Payment> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error retrieving all payments", e);
            throw new DaoException("Failed to retrieve payments list", e);
        }
    }

    @Override
    public List<Payment> findByStatus(PaymentStatus status) {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM payments WHERE payment_status = ? ORDER BY paid_at DESC";
        List<Payment> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status != null ? status.name() : PaymentStatus.PENDING.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error finding payments by status: {}", status, e);
            throw new DaoException("Failed to retrieve payments with status: " + status, e);
        }
    }

    @Override
    public int save(Payment payment) {
        String sql = "INSERT INTO payments (reservation_id, duration_hours, total_amount, " +
                     "payment_status, payment_method, paid_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, payment.getReservationId());
            ps.setBigDecimal(2, payment.getDurationHours());
            ps.setBigDecimal(3, payment.getTotalAmount());
            ps.setString(4, payment.getPaymentStatus() != null ? 
                    payment.getPaymentStatus().name() : PaymentStatus.PENDING.name());
            ps.setString(5, payment.getPaymentMethod() != null ? 
                    payment.getPaymentMethod().name() : PaymentMethod.CARD.name());

            LocalDateTime paidAt = payment.getPaidAt() != null ? payment.getPaidAt() : LocalDateTime.now();
            payment.setPaidAt(paidAt);
            ps.setTimestamp(6, JdbcUtils.toTimestamp(paidAt));

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new DaoException("Creating payment failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    payment.setPaymentId(generatedId);
                    return generatedId;
                } else {
                    throw new DaoException("Creating payment failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving payment for reservation: {}", payment.getReservationId(), e);
            throw new DaoException("Failed to save payment: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Payment payment) {
        String sql = "UPDATE payments SET reservation_id = ?, duration_hours = ?, total_amount = ?, " +
                     "payment_status = ?, payment_method = ?, paid_at = ? WHERE payment_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, payment.getReservationId());
            ps.setBigDecimal(2, payment.getDurationHours());
            ps.setBigDecimal(3, payment.getTotalAmount());
            ps.setString(4, payment.getPaymentStatus() != null ? 
                    payment.getPaymentStatus().name() : PaymentStatus.PENDING.name());
            ps.setString(5, payment.getPaymentMethod() != null ? 
                    payment.getPaymentMethod().name() : PaymentMethod.CARD.name());
            ps.setTimestamp(6, JdbcUtils.toTimestamp(payment.getPaidAt()));
            ps.setInt(7, payment.getPaymentId());

            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            logger.error("Error updating payment: {}", payment.getPaymentId(), e);
            throw new DaoException("Failed to update payment with id: " + payment.getPaymentId(), e);
        }
    }

    @Override
    public boolean updateStatus(int paymentId, PaymentStatus status) {
        String sql = "UPDATE payments SET payment_status = ? WHERE payment_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status != null ? status.name() : PaymentStatus.PENDING.name());
            ps.setInt(2, paymentId);
            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            logger.error("Error updating status for payment id {}: ", paymentId, e);
            throw new DaoException("Failed to update status for payment id: " + paymentId, e);
        }
    }

    @Override
    public boolean deleteById(int paymentId) {
        String sql = "DELETE FROM payments WHERE payment_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paymentId);
            int rowsDeleted = ps.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            logger.error("Error deleting payment: {}", paymentId, e);
            throw new DaoException("Failed to delete payment with id: " + paymentId, e);
        }
    }

    /**
     * Maps the current row of the ResultSet into a {@link Payment} entity.
     */
    private Payment mapRow(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setPaymentId(rs.getInt("payment_id"));
        payment.setReservationId(rs.getInt("reservation_id"));
        payment.setDurationHours(rs.getBigDecimal("duration_hours"));
        payment.setTotalAmount(rs.getBigDecimal("total_amount"));
        payment.setPaymentStatus(JdbcUtils.parseEnum(PaymentStatus.class, rs.getString("payment_status"), PaymentStatus.PENDING));
        payment.setPaymentMethod(JdbcUtils.parseEnum(PaymentMethod.class, rs.getString("payment_method"), PaymentMethod.CARD));
        payment.setPaidAt(JdbcUtils.toLocalDateTime(rs.getTimestamp("paid_at")));
        return payment;
    }
}
