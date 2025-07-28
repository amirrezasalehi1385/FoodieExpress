package org.FoodOrder.server.HttpHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.FoodOrder.server.DAO.CustomerDao;
import org.FoodOrder.server.DAO.RestaurantDao;
import org.FoodOrder.server.DTO.RestaurantDto;
import org.FoodOrder.server.models.Customer;
import org.FoodOrder.server.models.Restaurant;
import org.FoodOrder.server.service.RestaurantService;
import org.FoodOrder.server.utils.JwtUtil;
import org.hibernate.Hibernate;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class FavoriteHandler implements HttpHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JwtUtil jwtUtil = new JwtUtil();
    private final CustomerDao customerDao = new CustomerDao();
    private static FavoriteHandler instance;

    public static FavoriteHandler getInstance() {
        if (instance == null) instance = new FavoriteHandler();
        return instance;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        String token = extractToken(exchange);
        if (token == null || jwtUtil.validateToken(token) == null) {
            sendResponse(exchange, 401, "{\"error\":\"Unauthorized\"}");
            return;
        }
        String userId = jwtUtil.validateToken(token);
        if (path.matches("/favorites$") && method.equalsIgnoreCase("GET")) {
            handleGetFavorites(exchange, userId);
        } else if (path.matches("/favorites/\\d+$") && method.equalsIgnoreCase("POST")) {
            handleAddFavorite(exchange, userId);
        } else if (path.matches("/favorites/\\d+$") && method.equalsIgnoreCase("DELETE")) {
            handleRemoveFavorite(exchange, userId);
        } else {
            sendResponse(exchange, 404, "{\"error\":\"Not Found\"}");
        }
    }
    private void handleGetFavorites(HttpExchange exchange, String userId) throws IOException {
        try {
            Customer customer = customerDao.findById(Long.parseLong(userId));
            if (customer == null) {
                sendResponse(exchange, 404, "{\"error\":\"Customer not found\"}");
                return;
            }
            Hibernate.initialize(customer.getFavoriteRestaurants());
            List<Restaurant> favorites = customer.getFavoriteRestaurants();
            List<RestaurantDto> favoriteDtos = favorites.stream()
                    .map(RestaurantService::convertToDto)
                    .collect(Collectors.toList());

            String jsonResponse = objectMapper.writeValueAsString(favoriteDtos);
            sendJsonResponse(exchange, 200, jsonResponse);
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }
    private void handleAddFavorite(HttpExchange exchange, String userId) throws IOException {
        try {
            String[] pathParts = exchange.getRequestURI().getPath().split("/");
            int restaurantId = Integer.parseInt(pathParts[2]);
            Customer customer = customerDao.findById(Long.parseLong(userId));
            if (customer == null) {
                sendResponse(exchange, 404, "{\"error\":\"Customer not found\"}");
                return;
            }
            Restaurant restaurant = RestaurantDao.findById((long) restaurantId);
            if (restaurant == null) {
                sendResponse(exchange, 404, "{\"error\":\"Restaurant not found\"}");
                return;
            }
            Hibernate.initialize(customer.getFavoriteRestaurants());
            if (!customer.getFavoriteRestaurants().contains(restaurant)) {
                customer.getFavoriteRestaurants().add(restaurant);
                customerDao.update(customer);
                sendResponse(exchange, 200, "{\"message\":\"Restaurant added to favorites\"}");
            } else {
                sendResponse(exchange, 200, "{\"message\":\"Restaurant already in favorites\"}");
            }

        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\":\"Invalid restaurant ID\"}");
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    private void handleRemoveFavorite(HttpExchange exchange, String userId) throws IOException {
        try {
            String[] pathParts = exchange.getRequestURI().getPath().split("/");
            int restaurantId = Integer.parseInt(pathParts[2]);

            Customer customer = customerDao.findById(Long.parseLong(userId));
            if (customer == null) {
                sendResponse(exchange, 404, "{\"error\":\"Customer not found\"}");
                return;
            }

            Hibernate.initialize(customer.getFavoriteRestaurants());
            boolean removed = customer.getFavoriteRestaurants().removeIf(r -> r.getId() == restaurantId);
            if (removed) {
                customerDao.update(customer);
                sendResponse(exchange, 200, "{\"message\":\"Restaurant removed from favorites\"}");
            } else {
                sendResponse(exchange, 404, "{\"error\":\"Restaurant not in favorites\"}");
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\":\"Invalid restaurant ID\"}");
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"Internal Server Error: " + e.getMessage() + "\"}");
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
        exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream ms = exchange.getResponseBody()) {
            ms.write(response.getBytes());
        }
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        sendResponse(exchange, statusCode, json);
    }
}