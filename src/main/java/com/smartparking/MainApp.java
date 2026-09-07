package com.smartparking;

import com.smartparking.config.DatabaseConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * Main application entry point for the Smart Parking Management System (JavaFX 21).
 */
public class MainApp extends Application {

    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);
    private static final String APP_TITLE = "Smart Parking Management System";
    private static final int DEFAULT_WIDTH = 1100;
    private static final int DEFAULT_HEIGHT = 720;

    @Override
    public void init() throws Exception {
        super.init();
        logger.info("Initializing Smart Parking Application Foundation (Phase 1)...");
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Starting JavaFX Stage...");
            URL fxmlUrl = getClass().getResource("/fxml/main.fxml");
            
            Parent root;
            if (fxmlUrl != null) {
                root = FXMLLoader.load(fxmlUrl);
            } else {
                logger.warn("main.fxml not found on classpath, launching fallback container.");
                javafx.scene.layout.StackPane fallback = new javafx.scene.layout.StackPane();
                javafx.scene.control.Label lbl = new javafx.scene.control.Label(
                        "Smart Parking Management System - JavaFX Core Initialized"
                );
                fallback.getChildren().add(lbl);
                root = fallback;
            }

            Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);

            URL cssUrl = getClass().getResource("/css/style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            primaryStage.setTitle(APP_TITLE);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(600);
            primaryStage.show();

            logger.info("Application stage displayed successfully.");
        } catch (Exception e) {
            logger.error("Failed to launch JavaFX UI: {}", e.getMessage(), e);
        }
    }

    @Override
    public void stop() throws Exception {
        logger.info("Stopping application and closing database connections...");
        DatabaseConfig.shutdown();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
