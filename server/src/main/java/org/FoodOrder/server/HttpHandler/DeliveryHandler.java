package org.FoodOrder.server.HttpHandler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.FoodOrder.server.DAO.OrderDao;
import org.FoodOrder.server.controllers.DeliveryController;
import org.FoodOrder.server.DTO.OrderDto;
import org.FoodOrder.server.models.Order;
import org.FoodOrder.server.service.OrderService;
import org.FoodOrder.server.utils.JwtUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DeliveryHandler implements HttpHandler {
    private final Gson gson = new Gson();
    private final JwtUtil jwtUtil = new JwtUtil();
    private static DeliveryHandler instance;

    public static DeliveryHandler getInstance() {
        if (instance == null) instance = new DeliveryHandler();
        return instance;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            String token = extractToken(exchange);
            String userId = jwtUtil.validateToken(token);
            if (token == null || userId == null) {
                sendResponse(exchange, 401, gson.toJson(Map.of("error", "Unauthorized")));
                return;
            }
            if (path.equals("/deliveries/available") && "GET".equals(method)) {
                handleGetAvailableDeliveries(exchange, userId);
            } else if (path.matches("/deliveries/\\d+") && "PUT".equals(method)) {
                handleUpdateDeliveryStatus(exchange, userId);
            } else if (path.equals("/deliveries/history") && "GET".equals(method)) {
                handleGetDeliveryHistory(exchange, userId);
            } else {
                sendResponse(exchange, 404, gson.toJson(Map.of("error", "Not Found")));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, gson.toJson(Map.of("error", "Internal Server Error: " + e.getMessage())));
        }
    }

    private void handleGetAvailableDeliveries(HttpExchange exchange, String userId) throws IOException {
        try {
            List<Order> orders = DeliveryController.getAvailableDeliveries();
            List<OrderDto> orderDtos = orders.stream()
                    .map(OrderService::convertToDto)
                    .collect(Collectors.toList());

            String jsonResponse = gson.toJson(orderDtos);
            sendResponse(exchange, 200, jsonResponse);
        } catch (org.FoodOrder.server.exceptions.NotFoundException e) {
            sendResponse(exchange, 404, gson.toJson(Map.of("error", e.getMessage())));
        } catch (Exception e) {
            sendResponse(exchange, 500, gson.toJson(Map.of("error", "Internal Server Error: " + e.getMessage())));
        }
    }

    private void handleUpdateDeliveryStatus(HttpExchange exchange, String userId) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            Long orderId = Long.parseLong(path.substring(path.lastIndexOf('/') + 1));

            InputStream requestBody = exchange.getRequestBody();
            JsonObject requestData = JsonParser.parseString(new String(requestBody.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
            String newStatus = requestData.has("status") ? requestData.get("status").getAsString() : null;

            DeliveryController.updateDeliveryStatus(orderId, newStatus, Long.parseLong(userId));

            Order order = OrderDao.findById(orderId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Status changed successfully to '" +
                    ("accepted".equalsIgnoreCase(newStatus) ? "on the way" : "delivered") + "'");
            response.put("order", OrderService.convertToDto(order));

            sendResponse(exchange, 200, gson.toJson(response));
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, gson.toJson(Map.of("error", "Invalid delivery ID")));
        } catch (org.FoodOrder.server.exceptions.NotFoundException e) {
            sendResponse(exchange, 404, gson.toJson(Map.of("error", e.getMessage())));
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 403, gson.toJson(Map.of("error", "Invalid status change")));
        } catch (Exception e) {
            sendResponse(exchange, 500, gson.toJson(Map.of("error", "Internal Server Error: " + e.getMessage())));
        }
    }

    private void handleGetDeliveryHistory(HttpExchange exchange, String userId) throws IOException {
        try {
            Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());
            String search = queryParams.get("search");
            String vendor = queryParams.get("vendor");
            String user = queryParams.get("user");

            List<Order> orders = DeliveryController.getDeliveryHistory(Long.parseLong(userId), search, vendor, user);
            List<OrderDto> orderDtos = orders.stream()
                    .map(OrderService::convertToDto)
                    .collect(Collectors.toList());

            sendResponse(exchange, 200, gson.toJson(orderDtos));
        } catch (org.FoodOrder.server.exceptions.NotFoundException e) {
            sendResponse(exchange, 404, gson.toJson(Map.of("error", e.getMessage())));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, gson.toJson(Map.of("error", "Internal Server Error: " + e.getMessage())));
        }
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] keyValue = param.split("=", 2);
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return params;
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
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }
}
