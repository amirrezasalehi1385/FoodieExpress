package org.FoodOrder.server.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.FoodOrder.server.enums.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@DiscriminatorValue("SELLER")
public class Vendor extends User {

    @OneToMany(mappedBy = "seller", fetch = FetchType.EAGER)
    private List<Restaurant> restaurants = new ArrayList<>();

    public Vendor() {
        super();
        setRole(Role.SELLER);
    }

    public Vendor(String fullName, String address, String phoneNumber, String password) {
        super(fullName, address, phoneNumber, password);
        setRole(Role.SELLER);
    }

    public void addRestaurant(Restaurant restaurant) {
        restaurants.add(restaurant);
        restaurant.setSeller(this);
    }

    public void removeRestaurant(Restaurant restaurant) {
        restaurants.remove(restaurant);
        restaurant.setSeller(null);
    }
}
