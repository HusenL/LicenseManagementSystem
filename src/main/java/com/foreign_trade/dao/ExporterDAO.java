package com.foreign_trade.dao;

import com.foreign_trade.model.Exporter;
import com.foreign_trade.util.DbUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExporterDAO {

    private static final String INSERT_EXPORTER = "INSERT INTO Exporter (firm_name, iec_number, contact_person, country) VALUES (?, ?, ?, ?)";
    private static final String SELECT_BY_IEC = "SELECT * FROM Exporter WHERE iec_number = ?";
    private static final String SELECT_ALL_EXPORTERS = "SELECT * FROM Exporter";

    // --- C: Create (Insert) ---
    public int insertExporter(Exporter exporter) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;
        int id = -1;

        try {
            connection = DbUtil.getConnection();
            statement = connection.prepareStatement(INSERT_EXPORTER, Statement.RETURN_GENERATED_KEYS);

            statement.setString(1, exporter.getFirmName());
            statement.setString(2, exporter.getIecNumber());
            statement.setString(3, exporter.getContactPerson());
            statement.setString(4, exporter.getCountry());

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    id = generatedKeys.getInt(1);
                    exporter.setExporterId(id);
                }
            }
        } finally {
            DbUtil.closeConnection(connection);
            if (statement != null) statement.close();
            if (generatedKeys != null) generatedKeys.close();
        }
        return id;
    }

    // --- R: Read (Retrieve by IEC) ---
    public Exporter getExporterByIec(String iecNumber) throws SQLException {
        Exporter exporter = null;
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DbUtil.getConnection();
            statement = connection.prepareStatement(SELECT_BY_IEC);
            statement.setString(1, iecNumber);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                // Map the ResultSet data to the Exporter object
                exporter = new Exporter(
                        resultSet.getInt("exporter_id"),
                        resultSet.getString("firm_name"),
                        resultSet.getString("iec_number"),
                        resultSet.getString("contact_person"),
                        resultSet.getString("country")
                );
            }
        } finally {
            DbUtil.closeConnection(connection);
            if (statement != null) statement.close();
            if (resultSet != null) resultSet.close();
        }
        return exporter;
    }

    // --- R: Read All (For Populating ComboBoxes in UI) ---
    public List<Exporter> getAllExporters() throws SQLException {
        List<Exporter> exporters = new ArrayList<>();
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DbUtil.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(SELECT_ALL_EXPORTERS);

            while (resultSet.next()) {
                exporters.add(new Exporter(
                        resultSet.getInt("exporter_id"),
                        resultSet.getString("firm_name"),
                        resultSet.getString("iec_number"),
                        resultSet.getString("contact_person"),
                        resultSet.getString("country")
                ));
            }
        } finally {
            DbUtil.closeConnection(connection);
            if (statement != null) statement.close();
            if (resultSet != null) resultSet.close();
        }
        return exporters;
    }
}