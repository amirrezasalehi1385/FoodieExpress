package org.FoodOrder.server.HttpHandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.FoodOrder.server.DAO.CouponDao;
import org.FoodOrder.server.DAO.OrderDao;
import org.FoodOrder.server.DAO.RestaurantDao;
import org.FoodOrder.server.DAO.UserDao;
import org.FoodOrder.server.DTO.OrderDto;
import org.FoodOrder.server.DTO.RestaurantDto;
import org.FoodOrder.server.DTO.UserDto;
import org.FoodOrder.server.enums.ApprovalStatus;
import org.FoodOrder.server.enums.CouponType;
import org.FoodOrder.server.enums.Role;
import org.FoodOrder.server.models.Coupon;
import org.FoodOrder.server.models.Order;
import org.FoodOrder.server.models.Restaurant;
import org.FoodOrder.server.models.User;
import org.FoodOrder.server.service.OrderService;
import org.FoodOrder.server.service.RestaurantService;
import org.FoodOrder.server.service.UserService;
import org.FoodOrder.server.utils.HibernateUtil;
import org.FoodOrder.server.utils.JwtUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminHandler implements HttpHandler {

    private final Gson gson = new Gson();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SessionFactory sessionFactory;
    private final JwtUtil jwtUtil;
    private static AdminHandler instance;
    private final CouponDao couponDao = new CouponDao();

    public static AdminHandler getInstance() {
        if (instance == null) instance = new AdminHandler();
        return instance;
    }

    public AdminHandler() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
        this.jwtUtil = new JwtUtil();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            if (path.matches("/admin/users$") && "GET".equals(method)) {
                handleListUsers(exchange);
            } else if (path.matches("/admin/users/\\d+/status$") && "PUT".equals(method)) {
                handleUpdateUserStatus(exchange);
            } else if (path.matches("/admin/orders$") && "GET".equals(method)) {
                handleListOrders(exchange);
            } else if (path.equals("/admin/discounts") && "POST".equals(method)) {
                handleCreateDiscount(exchange);
            } else if (path.matches("/admin/discounts/\\d+") && "DELETE".equals(method)) {
                handleDeleteDiscount(exchange);
            } else if (path.equals("/admin/restaurants") && "GET".equals(method)) {
                handleListRestaurants(exchange);
            } else if (path.matches("/admin/restaurants/\\d+") && "DELETE".equals(method)) {
                handleDeleteRestaurant(exchange);
            } else {
                sendResponse(exchange, 404, "{\"error\":\"Not Found\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    private void handleListRestaurants(HttpExchange exchange) throws IOException {
        try (Session session = sessionFactory.openSession()) {
            List<Restaurant> restaurants = session.createQuery(
                    "SELECT DISTINCT r FROM Restaurant r JOIN FETCH r.seller",
                    Restaurant.class
            ).list();

            if (restaurants == null || restaurants.isEmpty()) {
                sendResponse(exchange, 404, "{\"error\":\"No restaurants found\"}");
                return;
            }

            List<RestaurantDto> restaurantDtos = RestaurantService.convertToDtoList(restaurants);
            String jsonResponse = objectMapper.writeValueAsString(restaurantDtos);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    private void handleDeleteRestaurant(HttpExchange exchange) throws IOException {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            String[] pathParts = exchange.getRequestURI().getPath().split("/");
            Long restaurantId = Long.parseLong(pathParts[3]);

            Restaurant restaurant = RestaurantDao.findById(restaurantId);
            if (restaurant == null) {
                sendResponse(exchange, 404, "{\"error\":\"Restaurant not found\"}");
                return;
            }

            RestaurantDao.delete(restaurantId);
            tx.commit();
            sendResponse(exchange, 200, "{\"message\":\"Restaurant deleted successfully\"}");
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\":\"Invalid restaurant ID\"}");
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    private void handleCreateDiscount(HttpExchange exchange) throws IOException {
        try {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();

            String type = jsonObject.get("type").getAsString();
            Long value = jsonObject.get("value").getAsLong();
            String scope = jsonObject.get("scope").getAsString();
            String startDate = jsonObject.get("startDate").getAsString();
            String endDate = jsonObject.get("endDate").getAsString();

            Coupon coupon = new Coupon();
            coupon.setCouponCode("COUPON-" + System.currentTimeMillis()); // نمونه کد کوپن
            coupon.setType(CouponType.valueOf(type.toUpperCase()));
            coupon.setValue(value);
            coupon.setMinPrice(0);
            coupon.setUserCount(0);

            couponDao.save(coupon);
            sendResponse(exchange, 201, gson.toJson(coupon));
        } catch (Exception e) {
            sendResponse(exchange, 400, "Bad Request: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleListUsers(HttpExchange exchange) throws IOException {
        try {
            UserDao userDao = new UserDao();
            List<User> users = userDao.getAll();
            if (users == null) {
                sendResponse(exchange, 404, "{\"error\":\"No users found\"}");
                return;
            }

            List<User> filteredUsers = users.stream()
                    .filter(user -> user.getRole() != Role.ADMIN)
                    .collect(Collectors.toList());

            List<UserDto> userDtos = UserService.convertToDtoList(filteredUsers);
            String jsonResponse = objectMapper.writeValueAsString(userDtos);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    private void handleUpdateUserStatus(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");
            if (parts.length < 4) {
                sendResponse(exchange, 400, "{\"error\":\"Invalid user ID\"}");
                return;
            }
            Long userId;
            try {
                userId = Long.parseLong(parts[3]);
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "{\"error\":\"Invalid user ID format\"}");
                return;
            }

            try (InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
                Map<String, String> requestBody = objectMapper.readValue(isr, new TypeReference<Map<String, String>>() {});
                String status = requestBody.get("status");
                if (status == null || (!status.equals("approved") && !status.equals("rejected"))) {
                    sendResponse(exchange, 400, "{\"error\":\"Invalid status. Must be 'approved' or 'rejected'\"}");
                    return;
                }

                UserDao userDao = new UserDao();
                User user = userDao.findById(userId);
                if (user == null) {
                    sendResponse(exchange, 404, "{\"error\":\"User not found\"}");
                    return;
                }

                ApprovalStatus approvalStatus = status.equals("approved") ? ApprovalStatus.APPROVED : ApprovalStatus.REJECTED;
                user.setApprovalStatus(approvalStatus);
                userDao.update(user);

                sendResponse(exchange, 200, "{\"message\":\"Status updated\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    private void handleListOrders(HttpExchange exchange) throws IOException {
        try {
            Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());
            String search = queryParams.get("search");
            String vendor = queryParams.get("vendor");
            String courier = queryParams.get("courier");
            String customer = queryParams.get("customer");
            String status = queryParams.get("status");

            OrderDao orderDao = new OrderDao();
            List<Order> orders = orderDao.findHistoryForAdmin(search, vendor, courier, customer, status);
            if (orders == null || orders.isEmpty()) {
                sendResponse(exchange, 404, "{\"error\":\"No orders found\"}");
                return;
            }

            List<OrderDto> orderDtos = OrderService.convertToDtoList(orders);
            String jsonResponse = objectMapper.writeValueAsString(orderDtos);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null && !query.isEmpty()) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return params;
    }

    private void handleDeleteDiscount(HttpExchange exchange) throws IOException {
        try {
            String[] pathParts = exchange.getRequestURI().getPath().split("/");
            int couponId = Integer.parseInt(pathParts[3]);

            if (!couponDao.existsById(couponId)) {
                sendResponse(exchange, 404, "Discount not found");
                return;
            }

            couponDao.deleteById(couponId);
            sendResponse(exchange, 200, "{\"message\":\"Discount deleted successfully\"}");
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "Bad Request: Invalid discount ID");
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
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }
}