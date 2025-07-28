package org.FoodOrder.client.models;

import org.FoodOrder.client.enums.Role;

public class Courier extends User {
    public Courier() {
        super();
        setRole(Role.COURIER);
    }

    public Courier(String fullName, String address, String phoneNumber, String password) {
        super(fullName, address, phoneNumber, password);
        setRole(Role.COURIER);
    }
}
