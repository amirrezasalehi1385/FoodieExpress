package org.FoodOrder.server.exceptions;

public class InvalidInputException extends Exception{
    private final Integer statusCode;
    public InvalidInputException(String message,Integer statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
}
