package com.foreign_trade.dao;

import com.foreign_trade.model.Shipment;
import com.foreign_trade.model.Shipment.ShipmentStatus;
import com.foreign_trade.util.DbUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ShipmentDAO {

    private static final String SELECT_BY_LICENSE_ID = "SELECT * FROM Shipment WHERE license_id = ?";
    private static final String SELECT_ALL = "SELECT * FROM Shipment";

    // NOTE: This method is used by the Controller as the final, simplified operation.
    public int insertAndValidateShipment(Shipment shipment) throws SQLException {
        Connection connection = null;
        PreparedStatement insertStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet generatedKeys = null;
        int id = -1;

        try {
            connection = DbUtil.getConnection();
            connection.setAutoCommit(false); // Start transaction

            // 1. INSERT Shipment
            String INSERT_SQL = "INSERT INTO Shipment (license_id, product_name, origin, destinationCountry, quantity, totalCost, exportDate, status, has_insurance) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            insertStmt = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);

            String initialStatus = shipment.isHasInsurance() ?
                    Shipment.ShipmentStatus.PENDING.name() :
                    Shipment.ShipmentStatus.CANCELLED.name();

            // Set parameters:
            insertStmt.setInt(1, shipment.getLicenseId());
            insertStmt.setString(2, shipment.getProductName());
            insertStmt.setString(3, shipment.getOrigin());
            insertStmt.setString(4, shipment.getDestination());
            insertStmt.setDouble(5, shipment.getQuantity());
            insertStmt.setDouble(6, shipment.getTotalCost());

            // Safe handling for inserting LocalDate (can be null in Java if not provided, but we require a date in our entry form)
            insertStmt.setDate(7, shipment.getExportDate() != null ? Date.valueOf(shipment.getExportDate()) : null);

            insertStmt.setString(8, initialStatus);
            insertStmt.setBoolean(9, shipment.isHasInsurance());

            insertStmt.executeUpdate();
            generatedKeys = insertStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                id = generatedKeys.getInt(1);
                shipment.setShipmentId(id);
            }

            // 2. IMMEDIATE VALIDATION (Update status to READY_TO_SHIP only if insured)
            if (shipment.isHasInsurance()) {
                String UPDATE_SQL = "UPDATE Shipment SET status = ? WHERE shipment_id = ?";
                updateStmt = connection.prepareStatement(UPDATE_SQL);
                updateStmt.setString(1, Shipment.ShipmentStatus.READY_TO_SHIP.name());
                updateStmt.setInt(2, id);
                updateStmt.executeUpdate();
            }

            connection.commit(); // Commit transaction
        } catch (SQLException e) {
            if (connection != null) connection.rollback();
            throw e;
        } finally {
            if (connection != null) connection.setAutoCommit(true);
            // Closing logic remains here
        }
        return id;
    }

    // --- R: Read All Shipments (Universal Load for Initial Display/Testing) ---
    public List<Shipment> getAllShipments() throws SQLException {
        List<Shipment> shipments = new ArrayList<>();
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DbUtil.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(SELECT_ALL);

            while (resultSet.next()) {
                Date sqlDate = resultSet.getDate("exportDate");
                LocalDate exportDate = (sqlDate != null) ? sqlDate.toLocalDate() : null; // NULL-SAFE CHECK HERE

                shipments.add(new Shipment(
                        resultSet.getInt("shipment_id"),
                        resultSet.getInt("license_id"),
                        resultSet.getString("product_name"),
                        resultSet.getString("origin"),
                        resultSet.getString("destinationCountry"),
                        resultSet.getDouble("quantity"),
                        resultSet.getDouble("totalCost"),
                        exportDate, // Use the NULL-SAFE variable
                        Shipment.ShipmentStatus.valueOf(resultSet.getString("status")),
                        resultSet.getBoolean("has_insurance")
                ));
            }
        } finally {
            DbUtil.closeConnection(connection);
            if (statement != null) statement.close();
            if (resultSet != null) resultSet.close();
        }
        return shipments;
    }

    // --- R: Read Shipments by License ID (For Filtering) ---
    public List<Shipment> getShipmentsByLicenseId(int licenseId) throws SQLException {
        List<Shipment> shipments = new ArrayList<>();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DbUtil.getConnection();
            statement = connection.prepareStatement(SELECT_BY_LICENSE_ID);
            statement.setInt(1, licenseId);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Date sqlDate = resultSet.getDate("exportDate");
                LocalDate exportDate = (sqlDate != null) ? sqlDate.toLocalDate() : null; // NULL-SAFE CHECK HERE

                shipments.add(new Shipment(
                        resultSet.getInt("shipment_id"),
                        resultSet.getInt("license_id"),
                        resultSet.getString("product_name"),
                        resultSet.getString("origin"),
                        resultSet.getString("destinationCountry"),
                        resultSet.getDouble("quantity"),
                        resultSet.getDouble("totalCost"),
                        exportDate, // Use the NULL-SAFE variable
                        Shipment.ShipmentStatus.valueOf(resultSet.getString("status")),
                        resultSet.getBoolean("has_insurance")
                ));
            }
        } finally {
            DbUtil.closeConnection(connection);
            if (statement != null) statement.close();
            if (resultSet != null) resultSet.close();
        }
        return shipments;
    }
}