package com.devcollab.ui;

import com.devcollab.database.DatabaseConnection;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("DevCollab - Login");

        Label titleLabel = new Label("Welcome to DevCollab");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        TextField idField = new TextField();
        idField.setPromptText("Enter your Student ID (e.g., 24BCA0002)");
        idField.setPrefWidth(250);

        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        Button registerRedirectButton = new Button("New User? Register");
        registerRedirectButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #2980b9; -fx-underline: true; -fx-cursor: hand;");

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-font-weight: bold;");

        // LOGIN ACTION TRIGGER: Validates against MySQL Students table
        loginButton.setOnAction(e -> {
            String studentId = idField.getText().trim().toUpperCase();

            if (studentId.isEmpty()) {
                statusLabel.setText("❌ Please enter your Student ID.");
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                return;
            }

            String query = "SELECT name FROM Students WHERE student_id = ?";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setString(1, studentId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String studentName = rs.getString("name");
                    statusLabel.setText("🚀 Login Successful!");
                    statusLabel.setStyle("-fx-text-fill: #2ecc71;");

                    // Launch Dashboard dynamically with real data
                    new DashboardApp().showDashboard(studentId, studentName);
                    primaryStage.close(); 
                } else {
                    statusLabel.setText("❌ ID not found! Please register first.");
                    statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                }

            } catch (SQLException ex) {
                statusLabel.setText("❌ Database connection error!");
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                ex.printStackTrace();
            }
        });

        // REDIRECT ACTION TRIGGER: Opens registration screen
     // 🚀 UPDATED REDIRECT ACTION TRIGGER: Launches the registration stage correctly
     // 🚀 CLEAN REDIRECT ACTION TRIGGER: Calls the structural instance method safely
        registerRedirectButton.setOnAction(e -> {
            // Instantiate the layout class and call its show method cleanly
            new MainApp().showRegistrationWindow(new Stage());
            
            // Close the current login window
            primaryStage.close();
        });
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(titleLabel, idField, loginButton, registerRedirectButton, statusLabel);

        Scene scene = new Scene(layout, 380, 280);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}