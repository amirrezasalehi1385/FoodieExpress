package org.FoodOrder.client.models;

import org.FoodOrder.client.enums.Role;

public class Customer extends User{
    public Customer() {
        super();
        setRole(Role.BUYER);
    }

    public Customer(String fullName, String address, String phoneNumber, String password) {
        super(fullName, address, phoneNumber, password);
        setRole(Role.BUYER);
    }
}
