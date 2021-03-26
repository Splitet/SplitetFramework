package io.splitet.sample.dto;

public class StockNotEnoughException extends Exception {
    public StockNotEnoughException() {
    }

    public StockNotEnoughException(String message) {
        super(message);
    }
}
