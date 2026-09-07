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
 * Controller for the Admin Dashboard view (AdminDashboard.fxml).
 * Enforces strict administrative role authorization and provides
 * operational overview placeholders for facility and bay management.
 */
public class AdminDashboardController {

    private static final Logger logger = LoggerFactory.getLogger(AdminDashboardController.class);

    @FXML
    private Label lblWelcome;

    @FXML
    private Label lblAdminNameTop;

    @FXML
    private Label lblAdminEmailTop;

    @FXML
    private Label lblSessionStatus;

    @FXML
    private Button btnLogoutTop;

    private final AuthService authService;

    public AdminDashboardController() {
        this(ServiceRegistry.getAuthService());
    }

    public AdminDashboardController(AuthService authService) {
        if (authService == null) {
            throw new IllegalArgumentException("AuthService must not be null.");
        }
        this.authService = authService;
    }

    @FXML
    public void initialize() {
        AppSession session = AppSession.getInstance();

        // 1. Verify authentication
        if (!session.isLoggedIn() || session.getCurrentUser() == null) {
            logger.warn("AdminDashboard accessed without active authentication. Redirecting to Login.");
            Platform.runLater(() -> {
                Stage stage = NavigationUtil.getStage(lblWelcome);
                NavigationUtil.navigateToLogin(stage, "Authentication is required to continue.", true);
            });
            return;
        }

        // 2. Strict Role Verification: Must possess Role.ADMIN
        if (!session.isAdmin()) {
            logger.warn("Security violation: Customer ID {} attempted unauthorized access to Admin Dashboard.", 
                    session.getCurrentUser().getUserId());
            Platform.runLater(() -> {
                Stage stage = NavigationUtil.getStage(lblWelcome);
                NavigationUtil.setPendingMessage("Access Denied: You do not have permission to access the Admin Dashboard.", true);
                NavigationUtil.navigateToCustomerDashboard(stage);
            });
            return;
        }

        User adminUser = session.getCurrentUser();
        populateAdminData(adminUser);
    }

    private void populateAdminData(User admin) {
        if (lblWelcome != null) {
            lblWelcome.setText("Campus Facility Operations • " + admin.getFullName());
        }
        if (lblAdminNameTop != null) {
            lblAdminNameTop.setText(admin.getFullName());
        }
        if (lblAdminEmailTop != null) {
            lblAdminEmailTop.setText(admin.getEmail());
        }
        if (lblSessionStatus != null) {
            lblSessionStatus.setText("Verified Admin ID: " + admin.getUserId() + " (" + admin.getEmail() + ")");
        }
    }

    /**
     * Handles the Logout action.
     */
    @FXML
    public void handleLogout(ActionEvent event) {
        logger.info("Administrator initiated sign-out.");
        Stage stage = NavigationUtil.getStage(btnLogoutTop != null ? btnLogoutTop : lblWelcome);
        authService.logout();
        NavigationUtil.logout(stage);
    }
}
