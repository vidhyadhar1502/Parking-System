package com.smartparking.controller;

import com.smartparking.model.User;
import com.smartparking.service.AuthService;
import com.smartparking.service.ServiceRegistry;
import com.smartparking.util.AppSession;
import com.smartparking.util.NavigationUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for the Customer Dashboard view (CustomerDashboard.fxml).
 * Provides the driver-facing landing portal with session verification,
 * user details display, and navigation placeholders for upcoming phases.
 */
public class CustomerDashboardController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerDashboardController.class);

    @FXML
    private Label lblWelcome;

    @FXML
    private Label lblUserNameTop;

    @FXML
    private Label lblUserEmailTop;

    @FXML
    private Label lblVehicleNumber;

    @FXML
    private Label lblPhoneNumber;

    @FXML
    private Label lblSessionStatus;

    @FXML
    private Label lblMessage;

    @FXML
    private Button btnLogoutTop;

    private final AuthService authService;

    public CustomerDashboardController() {
        this(ServiceRegistry.getAuthService());
    }

    public CustomerDashboardController(AuthService authService) {
        if (authService == null) {
            throw new IllegalArgumentException("AuthService must not be null.");
        }
        this.authService = authService;
    }

    @FXML
    public void initialize() {
        // Enforce session protection
        AppSession session = AppSession.getInstance();
        if (!session.isLoggedIn() || session.getCurrentUser() == null) {
            logger.warn("CustomerDashboard accessed without valid authentication. Enforcing redirect to Login.");
            Platform.runLater(() -> {
                Stage stage = NavigationUtil.getStage(lblWelcome);
                NavigationUtil.navigateToLogin(stage, "Authentication is required to continue.", true);
            });
            return;
        }

        // If an administrator accesses this screen, redirect to Admin Portal
        if (session.isAdmin()) {
            logger.info("Administrator user redirected from Customer Dashboard to Admin Dashboard.");
            Platform.runLater(() -> {
                Stage stage = NavigationUtil.getStage(lblWelcome);
                NavigationUtil.navigateToAdminDashboard(stage);
            });
            return;
        }

        User user = session.getCurrentUser();
        populateUserData(user);

        // Check for any flash message
        String pending = NavigationUtil.getAndClearPendingMessage();
        if (pending != null && !pending.trim().isEmpty()) {
            if (lblMessage != null) {
                lblMessage.setText(pending);
                lblMessage.setVisible(true);
            }
        }
    }

    private void populateUserData(User user) {
        if (lblWelcome != null) {
            lblWelcome.setText("Welcome back, " + user.getFullName());
        }
        if (lblUserNameTop != null) {
            lblUserNameTop.setText(user.getFullName());
        }
        if (lblUserEmailTop != null) {
            lblUserEmailTop.setText(user.getEmail());
        }
        if (lblVehicleNumber != null) {
            lblVehicleNumber.setText(user.getVehicleNumber() != null ? user.getVehicleNumber() : "None Registered");
        }
        if (lblPhoneNumber != null) {
            lblPhoneNumber.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "—");
        }
        if (lblSessionStatus != null) {
            lblSessionStatus.setText("Logged in as Customer (ID: " + user.getUserId() + ")");
        }
    }

    /**
     * Handles the Logout action.
     */
    @FXML
    public void handleLogout(ActionEvent event) {
        logger.info("Customer user initiated sign-out.");
        Stage stage = NavigationUtil.getStage(btnLogoutTop != null ? btnLogoutTop : lblWelcome);
        authService.logout();
        NavigationUtil.logout(stage);
    }
}
