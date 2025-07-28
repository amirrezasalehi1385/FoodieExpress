//package org.FoodOrder.server.HttpHandler;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.google.gson.Gson;
//import com.sun.net.httpserver.HttpExchange;
//import com.sun.net.httpserver.HttpHandler;
//import org.FoodOrder.server.DAO.OrderDao;
//import org.FoodOrder.server.DAO.TransactionDao;
//import org.FoodOrder.server.DAO.UserDao;
//import org.FoodOrder.server.enums.TransactionMethod;
//import org.FoodOrder.server.enums.TransactionStatus;
//import org.FoodOrder.server.enums.TransactionType;
//import org.FoodOrder.server.models.Order;
//import org.FoodOrder.server.models.Transaction;
//import org.FoodOrder.server.models.User;
//import org.FoodOrder.server.utils.HibernateUtil;
//import org.FoodOrder.server.utils.JwtUtil;
//import org.hibernate.Session;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
//import java.math.BigDecimal;
//import java.nio.charset.StandardCharsets;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//public class TransactionHandler implements HttpHandler {
//    private final ObjectMapper objectMapper;
//    private final Gson gson;
//    private final JwtUtil jwtUtil;
//    private final UserDao userDao;
//    private final OrderDao orderDao;
//    private final TransactionDao transactionDao;
//    private static TransactionHandler instance;
//
//    public static TransactionHandler getInstance() {
//        if (instance == null) {
//            instance = new TransactionHandler();
//        }
//        return instance;
//    }
//
//    private TransactionHandler() {
//        this.objectMapper = new ObjectMapper();
//        this.gson = new Gson();
//        this.jwtUtil = new JwtUtil();
//        this.userDao = new UserDao();
//        this.orderDao = new OrderDao();
//        this.transactionDao = new TransactionDao();
//    }
//
//    @Override
//    public void handle(HttpExchange exchange) throws IOException {
//        try {
//            String path = exchange.getRequestURI().getPath();
//            String method = exchange.getRequestMethod();
//            String token = extractToken(exchange);
//
//            if (token == null || jwtUtil.validateToken(token) == null) {
//                sendResponse(exchange, 401, "{\"error\":\"Unauthorized\"}");
//                return;
//            }
//
//            String userId = jwtUtil.validateToken(token);
//
//            if (path.equals("/transactions") && "GET".equals(method)) {
//                handleGetTransactions(exchange, userId);
//            }else {
//                sendResponse(exchange, 404, "{\"error\":\"Not Found\"}");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            sendResponse(exchange, 500, "{\"error\":\"Internal Server Error: " + e.getMessage() + "\"}");
//        }
//    }
//
//    private void handleGetTransactions(HttpExchange exchange, String userId) throws IOException {
//        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
//            List<org.FoodOrder.server.models.Transaction> transactions = transactionDao.findByUser(Long.parseLong(userId));
//            List<Map<String, Object>> transactionDtos = transactions.stream().map(t -> {
//                Map<String, Object> dto = new HashMap<>();
//                dto.put("id", t.getId());
//                dto.put("userId", t.getUser().getId());
//                dto.put("orderId", t.getOrder() != null ? t.getOrder().getId() : null);
//                dto.put("amount", t.getAmount());
//                dto.put("type", t.getType().toString());
//                dto.put("method", t.getMethod() != null ? t.getMethod().toString() : null);
//                dto.put("status", t.getStatus().toString());
//                dto.put("createdAt", t.getCreatedAt().toString());
//                return dto;
//            }).collect(Collectors.toList());
//
//            String jsonResponse = objectMapper.writeValueAsString(transactionDtos);
//            sendResponse(exchange, 200, jsonResponse);
//        } catch (Exception e) {
//            sendResponse(exchange, 500, "{\"error\":\"Failed to fetch transactions: " + e.getMessage() + "\"}");
//        }
//    }
//
//    private String extractToken(HttpExchange exchange) {
//        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
//        if (authHeader != null && authHeader.startsWith("Bearer ")) {
//            return authHeader.substring(7);
//        }
//        return null;
//    }
//
//    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
//        exchange.getResponseHeaders().set("Content-Type", "application/json");
//        exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
//        OutputStream os = exchange.getResponseBody();
//        os.write(response.getBytes(StandardCharsets.UTF_8));
//        os.close();
//    }
//
//    private String readRequestBody(HttpExchange exchange) throws IOException {
//        StringBuilder sb = new StringBuilder();
//        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                sb.append(line);
//            }
//        }
//        return sb.toString();
//    }
//}