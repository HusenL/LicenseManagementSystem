package com.foreign_trade.controller;

import com.foreign_trade.MainApp;
import com.foreign_trade.dao.ExporterDAO;
import com.foreign_trade.model.License;
import com.foreign_trade.model.Exporter;
import com.foreign_trade.service.LicenseIssuanceService;
import com.foreign_trade.service.PdfGenerator;
import com.itextpdf.text.DocumentException;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class LicenseController {

    @FXML private ComboBox<String> iecNumberComboBox;
    @FXML private TextField expiryDaysField;
    @FXML private Label exporterFirmNameLabel;
    @FXML private Label newLicenseNumberLabel;

    // --- Backend Instances ---
    private final LicenseIssuanceService issuanceService = new LicenseIssuanceService();
    private final ExporterDAO exporterDAO = new ExporterDAO();
    private final PdfGenerator pdfGenerator = new PdfGenerator();

    @FXML
    private void initialize() {
        populateIecComboBox();

        // Listener to display the Firm Name when an IEC is selected
        iecNumberComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                displayExporterDetails(newVal);
            }
        });
    }

    private void populateIecComboBox() {
        try {
            List<Exporter> exporters = exporterDAO.getAllExporters();

            // Clear existing items just in case it was called before cleanup
            iecNumberComboBox.getItems().clear();

            // Map Exporter objects to a list of just IEC strings
            List<String> iecNumbers = exporters.stream()
                    .map(Exporter::getIecNumber)
                    .collect(Collectors.toList());

            // Re-populate the ComboBox
            iecNumberComboBox.getItems().addAll(iecNumbers);

            // Automatically select the first item if the list is not empty
            if (!iecNumbers.isEmpty()) {
                iecNumberComboBox.getSelectionModel().selectFirst();
            }

        } catch (SQLException e) {
            System.err.println("Failed to populate IEC ComboBox: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "DB Error", "Failed to Load Exporters", "Cannot load IEC list: " + e.getMessage());
        }
    }

    private void displayExporterDetails(String iec) {
        try {
            Exporter exporter = exporterDAO.getExporterByIec(iec);
            if (exporter != null) {
                exporterFirmNameLabel.setText("Firm: " + exporter.getFirmName() + " (" + exporter.getCountry() + ")");
            }
        } catch (SQLException e) {
            exporterFirmNameLabel.setText("Error loading firm details.");
        }
    }


    @FXML
    public void handleIssueLicenseButton() {
        String iecNumber = iecNumberComboBox.getValue();
        int expiryDays;

        if (iecNumber == null || iecNumber.isEmpty() || expiryDaysField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Missing", null, "Please select an IEC and enter the expiry period.");
            return;
        }

        try {
            expiryDays = Integer.parseInt(expiryDaysField.getText().trim());

            // 1. DB Issuance
            License newLicense = issuanceService.issueNewLicense(iecNumber, expiryDays);

            // 2. Fetch Firm Name for PDF
            Exporter exporter = exporterDAO.getExporterByIec(iecNumber);
            String firmName = (exporter != null) ? exporter.getFirmName() : "Unknown Firm";

            // 3. PDF Generation
            String filePath = pdfGenerator.generateLicensePdf(newLicense, firmName);

            // 4. Display Success Message
            newLicenseNumberLabel.setText("SUCCESS! License Number: " + newLicense.getLicenseNumber() + " (File saved to: " + filePath + ")");
            showAlert(Alert.AlertType.INFORMATION, "License Issued", "New license successfully created.", "PDF document saved to: " + filePath);

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", null, "Expiry period must be a valid number.");
        } catch (LicenseIssuanceService.PrerequisiteException e) {
            showAlert(Alert.AlertType.ERROR, "Prerequisite Failed", "License Rejected", e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Issuance Failed", "Database error: " + e.getMessage());
        } catch (DocumentException | IOException e) {
            // Catch PDF specific errors
            showAlert(Alert.AlertType.ERROR, "Document Error", "PDF Generation Failed", "Could not create document: " + e.getMessage());
        }
    }

    // --- NAVIGATION HANDLERS ---

    private Stage getStage(Event event) {
        return (Stage) ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();
    }

    @FXML
    public void handleNavigateToExporter(ActionEvent event) {
        new MainApp().showExporterOverview(getStage(event));
    }

    @FXML
    public void handleNavigateToLicense(ActionEvent event) {
        new MainApp().showLicenseView(getStage(event));
    }

    // --- FIX: ADDED MISSING METHOD ---
    @FXML
    public void handleNavigateToShipmentLog(ActionEvent event) {
        new MainApp().showShipmentLogView(getStage(event));
    }

    // Assuming this was the handler that was causing the error due to non-existence:
    // It should now call the correct log view method.
    @FXML
    public void handleNavigateToShipment(ActionEvent event) {
        new MainApp().showShipmentLogView(getStage(event));
    }

    @FXML
    public void handleNavigateToShipmentEntry(ActionEvent event) {
        new MainApp().showShipmentEntryView(getStage(event));
    }

    @FXML
    public void handleNavigateToChatbot(ActionEvent event) {
        new MainApp().showChatbotView(getStage(event));
    }

    // --- UTILITY METHODS ---

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}