package com.devcollab.ui;

import com.devcollab.database.TaskDAO;
import com.devcollab.database.WalletDAO;
import com.devcollab.database.MessageDAO;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DashboardApp {

    private int activeChatTaskId = -1; 
    private Timeline chatRefreshTimeline; // Background Polling Heartbeat Timer

    public void showDashboard(String studentId, String studentName) {
        Stage stage = new Stage();
        stage.setTitle("DevCollab Hub - Workspace");

        // --- TOP HEADER ---
        Label welcomeLabel = new Label("Welcome back, " + studentName + "!");
        welcomeLabel.getStyleClass().add("title-label");
        
        Label tokenLabel = new Label("Wallet: " + WalletDAO.getBalance(studentId) + " Tokens");
        tokenLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #b45309; -fx-font-weight: bold;");

        Region spacerRegion = new Region();
        HBox.setHgrow(spacerRegion, Priority.ALWAYS);

        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);
        topRow.getChildren().addAll(welcomeLabel, spacerRegion, tokenLabel);
        HBox.setHgrow(topRow, Priority.ALWAYS);

        // --- STABLE ANALYTICS TAG BADGES ---
        Label statsPostedLabel = new Label("Posted: 0");
        statsPostedLabel.getStyleClass().add("analytics-metric");
        
        Label statsSolvedLabel = new Label("Solved: 0");
        statsSolvedLabel.getStyleClass().add("analytics-metric");
        
        Label statsEarnedLabel = new Label("Earned: 0 Tokens");
        statsEarnedLabel.getStyleClass().add("analytics-metric");
        
        Label statsSpentLabel = new Label("Spent: 0 Tokens");
        statsSpentLabel.getStyleClass().add("analytics-metric");
        
        HBox analyticsRow = new HBox(12, statsPostedLabel, statsSolvedLabel, statsEarnedLabel, statsSpentLabel);
        analyticsRow.setPadding(new Insets(12, 16, 12, 16));
        analyticsRow.getStyleClass().add("analytics-card");
        analyticsRow.setAlignment(Pos.CENTER_LEFT);

        // --- TASK BROADCAST SYSTEM FORM ---
        Label formTitle = new Label("Post a New Help Request");
        formTitle.getStyleClass().add("section-title");
        
        TextField taskTitleField = new TextField();
        taskTitleField.setId("taskTitleField"); // 🤖 Automation Selector Hook
        taskTitleField.setPromptText("What do you need help with? (e.g., UI Debugging)");
        HBox.setHgrow(taskTitleField, Priority.ALWAYS); 
        
        TextField tokenOfferField = new TextField();
        tokenOfferField.setId("tokenOfferField"); // 🤖 Automation Selector Hook
        tokenOfferField.setPromptText("Tokens");
        tokenOfferField.setMinWidth(75);
        tokenOfferField.setPrefWidth(75);
        
        Button postButton = new Button("Broadcast");
        postButton.setId("postButton"); // 🤖 Automation Selector Hook
        postButton.getStyleClass().addAll("button", "btn-success");
        postButton.setMinWidth(110);
        
        HBox formRow = new HBox(10, taskTitleField, tokenOfferField, postButton);
        formRow.setAlignment(Pos.CENTER_LEFT);

        // --- PANEL FEED 1: PUBLIC FEED BULLETIN ---
        Label feedTitle = new Label("Public Help Feed (Select to claim or start chat)");
        feedTitle.getStyleClass().add("section-title");
        
        ListView<String> publicFeedListView = new ListView<>();
        VBox.setVgrow(publicFeedListView, Priority.ALWAYS); 
        
        Button claimButton = new Button("Claim Selected Task");
        claimButton.getStyleClass().addAll("button", "btn-primary");
        claimButton.setMaxWidth(Double.MAX_VALUE); 

        // --- PANEL FEED 2: PERSONAL TASK MANAGER ---
        Label managingTitle = new Label("Tasks You Posted (Select to complete or view chat)");
        managingTitle.getStyleClass().add("section-title");
        
        ListView<String> personalRequestsListView = new ListView<>();
        VBox.setVgrow(personalRequestsListView, Priority.ALWAYS); 
        
        Button completeButton = new Button("Mark as Completed (Release Tokens)");
        completeButton.getStyleClass().addAll("button", "btn-warning");
        completeButton.setMaxWidth(Double.MAX_VALUE);

        Label statusLabel = new Label();
        statusLabel.setId("statusLabel"); // 🤖 Automation Selector Hook
        statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        // --- RIGHT CONTROLS: INTERACTIVE PROJECT WORKSPACE CHAT PANEL ---
        VBox chatPane = new VBox(12);
        chatPane.setPadding(new Insets(18));
        chatPane.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");
        chatPane.setMinWidth(320);
        HBox.setHgrow(chatPane, Priority.ALWAYS); 

        Label chatHeaderLabel = new Label("Room Chat Workspace");
        chatHeaderLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        TextArea chatDisplayArea = new TextArea();
        chatDisplayArea.setEditable(false);
        chatDisplayArea.setWrapText(true);
        VBox.setVgrow(chatDisplayArea, Priority.ALWAYS); 
        chatDisplayArea.setPromptText("Select an active project task card from the left feed panels to enter its private discussion room channel.");

        TextField messageInputField = new TextField();
        messageInputField.setPromptText("Write a coordination message...");
        messageInputField.setDisable(true); 
        HBox.setHgrow(messageInputField, Priority.ALWAYS);

        Button sendButton = new Button("Send");
        sendButton.getStyleClass().addAll("button", "btn-send");
        sendButton.setDisable(true);

        HBox chatInputRow = new HBox(8, messageInputField, sendButton);
        chatInputRow.setAlignment(Pos.CENTER_LEFT);
        
        chatPane.getChildren().addAll(chatHeaderLabel, chatDisplayArea, chatInputRow);

        // Operations synchronization routines
        Runnable refreshActiveChatRoom = () -> {
            if (activeChatTaskId != -1) {
                List<String> messages = MessageDAO.getChatHistory(activeChatTaskId);
                
                StringBuilder sb = new StringBuilder();
                for (String msg : messages) {
                    sb.append(msg).append("\n");
                }
                String newText = sb.toString();
                if (!chatDisplayArea.getText().equals(newText)) {
                    chatDisplayArea.setText(newText);
                    chatDisplayArea.setScrollTop(Double.MAX_VALUE); 
                }
            }
        };

        // BACKGROUND AUTO-REFRESH TIMELINE POLLER (Runs every 2 seconds)
        chatRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> {
            refreshActiveChatRoom.run();
        }));
        chatRefreshTimeline.setCycleCount(Animation.INDEFINITE);

        Runnable refreshDashboardData = () -> {
            publicFeedListView.setItems(FXCollections.observableArrayList(TaskDAO.getAllOpenTasks()));
            tokenLabel.setText("Wallet: " + WalletDAO.getBalance(studentId) + " Tokens");
            
            TaskDAO.AnalyticsSummary summary = TaskDAO.getUserAnalytics(studentId);
            statsPostedLabel.setText("Posted: " + summary.tasksPosted);
            statsSolvedLabel.setText("Solved: " + summary.tasksSolved);
            statsEarnedLabel.setText("Earned: " + summary.tokensEarned + " Tokens");
            statsSpentLabel.setText("Spent: " + summary.tokensSpent + " Tokens");

            List<String> personalTasks = new ArrayList<>();
            String query = "SELECT task_id, title, status FROM Tasks WHERE student_id = ? AND status != 'COMPLETED'";
            try (Connection conn = com.devcollab.database.DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, studentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        personalTasks.add("#ID:" + rs.getInt("task_id") + " -> " + rs.getString("title") + " [" + rs.getString("status") + "]");
                    }
                }
            } catch (SQLException e) { e.printStackTrace(); }
            personalRequestsListView.setItems(FXCollections.observableArrayList(personalTasks));
            refreshActiveChatRoom.run(); 
        };

        refreshDashboardData.run();

        // Target list click updates - Manages background timelines
        publicFeedListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                activeChatTaskId = Integer.parseInt(newVal.split(" ")[0].replace("#ID:", ""));
                chatHeaderLabel.setText("Room Chat Channel: Room #" + activeChatTaskId);
                messageInputField.setDisable(false);
                sendButton.setDisable(false);
                refreshActiveChatRoom.run();
                chatRefreshTimeline.play(); 
            }
        });

        personalRequestsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                activeChatTaskId = Integer.parseInt(newVal.split(" ")[0].replace("#ID:", ""));
                chatHeaderLabel.setText("Room Chat Channel: Room #" + activeChatTaskId);
                messageInputField.setDisable(false);
                sendButton.setDisable(false);
                refreshActiveChatRoom.run();
                chatRefreshTimeline.play(); 
            }
        });

        sendButton.setOnAction(e -> {
            String txt = messageInputField.getText().trim();
            if (!txt.isEmpty() && activeChatTaskId != -1) {
                if (MessageDAO.sendMessage(activeChatTaskId, studentId, txt)) {
                    messageInputField.clear();
                    refreshActiveChatRoom.run(); 
                }
            }
        });
        messageInputField.setOnAction(e -> sendButton.getOnAction().handle(e));

        completeButton.setOnAction(e -> {
            String selectedItem = personalRequestsListView.getSelectionModel().getSelectedItem();
            if (selectedItem == null) {
                statusLabel.setText("Select a task from your list first!");
                statusLabel.setStyle("-fx-text-fill: #ef4444;");
                return;
            }
            try {
                int taskId = Integer.parseInt(selectedItem.split(" ")[0].replace("#ID:", ""));
                if (TaskDAO.completeTask(taskId)) {
                    statusLabel.setText("Verification complete! Task finalized successfully.");
                    statusLabel.setStyle("-fx-text-fill: #10b981;");
                    refreshDashboardData.run();
                } else {
                    statusLabel.setText("Failed to finalize. Ensure someone claimed it first!");
                    statusLabel.setStyle("-fx-text-fill: #ef4444;");
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        claimButton.setOnAction(e -> {
            String selectedItem = publicFeedListView.getSelectionModel().getSelectedItem();
            if (selectedItem == null) {
                statusLabel.setText("Select an open task to claim first!");
                statusLabel.setStyle("-fx-text-fill: #ef4444;");
                return;
            }
            try {
                String taskId = selectedItem.split(" ")[0].replace("#ID:", "");
                if (selectedItem.contains("[" + studentId + "]")) {
                    statusLabel.setText("You cannot claim your own help request!");
                    statusLabel.setStyle("-fx-text-fill: #ef4444;");
                    return;
                }
                String[] parts = selectedItem.split(" -> ");
                String tokenPart = parts[parts.length - 1].replaceAll("[^0-9]", "").trim();
                int tokenAward = Integer.parseInt(tokenPart);

                if (TaskDAO.claimTask(taskId, studentId, tokenAward)) {
                    statusLabel.setText("Task claimed! Use right chat room to coordinate solution.");
                    statusLabel.setStyle("-fx-text-fill: #10b981;");
                    refreshDashboardData.run();
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        postButton.setOnAction(e -> {
            String title = taskTitleField.getText().trim();
            String tokenStr = tokenOfferField.getText().trim();
            if (title.isEmpty() || tokenStr.isEmpty()) {
                statusLabel.setText("Please fill in all fields.");
                statusLabel.setStyle("-fx-text-fill: #ef4444;");
                return;
            }
            try {
                int tokens = Integer.parseInt(tokenStr);
                int currentBalance = WalletDAO.getBalance(studentId);
                if (tokens <= 0 || tokens > currentBalance) {
                    statusLabel.setText("Invalid token amount!");
                    statusLabel.setStyle("-fx-text-fill: #ef4444;");
                    return;
                }
                if (TaskDAO.postTask(studentId, title, tokens)) {
                    statusLabel.setText("Broadcast successful!");
                    statusLabel.setStyle("-fx-text-fill: #10b981;");
                    taskTitleField.clear();
                    tokenOfferField.clear();
                    refreshDashboardData.run();
                }
            } catch (NumberFormatException ex) { statusLabel.setText("Tokens must be numeric."); }
        });

        // Stops timeline threads on exit
        stage.setOnCloseRequest(e -> {
            if (chatRefreshTimeline != null) {
                chatRefreshTimeline.stop();
            }
        });

        // Layout Assembly
        VBox leftPane = new VBox(10);
        leftPane.setPadding(new Insets(5));
        leftPane.setMinWidth(420);
        leftPane.setPrefWidth(440);
        
        leftPane.getChildren().addAll(
            topRow, analyticsRow, new Separator(), 
            formTitle, formRow, new Separator(), 
            feedTitle, publicFeedListView, claimButton, new Separator(),
            managingTitle, personalRequestsListView, completeButton, statusLabel
        );

        HBox coreLayout = new HBox(20, leftPane, chatPane);
        coreLayout.setPadding(new Insets(20));
        HBox.setHgrow(leftPane, Priority.NEVER);

        Scene scene = new Scene(coreLayout, 860, 560);
        
        String cssPath = getClass().getResource("/style.css") != null 
            ? getClass().getResource("/style.css").toExternalForm() 
            : "file:style.css";
        scene.getStylesheets().add(cssPath);

        stage.setScene(scene);
        stage.show();
    }
}