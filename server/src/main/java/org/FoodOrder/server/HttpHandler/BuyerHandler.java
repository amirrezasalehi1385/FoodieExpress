package org.FoodOrder.server.HttpHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.FoodOrder.server.DTO.FoodItemDto;
import org.FoodOrder.server.DTO.RestaurantDto;
import org.FoodOrder.server.controllers.BuyerController;
import org.FoodOrder.server.exceptions.NotAcceptableException;
import org.FoodOrder.server.models.FoodItem;
import org.FoodOrder.server.models.Restaurant;
import org.FoodOrder.server.service.FoodItemService;
import org.FoodOrder.server.service.RestaurantService;
import org.FoodOrder.server.utils.JwtUtil;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BuyerHandler implements HttpHandler {
    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;
    private static BuyerHandler instance;
    public static BuyerHandler getInstance() {
        if (instance == null) instance = new BuyerHandler();
        return instance;
    }
    public BuyerHandler() {
        this.objectMapper = new ObjectMapper();
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
                System.out.println("Unauthorized access attempt to /vendors");
                return;
            }
            String userId = jwtUtil.validateToken(token);
            System.out.println("User ID: " + userId);
            if (path.matches("/vendors$") && "GET".equals(method)) {
                handleListVendors(exchange, userId);
            } else if (path.matches("/vendors/\\d+/item$") && "GET".equals(method)) {
                handleListOfItems(exchange, userId);
            } else if (path.matches("/vendors/\\d+/item/\\d+$") && "GET".equals(method)) {
                handleGetSpecificItem(exchange);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void handleListVendors(HttpExchange exchange, String userId) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String search = "";
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    String key = keyValue[0];
                    String value = java.net.URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                    if (key.equals("search")) {
                        search = value.toLowerCase();
                    }
                }
            }
        }
        List<Restaurant> restaurants = BuyerController.getListOfVendors(search, userId);
        List<RestaurantDto> restaurantDTOs = restaurants.stream()
                .map(RestaurantService::convertToDto)
                .collect(Collectors.toList());
        String jsonResponse = objectMapper.writeValueAsString(restaurantDTOs);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(jsonResponse.getBytes());
        os.close();
    }

    private void handleListOfItems(HttpExchange exchange, String userId) throws IOException {
        try {
            String query = exchange.getRequestURI().getQuery();
            String search = "";
            String minPriceStr = "";
            String maxPriceStr = "";
            ArrayList<String> categories = new ArrayList<>();
            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2) {
                        String key = keyValue[0];
                        String value = java.net.URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                        switch (key) {
                            case "search" -> search = value.toLowerCase();
                            case "categories" -> {
                                String[] cats = value.toLowerCase().split(",");
                                for (String c : cats) {
                                    categories.add(c.trim());
                                }
                            }
                            case "minPrice" -> minPriceStr = value;
                            case "maxPrice" -> maxPriceStr = value;
                        }
                    }
                }
            }
            Integer minPrice = minPriceStr.isEmpty() ? null : Integer.parseInt(minPriceStr);
            Integer maxPrice = maxPriceStr.isEmpty() ? null : Integer.parseInt(maxPriceStr);
            String[] pathParts = exchange.getRequestURI().getPath().split("/");
            long restaurantId = Long.parseLong(pathParts[2]);
            List<FoodItem> foodItems = BuyerController.getListOfFoodItemByFilter(restaurantId, search, minPrice, maxPrice, categories);
            List<FoodItemDto> foodItemDtos = foodItems.stream()
                    .map(FoodItemService::convertToDto)
                    .collect(Collectors.toList());
            String jsonResponse = objectMapper.writeValueAsString(foodItemDtos);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(jsonResponse.getBytes());
            os.close();
        } catch (Exception e) {
            sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void handleGetSpecificItem(HttpExchange exchange) throws IOException {
        try {
            String[] pathParts = exchange.getRequestURI().getPath().split("/");
            long restaurantId = Long.parseLong(pathParts[2]);
            long foodItemId = Long.parseLong(pathParts[4]);
            FoodItem foodItem = BuyerController.getFoodItem(restaurantId, foodItemId);
            FoodItemDto foodItemDto = FoodItemService.convertToDto(foodItem);
            String jsonResponse = objectMapper.writeValueAsString(foodItemDto);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(jsonResponse.getBytes());
            os.close();
        } catch (NotAcceptableException e) {
            sendResponse(exchange, 406, "Not Acceptable");
        } catch (Exception e) {
            sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
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