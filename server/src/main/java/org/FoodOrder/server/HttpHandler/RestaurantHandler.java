package org.FoodOrder.server.HttpHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.FoodOrder.server.DAO.RestaurantDao;
import org.FoodOrder.server.DTO.*;
import org.FoodOrder.server.enums.RestaurantOrderStatus;
import org.FoodOrder.server.exceptions.InvalidInputException;
import org.FoodOrder.server.service.*;
import org.FoodOrder.server.DAO.CourierDao;
import org.FoodOrder.server.DAO.CustomerDao;
import org.FoodOrder.server.DAO.VendorDao;
import org.FoodOrder.server.controllers.RestaurantController;
import org.FoodOrder.server.controllers.VendorController;
import org.FoodOrder.server.enums.OrderStatus;
import org.FoodOrder.server.exceptions.ConflictException;
import org.FoodOrder.server.exceptions.NotAcceptableException;
import org.FoodOrder.server.exceptions.NotFoundException;
import org.FoodOrder.server.models.*;
import org.FoodOrder.server.utils.HibernateUtil;
import org.FoodOrder.server.utils.JwtUtil;
import org.hibernate.SessionFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RestaurantHandler implements HttpHandler {
    private final Gson gson = new Gson();
    private ObjectMapper objectMapper;
    private SessionFactory sessionFactory;
    private JwtUtil jwtUtil;
    private static RestaurantService restaurantService;
    private static RestaurantHandler instance;
    public static RestaurantHandler getInstance() {
        if (instance == null) instance = new RestaurantHandler();
        return instance;
    }

    public RestaurantHandler() {
        this.objectMapper = new ObjectMapper();
        this.sessionFactory = HibernateUtil.getSessionFactory();
        this.jwtUtil = new JwtUtil();
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            String token = extractToken(exchange);
            if (token == null || jwtUtil.validateToken(token) == null) {
                sendResponse(exchange, 401, "Unauthorized");
                System.out.println("Unauthorized access attempt to /restaurants");
                return;
            }
            String userId = jwtUtil.validateToken(token);
            System.out.println("User ID: " + userId);
            if (path.matches("/restaurants$") && "POST".equals(method)) {
                handleCreateRestaurant(exchange, userId);
                System.out.println("Restaurant created");
            } else if (path.matches("/restaurants/mine$") && "GET".equals(method)) {
                handleGetMyRestaurants(exchange, userId);
            } else if (path.matches("/restaurants/\\d+$") && "PUT".equals(method)) {
                handleUpdateRestaurant(exchange, userId);
            } else if(path.matches("/restaurants/\\d+$") && "GET".equals(method)){
                handleGetSpecificRestaurant(exchange,userId);
            }else if (path.matches("/restaurants/\\d+/item$") && "POST".equals(method)) {
                handleAddItemToRestaurant(exchange, userId);
            } else if (path.matches("/restaurants/\\d+/myItems$") && "GET".equals(method)) {
                handleGetItemsFromRestaurant(exchange, userId);
            } else if (path.matches("/restaurants/\\d+/item/\\d+$") && "PUT".equals(method)) {
                handleUpdateItem(exchange, userId);
            } else if (path.matches("/restaurants/\\d+/item/\\d+$") && "DELETE".equals(method)) {
                handleDeleteItem(exchange, userId);
            } else if (path.matches("/restaurants/\\d+/menu$") && "POST".equals(method)) {
                handleAddMenu(exchange, userId);
            } else if (path.matches("/restaurants/\\d+/menu/[^/]+$") && "DELETE".equals(method)) {
                handleDeleteMenu(exchange, userId);
            } else if (path.matches("/restaurants/\\d+/menu/[^/]+$") && "PUT".equals(method)) {
                handleAddItemToMenu(exchange, userId);
            } else if (path.matches("/restaurants/\\d+/menu/[^/]+/\\d+$") && "DELETE".equals(method)) {
                handleRemoveItemFromMenu(exchange, userId);
            } else if (path.matches("/restaurants/\\d+/orders$") && "GET".equals(method)) {
                handleGetRestaurantOrders(exchange, userId);
            } else if (path.matches("/restaurants/orders/\\d+$") && "PUT".equals(method)) {
                handleUpdateOrderStatus(exchange, userId);
            } else {
                sendResponse(exchange, 404, "Not Found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void handleCreateRestaurant(HttpExchange exchange, String userId) throws IOException {
        try {
            List<Restaurant> restaurants = RestaurantDao.findAll();
            for (Restaurant r : restaurants) {
                System.out.println(r.getPhone());
            }
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();

            String name = (jsonObject.has("name") && !jsonObject.get("name").isJsonNull())
                    ? jsonObject.get("name").getAsString()
                    : null;

            String address = (jsonObject.has("address") && !jsonObject.get("address").isJsonNull())
                    ? jsonObject.get("address").getAsString()
                    : null;

            String phone = (jsonObject.has("phone") && !jsonObject.get("phone").isJsonNull())
                    ? jsonObject.get("phone").getAsString()
                    : null;

            String logoBase64 = (jsonObject.has("logoBase64") && !jsonObject.get("logoBase64").isJsonNull())
                    ? jsonObject.get("logoBase64").getAsString()
                    : null;

            if (name == null || name.trim().isEmpty()) {
                sendResponse(exchange, 400, "Bad Request: Restaurant name is required");
                return;
            }
            if (address == null || address.trim().isEmpty()) {
                sendResponse(exchange, 400, "Bad Request: Restaurant address is required");
                return;
            }
            if (phone == null || phone.trim().isEmpty()) {
                sendResponse(exchange, 400, "Bad Request: Restaurant phone is required");
                return;
            }

            Restaurant restaurant = new Restaurant();
            restaurant.setName(name.trim());
            restaurant.setAddress(address.trim());
            restaurant.setPhone(phone.trim());

            if (logoBase64 != null && !logoBase64.trim().isEmpty()) {
                restaurant.setLogoBase64(logoBase64);
            }
            RestaurantController.addRestaurant(restaurant, Long.parseLong(userId));
            sendResponse(exchange, 201, gson.toJson(restaurant));
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "Bad Request: Invalid user ID format");
        } catch (ConflictException e) {
            sendResponse(exchange, 409, "Conflict: " + e.getMessage());
        } catch (NotFoundException e) {
            sendResponse(exchange, 404, "Not Found: " + e.getMessage());
        } catch (AccessDeniedException e) {
            sendResponse(exchange, 403, "Forbidden: " + e.getMessage());
        } catch (NotAcceptableException e) {
            sendResponse(exchange, 406, "Not Acceptable: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "Bad Request: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }
    private void handleGetSpecificRestaurant(HttpExchange exchange, String userId) throws IOException {
        try {
            String[] pathParts = exchange.getRequestURI().getPath().split("/");
            if (pathParts.length < 3) {
                sendResponse(exchange, 400, "{\"error\":\"Invalid URL format\"}");
                return;
            }
            Long restaurantId = Long.parseLong(pathParts[2]);

            Restaurant restaurant = RestaurantController.getRestaurantById(restaurantId);
            if (restaurant == null) {
                sendResponse(exchange, 404, "{\"error\":\"Restaurant not found\"}");
                return;
            }

            RestaurantDto restaurantDto = RestaurantService.convertToDto(restaurant);
            String jsonResponse = objectMapper.writeValueAsString(restaurantDto);

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\":\"Invalid restaurant ID format\"}");
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"Internal server error: " + e.getMessage() + "\"}");
        }
    }


    private void handleGetMyRestaurants(HttpExchange exchange, String userId) throws IOException {
        try {
            Long vendorId = Long.parseLong(userId);
            List<Restaurant> restaurants = RestaurantController.getRestaurantsByVendorId(vendorId);

            List<RestaurantDto> restaurantDTOs = restaurants.stream()
                    .map(RestaurantService::convertToDto)
                    .collect(Collectors.toList());

            ObjectMapper mapper = new ObjectMapper();
            String jsonResponse = mapper.writeValueAsString(restaurantDTOs);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(jsonResponse.getBytes());
            os.close();
        } catch (AccessDeniedException e) {
            sendResponse(exchange, 403, "Access Denied: " + e.getMessage());
        } catch (NotFoundException e) {
            sendResponse(exchange, 404, "Not Found: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal Server Error");
        }
    }



    private void handleUpdateRestaurant(HttpExchange exchange, String userId) throws IOException {
        try {
            String[] pathParts = exchange.getRequestURI().getPath().split("/");
            long restaurantId = Long.parseLong(pathParts[2]);

            InputStream requestBody = exchange.getRequestBody();
            Restaurant updatedRestaurant = gson.fromJson(new String(requestBody.readAllBytes()), Restaurant.class);

            RestaurantController.updateRestaurant(restaurantId, updatedRestaurant, Long.parseLong(userId));
            sendResponse(exchange, 200, gson.toJson(updatedRestaurant));
        } catch (NotFoundException e) {
            sendResponse(exchange, 404, e.getMessage());
        } catch (AccessDeniedException e) {
            sendResponse(exchange, 403, e.getMessage());
        } catch (Exception e) {
            sendResponse(exchange, 400, "Bad Request: " + e.getMessage());
        }
    }

    private void handleAddItemToRestaurant(HttpExchange exchange, String userId) throws IOException {
        try {
            String[] pathParts = exchange.getRequestURI().getPath().split("/");
            long restaurantId = Long.parseLong(pathParts[2]);

            InputStream requestBody = exchange.getRequestBody();
            FoodItemDto dto = gson.fromJson(new String(requestBody.readAllBytes()), FoodItemDto.class);

            FoodItem createdItem = RestaurantController.addItemToRestaurant(dto, restaurantId);
            FoodItemDto responseDto = FoodItemService.convertToDto(createdItem);

            sendResponse(exchange, 201, gson.toJson(responseDto));
        } catch (NotFoundException e) {
            sendResponse(exchange, 404, e.getMessage());
        } catch (InvalidInputException e) {
            sendResponse(exchange, 400, "Bad Request: " + e.getMessage());
        } catch (ConflictException e) {
            sendResponse(exchange, 409, "Conflict: " + e.getMessage());
        }
    }

    private void handleGetItemsFromRestaurant(HttpExchange exchange, String userId) throws IOException {
        try {
            String[] pathParts = exchange.getRequestURI().getPath().split("/");
            long restaurantId = Long.parseLong(pathParts[2]);
            List<FoodItem> foodItems = RestaurantController.getItemsFromRestaurant(restaurantId, Long.parseLong(userId));
            List<FoodItemDto> foodItemDtos = foodItems.stream()
                    .map(FoodItemService::convertToDto)
                    .collect(Collectors.toList());
            ObjectMapper mapper = new ObjectMapper();
            String jsonResponse = mapper.writeValueAsString(foodItemDtos);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(jsonResponse.getBytes());
            os.close();
        } catch (AccessDeniedException e) {
            sendResponse(exchange, 403, "Access Denied: " + e.getMessage());
        } catch (NotFoundException e) {
            sendResponse(exchange, 404, "Not Found: " + e.getMessage());
        }
    }
    private void handleUpdateItem(HttpExchange exchange, String userId) throws IOException {
        try {
            String[] pathParts = exchange.getRequestURI().getPath().split("/");
            long restaurantId = Long.parseLong(pathParts[2]);
            long itemId = Long.parseLong(pathParts[4]);
            InputStream requestBody = exchange.getRequestBody();
            FoodItem updatedItem = gson.fromJson(new String(requestBody.readAllBytes()), FoodItem.class);
            RestaurantController.editItemFromRestaurant(restaurantId, updatedItem, itemId, Long.parseLong(userId));
            sendResponse(exchange, 200, gson.toJson(updatedItem));
        } catch (NotFoundException e) {
            sendResponse(exchange, 404, e.getMessage());
        } catch (Exception e) {
            sendResponse(exchange, 400, "Bad Request: " + e.getMessage());
        }
    }

    private void handleDeleteItem(HttpExchange exchange, String userId) throws IOException {
        try {
            String[] pathParts = exchange.getRequestURI().getPath().split("/");
            long restaurantId = Long.parseLong(pathParts[2]);
            long itemId = Long.parseLong(pathParts[4]);
            Restaurant restaurant = RestaurantController.getRestaurantById(restaurantId);
            RestaurantController.deleteItemFromRestaurant(restaurant, itemId);
            sendResponse(exchange, 200, "{\"message\":\"Food item removed successfully\"}");
        } catch (NotFoundException e) {
            sendResponse(exchange, 404, e.getMessage());
        } catch (Exception e) {
            sendResponse(exchange, 400, "Bad Request: " + e.getMessage());
        }
    }

    private void handleAddMenu(HttpExchange exchange, String userId) throws IOException {
        try {
            String[] pathParts = exchange.getRequestURI().getPath().split("/");
            long restaurantId = Long.parseLong(pathParts[2]);

            InputStream requestBody = exchange.getRequestBody();
            Map<String, String> request = gson.fromJson(new String(requestBody.readAllBytes()), Map.class);
            String title = request.get("title");

            RestaurantController.addMenuToRestaurant(restaurantId, title);
            sendResponse(exchange, 200, "{\"title\":\"" + title + "\"}");
        } catch (NotFoundException e) {
            sendResponse(exchange, 404, e.getMessage());
        } catch (Exception e) {
            sendResponse(exchange, 400, "Bad Request: " + e.getMessage());
        }
    }

    private void handleDeleteMenu(HttpExchange exchange, String userId) throws IOException {
        try {
            String[] pathParts = exchange.getRequestURI().getPath().split("/");
            long restaurantId = Long.parseLong(pathParts[2]);
            String title = pathParts[4];

            RestaurantController.deleteMenuFromRestaurant(restaurantId, title);
            sendResponse(exchange, 200, "{\"message\":\"Food menu removed from restaurant successfully\"}");
        } catch (NotFoundException e) {
            sendResponse(exchange, 404, e.getMessage());
        } catch (Exception e) {
            sendResponse(exchange, 400, "Bad Request: " + e.getMessage());
        }
    }

    private void handleAddItemToMenu(HttpExchange exchange, String userId) throws IOException {
        try {
            String[] pathParts = exchange.getRequestURI().getPath().split("/");
            long restaurantId = Long.parseLong(pathParts[2]);
            String title = pathParts[4];

            InputStream requestBody = exchange.getRequestBody();
            Map<String, Long> request = gson.fromJson(new String(requestBody.readAllBytes()), Map.class);
            long itemId = request.get("item_id");

            RestaurantController.addItemToMenu(restaurantId, title, itemId);
            sendResponse(exchange, 200, "{\"message\":\"Food item added to menu successfully\"}");
        } catch (NotFoundException e) {
            sendResponse(exchange, 404, e.getMessage());
        } catch (Exception e) {
            sendResponse(exchange, 400, "Bad Request: " + e.getMessage());
        }
    }

    private void handleRemoveItemFromMenu(HttpExchange exchange, String userId) throws IOException {
        try {
            String[] pathParts = exchange.getRequestURI().getPath().split("/");
            long restaurantId = Long.parseLong(pathParts[2]);
            String title = pathParts[4];
            long itemId = Long.parseLong(pathParts[6]);

            RestaurantController.removeItemFromMenu(restaurantId, title, itemId);
            sendResponse(exchange, 200, "{\"message\":\"Item removed from restaurant menu successfully\"}");
        } catch (NotFoundException e) {
            sendResponse(exchange, 404, e.getMessage());
        } catch (Exception e) {
            sendResponse(exchange, 400, "Bad Request: " + e.getMessage());
        }
    }

    private void handleGetRestaurantOrders(HttpExchange exchange, String userId) throws IOException {
        try {
            String[] pathParts = exchange.getRequestURI().getPath().split("/");
            long restaurantId = Long.parseLong(pathParts[2]);

            Map<String, String> filters = new HashMap<>();
            String query = exchange.getRequestURI().getQuery();
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length > 1) {
                        filters.put(pair[0], pair[1]);
                    }
                }
            }

            List<Order> orders = RestaurantController.getOrdersByRestaurantId(restaurantId, filters);
            List<OrderDto> orderDtos = orders.stream()
                    .map(OrderService::convertToDto)
                    .collect(Collectors.toList());

            String jsonResponse = gson.toJson(orderDtos);
            sendResponse(exchange, 200, jsonResponse);
        } catch (NotFoundException e) {
            sendResponse(exchange, 404, e.getMessage());
        } catch (Exception e) {
            sendResponse(exchange, 400, "Bad Request: " + e.getMessage());
        }
    }

    private void handleUpdateOrderStatus(HttpExchange exchange, String userId) throws IOException {
        try {
            String[] pathParts = exchange.getRequestURI().getPath().split("/");
            long orderId = Long.parseLong(pathParts[3]);

            InputStream requestBody = exchange.getRequestBody();
            Map<String, String> request = gson.fromJson(new String(requestBody.readAllBytes()), Map.class);
            String status = request.get("status");
            Vendor vendor = VendorController.getVendorById(Long.parseLong(userId));

            RestaurantController.changeOrderStatus(vendor, status, orderId);

            sendResponse(exchange, 200, "{\"message\":\"Order status changed successfully\"}");
        } catch (NotFoundException e) {
            sendResponse(exchange, 404, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\":\"Bad Request: " + e.getMessage() + "\"}");
        } catch (AccessDeniedException e) {
            sendResponse(exchange, 403, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            sendResponse(exchange, 400, "{\"error\":\"Bad Request: " + e.getMessage() + "\"}");
        }
    }


    private String extractToken(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes(StandardCharsets.UTF_8));
        os.close();
    }

}