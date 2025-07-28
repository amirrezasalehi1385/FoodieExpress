package org.FoodOrder.server.exceptions;

public class AlreadyExistException extends Exception{
    private final Integer statusCode;
    public AlreadyExistException(String message, Integer statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
}
