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
//public class WalletBalanceHandler implements HttpHandler {
//    private final ObjectMapper objectMapper;
//    private final Gson gson;
//    private final JwtUtil jwtUtil;
//    private final UserDao userDao;
//    private final OrderDao orderDao;
//    private final TransactionDao transactionDao;
//    private static WalletBalanceHandler instance;
//
//    public static WalletBalanceHandler getInstance() {
//        if (instance == null) {
//            instance = new WalletBalanceHandler();
//        }
//        return instance;
//    }
//
//    private WalletBalanceHandler() {
//        this.objectMapper = new ObjectMapper();
//        this.gson = new Gson();
//        this.jwtUtil = new JwtUtil();
//        this.userDao = new UserDao();
//        this.orderDao = new OrderDao();
//        this.transactionDao = new TransactionDao();
//    }
//    @Override
//    public void handle(HttpExchange exchange) throws IOException {
//        try {
//            String path = exchange.getRequestURI().getPath();
//            String method = exchange.getRequestMethod();
//            String token = extractToken(exchange);
//            if (token == null || jwtUtil.validateToken(token) == null) {
//                sendResponse(exchange, 401, "{\"error\":\"Unauthorized\"}");
//                return;
//            }
//            String userId = jwtUtil.validateToken(token);
//            if (path.equals("/wallet/balance") && "GET".equals(method)) {
//                handleGetWalletBalance(exchange, userId);
//            } else {
//                sendResponse(exchange, 404, "{\"error\":\"Not Found\"}");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            sendResponse(exchange, 500, "{\"error\":\"Internal Server Error: " + e.getMessage() + "\"}");
//        }
//    }
//
//
//
//    private void handleWalletTopUp(HttpExchange exchange, String userId) throws IOException {
//        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
//            String requestBody = readRequestBody(exchange);
//            Map<String, Object> topUpData = objectMapper.readValue(requestBody, Map.class);
//
//            String method = (String) topUpData.get("method");
//            Number amountNum = (Number) topUpData.get("amount");
//            if (method == null || !List.of("online", "card").contains(method.toLowerCase()) || amountNum == null || amountNum.doubleValue() <= 0) {
//                sendResponse(exchange, 400, "{\"error\":\"Invalid input: method (online/card) and positive amount are required\"}");
//                return;
//            }
//
//            BigDecimal amount = new BigDecimal(amountNum.toString());
//            User user = userDao.findById(Long.parseLong(userId));
//            if (user == null) {
//                sendResponse(exchange, 401, "{\"error\":\"Invalid user\"}");
//                return;
//            }
//
//            org.FoodOrder.server.models.Transaction transaction = null;
//            org.hibernate.Transaction hibernateTx = session.beginTransaction();
//            try {
//                user.setBalance(user.getBalance().add(amount));
//                session.update(user);
//
//                transaction = new org.FoodOrder.server.models.Transaction();
//                transaction.setUser(user);
//                transaction.setAmount(amount);
//                transaction.setType(TransactionType.TOP_UP);
//                transaction.setMethod(TransactionMethod.valueOf(method.toUpperCase()));
//                transaction.setStatus(TransactionStatus.SUCCESS);
//                transactionDao.save(transaction);
//
//                hibernateTx.commit();
//            } catch (Exception e) {
//                if (hibernateTx != null) hibernateTx.rollback();
//                sendResponse(exchange, 500, "{\"error\":\"Failed to process top-up: " + e.getMessage() + "\"}");
//                return;
//            }
//
//            Map<String, String> response = new HashMap<>();
//            response.put("message", "Wallet topped up successfully");
//            sendResponse(exchange, 200, objectMapper.writeValueAsString(response));
//        }
//    }
//
//    private void handleGetWalletBalance(HttpExchange exchange, String userId) throws IOException {
//        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
//            User user = userDao.findById(Long.parseLong(userId));
//            if (user == null) {
//                sendResponse(exchange, 401, "{\"error\":\"Invalid user\"}");
//                return;
//            }
//
//            Map<String, Object> response = new HashMap<>();
//            response.put("balance", user.getBalance());
//            sendResponse(exchange, 200, objectMapper.writeValueAsString(response));
//        } catch (Exception e) {
//            sendResponse(exchange, 500, "{\"error\":\"Failed to fetch wallet balance: " + e.getMessage() + "\"}");
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