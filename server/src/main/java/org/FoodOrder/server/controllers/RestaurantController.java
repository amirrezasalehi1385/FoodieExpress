package org.FoodOrder.server.controllers;

import org.FoodOrder.server.DTO.FoodItemDto;
import org.FoodOrder.server.enums.OrderStatus;
import org.FoodOrder.server.enums.RestaurantOrderStatus;
import org.FoodOrder.server.enums.Role;
import org.FoodOrder.server.models.Restaurant;
import org.FoodOrder.server.models.*;
import org.FoodOrder.server.DAO.*;
import org.FoodOrder.server.exceptions.*;

import java.nio.file.AccessDeniedException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RestaurantController {

    private RestaurantController() {}
    public static List<Restaurant> getRestaurantsByVendorId(Long userId) throws NotFoundException, AccessDeniedException {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        UserDao userDao = new UserDao();
        User user = userDao.findById(userId);
        if (user == null) {
            throw new NotFoundException("User does not exist!", 404);
        }
        if (!(user instanceof Vendor)) {
            throw new AccessDeniedException("User is not authorized to create restaurants");
        }
        return RestaurantDao.findByVendorId(userId);
    }

    public static void changeOrderStatus(Vendor vendor, String status, Long orderId) throws NotFoundException, AccessDeniedException, IllegalArgumentException {
        OrderDao orderDao = new OrderDao();
        Order order = orderDao.findById(orderId);

        if (order == null) {
            throw new NotFoundException("Order not found", 404);
        }

        if (!order.getRestaurant().getSeller().getId().equals(vendor.getId())) {
            throw new AccessDeniedException("User is not authorized to change order status");
        }

        OrderStatus orderStatus = null;
        for (OrderStatus os : OrderStatus.values()) {
            if (os.getValue().equalsIgnoreCase(status)) {
                orderStatus = os;
                break;
            }
        }

        if (orderStatus == null) {
            throw new IllegalArgumentException("Invalid status value: " + status);
        }

        order.setStatus(orderStatus);
        orderDao.update(order);
    }

    public static void addRestaurant(Restaurant restaurant, Long userId)
            throws SQLException, NotAcceptableException, NotFoundException, AccessDeniedException, ConflictException {
        if (restaurant == null || userId == null) {
            throw new IllegalArgumentException("Restaurant or userId is null");
        }

        UserDao userDao = new UserDao();
        User user =  userDao.findById(userId);

        if (user == null) throw new NotFoundException("User does not exist!", 404);
        if (user.getRole() != Role.SELLER) throw new AccessDeniedException("Unauthorized");
        Vendor vendor = (Vendor) user;

        System.out.println(vendor.getId());
        restaurant.setSeller(vendor);
        System.out.println("==> ADDING: " + restaurant.getPhone());

        RestaurantDao.save(restaurant);
    }

    public static boolean restaurantExistsById(Long id) throws SQLException {
        try {
            Restaurant restaurant = new Restaurant();
            restaurant = RestaurantDao.findById(id);
            return restaurant != null;
        } catch (Exception e) {
            return false;
        }
    }
    public static void updateRestaurant(Long id, Restaurant updatedRestaurant, Long userId) throws SQLException, NotFoundException, AccessDeniedException, InvalidInputException, AlreadyExistException {
        UserDao userDao = new UserDao();
        User user = userDao.findById(userId);
        if(user == null){
            throw new NotFoundException("User Not exists!", 404);
        } else if(user.getRole() != Role.SELLER) {
            throw new AccessDeniedException("Access Denied!");
        }
        Restaurant restaurant = RestaurantDao.findById(id);
        if(restaurant == null){
            throw new NotFoundException("Restaurant Not exists!" ,404);
        }
        if(restaurant.getSeller() == null || !restaurant.getSeller().getId().equals(userId)){
            throw new AccessDeniedException("Access Denied!");
        }
        restaurant.setName(updatedRestaurant.getName());
        restaurant.setAddress(updatedRestaurant.getAddress());
        restaurant.setPhone(updatedRestaurant.getPhone());
        restaurant.setLogoBase64(updatedRestaurant.getLogoBase64());
        RestaurantDao.update(restaurant);
    }

    public static List<Restaurant> searchRestaurant(String input) throws SQLException {
        List<Restaurant> restaurants = RestaurantDao.findAll();
        restaurants.removeIf(restaurant -> !(restaurant.getName().toLowerCase().contains(input.toLowerCase())
                || input.toLowerCase().contains(restaurant.getName().toLowerCase())
                || input.toLowerCase().contains(restaurant.getCategory().toString().toLowerCase())
        ));
        return restaurants;
    }
    public static void addMenuToRestaurant(Long restaurantId, String title) throws Exception {
        Restaurant restaurant = RestaurantDao.findById(restaurantId);
        if (restaurant == null) {
            throw new NotFoundException("Restaurant not found", 404);
        }

        List<Menu> existingMenus = MenuDao.findByRestaurantId(restaurantId);
        if (existingMenus != null && existingMenus.stream().anyMatch(menu -> menu.getTitle().equals(title))) {
            throw new NotAcceptableException("Menu with title '" + title + "' already exists for this restaurant");
        }

        Menu menu = new Menu();
        menu.setTitle(title);
        menu.setRestaurant(restaurant);
        MenuDao.save(menu);
    }
    public static List<FoodItem> getItemsFromRestaurant(Long restaurantId, Long userId) throws NotFoundException, AccessDeniedException {
        UserDao userDao = new UserDao();
        User user = userDao.findById(userId);
        if(user == null){
            throw new NotFoundException("User Not exists!", 404);
        } else if(user.getRole() != Role.SELLER) {
            throw new AccessDeniedException("Access Denied!");
        }
        Restaurant restaurant = RestaurantDao.findById(restaurantId);
        if (restaurant == null) {
            throw new NotFoundException("Restaurant not found", 404);
        }
        if(!restaurant.getSeller().getId().equals(userId)){
            throw new AccessDeniedException("Access Denied!");
        }
        return FoodDao.findByRestaurantId(restaurantId);
    }
    public static Menu addItemToMenu(Long restaurantId, String title, Long itemId) throws Exception {
        List<Menu> menus = MenuDao.findByRestaurantId(restaurantId);
        if (menus == null) {
            throw new Exception("No menus found for restaurant");
        }
        Menu menu = menus.stream()
                .filter(m -> m.getTitle().equals(title))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Menu not found", 404));

        FoodItem foodItem = FoodDao.findById(itemId);
        if (foodItem == null) {
            throw new NotFoundException("Food item not found", 404);
        }
        List<FoodItem> items = menu.getItems();
        if (items == null) {
            items = new java.util.ArrayList<>();
            menu.setItems(items);
        }
        if (!items.contains(foodItem)) {
            items.add(foodItem);
            MenuDao.update(menu);
        }
        return menu;
    }

    public static void deleteMenuFromRestaurant(Long restaurantId, String title) throws Exception {
        List<Menu> menus = MenuDao.findByRestaurantId(restaurantId);
        if (menus == null) {
            throw new Exception("No menus found for restaurant");
        }
        Menu menu = menus.stream()
                .filter(m -> m.getTitle().equals(title))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Menu not found", 404));

        MenuDao.delete((long) menu.getId());
    }

    public static void removeItemFromMenu(Long restaurantId, String title, Long itemId) throws Exception {
        List<Menu> menus = MenuDao.findByRestaurantId(restaurantId);
        if (menus == null) {
            throw new NotAcceptableException("No menus found for restaurant");
        }
        Menu menu = menus.stream()
                .filter(m -> m.getTitle().equals(title))
                .findFirst()
                .orElseThrow(() -> new Exception("Menu not found"));
        FoodItem foodItem = FoodDao.findById(itemId);
        if (foodItem == null) {
            throw new NotFoundException("Food item not found", 404);
        }
        List<FoodItem> items = menu.getItems();
        if (items != null && items.remove(foodItem)) {
            MenuDao.update(menu);
        } else {
            throw new NotFoundException("Food item not found in menu",404);
        }
    }

    public static List<Menu> getMenusByRestaurant(Long restaurantId) throws Exception {
        List<Menu> menus = MenuDao.findByRestaurantId(restaurantId);
        if (menus == null) {
            throw new NotFoundException("No menus found for restaurant",404);
        }
        return menus;
    }
    public static FoodItem addItemToRestaurant(FoodItemDto dto, Long id) throws InvalidInputException, NotFoundException, ConflictException {
        if (dto.getName() == null || dto.getDescription() == null || dto.getPrice() < 0 || dto.getSupply() <= 0) {
            throw new InvalidInputException("Food Item not acceptable", 400);
        }

        Restaurant restaurant = RestaurantDao.findById(id);
        if (restaurant == null) {
            throw new NotFoundException("Restaurant not found", 404);
        }

        FoodItem item = new FoodItem();
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());
        item.setSupply(dto.getSupply());
        item.setCategories(dto.getCategories());
        item.setImageBase64(dto.getImageBase64());
        item.setRestaurant(restaurant);
        FoodDao.save(item);
        return item;
    }
    public static void deleteItemFromRestaurant(Restaurant restaurant,Long itemId) throws Exception {
        FoodItem item = FoodDao.findById(itemId);
        if (item == null) {throw new NotFoundException("Food item not found",404);}
        if (restaurant.getId() != item.getRestaurant().getId()) { throw new NotAcceptableException("Restaurant not acceptable");}
//        for (Menu menu:item.getMenus()) {
//            menu.removeItem((int) item.getId());
//            MenuDao.update(menu);
//        }
        FoodDao.deleteById(itemId);
    }
    public static void editItemFromRestaurant(Long restaurantId,FoodItem updatedItem, Long itemId, Long userId) throws NotFoundException, InvalidInputException, NotAcceptableException, AccessDeniedException {
        UserDao userDao = new UserDao();
        User user = userDao.findById(userId);
        if(user == null) {
            throw new NotFoundException("User not found",404);
        }else if(user.getRole() != Role.SELLER){
            throw  new NotAcceptableException("You are not allowed to edit this restaurant");
        }
        Restaurant restaurant = RestaurantDao.findById(restaurantId);
        if(restaurant == null) {throw new NotFoundException("Restaurant not found",404);}

        if(!restaurant.getSeller().getId().equals(userId)){
            throw new AccessDeniedException("You are not allowed to edit this restaurant");
        }
        FoodItem item = FoodDao.findById(itemId);
        if (item == null) {throw new NotFoundException("Food item not found",404);}
        if (item.getName() == null || item.getDescription() == null || item.getPrice() < 0 || item.getSupply() <= 0) {
            throw new InvalidInputException("Food Item not acceptable", 400);
        }
        if (restaurantId != item.getRestaurant().getId()) { throw new NotAcceptableException("Restaurant not acceptable");}
        item.setName(updatedItem.getName());
        item.setDescription(updatedItem.getDescription());
        item.setPrice(updatedItem.getPrice());
        item.setSupply(updatedItem.getSupply());
        item.setCategories(updatedItem.getCategories());
        item.setImageBase64(updatedItem.getImageBase64());
        FoodDao.update(item);
    }
    public static List<Order> getOrdersByRestaurantId(Long restaurantId, Map<String, String> filters) throws NotFoundException {
        Restaurant restaurant = RestaurantDao.findById(restaurantId);
        if (restaurant == null) {
            throw new NotFoundException("Restaurant not found", 404);
        }
        return OrderDao.getOrdersByRestaurantIdWithFilters(restaurantId, filters);
    }

    public static Restaurant getRestaurantById(Long restaurantId) throws NotFoundException {
        if(RestaurantDao.findById(restaurantId) == null){
            throw new NotFoundException("Restaurant Not found!!", 404);
        }
        return RestaurantDao.findById(restaurantId);
    }

}