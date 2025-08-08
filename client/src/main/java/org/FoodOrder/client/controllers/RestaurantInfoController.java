package org.FoodOrder.client.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.geometry.Pos;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.FoodOrder.client.http.HttpController;
import org.FoodOrder.client.http.HttpHeaders;
import org.FoodOrder.client.http.HttpMethod;
import org.FoodOrder.client.http.HttpResponse;
import org.FoodOrder.client.models.FoodItem;
import org.FoodOrder.client.models.Restaurant;
import org.FoodOrder.client.sessions.UserSession;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.ResourceBundle;

public class RestaurantInfoController implements Initializable {
    @FXML
    private Label nameLabel;
    @FXML
    private Label phoneLabel;
    @FXML
    private Label addressLabel;
    @FXML
    private Button addFoodItemBtn;
    private String newBase64Image = null;
    @FXML
    private Button cancleBtn;
    @FXML
    private ImageView profileImageView;
    private Restaurant restaurant;
    @FXML private Label statusMessageLbl;
    @FXML private GridPane foodItemsGrid;
    @FXML private Label dropdownArrow;
    @FXML private HBox dropdownHeader;
    @FXML private Button closeBtn;
    @FXML private Button addFoodBtn;
    private boolean isDropdownExpanded = false;
    private Label emptyListLabel;
    private List<FoodItem> foodItems = new ArrayList<>();
    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
        nameLabel.setText(restaurant.getName());
        phoneLabel.setText(restaurant.getAddress());
        addressLabel.setText(restaurant.getPhone());
        if (restaurant.getLogoBase64() != null && !restaurant.getLogoBase64().isEmpty()) {
            byte[] decodedBytes = Base64.getDecoder().decode(restaurant.getLogoBase64());
            Image image = new Image(new ByteArrayInputStream(decodedBytes));
            profileImageView.setImage(image);
            setupCircularClipping();
        }
        refreshFoodItemsList();
    }
    @FXML
    public void onCancleBtnAction(ActionEvent event) {
        try {
            Parent page = FXMLLoader.load(getClass().getResource("/org/FoodOrder/client/view/sellerRestaurant.fxml"));
            Stage stage = (Stage) cancleBtn.getScene().getWindow();
            stage.setScene(new Scene(page));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    private void refreshFoodItemsList() {
        foodItemsGrid.getChildren().clear();
        try {
            String userId = UserSession.getToken();
            String url = "http://localhost:8082/restaurants/" + restaurant.getId() + "/myItems";
            System.out.println(restaurant.getId());
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + UserSession.getToken());
            HttpResponse response = HttpController.sendRequest(url, HttpMethod.GET, null, headers);
            if (response.getStatusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                List<FoodItem> items = mapper.readValue(response.getBody(), new TypeReference<List<FoodItem>>() {});

                if (items.isEmpty()) {
                    VBox emptyBox = new VBox();
                    emptyBox.setAlignment(Pos.CENTER);
                    emptyBox.setStyle("-fx-padding: 40; -fx-background-color: #f8f9fa; -fx-background-radius: 15;");

                    Label emptyIcon = new Label("🍽️");
                    emptyIcon.setStyle("-fx-font-size: 48; -fx-text-fill: #bdc3c7;");

                    Label emptyMessage = new Label("No food items added yet");
                    emptyMessage.setStyle("-fx-font-size: 18; -fx-text-fill: #7f8c8d; -fx-font-weight: 600;");

                    Label emptySubtext = new Label("Add your first food item to get started");
                    emptySubtext.setStyle("-fx-font-size: 14; -fx-text-fill: #95a5a6;");

                    emptyBox.getChildren().addAll(emptyIcon, emptyMessage, emptySubtext);
                    foodItemsGrid.add(emptyBox, 0, 0, 2, 1);
                } else {
                    int row = 0, col = 0;
                    for (FoodItem item : items) {
                        VBox foodCard = createFoodItemCard(item);
                        foodItemsGrid.add(foodCard, col, row);

                        col++;
                        if (col >= 2) {
                            col = 0;
                            row++;
                        }
                    }
                }
                foodItemsGrid.setVisible(true);
                foodItemsGrid.setManaged(true);
            } else {
                System.out.println("Warning" + response.getStatusCode() + response.getBody());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private VBox createFoodItemCard(FoodItem item) {
        VBox card = new VBox(15);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(Region.USE_COMPUTED_SIZE);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMinWidth(250);
        card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 20; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 20, 0, 0, 8); " +
                        "-fx-padding: 25; " +
                        "-fx-border-color: #f1f3f4; " +
                        "-fx-border-radius: 20; " +
                        "-fx-border-width: 1;"
        );

        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 20; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 25, 0, 0, 12); " +
                        "-fx-padding: 25; " +
                        "-fx-border-color: #667eea; " +
                        "-fx-border-radius: 20; " +
                        "-fx-border-width: 2; " +
                        "-fx-scale-x: 1.02; " +
                        "-fx-scale-y: 1.02;"
        ));

        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 20; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 20, 0, 0, 8); " +
                        "-fx-padding: 25; " +
                        "-fx-border-color: #f1f3f4; " +
                        "-fx-border-radius: 20; " +
                        "-fx-border-width: 1; " +
                        "-fx-scale-x: 1.0; " +
                        "-fx-scale-y: 1.0;"
        ));
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(200, 150);
        imageContainer.setMaxSize(200, 150);
        imageContainer.setStyle(
                "-fx-background-color: #f8f9fa; " +
                        "-fx-background-radius: 15; " +
                        "-fx-border-color: #667eea; " +
                        "-fx-border-radius: 15; " +
                        "-fx-border-width: 2;"
        );

        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(130);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        Rectangle clip = new Rectangle(180, 130);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        imageView.setClip(clip);

        if (item.getImageBase64() != null && !item.getImageBase64().isEmpty()) {
            try {
                byte[] decodedBytes = Base64.getDecoder().decode(item.getImageBase64());
                imageView.setImage(new Image(new ByteArrayInputStream(decodedBytes)));
                imageContainer.getChildren().add(imageView);
            } catch (IllegalArgumentException e) {
                Label defaultIcon = new Label("🍽️");
                defaultIcon.setStyle("-fx-font-size: 48; -fx-text-fill: #667eea;");
                imageContainer.getChildren().add(defaultIcon);
            }
        } else {
            Label defaultIcon = new Label("🍽️");
            defaultIcon.setStyle("-fx-font-size: 48; -fx-text-fill: #667eea;");
            imageContainer.getChildren().add(defaultIcon);
        }

        VBox infoBox = new VBox(10);
        infoBox.setAlignment(Pos.CENTER);

        Label nameLabel = new Label(item.getName());
        nameLabel.setStyle(
                "-fx-text-fill: #2c3e50; " +
                        "-fx-font-size: 18; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-alignment: center;"
        );
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(220);

        Label priceLabel = new Label("$" + item.getPrice());
        priceLabel.setStyle(
                "-fx-text-fill: #27ae60; " +
                        "-fx-font-size: 16; " +
                        "-fx-font-weight: 700; " +
                        "-fx-text-alignment: center; " +
                        "-fx-background-color: rgba(39, 174, 96, 0.1); " +
                        "-fx-background-radius: 15; " +
                        "-fx-padding: 5 12;"
        );

        infoBox.getChildren().addAll(nameLabel, priceLabel);
        HBox buttonsBox = new HBox(12);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setPadding(new Insets(10, 0, 0, 0));

        Button editBtn = new Button("Edit");
        editBtn.setPrefWidth(90);
        editBtn.setPrefHeight(40);
        editBtn.setStyle(
                "-fx-background-color: #667eea; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 20; " +
                        "-fx-font-size: 13; " +
                        "-fx-font-weight: 600; " +
                        "-fx-cursor: hand;"
        );

        editBtn.setOnMouseEntered(e -> editBtn.setStyle(
                "-fx-background-color: #5a6fd8; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 20; " +
                        "-fx-font-size: 13; " +
                        "-fx-font-weight: 600; " +
                        "-fx-cursor: hand; " +
                        "-fx-scale-x: 1.05; " +
                        "-fx-scale-y: 1.05;"
        ));

        editBtn.setOnMouseExited(e -> editBtn.setStyle(
                "-fx-background-color: #667eea; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 20; " +
                        "-fx-font-size: 13; " +
                        "-fx-font-weight: 600; " +
                        "-fx-cursor: hand;"
        ));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setPrefWidth(90);
        deleteBtn.setPrefHeight(40);
        deleteBtn.setStyle(
                "-fx-background-color: #ff6b6b; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 20; " +
                        "-fx-font-size: 13; " +
                        "-fx-font-weight: 600; " +
                        "-fx-cursor: hand;"
        );

        deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle(
                "-fx-background-color: #ff5252; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 20; " +
                        "-fx-font-size: 13; " +
                        "-fx-font-weight: 600; " +
                        "-fx-cursor: hand; " +
                        "-fx-scale-x: 1.05; " +
                        "-fx-scale-y: 1.05;"
        ));

        deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle(
                "-fx-background-color: #ff6b6b; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 20; " +
                        "-fx-font-size: 13; " +
                        "-fx-font-weight: 600; " +
                        "-fx-cursor: hand;"
        ));
        deleteBtn.setOnAction(e -> {
            try {
                String url = "http://localhost:8082/restaurants/" + restaurant.getId() + "/item/" + item.getId();
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + UserSession.getToken());

                HttpResponse response = HttpController.sendRequest(url, HttpMethod.DELETE, null, headers);

                if (response.getStatusCode() == 200) {
                    foodItemsGrid.getChildren().remove(card);
                    refreshFoodItemsList();
                    System.out.println("Item deleted successfully.");
                } else {
                    System.out.println("Failed to delete item. Status: " + response.getStatusCode());
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        buttonsBox.getChildren().addAll(editBtn, deleteBtn);
        card.getChildren().addAll(imageContainer, infoBox, buttonsBox);
        editBtn.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/FoodOrder/client/view/editFoodItem.fxml"));
                Parent page = loader.load();
                EditFoodItemController controller = loader.getController();
                controller.setFoodItem(item);
                controller.setRestaurant(this.restaurant);
                Stage currentStage = (Stage) editBtn.getScene().getWindow();
                currentStage.setScene(new Scene(page));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        return card;
    }

    @FXML
    public void addFoodItemActionBtn(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/FoodOrder/client/view/createFoodItem.fxml"));
            Parent page = loader.load();
            FoodFormController foodFormController = loader.getController();
            foodFormController.setRestaurant(this.restaurant);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(page));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onCloseActionBtn(ActionEvent event) {
        try {
            Parent homePage = FXMLLoader.load(getClass().getResource("/org/FoodOrder/client/view/sellerRestaurant.fxml"));
            Scene homeScene = new Scene(homePage);
            Stage currentStage = (Stage) closeBtn.getScene().getWindow();
            currentStage.setScene(homeScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        foodItems.clear();
        if (foodItemsGrid != null) {
            foodItemsGrid.setHgap(25);
            foodItemsGrid.setVgap(25);
            foodItemsGrid.setPadding(new Insets(20));
            foodItemsGrid.setMaxWidth(Double.MAX_VALUE);
            foodItemsGrid.getColumnConstraints().clear();
            ColumnConstraints col1 = new ColumnConstraints();
            col1.setPercentWidth(50);
            col1.setHgrow(javafx.scene.layout.Priority.ALWAYS);
            col1.setFillWidth(true);
            col1.setMinWidth(Region.USE_COMPUTED_SIZE);
            ColumnConstraints col2 = new ColumnConstraints();
            col2.setPercentWidth(50);
            col2.setHgrow(javafx.scene.layout.Priority.ALWAYS);
            col2.setFillWidth(true);
            col2.setMinWidth(Region.USE_COMPUTED_SIZE);

            foodItemsGrid.getColumnConstraints().addAll(col1, col2);
        }

        foodItemsGrid.setVisible(false);
        foodItemsGrid.setManaged(false);    }

    private void setupCircularClipping() {
        Circle clip = new Circle();
        clip.setRadius(40);
        clip.setCenterX(40);
        clip.setCenterY(40);
        profileImageView.setClip(clip);
        profileImageView.setFitWidth(80);
        profileImageView.setFitHeight(80);
        profileImageView.setPreserveRatio(false);
        profileImageView.setSmooth(true);
    }
}