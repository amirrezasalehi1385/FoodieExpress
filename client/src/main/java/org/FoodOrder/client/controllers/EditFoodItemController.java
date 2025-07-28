package org.FoodOrder.client.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.FoodOrder.client.http.HttpController;
import org.FoodOrder.client.http.HttpHeaders;
import org.FoodOrder.client.http.HttpMethod;
import org.FoodOrder.client.http.HttpResponse;
import org.FoodOrder.client.models.FoodItem;
import org.FoodOrder.client.models.Restaurant;
import org.FoodOrder.client.sessions.UserSession;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Base64;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.HBox;
import java.util.ArrayList;


public class EditFoodItemController implements Initializable {
    @FXML private VBox imagePlaceholder;
    @FXML private Button uploadImageBtn;
    @FXML private Button removeImageBtn;
    @FXML private FlowPane badgeContainer;
    @FXML private TextField nameInput;
    @FXML private TextArea descriptionInput;
    @FXML private TextField priceInput;
    @FXML private TextField supplyInput;
    @FXML private Button addCategoryBtn;
    @FXML private Button cancelBtn;
    @FXML private Button editBtn;
    @FXML private Label statusMessageLbl;
    @FXML private ImageView profileImageView;
    private Restaurant restaurant;
    private FoodItem foodItem;
    private int restaurantId;
    private String newBase64Image;
    private ObservableList<String> categories = FXCollections.observableArrayList();



