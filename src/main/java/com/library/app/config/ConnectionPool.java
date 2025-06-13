package com.library.app.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class ConnectionPool {
    private static final int POOL_SIZE = 10;
    private static final String APPLICATION_PROPERTIES = "application.properties";
    private static final String DB_URL = "db.url";
    private static final String DB_USERNAME = "db.username";
    private static final String DB_PASSWORD = "db.password";
    private static final String DB_DRIVER_CLASS_NAME = "db.driver-class-name";
    private static final String CONNECTION_POOL_INITIALIZATION_FAILED = "Connection pool initialization failed";
    private static final String INTERRUPTED_WHILE_WAITING_FOR_DB_CONNECTION = "Interrupted while waiting for DB connection";

    private static ConnectionPool instance;
    private static BlockingQueue<Connection> pool;

    private final String url;
    private final String username;
    private final String password;

    private ConnectionPool() {
        try {
            pool = new ArrayBlockingQueue<>(POOL_SIZE);
            Properties props = new Properties();
            props.load(getClass().getClassLoader().getResourceAsStream(APPLICATION_PROPERTIES));

            url = props.getProperty(DB_URL);
            username = props.getProperty(DB_USERNAME);
            password = props.getProperty(DB_PASSWORD);

            Class.forName(props.getProperty(DB_DRIVER_CLASS_NAME));

            for (int i = 0; i < POOL_SIZE; i++) {
                pool.offer(createConnection());
            }

            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
        } catch (Exception e) {
            throw new RuntimeException(CONNECTION_POOL_INITIALIZATION_FAILED, e);
        }
    }

    public static ConnectionPool getInstance() {
        if (instance == null) {
            instance = new ConnectionPool();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        try {
            Connection connection = pool.poll(5, TimeUnit.SECONDS);
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                return createConnection();
            }

            return connection;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException(INTERRUPTED_WHILE_WAITING_FOR_DB_CONNECTION, e);
        }
    }

    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    pool.offer(connection);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public void shutdown() {
        for (Connection connection : pool) {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
