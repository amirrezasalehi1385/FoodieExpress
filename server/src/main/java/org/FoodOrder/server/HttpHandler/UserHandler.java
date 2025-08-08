package org.FoodOrder.server.HttpHandler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.FoodOrder.server.DAO.UserDao;
import org.FoodOrder.server.DTO.UserDto;
import org.FoodOrder.server.enums.Role;
import org.FoodOrder.server.exceptions.NotFoundException;
import org.FoodOrder.server.models.User;
import org.FoodOrder.server.service.UserService;
import org.FoodOrder.server.utils.JwtUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class UserHandler implements HttpHandler {
    private final Gson gson = new Gson();
    private final JwtUtil jwtUtil = new JwtUtil();
    private final UserDao userDao = new UserDao();
    private static UserHandler instance;

    public static UserHandler getInstance() {
        if (instance == null) instance = new UserHandler();
        return instance;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            String token = extractToken(exchange);
            String requestingUserId = jwtUtil.validateToken(token);

            if (token == null || requestingUserId == null) {
                sendResponse(exchange, 401, gson.toJson(Map.of("error", "Unauthorized")));
                return;
            }

            if (path.matches("/user/\\d+") && "GET".equals(method)) {
                handleGetUserById(exchange, requestingUserId, path);
            } else {
                sendResponse(exchange, 404, gson.toJson(Map.of("error", "Not Found")));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, gson.toJson(Map.of("error", "Internal Server Error: " + e.getMessage())));
        }
    }

    private void handleGetUserById(HttpExchange exchange, String requestingUserId, String path) throws IOException {
        try {
            Long userId = Long.parseLong(path.substring(path.lastIndexOf('/') + 1));

            User requestingUser = userDao.findById(Long.parseLong(requestingUserId));
            if (requestingUser == null || (!requestingUser.getId().equals(userId) && requestingUser.getRole() != Role.ADMIN)) {
                sendResponse(exchange, 403, gson.toJson(Map.of("error", "Forbidden")));
                return;
            }

            User user = userDao.findById(userId);
            if (user == null) {
                throw new NotFoundException("User not found", 404);
            }

            UserDto userDto = UserService.convertToDto(user);
            String jsonResponse = gson.toJson(userDto);
            sendResponse(exchange, 200, jsonResponse);
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, gson.toJson(Map.of("error", "Invalid user ID")));
        } catch (NotFoundException e) {
            sendResponse(exchange, e.getStatusCode(), gson.toJson(Map.of("error", e.getMessage())));
        } catch (Exception e) {
            sendResponse(exchange, 500, gson.toJson(Map.of("error", "Internal Server Error: " + e.getMessage())));
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