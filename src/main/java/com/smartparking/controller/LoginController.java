package com.smartparking.controller;

import com.smartparking.model.User;
import com.smartparking.service.AuthService;
import com.smartparking.service.AuthenticationException;
import com.smartparking.service.ServiceException;
import com.smartparking.service.ServiceRegistry;
import com.smartparking.service.ValidationException;
import com.smartparking.util.NavigationUtil;
import javafx.application.Platform;
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
 * Controller for the Login view (Login.fxml).
 * Connects user interface events to the business layer via {@link AuthService}.
 * Performs asynchronous credential submission without blocking the JavaFX UI thread.
 */
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Button btnLogin;

    @FXML
    private Button btnRegister;

    @FXML
    private Label lblMessage;

    @FXML
    private ProgressIndicator progressIndicator;

    private final AuthService authService;

    /**
     * Default constructor used by JavaFX FXMLLoader.
     * Injects the shared {@link AuthService} from {@link ServiceRegistry}.
     */
    public LoginController() {
        this(ServiceRegistry.getAuthService());
    }

    /**
     * Constructor injection for unit testing.
     *
     * @param authService the authentication service to use
     */
    public LoginController(AuthService authService) {
        if (authService == null) {
            throw new IllegalArgumentException("AuthService must not be null.");
        }
        this.authService = authService;
    }

    @FXML
    public void initialize() {
        // Display any pending message passed from prior view (e.g. registration success or logout)
        String pending = NavigationUtil.getAndClearPendingMessage();
        if (pending != null && !pending.trim().isEmpty()) {
            boolean isError = NavigationUtil.isPendingMessageError();
            showMessage(pending, isError);
        } else {
            clearMessage();
        }
    }

    /**
     * Handles the Login action when the user clicks LOGIN or presses ENTER.
     */
    @FXML
    public void handleLogin(ActionEvent event) {
        clearMessage();

        String email = txtEmail.getText();
        String password = txtPassword.getText();

        if (email == null || email.trim().isEmpty()) {
            showMessage("Please enter your email address.", true);
            txtEmail.requestFocus();
            return;
        }

        if (password == null || password.isEmpty()) {
            showMessage("Please enter your password.", true);
            txtPassword.requestFocus();
            return;
        }

        // Disable button and show indicator
        setProcessing(true);

        Task<User> loginTask = new Task<>() {
            @Override
            protected User call() {
                return authService.login(email, password);
            }
        };

        loginTask.setOnSucceeded(e -> {
            setProcessing(false);
            User authenticatedUser = loginTask.getValue();
            logger.info("User {} successfully authenticated.", authenticatedUser.getEmail());

            Stage stage = NavigationUtil.getStage(btnLogin);
            boolean navigated = NavigationUtil.navigateToDashboard(stage);

            if (!navigated) {
                logger.error("Navigation to dashboard failed after successful authentication for user ID {}", 
                        authenticatedUser.getUserId());
                showMessage("Login succeeded, but the dashboard could not be loaded. Please try again.", true);
            }
        });

        loginTask.setOnFailed(e -> {
            setProcessing(false);
            Throwable ex = loginTask.getException();

            if (ex instanceof AuthenticationException) {
                showMessage(ex.getMessage(), true);
            } else if (ex instanceof ValidationException) {
                showMessage(ex.getMessage(), true);
            } else if (ex instanceof ServiceException) {
                logger.error("Database connection failure during login: {}", ex.getMessage(), ex);
                showMessage("Unable to complete the request. Please verify database connectivity.", true);
            } else {
                logger.error("Unexpected error during login: {}", ex.getMessage(), ex);
                showMessage("Something went wrong while logging in. Please try again.", true);
            }
        });

        Thread worker = new Thread(loginTask, "smart-parking-login-worker");
        worker.setDaemon(true);
        worker.start();
    }

    /**
     * Navigates to the user registration screen.
     */
    @FXML
    public void handleGoToRegister(ActionEvent event) {
        Stage stage = NavigationUtil.getStage(btnRegister);
        boolean success = NavigationUtil.navigateToRegister(stage);
        if (!success) {
            showMessage("Unable to open the registration page.", true);
        }
    }

    private void setProcessing(boolean processing) {
        btnLogin.setDisable(processing);
        btnRegister.setDisable(processing);
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
    public TextField getTxtEmail() {
        return txtEmail;
    }

    public PasswordField getTxtPassword() {
        return txtPassword;
    }

    public Button getBtnLogin() {
        return btnLogin;
    }

    public Label getLblMessage() {
        return lblMessage;
    }
}
