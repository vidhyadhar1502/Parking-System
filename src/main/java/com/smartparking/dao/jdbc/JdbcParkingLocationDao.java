package com.smartparking.dao.jdbc;

import com.smartparking.config.DatabaseConfig;
import com.smartparking.dao.DaoException;
import com.smartparking.dao.ParkingLocationDao;
import com.smartparking.model.ParkingLocation;
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
 * JDBC implementation of {@link ParkingLocationDao} using HikariCP and PreparedStatements.
 */
public class JdbcParkingLocationDao implements ParkingLocationDao {

    private static final Logger logger = LoggerFactory.getLogger(JdbcParkingLocationDao.class);

    private static final String SELECT_COLUMNS = 
            "location_id, name, address, latitude, longitude, total_capacity, hourly_rate, is_active, created_at";

    private final DataSource dataSource;

    public JdbcParkingLocationDao() {
        this(DatabaseConfig.getDataSource());
    }

    public JdbcParkingLocationDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    @Override
    public Optional<ParkingLocation> findById(int locationId) {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM parking_locations WHERE location_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, locationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error finding parking location by id: {}", locationId, e);
            throw new DaoException("Failed to find parking location with id: " + locationId, e);
        }
    }

    @Override
    public List<ParkingLocation> findAll() {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM parking_locations ORDER BY location_id ASC";
        List<ParkingLocation> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error retrieving all parking locations", e);
            throw new DaoException("Failed to retrieve parking locations list", e);
        }
    }

    @Override
    public List<ParkingLocation> findActive() {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM parking_locations WHERE is_active = TRUE ORDER BY name ASC";
        List<ParkingLocation> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error retrieving active parking locations", e);
            throw new DaoException("Failed to retrieve active parking locations", e);
        }
    }

    @Override
    public int save(ParkingLocation location) {
        String sql = "INSERT INTO parking_locations (name, address, latitude, longitude, total_capacity, " +
                     "hourly_rate, is_active, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, location.getName());
            ps.setString(2, location.getAddress());
            ps.setBigDecimal(3, location.getLatitude());
            ps.setBigDecimal(4, location.getLongitude());
            ps.setInt(5, location.getTotalCapacity());
            ps.setBigDecimal(6, location.getHourlyRate());
            ps.setBoolean(7, location.isActive());

            LocalDateTime createdAt = location.getCreatedAt() != null ? location.getCreatedAt() : LocalDateTime.now();
            location.setCreatedAt(createdAt);
            ps.setTimestamp(8, JdbcUtils.toTimestamp(createdAt));

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new DaoException("Creating parking location failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    location.setLocationId(generatedId);
                    return generatedId;
                } else {
                    throw new DaoException("Creating parking location failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving parking location: {}", location.getName(), e);
            throw new DaoException("Failed to save parking location: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(ParkingLocation location) {
        String sql = "UPDATE parking_locations SET name = ?, address = ?, latitude = ?, longitude = ?, " +
                     "total_capacity = ?, hourly_rate = ?, is_active = ? WHERE location_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, location.getName());
            ps.setString(2, location.getAddress());
            ps.setBigDecimal(3, location.getLatitude());
            ps.setBigDecimal(4, location.getLongitude());
            ps.setInt(5, location.getTotalCapacity());
            ps.setBigDecimal(6, location.getHourlyRate());
            ps.setBoolean(7, location.isActive());
            ps.setInt(8, location.getLocationId());

            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            logger.error("Error updating parking location: {}", location.getLocationId(), e);
            throw new DaoException("Failed to update parking location with id: " + location.getLocationId(), e);
        }
    }

    @Override
    public boolean deleteById(int locationId) {
        String sql = "DELETE FROM parking_locations WHERE location_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, locationId);
            int rowsDeleted = ps.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            logger.error("Error deleting parking location: {}", locationId, e);
            throw new DaoException("Failed to delete parking location with id: " + locationId, e);
        }
    }

    /**
     * Maps the current row of the ResultSet into a {@link ParkingLocation} entity.
     */
    private ParkingLocation mapRow(ResultSet rs) throws SQLException {
        ParkingLocation loc = new ParkingLocation();
        loc.setLocationId(rs.getInt("location_id"));
        loc.setName(rs.getString("name"));
        loc.setAddress(rs.getString("address"));
        loc.setLatitude(rs.getBigDecimal("latitude"));
        loc.setLongitude(rs.getBigDecimal("longitude"));
        loc.setTotalCapacity(rs.getInt("total_capacity"));
        loc.setHourlyRate(rs.getBigDecimal("hourly_rate"));
        loc.setActive(rs.getBoolean("is_active"));
        loc.setCreatedAt(JdbcUtils.toLocalDateTime(rs.getTimestamp("created_at")));
        return loc;
    }
}
