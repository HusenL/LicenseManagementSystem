package com.foreign_trade.service;

// This class is now a standard, public custom exception
public class InsuranceException extends Exception {

    public InsuranceException(String message) {
        super(message);
    }
}
