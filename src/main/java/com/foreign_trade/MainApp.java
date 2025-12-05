package com.foreign_trade;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.io.IOException;

public class MainApp extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Foreign Trade License Management System");

        // --- STARTUP SCREEN ---
        // Launching Exporter Registration as the first required step.
        showExporterOverview(primaryStage);
    }

    // --- SCREEN LOADERS (Accepting a Stage for navigation) ---

    /** Loads and displays the Exporter Registration screen (ExporterView.fxml). */
    public void showExporterOverview(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/view/ExporterView.fxml"));
            AnchorPane exporterOverview = loader.load();

            stage.setScene(new Scene(exporterOverview));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not load ExporterView FXML.");
        }
    }

    /** Loads and displays the License Issuance screen (LicenseView.fxml). */
    public void showLicenseView(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/view/LicenseView.fxml"));
            AnchorPane licenseView = loader.load();

            stage.setScene(new Scene(licenseView));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not load LicenseView FXML.");
        }
    }

    /** Loads and displays the NEW Shipment Entry form (ShipmentEntryView.fxml).
     * NOTE: This method was accidentally deleted and needed to be restored. */
    public void showShipmentEntryView(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/view/ShipmentEntryView.fxml"));
            AnchorPane view = loader.load();
            stage.setScene(new Scene(view));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not load ShipmentEntryView FXML.");
        }
    }

    // Inside MainApp.java

    /** Loads and displays the NEW Shipment Log filtered table (ShipmentLogView.fxml). */
    public void showShipmentLogView(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader();

            // FIX: Use the class loader to find the resource, which often resolves path issues
            loader.setLocation(MainApp.class.getResource("/view/ShipmentLogView.fxml"));

            // Ensure this FXML file physically exists in src/main/resources/view/

            AnchorPane view = loader.load();
            stage.setScene(new Scene(view));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Print a specific error to confirm the failing file name
            System.err.println("CRITICAL: Failed to load FXML file: ShipmentLogView.fxml");
        }
    }

    /** Loads and displays the Chatbot screen (ChatbotView.fxml). */
    public void showChatbotView(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/view/ChatbotView.fxml"));
            AnchorPane chatbotView = loader.load();
            stage.setScene(new Scene(chatbotView));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not load ChatbotView FXML.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}