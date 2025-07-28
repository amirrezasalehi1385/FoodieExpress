package org.FoodOrder.server.DTO;

import lombok.Getter;
import java.util.List;

@Getter
public class CartDto {
    private Long id;
    private Long customerId;
    private List<CartItemDto> items;

    public CartDto() {}

    public CartDto(Long id, Long customerId, List<CartItemDto> items) {
        this.id = id;
        this.customerId = customerId;
        this.items = items;
    }
}