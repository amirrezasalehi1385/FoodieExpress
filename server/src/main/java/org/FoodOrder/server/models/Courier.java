package org.FoodOrder.server.models;


import org.FoodOrder.server.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@DiscriminatorValue("COURIER")
public class Courier extends User {

    @OneToMany(mappedBy = "courier", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Order> ordersAssigned = new ArrayList<>();
    public Courier() {
        super();
        setRole(Role.COURIER);
    }
    public Courier(String fullName, String address, String phoneNumber, String email, String password, String profileImageBase64, BankInfo bankInfo) {
        super(fullName, address, phoneNumber, email, password, profileImageBase64, bankInfo);
        setRole(Role.COURIER);
    }
}