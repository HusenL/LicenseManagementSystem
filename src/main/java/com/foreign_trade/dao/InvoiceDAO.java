package com.foreign_trade.dao;

import com.foreign_trade.model.Invoice;
import com.foreign_trade.model.Invoice.PaymentStatus;
import com.foreign_trade.util.DbUtil;

import java.sql.*;
import java.time.LocalDate;

public class InvoiceDAO {

    private static final String INSERT_INVOICE =
            "INSERT INTO Invoice (shipment_id, amount, payment_date, payment_status) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_STATUS =
            "UPDATE Invoice SET payment_status = ?, payment_date = ? WHERE shipment_id = ?";
    private static final String SELECT_BY_SHIPMENT =
            "SELECT * FROM Invoice WHERE shipment_id = ?";


    // --- C: Create (Insert New Invoice) ---
    public int insertInvoice(Invoice invoice) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;
        int id = -1;

        try {
            connection = DbUtil.getConnection();
            statement = connection.prepareStatement(INSERT_INVOICE, Statement.RETURN_GENERATED_KEYS);

            statement.setInt(1, invoice.getShipmentId());
            statement.setDouble(2, invoice.getAmount());
            // paymentDate can be NULL if PENDING
            statement.setDate(3, invoice.getPaymentDate() != null ? Date.valueOf(invoice.getPaymentDate()) : null);
            statement.setString(4, invoice.getPaymentStatus().name());

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    id = generatedKeys.getInt(1);
                    invoice.setInvoiceId(id);
                }
            }
        } finally {
            DbUtil.closeConnection(connection);
            if (statement != null) statement.close();
            if (generatedKeys != null) generatedKeys.close();
        }
        return id;
    }

    // --- U: Update (Mark Invoice as Paid) ---
    public void updatePaymentStatus(int shipmentId, PaymentStatus newStatus, LocalDate paymentDate) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DbUtil.getConnection();
            statement = connection.prepareStatement(UPDATE_STATUS);
            statement.setString(1, newStatus.name());
            statement.setDate(2, Date.valueOf(paymentDate));
            statement.setInt(3, shipmentId);
            statement.executeUpdate();
        } finally {
            DbUtil.closeConnection(connection);
            if (statement != null) statement.close();
        }
    }

    // --- R: Read (Retrieve by Shipment ID) ---
    public Invoice getInvoiceByShipmentId(int shipmentId) throws SQLException {
        Invoice invoice = null;
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DbUtil.getConnection();
            statement = connection.prepareStatement(SELECT_BY_SHIPMENT);
            statement.setInt(1, shipmentId);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Date sqlDate = resultSet.getDate("payment_date");
                LocalDate paymentDate = (sqlDate != null) ? sqlDate.toLocalDate() : null;

                invoice = new Invoice(
                        resultSet.getInt("invoice_id"),
                        resultSet.getInt("shipment_id"),
                        resultSet.getDouble("amount"),
                        paymentDate,
                        PaymentStatus.valueOf(resultSet.getString("payment_status"))
                );
            }
        } finally {
            DbUtil.closeConnection(connection);
            if (statement != null) statement.close();
            if (resultSet != null) resultSet.close();
        }
        return invoice;
    }
}