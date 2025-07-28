package org.FoodOrder.client.util;


import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Base64;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JWTController {
    private static String jwtKey;

    public static String getJwtKey() {
        return jwtKey;
    }

    public static void setJwtKey(String jwtKey) {
        JWTController.jwtKey = jwtKey;
    }

    public static void removeJwtKey() { JWTController.jwtKey = null; }

    public static String getSubjectFromJwt(String jwt) {
        try {
            String[] jwtParts = jwt.split("\\.");

            if (jwtParts.length == 3) {
                String encodedPayload = jwtParts[1];
                byte[] decodedPayload = Base64.getUrlDecoder().decode(encodedPayload);
                String payloadJson = new String(decodedPayload, StandardCharsets.UTF_8);
                String sub = payloadJson.substring(payloadJson.indexOf("\"sub\":") + 7);
                sub = sub.substring(0, sub.indexOf("\""));

                return sub;
            } else {
                throw new IllegalArgumentException("Invalid JWT format");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static String extractRoleFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;
            String payloadJson = new String(Base64.getDecoder().decode(parts[1]));
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> payloadMap = mapper.readValue(payloadJson, Map.class);
            return (String) payloadMap.get("role");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}