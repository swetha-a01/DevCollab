package com.devcollab.ui;

import javafx.stage.Stage;

import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

public class MainAppUITest extends ApplicationTest {

    private MainApp registrationWindow;

    @Override
    public void start(Stage stage) throws Exception {
        // Launches our modular registration window inside the headless test context
        registrationWindow = new MainApp();
        registrationWindow.showRegistrationWindow(stage);
    }

    @Test
    public void testInvalidRegistrationTriggersUIErrorMessage() {
        // 1. Robot clicks directly on the ID text box using its unique CSS ID selector and types an invalid ID
        clickOn("#idField").write("bad_format_id");

        // 2. Robot targets the Name text box using its CSS ID hook and types input
        clickOn("#nameField").write("Automated Robot");

        // 3. Robot targets the Email text box using its CSS ID hook and types a standard personal email
        clickOn("#emailField").write("robot@gmail.com");

        // 4. Robot clicks the submit button via its structural ID hook
        clickOn("#registerButton");

        // 5. Assertion verification check: Verify the status label matching our validation error string
        verifyThat("#statusLabel", hasText("❌ Invalid ID! Use format like 24BCA0002"));
    }
}