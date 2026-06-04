package com.devcollab.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StudentDAO {
    
    // Method to save a new student onto the DevCollab network
    public static boolean registerStudent(String studentId, String name, String email) {
        String query = "INSERT INTO Students (student_id, name, email) VALUES (?, ?, ?)";
        
        // Try-with-resources automatically closes the connection when done
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            // Binding our form inputs safely to prevent SQL injection vulnerabilities
            stmt.setString(1, studentId);
            stmt.setString(2, name);
            stmt.setString(3, email);
            
            // Executes the insert statement. Returns greater than 0 if a row was created.
            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
            
        } catch (SQLException e) {
            System.out.println("❌ Database Insertion Error!");
            e.printStackTrace();
            return false;
        }
    }
}