package org.FoodOrder.client.models;

public class OrderItem {
    private Long itemId;
    private String itemName;
    private int quantity;
    private double price;
    public Long getItemId() {
        return itemId;
    }
    public String getItemName() {
        return itemName;
    }
    public int getQuantity() {
        return quantity;
    }
    public double getPrice() {
        return price;
    }
    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}