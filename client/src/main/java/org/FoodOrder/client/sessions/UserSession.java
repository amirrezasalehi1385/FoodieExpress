package org.FoodOrder.client.sessions;

import org.FoodOrder.client.util.JWTController;
import org.FoodOrder.client.util.TokenEncryption;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class UserSession {
    private static String token = null;
    private static String role;
    private static final String TOKEN_FILE = "user_token.txt";

    public static void setToken(String t) {
        token = t;
        try {
            String encryptedToken = TokenEncryption.encrypt(t != null ? t : "");
            Files.writeString(Paths.get(TOKEN_FILE), encryptedToken, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to save encrypted token to file: " + e.getMessage());
        }
    }
    public static String getToken() {
        if (token == null) {
            try {
                if (Files.exists(Paths.get(TOKEN_FILE))) {
                    String encryptedToken = Files.readString(Paths.get(TOKEN_FILE), StandardCharsets.UTF_8).trim();
                    if (!encryptedToken.isEmpty()) {
                        token = TokenEncryption.decrypt(encryptedToken);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Failed to read or decrypt token from file: " + e.getMessage());
            }
        }
        return token;
    }
    public static boolean isLoggedIn() {
        String savedToken = getToken();
        if (savedToken == null || savedToken.isEmpty()) {
            return false;
        }

        String userId = JWTController.getSubjectFromJwt(savedToken);
        return userId != null;
    }
    public static void clear() {
        token = null;
        try {
            Files.writeString(Paths.get(TOKEN_FILE), "", StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to clear token file: " + e.getMessage());
        }
    }

    public static String getRole() {
        if (role == null && getToken() != null) {
            role = JWTController.extractRoleFromToken(getToken());
        }
        return role;
    }

    public static void setRole(String r) {
        role = r;
    }
}