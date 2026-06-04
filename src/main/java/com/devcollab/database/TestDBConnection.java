package com.devcollab.database;

import java.sql.Connection;
import java.sql.SQLException;

public class TestDBConnection {
    public static void main(String[] args) {
        System.out.println("🔄 Attempting to connect to your MySQL terminal database...");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("🚀 SUCCESS! Java has established a secure link to MySQL.");
            }
        } catch (SQLException e) {
            System.out.println("❌ CONNECTION FAILED!");
            System.out.println("Error details: " + e.getMessage());
            e.printStackTrace();
        }
    }
}