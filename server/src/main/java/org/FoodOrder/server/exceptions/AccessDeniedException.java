package org.FoodOrder.server.exceptions;

public class AccessDeniedException extends Exception {
    private final Integer statusCode;
    public AccessDeniedException(String message, Integer statusCode) {
      super(message);
      this.statusCode = statusCode;
    }
}
