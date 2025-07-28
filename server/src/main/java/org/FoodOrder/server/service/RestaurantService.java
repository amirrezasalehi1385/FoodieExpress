package org.FoodOrder.server.service;

import java.util.List;
import java.util.stream.Collectors;
import org.FoodOrder.server.DTO.RestaurantDto;
import org.FoodOrder.server.models.Restaurant;

public class RestaurantService {
    public static RestaurantDto convertToDto(Restaurant restaurant) {
        Double avgRatingObj =  restaurant.getAverageRating();
        double avgRating = avgRatingObj != null ? avgRatingObj : 0.0;

        return new RestaurantDto(
                restaurant.getId(),
                restaurant.getName(),
                avgRating,
                restaurant.getLogoBase64(),
                restaurant.getAddress(),
                restaurant.getPhone(),
                restaurant.isFavorite()
        );
    }

    public static List<RestaurantDto> convertToDtoList(List<Restaurant> restaurants) {
        return restaurants.stream()
                .map(RestaurantService::convertToDto)
                .collect(Collectors.toList());
    }
}