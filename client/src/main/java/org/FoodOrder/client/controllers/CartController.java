package org.FoodOrder.client.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.FoodOrder.client.http.HttpController;
import org.FoodOrder.client.http.HttpHeaders;
import org.FoodOrder.client.http.HttpMethod;
import org.FoodOrder.client.http.HttpResponse;
import org.FoodOrder.client.models.Cart;
import org.FoodOrder.client.models.CartItem;
import org.FoodOrder.client.models.FoodItem;
import org.FoodOrder.client.models.Restaurant;
import org.FoodOrder.client.sessions.UserSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class CartController implements Initializable {
    @FXML private VBox cartItemsBox;
    @FXML private Label totalPriceLabel;
    @FXML private Button submitOrderButton;
    @FXML private Button backBtn;
    @FXML private Label statusMessageLbl;
    @FXML private TextField deliveryAddressField;
    @FXML private BorderPane rootPane;
    @FXML private RadioButton walletPaymentRadio;
    @FXML private RadioButton onlinePaymentRadio;

    private Restaurant restaurant;
    private Cart cart;
    private List<FoodItem> foodItems = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private PauseTransition cartRefreshTimer;
    private BigDecimal walletBalance = BigDecimal.ZERO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cartItemsBox.setSpacing(15);
        cartItemsBox.setPadding(new Insets(10));
        submitOrderButton.setDisable(true);
        setupCartRefreshTimer();
        loadWalletBalance();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/FoodOrder/client/view/footer.fxml"));
            Parent footer = loader.load();
            FooterController footerController = loader.getController();
            footerController.setActive("cart");
            rootPane.setBottom(footer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupCartRefreshTimer() {
        cartRefreshTimer = new PauseTransition(Duration.millis(200));
        cartRefreshTimer.setCycleCount(Timeline.INDEFINITE);
        cartRefreshTimer.setOnFinished(event -> Platform.runLater(this::refreshCart));
        cartRefreshTimer.play();
    }

    public void setCartData(Restaurant restaurant, Cart cart, List<FoodItem> foodItems) {
        this.restaurant = restaurant;
        this.foodItems = foodItems;
        this.cart = cart;
        refreshCart();
    }

    private void refreshCart() {
        Task<Cart> task = new Task<>() {
            @Override
            protected Cart call() throws Exception {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + UserSession.getToken());
                HttpResponse response = HttpController.sendRequest("http://localhost:8082/cart", HttpMethod.GET, null, headers);
                if (response.getStatusCode() == 200) {
                    return objectMapper.readValue(response.getBody(), Cart.class);
                }
                return null;
            }
        };

        task.setOnSucceeded(event -> {
            cart = task.getValue();
            updateCartUI();
            checkSubmitButtonState();
        });

        task.setOnFailed(event -> {
            showStatusMessage("❌ Error loading cart", "#e74c3c", "#fff5f5");
        });

        new Thread(task).start();
    }

    private void updateCartUI() {
        cartItemsBox.getChildren().clear();
        double totalPrice = 0.0;

        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            showEmptyCart();
            return;
        }

        for (CartItem cartItem : cart.getItems()) {
            FoodItem item = cartItem.getFood();
            int quantity = cartItem.getCount();
            VBox itemBox = createCartItemBox(item, quantity);
            cartItemsBox.getChildren().add(itemBox);
            totalPrice += (double) item.getPrice() * quantity;
        }

        totalPriceLabel.setText(String.format("$%.2f", totalPrice));

        if (cart.getItems().isEmpty()) {
            showEmptyCart();
            submitOrderButton.setDisable(true);
        } else {
            submitOrderButton.setDisable(false);
        }
    }

    private void showEmptyCart() {
        VBox emptyBox = new VBox(10);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setStyle("-fx-padding: 40; -fx-background-color: #f8f9fa; -fx-background-radius: 15;");

        Label emptyIcon = new Label("🛒");
        emptyIcon.setStyle("-fx-font-size: 48; -fx-text-fill: #bdc3c7;");

        Label emptyMessage = new Label("Your cart is empty");
        emptyMessage.setStyle("-fx-font-size: 18; -fx-text-fill: #7f8c8d; -fx-font-weight: 600;");

        Label emptySubtext = new Label("Go back and add some delicious items!");
        emptySubtext.setStyle("-fx-font-size: 14; -fx-text-fill: #95a5a6;");

        emptyBox.getChildren().addAll(emptyIcon, emptyMessage, emptySubtext);
        cartItemsBox.getChildren().add(emptyBox);
        totalPriceLabel.setText("$0.00");
    }

    private VBox createCartItemBox(FoodItem item, int quantity) {

        VBox itemBox = new VBox(15);
        itemBox.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 15; " +
                        "-fx-padding: 20; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 10, 0, 0, 5); " +
                        "-fx-border-color: #f1f3f4; " +
                        "-fx-border-radius: 15; " +
                        "-fx-border-width: 1;"
        );
        itemBox.setAlignment(Pos.CENTER_LEFT);

        HBox contentBox = new HBox(15);
        contentBox.setAlignment(Pos.CENTER_LEFT);

        VBox itemDetails = new VBox(5);
        itemDetails.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(item.getName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label unitPriceLabel = new Label("Unit Price: $" + String.format("%.2f", (double) item.getPrice()));
        unitPriceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        itemDetails.getChildren().addAll(nameLabel, unitPriceLabel);

        HBox quantityControls = new HBox(10);
        quantityControls.setAlignment(Pos.CENTER);

        Button minusBtn = new Button("−");
        minusBtn.setStyle(
                "-fx-background-color: #e74c3c; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-pref-width: 30; " +
                        "-fx-pref-height: 30; " +
                        "-fx-border-radius: 50; " +
                        "-fx-background-radius: 50; " +
                        "-fx-cursor: hand;"
        );

        Label quantityLabel = new Label(String.valueOf(quantity));
        quantityLabel.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #2c3e50; " +
                        "-fx-min-width: 30; " +
                        "-fx-alignment: center;"
        );

        Button plusBtn = new Button("+");
        plusBtn.setStyle(
                "-fx-background-color: #27ae60; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-pref-width: 30; " +
                        "-fx-pref-height: 30; " +
                        "-fx-border-radius: 50; " +
                        "-fx-background-radius: 50; " +
                        "-fx-cursor: hand;"
        );

        quantityControls.getChildren().addAll(minusBtn, quantityLabel, plusBtn);

        VBox rightSection = new VBox(10);
        rightSection.setAlignment(Pos.CENTER_RIGHT);

        Label totalPriceLabel = new Label(String.format("$%.2f", (double) item.getPrice() * quantity));
        totalPriceLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");

        Button removeBtn = new Button("❌ Remove");
        removeBtn.setStyle(
                "-fx-background-color: #e74c3c; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 12px; " +
                        "-fx-background-radius: 8; " +
                        "-fx-padding: 8 12; " +
                        "-fx-cursor: hand;"
        );

        rightSection.getChildren().addAll(totalPriceLabel, removeBtn);

        plusBtn.setOnAction(e -> checkStockAndUpdateQuantity(item, 1, quantityLabel, totalPriceLabel));
        minusBtn.setOnAction(e -> {
            if (quantity > 1) {
                updateCartItem(item, -1);
            } else {
                removeCartItem(item);
            }
        });
        removeBtn.setOnAction(e -> removeCartItem(item));

        HBox.setHgrow(itemDetails, Priority.ALWAYS);
        contentBox.getChildren().addAll(itemDetails, quantityControls, rightSection);
        itemBox.getChildren().add(contentBox);

        return itemBox;
    }

    private void checkStockAndUpdateQuantity(FoodItem item, int changeAmount, Label quantityLabel, Label totalPriceLabel) {

        Task<FoodItem> task = new Task<>() {
            @Override
            protected FoodItem call() throws Exception {
                String url = "http://localhost:8082/vendors/" + restaurant.getId() + "/item/" + item.getId();
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + UserSession.getToken());
                HttpResponse response = HttpController.sendRequest(url, HttpMethod.GET, null, headers);
                if (response.getStatusCode() == 200) {
                    return objectMapper.readValue(response.getBody(), FoodItem.class);
                }
                return null;
            }
        };

        task.setOnSucceeded(event -> {
            FoodItem updatedItem = task.getValue();
            int currentQuantity = cart.getItems().stream()
                    .filter(cartItem -> cartItem.getFood().getId() == item.getId())
                    .mapToInt(CartItem::getCount)
                    .findFirst()
                    .orElse(0);

            if (updatedItem != null && updatedItem.getSupply() >= currentQuantity + changeAmount) {
                updateCartItem(item, changeAmount);
            } else {
                showStatusMessage("❌ Not enough stock for " + item.getName(), "#e74c3c", "#fff5f5");
            }
        });

        task.setOnFailed(event -> {
            showStatusMessage("❌ Error checking stock", "#e74c3c", "#fff5f5");
        });

        new Thread(task).start();
    }

    private void updateCartItem(FoodItem item, int count) {
        Task<Cart> task = new Task<>() {
            @Override
            protected Cart call() throws Exception {
                Map<String, Object> itemData = new HashMap<>();
                itemData.put("foodId", item.getId());
                itemData.put("count", count);
                String requestBody = objectMapper.writeValueAsString(itemData);

                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + UserSession.getToken());
                headers.set("Content-Type", "application/json");

                HttpResponse response = HttpController.sendRequest("http://localhost:8082/cart/add", HttpMethod.POST, requestBody, headers);
                if (response.getStatusCode() == 200) {
                    return objectMapper.readValue(response.getBody(), Cart.class);
                }
                return null;
            }
        };

        task.setOnSucceeded(event -> {
            cart = task.getValue();
            updateCartUI();
            checkSubmitButtonState();
            showStatusMessage("✅ " + item.getName() + " updated in cart", "#27ae60", "#f0fff4");
        });

        task.setOnFailed(event -> {
            showStatusMessage("❌ Error updating cart", "#e74c3c", "#fff5f5");
        });

        new Thread(task).start();
    }

    private void removeCartItem(FoodItem item) {
        Task<Cart> task = new Task<>() {
            @Override
            protected Cart call() throws Exception {
                Map<String, Object> itemData = new HashMap<>();
                itemData.put("foodId", item.getId());
                String requestBody = objectMapper.writeValueAsString(itemData);

                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + UserSession.getToken());
                headers.set("Content-Type", "application/json");

                HttpResponse response = HttpController.sendRequest("http://localhost:8082/cart/remove", HttpMethod.POST, requestBody, headers);
                if (response.getStatusCode() == 200) {
                    return objectMapper.readValue(response.getBody(), Cart.class);
                }
                return null;
            }
        };

        task.setOnSucceeded(event -> {
            cart = task.getValue();
            updateCartUI();
            checkSubmitButtonState();
            showStatusMessage("✅ " + item.getName() + " removed from cart", "#27ae60", "#f0fff4");
        });

        task.setOnFailed(event -> {
            showStatusMessage("❌ Error removing item", "#e74c3c", "#fff5f5");
        });

        new Thread(task).start();
    }

    @FXML
    private void onSubmitOrderAction(ActionEvent event) {
        try {
            if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
                showStatusMessage("❌ Cart is empty", "#e74c3c", "#fff5f5");
                return;
            }

            String deliveryAddress = deliveryAddressField.getText();
            if (deliveryAddress == null || deliveryAddress.trim().isEmpty()) {
                showStatusMessage("❌ Please enter a delivery address", "#e74c3c", "#fff5f5");
                return;
            }

            for (CartItem cartItem : cart.getItems()) {
                FoodItem item = cartItem.getFood();
                int quantity = cartItem.getCount();
                if (item.getSupply() < quantity) {
                    showStatusMessage("❌ Insufficient stock for " + item.getName(), "#e74c3c", "#fff5f5");
                    return;
                }
            }
            double totalPrice = Double.parseDouble(totalPriceLabel.getText().replace("$", ""));
            if (walletBalance.compareTo(BigDecimal.valueOf(totalPrice)) < 0) {
                showStatusMessage("❌ Insufficient balance in wallet", "#e74c3c", "#fff5f5");
                return;
            }
            deductFromWallet(totalPrice);

        } catch (Exception e) {
            Platform.runLater(() -> {
                showStatusMessage("❌ Error: " + e.getMessage(), "#e74c3c", "#fff5f5");
            });
            e.printStackTrace();
        }
    }

    private void deductFromWallet(double amount) {
        Task<HttpResponse> task = new Task<>() {
            @Override
            protected HttpResponse call() throws Exception {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + UserSession.getToken());
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("amount", amount);
                String body = objectMapper.writeValueAsString(bodyMap);
                HttpResponse response = HttpController.sendRequest("http://localhost:8082/wallet/deduct", HttpMethod.POST, body, headers);
                return response;
            }
        };

        task.setOnSucceeded(event -> {
            HttpResponse response = task.getValue();
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                Platform.runLater(() -> {
                    showStatusMessage("✅ Payment successful! Order submitted.", "#27ae60", "#f0fff4");
                    submitOrder();
                });
            } else {
                Platform.runLater(() -> {
                    showStatusMessage("❌ Payment failed: " + response.getBody(), "#e74c3c", "#fff5f5");
                });
            }
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                showStatusMessage("❌ Error deducting from wallet: " + task.getException().getMessage(), "#e74c3c", "#fff5f5");
            });
        });

        new Thread(task).start();
    }

    private void submitOrder() {
        try {
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("vendor_id", restaurant.getId());
            orderData.put("delivery_address", deliveryAddressField.getText());
            List<Map<String, Object>> items = new ArrayList<>();
            for (CartItem cartItem : cart.getItems()) {
                Map<String, Object> item = new HashMap<>();
                item.put("item_id", cartItem.getFood().getId());
                item.put("quantity", cartItem.getCount());
                items.add(item);
            }
            orderData.put("items", items);

            String requestBody = objectMapper.writeValueAsString(orderData);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + UserSession.getToken());
            headers.set("Content-Type", "application/json");

            Task<HttpResponse> orderTask = new Task<>() {
                @Override
                protected HttpResponse call() throws Exception {
                    return HttpController.sendRequest(
                            "http://localhost:8082/orders",
                            HttpMethod.POST,
                            requestBody,
                            headers
                    );
                }
            };

            orderTask.setOnSucceeded(b -> {
                HttpResponse response = orderTask.getValue();
                if (response.getStatusCode() == 200) {
                    Task<HttpResponse> clearTask = new Task<>() {
                        @Override
                        protected HttpResponse call() throws Exception {
                            return HttpController.sendRequest(
                                    "http://localhost:8082/cart/clear",
                                    HttpMethod.POST,
                                    null,
                                    headers
                            );
                        }
                    };

                    clearTask.setOnSucceeded(e -> {
                        HttpResponse clearResponse = clearTask.getValue();
                        if (clearResponse.getStatusCode() == 200) {
                            cart.clear();
                            Platform.runLater(() -> {
                                updateCartUI();
                                System.out.println("✅ Order submitted successfully! Cart cleared.");
                                PauseTransition delay = new PauseTransition(Duration.seconds(2));
                                delay.setOnFinished(delayEvent -> navigateBackToFoodItems());
                                delay.play();
                            });
                        } else {
                            Platform.runLater(() -> {
                                System.out.println("❌ Order submitted but failed to clear cart: " + (clearResponse.getBody() != null ? clearResponse.getBody() : "No response body"));
                            });
                        }
                    });

                    clearTask.setOnFailed(e -> {
                        Platform.runLater(() -> {
                            showStatusMessage("❌ Order submitted but error clearing cart: " + clearTask.getException().getMessage(), "#e74c3c", "#fff5f5");
                        });
                    });

                    new Thread(clearTask).start();
                } else {
                    Platform.runLater(() -> {
                        showStatusMessage("❌ Failed to submit order: " + response.getBody(), "#e74c3c", "#fff5f5");
                    });
                }
            });

            orderTask.setOnFailed(a -> {
                Platform.runLater(() -> {
                    showStatusMessage("❌ Error submitting order: " + orderTask.getException().getMessage(), "#e74c3c", "#fff5f5");
                });
            });

            new Thread(orderTask).start();
        } catch (Exception e) {
            Platform.runLater(() -> {
                showStatusMessage("❌ Error: " + e.getMessage(), "#e74c3c", "#fff5f5");
            });
            e.printStackTrace();
        }
    }

    @FXML
    private void onBackBtnAction(ActionEvent event) {
        navigateBackToFoodItems();
    }

    private void navigateBackToFoodItems() {
        if (cartRefreshTimer != null) {
            cartRefreshTimer.stop();
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/FoodOrder/client/view/listOfFoodItems.fxml"));
            Parent foodItemsPage = loader.load();
            SeeFoodItemsController controller = loader.getController();

            if (restaurant != null) {
                controller.setRestaurant(restaurant);
            }

            Scene foodItemsScene = new Scene(foodItemsPage);
            Stage currentStage = (Stage) backBtn.getScene().getWindow();
            currentStage.setScene(foodItemsScene);
        } catch (IOException e) {
            e.printStackTrace();
            showStatusMessage("❌ Error navigating back", "#e74c3c", "#fff5f5");
        }
    }

    private void loadWalletBalance() {
        Task<BigDecimal> task = new Task<>() {
            @Override
            protected BigDecimal call() throws Exception {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + UserSession.getToken());
                HttpResponse response = HttpController.sendRequest("http://localhost:8082/wallet/balance", HttpMethod.GET, null, headers);
                if (response.getStatusCode() == 200) {
                    Map<String, Object> data = objectMapper.readValue(response.getBody(), Map.class);
                    return new BigDecimal(data.get("balance").toString());
                }
                return BigDecimal.ZERO;
            }
        };

        task.setOnSucceeded(event -> {
            walletBalance = task.getValue();
            checkSubmitButtonState();
        });

        task.setOnFailed(event -> {
            showStatusMessage("❌ Error loading wallet balance", "#e74c3c", "#fff5f5");
        });

        new Thread(task).start();
    }

    private void checkSubmitButtonState() {
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            submitOrderButton.setDisable(true);
            return;
        }

        double totalPrice = Double.parseDouble(totalPriceLabel.getText().replace("$", ""));
        boolean isWalletSelected = walletPaymentRadio.isSelected();
        if (isWalletSelected && walletBalance.compareTo(BigDecimal.valueOf(totalPrice)) >= 0) {
            submitOrderButton.setDisable(false);
        } else {
            submitOrderButton.setDisable(true);
        }
    }

    private void showStatusMessage(String message, String textColor, String backgroundColor) {
        Platform.runLater(() -> {
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
        });
    }
}