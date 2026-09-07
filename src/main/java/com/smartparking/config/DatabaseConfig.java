package com.smartparking.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Enterprise Database Configuration & HikariCP Connection Pool Manager.
 * Credentials are securely loaded via Environment Variables first,
 * with fallback to classpath/external 'db.properties' configuration.
 * Hard-coded secrets are strictly avoided.
 */
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    private static volatile HikariDataSource dataSource;
    private static final Object LOCK = new Object();

    private DatabaseConfig() {
        // Private constructor for singleton
    }

    /**
     * Initializes and returns the HikariCP DataSource.
     */
    public static DataSource getDataSource() {
        if (dataSource == null) {
            synchronized (LOCK) {
                if (dataSource == null) {
                    initDataSource();
                }
            }
        }
        return dataSource;
    }

    /**
     * Obtains a managed database connection from the HikariCP connection pool.
     *
     * @return an active SQL Connection
     * @throws SQLException if a connection cannot be leased from the pool
     */
    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    /**
     * Initializes the HikariCP configuration with high-performance defaults.
     */
    private static void initDataSource() {
        Properties props = loadProperties();

        String host = getEnvOrProperty("DB_HOST", "db.host", props, "localhost");
        String port = getEnvOrProperty("DB_PORT", "db.port", props, "3306");
        String dbName = getEnvOrProperty("DB_NAME", "db.name", props, "smart_parking");
        String user = getEnvOrProperty("DB_USER", "db.user", props, "root");
        String password = getEnvOrProperty("DB_PASSWORD", "db.password", props, "");

        String jdbcUrl = String.format(
                "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8",
                host, port, dbName
        );

        logger.info("Initializing HikariCP connection pool for database: {}@{}:{}", dbName, host, port);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(user);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // Pool Performance & Resilience Tuning
        config.setMaximumPoolSize(getIntProp(props, "hikaricp.maximumPoolSize", 10));
        config.setMinimumIdle(getIntProp(props, "hikaricp.minimumIdle", 2));
        config.setIdleTimeout(getLongProp(props, "hikaricp.idleTimeout", 30000L));
        config.setConnectionTimeout(getLongProp(props, "hikaricp.connectionTimeout", 20000L));
        config.setMaxLifetime(getLongProp(props, "hikaricp.maxLifetime", 1800000L));
        config.setPoolName("SmartParkingHikariPool");

        // MySQL Statement Caching Optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        dataSource = new HikariDataSource(config);
        logger.info("HikariCP pool initialized successfully.");
    }

    /**
     * Resolves configuration preference: Environment Variable -> Properties File -> Fallback Default
     */
    private static String getEnvOrProperty(String envKey, String propKey, Properties props, String defaultValue) {
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.trim().isEmpty()) {
            return envVal.trim();
        }
        String propVal = props.getProperty(propKey);
        if (propVal != null && !propVal.trim().isEmpty()) {
            return propVal.trim();
        }
        return defaultValue;
    }

    private static int getIntProp(Properties props, String key, int defaultValue) {
        try {
            String val = props.getProperty(key);
            return val != null ? Integer.parseInt(val.trim()) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static long getLongProp(Properties props, String key, long defaultValue) {
        try {
            String val = props.getProperty(key);
            return val != null ? Long.parseLong(val.trim()) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Loads 'db.properties' from the classpath if available.
     */
    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream in = DatabaseConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
                logger.info("Loaded external db.properties configuration.");
            }
        } catch (Exception e) {
            logger.warn("Could not read db.properties from classpath (using environment defaults): {}", e.getMessage());
        }
        return props;
    }

    /**
     * Tests whether a database connection can currently be leased from the pool.
     *
     * @return true if connectivity is confirmed, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            logger.error("Database connection check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Gracefully flushes and closes all connections in the pool upon app shutdown.
     */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            logger.info("Closing HikariCP connection pool...");
            dataSource.close();
        }
    }
}
