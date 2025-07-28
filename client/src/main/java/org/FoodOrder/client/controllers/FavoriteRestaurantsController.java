package org.FoodOrder.client.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
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

public class FavoriteRestaurantsController implements Initializable {
    @FXML private VBox restaurantContainer;
    @FXML private BorderPane rootPane;
    @FXML private Button backButton;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/FoodOrder/client/view/footer.fxml"));
            Parent footer = loader.load();
            FooterController footerController = loader.getController();
            footerController.setActive("favorites");
            rootPane.setBottom(footer);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Failed to load footer: " + e.getMessage());
        }
        backButton.setOnAction(e -> goToHome());
        backButton.setOnMouseEntered(e -> backButton.setStyle(
                "-fx-background-color: #c0392b; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 10 20; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0.5, 0.0, 2.0);"
        ));
        backButton.setOnMouseExited(e -> backButton.setStyle(
                "-fx-background-color: #e74c3c; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 10 20; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0.5, 0.0, 2.0);"
        ));

        loadFavoriteRestaurants();
    }

    private void goToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/FoodOrder/client/view/home.fxml"));
            Parent page = loader.load();
            Stage currentStage = (Stage) backButton.getScene().getWindow();
            currentStage.setScene(new Scene(page));
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Failed to load home page: " + e.getMessage());
        }
    }

    private void loadFavoriteRestaurants() {
        showLoadingIndicator();
        Task<List<Restaurant>> task = new Task<>() {
            @Override
            protected List<Restaurant> call() throws Exception {
                String token = UserSession.getToken();
                String url = "http://localhost:8082/favorites";
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + token);
                HttpResponse response = HttpController.sendRequest(url, HttpMethod.GET, null, headers);

                if (response.getStatusCode() == 200) {
                    return objectMapper.readValue(response.getBody(), new TypeReference<List<Restaurant>>() {});
                } else {
                    System.out.println("Error loading favorites: " + response.getBody());
                    return null;
                }
            }
        };

        task.setOnSucceeded(event -> {
            List<Restaurant> restaurants = task.getValue();
            Platform.runLater(() -> {
                restaurantContainer.getChildren().clear();
                if (restaurants != null && !restaurants.isEmpty()) {
                    createRestaurantGrid(restaurants);
                } else {
                    showEmptyState();
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                restaurantContainer.getChildren().clear();
                showErrorState();
            });
            System.err.println("Failed to load favorites: " + task.getException().getMessage());
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }

    private Node createRestaurantCard(Restaurant restaurant) {
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

        HBox ratingContainer = new HBox(10);
        ratingContainer.setAlignment(Pos.CENTER_LEFT);

        Label ratingLabel = new Label("⭐ " + String.format("%.1f", restaurant.getAverageRating() != null ? restaurant.getAverageRating() : 0.0));
        ratingLabel.setStyle(
                "-fx-font-size: 14px; " +
        "-fx-text-fill: #f39c12; " +
                "-fx-font-weight: bold;"
        );

        Label reviewsLabel = new Label("(120 reviews)");
        reviewsLabel.setStyle(
                "-fx-font-size: 12px; " +
                        "-fx-text-fill: #95a5a6; " +
                        "-fx-font-weight: normal;"
        );
        ratingContainer.getChildren().addAll(ratingLabel, reviewsLabel);

        Button deleteButton = new Button("Delete");
        deleteButton.setPrefWidth(320);
        deleteButton.setPrefHeight(35);
        deleteButton.setStyle(
                "-fx-background-color: #e74c3c; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
        );

        deleteButton.setOnMouseEntered(e -> {
            deleteButton.setStyle(
                    "-fx-background-color: #c0392b; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 14px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-border-radius: 8; " +
                            "-fx-background-radius: 8; " +
                            "-fx-cursor: hand;"
            );
        });

        deleteButton.setOnMouseExited(e -> {
            deleteButton.setStyle(
                    "-fx-background-color: #e74c3c; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 14px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-border-radius: 8; " +
                            "-fx-background-radius: 8; " +
                            "-fx-cursor: hand;"
            );
        });

        deleteButton.setOnAction(e -> removeFavorite(restaurant.getId(), card));

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
                showErrorAlert("Failed to load menu: " + ex.getMessage());
            }
        });

        infoContainer.getChildren().addAll(nameLabel, addressContainer, ratingContainer);
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.SOMETIMES);
        card.getChildren().addAll(imageContainer, infoContainer, spacer, viewButton, deleteButton);

        return card;
    }

    private void removeFavorite(int restaurantId, Node card) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + UserSession.getToken());
        String url = "http://localhost:8082/favorites/" + restaurantId;

        new Thread(() -> {
            try {
                HttpResponse response = HttpController.sendRequest(url, HttpMethod.DELETE, null, headers);
                Platform.runLater(() -> {
                    if (response.getStatusCode() == 200) {
                        // پیدا کردن HBox والد کارت
                        Node parent = card.getParent();
                        if (parent instanceof HBox) {
                            HBox parentHBox = (HBox) parent;
                            parentHBox.getChildren().remove(card);
                            // اگر HBox خالی شد، اون رو از mainContainer حذف کن
                            if (parentHBox.getChildren().isEmpty()) {
                                Node grandParent = parentHBox.getParent();
                                if (grandParent instanceof VBox) {
                                    ((VBox) grandParent).getChildren().remove(parentHBox);
                                }
                            }
                            // اگر هیچ کارتی باقی نموند، حالت خالی رو نمایش بده
                            if (restaurantContainer.getChildren().isEmpty() ||
                                    (restaurantContainer.getChildren().size() == 1 &&
                                            ((VBox) restaurantContainer.getChildren().get(0)).getChildren().isEmpty())) {
                                showEmptyState();
                            }
                        }
                    } else {
                        showErrorAlert("Failed to remove favorite: " + response.getBody());
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> showErrorAlert("Network error: " + e.getMessage()));
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

        Label loadingText = new Label("Loading your favorite restaurants...");
        loadingText.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-text-fill: #7f8c8d;"
        );

        loadingContainer.getChildren().addAll(loadingIcon, loadingText);
        restaurantContainer.getChildren().add(loadingContainer);
    }

    private void createRestaurantGrid(List<Restaurant> restaurants) {
        restaurantContainer.getChildren().clear();
        VBox mainContainer = new VBox(25);
        mainContainer.setAlignment(Pos.CENTER);
        HBox currentRow = null;
        int itemsPerRow = 2;
        int currentCount = 0;

        for (Restaurant restaurant : restaurants) {
            if (currentCount % itemsPerRow == 0) {
                currentRow = new HBox(20);
                currentRow.setAlignment(Pos.CENTER);
                currentRow.setPadding(new Insets(0));
                currentRow.setPrefWidth(760);
                mainContainer.getChildren().add(currentRow);
            }

            Node restaurantCard = createRestaurantCard(restaurant);
            currentRow.getChildren().add(restaurantCard);
            currentCount++;
        }

        restaurantContainer.getChildren().add(mainContainer);
    }

    private void showEmptyState() {
        restaurantContainer.getChildren().clear();

        VBox emptyContainer = new VBox(20);
        emptyContainer.setAlignment(Pos.CENTER);
        emptyContainer.setPadding(new Insets(50));

        Label emptyIcon = new Label("🍽️");
        emptyIcon.setStyle(
                "-fx-font-size: 64px; " +
                        "-fx-opacity: 0.5;"
        );

        Label emptyTitle = new Label("No Favorite Restaurants");
        emptyTitle.setStyle(
                "-fx-font-size: 24px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #2c3e50;"
        );

        Label emptyDescription = new Label("You haven't added any restaurants to your favorites yet.");
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
        restaurantContainer.getChildren().clear();

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

        Label errorDescription = new Label("We couldn't load your favorite restaurants. Please check your connection and try again.");
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
        retryButton.setOnAction(e -> loadFavoriteRestaurants());

        errorContainer.getChildren().addAll(errorIcon, errorTitle, errorDescription, retryButton);
        restaurantContainer.getChildren().add(errorContainer);
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred");
        alert.setContentText(message);
        alert.showAndWait();
    }
}