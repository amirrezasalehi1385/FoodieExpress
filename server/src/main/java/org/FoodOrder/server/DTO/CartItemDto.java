package org.FoodOrder.server.DTO;

import lombok.Getter;
import org.FoodOrder.server.models.FoodItem;

@Getter
public class CartItemDto {
    private Long id;
    private FoodItemDto food;
    private int count;
    public CartItemDto() {}
    public CartItemDto(Long id, FoodItemDto food, int count) {
        this.id = id;
        this.food = food;
        this.count = count;
    }
}