package org.FoodOrder.server.service;

import org.FoodOrder.server.DTO.FoodItemDto;
import org.FoodOrder.server.models.FoodItem;

import java.util.List;
import java.util.stream.Collectors;

public class FoodItemService {
    public static FoodItemDto convertToDto(FoodItem foodItem) {
        return new FoodItemDto(
                foodItem.getId(),
                foodItem.getName(),
                foodItem.getDescription(),
                foodItem.getSupply(),
                foodItem.getCategories(),
                foodItem.getImageBase64(),
                foodItem.getPrice()
        );
    }

    public static List<FoodItemDto> convertToDtoList(List<FoodItem> items) {
        return items.stream()
                .map(FoodItemService::convertToDto)
                .collect(Collectors.toList());
    }
    public static FoodItem convertToModel(FoodItemDto dto) {
        FoodItem item = new FoodItem();
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());
        item.setSupply(dto.getSupply());
        item.setCategories(dto.getCategories());
        item.setImageBase64(dto.getImageBase64());
        return item;
    }

}
