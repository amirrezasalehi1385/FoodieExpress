package org.FoodOrder.client.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
import org.FoodOrder.client.sessions.UserSession;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class OrderHistoryController implements Initializable {
    @FXML private ScrollPane orderScrollPane;
    @FXML private VBox orderHistoryBox;
    @FXML private Label statusMessageLbl;
    @FXML private Button backBtn;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String BASE_URL = "http://localhost:8082";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        orderHistoryBox.setSpacing(15);
        orderHistoryBox.setPadding(new Insets(10));
        loadOrderHistory();
    }

    private void loadCommentsForOrder(Long orderId, VBox commentsBox) {
        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                String url = BASE_URL + "/ratings/" + orderId; // تغییر به endpoint موجود
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + UserSession.getToken());
                HttpResponse response = HttpController.sendRequest(url, HttpMethod.GET, null, headers);
                System.out.println("API Response for rating (orderId: " + orderId + "): " + response.getBody()); // لاگ برای دیباگ
                if (response.getStatusCode() == 200) {
                    return objectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});
                }
                return new HashMap<>();
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Map<String, Object> ratingData = task.getValue();
                commentsBox.getChildren().clear(); // پاک کردن محتوای قبلی
                Label commentsTitle = new Label("Rating:");
                commentsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                commentsBox.getChildren().add(commentsTitle);
                Integer rating = ratingData.get("rating") != null ? ((Number) ratingData.get("rating")).intValue() : 0;
                String comment = ratingData.get("comment") != null ? ratingData.get("comment").toString() : "";
                if (rating > 0 || !comment.isEmpty()) {
                    Label commentLabel = new Label("Rating: " + "★".repeat(rating) + " " + comment);
                    commentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50; -fx-padding: 5; -fx-background-color: #ffffff; -fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
                    commentLabel.setWrapText(true);
                    commentLabel.setMaxWidth(300);
                    commentsBox.getChildren().add(commentLabel);
                } else {
                    Label noCommentsLabel = new Label("No rating or comment yet");
                    noCommentsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");
                    commentsBox.getChildren().add(noCommentsLabel);
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                commentsBox.getChildren().clear(); // پاک کردن محتوای قبلی
                Label commentsTitle = new Label("Rating:");
                commentsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                commentsBox.getChildren().add(commentsTitle);
                Label errorLabel = new Label("Failed to load rating");
                errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #e74c3c; -fx-font-style: italic;");
                commentsBox.getChildren().add(errorLabel);
            });
        });

        new Thread(task).start();
    }

    private void loadOrderHistory() {
        showLoadingIndicator();
        Task<List<Order>> task = new Task<>() {
            @Override
            protected List<Order> call() throws Exception {
                String url = BASE_URL + "/orders/history/";
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + UserSession.getToken());
                HttpResponse response = HttpController.sendRequest(url, HttpMethod.GET, null, headers);
                if (response.getStatusCode() == 200) {
                    return objectMapper.readValue(response.getBody(), new TypeReference<List<Order>>() {});
                } else {
                    throw new Exception("Failed to load order history: " + response.getBody());
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
                showStatusMessage("❌ Failed to load order history: " + task.getException().getMessage(), "#e74c3c", "#fff5f5");
            });
        });

        new Thread(task).start();
    }

    private void showEmptyHistory() {
        VBox emptyBox = new VBox(10);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setStyle("-fx-padding: 40; -fx-background-color: #f8f9fa; -fx-background-radius: 15;");

        Label emptyIcon = new Label("📜");
        emptyIcon.setStyle("-fx-font-size: 48; -fx-text-fill: #bdc3c7;");

        Label emptyMessage = new Label("No orders found");
        emptyMessage.setStyle("-fx-font-size: 18; -fx-text-fill: #7f8c8d; -fx-font-weight: 600;");

        Label emptySubtext = new Label("Start ordering to see your history here!");
        emptySubtext.setStyle("-fx-font-size: 14; -fx-text-fill: #95a5a6;");

        emptyBox.getChildren().addAll(emptyIcon, emptyMessage, emptySubtext);
        orderHistoryBox.getChildren().add(emptyBox);
    }

    private void showLoadingIndicator() {
        orderHistoryBox.getChildren().clear();
        VBox loadingBox = new VBox(10);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setStyle("-fx-padding: 40; -fx-background-color: #f8f9fa; -fx-background-radius: 15;");

        Label loadingIcon = new Label("🔄");
        loadingIcon.setStyle("-fx-font-size: 48; -fx-text-fill: #3498db;");

        Label loadingMessage = new Label("Loading order history...");
        loadingMessage.setStyle("-fx-font-size: 18; -fx-text-fill: #7f8c8d; -fx-font-weight: 600;");

        loadingBox.getChildren().addAll(loadingIcon, loadingMessage);
        orderHistoryBox.getChildren().add(loadingBox);
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

        PauseTransition hideMessage = new PauseTransition(Duration.seconds(3));
        hideMessage.setOnFinished(e -> statusMessageLbl.setVisible(false));
        hideMessage.play();
    }

    private VBox createOrderCard(Order order) {
        VBox card = new VBox(15);
        card.setAlignment(Pos.TOP_LEFT);

        String status = order.getStatus() != null ? order.getStatus().toLowerCase() : "unknown";
        String icon, backgroundColor, borderColor;
        switch (status) {
            case "submitted":
                icon = "📝";
                backgroundColor = "#e8f0fe";
                borderColor = "#3498db";
                break;
            case "unpaid and cancelled":
            case "cancelled":
                icon = "❌";
                backgroundColor = "#ffebee";
                borderColor = "#e74c3c";
                break;
            case "waiting vendor":
                icon = "⏳";
                backgroundColor = "#fff3e0";
                borderColor = "#f39c12";
                break;
            case "finding courier":
                icon = "🔍";
                backgroundColor = "#e8f5e9";
                borderColor = "#2ecc71";
                break;
            case "on the way":
                icon = "🚚";
                backgroundColor = "#e3f2fd";
                borderColor = "#3498db";
                break;
            case "completed":
                icon = "✅";
                backgroundColor = "#e8f5e9";
                borderColor = "#27ae60";
                break;
            default:
                icon = "📦";
                backgroundColor = "#f8f9fa";
                borderColor = "#7f8c8d";
        }

        card.setStyle(
                "-fx-background-color: " + backgroundColor + "; " +
                        "-fx-background-radius: 20; " +
                        "-fx-padding: 20; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 10, 0, 0, 5); " +
                        "-fx-border-color: " + borderColor + "; " +
                        "-fx-border-radius: 20; " +
                        "-fx-border-width: 2;"
        );

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        Label statusIconLabel = new Label(icon);
        statusIconLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: " + borderColor + ";");
        Label orderIdLabel = new Label("Order #" + order.getId());
        orderIdLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        headerBox.getChildren().addAll(statusIconLabel, orderIdLabel);

        Label restaurantLabel = new Label("Restaurant ID: " + (order.getRestaurantId() != null ? order.getRestaurantId() : "Unknown"));
        restaurantLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        Label statusLabel = new Label("Status: " + (order.getStatus() != null ? order.getStatus() : "Unknown"));
        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + borderColor + "; -fx-font-weight: bold;");

        Label addressLabel = new Label("Delivery Address: " + (order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "Unknown"));
        addressLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        Label totalPriceLabel = new Label("Total: $" + String.format("%.2f", order.getTotalPrice()));
        totalPriceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        VBox ratingBox = new VBox(10);
        ratingBox.setAlignment(Pos.TOP_LEFT);
        ratingBox.setVisible(status.equals("completed"));
        ratingBox.setManaged(status.equals("completed"));

        HBox starBox = new HBox(5);
        starBox.setAlignment(Pos.CENTER_LEFT);
        ToggleGroup ratingGroup = new ToggleGroup();
        for (int i = 1; i <= 5; i++) {
            ToggleButton starButton = new ToggleButton("★");
            starButton.setStyle(
                    "-fx-font-size: 16px; " +
                            "-fx-text-fill: #bdc3c7; " +
                            "-fx-background-color: transparent; " +
                            "-fx-cursor: hand;"
            );
            starButton.setUserData(i);
            starButton.setToggleGroup(ratingGroup);
            int finalI = i;
            starButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    for (int j = 0; j < 5; j++) {
                        ToggleButton btn = (ToggleButton) starBox.getChildren().get(j);
                        btn.setStyle("-fx-font-size: 16px; " +
                                "-fx-text-fill: " + (j < finalI ? "#f1c40f" : "#bdc3c7") + "; " +
                                "-fx-background-color: transparent; " +
                                "-fx-cursor: hand;");
                    }
                }
            });
            starBox.getChildren().add(starButton);
        }

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Add your comment here...");
        commentArea.setPrefRowCount(2);
        commentArea.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-padding: 5; " +
                        "-fx-background-radius: 5;"
        );
        commentArea.setMaxWidth(300);

        Button submitButton = new Button("Submit Rating");
        submitButton.setStyle(
                "-fx-background-color: #27ae60; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 10 20; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
        );
        submitButton.setOnAction(event -> {
            Integer rating = (Integer) ratingGroup.getSelectedToggle().getUserData();
            String comment = commentArea.getText();
            if (rating != null) {
                submitRating(order.getId(), rating, comment);
                // پنهان کردن و آزاد کردن فضا بعد از ثبت
                starBox.setDisable(true);
                commentArea.setVisible(false);
                commentArea.setManaged(false);
                submitButton.setVisible(false);
                submitButton.setManaged(false);
            } else {
                showStatusMessage("❌ Please select a rating!", "#e74c3c", "#fff5f5");
            }
        });

        ratingBox.getChildren().addAll(starBox, commentArea, submitButton);

        loadRatingForOrder(order.getId(), starBox, commentArea, submitButton);

        // بخش نمایش کامنت‌ها
        VBox commentsBox = new VBox(10);
        commentsBox.setStyle("-fx-padding: 10; -fx-background-color: #f8f9fa; -fx-background-radius: 10;");
        Label commentsTitle = new Label("Comments:");
        commentsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        commentsBox.getChildren().add(commentsTitle);
        loadCommentsForOrder(order.getId(), commentsBox);

        VBox itemsBox = new VBox(10);
        itemsBox.setStyle("-fx-padding: 10; -fx-background-color: #f8f9fa; -fx-background-radius: 10;");
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
        card.getChildren().addAll(headerBox, restaurantLabel, statusLabel, addressLabel, totalPriceLabel, ratingBox, itemsBox, commentsBox);
        return card;
    }

    private void submitRating(Long orderId, int rating, String comment) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                String url = BASE_URL + "/ratings";
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + UserSession.getToken());
                headers.set("Content-Type", "application/json");

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("orderId", orderId.toString());
                requestBody.put("rating", rating);
                requestBody.put("comment", comment != null ? comment : "");
                String body = objectMapper.writeValueAsString(requestBody);

                HttpResponse response = HttpController.sendRequest(url, HttpMethod.POST, body, headers);
                System.out.println("Response Code: " + response.getStatusCode()); // لاگ برای دیباگ
                System.out.println("Response Body: " + response.getBody()); // لاگ برای دیباگ
                if (response.getStatusCode() != 200) {
                    throw new Exception("Failed to submit rating: " + response.getBody());
                }
                return null;
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                showStatusMessage("✅ Rating and comment submitted successfully!", "#27ae60", "#e8f5e9");
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                String errorMessage = task.getException().getMessage();
                System.out.println("Error: " + errorMessage); // لاگ برای دیباگ
                showStatusMessage("❌ Failed to submit rating: " + errorMessage, "#e74c3c", "#fff5f5");
            });
        });

        new Thread(task).start();
    }

    private void loadRatingForOrder(Long orderId, HBox starBox, TextArea commentArea, Button submitButton) {
        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                String url = BASE_URL + "/ratings/" + orderId;
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + UserSession.getToken());
                HttpResponse response = HttpController.sendRequest(url, HttpMethod.GET, null, headers);
                if (response.getStatusCode() == 200) {
                    return objectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});
                }
                return new HashMap<>();
            }
        };

        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                try {
                    Map<String, Object> response = task.getValue();
                    int rating = ((Number) response.getOrDefault("rating", 0)).intValue();
                    String comment = response.get("comment") != null ? response.get("comment").toString() : "";
                    if (rating > 0) {
                        for (int j = 0; j < 5; j++) {
                            ToggleButton btn = (ToggleButton) starBox.getChildren().get(j);
                            btn.setStyle("-fx-font-size: 16px; " +
                                    "-fx-text-fill: " + (j < rating ? "#f1c40f" : "#bdc3c7") + "; " +
                                    "-fx-background-color: transparent; " +
                                    "-fx-cursor: hand;");
                            btn.setSelected(j < rating);
                            btn.setDisable(true);
                        }
                        commentArea.setText(comment);
                        commentArea.setVisible(false);
                        commentArea.setManaged(false);
                        submitButton.setVisible(false);
                        submitButton.setManaged(false);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                showStatusMessage("❌ Failed to load rating: " + task.getException().getMessage(), "#e74c3c", "#fff5f5");
            });
        });

        new Thread(task).start();
    }

    @FXML
    private void onBackBtnAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/FoodOrder/client/view/home.fxml"));
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