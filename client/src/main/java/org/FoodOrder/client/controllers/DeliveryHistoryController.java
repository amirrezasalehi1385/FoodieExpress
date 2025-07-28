package org.FoodOrder.client.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
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

public class DeliveryHistoryController implements Initializable {
    @FXML private TextField searchField;
    @FXML private TextField vendorField;
    @FXML private TextField userField;
    @FXML private Button filterButton;
    @FXML private VBox deliveryHistoryBox;
    @FXML private Label statusMessageLbl;
    @FXML private Button backButton;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String BASE_URL = "http://localhost:8082";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        deliveryHistoryBox.setSpacing(15);
        loadDeliveryHistory();
    }

    private void loadDeliveryHistory() {
        showLoadingMessage();
        Task<List<Order>> task = new Task<>() {
            @Override
            protected List<Order> call() throws Exception {
                String url = BASE_URL + "/deliveries/history";
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + UserSession.getToken());
                String query = buildQueryParams();
                if (!query.isEmpty()) {
                    url += "?" + query;
                }
                HttpResponse response = HttpController.sendRequest(url, HttpMethod.GET, null, headers);
                if (response.getStatusCode() == 200) {
                    return objectMapper.readValue(response.getBody(), new TypeReference<List<Order>>() {});
                } else {
                    throw new Exception("Failed to load delivery history: " + response.getBody());
                }
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                deliveryHistoryBox.getChildren().clear();
                List<Order> orders = task.getValue();
                if (orders.isEmpty()) {
                    showEmptyMessage();
                } else {
                    for (Order order : orders) {
                        VBox orderCard = createOrderCard(order);
                        deliveryHistoryBox.getChildren().add(orderCard);
                    }
                }
                statusMessageLbl.setVisible(false);
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                showStatusMessage("❌ Failed to load delivery history: " + task.getException().getMessage(), "#e74c3c", "#fff5f5");
            });
        });

        new Thread(task).start();
    }

    private String buildQueryParams() {
        StringBuilder query = new StringBuilder();
        if (!searchField.getText().isEmpty()) {
            if (query.length() > 0) query.append("&");
            query.append("search=").append(searchField.getText());
        }
        if (!vendorField.getText().isEmpty()) {
            if (query.length() > 0) query.append("&");
            query.append("vendor=").append(vendorField.getText());
        }
        if (!userField.getText().isEmpty()) {
            if (query.length() > 0) query.append("&");
            query.append("user=").append(userField.getText());
        }
        return query.toString();
    }

    private VBox createOrderCard(Order order) {
        VBox card = new VBox(10);
        card.setStyle("-fx-padding: 10; -fx-background-color: #ffffff; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        Label orderIdLabel = new Label("Order #" + order.getId());
        orderIdLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label statusLabel = new Label("Status: " + order.getStatus());
        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #3498db;");

        Label addressLabel = new Label("Address: " + (order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "Unknown"));
        addressLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        card.getChildren().addAll(orderIdLabel, statusLabel, addressLabel);
        return card;
    }

    private void showLoadingMessage() {
        deliveryHistoryBox.getChildren().clear();
        Label loadingLabel = new Label("Loading deliveries...");
        loadingLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d; -fx-alignment: center;");
        deliveryHistoryBox.getChildren().add(loadingLabel);
    }

    private void showEmptyMessage() {
        deliveryHistoryBox.getChildren().clear();
        Label emptyLabel = new Label("No deliveries found");
        emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d; -fx-alignment: center;");
        deliveryHistoryBox.getChildren().add(emptyLabel);
    }

    private void showStatusMessage(String message, String textColor, String backgroundColor) {
        statusMessageLbl.setText(message);
        statusMessageLbl.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-text-fill: " + textColor + "; " +
                        "-fx-background-color: " + backgroundColor + "; " +
                        "-fx-padding: 10; " +
                        "-fx-background-radius: 8; " +
                        "-fx-font-weight: bold;"
        );
        statusMessageLbl.setVisible(true);
    }
    @FXML
    private void onBackBtnAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/FoodOrder/client/view/home.fxml"));
            Parent page = loader.load();
            Scene scene = new Scene(page);
            Stage currentStage = (Stage) backButton.getScene().getWindow();
            currentStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showStatusMessage("❌ Error navigating back: " + e.getMessage(), "#e74c3c", "#fff5f5");
        }
    }
    @FXML
    private void onFilterAction(ActionEvent event) {
        loadDeliveryHistory();
    }
}