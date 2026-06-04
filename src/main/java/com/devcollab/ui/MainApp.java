package com.devcollab.ui;

import com.devcollab.database.StudentDAO;
import com.devcollab.logic.ValidationEngine;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainApp {

    public void showRegistrationWindow(Stage primaryStage) {
        primaryStage.setTitle("DevCollab - Campus Registration");

        // UI Header Components
        Label titleLabel = new Label("Join DevCollab Network");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label subLabel = new Label("Create an account to exchange skills.");
        subLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");

        // 💡 UPDATED INPUT FIELDS with explicit CSS IDs for robotic targeting
        TextField idField = new TextField();
        idField.setId("idField"); // 🚀 Robotic Hook 1
        idField.setPromptText("Enter Student ID (e.g., 24BCA0002)");

        TextField nameField = new TextField();
        nameField.setId("nameField"); // 🚀 Robotic Hook 2
        nameField.setPromptText("Enter Full Name");

        TextField emailField = new TextField();
        emailField.setId("emailField"); // 🚀 Robotic Hook 3
        emailField.setPromptText("Enter Campus Email");

        // Submit Button
        Button registerButton = new Button("Create Account");
        registerButton.setId("registerButton"); // 🚀 Robotic Hook 4 (Optional but good)
        registerButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        
        // Redirect to Login Link
        Button loginRedirectButton = new Button("Already have an account? Login");
        loginRedirectButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #2980b9; -fx-underline: true; -fx-cursor: hand;");

        Label statusLabel = new Label();
        statusLabel.setId("statusLabel"); // 🚀 Robotic Hook 5 (Crucial for validation assert)
        statusLabel.setStyle("-fx-font-weight: bold;");

        // Redirect Action Handler back to Login
        loginRedirectButton.setOnAction(e -> {
            new LoginApp().start(new Stage());
            primaryStage.close();
        });

        // Registration Logic Handler
        registerButton.setOnAction(e -> {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();

            if (id.isEmpty() || name.isEmpty() || email.isEmpty()) {
                statusLabel.setText("❌ All fields are required!");
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            } 
            else if (!ValidationEngine.isValidStudentID(id)) {
                statusLabel.setText("❌ Invalid ID! Use format like 24BCA0002");
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            } 
            else if (!ValidationEngine.isValidCampusEmail(email)) {
                statusLabel.setText("❌ Use campus email (@vitstudent.ac.in)");
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            } 
            else {
                boolean isSaved = StudentDAO.registerStudent(id, name, email);
                if (isSaved) {
                    statusLabel.setText("🚀 Success! Profile added to MySQL.");
                    statusLabel.setStyle("-fx-text-fill: #2ecc71;");
                    
                    new DashboardApp().showDashboard(id, name);
                    primaryStage.close();
                } else {
                    statusLabel.setText("❌ ID or Email already exists in database.");
                    statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                }
            }
        });

        // Window Layout Architecture
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(25));
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(titleLabel, subLabel, idField, nameField, emailField, registerButton, loginRedirectButton, statusLabel);

        Scene scene = new Scene(layout, 380, 340);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}