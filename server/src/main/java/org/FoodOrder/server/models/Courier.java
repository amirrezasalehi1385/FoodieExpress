package org.FoodOrder.server.models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.FoodOrder.server.enums.Role;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@DiscriminatorValue("COURIER")
public class Courier extends User {

    @OneToMany(mappedBy = "courier", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Order> ordersAssigned = new ArrayList<>();
    public Courier() {
        super();
        setRole(Role.COURIER);
    }
    public Courier(String fullName, String address, String phoneNumber, String password) {
        super(fullName, address, phoneNumber, password);
        setRole(Role.COURIER);
    }
}