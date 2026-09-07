package com.smartparking.controller;

import com.smartparking.model.User;
import com.smartparking.service.AuthService;
import com.smartparking.service.ServiceException;
import com.smartparking.service.ServiceRegistry;
import com.smartparking.service.ValidationException;
import com.smartparking.util.NavigationUtil;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for the User Registration view (Register.fxml).
 * Handles account creation input validation, password confirmation matching,
 * and passes registration to {@link AuthService}.
 */
public class RegisterController {

    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);

    @FXML
    private TextField txtFullName;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtPhoneNumber;

    @FXML
    private TextField txtVehicleNumber;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private PasswordField txtConfirmPassword;

    @FXML
    private Button btnRegister;

    @FXML
    private Button btnBackToLogin;

    @FXML
    private Button btnBackToLoginTop;

    @FXML
    private Label lblMessage;

    @FXML
    private ProgressIndicator progressIndicator;

    private final AuthService authService;

    /**
     * Default constructor used by JavaFX FXMLLoader.
     */
    public RegisterController() {
        this(ServiceRegistry.getAuthService());
    }

    /**
     * Constructor injection for unit testing.
     *
     * @param authService the authentication service to use
     */
    public RegisterController(AuthService authService) {
        if (authService == null) {
            throw new IllegalArgumentException("AuthService must not be null.");
        }
        this.authService = authService;
    }

    @FXML
    public void initialize() {
        clearMessage();
    }

    /**
     * Handles account registration submission.
     */
    @FXML
    public void handleRegister(ActionEvent event) {
        clearMessage();

        String fullName = txtFullName.getText();
        String email = txtEmail.getText();
        String phoneNumber = txtPhoneNumber.getText();
        String vehicleNumber = txtVehicleNumber.getText();
        String password = txtPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

        // UI-level validation: Password confirmation match
        if (password == null || password.isEmpty()) {
            showMessage("Password is required.", true);
            txtPassword.requestFocus();
            return;
        }

        if (confirmPassword == null || !password.equals(confirmPassword)) {
            showMessage("Passwords do not match. Please verify and re-enter.", true);
            txtConfirmPassword.clear();
            txtConfirmPassword.requestFocus();
            return;
        }

        setProcessing(true);

        Task<User> registerTask = new Task<>() {
            @Override
            protected User call() {
                // Notice: confirmPassword is NEVER passed to the service/domain tier
                return authService.register(fullName, email, phoneNumber, password, vehicleNumber);
            }
        };

        registerTask.setOnSucceeded(e -> {
            setProcessing(false);
            User newUser = registerTask.getValue();
            logger.info("Customer account registered successfully for email '{}' (User ID: {})", 
                    newUser.getEmail(), newUser.getUserId());

            Stage stage = NavigationUtil.getStage(btnRegister);
            boolean navigated = NavigationUtil.navigateToLogin(
                    stage, 
                    "Account created successfully! Please sign in with your new credentials.", 
                    false
            );

            if (!navigated) {
                logger.error("Registration succeeded for user ID {}, but navigation back to Login failed.", 
                        newUser.getUserId());
                showMessage("Account created successfully, but the login page could not be opened. Please restart the application.", true);
            }
        });

        registerTask.setOnFailed(e -> {
            setProcessing(false);
            Throwable ex = registerTask.getException();

            // Clear password fields on failure for security
            txtPassword.clear();
            txtConfirmPassword.clear();

            if (ex instanceof ValidationException) {
                showMessage(ex.getMessage(), true);
            } else if (ex instanceof ServiceException) {
                logger.error("Service exception during user registration: {}", ex.getMessage(), ex);
                showMessage("Unable to complete registration. Please verify database connectivity.", true);
            } else {
                logger.error("Unexpected failure during registration: {}", ex.getMessage(), ex);
                showMessage("Something went wrong during registration. Please try again.", true);
            }
        });

        Thread worker = new Thread(registerTask, "smart-parking-register-worker");
        worker.setDaemon(true);
        worker.start();
    }

    /**
     * Navigates back to the Login view.
     */
    @FXML
    public void handleBackToLogin(ActionEvent event) {
        Stage stage = NavigationUtil.getStage(btnBackToLogin != null ? btnBackToLogin : btnBackToLoginTop);
        boolean success = NavigationUtil.navigateToLogin(stage, null, false);
        if (!success) {
            showMessage("Unable to open the login page.", true);
        }
    }

    private void setProcessing(boolean processing) {
        btnRegister.setDisable(processing);
        if (btnBackToLogin != null) btnBackToLogin.setDisable(processing);
        if (btnBackToLoginTop != null) btnBackToLoginTop.setDisable(processing);
        if (progressIndicator != null) {
            progressIndicator.setVisible(processing);
        }
    }

    private void showMessage(String message, boolean isError) {
        if (lblMessage != null) {
            lblMessage.setText(message);
            lblMessage.getStyleClass().removeAll("message-error", "message-success");
            lblMessage.getStyleClass().add(isError ? "message-error" : "message-success");
            lblMessage.setVisible(true);
        }
    }

    private void clearMessage() {
        if (lblMessage != null) {
            lblMessage.setText("");
            lblMessage.setVisible(false);
        }
    }

    // Accessors for unit testing
    public TextField getTxtFullName() {
        return txtFullName;
    }

    public TextField getTxtEmail() {
        return txtEmail;
    }

    public TextField getTxtPhoneNumber() {
        return txtPhoneNumber;
    }

    public TextField getTxtVehicleNumber() {
        return txtVehicleNumber;
    }

    public PasswordField getTxtPassword() {
        return txtPassword;
    }

    public PasswordField getTxtConfirmPassword() {
        return txtConfirmPassword;
    }

    public Button getBtnRegister() {
        return btnRegister;
    }

    public Label getLblMessage() {
        return lblMessage;
    }
}
