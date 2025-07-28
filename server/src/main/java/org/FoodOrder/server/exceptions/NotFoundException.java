package org.FoodOrder.server.exceptions;

public class NotFoundException extends Exception{
    private final Integer statusCode;
    public NotFoundException(String message, Integer statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
    public Integer getStatusCode() {
        return statusCode;
    }
}