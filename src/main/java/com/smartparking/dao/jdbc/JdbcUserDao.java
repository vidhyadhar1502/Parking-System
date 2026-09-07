package com.smartparking.dao.jdbc;

import com.smartparking.config.DatabaseConfig;
import com.smartparking.dao.DaoException;
import com.smartparking.dao.UserDao;
import com.smartparking.model.User;
import com.smartparking.model.enums.Role;
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
 * JDBC implementation of {@link UserDao} using HikariCP and PreparedStatements.
 */
public class JdbcUserDao implements UserDao {

    private static final Logger logger = LoggerFactory.getLogger(JdbcUserDao.class);

    private static final String SELECT_COLUMNS = 
            "user_id, full_name, email, password_hash, phone_number, role, vehicle_number, created_at";

    private final DataSource dataSource;

    public JdbcUserDao() {
        this(DatabaseConfig.getDataSource());
    }

    public JdbcUserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    @Override
    public Optional<User> findById(int userId) {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM users WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error finding user by id: {}", userId, e);
            throw new DaoException("Failed to find user with id: " + userId, e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM users WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error finding user by email: {}", email, e);
            throw new DaoException("Failed to find user with email: " + email, e);
        }
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM users ORDER BY user_id DESC";
        List<User> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error finding all users", e);
            throw new DaoException("Failed to retrieve users list", e);
        }
    }

    @Override
    public List<User> findByRole(Role role) {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM users WHERE role = ? ORDER BY user_id ASC";
        List<User> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role != null ? role.name() : Role.CUSTOMER.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error finding users by role: {}", role, e);
            throw new DaoException("Failed to retrieve users by role: " + role, e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Error checking existence of email: {}", email, e);
            throw new DaoException("Failed to check user email existence: " + email, e);
        }
    }

    @Override
    public int save(User user) {
        String sql = "INSERT INTO users (full_name, email, password_hash, phone_number, role, vehicle_number, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getPhoneNumber());
            ps.setString(5, user.getRole() != null ? user.getRole().name() : Role.CUSTOMER.name());
            JdbcUtils.setNullableString(ps, 6, user.getVehicleNumber());
            
            LocalDateTime createdAt = user.getCreatedAt() != null ? user.getCreatedAt() : LocalDateTime.now();
            user.setCreatedAt(createdAt);
            ps.setTimestamp(7, JdbcUtils.toTimestamp(createdAt));

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new DaoException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    user.setUserId(generatedId);
                    return generatedId;
                } else {
                    throw new DaoException("Creating user failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving user: {}", user.getEmail(), e);
            throw new DaoException("Failed to save user: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(User user) {
        String sql = "UPDATE users SET full_name = ?, email = ?, password_hash = ?, phone_number = ?, " +
                     "role = ?, vehicle_number = ? WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getPhoneNumber());
            ps.setString(5, user.getRole() != null ? user.getRole().name() : Role.CUSTOMER.name());
            JdbcUtils.setNullableString(ps, 6, user.getVehicleNumber());
            ps.setInt(7, user.getUserId());

            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            logger.error("Error updating user: {}", user.getUserId(), e);
            throw new DaoException("Failed to update user with id: " + user.getUserId(), e);
        }
    }

    @Override
    public boolean deleteById(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            int rowsDeleted = ps.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            logger.error("Error deleting user: {}", userId, e);
            throw new DaoException("Failed to delete user with id: " + userId, e);
        }
    }

    /**
     * Maps the current row of the ResultSet into a {@link User} entity.
     */
    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setPhoneNumber(rs.getString("phone_number"));
        user.setRole(JdbcUtils.parseEnum(Role.class, rs.getString("role"), Role.CUSTOMER));
        user.setVehicleNumber(rs.getString("vehicle_number"));
        user.setCreatedAt(JdbcUtils.toLocalDateTime(rs.getTimestamp("created_at")));
        return user;
    }
}
