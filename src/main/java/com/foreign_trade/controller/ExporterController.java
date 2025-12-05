package com.foreign_trade.controller;

import com.foreign_trade.MainApp;
import com.foreign_trade.dao.ExporterDAO;
import com.foreign_trade.model.Exporter;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.stage.Stage;

import java.sql.SQLException;
import javafx.scene.Node; // Required for safely getting the Stage

public class ExporterController {

    // --- FXML UI Components ---
    @FXML
    private TextField firmNameField;
    @FXML
    private TextField iecNumberField;
    @FXML
    private TextField contactPersonField;
    @FXML
    private TextField countryField;

    private final ExporterDAO exporterDAO = new ExporterDAO();

    // --- CORE LOGIC (Changed to public) ---

    @FXML
    public void handleRegisterButton() { // <-- CHANGED TO PUBLIC
        if (isInputValid()) {
            String firmName = firmNameField.getText();
            String iecNumber = iecNumberField.getText();
            String contactPerson = contactPersonField.getText();
            String country = countryField.getText();

            Exporter newExporter = new Exporter(firmName, iecNumber, contactPerson, country);

            try {
                exporterDAO.insertExporter(newExporter);

                // 1. Show simple success alert
                showAlert(Alert.AlertType.INFORMATION, "Success",
                        "Exporter Registered!",
                        "Firm " + firmName + " registered successfully.");

                clearFields();

                // 2. Safely get the Stage for navigation prompt
                Stage mainStage = (Stage) firmNameField.getScene().getWindow();
                showNavigationOptions(mainStage);

            } catch (SQLException e) {
                String errorMessage = e.getMessage().contains("Duplicate entry")
                        ? "IEC Number already registered."
                        : "Database error occurred.";

                showAlert(Alert.AlertType.ERROR, "Registration Failed",
                        errorMessage,
                        "Error details: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Unexpected Error During Registration: " + e.getMessage());
            }
        }
    }

    // --- NAVIGATION DIALOG ---

    private void showNavigationOptions(Stage mainStage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exporter Registered");
        alert.setHeaderText("Exporter successfully saved to database.");
        alert.setContentText("What would you like to do next?");

        ButtonType issueLicenseButton = new ButtonType("Issue License", ButtonBar.ButtonData.YES);
        ButtonType stayOnPageButton = new ButtonType("Stay on Page", ButtonBar.ButtonData.NO);
        ButtonType closeButton = new ButtonType("Close App", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(issueLicenseButton, stayOnPageButton, closeButton);

        alert.showAndWait().ifPresent(button -> {
            if (button == issueLicenseButton) {
                new MainApp().showLicenseView(mainStage);
            }
        });
    }

    // --- NAVIGATION HANDLERS (Menu Bar - ALL PUBLIC) ---

    private Stage getStage(Event event) {
        // Helper method to safely extract the Stage from a MenuItem event
        return (Stage) ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();
    }

    @FXML
    public void handleNavigateToExporter(ActionEvent event) { // <-- CHANGED TO PUBLIC
        new MainApp().showExporterOverview(getStage(event));
    }

    @FXML
    public void handleNavigateToLicense(ActionEvent event) { // <-- CHANGED TO PUBLIC
        new MainApp().showLicenseView(getStage(event));
    }

    @FXML
    public void handleNavigateToShipmentEntry(ActionEvent event) { // <-- CHANGED TO PUBLIC
        new MainApp().showShipmentEntryView(getStage(event));
    }

    @FXML
    public void handleNavigateToShipmentLog(ActionEvent event) { // <-- CHANGED TO PUBLIC
        new MainApp().showShipmentLogView(getStage(event));
    }

    @FXML
    public void handleNavigateToChatbot(ActionEvent event) { // <-- CHANGED TO PUBLIC
        new MainApp().showChatbotView(getStage(event));
    }

    // --- Utility Methods ---

    private boolean isInputValid() {
        if (firmNameField.getText().isEmpty() || iecNumberField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error",
                    "Missing Data",
                    "Firm Name and IEC Number are required fields.");
            return false;
        }
        return true;
    }

    private void clearFields() {
        firmNameField.clear();
        iecNumberField.clear();
        contactPersonField.clear();
        countryField.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}