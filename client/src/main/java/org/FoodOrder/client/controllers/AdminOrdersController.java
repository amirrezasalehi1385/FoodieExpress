
package org.FoodOrder.client.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.FoodOrder.client.http.HttpController;
import org.FoodOrder.client.http.HttpHeaders;
import org.FoodOrder.client.http.HttpMethod;
import org.FoodOrder.client.http.HttpResponse;
import org.FoodOrder.client.models.Order;
import org.FoodOrder.client.models.OrderItem;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AdminOrdersController implements Initializable {

    @FXML private ScrollPane orderScrollPane;
    @FXML private VBox orderHistoryBox;
    @FXML private Button backBtn;
    @FXML private TextField searchField;
    @FXML private TextField vendorField;
    @FXML private TextField courierField;
    @FXML private TextField customerField;
    @FXML private TextField statusField;
    @FXML private Button filterButton;
    @FXML private BorderPane rootPane;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String BASE_URL = "http://localhost:8082";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        orderHistoryBox.setSpacing(15);
        orderHistoryBox.setPadding(new Insets(10));
        loadOrderHistory();
    }

    @FXML
    private void onFilterButtonAction() {
        loadOrderHistory();
    }

    private void loadOrderHistory() {
        showLoadingIndicator();
        Task<List<Order>> task = new Task<>() {
            @Override
            protected List<Order> call() throws Exception {
                StringBuilder url = new StringBuilder(BASE_URL + "/admin/orders");
                StringBuilder query = new StringBuilder();
                if (!searchField.getText().isEmpty()) {
                    query.append("search=").append(searchField.getText()).append("&");
                }
                if (!vendorField.getText().isEmpty()) {
                    query.append("vendor=").append(vendorField.getText()).append("&");
                }
                if (!courierField.getText().isEmpty()) {
                    query.append("courier=").append(courierField.getText()).append("&");
                }
                if (!customerField.getText().isEmpty()) {
                    query.append("customer=").append(customerField.getText()).append("&");
                }
                if (!statusField.getText().isEmpty()) {
                    query.append("status=").append(statusField.getText());
                }
                if (query.length() > 0) {
                    url.append("?").append(query);
                }

                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");

                HttpResponse response = HttpController.sendRequest(url.toString(), HttpMethod.GET, null, headers);
                if (response.getStatusCode() == 200) {
                    return objectMapper.readValue(response.getBody(), new TypeReference<List<Order>>() {});
                } else {
                    throw new Exception("Failed to load orders: " + response.getBody());
                }
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                orderHistoryBox.getChildren().clear();
                List<Order> orders = task.getValue();
                if (orders.isEmpty()) {
                    showEmptyHistory();
                } else {
                    for (Order order : orders) {
                        VBox orderCard = createOrderCard(order);
                        orderHistoryBox.getChildren().add(orderCard);
                    }
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                showStatusMessage("❌ Failed to load orders: " + task.getException().getMessage(), "#e74c3c", "#fff5f5");
            });
        });

        new Thread(task).start();
    }

    private void showEmptyHistory() {
        VBox emptyBox = new VBox(10);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.getStyleClass().add("card");

        Label emptyIcon = new Label("📜");
        emptyIcon.setStyle("-fx-font-size: 48; -fx-text-fill: #bdc3c7;");

        Label emptyMessage = new Label("No orders found");
        emptyMessage.setStyle("-fx-font-size: 18; -fx-text-fill: #7f8c8d; -fx-font-weight: 600;");

        Label emptySubtext = new Label("Try adjusting your filters!");
        emptySubtext.setStyle("-fx-font-size: 14; -fx-text-fill: #95a5a6;");

        emptyBox.getChildren().addAll(emptyIcon, emptyMessage, emptySubtext);
        orderHistoryBox.getChildren().add(emptyBox);
    }

    private void showLoadingIndicator() {
        orderHistoryBox.getChildren().clear();
        VBox loadingBox = new VBox(10);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.getStyleClass().add("card");

        Label loadingIcon = new Label("🔄");
        loadingIcon.setStyle("-fx-font-size: 48; -fx-text-fill: #3498db;");

        Label loadingMessage = new Label("Loading orders...");
        loadingMessage.setStyle("-fx-font-size: 18; -fx-text-fill: #7f8c8d; -fx-font-weight: 600;");

        loadingBox.getChildren().addAll(loadingIcon, loadingMessage);
        orderHistoryBox.getChildren().add(loadingBox);
    }

    private void showStatusMessage(String message, String textColor, String backgroundColor) {
        Label statusMessageLbl = new Label(message);
        statusMessageLbl.getStyleClass().add("status-message");
        statusMessageLbl.setStyle(
                "-fx-text-fill: " + textColor + "; " +
                        "-fx-background-color: " + backgroundColor + ";"
        );
        orderHistoryBox.getChildren().add(statusMessageLbl);

        PauseTransition hideMessage = new PauseTransition(Duration.seconds(3));
        hideMessage.setOnFinished(e -> orderHistoryBox.getChildren().remove(statusMessageLbl));
        hideMessage.play();
    }

    private VBox createOrderCard(Order order) {
        VBox card = new VBox(15);
        card.setAlignment(Pos.TOP_LEFT);
        card.getStyleClass().add("card");

        String status = order.getStatus() != null ? order.getStatus().toLowerCase() : "unknown";
        String icon, borderColor;
        switch (status) {
            case "submitted":
                icon = "📝";
                borderColor = "#3498db";
                card.getStyleClass().add("card-submitted");
                break;
            case "cancelled":
            case "unpaid and cancelled":
                icon = "❌";
                borderColor = "#e74c3c";
                card.getStyleClass().add("card-cancelled");
                break;
            case "waiting_vendor":
                icon = "⏳";
                borderColor = "#f39c12";
                card.getStyleClass().add("card-waiting");
                break;
            case "finding_courier":
                icon = "🔍";
                borderColor = "#2ecc71";
                card.getStyleClass().add("card-finding");
                break;
            case "on_the_way":
                icon = "🚚";
                borderColor = "#3498db";
                card.getStyleClass().add("card-on-the-way");
                break;
            case "completed":
                icon = "✅";
                borderColor = "#27ae60";
                card.getStyleClass().add("card-completed");
                break;
            default:
                icon = "📦";
                borderColor = "#7f8c8d";
                card.getStyleClass().add("card-default");
        }

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        Label statusIconLabel = new Label(icon);
        statusIconLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: " + borderColor + ";");
        Label orderIdLabel = new Label("Order #" + order.getId());
        orderIdLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        headerBox.getChildren().addAll(statusIconLabel, orderIdLabel);

        Label restaurantLabel = new Label("Restaurant ID: " + (order.getRestaurantId() != null ? order.getRestaurantId() : "Unknown"));
        restaurantLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        Label customerLabel = new Label("Customer ID: " + (order.getCustomerId() != null ? order.getCustomerId() : "Unknown"));
        customerLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        Label statusLabel = new Label("Status: " + (order.getStatus() != null ? order.getStatus() : "Unknown"));
        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + borderColor + "; -fx-font-weight: bold;");

        Label addressLabel = new Label("Delivery Address: " + (order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "Unknown"));
        addressLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        Label totalPriceLabel = new Label("Total: $" + String.format("%.2f", order.getTotalPrice()));
        totalPriceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        VBox itemsBox = new VBox(10);
        itemsBox.getStyleClass().add("items-box");
        Label itemsTitle = new Label("Items:");
        itemsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem item : order.getItems()) {
                HBox itemRow = new HBox(10);
                Label itemName = new Label((item.getItemName() != null ? item.getItemName() : "Unknown") + " (x" + item.getQuantity() + ")");
                itemName.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
                Label itemPrice = new Label("$" + String.format("%.2f", item.getPrice() * item.getQuantity()));
                itemPrice.setStyle("-fx-font-size: 14px; -fx-text-fill: #27ae60;");
                itemRow.getChildren().addAll(itemName, itemPrice);
                itemsBox.getChildren().add(itemRow);
            }
        } else {
            Label noItemsLabel = new Label("No items in this order");
            noItemsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");
            itemsBox.getChildren().add(noItemsLabel);
        }

        itemsBox.getChildren().add(0, itemsTitle);
        card.getChildren().addAll(headerBox, restaurantLabel, customerLabel, statusLabel, addressLabel, totalPriceLabel, itemsBox);
        return card;
    }

    @FXML
    private void onBackBtnAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/FoodOrder/client/view/adminHome.fxml"));
            Parent page = loader.load();
            Scene scene = new Scene(page);
            Stage currentStage = (Stage) backBtn.getScene().getWindow();
            currentStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showStatusMessage("❌ Error navigating back: " + e.getMessage(), "#e74c3c", "#fff5f5");
        }
    }
}