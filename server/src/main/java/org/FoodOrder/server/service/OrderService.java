
package org.FoodOrder.server.service;

import org.FoodOrder.server.DTO.OrderDto;
import org.FoodOrder.server.DTO.OrderItemDto;
import org.FoodOrder.server.models.Order;
import org.FoodOrder.server.models.OrderItem;

import java.util.List;
import java.util.stream.Collectors;

public class OrderService {

    public static OrderDto convertToDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId((long) order.getId());
        dto.setCustomerId(order.getCustomer().getId());
        dto.setRestaurantId((long) order.getRestaurant().getId());
        dto.setDeliveryAddress(order.getDeliveryAddress());
        dto.setStatus(order.getStatus().toString());
        dto.setTotalPrice(order.getTotalPrice().doubleValue());
        dto.setItems(order.getItems().stream()
                .map(OrderService::convertToOrderItemDto)
                .collect(Collectors.toList()));
        return dto;
    }

    public static List<OrderDto> convertToDtoList(List<Order> orders) {
        return orders.stream()
                .map(OrderService::convertToDto)
                .collect(Collectors.toList());
    }

    private static OrderItemDto convertToOrderItemDto(OrderItem item) {
        OrderItemDto dto = new OrderItemDto();
        dto.setItemId(item.getId());
        dto.setItemName(item.getFood().getName());
        dto.setQuantity(item.getCount());
        dto.setPrice(item.getFood().getPrice());
        return dto;
    }
}