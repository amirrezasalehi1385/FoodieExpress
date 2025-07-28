package org.FoodOrder.server.HttpHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.FoodOrder.server.DAO.CartDao;
import org.FoodOrder.server.DAO.CustomerDao;
import org.FoodOrder.server.DTO.CartDto;
import org.FoodOrder.server.DTO.FoodItemDto;
import org.FoodOrder.server.models.Cart;
import org.FoodOrder.server.models.CartItem;
import org.FoodOrder.server.models.Customer;
import org.FoodOrder.server.models.FoodItem;
import org.FoodOrder.server.service.CartService;
import org.FoodOrder.server.service.FoodItemService;
import org.FoodOrder.server.utils.JwtUtil;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class CartHandler implements HttpHandler {
    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;
    private final CartDao cartDao;
    private static CartHandler instance;
    private final SessionFactory sessionFactory;

    public static CartHandler getInstance() {
        if (instance == null) {
            instance = new CartHandler();
        }
        return instance;
    }

    public CartHandler() {
        this.objectMapper = new ObjectMapper();
        this.jwtUtil = new JwtUtil();
        this.sessionFactory = new Configuration().configure().buildSessionFactory();
        this.cartDao = new CartDao();
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

            if (path.equals("/cart") && "GET".equals(method)) {
                handleGetCart(exchange, userId);
            } else if (path.equals("/cart/add") && "POST".equals(method)) {
                handleAddItem(exchange, userId);
            } else if (path.equals("/cart/remove") && "POST".equals(method)) {
                handleRemoveItem(exchange, userId);
            } else if (path.equals("/cart/clear") && "POST".equals(method)) {
                handleClearCart(exchange, userId);
            } else {
                sendResponse(exchange, 404, "Not Found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void handleGetCart(HttpExchange exchange, String userId) throws IOException {
        try {
            Cart cart = cartDao.findByCustomerId(Long.parseLong(userId));
            if (cart == null) {
                sendResponse(exchange, 404, "Cart not found");
                return;
            }

            try (var session = sessionFactory.openSession()) {
                session.beginTransaction();
                cart = session.get(Cart.class, cart.getId());
                Hibernate.initialize(cart.getItems());
                session.getTransaction().commit();
                CartDto cartDto = CartService.convertToCartDto(cart);
                String jsonResponse = objectMapper.writeValueAsString(cartDto);
                sendResponse(exchange, 200, jsonResponse);
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "Server error: " + e.getMessage());
        }
    }

    private void handleAddItem(HttpExchange exchange, String userId) throws IOException {
        try {
            String requestBody = readRequestBody(exchange);
            Map<String, Object> itemData = objectMapper.readValue(requestBody, Map.class);
            Long foodId = Long.valueOf(itemData.get("foodId").toString());
            Integer quantity = itemData.get("quantity") != null ? Integer.parseInt(itemData.get("quantity").toString()) : 0;
            if (quantity == 0) {
                sendResponse(exchange, 400, "Quantity cannot be zero or missing");
                return;
            }
            CustomerDao customerDao = new CustomerDao();
            Customer customer = customerDao.findById(Long.parseLong(userId));
            System.out.println("Looking for cart with userId: " + customer.getId());
            Cart cart = cartDao.findByCustomerId(customer.getId());
            if (cart == null) {
                System.out.println("Cart not found, creating new cart for userId: " + customer.getId());
                cart = new Cart();
                cart.setCustomer(customer);
                cartDao.save(cart);
            }

            try (var session = sessionFactory.openSession()) {
                session.beginTransaction();
                cart = session.get(Cart.class, cart.getId());
                Hibernate.initialize(cart.getItems());
                FoodItem food = session.get(FoodItem.class, foodId);
                if (food == null) {
                    sendResponse(exchange, 404, "Food item not found");
                    return;
                }
                cart.addItem(food, quantity);
                session.update(cart);
                session.getTransaction().commit();

                CartDto cartDto = CartService.convertToCartDto(cart);
                String jsonResponse = objectMapper.writeValueAsString(cartDto);
                sendResponse(exchange, 200, jsonResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Server error: " + e.getMessage());
        }
    }

    private void handleRemoveItem(HttpExchange exchange, String userId) throws IOException {
        try {
            String requestBody = readRequestBody(exchange);
            Map<String, Object> itemData = objectMapper.readValue(requestBody, Map.class);
            Long foodId = Long.valueOf(itemData.get("foodId").toString());

            try (var session = sessionFactory.openSession()) {
                session.beginTransaction();

                // پیدا کردن سبد خرید
                Cart cart = session.createQuery("FROM Cart WHERE customer.id = :customerId", Cart.class)
                        .setParameter("customerId", Long.parseLong(userId))
                        .uniqueResult();
                if (cart == null) {
                    sendResponse(exchange, 404, "Cart not found");
                    return;
                }

                // لود آیتم‌های سبد خرید
                Hibernate.initialize(cart.getItems());

                // پیدا کردن آیتم سبد خرید برای حذف
                CartItem itemToRemove = cart.getItems().stream()
                        .filter(item -> item.getFood().getId() == foodId)
                        .findFirst()
                        .orElse(null);
                if (itemToRemove == null) {
                    sendResponse(exchange, 404, "Item not found in cart");
                    return;
                }

                // حذف آیتم از لیست و دیتابیس
                cart.getItems().remove(itemToRemove);
                session.remove(itemToRemove); // حذف مستقیم CartItem از دیتابیس
                session.update(cart); // به‌روزرسانی Cart در دیتابیس

                session.getTransaction().commit();

                CartDto cartDto = CartService.convertToCartDto(cart);
                String jsonResponse = objectMapper.writeValueAsString(cartDto);
                sendResponse(exchange, 200, jsonResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Server error: " + e.getMessage());
        }
    }


    private void handleClearCart(HttpExchange exchange, String userId) throws IOException {
        try {
            cartDao.removeByCustomerId(Long.parseLong(userId));
            sendResponse(exchange, 200, "Cart cleared");
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