package com.foreign_trade.service;

import com.foreign_trade.dao.ShipmentDAO;
import com.foreign_trade.model.Shipment;
import java.sql.SQLException;
import java.util.List;

public class ShipmentService {

    private final ShipmentDAO shipmentDAO = new ShipmentDAO();

    // The custom exception class remains defined in its own file: InsuranceException.java

    /**
     * Retrieves all shipment records. (Used for initial display/testing).
     */
    public List<Shipment> getShipmentRecords() throws SQLException {
        return shipmentDAO.getAllShipments();
    }

    /**
     * Retrieves shipment records filtered by license ID. (Required for UI filtering).
     */
    public List<Shipment> getShipmentRecordsByLicenseId(int licenseId) throws SQLException {
        return shipmentDAO.getShipmentsByLicenseId(licenseId);
    }

    // NOTE: logNewShipment and prepareShipment were removed, as their functionality is now
    // inside ShipmentDAO.insertAndValidateShipment.
}