    public void setFoodItem(FoodItem foodItem) {
        this.foodItem = foodItem;
        nameInput.setText(foodItem.getName());
        descriptionInput.setText(foodItem.getDescription());
        priceInput.setText(String.valueOf(foodItem.getPrice()));
        supplyInput.setText(String.valueOf(foodItem.getSupply()));
        if (foodItem.getImageBase64() != null && !foodItem.getImageBase64().isEmpty()) {
            try {
                byte[] decodedBytes = Base64.getDecoder().decode(foodItem.getImageBase64());
                Image image = new Image(new ByteArrayInputStream(decodedBytes));
                profileImageView.setImage(image);
                imagePlaceholder.setVisible(false);
                profileImageView.setVisible(true);
                removeImageBtn.setVisible(true);
            } catch (IllegalArgumentException e) {
                statusMessageLbl.setText("Error loading image");
                statusMessageLbl.setStyle("-fx-text-fill: #ff0000;");
            }
        }
        if (foodItem.getCategories() != null) {
            categories.addAll(foodItem.getCategories());
            updateBadgeContainer();
        }
    }
    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    @FXML
    public void onEditBtnAction(ActionEvent event) {
        try {
            if (nameInput.getText().isEmpty() || descriptionInput.getText().isEmpty() ||
                    priceInput.getText().isEmpty() || supplyInput.getText().isEmpty()) {
                statusMessageLbl.setText("All fields are required");
                statusMessageLbl.setStyle("-fx-text-fill: #ff0000;");
                return;
            }

            int price;
            int supply;
            try {
                price = Integer.parseInt(priceInput.getText());
                supply = Integer.parseInt(supplyInput.getText());
                if (price < 0 || supply <= 0) {
                    statusMessageLbl.setText("Price must be non-negative and supply must be positive");
                    statusMessageLbl.setStyle("-fx-text-fill: #ff0000;");
                    return;
                }
            } catch (NumberFormatException e) {
                statusMessageLbl.setText("Invalid price or supply format");
                statusMessageLbl.setStyle("-fx-text-fill: #ff0000;");
                return;
            }

            FoodItem updatedItem = new FoodItem();
            updatedItem.setName(nameInput.getText());
            updatedItem.setDescription(descriptionInput.getText());
            updatedItem.setPrice(price);
            updatedItem.setSupply(supply);
            updatedItem.setImageBase64(newBase64Image != null ? newBase64Image : foodItem.getImageBase64());
            updatedItem.setCategories(new ArrayList<>(categories));
            String url = "http://localhost:8082/restaurants/" + restaurant.getId() + "/item/" + foodItem.getId();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + UserSession.getToken());
            headers.set("Content-Type", "application/json");

            ObjectMapper mapper = new ObjectMapper();
            String requestBody = mapper.writeValueAsString(updatedItem);
            HttpResponse response = HttpController.sendRequest(url, HttpMethod.PUT, requestBody, headers);
            System.out.println(response.getStatusCode() + response.getBody());
            if (response.getStatusCode() == 200) {
                statusMessageLbl.setText("Food item updated successfully");
                statusMessageLbl.setStyle("-fx-text-fill: #008000;");
                returnToRestaurantInfo(event);
            } else if (response.getStatusCode() == 400) {
                statusMessageLbl.setText("Invalid input: " + response.getBody());
                statusMessageLbl.setStyle("-fx-text-fill: #ff0000;");
            } else if (response.getStatusCode() == 401) {
                statusMessageLbl.setText("Unauthorized: Please log in again");
                statusMessageLbl.setStyle("-fx-text-fill: #ff0000;");
            } else if (response.getStatusCode() == 404) {
                statusMessageLbl.setText("Food item or restaurant not found");
                statusMessageLbl.setStyle("-fx-text-fill: #ff0000;");
            } else {
                statusMessageLbl.setText("Error: " + response.getBody());
                statusMessageLbl.setStyle("-fx-text-fill: #ff0000;");
            }
        } catch (IOException e) {
            e.printStackTrace();
            statusMessageLbl.setText("Error connecting to server");
            statusMessageLbl.setStyle("-fx-text-fill: #ff0000;");
        }
    }

    @FXML
    public void onCancelBtnAction(ActionEvent event) {
        returnToRestaurantInfo(event);
    }

    @FXML
    public void onUploadImageBtnAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (file != null) {
            try {
                byte[] fileContent = Files.readAllBytes(file.toPath());
                newBase64Image = Base64.getEncoder().encodeToString(fileContent);
                Image image = new Image(new ByteArrayInputStream(fileContent));
                profileImageView.setImage(image);
                imagePlaceholder.setVisible(false);
                profileImageView.setVisible(true);
                removeImageBtn.setVisible(true);
                statusMessageLbl.setText("Image uploaded successfully");
                statusMessageLbl.setStyle("-fx-text-fill: #008000;");
            } catch (IOException e) {
                e.printStackTrace();
                statusMessageLbl.setText("Error uploading image");
                statusMessageLbl.setStyle("-fx-text-fill: #ff0000;");
            }
        }
    }

    @FXML
    public void onRemoveImageBtnAction(ActionEvent event) {
        newBase64Image = null;
        profileImageView.setImage(null);
        profileImageView.setVisible(false);
        imagePlaceholder.setVisible(true);
        removeImageBtn.setVisible(false);
        statusMessageLbl.setText("Image removed");
        statusMessageLbl.setStyle("-fx-text-fill: #008000;");
    }

    @FXML
    public void onAddCategoryBtnAction(ActionEvent event) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Add New Category");
        ButtonType addButtonType = new ButtonType("Add Category", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        TextField categoryInput = new TextField();
        categoryInput.setPromptText("Category name");
        categoryInput.setStyle(
                "-fx-background-radius: 10; -fx-padding: 8; -fx-border-color: #ced4da; -fx-border-radius: 10; -fx-background-color: white;"
        );
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.getChildren().add(categoryInput);
        dialog.getDialogPane().setContent(content);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return categoryInput.getText().trim();
            }
            return null;
        });
        dialog.showAndWait().ifPresent(categoryName -> {
            if (!categoryName.isEmpty() && !categories.contains(categoryName)) {
                categories.add(categoryName);
                updateBadgeContainer();
                statusMessageLbl.setText("Category added");
                statusMessageLbl.setStyle("-fx-text-fill: #008000;");
            } else {
                statusMessageLbl.setText("Category is empty or already exists");
                statusMessageLbl.setStyle("-fx-text-fill: #ff0000;");
            }
        });
    }

    private void removeCategory(String category) {
        categories.remove(category);
        updateBadgeContainer();
        statusMessageLbl.setText("Category removed");
        statusMessageLbl.setStyle("-fx-text-fill: #008000;");
    }

    private void updateBadgeContainer() {
        badgeContainer.getChildren().clear();
        for (String category : categories) {
            HBox badgeBox = new HBox(5);
            badgeBox.setAlignment(Pos.CENTER_LEFT);
            Label badge = new Label(category);
            badge.setStyle(
                    "-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 5 10; -fx-font-size: 12;"
            );
            Button removeBadgeBtn = new Button("✖");
            removeBadgeBtn.setStyle(
                    "-fx-background-color: #dc3545; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 2 6; -fx-font-size: 10;"
            );
            removeBadgeBtn.setOnAction(e -> removeCategory(category));
            badgeBox.getChildren().addAll(badge, removeBadgeBtn);
            badgeContainer.getChildren().add(badgeBox);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        profileImageView.setFitWidth(190);
        profileImageView.setFitHeight(190);
        profileImageView.setPreserveRatio(true);
        profileImageView.setSmooth(true);
        setupButtonHoverEffects();
    }

    private void setupButtonHoverEffects() {
        editBtn.setOnMouseEntered(e -> editBtn.setStyle(
                "-fx-background-color: rgba(41,128,185,0.9); -fx-text-fill: white; -fx-background-radius: 25; -fx-padding: 12 25; -fx-font-weight: bold; -fx-font-size: 14; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 3);"
        ));
        editBtn.setOnMouseExited(e -> editBtn.setStyle(
                "-fx-background-color: rgba(52,152,219,0.9); -fx-text-fill: white; -fx-background-radius: 25; -fx-padding: 12 25; -fx-font-weight: bold; -fx-font-size: 14; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);"
        ));
        cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle(
                "-fx-background-color: rgba(192,57,43,0.9); -fx-text-fill: white; -fx-background-radius: 25; -fx-padding: 12 25; -fx-font-weight: bold; -fx-font-size: 14; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 3);"
        ));
        cancelBtn.setOnMouseExited(e -> cancelBtn.setStyle(
                "-fx-background-color: rgba(231,76,60,0.9); -fx-text-fill: white; -fx-background-radius: 25; -fx-padding: 12 25; -fx-font-weight: bold; -fx-font-size: 14; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);"
        ));
        uploadImageBtn.setOnMouseEntered(e -> uploadImageBtn.setStyle(
                "-fx-background-color: #218838; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 20; -fx-font-weight: bold;"
        ));
        uploadImageBtn.setOnMouseExited(e -> uploadImageBtn.setStyle(
                "-fx-background-color: #28a745; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 20; -fx-font-weight: bold;"
        ));
        removeImageBtn.setOnMouseEntered(e -> removeImageBtn.setStyle(
                "-fx-background-color: #c82333; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 20;"
        ));
        removeImageBtn.setOnMouseExited(e -> removeImageBtn.setStyle(
                "-fx-background-color: #dc3545; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 20;"
        ));

        addCategoryBtn.setOnMouseEntered(e -> addCategoryBtn.setStyle(
                "-fx-background-color: #138496; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 12;"
        ));
        addCategoryBtn.setOnMouseExited(e -> addCategoryBtn.setStyle(
                "-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 12;"
        ));
    }

    private void returnToRestaurantInfo(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/FoodOrder/client/view/restaurantInfo.fxml"));
            Parent page = loader.load();
            RestaurantInfoController controller = loader.getController();
            controller.setRestaurant(this.restaurant);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(page));
        } catch (IOException e) {
            e.printStackTrace();
            statusMessageLbl.setText("Error returning to restaurant info");
            statusMessageLbl.setStyle("-fx-text-fill: #ff0000;");
        }
    }
}