package org.FoodOrder.client.services;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.FoodOrder.client.http.HttpController;
import org.FoodOrder.client.http.HttpHeaders;
import org.FoodOrder.client.http.HttpMethod;
import org.FoodOrder.client.http.HttpResponse;
import org.FoodOrder.client.models.Restaurant;
import org.FoodOrder.client.sessions.UserSession;

import java.util.List;

public class RestaurantService {

    public static List<Restaurant> loadMyRestaurants() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + UserSession.getToken());
        headers.set("Content-Type", "application/json");

        HttpResponse response = HttpController.sendRequest(
                "http://localhost:8082/restaurants/mine",
                HttpMethod.GET,
                null,
                headers
        );

        if (response.getStatusCode() == 200) {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(response.getBody(), new TypeReference<List<Restaurant>>() {});
        } else {
            throw new RuntimeException("Failed to load restaurants. Status: " + response.getStatusCode());
        }
    }
}
