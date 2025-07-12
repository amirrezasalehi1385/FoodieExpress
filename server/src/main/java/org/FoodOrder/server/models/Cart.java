package org.FoodOrder.server.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "cart")
@Getter
@Setter
@NoArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false, unique = true)
    private Customer customer;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    public void addItem(FoodItem food, int count) {
        for (CartItem item : items) {
            if (item.getFood().equals(food)) {
                item.setCount(item.getCount() + count);
                return;
            }
        }
        CartItem newItem = new CartItem(this, food, count);
        items.add(newItem);
    }

    public void removeItem(FoodItem food) {
        items.removeIf(item -> item.getFood().equals(food));
    }

    public void clear() {
        items.clear();
    }
}

