package org.FoodOrder.client.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.FoodOrder.client.http.HttpController;
import org.FoodOrder.client.http.HttpHeaders;
import org.FoodOrder.client.http.HttpMethod;
import org.FoodOrder.client.http.HttpResponse;
import org.FoodOrder.client.models.Order;
import org.FoodOrder.client.sessions.UserSession;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class RestaurantOrdersController implements Initializable {

    @FXML
    private BorderPane rootPane;
    @FXML
    private Button backBtn;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private static final String BASE_URL = "http://localhost:8082";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadRestaurants();
        rootPane.getStylesheets().add(getClass().getResource("/org/FoodOrder/client/view/restaurant-orders.css").toExternalForm());
    }

    private void loadRestaurants() {
        try {
            HttpHeaders headers = getAuthHeaders();
            String url = BASE_URL + "/restaurants/mine";
            HttpResponse response = HttpController.sendRequest(url, HttpMethod.GET, null, headers);

            if (response.getStatusCode() == 200) {
                List<Map<String, Object>> restaurants = objectMapper.readValue(response.getBody(),
                        new TypeReference<List<Map<String, Object>>>() {});
                VBox mainContent = new VBox(20);
                mainContent.setStyle("-fx-padding: 40; -fx-background-color: transparent;");
                mainContent.setAlignment(Pos.CENTER);

                for (Map<String, Object> restaurant : restaurants) {
                    Long restaurantId = ((Number) restaurant.get("id")).longValue();
                    String restaurantName = (String) restaurant.get("name");
                    mainContent.getChildren().add(createRestaurantBox(restaurantId, restaurantName));
                }

                ScrollPane scrollPane = new ScrollPane(mainContent);
                scrollPane.setFitToWidth(true);
                scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
                rootPane.setCenter(scrollPane);
            } else {
                showAlert("Error", "Failed to load restaurants: " + response.getBody());
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to connect to server: " + e.getMessage());
        }
    }

    private VBox createRestaurantBox(Long restaurantId, String restaurantName) {
        VBox restaurantBox = new VBox(20);
        restaurantBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.95); -fx-background-radius: 20; -fx-padding: 25; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0.5, 0.0, 5.0);");
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label(restaurantName);
        nameLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        int submittedCount = loadSubmittedCount(restaurantId);
        Label badge = new Label(submittedCount > 0 ? String.valueOf(submittedCount) : "");
        badge.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 2px 6px; -fx-background-radius: 10; -fx-font-size: 12px;");
        badge.setVisible(submittedCount > 0);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        headerBox.getChildren().addAll(nameLabel, spacer, badge);

        VBox lastOrderPreview = new VBox(5);
        lastOrderPreview.setStyle("-fx-padding: 10;");
        loadLastOrderPreview(restaurantId, lastOrderPreview);

        Button viewMoreBtn = new Button("View More");
        viewMoreBtn.getStyleClass().addAll("button", "view-more-button");
        viewMoreBtn.setOnAction(event -> expandOrders(restaurantId, restaurantBox));

        restaurantBox.getChildren().addAll(headerBox, lastOrderPreview, viewMoreBtn);
        return restaurantBox;
    }

    private int loadSubmittedCount(Long restaurantId) {
        try {
            HttpHeaders headers = getAuthHeaders();
            String url = BASE_URL + "/restaurants/" + restaurantId + "/orders";
            HttpResponse response = HttpController.sendRequest(url, HttpMethod.GET, null, headers);

            if (response.getStatusCode() == 200) {
                List<Order> orders = objectMapper.readValue(response.getBody(),
                        new TypeReference<List<Order>>() {});
                return (int) orders.stream().filter(order -> "submitted".equalsIgnoreCase(order.getStatus())).count();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void loadLastOrderPreview(Long restaurantId, VBox previewContainer) {
        try {
            HttpHeaders headers = getAuthHeaders();
            String url = BASE_URL + "/restaurants/" + restaurantId + "/orders";
            HttpResponse response = HttpController.sendRequest(url, HttpMethod.GET, null, headers);

            if (response.getStatusCode() == 200) {
                List<Order> orders = objectMapper.readValue(response.getBody(),
                        new TypeReference<List<Order>>() {});
                List<Order> filteredOrders = orders.stream()
                        .filter(order -> !"cancelled".equalsIgnoreCase(order.getStatus()))
                        .sorted((o1, o2) -> o2.getId().compareTo(o1.getId()))
                        .collect(Collectors.toList());

                previewContainer.getChildren().clear();
                if (!filteredOrders.isEmpty()) {
                    Order lastOrder = filteredOrders.get(0);
                    String status = lastOrder.getStatus() != null ? lastOrder.getStatus().toLowerCase() : "unknown";
                    String textColor = getTextColor(status);
                    Label previewLabel = new Label("Order #" + lastOrder.getId() + " - Status: " + lastOrder.getStatus());
                    previewLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + textColor + ";");
                    previewContainer.getChildren().add(previewLabel);
                } else {
                    Label noOrdersLabel = new Label("No orders available");
                    noOrdersLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d; -fx-font-style: italic;");
                    previewContainer.getChildren().add(noOrdersLabel);
                }
            }
        } catch (IOException e) {
            previewContainer.getChildren().clear();
            Label errorLabel = new Label("Failed to load orders: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 12px;");
            previewContainer.getChildren().add(errorLabel);
        }
    }

    private String getTextColor(String status) {
        return switch (status) {
            case "submitted", "on the way" -> "#3498db";
            case "cancelled" -> "#e74c3c";
            case "waiting vendor" -> "#f39c12";
            case "finding courier" -> "#2ecc71";
            case "completed" -> "#27ae60";
            default -> "#7f8c8d";
        };
    }

    private void expandOrders(Long restaurantId, VBox restaurantBox) {
        try {
            HttpHeaders headers = getAuthHeaders();
            String url = BASE_URL + "/restaurants/" + restaurantId + "/orders";
            HttpResponse response = HttpController.sendRequest(url, HttpMethod.GET, null, headers);

            if (response.getStatusCode() == 200) {
                List<Order> orders = objectMapper.readValue(response.getBody(),
                        new TypeReference<List<Order>>() {});
                List<Order> filteredOrders = orders.stream()
                        .filter(order -> !"cancelled".equalsIgnoreCase(order.getStatus()))
                        .sorted((o1, o2) -> o2.getId().compareTo(o1.getId()))
                        .collect(Collectors.toList());

                VBox ordersContainer = new VBox(15);
                if (orders.isEmpty()) {
                    Label noOrdersLabel = new Label("No orders available");
                    noOrdersLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d; -fx-font-style: italic;");
                    ordersContainer.getChildren().add(noOrdersLabel);
                } else {
                    for (Order order : filteredOrders) {
                        VBox orderBox = createOrderBox(order, restaurantId);
                        ordersContainer.getChildren().add(orderBox);
                    }
                    Button refreshButton = new Button("Refresh Orders");
                    refreshButton.getStyleClass().addAll("button", "refresh-button");
                    refreshButton.setOnAction(event -> expandOrders(restaurantId, restaurantBox));
                    ordersContainer.getChildren().add(refreshButton);
                }

                int index = restaurantBox.getChildren().indexOf(restaurantBox.getChildren().get(1));
                if (index >= 0 && index < restaurantBox.getChildren().size()) {
                    restaurantBox.getChildren().set(index, ordersContainer);
                }
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to load orders: " + e.getMessage());
        }
    }

    private VBox createOrderBox(Order order, Long restaurantId) {
        VBox orderBox = new VBox(8);
        String status = order.getStatus() != null ? order.getStatus().toLowerCase() : "unknown";
        String backgroundColor, borderColor, textColor, icon;

        switch (status) {
            case "submitted":
                icon = "📝";
                backgroundColor = "#e8f0fe";
                borderColor = "#3498db";
                textColor = "#3498db";
                break;
            case "cancelled":
                icon = "❌";
                backgroundColor = "#ffebee";
                borderColor = "#e74c3c";
                textColor = "#e74c3c";
                break;
            case "waiting vendor":
                icon = "⏳";
                backgroundColor = "#fff3e0";
                borderColor = "#f39c12";
                textColor = "#f39c12";
                break;
            case "finding courier":
                icon = "🔍";
                backgroundColor = "#e8f5e9";
                borderColor = "#2ecc71";
                textColor = "#2ecc71";
                break;
            case "on the way":
                icon = "🚚";
                backgroundColor = "#e3f2fd";
                borderColor = "#3498db";
                textColor = "#3498db";
                break;
            case "completed":
                icon = "✅";
                backgroundColor = "#e8f5e9";
                borderColor = "#27ae60";
                textColor = "#27ae60";
                break;
            default:
                icon = "📦";
                backgroundColor = "#f8f9fa";
                borderColor = "#7f8c8d";
                textColor = "#7f8c8d";
        }

        orderBox.setStyle("-fx-background-color: " + backgroundColor + "; " +
                "-fx-border-color: " + borderColor + "; " +
                "-fx-border-radius: 6px; " +
                "-fx-background-radius: 6px; " +
                "-fx-padding: 12px;");

        Label orderIdLabel = new Label("Order #" + order.getId());
        orderIdLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label statusLabel = new Label(icon + " Status: " + order.getStatus());
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + textColor + ";");

        Label customerLabel = new Label("Customer: " + (order.getCustomerId() != null ? "ID: " + order.getCustomerId() : "Unknown"));
        customerLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
        Label addressLabel = new Label("Address: " + (order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "Unknown"));
        addressLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

        Label totalLabel = new Label("Total: $" + String.format("%.2f", order.getTotalPrice()));
        totalLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #495057; -fx-font-weight: bold;");

        VBox itemsBox = new VBox(5);
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (var item : order.getItems()) {
                Label itemLabel = new Label(item.getItemName() + ": " + item.getQuantity() + " x $" + item.getPrice());
                itemLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #495057;");
                itemsBox.getChildren().add(itemLabel);
            }
        } else {
            Label noItemsLabel = new Label("No items in this order");
            noItemsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d; -fx-font-style: italic;");
            itemsBox.getChildren().add(noItemsLabel);
        }

        Button acceptButton = new Button("Accept");
        acceptButton.getStyleClass().addAll("button", "accept-button");
        acceptButton.setOnAction(event -> handleAccept(order, restaurantId, orderBox.getParent().lookup(".orders-container")));
        acceptButton.setDisable(!"submitted".equalsIgnoreCase(order.getStatus()));

        Button rejectButton = new Button("Reject Order");
        rejectButton.getStyleClass().addAll("button", "reject-button");
        rejectButton.setOnAction(event -> handleReject(order, restaurantId, orderBox.getParent().lookup(".orders-container")));
        rejectButton.setDisable("on the way".equalsIgnoreCase(order.getStatus()) || "completed".equalsIgnoreCase(order.getStatus()));

        HBox buttonBox = new HBox(10, acceptButton, rejectButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        orderBox.getChildren().addAll(orderIdLabel, statusLabel, customerLabel, addressLabel, totalLabel, itemsBox, buttonBox);

        return orderBox;
    }

    private void handleAccept(Order order, Long restaurantId, Node container) {
        updateOrderStatus(order.getId(), "finding courier", restaurantId, (VBox) container);
    }

    private void handleReject(Order order, Long restaurantId, Node container) {
        updateOrderStatus(order.getId(), "cancelled", restaurantId, (VBox) container);
    }

    private void updateOrderStatus(Long orderId, String status, Long restaurantId, VBox ordersContainer) {
        try {
            HttpHeaders headers = getAuthHeaders();
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("status", status);
            String body = objectMapper.writeValueAsString(requestBody);

            String url = BASE_URL + "/restaurants/orders/" + orderId;
            HttpResponse response = HttpController.sendRequest(url, HttpMethod.PUT, body, headers);

            if (response.getStatusCode() == 200) {
                showAlert("Success", "Order status updated to " + status);
                loadLastOrderPreview(restaurantId, (VBox) ordersContainer.getChildren().get(1));
            } else {
                showAlert("Error", "Failed to update order status: " + response.getBody());
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to connect to server: " + e.getMessage());
        }
    }

    private HttpHeaders getAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + UserSession.getToken());
        headers.set("Content-Type", "application/json");
        return headers;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void onBackBtnAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/FoodOrder/client/view/home.fxml"));
            Parent homePage = loader.load();
            Scene restaurantsScene = new Scene(homePage);
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(restaurantsScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}