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
import java.util.UUID;

public class RestaurantOrdersController implements Initializable {
    @FXML
    private Accordion restaurantsAccordion;
    @FXML
    private Button backBtn;
    @FXML
    private BorderPane rootPane;
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

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private static final String BASE_URL = "http://localhost:8082"; // هماهنگ با سرور

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
                restaurantsAccordion.getPanes().clear();
                for (Map<String, Object> restaurant : restaurants) {
                    Long restaurantId = ((Number) restaurant.get("id")).longValue();
                    String restaurantName = (String) restaurant.get("name");
                    addRestaurantPane(restaurantId, restaurantName);
                }
            }else {
                showAlert("Error", "Failed to load restaurants: " + response.getBody());
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to connect to server: " + e.getMessage());
        }
    }

    private void addRestaurantPane(Long restaurantId, String restaurantName) {
        VBox ordersContainer = new VBox(10);
        ordersContainer.setStyle("-fx-padding: 15; -fx-background-color: #f8f9fa; -fx-background-radius: 10;");
        ordersContainer.getStyleClass().add("orders-container"); // اضافه کردن کلاس CSS

        Label ordersLabel = new Label("Orders");
        ordersLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label loadingLabel = new Label("Click to load orders...");
        loadingLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d; -fx-font-style: italic;");

        ordersContainer.getChildren().addAll(ordersLabel, loadingLabel);

        TitledPane pane = new TitledPane(restaurantName, ordersContainer);
        pane.getStyleClass().add("restaurant-pane");
        restaurantsAccordion.getPanes().add(pane);

        pane.expandedProperty().addListener((obs, wasExpanded, isExpanded) -> {
            if (isExpanded && ordersContainer.getChildren().size() <= 2) {
                loadOrdersForRestaurant(restaurantId, ordersContainer);
            }
        });
    }

    private void loadOrdersForRestaurant(Long restaurantId, VBox ordersContainer) {
        try {
            ordersContainer.getChildren().clear();
            Label ordersLabel = new Label("Orders");
            ordersLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

            ProgressIndicator loadingIndicator = new ProgressIndicator();
            loadingIndicator.setStyle("-fx-progress-color: #3498db;");
            loadingIndicator.setMaxSize(30, 30);

            ordersContainer.getChildren().addAll(ordersLabel, loadingIndicator);

            HttpHeaders headers = getAuthHeaders();
            String url = BASE_URL + "/restaurants/" + restaurantId + "/orders";
            HttpResponse response = HttpController.sendRequest(url, HttpMethod.GET, null, headers);

            if (response.getStatusCode() == 200) {
                List<Order> orders = objectMapper.readValue(response.getBody(),
                        new TypeReference<List<Order>>() {});

                ordersContainer.getChildren().clear();
                ordersContainer.getChildren().add(ordersLabel);

                if (orders.isEmpty()) {
                    Label noOrdersLabel = new Label("No orders available");
                    noOrdersLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d; -fx-font-style: italic;");
                    ordersContainer.getChildren().add(noOrdersLabel);
                } else {
                    Accordion ordersAccordion = new Accordion();
                    ordersAccordion.setStyle("-fx-background-color: transparent;");

                    for (Order order : orders) {
                        if (!"cancelled".equalsIgnoreCase(order.getStatus())) { // حذف سفارش‌های cancelled
                            TitledPane orderPane = createOrderPane(order, restaurantId);
                            ordersAccordion.getPanes().add(orderPane);
                        }
                    }
                    Button refreshButton = new Button("Refresh Orders");
                    refreshButton.getStyleClass().addAll("button", "refresh-button");
                    refreshButton.setOnAction(event -> loadOrdersForRestaurant(restaurantId, ordersContainer));
                    ordersContainer.getChildren().addAll(ordersAccordion, refreshButton);
                    FadeTransition fade = new FadeTransition(javafx.util.Duration.millis(500), ordersAccordion);
                    fade.setFromValue(0.0);
                    fade.setToValue(1.0);
                    fade.play();
                }
            } else if (response.getStatusCode() == 401) {
                showAlert("Unauthorized", "Invalid or expired token. Please log in again.");
            } else {
                ordersContainer.getChildren().clear();
                ordersContainer.getChildren().add(ordersLabel);
                Label errorLabel = new Label("Failed to load orders: " + response.getBody());
                errorLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 12px;");
                ordersContainer.getChildren().add(errorLabel);
            }
        } catch (IOException e) {
            ordersContainer.getChildren().clear();
            Label ordersLabel = new Label("Orders");
            ordersLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            Label errorLabel = new Label("Failed to connect to server: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 12px;");
            ordersContainer.getChildren().addAll(ordersLabel, errorLabel);
        }
    }

    private TitledPane createOrderPane(Order order, Long restaurantId) {
        VBox orderContent = new VBox(8);
        orderContent.setStyle("-fx-padding: 10;");

        Label orderIdLabel = new Label("Order #" + order.getId());
        orderIdLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label statusLabel = new Label("Status: " + order.getStatus());
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

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
        acceptButton.setOnAction(event -> handleAccept(order, restaurantId, orderContent.getParent().getParent().lookup(".orders-container")));
        acceptButton.setDisable("completed".equalsIgnoreCase(order.getStatus()) || "on the way".equalsIgnoreCase(order.getStatus()));

        Button rejectButton = new Button("Reject Order");
        rejectButton.getStyleClass().addAll("button", "reject-button");
        rejectButton.setOnAction(event -> handleReject(order, restaurantId, orderContent.getParent().getParent().lookup(".orders-container")));
        rejectButton.setDisable("completed".equalsIgnoreCase(order.getStatus()) || "on the way".equalsIgnoreCase(order.getStatus()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buttonBox = new HBox(10, spacer, acceptButton, rejectButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        orderContent.getChildren().addAll(orderIdLabel, statusLabel, customerLabel, addressLabel, totalLabel, itemsBox, buttonBox);

        TitledPane orderPane = new TitledPane("Order #" + order.getId(), orderContent);
        orderPane.getStyleClass().add("order-pane");

        return orderPane;
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
                loadOrdersForRestaurant(restaurantId, ordersContainer);
            }else {
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

}