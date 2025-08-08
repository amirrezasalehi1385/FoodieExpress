package org.FoodOrder.client.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
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
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.FoodOrder.client.http.HttpController;
import org.FoodOrder.client.http.HttpHeaders;
import org.FoodOrder.client.http.HttpMethod;
import org.FoodOrder.client.http.HttpResponse;
import org.FoodOrder.client.models.Order;
import org.FoodOrder.client.models.Restaurant;
import org.FoodOrder.client.sessions.UserSession;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class AvailableDeliveriesController implements Initializable {

    @FXML private BorderPane rootPane;
    @FXML private Accordion findingCourierAccordion;
    @FXML private Accordion onTheWayAccordion;
    @FXML private TitledPane findingLoadingPane;
    @FXML private TitledPane onTheWayLoadingPane;
    @FXML private VBox emptyStateContainer;
    @FXML private Button backBtn;
    @FXML private Label lastUpdatedLabel;
    @FXML private Label statusMessageLbl;
    @FXML private Button refreshBtn;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String BASE_URL = "http://localhost:8082";
    private Map<Long, String> restaurantNames = new HashMap<>();
    private List<Order> findingCourierOrders = new ArrayList<>();
    private List<Order> onTheWayOrders = new ArrayList<>();
    private ScheduledExecutorService scheduler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupUI();
        loadAvailableDeliveries();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/FoodOrder/client/view/footer.fxml"));
            Parent footer = loader.load();
            FooterController footerController = loader.getController();
            footerController.setActive("deliveries");
            rootPane.setBottom(footer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupUI() {
        if (emptyStateContainer != null) emptyStateContainer.setVisible(false);
        if (statusMessageLbl != null) statusMessageLbl.setVisible(false);
        if (refreshBtn != null) {
            refreshBtn.setVisible(true);
            refreshBtn.getStyleClass().add("refresh-button");
            refreshBtn.setOnMouseClicked(e -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(100), refreshBtn);
                scale.setToX(0.9);
                scale.setToY(0.9);
                scale.setCycleCount(2);
                scale.setAutoReverse(true);
                scale.play();
                handleCheckAgain();
            });
        }
        rootPane.getStylesheets().add(getClass().getResource("/org/FoodOrder/client/view/available-deliveries.css").toExternalForm());
        scheduler = Executors.newScheduledThreadPool(1);
    }

    private void loadAvailableDeliveries() {
        showLoadingState();
        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    HttpHeaders headers = getAuthHeaders();
                    String url = BASE_URL + "/deliveries/available";
                    HttpResponse response = HttpController.sendRequest(url, HttpMethod.GET, null, headers);
                    if (response.getStatusCode() == 200) {
                        List<Order> allOrders = objectMapper.readValue(response.getBody(), new TypeReference<List<Order>>() {});
                        loadRestaurantNamesSync(allOrders);
                        findingCourierOrders.clear();
                        onTheWayOrders.clear();
                        for (Order order : allOrders) {
                            if ("finding courier".equalsIgnoreCase(order.getStatus())) {
                                findingCourierOrders.add(order);
                            } else if ("on the way".equalsIgnoreCase(order.getStatus())) {
                                onTheWayOrders.add(order);
                            }
                        }
                    } else {
                        throw new RuntimeException("Failed to load deliveries: " + response.getBody());
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Connection failed: " + e.getMessage(), e);
                }
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    updateDeliveryUI();
                    updateStatistics();
                    updateLastUpdatedTime();
                    showStatusMessage("✅ Deliveries loaded successfully!", "#27ae60", "#e8f5e9");
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    updateDeliveryUI();
                    showStatusMessage("❌ Failed to load deliveries: " + getException().getMessage(), "#e74c3c", "#fff5f5");
                    if (findingCourierOrders.isEmpty() && onTheWayOrders.isEmpty()) {
                        showRefreshButton();
                    }
                });
            }
        };
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }

    private void loadRestaurantNamesSync(List<Order> orders) throws IOException {
        for (Order order : orders) {
            if (!restaurantNames.containsKey(order.getRestaurantId())) {
                HttpHeaders headers = getAuthHeaders();
                String url = BASE_URL + "/restaurants/" + order.getRestaurantId();
                HttpResponse response = HttpController.sendRequest(url, HttpMethod.GET, null, headers);
                if (response.getStatusCode() == 200) {
                    Restaurant restaurant = objectMapper.readValue(response.getBody(), Restaurant.class);
                    restaurantNames.put(order.getRestaurantId(), restaurant.getName());
                }
            }
        }
    }

    private void updateDeliveryUI() {
        findingCourierAccordion.getPanes().clear();
        onTheWayAccordion.getPanes().clear();

        if (findingCourierOrders.isEmpty() && onTheWayOrders.isEmpty()) {
            showEmptyState();
            showRefreshButton();
        } else {
            hideEmptyState();
            VBox accordionContainer = new VBox(10);
            accordionContainer.getChildren().addAll(findingCourierAccordion, onTheWayAccordion);
            rootPane.setCenter(accordionContainer);
            createFindingCourierPanes();
            createOnTheWayPanes();
        }
    }

    private void createFindingCourierPanes() {
        for (int i = 0; i < findingCourierOrders.size(); i++) {
            Order order = findingCourierOrders.get(i);
            TitledPane pane = createEnhancedOrderPane(order, i, "findingCourier");
            findingCourierAccordion.getPanes().add(pane);
            addEntranceAnimation(pane, i * 100);
        }
    }

    private void createOnTheWayPanes() {
        for (int i = 0; i < onTheWayOrders.size(); i++) {
            Order order = onTheWayOrders.get(i);
            TitledPane pane = createEnhancedOrderPane(order, i, "onTheWay");
            onTheWayAccordion.getPanes().add(pane);
            addEntranceAnimation(pane, i * 100);
        }
    }

    private TitledPane createEnhancedOrderPane(Order order, int index, String type) {
        TitledPane pane = new TitledPane();
        String restaurantName = restaurantNames.getOrDefault(order.getRestaurantId(), "Loading...");
        String statusIcon = getStatusIcon(order.getStatus());

        pane.setText(String.format("%s Order #%d - %s", statusIcon, order.getId(), restaurantName));
        pane.setCollapsible(true);
        pane.setAnimated(true);

        if (order.getStatus().equalsIgnoreCase("ready")) {
            pane.getStyleClass().add("ready-order");
        } else {
            pane.getStyleClass().add("high-priority");
        }

        VBox content = createEnhancedOrderContent(order, type);
        pane.setContent(content);

        return pane;
    }

    private VBox createEnhancedOrderContent(Order order, String type) {
        VBox content = new VBox(15);
        content.getStyleClass().add("order-content");
        content.setAlignment(Pos.TOP_LEFT);

        HBox headerBox = new HBox(20);
        headerBox.getStyleClass().add("order-header");
        headerBox.setAlignment(Pos.CENTER_LEFT);

        VBox orderInfo = new VBox(5);
        Label orderIdLabel = new Label("Order #" + order.getId());
        orderIdLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        Label statusLabel = new Label("Status: " + order.getStatus());
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + getStatusColor(order.getStatus()) + "; -fx-font-weight: bold;");
        orderInfo.getChildren().addAll(orderIdLabel, statusLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label priceLabel = new Label("$" + String.format("%.2f", order.getTotalPrice()));
        priceLabel.getStyleClass().add("price-badge");
        headerBox.getChildren().addAll(orderInfo, spacer, priceLabel);

        HBox addressBox = new HBox(10);
        addressBox.setAlignment(Pos.CENTER_LEFT);
        Label addressIcon = new Label("📍");
        addressIcon.setStyle("-fx-font-size: 16px;");
        Label addressLabel = new Label(order.getDeliveryAddress());
        addressLabel.setStyle("-fx-text-fill: #34495e; -fx-font-size: 14px; -fx-wrap-text: true;");
        addressBox.getChildren().addAll(addressIcon, addressLabel);

        VBox itemsBox = new VBox(8);
        itemsBox.getStyleClass().add("order-items");
        Label itemsHeader = new Label("🍽 Items:");
        itemsHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 14px;");
        itemsBox.getChildren().add(itemsHeader);
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (var item : order.getItems()) {
                HBox itemBox = new HBox(10);
                itemBox.setAlignment(Pos.CENTER_LEFT);
                itemBox.setStyle("-fx-background-color: rgba(52, 152, 219, 0.1); -fx-background-radius: 8; -fx-padding: 8 12;");
                Label itemLabel = new Label(item.getItemName() + " × " + item.getQuantity());
                itemLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 13px;");
                itemBox.getChildren().add(itemLabel);
                itemsBox.getChildren().add(itemBox);
            }
        } else {
            Label noItemsLabel = new Label("No items available");
            noItemsLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic;");
            itemsBox.getChildren().add(noItemsLabel);
        }

        HBox actionBox = new HBox(10);
        actionBox.getStyleClass().add("order-actions");
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        if ("findingCourier".equals(type)) {
            Button acceptButton = new Button("✅ Accept Delivery");
            acceptButton.getStyleClass().add("accept-button");
            acceptButton.setOnAction(e -> handleAcceptDelivery(order));
            actionBox.getChildren().addAll(acceptButton);
        } else if ("onTheWay".equals(type)) {
            Button deliverButton = new Button("🚚 Deliver");
            deliverButton.getStyleClass().add("deliver-button");
            deliverButton.setOnAction(e -> handleDeliverOrder(order));
            actionBox.getChildren().addAll(deliverButton);
        }

        Region separator = new Region();
        separator.getStyleClass().add("content-separator");

        content.getChildren().addAll(headerBox, separator, addressBox, itemsBox, actionBox);
        return content;
    }

    private String getStatusIcon(String status) {
        switch (status.toLowerCase()) {
            case "pending": return "⏳";
            case "confirmed": return "✅";
            case "preparing": return "👨‍🍳";
            case "ready": return "🎯";
            case "delivered": return "✨";
            case "on the way": return "🚚";
            case "finding courier": return "🔍";
            default: return "📦";
        }
    }

    private String getStatusColor(String status) {
        switch (status.toLowerCase()) {
            case "pending": return "#f39c12";
            case "confirmed": return "#27ae60";
            case "preparing": return "#3498db";
            case "ready": return "#e74c3c";
            case "delivered": return "#95a5a6";
            case "on the way": return "#3498db";
            case "finding courier": return "#f1c40f";
            default: return "#7f8c8d";
        }
    }

    private void addEntranceAnimation(TitledPane pane, long delay) {
        pane.setOpacity(0);
        pane.setScaleX(0.8);
        pane.setScaleY(0.8);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), pane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setDelay(Duration.millis(delay));
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(400), pane);
        scaleIn.setFromX(0.8);
        scaleIn.setFromY(0.8);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);
        scaleIn.setDelay(Duration.millis(delay));
        fadeIn.play();
        scaleIn.play();
    }

    private void showLoadingState() {
        findingCourierAccordion.getPanes().clear();
        onTheWayAccordion.getPanes().clear();

        VBox loadingContainer = new VBox(20);
        loadingContainer.setAlignment(Pos.CENTER);
        loadingContainer.setPadding(new Insets(50));

        Label loadingIcon = new Label("🔄");
        loadingIcon.setStyle(
                "-fx-font-size: 48px; " +
                        "-fx-rotate: 0;"
        );

        Label loadingText = new Label("Loading available deliveries...");
        loadingText.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-text-fill: #7f8c8d;"
        );

        loadingContainer.getChildren().addAll(loadingIcon, loadingText);
        rootPane.setCenter(loadingContainer);
    }

    private void showEmptyState() {
        if (emptyStateContainer != null) {
            rootPane.setCenter(emptyStateContainer);
            emptyStateContainer.setVisible(true);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), emptyStateContainer);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        }
    }

    private void hideEmptyState() {
        if (emptyStateContainer != null) {
            emptyStateContainer.setVisible(false);
        }
    }

    private void showStatusMessage(String message, String textColor, String backgroundColor) {
        if (statusMessageLbl != null) {
            statusMessageLbl.setText(message);
            statusMessageLbl.setStyle(
                    "-fx-text-fill: " + textColor + "; " +
                            "-fx-background-color: " + backgroundColor + "; " +
                            "-fx-padding: 10px; " +
                            "-fx-background-radius: 5px;"
            );
            statusMessageLbl.setVisible(true);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), statusMessageLbl);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            FadeTransition fadeOut = new FadeTransition(Duration.millis(3000), statusMessageLbl);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setDelay(Duration.millis(2000));
            fadeIn.play();
            fadeOut.play();
            fadeOut.setOnFinished(e -> statusMessageLbl.setVisible(false));
        }
    }

    private void showRefreshButton() {
        if (refreshBtn != null) {
            refreshBtn.setVisible(true);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), refreshBtn);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        }
    }

    private void hideRefreshButton() {
        if (refreshBtn != null) {
            refreshBtn.setVisible(false);
        }
    }

    private void updateStatistics() {
        int findingCount = findingCourierOrders.size();
        int onTheWayCount = onTheWayOrders.size();
        System.out.println("Finding Courier: " + findingCount + ", On The Way: " + onTheWayCount);
    }

    private boolean isNearbyOrder(Order order) {
        return Math.random() > 0.7;
    }

    private void updateLastUpdatedTime() {
        if (lastUpdatedLabel != null) {
            String timeString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            lastUpdatedLabel.setText("Last updated: " + timeString);
        }
    }

    private void handleAcceptDelivery(Order order) {
        showStatusMessage("Processing accept request...", "#3498db", "#e8f5e9");
        Task<Void> acceptTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                HttpHeaders headers = getAuthHeaders();
                String url = BASE_URL + "/deliveries/" + order.getId();
                String requestBody = objectMapper.writeValueAsString(new HashMap<String, String>() {{
                    put("status", "accepted");
                }});
                HttpResponse response = HttpController.sendRequest(url, HttpMethod.PUT, requestBody, headers);
                if (response.getStatusCode() != 200) {
                    throw new RuntimeException("Failed to accept delivery: " + response.getBody());
                }
                order.setStatus("on the way");
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    findingCourierOrders.remove(order);
                    onTheWayOrders.add(order);
                    updateDeliveryUI();
                    showStatusMessage("✅ Delivery accepted! Status updated to 'on the way'.", "#27ae60", "#e8f5e9");
                    PauseTransition delay = new PauseTransition(Duration.millis(500));
                    delay.setOnFinished(e -> loadAvailableDeliveries());
                    delay.play();
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showStatusMessage("❌ Failed to accept delivery: " + getException().getMessage(), "#e74c3c", "#fff5f5");
                });
            }
        };
        new Thread(acceptTask).start();
    }

    private void handleDeliverOrder(Order order) {
        showStatusMessage("Processing deliver request...", "#3498db", "#e8f5e9");
        Task<Void> deliverTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                HttpHeaders headers = getAuthHeaders();
                String url = BASE_URL + "/deliveries/" + order.getId();
                String requestBody = objectMapper.writeValueAsString(new HashMap<String, String>() {{
                    put("status", "delivered");
                }});
                HttpResponse response = HttpController.sendRequest(url, HttpMethod.PUT, requestBody, headers);
                if (response.getStatusCode() != 200) {
                    throw new RuntimeException("Failed to deliver order: " + response.getBody());
                }
                order.setStatus("delivered");
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    onTheWayOrders.remove(order);
                    updateDeliveryUI();
                    showStatusMessage("✅ Order delivered! Status updated to 'delivered'.", "#27ae60", "#e8f5e9");
                    PauseTransition delay = new PauseTransition(Duration.millis(500));
                    delay.setOnFinished(e -> loadAvailableDeliveries());
                    delay.play();
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showStatusMessage("❌ Failed to deliver order: " + getException().getMessage(), "#e74c3c", "#fff5f5");
                });
            }
        };
        new Thread(deliverTask).start();
    }

    @FXML
    private void handleCheckAgain() {
        showStatusMessage("Refreshing deliveries...", "#3498db", "#e8f5e9");
        loadAvailableDeliveries();
    }

    @FXML
    private void onBackBtnAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/FoodOrder/client/view/home.fxml"));
            Parent homePage = loader.load();
            Scene homeScene = new Scene(homePage);
            Stage stage = (Stage) backBtn.getScene().getWindow();
            stage.setScene(homeScene);
            stage.show();
        } catch (IOException e) {
            showStatusMessage("❌ Failed to load home page: " + e.getMessage(), "#e74c3c", "#fff5f5");
        } finally {
            cleanup();
        }
    }

    private HttpHeaders getAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + UserSession.getToken());
        headers.set("Content-Type", "application/json");
        return headers;
    }

    private void cleanup() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    public void shutdown() {
        cleanup();
    }
}