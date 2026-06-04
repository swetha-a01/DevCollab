package com.devcollab.ui;

import com.devcollab.database.DatabaseConnection;
import com.devcollab.database.WalletDAO;
import javafx.stage.Stage;
import javafx.scene.control.TextField; 
import javafx.scene.control.Label;     
import org.junit.jupiter.api.BeforeEach; 
import org.junit.jupiter.api.Test;       
import org.testfx.framework.junit5.ApplicationTest; 
import org.testfx.matcher.control.TextInputControlMatchers; // 🚀 FIXED: Added the explicit text matching package asset

import static org.testfx.api.FxAssert.verifyThat; 
import static org.junit.jupiter.api.Assertions.assertEquals; 

import java.sql.Connection;
import java.sql.Statement;

public class DashboardAutomationTest extends ApplicationTest {

    private DashboardApp app;
    private final String TEST_USER_ID = "24BCA0225";
    private final String TEST_USER_NAME = "Swetha A";

    @Override
    public void start(Stage stage) throws Exception {
        app = new DashboardApp();
        app.showDashboard(TEST_USER_ID, TEST_USER_NAME);
    }

    @BeforeEach 
    public void setUpDatabaseSlate() throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("TRUNCATE TABLE Messages");
            stmt.executeUpdate("DELETE FROM Tasks");
            stmt.executeUpdate("UPDATE Students SET tokens = 5 WHERE student_id = '" + TEST_USER_ID + "'");
        }
    }

    @Test
    public void testAutomatedTaskBroadcastEscrowFlow() {
        // 1. Click task title text input box and type a task description
        clickOn("#taskTitleField");
        write("Automated Test Request");

        // 2. Click token offer field and type a numeric pledge amount
        clickOn("#tokenOfferField");
        write("2");

        // 3. Click the broadcast submit button to deploy to database
        clickOn("#postButton");

        // 4. 🔥 FIXED: Uses explicit TestFX Matchers to check if text fields successfully cleared
        verifyThat("#taskTitleField", TextInputControlMatchers.hasText(""));
        verifyThat("#tokenOfferField", TextInputControlMatchers.hasText(""));

        // 5. Assert that tokens were not deducted from creator yet (Escrow validation check)
        int currentBalance = WalletDAO.getBalance(TEST_USER_ID);
        assertEquals(5, currentBalance, "Escrow Violation: Tokens were deducted immediately upon broadcast!");
        
        // 6. Verify layout alerts display confirmation messages
        verifyThat("#statusLabel", (Label l) -> l.getText().contains("Broadcast successful!"));
    }
}