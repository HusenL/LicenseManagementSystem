package com.foreign_trade.service;

import com.foreign_trade.dao.LicenseDAO;
import com.foreign_trade.model.License;
import com.foreign_trade.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ChatbotService {

    private static final String SELECT_FAQ_BY_KEYWORD =
            "SELECT answer FROM FAQ WHERE question LIKE ?";

    private final LicenseDAO licenseDAO = new LicenseDAO();

    /**
     * Finds the best static answer based on user input using keyword matching.
     * @param userInput The question entered by the user.
     * @return The answer from the FAQ table, or a generic response if none is found.
     */
    public String getStaticFAQAnswer(String userInput) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        // Sanitize and prepare input for a LIKE search (case-insensitive and partial match)
        String keywordSearch = "%" + userInput.toLowerCase() + "%";

        try {
            connection = DbUtil.getConnection();
            // Note: This simple implementation matches the keyword against the whole question column
            statement = connection.prepareStatement(SELECT_FAQ_BY_KEYWORD);
            statement.setString(1, keywordSearch);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                // Return the first matching answer
                return resultSet.getString("answer");
            }

        } catch (SQLException e) {
            // Log the error but return a user-friendly message
            System.err.println("Chatbot DB error: " + e.getMessage());
        } finally {
            // Note: In a real app, DAOs should close their own resources. This is kept here for simplicity.
            DbUtil.closeConnection(connection);
            try {
                if (statement != null) statement.close();
                if (resultSet != null) resultSet.close();
            } catch (SQLException e) {
                // ignore
            }
        }

        return "I'm sorry, I couldn't find an answer to that specific question. Please try rephrasing.";
    }

    /**
     * Checks the validity of a license based on its number (live DB check).
     * @param licenseNumber The number provided by the user.
     * @return A status message regarding validity.
     */
    public String checkLicenseValidity(String licenseNumber) {
        try {
            License license = licenseDAO.getLicenseByNumber(licenseNumber);

            if (license == null) {
                return "Error: License number " + licenseNumber + " was not found in our records.";
            }

            if (license.isValid()) {
                return "Yes, your license (" + licenseNumber + ") is currently **VALID**." +
                        " It expires on: " + license.getExpiryDate();
            } else {
                return "No, your license (" + licenseNumber + ") **EXPIRED** on: " + license.getExpiryDate() +
                        ". Please apply for a renewal.";
            }
        } catch (SQLException e) {
            return "A database error occurred while checking your license status.";
        }
    }

    /**
     * Proactively checks for licenses expiring soon (The Reminder feature).
     * @param days The number of days to look ahead (e.g., 10 days).
     * @return A list of reminder messages.
     */
    public List<String> generateRenewalReminders(int days) {
        try {
            LocalDate now = LocalDate.now();
            List<License> expiringLicenses = licenseDAO.getLicensesExpiringInDays(days);

            return expiringLicenses.stream()
                    .map(license -> {
                        long remainingDays = java.time.temporal.ChronoUnit.DAYS.between(now, license.getExpiryDate());
                        return String.format(
                                "REMINDER: License %s (Exporter ID: %d) will expire in %d days (%s). Please renew immediately.",
                                license.getLicenseNumber(),
                                license.getExporterId(),
                                remainingDays,
                                license.getExpiryDate()
                        );
                    })
                    .collect(Collectors.toList());

        } catch (SQLException e) {
            System.err.println("Error generating renewal reminders: " + e.getMessage());
            // You will also need to add 'import java.util.Collections;' at the top of the file
            return java.util.Collections.singletonList("Automated reminder system failed to query database.");
        }
    }
}
