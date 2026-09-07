package com.smartparking.dao.jdbc;

import com.smartparking.config.DatabaseConfig;
import com.smartparking.dao.DaoException;
import com.smartparking.dao.ParkingSlotDao;
import com.smartparking.model.ParkingSlot;
import com.smartparking.model.enums.SlotStatus;
import com.smartparking.model.enums.SlotType;
import com.smartparking.util.JdbcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link ParkingSlotDao} using HikariCP, PreparedStatements,
 * and support for row-level locking via {@code SELECT ... FOR UPDATE}.
 */
public class JdbcParkingSlotDao implements ParkingSlotDao {

    private static final Logger logger = LoggerFactory.getLogger(JdbcParkingSlotDao.class);

    private static final String SELECT_COLUMNS = 
            "slot_id, location_id, slot_number, floor_level, slot_type, status";

    private final DataSource dataSource;

    public JdbcParkingSlotDao() {
        this(DatabaseConfig.getDataSource());
    }

    public JdbcParkingSlotDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    @Override
    public Optional<ParkingSlot> findById(int slotId) {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM parking_slots WHERE slot_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error finding parking slot by id: {}", slotId, e);
            throw new DaoException("Failed to find parking slot with id: " + slotId, e);
        }
    }

    @Override
    public List<ParkingSlot> findAll() {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM parking_slots ORDER BY location_id ASC, floor_level ASC, slot_number ASC";
        List<ParkingSlot> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error retrieving all parking slots", e);
            throw new DaoException("Failed to retrieve parking slots list", e);
        }
    }

    @Override
    public List<ParkingSlot> findByLocationId(int locationId) {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM parking_slots WHERE location_id = ? ORDER BY floor_level ASC, slot_number ASC";
        List<ParkingSlot> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, locationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error finding slots for location: {}", locationId, e);
            throw new DaoException("Failed to retrieve slots for location id: " + locationId, e);
        }
    }

    @Override
    public List<ParkingSlot> findByLocationAndStatus(int locationId, SlotStatus status) {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM parking_slots WHERE location_id = ? AND status = ? ORDER BY floor_level ASC, slot_number ASC";
        List<ParkingSlot> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, locationId);
            ps.setString(2, status != null ? status.name() : SlotStatus.AVAILABLE.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error finding slots by location {} and status {}: ", locationId, status, e);
            throw new DaoException("Failed to retrieve slots for location " + locationId + " with status: " + status, e);
        }
    }

    @Override
    public List<ParkingSlot> findByFloor(int locationId, int floorLevel) {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM parking_slots WHERE location_id = ? AND floor_level = ? ORDER BY slot_number ASC";
        List<ParkingSlot> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, locationId);
            ps.setInt(2, floorLevel);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error finding slots by location {} and floor {}: ", locationId, floorLevel, e);
            throw new DaoException("Failed to retrieve slots on floor " + floorLevel + " for location: " + locationId, e);
        }
    }

    @Override
    public long countByLocationAndStatus(int locationId, SlotStatus status) {
        String sql = "SELECT COUNT(*) FROM parking_slots WHERE location_id = ? AND status = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, locationId);
            ps.setString(2, status != null ? status.name() : SlotStatus.AVAILABLE.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            return 0L;
        } catch (SQLException e) {
            logger.error("Error counting slots for location {} and status {}: ", locationId, status, e);
            throw new DaoException("Failed to count slots for location " + locationId + " with status " + status, e);
        }
    }

    @Override
    public boolean updateStatus(int slotId, SlotStatus status) {
        String sql = "UPDATE parking_slots SET status = ? WHERE slot_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status != null ? status.name() : SlotStatus.AVAILABLE.name());
            ps.setInt(2, slotId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            logger.error("Error updating status for slot id {}: ", slotId, e);
            throw new DaoException("Failed to update status for slot id: " + slotId, e);
        }
    }

    @Override
    public int save(ParkingSlot slot) {
        String sql = "INSERT INTO parking_slots (location_id, slot_number, floor_level, slot_type, status) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, slot.getLocationId());
            ps.setString(2, slot.getSlotNumber());
            ps.setInt(3, slot.getFloorLevel());
            ps.setString(4, slot.getSlotType() != null ? slot.getSlotType().name() : SlotType.STANDARD.name());
            ps.setString(5, slot.getStatus() != null ? slot.getStatus().name() : SlotStatus.AVAILABLE.name());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new DaoException("Creating parking slot failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    slot.setSlotId(generatedId);
                    return generatedId;
                } else {
                    throw new DaoException("Creating parking slot failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving parking slot: {}", slot.getSlotNumber(), e);
            throw new DaoException("Failed to save parking slot: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(ParkingSlot slot) {
        String sql = "UPDATE parking_slots SET location_id = ?, slot_number = ?, floor_level = ?, " +
                     "slot_type = ?, status = ? WHERE slot_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slot.getLocationId());
            ps.setString(2, slot.getSlotNumber());
            ps.setInt(3, slot.getFloorLevel());
            ps.setString(4, slot.getSlotType() != null ? slot.getSlotType().name() : SlotType.STANDARD.name());
            ps.setString(5, slot.getStatus() != null ? slot.getStatus().name() : SlotStatus.AVAILABLE.name());
            ps.setInt(6, slot.getSlotId());

            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            logger.error("Error updating parking slot: {}", slot.getSlotId(), e);
            throw new DaoException("Failed to update parking slot with id: " + slot.getSlotId(), e);
        }
    }

    @Override
    public boolean deleteById(int slotId) {
        String sql = "DELETE FROM parking_slots WHERE slot_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            int rowsDeleted = ps.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            logger.error("Error deleting parking slot: {}", slotId, e);
            throw new DaoException("Failed to delete parking slot with id: " + slotId, e);
        }
    }

    @Override
    public Optional<ParkingSlot> findByIdForUpdate(Connection connection, int slotId) {
        if (connection == null) {
            throw new IllegalArgumentException("Connection must not be null for transactional locking queries.");
        }
        String sql = "SELECT " + SELECT_COLUMNS + " FROM parking_slots WHERE slot_id = ? FOR UPDATE";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error executing SELECT FOR UPDATE on slot id: {}", slotId, e);
            throw new DaoException("Failed to lock parking slot with id: " + slotId, e);
        }
    }

    /**
     * Maps the current row of the ResultSet into a {@link ParkingSlot} entity.
     */
    private ParkingSlot mapRow(ResultSet rs) throws SQLException {
        ParkingSlot slot = new ParkingSlot();
        slot.setSlotId(rs.getInt("slot_id"));
        slot.setLocationId(rs.getInt("location_id"));
        slot.setSlotNumber(rs.getString("slot_number"));
        slot.setFloorLevel(rs.getInt("floor_level"));
        slot.setSlotType(JdbcUtils.parseEnum(SlotType.class, rs.getString("slot_type"), SlotType.STANDARD));
        slot.setStatus(JdbcUtils.parseEnum(SlotStatus.class, rs.getString("status"), SlotStatus.AVAILABLE));
        return slot;
    }
}
