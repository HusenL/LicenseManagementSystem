package com.foreign_trade.model;

import java.time.LocalDate;

public class Shipment {

    public enum ShipmentStatus {
        PENDING, READY_TO_SHIP, SHIPPED, CLEARED, CANCELLED
    }

    private int shipmentId;
    private int licenseId;
    private String productName;
    private String origin;
    private String destination; // Mapped from destinationCountry in DB
    private double quantity;
    private double totalCost;
    private LocalDate exportDate;
    private ShipmentStatus status;
    private boolean hasInsurance;

    // --- Full Constructor (Used when retrieving from DB) ---
    public Shipment(int shipmentId, int licenseId, String productName, String origin, String destination,
                    double quantity, double totalCost, LocalDate exportDate, ShipmentStatus status, boolean hasInsurance) {
        this.shipmentId = shipmentId;
        this.licenseId = licenseId;
        this.productName = productName;
        this.origin = origin;
        this.destination = destination;
        this.quantity = quantity;
        this.totalCost = totalCost;
        this.exportDate = exportDate;
        this.status = status;
        this.hasInsurance = hasInsurance;
    }

    // Constructor for new Shipment (Used when inserting from Controller)
    public Shipment(int licenseId, String productName, String origin, String destination,
                    double quantity, double totalCost, LocalDate exportDate, boolean hasInsurance) {
        this(-1, licenseId, productName, origin, destination, quantity, totalCost, exportDate, ShipmentStatus.PENDING, hasInsurance);
    }

    // --- Getters and Setters ---
    public int getShipmentId() { return shipmentId; }
    public void setShipmentId(int shipmentId) { this.shipmentId = shipmentId; }

    public int getLicenseId() { return licenseId; }
    public String getProductName() { return productName; }
    public String getOrigin() { return origin; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public double getQuantity() { return quantity; }
    public double getTotalCost() { return totalCost; }
    public LocalDate getExportDate() { return exportDate; }

    public ShipmentStatus getStatus() { return status; }
    public void setStatus(ShipmentStatus status) { this.status = status; }
    public boolean isHasInsurance() { return hasInsurance; }
}