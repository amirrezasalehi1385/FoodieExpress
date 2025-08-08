package org.FoodOrder.client.models;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private Long id;
    private Long customerId;
    private List<CartItem> items = new ArrayList<>();
    public Cart() {}
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getCustomerId() {
        return customerId;
    }
    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }
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

    public void removeItem(FoodItem food, int quantity) {
        CartItem cartItem = items.stream()
                .filter(item -> item.getFood().equals(food))
                .findFirst()
                .orElse(null);
        if (cartItem != null) {
            int currentQuantity = cartItem.getCount();
            if (currentQuantity <= quantity) {
                items.removeIf(item -> item.getFood().equals(food));
            } else {
                cartItem.setCount(currentQuantity - quantity);
            }
        }
    }

    public void clear() {
        items.clear();
    }
}