package com.smartparking.util;

import com.smartparking.config.DatabaseConfig;
import com.smartparking.dao.DaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;

/**
 * Common JDBC Utilities for parameter mapping, null-safe type conversions,
 * and declarative transaction orchestration.
 */
public final class JdbcUtils {

    private static final Logger logger = LoggerFactory.getLogger(JdbcUtils.class);

    private JdbcUtils() {
        // Private constructor for utility class
    }

    /**
     * Converts a Java 8+ LocalDateTime to a java.sql.Timestamp.
     */
    public static Timestamp toTimestamp(LocalDateTime ldt) {
        return ldt != null ? Timestamp.valueOf(ldt) : null;
    }

    /**
     * Converts a java.sql.Timestamp to a Java 8+ LocalDateTime.
     */
    public static LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts != null ? ts.toLocalDateTime() : null;
    }

    /**
     * Safely converts a database string to an Enum constant with a fallback default.
     */
    public static <E extends Enum<E>> E parseEnum(Class<E> enumClass, String value, E defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Enum.valueOf(enumClass, value.trim());
        } catch (IllegalArgumentException e) {
            logger.warn("Unrecognized enum constant '{}' for class {}. Falling back to default: {}", 
                    value, enumClass.getSimpleName(), defaultValue);
            return defaultValue;
        }
    }

    /**
     * Safely sets a nullable String on a PreparedStatement.
     */
    public static void setNullableString(PreparedStatement ps, int paramIndex, String value) throws SQLException {
        if (value != null) {
            ps.setString(paramIndex, value);
        } else {
            ps.setNull(paramIndex, Types.VARCHAR);
        }
    }

    /**
     * Safely sets a nullable LocalDateTime as a Timestamp on a PreparedStatement.
     */
    public static void setNullableTimestamp(PreparedStatement ps, int paramIndex, LocalDateTime ldt) throws SQLException {
        if (ldt != null) {
            ps.setTimestamp(paramIndex, Timestamp.valueOf(ldt));
        } else {
            ps.setNull(paramIndex, Types.TIMESTAMP);
        }
    }

    /**
     * Safely sets a nullable Integer on a PreparedStatement.
     */
    public static void setNullableInt(PreparedStatement ps, int paramIndex, Integer value) throws SQLException {
        if (value != null) {
            ps.setInt(paramIndex, value);
        } else {
            ps.setNull(paramIndex, Types.INTEGER);
        }
    }

    /**
     * Safely sets a nullable BigDecimal on a PreparedStatement.
     */
    public static void setNullableBigDecimal(PreparedStatement ps, int paramIndex, BigDecimal value) throws SQLException {
        if (value != null) {
            ps.setBigDecimal(paramIndex, value);
        } else {
            ps.setNull(paramIndex, Types.DECIMAL);
        }
    }

    /**
     * Functional callback interface for transactional units of work that return a result.
     */
    @FunctionalInterface
    public interface TransactionCallback<T> {
        T doInTransaction(Connection connection) throws SQLException, DaoException;
    }

    /**
     * Functional callback interface for void transactional units of work.
     */
    @FunctionalInterface
    public interface TransactionAction {
        void doInTransaction(Connection connection) throws SQLException, DaoException;
    }

    /**
     * Executes a transactional block using a managed connection from DatabaseConfig.
     * Disables auto-commit, executes the callback, commits on success,
     * and rolls back automatically if any exception occurs.
     *
     * @param callback the work to execute within the transaction
     * @param <T> the return type
     * @return the result of the transactional operation
     * @throws DaoException if the transaction fails or rollbacks
     */
    public static <T> T executeTransaction(TransactionCallback<T> callback) {
        Connection conn = null;
        boolean originalAutoCommit = true;
        try {
            conn = DatabaseConfig.getConnection();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            T result = callback.doInTransaction(conn);

            conn.commit();
            return result;
        } catch (SQLException | DaoException e) {
            if (conn != null) {
                try {
                    logger.warn("Transaction encountered error. Rolling back...", e);
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Failed to rollback transaction", ex);
                }
            }
            if (e instanceof DaoException) {
                throw (DaoException) e;
            }
            throw new DaoException("Transaction execution failed: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(originalAutoCommit);
                    conn.close();
                } catch (SQLException ex) {
                    logger.error("Failed to reset auto-commit or close connection", ex);
                }
            }
        }
    }

    /**
     * Executes a void transactional block using a managed connection from DatabaseConfig.
     */
    public static void executeTransactionVoid(TransactionAction action) {
        executeTransaction(conn -> {
            action.doInTransaction(conn);
            return null;
        });
    }
}
