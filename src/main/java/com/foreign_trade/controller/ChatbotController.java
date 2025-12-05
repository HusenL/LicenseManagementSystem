package com.foreign_trade.controller;

import com.foreign_trade.MainApp;
import com.foreign_trade.service.ChatbotService;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.MenuItem;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChatbotController {

    @FXML private TextField userInputField;
    @FXML private TextArea chatDisplayArea;

    private final ChatbotService chatbotService = new ChatbotService();

    // --- NAVIGATION HANDLERS (ALL PUBLIC) ---

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

    // FIX ADDED: The handler for the Shipment Log View menu item
    @FXML
    public void handleNavigateToShipmentLog(ActionEvent event) {
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

    // --- CORE LOGIC ---

    @FXML
    public void handleUserInput() {
        String userInput = userInputField.getText().trim();
        if (userInput.isEmpty()) {
            return;
        }

        appendChat("You: " + userInput + "\n");
        userInputField.clear();

        String response;
        String lowerInput = userInput.toLowerCase();

        try {
            if (lowerInput.contains("status of") || lowerInput.contains("valid")) {
                response = handleLicenseValidityCheck(lowerInput);
            } else if (lowerInput.contains("expiring") || lowerInput.contains("reminders")) {
                response = handleReminderCheck();
            } else {
                response = chatbotService.getStaticFAQAnswer(userInput);
            }
        } catch (SQLException e) {
            response = "BOT: Automated reminder system failed to query database due to a connection error.";
            System.err.println("Chatbot DB error: " + e.getMessage());
        } catch (Exception e) {
            response = "BOT ERROR: Sorry, a system error occurred. Please try again.";
            System.err.println("Chatbot runtime error: " + e.getMessage());
        }

        appendChat("BOT: " + response + "\n\n");
    }

    // --- LOGIC METHODS ---

    private String handleLicenseValidityCheck(String userInput) {
        String licenseNumber = extractLicenseNumber(userInput);
        if (licenseNumber.isEmpty()) {
            return "Please provide the exact license number you want to check.";
        }
        return chatbotService.checkLicenseValidity(licenseNumber);
    }

    private String handleReminderCheck() throws SQLException {
        LocalDate now = LocalDate.now();
        List<String> reminders = chatbotService.generateRenewalReminders(30);

        if (reminders.isEmpty()) {
            return "No licenses are currently expiring within the next 30 days.";
        }

        return "FOUND EXPIRING LICENSES:\n" + reminders.stream().collect(Collectors.joining("\n"));
    }

    // --- UTILITIES ---
    private String extractLicenseNumber(String input) {
        Pattern pattern = Pattern.compile("[A-Z]{2,4}-\\d{4}-\\d{4,5}");
        Matcher matcher = pattern.matcher(input.toUpperCase());

        if (matcher.find()) {
            return matcher.group(0);
        }
        return "";
    }

    private void appendChat(String message) {
        chatDisplayArea.appendText(message);
    }
}