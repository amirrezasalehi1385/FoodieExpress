package org.FoodOrder.server.exceptions;

public class ConflictException extends Exception{
    private final Integer statusCode;
    public ConflictException(String message, Integer statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
}
