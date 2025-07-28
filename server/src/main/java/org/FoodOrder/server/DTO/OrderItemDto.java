package org.FoodOrder.server.DTO;


import lombok.Getter;

@Getter
public class OrderItemDto {
    private Long itemId;
    private String itemName;
    private int quantity;
    private double price;

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