package org.FoodOrder.server.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.FoodOrder.server.enums.Role;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@DiscriminatorValue("BUYER")
public class Customer extends User {

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.TRUE)
    @JsonIgnore
    private List<Order> ordersAssigned = new ArrayList<>();



    @ManyToMany
    @JoinTable(name = "customer_favorite_restaurants",
            joinColumns = @JoinColumn(name = "customer_id"),
            inverseJoinColumns = @JoinColumn(name = "restaurant_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"customer_id", "restaurant_id"}))
    private List<Restaurant> favoriteRestaurants = new ArrayList<>();

    public Customer() {
        super();
        setRole(Role.BUYER);
    }

    public Customer(String fullName, String address, String phoneNumber, String password) {
        super(fullName, address, phoneNumber, password);
        setRole(Role.BUYER);
    }

    public void addFavorite(Restaurant restaurant) {
        if (!this.favoriteRestaurants.contains(restaurant)) {
            this.favoriteRestaurants.add(restaurant);
        }
    }

    public boolean removeFavoriteById(int restaurantId) {
        return this.favoriteRestaurants.removeIf(restaurant -> restaurant.getId() == restaurantId);
    }
}