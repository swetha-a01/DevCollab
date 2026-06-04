package com.devcollab.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {
    
    /**
     * Broadcasts a new skill exchange request to the public feed bulletin board.
     * Tokens are verified at the UI layer but remain in the poster's wallet until completion (Escrow).
     */
    public static boolean postTask(String studentId, String title, int tokens) {
        String query = "INSERT INTO Tasks (student_id, title, tokens_offered, status) VALUES (?, ?, ?, 'OPEN')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, studentId);
            stmt.setString(2, title);
            stmt.setInt(3, tokens);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("❌ SQL Error inside TaskDAO.postTask!");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Fetches all active open tasks across the entire campus network to populate the public feed bulletin list.
     */
    public static List<String> getAllOpenTasks() {
        List<String> taskList = new ArrayList<>();
        String query = "SELECT task_id, student_id, title, tokens_offered FROM Tasks WHERE status = 'OPEN' ORDER BY task_id DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                int id = rs.getInt("task_id");
                String author = rs.getString("student_id");
                String title = rs.getString("title");
                int tokens = rs.getInt("tokens_offered");
                
                // Formats the exact string parsed by the JavaFX selection monitors
                taskList.add("#ID:" + id + " 📌 [" + author + "] " + title + " ➔ 🪙 " + tokens + " Tokens");
            }
        } catch (SQLException e) {
            System.out.println("❌ SQL Error inside TaskDAO.getAllOpenTasks!");
            e.printStackTrace();
        }
        return taskList;
    }

    /**
     * Handles a student claiming an open task request.
     * Pure Escrow Rule: Changes status to 'CLAIMED' and assigns the helper_id. No tokens move yet!
     */
    public static boolean claimTask(String taskId, String helperId, int tokensToEarn) {
        String updateTaskQuery = "UPDATE Tasks SET status = 'CLAIMED', helper_id = ? WHERE task_id = ? AND status = 'OPEN'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateTaskQuery)) {
            
            stmt.setString(1, helperId);
            stmt.setInt(2, Integer.parseInt(taskId));
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("❌ SQL Error inside TaskDAO.claimTask!");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Finalizes a workspace assignment transaction inside an atomic SQL block.
     * Deducts the tokens from the poster, rewards the helper, and marks the task as COMPLETED.
     */
    public static boolean completeTask(int taskId) {
        String selectQuery = "SELECT student_id, helper_id, tokens_offered FROM Tasks WHERE task_id = ?";
        String updateTaskStatus = "UPDATE Tasks SET status = 'COMPLETED' WHERE task_id = ?";
        String deductPosterTokens = "UPDATE Students SET tokens = tokens - ? WHERE student_id = ?";
        String rewardHelperTokens = "UPDATE Students SET tokens = tokens + ? WHERE student_id = ?";
        
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Begin ACID Transaction Block

            String posterId = "";
            String helperId = "";
            int tokensToMove = 0;

            // Step 1: Read the task metadata mapping
            try (PreparedStatement stmt = conn.prepareStatement(selectQuery)) {
                stmt.setInt(1, taskId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        posterId = rs.getString("student_id");
                        helperId = rs.getString("helper_id");
                        tokensToMove = rs.getInt("tokens_offered");
                    }
                }
            }

            // Safety Guard: Stop execution if the task hasn't actually been claimed by anyone
            if (helperId == null || helperId.trim().isEmpty()) {
                System.out.println("⚠️ Aborting completion: No helper is registered to claim this task ID.");
                conn.rollback();
                return false;
            }

            // Step 2: Deduct the token capital out of the requester's account row
            try (PreparedStatement stmtDeduct = conn.prepareStatement(deductPosterTokens)) {
                stmtDeduct.setInt(1, tokensToMove);
                stmtDeduct.setString(2, posterId);
                stmtDeduct.executeUpdate();
            }

            // Step 3: Credit the token payload into the peer specialist helper's account row
            try (PreparedStatement stmtReward = conn.prepareStatement(rewardHelperTokens)) {
                stmtReward.setInt(1, tokensToMove);
                stmtReward.setString(2, helperId);
                stmtReward.executeUpdate();
            }

            // Step 4: Toggle operational system flag to clean the dashboard display logs
            try (PreparedStatement stmtComplete = conn.prepareStatement(updateTaskStatus)) {
                stmtComplete.setInt(1, taskId);
                stmtComplete.executeUpdate();
            }

            conn.commit(); // Deploy all updates simultaneously
            return true;

        } catch (SQLException e) {
            System.out.println("❌ SQL Transaction Exception inside TaskDAO.completeTask! Rolling back modifications...");
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    /**
     * Aggregates relational account parameters to compute personal analytics summaries.
     */
    public static AnalyticsSummary getUserAnalytics(String studentId) {
        AnalyticsSummary summary = new AnalyticsSummary();
        
        String postedQuery = "SELECT COUNT(*) as total_posted, " +
                             "COALESCE(SUM(CASE WHEN status = 'COMPLETED' THEN tokens_offered ELSE 0 END), 0) as tokens_spent " +
                             "FROM Tasks WHERE student_id = ?";
                             
        String solvedQuery = "SELECT COUNT(*) as total_solved, " +
                             "COALESCE(SUM(tokens_offered), 0) as tokens_earned " +
                             "FROM Tasks WHERE helper_id = ? AND status = 'COMPLETED'";

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Aggregate metrics regarding tasks posted by the user
            try (PreparedStatement stmt = conn.prepareStatement(postedQuery)) {
                stmt.setString(1, studentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        summary.tasksPosted = rs.getInt("total_posted");
                        summary.tokensSpent = rs.getInt("tokens_spent");
                    }
                }
            }
            
            // Aggregate metrics regarding tasks solved by the user
            try (PreparedStatement stmt = conn.prepareStatement(solvedQuery)) {
                stmt.setString(1, studentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        summary.tasksSolved = rs.getInt("total_solved");
                        summary.tokensEarned = rs.getInt("tokens_earned");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ SQL Error inside TaskDAO.getUserAnalytics!");
            e.printStackTrace();
        }
        return summary;
    }

    /**
     * Data Transfer Object structure wrapping numeric analytics fields.
     */
    public static class AnalyticsSummary {
        public int tasksPosted = 0;
        public int tasksSolved = 0;
        public int tokensEarned = 0;
        public int tokensSpent = 0;
    }
}