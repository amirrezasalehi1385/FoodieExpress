package org.FoodOrder.server.HttpHandler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.FoodOrder.server.DAO.OrderDao;
import org.FoodOrder.server.DAO.ReviewDao;
import org.FoodOrder.server.DAO.UserDao;
import org.FoodOrder.server.models.Order;
import org.FoodOrder.server.models.Review;
import org.FoodOrder.server.models.Restaurant;
import org.FoodOrder.server.models.User;
import org.FoodOrder.server.utils.HibernateUtil;
import org.FoodOrder.server.utils.JwtUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RatingHandler implements HttpHandler {
    private final Gson gson = new Gson();
    private final SessionFactory sessionFactory;
    private final JwtUtil jwtUtil;
    private final OrderDao orderDao = new OrderDao();
    private final ReviewDao reviewDao = new ReviewDao();
    private final UserDao userDao = new UserDao();
    private static RatingHandler instance;

    public static RatingHandler getInstance() {
        if (instance == null) {
            instance = new RatingHandler();
        }
        return instance;
    }

    private RatingHandler() {
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
                sendResponse(exchange, 401, "{\"error\":\"Unauthorized\"}");
                return;
            }
            String userId = jwtUtil.validateToken(token);

            if (path.matches("/ratings$") && "POST".equals(method)) {
                handlePostRating(exchange, userId);
            } else if (path.matches("/ratings/\\d+$") && "GET".equals(method)) {
                handleGetRating(exchange, userId);
            } else {
                sendResponse(exchange, 404, "{\"error\":\"Not Found\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    private void handlePostRating(HttpExchange exchange, String userId) throws IOException {
        try {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();

            String orderIdStr = jsonObject.has("orderId") ? jsonObject.get("orderId").getAsString() : null;
            Integer rating = jsonObject.has("rating") ? jsonObject.get("rating").getAsInt() : null;
            String comment = jsonObject.has("comment") && !jsonObject.get("comment").isJsonNull()
                    ? jsonObject.get("comment").getAsString()
                    : "";
            if (orderIdStr == null || rating == null || rating < 1 || rating > 5) {
                sendResponse(exchange, 400, "{\"error\":\"Invalid input: orderId and rating (1-5) are required\"}");
                return;
            }
            Long orderId;
            try {
                orderId = Long.parseLong(orderIdStr);
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "{\"error\":\"Invalid orderId format\"}");
                return;
            }
            User user = userDao.findById(Long.valueOf(userId));
            if (user == null) {
                sendResponse(exchange, 401, "{\"error\":\"Invalid or expired token\"}");
                return;
            }

            Order order = OrderDao.findById(orderId);
            if (order == null) {
                sendResponse(exchange, 404, "{\"error\":\"Order not found\"}");
                return;
            }

            if (!order.getCustomer().getId().equals(Long.parseLong(userId))) {
                sendResponse(exchange, 403, "{\"error\":\"You can only rate your own orders\"}");
                return;
            }

            Review existingReview = reviewDao.findByOrderId(orderId);
            if (existingReview != null) {
                sendResponse(exchange, 400, "{\"error\":\"This order has already been rated\"}");
                return;
            }
            Review review = new Review();
            review.setOrder(order);
            review.setUser(user);
            review.setRating(rating);
            review.setComment(comment);
            review.setRestaurant(order.getRestaurant());

            Transaction transaction = null;
            try (Session session = sessionFactory.openSession()) {
                transaction = session.beginTransaction();
                session.save(review);
                updateRestaurantAverageRating(order.getRestaurant(), session); // فراخوانی داخل Session
                transaction.commit();
            } catch (Exception e) {
                if (transaction != null) transaction.rollback();
                sendResponse(exchange, 500, "{\"error\":\"Failed to save rating: " + e.getMessage() + "\"}");
                return;
            }
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", "Rating submitted successfully");
            sendResponse(exchange, 200, gson.toJson(responseBody));
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    private void handleGetRating(HttpExchange exchange, String userId) throws IOException {
        try {
            String[] pathParts = exchange.getRequestURI().getPath().split("/");
            Long orderId = Long.parseLong(pathParts[2]);

            // دریافت کاربر
            User user = userDao.findById(Long.valueOf(userId));
            if (user == null) {
                sendResponse(exchange, 401, "{\"error\":\"Invalid or expired token\"}");
                return;
            }

            // بررسی سفارش
            Order order = OrderDao.findById(orderId);
            if (order == null) {
                sendResponse(exchange, 404, "{\"error\":\"Order not found\"}");
                return;
            }

            // بررسی اینکه سفارش متعلق به کاربر است
            if (!order.getCustomer().getId().equals(Long.parseLong(userId))) {
                sendResponse(exchange, 403, "{\"error\":\"You can only view ratings for your own orders\"}");
                return;
            }

            // دریافت Review
            Review review = reviewDao.findByOrderId(orderId);
            if (review == null) {
                sendResponse(exchange, 404, "{\"error\":\"No rating found for this order\"}");
                return;
            }
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("rating", review.getRating());
            responseBody.put("comment", review.getComment());
            sendResponse(exchange, 200, gson.toJson(responseBody));
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\":\"Invalid orderId format\"}");
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    private void updateRestaurantAverageRating(Restaurant restaurant, Session session) {
        List<Review> reviews = session.createQuery("FROM Review r WHERE r.restaurant.id = :restaurantId", Review.class)
                .setParameter("restaurantId", restaurant.getId())
                .getResultList();
        if (!reviews.isEmpty()) {
            double sum = 0;
            for (Review review : reviews) {
                sum += review.getRating();
            }
            restaurant.setAverageRating(sum / reviews.size());
        } else {
            restaurant.setAverageRating(0.0);
        }
        session.merge(restaurant); // به جای session.update
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
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes(StandardCharsets.UTF_8));
        os.close();
    }
}