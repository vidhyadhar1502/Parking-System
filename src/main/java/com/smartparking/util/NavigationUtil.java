package com.smartparking.util;

import com.smartparking.model.User;
import com.smartparking.model.enums.Role;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Reusable navigation engine managing JavaFX view transitions, stage reuse,
 * role-based route dispatching, and deterministic error handling.
 */
public final class NavigationUtil {

    private static final Logger logger = LoggerFactory.getLogger(NavigationUtil.class);

    public static final String FXML_LOGIN = "/fxml/Login.fxml";
    public static final String FXML_REGISTER = "/fxml/Register.fxml";
    public static final String FXML_CUSTOMER_DASHBOARD = "/fxml/CustomerDashboard.fxml";
    public static final String FXML_ADMIN_DASHBOARD = "/fxml/AdminDashboard.fxml";

    private static final int DEFAULT_WIDTH = 1100;
    private static final int DEFAULT_HEIGHT = 720;

    private static final AtomicBoolean isNavigating = new AtomicBoolean(false);
    private static volatile String pendingMessage;
    private static volatile boolean isPendingMessageError = false;

    private NavigationUtil() {
        // Prevent instantiation
    }

    /**
     * Retrieves any message queued for the destination screen (e.g. registration success)
     * and clears the queue atomically.
     */
    public static synchronized String getAndClearPendingMessage() {
        String msg = pendingMessage;
        pendingMessage = null;
        return msg;
    }

    /**
     * Checks if the queued pending message represents an error or info/success.
     */
    public static synchronized boolean isPendingMessageError() {
        return isPendingMessageError;
    }

    /**
     * Queues a message to be consumed by the next loaded controller.
     */
    public static synchronized void setPendingMessage(String message, boolean isError) {
        pendingMessage = message;
        isPendingMessageError = isError;
    }

    /**
     * Helper to extract Stage from any UI component/node.
     */
    public static Stage getStage(Node node) {
        if (node != null && node.getScene() != null) {
            return (Stage) node.getScene().getWindow();
        }
        return null;
    }

    /**
     * Switches the current stage's root to the specified FXML resource.
     * Guaranteed to never crash the JVM, freeze the UI, or leave a blank screen on failure.
     *
     * @param stage    the primary stage
     * @param fxmlPath classpath path to the target FXML file
     * @param title    optional window title (null to keep existing)
     * @return true if navigation successfully loaded and switched root, false otherwise
     */
    public static boolean navigateTo(Stage stage, String fxmlPath, String title) {
        if (stage == null) {
            logger.error("Navigation failed: target Stage is null for path {}", fxmlPath);
            return false;
        }

        // Prevent duplicate concurrent navigation requests (e.g. double clicks)
        if (!isNavigating.compareAndSet(false, true)) {
            logger.warn("Navigation to {} rejected: navigation already in progress.", fxmlPath);
            return false;
        }

        try {
            URL resourceUrl = NavigationUtil.class.getResource(fxmlPath);
            if (resourceUrl == null) {
                logger.error("Missing FXML resource: cannot locate '{}' on classpath.", fxmlPath);
                return false;
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();

            Scene scene = stage.getScene();
            if (scene == null) {
                scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
                URL cssUrl = NavigationUtil.class.getResource("/css/style.css");
                if (cssUrl != null) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                }
                stage.setScene(scene);
            } else {
                // Reuse scene and apply new root
                scene.setRoot(root);
            }

            if (title != null && !title.isEmpty()) {
                stage.setTitle(title);
            }

            logger.info("Navigation successfully completed to {}", fxmlPath);
            return true;
        } catch (Exception e) {
            logger.error("Failed to load and navigate to FXML '{}': {}", fxmlPath, e.getMessage(), e);
            return false;
        } finally {
            isNavigating.set(false);
        }
    }

    /**
     * Navigates to the Login screen with an optional flash message.
     */
    public static boolean navigateToLogin(Stage stage, String message, boolean isError) {
        if (message != null && !message.isEmpty()) {
            setPendingMessage(message, isError);
        }
        return navigateTo(stage, FXML_LOGIN, "Smart Parking • Login");
    }

    /**
     * Navigates to the Registration screen.
     */
    public static boolean navigateToRegister(Stage stage) {
        return navigateTo(stage, FXML_REGISTER, "Smart Parking • Create Account");
    }

    /**
     * Dispatches authenticated user to the appropriate dashboard based on their verified Role.
     * Enforces strict session and role validation.
     */
    public static boolean navigateToDashboard(Stage stage) {
        AppSession session = AppSession.getInstance();
        if (!session.isLoggedIn() || session.getCurrentUser() == null) {
            logger.warn("Unauthenticated dashboard access attempted. Redirecting to login.");
            return navigateToLogin(stage, "Authentication is required to continue.", true);
        }

        User user = session.getCurrentUser();
        Role role = user.getRole();

        if (role == Role.CUSTOMER) {
            return navigateToCustomerDashboard(stage);
        } else if (role == Role.ADMIN) {
            return navigateToAdminDashboard(stage);
        } else {
            logger.error("User ID {} possesses an invalid or unsupported role: {}", user.getUserId(), role);
            session.logout();
            return navigateToLogin(stage, "Your session is invalid. Please log in again.", true);
        }
    }

    /**
     * Navigates to the Customer Dashboard.
     * Verifies that the user is logged in.
     */
    public static boolean navigateToCustomerDashboard(Stage stage) {
        AppSession session = AppSession.getInstance();
        if (!session.isLoggedIn()) {
            logger.warn("Unauthorized Customer Dashboard access attempted.");
            return navigateToLogin(stage, "Authentication is required to continue.", true);
        }
        return navigateTo(stage, FXML_CUSTOMER_DASHBOARD, "Smart Parking • Customer Dashboard");
    }

    /**
     * Navigates to the Admin Dashboard.
     * Enforces authentication AND administrative authorization.
     */
    public static boolean navigateToAdminDashboard(Stage stage) {
        AppSession session = AppSession.getInstance();
        if (!session.isLoggedIn()) {
            logger.warn("Unauthorized Admin Dashboard access attempted (not logged in).");
            return navigateToLogin(stage, "Authentication is required to continue.", true);
        }

        if (!session.isAdmin()) {
            logger.warn("Customer user ID {} attempted unauthorized access to Admin Dashboard.", 
                    session.getCurrentUser().getUserId());
            setPendingMessage("You do not have permission to access the Admin Dashboard.", true);
            return navigateToCustomerDashboard(stage);
        }

        return navigateTo(stage, FXML_ADMIN_DASHBOARD, "Smart Parking • Admin Portal");
    }

    /**
     * Logs out the current user by clearing session state first, then returning to the login screen.
     */
    public static void logout(Stage stage) {
        logger.info("Logging out current user and clearing session state...");
        AppSession.getInstance().logout();
        boolean success = navigateToLogin(stage, "You have been logged out successfully.", false);
        if (!success) {
            logger.error("Logout completed, but Login page failed to render.");
        }
    }
}
