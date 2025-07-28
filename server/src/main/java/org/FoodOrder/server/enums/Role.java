package org.FoodOrder.server.enums;

public enum Role {
    ADMIN,
    COURIER,
    BUYER,
    SELLER;

    public static Role getRole(String value) {
        return switch (value) {
            case "admin" -> Role.ADMIN;
            case "courier" -> Role.COURIER;
            case "buyer" -> Role.BUYER;
            case "seller" -> Role.SELLER;
            default -> null;
        };
    }
}
