package com.devcollab.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    // Method to save a chat message to the MySQL database
    public static boolean sendMessage(int taskId, String senderId, String text) {
        String query = "INSERT INTO Messages (task_id, sender_id, message_text) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, taskId);
            stmt.setString(2, senderId);
            stmt.setString(3, text);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("❌ SQL Error inside MessageDAO.sendMessage!");
            e.printStackTrace();
            return false;
        }
    }

    // Method to load chat history for a specific task workspace conversation
    public static List<String> getChatHistory(int taskId) {
        List<String> history = new ArrayList<>();
        String query = "SELECT sender_id, message_text FROM Messages WHERE task_id = ? ORDER BY message_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, taskId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String sender = rs.getString("sender_id");
                    String text = rs.getString("message_text");
                    history.add("[" + sender + "]: " + text);
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ SQL Error inside MessageDAO.getChatHistory!");
            e.printStackTrace();
        }
        return history;
    }
}