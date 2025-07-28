package org.FoodOrder.server.service;

import org.FoodOrder.server.DTO.CartDto;
import org.FoodOrder.server.DTO.CartItemDto;
import org.FoodOrder.server.models.Cart;
import org.FoodOrder.server.models.CartItem;
import org.FoodOrder.server.models.FoodItem;

import java.util.List;
import java.util.stream.Collectors;



import java.util.List;
import java.util.stream.Collectors;

public class CartService {
    public static CartItemDto convertToCartItemDto(CartItem cartItem) {
        return new CartItemDto(
                cartItem.getId(),
                FoodItemService.convertToDto(cartItem.getFood()),
                cartItem.getCount()
        );
    }

    public static List<CartItemDto> convertToCartItemDtoList(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(CartService::convertToCartItemDto)
                .collect(Collectors.toList());
    }

    public static CartDto convertToCartDto(Cart cart) {
        List<CartItemDto> itemDtos = convertToCartItemDtoList(cart.getItems());
        return new CartDto(cart.getId(), cart.getCustomer().getId(), itemDtos);
    }
}