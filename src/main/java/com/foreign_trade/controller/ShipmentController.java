package com.foreign_trade.controller;

import com.foreign_trade.MainApp;
import com.foreign_trade.dao.LicenseDAO;
import com.foreign_trade.dao.ShipmentDAO;
import com.foreign_trade.model.License;
import com.foreign_trade.model.Shipment;
import com.foreign_trade.service.ShipmentService;
import com.foreign_trade.service.InsuranceException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate; // CRITICAL: Required for DatePicker and Model
import java.util.List;
import java.util.stream.Collectors;

public class ShipmentController {

    // --- FXML Components for Entry View ---
    @FXML private ComboBox<String> licenseIdComboBox;
    @FXML private TextField productNameField;
    @FXML private TextField quantityField;
    @FXML private TextField destinationField;
    @FXML private TextField costField;
    @FXML private DatePicker dateField; // The component that was causing the error
    @FXML private CheckBox hasInsuranceCheckbox;

    // --- FXML Components for Log View ---
    @FXML private TableView<Shipment> shipmentTable;
    @FXML private TableColumn<Shipment, Integer> idColumn;
    @FXML private TableColumn<Shipment, String> productColumn;
    @FXML private TableColumn<Shipment, String> destinationColumn;
    @FXML private TableColumn<Shipment, Shipment.ShipmentStatus> statusColumn;
    @FXML private TableColumn<Shipment, Double> costColumn;
    @FXML private TableColumn<Shipment, Boolean> insuranceColumn;
    @FXML private TableColumn<Shipment, LocalDate> dateColumn;

    // --- Backend Instances (Instantiated ONCE at class level) ---
    private final ShipmentService shipmentService = new ShipmentService();
    private final LicenseDAO licenseDAO = new LicenseDAO();
    private final ShipmentDAO shipmentDAO = new ShipmentDAO(); // Used for direct logging
    private ObservableList<Shipment> shipmentData = FXCollections.observableArrayList();


    @FXML
    private void initialize() {
        // This method runs for BOTH Entry and Log views. We check which one is active.

        if (shipmentTable != null) {
            // Logic for ShipmentLogView (Table View)
            initializeTableView();
            populateLicenseComboBox(); // Populates list, selects first, and calls filter
        }

        if (licenseIdComboBox != null && shipmentTable == null) {
            // Logic for ShipmentEntryView (Input Screen)
            populateLicenseComboBox();
        }

        // --- FIX: Explicitly initialize the DatePicker if it exists on the screen ---
        if (dateField != null) {
            dateField.setValue(LocalDate.now());
        }
    }

    private void initializeTableView() {
        // Only initializes columns if the TableView object exists on the screen
        idColumn.setCellValueFactory(new PropertyValueFactory<>("shipmentId"));
        productColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        destinationColumn.setCellValueFactory(new PropertyValueFactory<>("destination"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        costColumn.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        insuranceColumn.setCellValueFactory(new PropertyValueFactory<>("hasInsurance"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("exportDate"));

        shipmentTable.setItems(shipmentData);
    }

    private void populateLicenseComboBox() {
        try {
            List<License> licenses = licenseDAO.getAllLicenses();
            List<String> licenseNumbers = licenses.stream()
                    .map(License::getLicenseNumber)
                    .collect(Collectors.toList());
            licenseIdComboBox.getItems().addAll(licenseNumbers);

            // Only auto-select and load if the table view is present (ShipmentLogView)
            if (shipmentTable != null && !licenseNumbers.isEmpty()) {
                licenseIdComboBox.getSelectionModel().selectFirst();
                handleLicenseSelection();
            }

        } catch (SQLException e) {
            System.err.println("Failed to populate IEC ComboBox: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "DB Error", "Failed to Load Exporters", "Cannot load IEC list: " + e.getMessage());
        }
    }

    @FXML
    private void handleLicenseSelection() {
        String licenseNumber = licenseIdComboBox.getValue();

        if (shipmentTable != null) { // Only run filtering logic if on the Log View
            shipmentData.clear();
        }

        if (licenseNumber == null || licenseNumber.isEmpty()) {
            return;
        }

        if (shipmentTable != null) {
            try {
                License license = licenseDAO.getLicenseByNumber(licenseNumber);

                if (license != null) {
                    shipmentData.addAll(shipmentService.getShipmentRecordsByLicenseId(license.getLicenseId()));
                }

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "DB Error", "Failed to filter shipments.", e.getMessage());
            }
        }
    }


    @FXML
    private void handleLogShipmentButton() {
        String licenseNumber = licenseIdComboBox.getValue();

        if (licenseNumber == null || licenseNumber.isEmpty() || productNameField.getText().isEmpty() || quantityField.getText().isEmpty() || costField.getText().isEmpty() || destinationField.getText().isEmpty() || dateField.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Input Error", null, "Please fill in all shipment fields.");
            return;
        }

        try {
            double quantity = Double.parseDouble(quantityField.getText());
            double totalCost = Double.parseDouble(costField.getText());
            boolean hasInsurance = hasInsuranceCheckbox.isSelected();

            License license = licenseDAO.getLicenseByNumber(licenseNumber);
            int licenseId = (license != null) ? license.getLicenseId() : -1;

            if (licenseId == -1) {
                showAlert(Alert.AlertType.ERROR, "License Error", "License Not Found", "Could not retrieve ID for selected license number.");
                return;
            }

            Shipment newShipment = new Shipment(
                    licenseId,
                    productNameField.getText(),
                    "Origin Placeholder",
                    destinationField.getText(),
                    quantity,
                    totalCost,
                    dateField.getValue(),
                    hasInsurance
            );

            // Log and Validate Shipment
            int newId = shipmentDAO.insertAndValidateShipment(newShipment);

            if (hasInsurance) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Shipment Logged & Ready", "Shipment " + newId + " is ready for export.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Shipment Cancelled", "Insurance Required",
                        "Shipment " + newId + " was cancelled: No insurance found.");
            }

            // Optional: Auto-switch to the Log View after success
            // Stage stage = (Stage) licenseIdComboBox.getScene().getWindow();
            // new MainApp().showShipmentLogView(stage);

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", null, "Quantity/Cost must be valid numbers.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", "Failed to log shipment.", e.getMessage());
        }
    }


    // --- NAVIGATION HANDLERS ---

    private Stage getStage(Event event) {
        return (Stage) ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();
    }

    @FXML
    private void handleNavigateToExporter(ActionEvent event) { new MainApp().showExporterOverview(getStage(event)); }

    @FXML
    private void handleNavigateToLicense(ActionEvent event) { new MainApp().showLicenseView(getStage(event)); }

    @FXML
    private void handleNavigateToShipmentEntry(ActionEvent event) { new MainApp().showShipmentEntryView(getStage(event)); }

    @FXML
    private void handleNavigateToShipmentLog(ActionEvent event) { new MainApp().showShipmentLogView(getStage(event)); }

    @FXML
    private void handleNavigateToChatbot(ActionEvent event) { new MainApp().showChatbotView(getStage(event)); }

    // --- Utility Methods ---
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}