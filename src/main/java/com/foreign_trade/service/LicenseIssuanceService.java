package com.foreign_trade.service;

import com.foreign_trade.dao.ExporterDAO;
import com.foreign_trade.dao.LicenseDAO;
import com.foreign_trade.model.Exporter;
import com.foreign_trade.model.License;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Random;

public class LicenseIssuanceService {

    private final ExporterDAO exporterDAO = new ExporterDAO();
    private final LicenseDAO licenseDAO = new LicenseDAO();
    private final Random random = new Random();

    // Custom Exception for business rule failure
    public static class PrerequisiteException extends Exception {
        public PrerequisiteException(String message) {
            super(message);
        }
    }

    /**
     * Validates prerequisites and issues a new license.
     * 1. Checks if the Exporter exists via IEC.
     * 2. Generates a unique license number.
     * 3. Inserts the new license into the database.
     * * @param iecNumber The IEC number of the exporter requesting the license.
     * @param expiryPeriodDays The duration of the license validity.
     * @return The newly created License object.
     * @throws SQLException If a database error occurs.
     * @throws PrerequisiteException If the Exporter is not registered (IEC missing).
     */
    public License issueNewLicense(String iecNumber, int expiryPeriodDays)
            throws SQLException, PrerequisiteException {

        // --- 1. Check IEC Prerequisite ---
        Exporter exporter = exporterDAO.getExporterByIec(iecNumber);
        if (exporter == null) {
            throw new PrerequisiteException("License application rejected: Exporter with IEC " + iecNumber + " is not registered.");
        }

        // --- 2. Prepare License Details ---
        int exporterId = exporter.getExporterId();
        LocalDate issueDate = LocalDate.now();
        LocalDate expiryDate = issueDate.plusDays(expiryPeriodDays);
        String licenseNumber = generateUniqueLicenseNumber(exporter.getCountry());
        String signatureUrl = "/signatures/" + exporterId + ".png"; // Placeholder signature path

        // --- 3. Create and Insert License ---
        License newLicense = new License(
                exporterId,
                licenseNumber,
                issueDate,
                expiryDate,
                signatureUrl
        );

        int id = licenseDAO.insertLicense(newLicense);
        newLicense.setLicenseId(id); // Update POJO with generated ID

        return newLicense;
    }

    /**
     * Generates a unique, country-prefixed license number (simple implementation).
     */
    private String generateUniqueLicenseNumber(String country) {
        String prefix = country.length() >= 3 ? country.substring(0, 3).toUpperCase() : "GEN";
        int uniqueNum = 10000 + random.nextInt(90000);
        return prefix + "-" + LocalDate.now().getYear() + "-" + uniqueNum;
    }
}
