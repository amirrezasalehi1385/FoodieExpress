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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.FoodOrder.client.http.HttpController;
import org.FoodOrder.client.http.HttpHeaders;
import org.FoodOrder.client.http.HttpMethod;
import org.FoodOrder.client.http.HttpResponse;
import org.FoodOrder.client.models.Restaurant;
import org.FoodOrder.client.sessions.UserSession;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

public class SeeAllRestaurantsController implements Initializable {
    @FXML private VBox restaurantContainer;
    @FXML private TextField searchField;
    @FXML private BorderPane rootPane;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private PauseTransition searchPause;

    @FXML
    private void onSearchClicked() {
        loadRestaurants();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/FoodOrder/client/view/footer.fxml"));
            Parent footer = loader.load();
            FooterController footerController = loader.getController();
            footerController.setActive("restaurant");
            rootPane.setBottom(footer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        setupAutoSearch();
        loadRestaurants();
    }

    private void setupAutoSearch() {
        searchPause = new PauseTransition(Duration.millis(500));
        searchPause.setOnFinished(event -> loadRestaurants());

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchPause.stop();
            searchPause.play();
        });

        searchField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                searchField.setStyle(
                        "-fx-background-color: #ffffff; " +
                                "-fx-border-color: #3498db; " +
                                "-fx-border-width: 2; " +
                                "-fx-border-radius: 25; " +
                                "-fx-background-radius: 25; " +
                                "-fx-padding: 0 20 0 20; " +
                                "-fx-font-size: 14px; " +
                                "-fx-prompt-text-fill: #6c757d; " +
                                "-fx-effect: dropshadow(gaussian, rgba(52, 152, 219, 0.2), 8, 0, 0, 2);"
                );
            } else {
                searchField.setStyle(
                        "-fx-background-color: #f8f9fa; " +
                                "-fx-border-color: #dee2e6; " +
                                "-fx-border-width: 2; " +
                                "-fx-border-radius: 25; " +
                                "-fx-background-radius: 25; " +
                                "-fx-padding: 0 20 0 20; " +
                                "-fx-font-size: 14px; " +
                                "-fx-prompt-text-fill: #6c757d; " +
                                "-fx-effect: innershadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);"
                );
            }
        });
    }

    private void loadRestaurants() {
        showLoadingIndicator();

        Task<Void> task = new Task<>() {
            private List<Restaurant> restaurants;
            private Set<Integer> favoriteIds;

            @Override
            protected Void call() throws Exception {
                String token = UserSession.getToken();
                String baseUrl = "http://localhost:8082/vendors";

                String search = searchField.getText() != null ? searchField.getText().trim() : "";
                StringBuilder urlBuilder = new StringBuilder(baseUrl);
                if (!search.isEmpty()) {
                    urlBuilder.append("?search=").append(java.net.URLEncoder.encode(search, StandardCharsets.UTF_8));
                }

                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + token);
                HttpResponse restaurantResponse = HttpController.sendRequest(urlBuilder.toString(), HttpMethod.GET, null, headers);

                if (restaurantResponse.getStatusCode() == 200) {
                    restaurants = objectMapper.readValue(restaurantResponse.getBody(), new TypeReference<List<Restaurant>>() {});
                } else {
                    throw new IOException("Failed to load restaurants");
                }

                // Load favorites
                HttpResponse favoriteResponse = HttpController.sendRequest("http://localhost:8082/favorites", HttpMethod.GET, null, headers);
                if (favoriteResponse.getStatusCode() == 200) {
                    List<Restaurant> favorites = objectMapper.readValue(favoriteResponse.getBody(), new TypeReference<List<Restaurant>>() {});
                    favoriteIds = favorites.stream().map(Restaurant::getId).collect(Collectors.toSet());
                } else {
                    favoriteIds = Set.of();
                }
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    restaurantContainer.getChildren().clear();
                    if (restaurants != null && !restaurants.isEmpty()) {
                        createRestaurantGrid(restaurants, favoriteIds);
                    } else {
                        showEmptyState();
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    restaurantContainer.getChildren().clear();
                    showErrorState();
                });
                getException().printStackTrace();
            }
        };

        new Thread(task).start();
    }

    private Node createRestaurantCard(Restaurant restaurant, boolean isFavorite) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.TOP_CENTER);
        card.setMaxWidth(380);
        card.setMinHeight(420);
        card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: transparent; " +
                        "-fx-border-radius: 15; " +
                        "-fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5); " +
                        "-fx-cursor: hand;"
        );

        card.setOnMouseEntered(e -> {
            card.setStyle(
                    "-fx-background-color: white; " +
                            "-fx-border-color: transparent; " +
                            "-fx-border-radius: 15; " +
                            "-fx-background-radius: 15; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0, 0, 8); " +
                            "-fx-cursor: hand; " +
                            "-fx-scale-x: 1.02; " +
                            "-fx-scale-y: 1.02;"
            );
        });

        card.setOnMouseExited(e -> {
            card.setStyle(
                    "-fx-background-color: white; " +
                            "-fx-border-color: transparent; " +
                            "-fx-border-radius: 15; " +
                            "-fx-background-radius: 15; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5); " +
                            "-fx-cursor: hand; " +
                            "-fx-scale-x: 1.0; " +
                            "-fx-scale-y: 1.0;"
            );
        });

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(340, 280);
        imageContainer.setMaxSize(340, 280);
        imageContainer.setStyle(
                "-fx-background-color: #f8f9fa; " +
                        "-fx-background-radius: 10; " +
                        "-fx-border-radius: 10;"
        );

        ImageView imageView = new ImageView();
        imageView.setFitHeight(140);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);

        Rectangle clip = new Rectangle(340, 280);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        imageView.setClip(clip);

        boolean imageLoaded = false;
        if (restaurant.getLogoBase64() != null && !restaurant.getLogoBase64().isEmpty()) {
            try {
                byte[] imageBytes = Base64.getDecoder().decode(restaurant.getLogoBase64());
                Image image = new Image(new ByteArrayInputStream(imageBytes));
                if (!image.isError()) {
                    imageView.setImage(image);
                    imageContainer.getChildren().add(imageView);
                    imageLoaded = true;
                }
            } catch (Exception e) {
                System.err.println("Error loading image for restaurant: " + restaurant.getName());
            }
        }

        if (!imageLoaded) {
            VBox placeholderContainer = new VBox(10);
            placeholderContainer.setAlignment(Pos.CENTER);
            placeholderContainer.setPrefSize(340, 280);
            placeholderContainer.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #f8f9fa, #e9ecef); " +
                            "-fx-background-radius: 10;"
            );

            Label placeholderIcon = new Label("🏪");
            placeholderIcon.setStyle(
                    "-fx-font-size: 64px; " +
                            "-fx-text-fill: #adb5bd;"
            );

            Label placeholderText = new Label("No Image Available");
            placeholderText.setStyle(
                    "-fx-font-size: 14px; " +
                            "-fx-text-fill: #6c757d; " +
                            "-fx-font-weight: normal;"
            );

            placeholderContainer.getChildren().addAll(placeholderIcon, placeholderText);
            imageContainer.getChildren().add(placeholderContainer);
        }

        Button favoriteButton = new Button(isFavorite ? "❤" : "♡");
        favoriteButton.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-font-size: 20px; " +
                        "-fx-text-fill: " + (isFavorite ? "#ff0000" : "#000000") + "; " +
                        "-fx-padding: 5; " +
                        "-fx-cursor: hand;"
        );
        favoriteButton.setOnAction(e -> toggleFavorite(restaurant.getId(), favoriteButton));

        StackPane.setAlignment(favoriteButton, Pos.TOP_RIGHT);
        StackPane.setMargin(favoriteButton, new Insets(10));
        imageContainer.getChildren().add(favoriteButton);

        VBox infoContainer = new VBox(8);
        infoContainer.setAlignment(Pos.CENTER_LEFT);
        infoContainer.setPadding(new Insets(5, 0, 0, 0));

        Label nameLabel = new Label(restaurant.getName());
        nameLabel.setStyle(
                "-fx-font-size: 20px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #2c3e50; " +
                        "-fx-wrap-text: true;"
        );
        nameLabel.setMaxWidth(320);

        HBox addressContainer = new HBox(8);
        addressContainer.setAlignment(Pos.CENTER_LEFT);

        Label addressIcon = new Label("📍");
        addressIcon.setStyle("-fx-font-size: 14px;");

        Label addressLabel = new Label(restaurant.getAddress());
        addressLabel.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-text-fill: #7f8c8d; " +
                        "-fx-wrap-text: true;"
        );
        addressLabel.setMaxWidth(290);

        addressContainer.getChildren().addAll(addressIcon, addressLabel);

        HBox ratingContainer = new HBox(5);
        ratingContainer.setAlignment(Pos.CENTER_LEFT);

        Double avgRating = restaurant.getAverageRating();
        int fullStars = (int) Math.floor(avgRating != null ? avgRating : 0.0);
        boolean hasHalfStar = (avgRating != null && avgRating - fullStars >= 0.5);

        for (int i = 0; i < 5; i++) {
            Label star = new Label(i < fullStars ? "★" : (i == fullStars && hasHalfStar ? "⯪" : "☆"));
            star.setStyle(
                    "-fx-font-size: 14px; " +
                            "-fx-text-fill: " + (i < fullStars || (i == fullStars && hasHalfStar) ? "#f39c12" : "#d3d3d3") + ";"
            );
            ratingContainer.getChildren().add(star);
        }

        Label ratingValueLabel = new Label(String.format("(%.1f)", avgRating != null ? avgRating : 0.0));
        ratingValueLabel.setStyle(
                "-fx-font-size: 12px; " +
                        "-fx-text-fill: #95a5a6;"
        );
        ratingContainer.getChildren().add(ratingValueLabel);

        Button viewButton = new Button("View Menu");
        viewButton.setPrefWidth(320);
        viewButton.setPrefHeight(35);
        viewButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #3498db, #2ecc71); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
        );

        viewButton.setOnMouseEntered(e -> {
            viewButton.setStyle(
                    "-fx-background-color: linear-gradient(to right, #2980b9, #27ae60); " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 14px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-border-radius: 8; " +
                            "-fx-background-radius: 8; " +
                            "-fx-cursor: hand;"
            );
        });

        viewButton.setOnMouseExited(e -> {
            viewButton.setStyle(
                    "-fx-background-color: linear-gradient(to right, #3498db, #2ecc71); " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 14px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-border-radius: 8; " +
                            "-fx-background-radius: 8; " +
                            "-fx-cursor: hand;"
            );
        });

        viewButton.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/FoodOrder/client/view/listOfFoodItems.fxml"));
                Parent page = loader.load();
                SeeFoodItemsController controller = loader.getController();
                controller.setRestaurant(restaurant);
                Stage currentStage = (Stage) viewButton.getScene().getWindow();
                currentStage.setScene(new Scene(page));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        infoContainer.getChildren().addAll(nameLabel, addressContainer, ratingContainer);
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.SOMETIMES);
        card.getChildren().addAll(imageContainer, infoContainer, spacer, viewButton);

        return card;
    }

    private void toggleFavorite(int restaurantId, Button favoriteButton) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + UserSession.getToken());

        HttpMethod method = favoriteButton.getText().equals("♡") ? HttpMethod.POST : HttpMethod.DELETE;
        String url = "http://localhost:8082/favorites/" + restaurantId;

        new Thread(() -> {
            try {
                HttpResponse response = HttpController.sendRequest(url, method, null, headers);
                Platform.runLater(() -> {
                    if (response.getStatusCode() == 200) {
                        favoriteButton.setText(method == HttpMethod.POST ? "❤" : "♡");
                        favoriteButton.setStyle(
                                "-fx-background-color: transparent; " +
                                        "-fx-font-size: 20px; " +
                                        "-fx-text-fill: " + (method == HttpMethod.POST ? "#ff0000" : "#000000") + "; " +
                                        "-fx-padding: 5; " +
                                        "-fx-cursor: hand;"
                        );
                    } else {
                        System.err.println("Failed to toggle favorite: " + response.getBody());
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> System.err.println("Network error: " + e.getMessage()));
            }
        }).start();
    }

    private void showLoadingIndicator() {
        restaurantContainer.getChildren().clear();

        VBox loadingContainer = new VBox(20);
        loadingContainer.setAlignment(Pos.CENTER);
        loadingContainer.setPadding(new Insets(50));

        Label loadingIcon = new Label("🔄");
        loadingIcon.setStyle(
                "-fx-font-size: 48px; " +
                        "-fx-rotate: 0;"
        );

        Label loadingText = new Label("Loading delicious restaurants...");
        loadingText.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-text-fill: #7f8c8d;"
        );

        loadingContainer.getChildren().addAll(loadingIcon, loadingText);
        restaurantContainer.getChildren().add(loadingContainer);
    }

    private void createRestaurantGrid(List<Restaurant> restaurants, Set<Integer> favoriteIds) {
        restaurantContainer.getChildren().clear();
        VBox mainContainer = new VBox(25);
        HBox currentRow = null;
        int itemsPerRow = 2;
        int currentCount = 0;

        for (Restaurant restaurant : restaurants) {
            if (currentCount % itemsPerRow == 0) {
                currentRow = new HBox(20);
                currentRow.setAlignment(Pos.CENTER);
                currentRow.setPadding(new Insets(0, 20, 0, 20));
                mainContainer.getChildren().add(currentRow);
            }

            Node restaurantCard = createRestaurantCard(restaurant, favoriteIds.contains(restaurant.getId()));
            currentRow.getChildren().add(restaurantCard);
            currentCount++;
        }

        restaurantContainer.getChildren().add(mainContainer);
    }

    private void showEmptyState() {
        VBox emptyContainer = new VBox(20);
        emptyContainer.setAlignment(Pos.CENTER);
        emptyContainer.setPadding(new Insets(50));

        Label emptyIcon = new Label("🍽️");
        emptyIcon.setStyle(
                "-fx-font-size: 64px; " +
                        "-fx-opacity: 0.5;"
        );

        Label emptyTitle = new Label("No Restaurants Found");
        emptyTitle.setStyle(
                "-fx-font-size: 24px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #2c3e50;"
        );

        Label emptyDescription = new Label("Try adjusting your search criteria or check back later for new restaurants.");
        emptyDescription.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-text-fill: #7f8c8d; " +
                        "-fx-text-alignment: center; " +
                        "-fx-wrap-text: true;"
        );
        emptyDescription.setMaxWidth(400);

        emptyContainer.getChildren().addAll(emptyIcon, emptyTitle, emptyDescription);
        restaurantContainer.getChildren().add(emptyContainer);
    }

    private void showErrorState() {
        VBox errorContainer = new VBox(20);
        errorContainer.setAlignment(Pos.CENTER);
        errorContainer.setPadding(new Insets(50));

        Label errorIcon = new Label("⚠️");
        errorIcon.setStyle(
                "-fx-font-size: 64px; " +
                        "-fx-opacity: 0.7;"
        );

        Label errorTitle = new Label("Something Went Wrong");
        errorTitle.setStyle(
                "-fx-font-size: 24px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #e74c3c;"
        );

        Label errorDescription = new Label("We couldn't load the restaurants. Please check your connection and try again.");
        errorDescription.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-text-fill: #7f8c8d; " +
                        "-fx-text-alignment: center; " +
                        "-fx-wrap-text: true;"
        );
        errorDescription.setMaxWidth(400);

        Button retryButton = new Button("Try Again");
        retryButton.setStyle(
                "-fx-background-color: #3498db; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 10 20; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
        );
        retryButton.setOnAction(e -> loadRestaurants());

        errorContainer.getChildren().addAll(errorIcon, errorTitle, errorDescription, retryButton);
        restaurantContainer.getChildren().add(errorContainer);
    }
}