package org.FoodOrder.server.HttpHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.FoodOrder.server.DAO.CourierDao;
import org.FoodOrder.server.DAO.CustomerDao;
import org.FoodOrder.server.DAO.VendorDao;
import org.FoodOrder.server.enums.Role;
import org.FoodOrder.server.utils.HibernateUtil;
import org.FoodOrder.server.utils.JwtUtil;
import org.FoodOrder.server.utils.PasswordUtil;
import org.FoodOrder.server.models.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.mindrot.jbcrypt.BCrypt;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class AuthHandler implements HttpHandler {
    private ObjectMapper objectMapper;
    private SessionFactory sessionFactory;
    private JwtUtil jwtUtil;
    private static AuthHandler instance;
    private static CustomerDao customerDao = new CustomerDao();
    private static VendorDao vendorDao = new VendorDao();
    private static CourierDao courierDao = new CourierDao();

    public static AuthHandler getInstance() {
        if (instance == null) instance = new AuthHandler();
        return instance;
    }

    public AuthHandler() {
        this.objectMapper = new ObjectMapper();
        this.sessionFactory = HibernateUtil.getSessionFactory();
        this.jwtUtil = new JwtUtil();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            switch (path) {
                case "/auth/register":
                    if ("POST".equals(method)) {
                        handleRegister(exchange);
                    } else {
                        sendMethodNotAllowed(exchange);
                    }
                    break;
                case "/auth/login":
                    if ("POST".equals(method)) {
                        handleLogin(exchange);
                    } else {
                        sendMethodNotAllowed(exchange);
                    }
                    break;
                case "/auth/profile":
                    if ("GET".equals(method)) {
                        handleGetProfile(exchange);
                    } else if ("PUT".equals(method)) {
                        handleUpdateProfile(exchange);
                    } else {
                        sendMethodNotAllowed(exchange);
                    }
                    break;
                case "/auth/logout":
                    if ("POST".equals(method)) {
                        handleLogout(exchange);
                    } else {
                        sendMethodNotAllowed(exchange);
                    }
                    break;
                default:
                    sendNotFound(exchange);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendInternalServerError(exchange, "Internal server error: " + e.getMessage());
        }
    }

    public void handleRegister(HttpExchange exchange) throws IOException {
        try {
            String requestBody = getRequestBody(exchange);
            JsonNode jsonNode = objectMapper.readTree(requestBody);
            String[] requiredFields = {"full_name", "phone", "password", "role", "address"};
            for (String field : requiredFields) {
                if (!jsonNode.has(field) || jsonNode.get(field).asText().isEmpty()) {
                    sendBadRequest(exchange, "Missing required field: " + field);
                    return;
                }
            }
            String fullName = jsonNode.get("full_name").asText();
            String phone = jsonNode.get("phone").asText();
            String email = jsonNode.has("email") ? jsonNode.get("email").asText() : null;
            String password = jsonNode.get("password").asText();
            String role = jsonNode.get("role").asText();
            String address = jsonNode.get("address").asText();
            BankInfo bankInfo = null;
            if (jsonNode.has("bank_info")) {
                JsonNode bankInfoNode = jsonNode.get("bank_info");
                String bankName = bankInfoNode.has("bank_name") ? bankInfoNode.get("bank_name").asText() : null;
                String accountNumber = bankInfoNode.has("account_number") ? bankInfoNode.get("account_number").asText() : null;

                if (bankName != null && accountNumber != null) {
                    bankInfo = new BankInfo(bankName, accountNumber);
                }
            }
            String profileImageBase64 = jsonNode.has("profileImageBase64") ? jsonNode.get("profileImageBase64").asText() : null;
            if (!role.equals("buyer") && !role.equals("seller") && !role.equals("courier")) {
                sendBadRequest(exchange, "Invalid role. Must be buyer, seller, or courier");
                return;
            }

            Session session = sessionFactory.openSession();
            Transaction transaction = session.beginTransaction();

            try {
                Query<User> query = session.createQuery("FROM User u WHERE u.phone = :phone", User.class);
                query.setParameter("phone", phone);
                User existingUser = query.uniqueResult();

                if (existingUser != null) {
                    sendConflict(exchange, "Phone number already exists");
                    return;
                }
                User user;
                if (role.equals("buyer")) {
                    user = new Customer();
                } else if (role.equals("seller")) {
                    user = new Vendor();
                } else {
                    user = new Courier();
                }
                if (email != null) {
                    user.setEmail(email);
                }
                if (profileImageBase64 != null) {
                    user.setProfileImageBase64(profileImageBase64);
                }
                if (bankInfo != null) {
                    user.setBankInfo(bankInfo);
                }
                user.setFullName(fullName);
                user.setPhone(phone);
                user.setPassword(PasswordUtil.hashPassword(password));
                user.setWalletBalance(BigDecimal.ZERO);
                user.setAddress(address);

                session.save(user);
                transaction.commit();
                String token = jwtUtil.generateToken(String.valueOf(user.getId()), user.getRole().toString());
                Map<String, Object> response = new HashMap<>();
                response.put("message", "User registered successfully");
                response.put("user_id", user.getId());
                response.put("token", token);
                sendJsonResponse(exchange, 200, response);

            } catch (Exception e) {
                transaction.rollback();
                throw e;
            } finally {
                session.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendInternalServerError(exchange, "Internal server error: " + e.getMessage());
        }
    }

    public void handleLogin(HttpExchange exchange) throws IOException {
        try {
            String requestBody = getRequestBody(exchange);
            JsonNode jsonNode = objectMapper.readTree(requestBody);

            String phone = jsonNode.get("phone").asText();
            String password = jsonNode.get("password").asText();

            if (phone == null || password == null || phone.isEmpty() || password.isEmpty()) {
                sendBadRequest(exchange, "Phone and password are required");
                return;
            }

            Session session = sessionFactory.openSession();
            Transaction transaction = session.beginTransaction();

            try {
                // بررسی لاگین ادمین
                if ("admin".equals(phone) && "admin".equals(password)) {
                    // بررسی وجود هر کاربری با phone = "admin"
                    Query<User> query = session.createQuery("FROM User u WHERE u.phone = :phone", User.class);
                    query.setParameter("phone", phone);
                    User existingUser = query.uniqueResult();

                    User adminUser;
                    if (existingUser != null) {
                        // اگر کاربر با phone = "admin" وجود دارد
                        if (existingUser.getRole() != Role.ADMIN) {
                            // اگر نقش ADMIN نیست، نقش را به ADMIN تغییر می‌دهیم
                            existingUser.setRole(Role.ADMIN);
                            session.update(existingUser);
                        }
                        adminUser = existingUser;
                    } else {
                        // ایجاد کاربر ادمین جدید
                        adminUser = new User();
                        adminUser.setFullName("Administrator");
                        adminUser.setPhone("admin");
                        adminUser.setPassword(PasswordUtil.hashPassword("admin"));
                        adminUser.setRole(Role.ADMIN);
                        adminUser.setWalletBalance(BigDecimal.ZERO);
                        adminUser.setAddress("Admin Office");
                        session.save(adminUser);
                    }

                    String token = jwtUtil.generateToken(String.valueOf(adminUser.getId()), Role.ADMIN.toString());
                    Map<String, Object> response = new HashMap<>();
                    response.put("message", "Admin logged in successfully");
                    response.put("token", token);
                    response.put("user", userToMap(adminUser));
                    transaction.commit();
                    sendJsonResponse(exchange, 200, response);
                    return;
                }

                // لاگین کاربران عادی
                Query<User> query = session.createQuery("FROM User u WHERE u.phone = :phone", User.class);
                query.setParameter("phone", phone);
                User user = query.uniqueResult();

                if (user == null || !verifyPassword(password, user.getPassword())) {
                    sendUnauthorized(exchange, "Invalid phone or password");
                    return;
                }

                String token = jwtUtil.generateToken(String.valueOf(user.getId()), user.getRole().toString());
                Map<String, Object> response = new HashMap<>();
                response.put("message", "User logged in successfully");
                response.put("token", token);
                response.put("user", userToMap(user));
                transaction.commit();
                sendJsonResponse(exchange, 200, response);
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            } finally {
                session.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendInternalServerError(exchange, "Internal server error: " + e.getMessage());
        }
    }

    public void handleGetProfile(HttpExchange exchange) throws IOException {
        try {
            String userId = authenticateUser(exchange);
            if (userId == null) {
                sendUnauthorized(exchange, "Authentication required");
                return;
            }
            Session session = sessionFactory.openSession();
            try {
                User user = session.get(User.class, userId);
                if (user == null) {
                    sendNotFound(exchange);
                    return;
                }
                sendJsonResponse(exchange, 200, userToMap(user));
            } finally {
                session.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendInternalServerError(exchange, "Internal server error: " + e.getMessage());
        }
    }

    public void handleUpdateProfile(HttpExchange exchange) throws IOException {
        try {
            String userId = authenticateUser(exchange);
            if (userId == null) {
                sendUnauthorized(exchange, "Authentication required");
                return;
            }

            String requestBody = getRequestBody(exchange);
            JsonNode jsonNode = objectMapper.readTree(requestBody);

            Session session = sessionFactory.openSession();
            Transaction transaction = session.beginTransaction();

            try {
                User user = session.get(User.class, userId);
                if (user == null) {
                    sendNotFound(exchange);
                    return;
                }
                if (jsonNode.has("full_name")) {
                    user.setFullName(jsonNode.get("full_name").asText());
                }
                if (jsonNode.has("email")) {
                    user.setEmail(jsonNode.get("email").asText());
                }
                if (jsonNode.has("address")) {
                    user.setAddress(jsonNode.get("address").asText());
                }
                if (jsonNode.has("profileImageBase64")) {
                    user.setProfileImageBase64(jsonNode.get("profileImageBase64").asText());
                }
                if (jsonNode.has("bank_name") && user.getBankInfo() != null) {
                    user.getBankInfo().setBankName(jsonNode.get("bank_name").asText());
                }
                if (jsonNode.has("account_number") && user.getBankInfo() != null) {
                    user.getBankInfo().setAccountNumber(jsonNode.get("account_number").asText());
                }

                session.update(user);
                transaction.commit();

                Map<String, String> response = new HashMap<>();
                response.put("message", "Profile updated successfully");
                sendJsonResponse(exchange, 200, response);

            } catch (Exception e) {
                transaction.rollback();
                throw e;
            } finally {
                session.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendInternalServerError(exchange, "Internal server error: " + e.getMessage());
        }
    }

    public void handleLogout(HttpExchange exchange) throws IOException {
        try {
            String userId = authenticateUser(exchange);
            if (userId == null) {
                sendUnauthorized(exchange, "Authentication required");
                return;
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "User logged out successfully");
            sendJsonResponse(exchange, 200, response);

        } catch (Exception e) {
            e.printStackTrace();
            sendInternalServerError(exchange, "Internal server error: " + e.getMessage());
        }
    }

    private String authenticateUser(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7);
        return jwtUtil.validateToken(token);
    }

    private String getRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        return baos.toString(StandardCharsets.UTF_8);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    private boolean verifyPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }

    private Map<String, Object> userToMap(User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("full_name", user.getFullName());
        userMap.put("phone", user.getPhone());
        userMap.put("email", user.getEmail());
        userMap.put("role", user.getRole().toString().toLowerCase());
        userMap.put("address", user.getAddress());
        userMap.put("profileImageBase64", user.getProfileImageBase64());
        if (user.getBankInfo() != null) {
            userMap.put("bank_name", user.getBankInfo().getBankName());
            userMap.put("account_number", user.getBankInfo().getAccountNumber());
        }
        return userMap;
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, Object response) throws IOException {
        String jsonResponse = objectMapper.writeValueAsString(response);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, jsonResponse.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
        }
    }

    private void sendBadRequest(HttpExchange exchange, String message) throws IOException {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        sendJsonResponse(exchange, 400, error);
    }

    private void sendUnauthorized(HttpExchange exchange, String message) throws IOException {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        sendJsonResponse(exchange, 401, error);
    }

    private void sendConflict(HttpExchange exchange, String message) throws IOException {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        sendJsonResponse(exchange, 409, error);
    }

    private void sendNotFound(HttpExchange exchange) throws IOException {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Resource not found");
        sendJsonResponse(exchange, 404, error);
    }

    private void sendMethodNotAllowed(HttpExchange exchange) throws IOException {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Method not allowed");
        sendJsonResponse(exchange, 405, error);
    }

    private void sendInternalServerError(HttpExchange exchange, String message) throws IOException {
        Map<String, String> error = new HashMap<>();
        error.put("error", message != null ? message : "Internal server error");
        sendJsonResponse(exchange, 500, error);
    }
}