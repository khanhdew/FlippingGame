package com.khanhdew.flipping.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;


public class DbConnector {
    private static DbConnector instance;
    private Connection connection;

    private DbConnector() {
        try {
            String DB_URL = "jdbc:sqlite:flipping.db";
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(true);
            if (executeScript(connection)) {
                System.out.println("Script executed successfully");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            System.out.println("Schema file not found");
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
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/schema.sql"))) {
            StringBuilder currentStatement = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.endsWith(";")) {
                    currentStatement.append(line, 0, line.length() - 1);
                    try (Statement statement = connection.createStatement()) {
                        statement.execute(currentStatement.toString());
                    }
                    currentStatement.setLength(0);
                } else {
                    currentStatement.append(line);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
        return true;
    }
}