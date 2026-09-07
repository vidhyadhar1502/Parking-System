package com.smartparking.dao.jdbc;

import com.smartparking.config.DatabaseConfig;
import com.smartparking.dao.DaoException;
import com.smartparking.dao.ReservationDao;
import com.smartparking.model.Reservation;
import com.smartparking.model.enums.ReservationStatus;
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
 * JDBC implementation of {@link ReservationDao} using HikariCP and PreparedStatements.
 */
public class JdbcReservationDao implements ReservationDao {

    private static final Logger logger = LoggerFactory.getLogger(JdbcReservationDao.class);

    private static final String SELECT_COLUMNS = 
            "reservation_id, reservation_code, user_id, slot_id, reservation_time, " +
            "check_in_time, check_out_time, status, qr_code_file_path, created_at";

    private final DataSource dataSource;

    public JdbcReservationDao() {
        this(DatabaseConfig.getDataSource());
    }

    public JdbcReservationDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    @Override
    public Optional<Reservation> findById(int reservationId) {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM reservations WHERE reservation_id = ?";
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
            logger.error("Error finding reservation by id: {}", reservationId, e);
            throw new DaoException("Failed to find reservation with id: " + reservationId, e);
        }
    }

    @Override
    public Optional<Reservation> findByCode(String reservationCode) {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM reservations WHERE reservation_code = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reservationCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error finding reservation by code: {}", reservationCode, e);
            throw new DaoException("Failed to find reservation with code: " + reservationCode, e);
        }
    }

    @Override
    public List<Reservation> findAll() {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM reservations ORDER BY reservation_id DESC";
        List<Reservation> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error retrieving all reservations", e);
            throw new DaoException("Failed to retrieve reservations list", e);
        }
    }

    @Override
    public List<Reservation> findByUserId(int userId) {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM reservations WHERE user_id = ? ORDER BY reservation_time DESC";
        List<Reservation> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error finding reservations for user: {}", userId, e);
            throw new DaoException("Failed to retrieve reservations for user id: " + userId, e);
        }
    }

    @Override
    public List<Reservation> findBySlotId(int slotId) {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM reservations WHERE slot_id = ? ORDER BY reservation_time DESC";
        List<Reservation> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error finding reservations for slot: {}", slotId, e);
            throw new DaoException("Failed to retrieve reservations for slot id: " + slotId, e);
        }
    }

    @Override
    public List<Reservation> findByStatus(ReservationStatus status) {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM reservations WHERE status = ? ORDER BY reservation_time DESC";
        List<Reservation> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status != null ? status.name() : ReservationStatus.CONFIRMED.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error finding reservations by status: {}", status, e);
            throw new DaoException("Failed to retrieve reservations with status: " + status, e);
        }
    }

    @Override
    public List<Reservation> findActiveByUserId(int userId) {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM reservations " +
                     "WHERE user_id = ? AND status IN ('CONFIRMED', 'CHECKED_IN') " +
                     "ORDER BY reservation_time DESC";
        List<Reservation> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error finding active reservations for user: {}", userId, e);
            throw new DaoException("Failed to retrieve active reservations for user id: " + userId, e);
        }
    }

    @Override
    public int save(Reservation reservation) {
        String sql = "INSERT INTO reservations (reservation_code, user_id, slot_id, reservation_time, " +
                     "check_in_time, check_out_time, status, qr_code_file_path, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, reservation.getReservationCode());
            ps.setInt(2, reservation.getUserId());
            ps.setInt(3, reservation.getSlotId());

            LocalDateTime resTime = reservation.getReservationTime() != null ? 
                    reservation.getReservationTime() : LocalDateTime.now();
            reservation.setReservationTime(resTime);
            ps.setTimestamp(4, JdbcUtils.toTimestamp(resTime));

            JdbcUtils.setNullableTimestamp(ps, 5, reservation.getCheckInTime());
            JdbcUtils.setNullableTimestamp(ps, 6, reservation.getCheckOutTime());
            ps.setString(7, reservation.getStatus() != null ? 
                    reservation.getStatus().name() : ReservationStatus.CONFIRMED.name());
            JdbcUtils.setNullableString(ps, 8, reservation.getQrCodeFilePath());

            LocalDateTime createdAt = reservation.getCreatedAt() != null ? 
                    reservation.getCreatedAt() : LocalDateTime.now();
            reservation.setCreatedAt(createdAt);
            ps.setTimestamp(9, JdbcUtils.toTimestamp(createdAt));

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new DaoException("Creating reservation failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    reservation.setReservationId(generatedId);
                    return generatedId;
                } else {
                    throw new DaoException("Creating reservation failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving reservation: {}", reservation.getReservationCode(), e);
            throw new DaoException("Failed to save reservation: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Reservation reservation) {
        String sql = "UPDATE reservations SET reservation_code = ?, user_id = ?, slot_id = ?, " +
                     "reservation_time = ?, check_in_time = ?, check_out_time = ?, status = ?, " +
                     "qr_code_file_path = ? WHERE reservation_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reservation.getReservationCode());
            ps.setInt(2, reservation.getUserId());
            ps.setInt(3, reservation.getSlotId());
            ps.setTimestamp(4, JdbcUtils.toTimestamp(reservation.getReservationTime()));
            JdbcUtils.setNullableTimestamp(ps, 5, reservation.getCheckInTime());
            JdbcUtils.setNullableTimestamp(ps, 6, reservation.getCheckOutTime());
            ps.setString(7, reservation.getStatus() != null ? 
                    reservation.getStatus().name() : ReservationStatus.CONFIRMED.name());
            JdbcUtils.setNullableString(ps, 8, reservation.getQrCodeFilePath());
            ps.setInt(9, reservation.getReservationId());

            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            logger.error("Error updating reservation: {}", reservation.getReservationId(), e);
            throw new DaoException("Failed to update reservation with id: " + reservation.getReservationId(), e);
        }
    }

    @Override
    public boolean updateStatus(int reservationId, ReservationStatus status) {
        String sql = "UPDATE reservations SET status = ? WHERE reservation_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status != null ? status.name() : ReservationStatus.CONFIRMED.name());
            ps.setInt(2, reservationId);
            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            logger.error("Error updating status for reservation id {}: ", reservationId, e);
            throw new DaoException("Failed to update status for reservation id: " + reservationId, e);
        }
    }

    @Override
    public boolean deleteById(int reservationId) {
        String sql = "DELETE FROM reservations WHERE reservation_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            int rowsDeleted = ps.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            logger.error("Error deleting reservation: {}", reservationId, e);
            throw new DaoException("Failed to delete reservation with id: " + reservationId, e);
        }
    }

    /**
     * Maps the current row of the ResultSet into a {@link Reservation} entity.
     */
    private Reservation mapRow(ResultSet rs) throws SQLException {
        Reservation res = new Reservation();
        res.setReservationId(rs.getInt("reservation_id"));
        res.setReservationCode(rs.getString("reservation_code"));
        res.setUserId(rs.getInt("user_id"));
        res.setSlotId(rs.getInt("slot_id"));
        res.setReservationTime(JdbcUtils.toLocalDateTime(rs.getTimestamp("reservation_time")));
        res.setCheckInTime(JdbcUtils.toLocalDateTime(rs.getTimestamp("check_in_time")));
        res.setCheckOutTime(JdbcUtils.toLocalDateTime(rs.getTimestamp("check_out_time")));
        res.setStatus(JdbcUtils.parseEnum(ReservationStatus.class, rs.getString("status"), ReservationStatus.CONFIRMED));
        res.setQrCodeFilePath(rs.getString("qr_code_file_path"));
        res.setCreatedAt(JdbcUtils.toLocalDateTime(rs.getTimestamp("created_at")));
        return res;
    }
}
