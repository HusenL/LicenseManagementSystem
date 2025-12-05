package com.foreign_trade.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbUtil {

    // !! IMPORTANT: REPLACE 'your_password' WITH YOUR ACTUAL MYSQL PASSWORD !!
    private static final String URL = "jdbc:mysql://localhost:3306/foreign_trade_db";
    private static final String USER = "root"; // Assuming root user for testing
    private static final String PASSWORD = "Husen@786"; // <--- REPLACE THIS LINE!

    /**
     * Establishes a connection to the MySQL database.
     * @return A Connection object.
     * @throws SQLException if a database access error occurs.
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Attempt to establish the connection
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            // Display the specific SQL error for troubleshooting
            System.err.println("\n--- DETAILED SQL CONNECTION ERROR ---");
            System.err.println("URL: " + URL);
            System.err.println("User: " + USER);
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("SQL Message: " + e.getMessage());
            System.err.println("-------------------------------------");

            // Re-throw the exception to notify the calling DAO/Service method
            throw e;
        }
    }

    /**
     * Closes the connection, preventing resource leaks.
     * @param connection The Connection object to close.
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}
