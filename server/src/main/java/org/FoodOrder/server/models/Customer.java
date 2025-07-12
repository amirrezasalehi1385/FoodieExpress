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
@DiscriminatorValue("CUSTOMER")
public class Customer extends User {

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Order> ordersAssigned = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "favorite_restaurants",
            joinColumns = @JoinColumn(name = "customer_id"),
            inverseJoinColumns = @JoinColumn(name = "restaurant_id")
    )
    private List<Restaurant> favoriteRestaurants = new ArrayList<>();

    public Customer() {
        super();
        setRole(Role.CUSTOMER);
    }

    public Customer(String fullName, String address, String phoneNumber, String email, String password, String profileImageBase64, String bankName, String accountNumber, BankInfo bankInfo) {
        super(fullName, address, phoneNumber, email, password, profileImageBase64, bankName, accountNumber, bankInfo);
        setRole(Role.CUSTOMER);
    }
}