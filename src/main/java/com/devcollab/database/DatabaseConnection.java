package com.devcollab.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // URL points to your local machine (localhost) on the default MySQL port (3306)
    private static final String URL = "jdbc:mysql://localhost:3306/devcollab_db";
    private static final String USER = "root";
    
    // ⚠️ REPLACE THIS with the exact root password you typed into your MySQL terminal!
    private static final String PASSWORD = "luffy0129"; 

    public static Connection getConnection() throws SQLException {
        try {
            // This loads the driver we just configured in your pom.xml
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found in classpath.", e);
        }
    }
}