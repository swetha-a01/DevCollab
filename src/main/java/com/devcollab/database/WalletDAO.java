package com.devcollab.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WalletDAO {

    // Method to fetch current real-time token balance from MySQL
    public static int getBalance(String studentId) {
        String query = "SELECT tokens FROM Students WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("tokens");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // Returns 0 if there's an error
    }

    // Method to deduct tokens when a user broadcasts a task request
    public static boolean deductTokens(String studentId, int amount) {
        String query = "UPDATE Students SET tokens = tokens - ? WHERE student_id = ? AND tokens >= ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, amount);
            stmt.setString(2, studentId);
            stmt.setInt(3, amount); // Ensures balance doesn't drop below zero
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Method to increment tokens when a user earns tokens from a completed task
    public static boolean incrementTokens(String studentId, int amount) {
        String query = "UPDATE Students SET tokens = tokens + ? WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, amount);
            stmt.setString(2, studentId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}