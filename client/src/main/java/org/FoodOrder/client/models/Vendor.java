package org.FoodOrder.client.models;
import org.FoodOrder.client.enums.*;

import java.util.ArrayList;
import java.util.List;

public class Vendor extends User {

    private List<Restaurant> restaurants = new ArrayList<>();

    public Vendor() {
        super();
        setRole(Role.SELLER);
    }

    public Vendor(String fullName, String address, String phoneNumber, String password) {
        super(fullName, address, phoneNumber, password);
        setRole(Role.SELLER);
    }

    public List<Restaurant> getRestaurants() {
        return restaurants;
    }

    public void setRestaurants(List<Restaurant> restaurants) {
        this.restaurants = restaurants;
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
