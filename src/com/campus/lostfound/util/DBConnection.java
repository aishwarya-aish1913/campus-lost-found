package com.campus.lostfound.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/campus_lost_found_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root"; // <--- UNGA PASSWORD

    static {
        try {
            // Explicit MySQL JDBC Driver Load (OOPs Unit V)
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver Class Error: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}