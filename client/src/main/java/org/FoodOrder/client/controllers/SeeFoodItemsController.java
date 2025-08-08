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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
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
import org.controlsfx.control.RangeSlider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SeeFoodItemsController implements Initializable {
    @FXML private Label nameLabel;
    @FXML private Label phoneLabel;
    @FXML private Label addressLabel;
    @FXML private ImageView profileImageView;
    @FXML private Label statusMessageLbl;
    @FXML private GridPane foodItemsGrid;
    @FXML private Label dropdownArrow;
    @FXML private HBox dropdownHeader;
    @FXML private Button closeBtn;
    @FXML private TextField searchField;
    @FXML private RangeSlider priceRangeSlider;
    @FXML private Label minPriceLabel;
    @FXML private Label maxPriceLabel;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private Button cartBtn;
    @FXML private BorderPane rootPane;
    private final Map<Long, VBox> foodItemCardCache = new HashMap<>();
    private static final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private Restaurant restaurant;
    private List<FoodItem> foodItems = new ArrayList<>();
    private PauseTransition searchPause;
    private Cart cart;
    private PauseTransition cartUpdateTimer;
    private Map<Long, Integer> localSupplyCache = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        foodItems.clear();
        setupCartUpdateTimer();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/FoodOrder/client/view/footer.fxml"));
        Parent footer;
        try {
            footer = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        FooterController footerController = loader.getController();
        footerController.setActive("restaurant");
        rootPane.setBottom(footer);

        if (foodItemsGrid != null) {
            foodItemsGrid.setHgap(20);
            foodItemsGrid.setVgap(20);
            foodItemsGrid.setPadding(new Insets(15));
            foodItemsGrid.setMaxWidth(Double.MAX_VALUE);
            foodItemsGrid.getColumnConstraints().clear();
            ColumnConstraints col1 = new ColumnConstraints();
            col1.setPercentWidth(33.33);
            col1.setHgrow(Priority.ALWAYS);
            col1.setFillWidth(true);
            ColumnConstraints col2 = new ColumnConstraints();
            col2.setPercentWidth(33.33);
            col2.setHgrow(Priority.ALWAYS);
            col2.setFillWidth(true);
            ColumnConstraints col3 = new ColumnConstraints();
            col3.setPercentWidth(33.33);
            col3.setHgrow(Priority.ALWAYS);
            col3.setFillWidth(true);
            foodItemsGrid.getColumnConstraints().addAll(col1, col2, col3);
        }

        foodItemsGrid.setVisible(false);
        foodItemsGrid.setManaged(false);

        setupSearchField();
        setupPriceRangeSlider();
        setupCategoryComboBox();
    }

    private void setupCartUpdateTimer() {
        cartUpdateTimer = new PauseTransition(Duration.millis(100));
        cartUpdateTimer.setOnFinished(event -> updateCartButtonDisplay());
        cartUpdateTimer.play();
    }

    private void updateCartButtonDisplay() {
        Platform.runLater(() -> {
            if (cart != null) {
                int totalItems = cart.getItems().stream().mapToInt(CartItem::getCount).sum();
                if (totalItems > 0) {
                    cartBtn.setText("🛒 Cart (" + totalItems + ")");
                    cartBtn.setDisable(false);
                    cartBtn.setStyle("-fx-background-color: linear-gradient(to right, #e74c3c, #c0392b); -fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold; -fx-padding: 12 20; -fx-border-radius: 25; -fx-background-radius: 25; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(231, 76, 60, 0.4), 10, 0, 0, 3);");
                    cartBtn.setOnMouseEntered(e -> cartBtn.setStyle("-fx-background-color: linear-gradient(to right, #c0392b, #a93226); -fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold; -fx-padding: 12 20; -fx-border-radius: 25; -fx-background-radius: 25; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(231, 76, 60, 0.5), 12, 0, 0, 4);"));
                    cartBtn.setOnMouseExited(e -> cartBtn.setStyle("-fx-background-color: linear-gradient(to right, #e74c3c, #c0392b); -fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold; -fx-padding: 12 20; -fx-border-radius: 25; -fx-background-radius: 25; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(231, 76, 60, 0.4), 10, 0, 0, 3);"));
                } else {
                    cartBtn.setText("🛒 Cart");
                    cartBtn.setDisable(true);
                    cartBtn.setStyle("-fx-background-color: #bdc3c7; -fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold; -fx-padding: 12 20; -fx-border-radius: 25; -fx-background-radius: 25; -fx-opacity: 0.7; -fx-cursor: default;");
                    cartBtn.setOnMouseEntered(null);
                    cartBtn.setOnMouseExited(null);
                }
            }
        });
    }

    private void setupSearchField() {
        searchPause = new PauseTransition(Duration.millis(500));
        searchPause.setOnFinished(event -> refreshFoodItemsList());
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchPause.stop();
            searchPause.play();
        });
        searchField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) searchField.setStyle("-fx-background-color: #ffffff; -fx-border-color: #3498db; -fx-border-width: 2; -fx-border-radius: 25; -fx-background-radius: 25; -fx-padding: 0 20 0 20; -fx-font-size: 14; -fx-prompt-text-fill: #6c757d; -fx-effect: dropshadow(gaussian, rgba(52, 152, 219, 0.2), 8, 0, 0, 2);");
            else searchField.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 2; -fx-border-radius: 25; -fx-background-radius: 25; -fx-padding: 0 20 0 20; -fx-font-size: 14; -fx-prompt-text-fill: #6c757d; -fx-effect: innershadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);");
        });
    }

    private void setupPriceRangeSlider() {
        priceRangeSlider.setMin(0);
        priceRangeSlider.setMax(50);
        priceRangeSlider.setLowValue(0);
        priceRangeSlider.setHighValue(50);
        priceRangeSlider.setMajorTickUnit(12);
        priceRangeSlider.setMinorTickCount(1);
        priceRangeSlider.setShowTickLabels(true);
        priceRangeSlider.setShowTickMarks(true);

        priceRangeSlider.lowValueProperty().addListener((obs, oldVal, newVal) -> {
            minPriceLabel.setText("$" + newVal.intValue());
            refreshFoodItemsList();
        });
        priceRangeSlider.highValueProperty().addListener((obs, oldVal, newVal) -> {
            maxPriceLabel.setText("$" + newVal.intValue());
            refreshFoodItemsList();
        });

        minPriceLabel.setText("$" + (int) priceRangeSlider.getLowValue());
        maxPriceLabel.setText("$" + (int) priceRangeSlider.getHighValue());
    }

    private void setupCategoryComboBox() {
        categoryComboBox.getItems().addAll("Pizza", "Burger", "Pasta", "Salad", "Dessert");
        categoryComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> refreshFoodItemsList());
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
        nameLabel.setText(restaurant.getName());
        phoneLabel.setText(restaurant.getPhone());
        addressLabel.setText(restaurant.getAddress());
        if (restaurant.getLogoBase64() != null && !restaurant.getLogoBase64().isEmpty()) {
            byte[] decodedBytes = Base64.getDecoder().decode(restaurant.getLogoBase64());
            Image image = new Image(new ByteArrayInputStream(decodedBytes));
            profileImageView.setImage(image);
            setupCircularClipping();
        }
        loadCartFromServerAndRefresh();
    }

    private void loadCartFromServerAndRefresh() {
        Task<Cart> cartTask = new Task<>() {
            @Override
            protected Cart call() throws Exception {
                String url = "http://localhost:8082/cart";
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + UserSession.getToken());
                HttpResponse response = HttpController.sendRequest(url, HttpMethod.GET, null, headers);
                if (response.getStatusCode() == 200) {
                    return objectMapper.readValue(response.getBody(), Cart.class);
                }
                return new Cart();
            }
        };

        cartTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                cart = cartTask.getValue();
                updateCartButtonDisplay();
                refreshFoodItemsList();
            });
        });

        cartTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                Throwable ex = cartTask.getException();
                System.err.println("❌ Failed to load cart: " + (ex != null ? ex.getMessage() : "Unknown error"));
                cart = new Cart();
                refreshFoodItemsList();
            });
        });

        executorService.submit(cartTask);
    }

    private void refreshFoodItemsList() {
        showLoadingIndicator();
        Task<List<FoodItem>> task = new Task<>() {
            @Override
            protected List<FoodItem> call() throws Exception {
                StringBuilder urlBuilder = new StringBuilder("http://localhost:8082/vendors/" + restaurant.getId() + "/item");
                boolean hasQuery = false;
                String search = searchField.getText() != null ? searchField.getText().trim() : "";
                if (!search.isEmpty()) {
                    urlBuilder.append(hasQuery ? "&" : "?").append("search=").append(URLEncoder.encode(search, StandardCharsets.UTF_8));
                    hasQuery = true;
                }
                int minPrice = (int) priceRangeSlider.getLowValue();
                int maxPrice = (int) priceRangeSlider.getHighValue();
                if (minPrice > 0) {
                    urlBuilder.append(hasQuery ? "&" : "?").append("minPrice=").append(minPrice);
                    hasQuery = true;
                }
                if (maxPrice < 200000) {
                    urlBuilder.append(hasQuery ? "&" : "?").append("maxPrice=").append(maxPrice);
                    hasQuery = true;
                }
                String selectedCategory = categoryComboBox.getSelectionModel().getSelectedItem();
                if (selectedCategory != null) {
                    urlBuilder.append(hasQuery ? "&" : "?").append("categories=").append(URLEncoder.encode(selectedCategory, StandardCharsets.UTF_8));
                    hasQuery = true;
                }

                String url = urlBuilder.toString();
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + UserSession.getToken());
                HttpResponse response = HttpController.sendRequest(url, HttpMethod.GET, null, headers);
                if (response.getStatusCode() == 200) {
                    List<FoodItem> items = objectMapper.readValue(response.getBody(), new TypeReference<List<FoodItem>>() {});
                    items.forEach(item -> localSupplyCache.put(item.getId(), item.getSupply()));
                    return items;
                }
                return new ArrayList<>();
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                foodItemsGrid.getChildren().clear();
                foodItems = task.getValue();
                if (foodItems.isEmpty()) {
                    VBox emptyBox = new VBox(10);
                    emptyBox.setAlignment(Pos.CENTER);
                    emptyBox.setStyle("-fx-padding: 40; -fx-background-color: #f8f9fa; -fx-background-radius: 15;");
                    Label emptyIcon = new Label("🍽️");
                    emptyIcon.setStyle("-fx-font-size: 48; -fx-text-fill: #bdc3c7;");
                    Label emptyMessage = new Label("No food items found");
                    emptyMessage.setStyle("-fx-font-size: 18; -fx-text-fill: #7f8c8d; -fx-font-weight: 600;");
                    Label emptySubtext = new Label("Try adjusting your search or filters");
                    emptySubtext.setStyle("-fx-font-size: 14; -fx-text-fill: #95a5a6;");
                    emptyBox.getChildren().addAll(emptyIcon, emptyMessage, emptySubtext);
                    foodItemsGrid.add(emptyBox, 0, 0, 3, 1);
                } else {
                    int row = 0, col = 0;
                    for (FoodItem item : foodItems) {
                        VBox card = foodItemCardCache.computeIfAbsent(item.getId(), id -> createFoodItemCard(item));
                        foodItemsGrid.add(card, col, row);
                        col++;
                        if (col >= 3) {
                            col = 0;
                            row++;
                        }
                        updateCardQuantity(card, item);
                    }
                }
                foodItemsGrid.setVisible(true);
                foodItemsGrid.setManaged(true);
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                foodItemsGrid.getChildren().clear();
                VBox errorBox = new VBox(10);
                errorBox.setAlignment(Pos.CENTER);
                errorBox.setStyle("-fx-padding: 40; -fx-background-color: #f8f9fa; -fx-background-radius: 15;");
                Label errorIcon = new Label("⚠️");
                errorIcon.setStyle("-fx-font-size: 48; -fx-text-fill: #e74c3c;");
                Label errorMessage = new Label("Failed to load food items");
                errorMessage.setStyle("-fx-font-size: 18; -fx-text-fill: #e74c3c; -fx-font-weight: 600;");
                Label errorSubtext = new Label("Please check your connection and try again.");
                errorSubtext.setStyle("-fx-font-size: 14; -fx-text-fill: #95a5a6;");
                Button retryButton = new Button("Try Again");
                retryButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold; -fx-padding: 10 20; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;");
                retryButton.setOnAction(e -> refreshFoodItemsList());
                errorBox.getChildren().addAll(errorIcon, errorMessage, errorSubtext, retryButton);
                foodItemsGrid.add(errorBox, 0, 0, 3, 1);
                foodItemsGrid.setVisible(true);
                foodItemsGrid.setManaged(true);
                System.err.println("❌ Failed to load food items: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
            });
        });

        executorService.submit(task);
    }

    private void showLoadingIndicator() {
        foodItemsGrid.getChildren().clear();
        VBox loadingBox = new VBox(10);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setStyle("-fx-padding: 40; -fx-background-color: #f8f9fa; -fx-background-radius: 15;");

        Label loadingIcon = new Label("🔄");
        loadingIcon.setStyle("-fx-font-size: 48; -fx-text-fill: #3498db;");

        Label loadingMessage = new Label("Loading food items...");
        loadingMessage.setStyle("-fx-font-size: 18; -fx-text-fill: #7f8c8d; -fx-font-weight: 600;");

        loadingBox.getChildren().addAll(loadingIcon, loadingMessage);
        foodItemsGrid.add(loadingBox, 0, 0, 2, 1);
        foodItemsGrid.setVisible(true);
        foodItemsGrid.setManaged(true);
    }

    private VBox createFoodItemCard(FoodItem item) {
        VBox card = new VBox(0);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(Region.USE_COMPUTED_SIZE);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMinWidth(250);
        card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.5, 0, 2); " +
                        "-fx-border-color: #e0e0e0; " +
                        "-fx-border-radius: 12; " +
                        "-fx-border-width: 1;"
        );

        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0.5, 0, 4); " +
                        "-fx-border-color: #2196F3; " +
                        "-fx-border-radius: 12; " +
                        "-fx-border-width: 1;"
        ));

        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.5, 0, 2); " +
                        "-fx-border-color: #e0e0e0; " +
                        "-fx-border-radius: 12; " +
                        "-fx-border-width: 1;"
        ));

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(160);
        imageContainer.setMaxHeight(160);
        imageContainer.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 12 12 0 0;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(250);
        imageView.setFitHeight(160);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        if (item.getImageBase64() != null && !item.getImageBase64().isEmpty()) {
            try {
                byte[] decodedBytes = Base64.getDecoder().decode(item.getImageBase64());
                Image originalImage = new Image(new ByteArrayInputStream(decodedBytes));
                imageView.setImage(originalImage);

                Rectangle clip = new Rectangle(250, 160);
                clip.setArcWidth(12);
                clip.setArcHeight(12);
                imageView.setClip(clip);

                imageContainer.getChildren().add(imageView);
            } catch (IllegalArgumentException e) {
                Label defaultIcon = new Label("🍽");
                defaultIcon.setStyle("-fx-font-size: 48; -fx-text-fill: #9e9e9e;");
                imageContainer.getChildren().add(defaultIcon);
            }
        } else {
            Label defaultIcon = new Label("🍽");
            defaultIcon.setStyle("-fx-font-size: 48; -fx-text-fill: #9e9e9e;");
            imageContainer.getChildren().add(defaultIcon);
        }

        VBox contentBox = new VBox(12);
        contentBox.setAlignment(Pos.TOP_LEFT);
        contentBox.setPadding(new Insets(16));
        contentBox.setStyle("-fx-background-color: white; -fx-background-radius: 0 0 12 12;");

        Label nameLabel = new Label(item.getName());
        nameLabel.setStyle("-fx-text-fill: #212121; -fx-font-size: 16px; -fx-font-weight: bold;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(220);

        Label priceLabel = new Label("$" + String.format("%.2f", new BigDecimal(item.getPrice())));
        priceLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 18px; -fx-font-weight: bold;");

        VBox cartSection = createAddToCartSection(item);

        contentBox.getChildren().addAll(nameLabel, priceLabel, cartSection);

        card.getChildren().addAll(imageContainer, contentBox);
        return card;
    }

    private VBox createAddToCartSection(FoodItem item) {
        VBox cartSection = new VBox(8);
        cartSection.setAlignment(Pos.CENTER);
        cartSection.setPadding(new Insets(10, 0, 0, 0));
        StackPane cartControlsPane = new StackPane();
        cartControlsPane.setAlignment(Pos.CENTER);

        Button addToCartBtn = new Button("🛒 Add to Cart");
        addToCartBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #667eea, #764ba2); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 12 20; " +
                        "-fx-border-radius: 25; " +
                        "-fx-background-radius: 25; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(102, 126, 234, 0.3), 8, 0, 0, 2);"
        );
        addToCartBtn.setOnMouseEntered(e -> addToCartBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #5a6fd8, #6a42a0); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 12 20; " +
                        "-fx-border-radius: 25; " +
                        "-fx-background-radius: 25; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(102, 126, 234, 0.5), 12, 0, 0, 4);"
        ));
        addToCartBtn.setOnMouseExited(e -> addToCartBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #667eea, #764ba2); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 12 20; " +
                        "-fx-border-radius: 25; " +
                        "-fx-background-radius: 25; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(102, 126, 234, 0.3), 8, 0, 0, 2);"
        ));

        HBox quantityControls = new HBox(15);
        quantityControls.setAlignment(Pos.CENTER);
        quantityControls.setVisible(false);
        quantityControls.setManaged(false);

        Button minusBtn = new Button("−");
        minusBtn.setStyle(
                "-fx-background-color: #e74c3c; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 18; " +
                        "-fx-font-weight: bold; " +
                        "-fx-pref-width: 35; " +
                        "-fx-pref-height: 35; " +
                        "-fx-border-radius: 50; " +
                        "-fx-background-radius: 50; " +
                        "-fx-cursor: hand;"
        );
        minusBtn.setOnMouseEntered(e -> minusBtn.setStyle(
                "-fx-background-color: #c0392b; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 18; " +
                        "-fx-font-weight: bold; " +
                        "-fx-pref-width: 35; " +
                        "-fx-pref-height: 35; " +
                        "-fx-border-radius: 50; " +
                        "-fx-background-radius: 50; " +
                        "-fx-cursor: hand;"
        ));
        minusBtn.setOnMouseExited(e -> minusBtn.setStyle(
                "-fx-background-color: #e74c3c; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 18; " +
                        "-fx-font-weight: bold; " +
                        "-fx-pref-width: 35; " +
                        "-fx-pref-height: 35; " +
                        "-fx-border-radius: 50; " +
                        "-fx-background-radius: 50; " +
                        "-fx-cursor: hand;"
        ));

        Label quantityLabel = new Label("1");
        quantityLabel.setStyle(
                "-fx-text-fill: #2c3e50; " +
                        "-fx-font-size: 16; " +
                        "-fx-font-weight: bold; " +
                        "-fx-min-width: 30; " +
                        "-fx-alignment: center;"
        );

        Button plusBtn = new Button("+");
        plusBtn.setStyle(
                "-fx-background-color: #27ae60; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 18; " +
                        "-fx-font-weight: bold; " +
                        "-fx-pref-width: 35; " +
                        "-fx-pref-height: 35; " +
                        "-fx-border-radius: 50; " +
                        "-fx-background-radius: 50; " +
                        "-fx-cursor: hand;"
        );
        plusBtn.setOnMouseEntered(e -> plusBtn.setStyle(
                "-fx-background-color: #229954; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 18; " +
                        "-fx-font-weight: bold; " +
                        "-fx-pref-width: 35; " +
                        "-fx-pref-height: 35; " +
                        "-fx-border-radius: 50; " +
                        "-fx-background-radius: 50; " +
                        "-fx-cursor: hand;"
        ));
        plusBtn.setOnMouseExited(e -> plusBtn.setStyle(
                "-fx-background-color: #27ae60; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 18; " +
                        "-fx-font-weight: bold; " +
                        "-fx-pref-width: 35; " +
                        "-fx-pref-height: 35; " +
                        "-fx-border-radius: 50; " +
                        "-fx-background-radius: 50; " +
                        "-fx-cursor: hand;"
        ));

        quantityControls.getChildren().addAll(minusBtn, quantityLabel, plusBtn);
        cartControlsPane.getChildren().addAll(addToCartBtn, quantityControls);

        int initialSupply = localSupplyCache.getOrDefault(item.getId(), item.getSupply());
        if (initialSupply <= 0) {
            addToCartBtn.setDisable(true);
            addToCartBtn.setStyle(
                    "-fx-background-color: #bdc3c7; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 14; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 12 20; " +
                            "-fx-border-radius: 25; " +
                            "-fx-background-radius: 25; " +
                            "-fx-cursor: default; " +
                            "-fx-effect: none;"
            );
        }

        addToCartBtn.setOnAction(e -> {
            int supply = localSupplyCache.getOrDefault(item.getId(), item.getSupply());
            if (supply > 0) {
                addToCart(item, 1, addToCartBtn, quantityControls, quantityLabel, plusBtn);
            } else {
                showStatusMessage("❌ Out of stock: " + item.getName(), "#e74c3c", "#fff5f5");
            }
        });

        plusBtn.setOnAction(e -> {
            int currentQuantity = Integer.parseInt(quantityLabel.getText());
            int supply = localSupplyCache.getOrDefault(item.getId(), item.getSupply());
            if (supply > currentQuantity) {
                addToCart(item, 1, null, null, quantityLabel, plusBtn);
            } else {
                showStatusMessage("❌ Out of stock: " + item.getName(), "#e74c3c", "#fff5f5");
            }
        });

        minusBtn.setOnAction(e -> {
            int currentQuantity = Integer.parseInt(quantityLabel.getText());
            if (currentQuantity > 1) {
                removeFromCart(item, 1, quantityLabel, plusBtn);
            } else if (currentQuantity == 1) {
                removeFromCart(item, 1, quantityLabel, plusBtn, addToCartBtn, quantityControls);
            }
        });
        int existingQuantity = cart.getItems().stream()
                .filter(cartItem -> cartItem.getFood().getId() == item.getId())
                .mapToInt(CartItem::getCount)
                .findFirst()
                .orElse(0);
        if (existingQuantity > 0) {
            addToCartBtn.setVisible(false);
            addToCartBtn.setManaged(false);
            quantityControls.setVisible(true);
            quantityControls.setManaged(true);
            quantityLabel.setText(String.valueOf(existingQuantity));
            localSupplyCache.put(item.getId(), localSupplyCache.getOrDefault(item.getId(), item.getSupply()) - existingQuantity);
            if (localSupplyCache.get(item.getId()) <= 0) {
                plusBtn.setDisable(true);
                plusBtn.setStyle(
                        "-fx-background-color: #bdc3c7; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-size: 18; " +
                                "-fx-font-weight: bold; " +
                                "-fx-pref-width: 35; " +
                                "-fx-pref-height: 35; " +
                                "-fx-border-radius: 50; " +
                                "-fx-background-radius: 50; " +
                                "-fx-cursor: default;"
                );
            }
        }

        cartSection.getChildren().add(cartControlsPane);
        return cartSection;
    }

    private void addToCart(FoodItem item, int quantity, Button addToCartBtn, HBox quantityControls, Label quantityLabel, Button plusBtn) {
        Task<HttpResponse> task = new Task<>() {
            @Override
            protected HttpResponse call() throws Exception {
                String url = "http://localhost:8082/cart/add";
                HttpHeaders headers = new HttpHeaders();
                String token = UserSession.getToken();
                if (token == null) {
                    System.err.println("❌ User token is null");
                    return null;
                }
                headers.set("Authorization", "Bearer " + token);

                Map<String, Object> body = new HashMap<>();
                Long foodId = item.getId();
                Long restaurantId = (long) restaurant.getId();
                if (foodId == null || restaurantId == null) {
                    System.err.println("❌ Invalid foodId or restaurantId: foodId=" + foodId + ", restaurantId=" + restaurantId);
                    return null;
                }
                System.out.println("Before send - foodId: " + foodId + ", quantity: " + quantity + ", restaurantId: " + restaurantId);
                body.put("foodId", foodId);
                body.put("quantity", quantity);
                body.put("restaurantId", restaurantId);

                String requestBody = objectMapper.writeValueAsString(body);
                System.out.println("Request body before send: " + requestBody);
                HttpResponse response = HttpController.sendRequest(url, HttpMethod.POST, requestBody, headers);
                System.out.println("Server response body: " + response.getBody());
                return response;
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                HttpResponse response = task.getValue();
                if (response != null && response.getStatusCode() == 200) {
                    cart.addItem(item, quantity);
                    if (addToCartBtn != null && quantityControls != null && quantityLabel != null) {
                        addToCartBtn.setVisible(false);
                        addToCartBtn.setManaged(false);
                        quantityControls.setVisible(true);
                        quantityControls.setManaged(true);
                        quantityLabel.setText(String.valueOf(quantity));
                    } else if (quantityLabel != null) {
                        int newQuantity = Integer.parseInt(quantityLabel.getText()) + quantity;
                        quantityLabel.setText(String.valueOf(newQuantity));
                    }
                    updateCartButtonDisplay();
                    System.out.println("✅ " + item.getName() + " added to cart");
                    int supply = localSupplyCache.getOrDefault(item.getId(), item.getSupply()) - quantity;
                    localSupplyCache.put(item.getId(), supply);
                    if (supply <= 0 && plusBtn != null) {
                        plusBtn.setDisable(true);
                        plusBtn.setStyle(
                                "-fx-background-color: #bdc3c7; " +
                                        "-fx-text-fill: white; " +
                                        "-fx-font-size: 18; " +
                                        "-fx-font-weight: bold; " +
                                        "-fx-pref-width: 35; " +
                                        "-fx-pref-height: 35; " +
                                        "-fx-border-radius: 50; " +
                                        "-fx-background-radius: 50; " +
                                        "-fx-cursor: default;"
                        );
                    }
                } else if (response != null) {
                    System.err.println("❌ Failed to add " + item.getName() + ": Status code " + response.getStatusCode() + ", Body: " + response.getBody());
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                Throwable ex = task.getException();
                System.err.println("❌ Error adding to cart: " + (ex != null ? ex.getMessage() : "Unknown error"));
            });
        });

        executorService.submit(task);
    }

    private void removeFromCart(FoodItem item, int quantity, Label quantityLabel, Button plusBtn, Button addToCartBtn, HBox quantityControls) {
        Task<HttpResponse> task = new Task<>() {
            @Override
            protected HttpResponse call() throws Exception {
                String url = "http://localhost:8082/cart/remove";
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + UserSession.getToken());
                Map<String, Object> body = new HashMap<>();
                body.put("foodId", item.getId());
                body.put("quantity", quantity);
                body.put("restaurantId", restaurant.getId());
                String requestBody = objectMapper.writeValueAsString(body);
                return HttpController.sendRequest(url, HttpMethod.POST, requestBody, headers);
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                HttpResponse response = task.getValue();
                if (response.getStatusCode() == 200) {
                    cart.removeItem(item, quantity);
                    int newQuantity = Integer.parseInt(quantityLabel.getText()) - quantity;
                    if (newQuantity > 0) {
                        quantityLabel.setText(String.valueOf(newQuantity));
                        System.out.println("✅ Quantity updated for " + item.getName());
                        localSupplyCache.put(item.getId(), localSupplyCache.getOrDefault(item.getId(), item.getSupply()) + quantity);
                        plusBtn.setDisable(false);
                        plusBtn.setStyle(
                                "-fx-background-color: #27ae60; " +
                                        "-fx-text-fill: white; " +
                                        "-fx-font-size: 18; " +
                                        "-fx-font-weight: bold; " +
                                        "-fx-pref-width: 35; " +
                                        "-fx-pref-height: 35; " +
                                        "-fx-border-radius: 50; " +
                                        "-fx-background-radius: 50; " +
                                        "-fx-cursor: hand;"
                        );
                    } else {
                        quantityControls.setVisible(false);
                        quantityControls.setManaged(false);
                        addToCartBtn.setVisible(true);
                        addToCartBtn.setManaged(true);
                        System.out.println("✅ " + item.getName() + " removed from cart");
                        localSupplyCache.put(item.getId(), localSupplyCache.getOrDefault(item.getId(), item.getSupply()) + quantity);
                        addToCartBtn.setDisable(localSupplyCache.get(item.getId()) <= 0);
                        if (addToCartBtn.isDisable()) {
                            addToCartBtn.setStyle(
                                    "-fx-background-color: #bdc3c7; " +
                                            "-fx-text-fill: white; " +
                                            "-fx-font-size: 14; " +
                                            "-fx-font-weight: bold; " +
                                            "-fx-padding: 12 20; " +
                                            "-fx-border-radius: 25; " +
                                            "-fx-background-radius: 25; " +
                                            "-fx-cursor: default; " +
                                            "-fx-effect: none;"
                            );
                        } else {
                            addToCartBtn.setStyle(
                                    "-fx-background-color: linear-gradient(to right, #667eea, #764ba2); " +
                                            "-fx-text-fill: white; " +
                                            "-fx-font-size: 14; " +
                                            "-fx-font-weight: bold; " +
                                            "-fx-padding: 12 20; " +
                                            "-fx-border-radius: 25; " +
                                            "-fx-background-radius: 25; " +
                                            "-fx-cursor: hand; " +
                                            "-fx-effect: dropshadow(gaussian, rgba(102, 126, 234, 0.3), 8, 0, 0, 2);"
                            );
                        }
                    }
                    updateCartButtonDisplay();
                } else {
                    System.err.println("❌ Failed to remove " + item.getName() + ": " + response.getBody());
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                Throwable ex = task.getException();
                System.err.println("❌ Error removing from cart: " + (ex != null ? ex.getMessage() : "Unknown error"));
            });
        });

        executorService.submit(task);
    }

    private void removeFromCart(FoodItem item, int quantity, Label quantityLabel, Button plusBtn) {
        removeFromCart(item, quantity, quantityLabel, plusBtn, null, null);
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

    private void updateCardQuantity(VBox card, FoodItem item) {
        if (cart == null) return;
        Node contentBox = card.getChildren().get(1);
        if (contentBox instanceof VBox) {
            VBox cartSection = (VBox) ((VBox) contentBox).getChildren().get(2);
            StackPane cartControlsPane = (StackPane) cartSection.getChildren().get(0);
            Button addToCartBtn = (Button) cartControlsPane.getChildren().get(0);
            HBox quantityControls = (HBox) cartControlsPane.getChildren().get(1);
            Label quantityLabel = (Label) quantityControls.getChildren().get(1);
            Button plusBtn = (Button) quantityControls.getChildren().get(2);

            int existingQuantity = cart.getItems().stream()
                    .filter(cartItem -> cartItem.getFood().getId() == item.getId())
                    .mapToInt(CartItem::getCount)
                    .findFirst()
                    .orElse(0);
            if (existingQuantity > 0) {
                addToCartBtn.setVisible(false);
                addToCartBtn.setManaged(false);
                quantityControls.setVisible(true);
                quantityControls.setManaged(true);
                quantityLabel.setText(String.valueOf(existingQuantity));
                int supply = localSupplyCache.getOrDefault(item.getId(), item.getSupply()) - existingQuantity;
                localSupplyCache.put(item.getId(), supply);
                if (supply <= 0) {
                    plusBtn.setDisable(true);
                    plusBtn.setStyle("-fx-background-color: #bdc3c7; -fx-text-fill: white; -fx-font-size: 18; -fx-font-weight: bold; -fx-pref-width: 35; -fx-pref-height: 35; -fx-border-radius: 50; -fx-background-radius: 50; -fx-cursor: default;");
                }
            } else {
                addToCartBtn.setVisible(true);
                addToCartBtn.setManaged(true);
                quantityControls.setVisible(false);
                quantityControls.setManaged(false);
                addToCartBtn.setDisable(localSupplyCache.getOrDefault(item.getId(), item.getSupply()) <= 0);
                if (addToCartBtn.isDisable()) {
                    addToCartBtn.setStyle("-fx-background-color: #bdc3c7; -fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold; -fx-padding: 12 20; -fx-border-radius: 25; -fx-background-radius: 25; -fx-cursor: default; -fx-effect: none;");
                } else {
                    addToCartBtn.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2); -fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold; -fx-padding: 12 20; -fx-border-radius: 25; -fx-background-radius: 25; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(102, 126, 234, 0.3), 8, 0, 0, 2);");
                }
            }
        }
    }

    @FXML
    private void onCartBtnAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/FoodOrder/client/view/cart.fxml"));
            Parent cartPage = loader.load();
            CartController cartController = loader.getController();
            cartController.setCartData(restaurant, cart, foodItems);
            Scene cartScene = new Scene(cartPage);
            Stage currentStage = (Stage) cartBtn.getScene().getWindow();
            currentStage.setScene(cartScene);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("❌ Error navigating to cart: " + ex.getMessage());
        }
    }

    @FXML
    public void onCloseActionBtn(ActionEvent event) {
        if (cartUpdateTimer != null) cartUpdateTimer.stop();
        try {
            Parent homePage = FXMLLoader.load(getClass().getResource("/org/FoodOrder/client/view/buyerRestaurants.fxml"));
            Scene homeScene = new Scene(homePage);
            Stage currentStage = (Stage) closeBtn.getScene().getWindow();
            currentStage.setScene(homeScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupCircularClipping() {
        Circle clip = new Circle();
        clip.setRadius(55);
        clip.setCenterX(55);
        clip.setCenterY(55);
        profileImageView.setClip(clip);
        profileImageView.setFitWidth(110);
        profileImageView.setFitHeight(110);
        profileImageView.setPreserveRatio(true);
        profileImageView.setSmooth(true);
    }

}