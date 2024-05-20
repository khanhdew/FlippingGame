package com.khanhdew.testjfx.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnector {
    private static DbConnector instance;
    private Connection connection;

    private DbConnector() {
        try {
            String DB_URL = "jdbc:postgresql://localhost:5432/flipping";
            String user_name = "postgres";
            String password = "khanhdew123aA@#$";
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(DB_URL, user_name, password);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
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

}