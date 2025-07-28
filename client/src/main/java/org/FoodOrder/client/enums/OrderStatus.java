package org.FoodOrder.client.enums;

public enum OrderStatus {
    SUBMITTED("SUBMITTED"),
    UNPAID_AND_CANCELLED("UNPAID_AND_CANCELLED"),
    WAITING_VENDOR("WAITING_VENDOR"),
    CANCELLED("CANCELLED"),
    FINDING_COURIER("FINDING_COURIER"),
    ON_THE_WAY("ON_THE_WAY"),
    COMPLETED("COMPLETED");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static OrderStatus fromString(String status) {
        for (OrderStatus os : OrderStatus.values()) {
            if (os.value.equalsIgnoreCase(status)) {
                return os;
            }
        }
        throw new RuntimeException("Invalid order status: " + status);
    }
}