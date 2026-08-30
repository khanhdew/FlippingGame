package com.khanhdew.flipping.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class DbConnector {
    private static DbConnector instance;
    private Connection connection;

    private DbConnector() {
        try {
            String userHome = System.getProperty("user.home");
            File appDir = new File(userHome, ".flippinggame");
            if (!appDir.exists()) {
                appDir.mkdirs();
            }
            File dbFile = new File(appDir, "flipping.db");
            String DB_URL = "jdbc:sqlite:" + dbFile.getAbsolutePath();

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(true);
            if (executeScript(connection)) {
                System.out.println("Script executed successfully");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC Driver not found", e);
        } catch (IOException e) {
            System.err.println("Schema file not found or failed to execute: " + e.getMessage());
        }
        System.out.println("Connected to database");
    }

    public static synchronized DbConnector getInstance() {
        if (instance == null) {
            instance = new DbConnector();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeDB() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    boolean executeScript(Connection connection) throws IOException, SQLException {
        try (InputStream in = getClass().getResourceAsStream("/schema.sql")) {
            if (in == null) {
                throw new IOException("Resource /schema.sql not found on classpath");
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                StringBuilder currentStatement = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("--")) {
                        continue;
                    }
                    if (line.endsWith(";")) {
                        currentStatement.append(line, 0, line.length() - 1);
                        try (Statement statement = connection.createStatement()) {
                            statement.execute(currentStatement.toString());
                        }
                        currentStatement.setLength(0);
                    } else {
                        currentStatement.append(line).append(" ");
                    }
                }
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ignored) {
            }
            throw e;
        }
        return true;
    }
}