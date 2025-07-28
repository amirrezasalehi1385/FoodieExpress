package org.FoodOrder.server.enums;

import org.FoodOrder.server.controllers.RestaurantController;

public enum RestaurantOrderStatus {
    BASE,
    ACCEPTED,
    REJECTED,
    SERVED;
    public static RestaurantOrderStatus fromString(String status) {
        try {
            return RestaurantOrderStatus.valueOf(status.toUpperCase()); // Safe way
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid order status: " + status);
        }
    }
}
