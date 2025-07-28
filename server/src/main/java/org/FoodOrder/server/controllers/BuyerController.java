package org.FoodOrder.server.controllers;

import org.FoodOrder.server.DAO.*;
import org.FoodOrder.server.exceptions.NotAcceptableException;
import org.FoodOrder.server.models.*;
import org.hibernate.Hibernate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BuyerController {
    public static List<Restaurant> getListOfVendors(String search, String userId) {
        List<Restaurant> restaurants = RestaurantDao.findAll();
        CustomerDao customerDao = new CustomerDao();
        Customer customer = customerDao.findById(Long.parseLong(userId));
        final Set<Integer> favoriteIds;
        if (customer != null) {
            Hibernate.initialize(customer.getFavoriteRestaurants());
            favoriteIds = customer.getFavoriteRestaurants().stream()
                    .map(Restaurant::getId)
                    .collect(Collectors.toSet());
        } else {
            favoriteIds = new HashSet<>();
        }
        if (search == null || search.trim().isEmpty()) {
            restaurants.forEach(restaurant -> restaurant.setFavorite(favoriteIds.contains(restaurant.getId())));
            return restaurants;
        }
        restaurants.removeIf(restaurant -> !restaurant.getName().toLowerCase().contains(search.toLowerCase()));
        restaurants.forEach(restaurant -> restaurant.setFavorite(favoriteIds.contains(restaurant.getId())));
        return restaurants;
    }

    public static FoodItem getFoodItem(Long restaurantId, Long foodItemId) throws NotAcceptableException {
        FoodItem foodItem = FoodDao.findById(foodItemId);
        if (foodItem == null) return null;
        if (foodItem.getRestaurant().getId() != restaurantId) throw new NotAcceptableException("restaurant not acceptable");
        Hibernate.initialize(foodItem.getCategories());
        return foodItem;
    }

    public static List<FoodItem> getListOfFoodItemByFilter(Long restaurantId, String search, Integer minPrice, Integer maxPrice, List<String> categories) {
        List<FoodItem> foodItems = FoodDao.findByRestaurantId(restaurantId);
        List<FoodItem> filteredItems = new ArrayList<>(foodItems);
        boolean noSearch = search == null || search.trim().isEmpty();
        boolean noMinPrice = minPrice == null;
        boolean noMaxPrice = maxPrice == null;
        boolean noCategories = categories == null || categories.isEmpty();
        if (noSearch && noMinPrice && noMaxPrice && noCategories) {
            return filteredItems;
        }
        filteredItems = filteredItems.stream()
                .filter(foodItem -> {
                    if (!noSearch) {
                        if (!foodItem.getName().toLowerCase().contains(search.toLowerCase())) return false;
                    }
                    if (!noMinPrice && foodItem.getPrice() < minPrice) return false;
                    if (!noMaxPrice && foodItem.getPrice() > maxPrice) return false;
                    return noCategories || checkCategory(categories, foodItem);
                })
                .collect(Collectors.toList());
        return filteredItems;
    }
    private static boolean checkCategory(List<String> categories, FoodItem foodItem) {
        if (categories == null || foodItem.getCategories() == null) {
            return false;
        }
        return foodItem.getCategories().stream()
                .anyMatch(category -> categories.stream()
                        .anyMatch(c -> c.equalsIgnoreCase(category)));
    }
}