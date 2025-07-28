package org.FoodOrder.server.HttpHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.FoodOrder.server.DTO.OrderDto;
import org.FoodOrder.server.controllers.OrderController;
import org.FoodOrder.server.exceptions.NotFoundException;
import org.FoodOrder.server.models.Order;
import org.FoodOrder.server.service.OrderService;
import org.FoodOrder.server.utils.JwtUtil;
import org.FoodOrder.server.utils.HibernateUtil;
import org.hibernate.SessionFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderHandler implements HttpHandler {
    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;
    private final Gson gson = new Gson();
    private SessionFactory sessionFactory;
    private static OrderHandler instance;

    public static OrderHandler getInstance() {
        if (instance == null) instance = new OrderHandler();
        return instance;
    }

    public OrderHandler() {
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
                return;
            }

            String userId = jwtUtil.validateToken(token);

            if (path.equals("/orders") && "POST".equals(method)) {
                handleSubmitOrder(exchange, userId);
            } else if (path.equals("/orders/history/") && "GET".equals(method)) {
                handleGetOrdersHistory(exchange, userId);
            } else {
                sendResponse(exchange, 404, "Not Found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void handleSubmitOrder(HttpExchange exchange, String userId) throws IOException {
        try {
            String requestBody = readRequestBody(exchange);
            Map<String, Object> orderData = objectMapper.readValue(requestBody, Map.class);

            Order order = OrderController.submitOrder(userId, orderData);

            String jsonResponse = objectMapper.writeValueAsString(OrderService.convertToDto(order));
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(jsonResponse.getBytes());
            os.close();
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "Invalid order: " + e.getMessage());
        } catch (Exception e) {
            sendResponse(exchange, 500, "Server error: " + e.getMessage());
        }
    }

    private void handleGetOrdersHistory(HttpExchange exchange, String userId) throws IOException {
        try {
            List<Order> orders = OrderController.getOrders(Long.parseLong(userId));
            List<OrderDto> orderDtos = orders.stream()
                    .map(OrderService::convertToDto)
                    .collect(Collectors.toList());

            String jsonResponse = objectMapper.writeValueAsString(orderDtos);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(jsonResponse.getBytes());
            os.close();
        } catch (NotFoundException e) {
            sendResponse(exchange, e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            sendResponse(exchange, 500, "Server error: " + e.getMessage());
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

    private String readRequestBody(HttpExchange exchange) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}