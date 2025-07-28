package org.FoodOrder.client.models;

import java.util.ArrayList;
import java.util.List;

public class Menu {
    private int id;
    private String title;
    private List<FoodItem> items = new ArrayList<>();
    private int restaurantId;  // به جای ارجاع به Restaurant، فقط شناسه را ذخیره می‌کنیم

    public Menu() {}

    public Menu(int id, String title, int restaurantId) {
        this.id = id;
        this.title = title;
        this.restaurantId = restaurantId;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public List<FoodItem> getItems() {
        return items;
    }
    public void setItems(List<FoodItem> items) {
        this.items = items;
    }

    public int getRestaurantId() {
        return restaurantId;
    }
    public void setRestaurantId(int restaurantId) {
        this.restaurantId = restaurantId;
    }

    public void addItem(FoodItem item) {
        items.add(item);
    }

    public void removeItem(int itemId) {
        items.removeIf(item -> item.getId() == itemId);
    }
}
