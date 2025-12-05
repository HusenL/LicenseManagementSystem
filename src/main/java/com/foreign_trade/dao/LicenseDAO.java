package com.foreign_trade.dao;

import com.foreign_trade.model.License;
import com.foreign_trade.util.DbUtil;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LicenseDAO {

    private static final String INSERT_LICENSE = "INSERT INTO License (exporter_id, license_number, issue_date, expiry_date, signature_url) VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_BY_NUMBER = "SELECT * FROM License WHERE license_number = ?";
    private static final String SELECT_EXPIRING = "SELECT * FROM License WHERE expiry_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL ? DAY)";
    private static final String SELECT_ALL_LICENSES = "SELECT * FROM License"; // The new query constant

    // --- C: Create (Insert) ---
    public int insertLicense(License license) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;
        int id = -1;

        try {
            connection = DbUtil.getConnection();
            statement = connection.prepareStatement(INSERT_LICENSE, Statement.RETURN_GENERATED_KEYS);

            statement.setInt(1, license.getExporterId());
            statement.setString(2, license.getLicenseNumber());
            // Convert LocalDate to java.sql.Date for JDBC
            statement.setDate(3, Date.valueOf(license.getIssueDate()));
            statement.setDate(4, Date.valueOf(license.getExpiryDate()));
            statement.setString(5, license.getSignatureUrl());

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    id = generatedKeys.getInt(1);
                    license.setLicenseId(id);
                }
            }
        } finally {
            DbUtil.closeConnection(connection);
            if (statement != null) statement.close();
            if (generatedKeys != null) generatedKeys.close();
        }
        return id;
    }

    // --- R: Read (Retrieve by License Number) ---
    public License getLicenseByNumber(String licenseNumber) throws SQLException {
        License license = null;
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DbUtil.getConnection();
            statement = connection.prepareStatement(SELECT_BY_NUMBER);
            statement.setString(1, licenseNumber);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                license = new License(
                        resultSet.getInt("license_id"),
                        resultSet.getInt("exporter_id"),
                        resultSet.getString("license_number"),
                        resultSet.getDate("issue_date").toLocalDate(),
                        resultSet.getDate("expiry_date").toLocalDate(),
                        resultSet.getString("signature_url")
                );
            }
        } finally {
            DbUtil.closeConnection(connection);
            if (statement != null) statement.close();
            if (resultSet != null) resultSet.close();
        }
        return license;
    }

    // --- R: Read All (New method for UI ComboBox population) ---
    /**
     * Retrieves a list of all licenses from the database.
     */
    public List<License> getAllLicenses() throws SQLException {
        List<License> licenses = new ArrayList<>();
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DbUtil.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(SELECT_ALL_LICENSES); // Uses the new constant

            while (resultSet.next()) {
                licenses.add(new License(
                        resultSet.getInt("license_id"),
                        resultSet.getInt("exporter_id"),
                        resultSet.getString("license_number"),
                        resultSet.getDate("issue_date").toLocalDate(),
                        resultSet.getDate("expiry_date").toLocalDate(),
                        resultSet.getString("signature_url")
                ));
            }
        } finally {
            // Ensure resources are closed
            DbUtil.closeConnection(connection);
            if (statement != null) statement.close();
            if (resultSet != null) resultSet.close();
        }
        return licenses;
    }


    // --- Utility: Get Licenses Expiring Soon (for Chatbot Reminder) ---
    public List<License> getLicensesExpiringInDays(int days) throws SQLException {
        List<License> expiringLicenses = new ArrayList<>();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DbUtil.getConnection();
            statement = connection.prepareStatement(SELECT_EXPIRING);
            statement.setInt(1, days);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                expiringLicenses.add(new License(
                        resultSet.getInt("license_id"),
                        resultSet.getInt("exporter_id"),
                        resultSet.getString("license_number"),
                        resultSet.getDate("issue_date").toLocalDate(),
                        resultSet.getDate("expiry_date").toLocalDate(),
                        resultSet.getString("signature_url")
                ));
            }
        } finally {
            DbUtil.closeConnection(connection);
            if (statement != null) statement.close();
            if (resultSet != null) resultSet.close();
        }
        return expiringLicenses;
    }
}