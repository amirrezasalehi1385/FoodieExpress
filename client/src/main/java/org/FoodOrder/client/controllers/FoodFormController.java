package org.FoodOrder.client.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.FoodOrder.client.http.HttpController;
import org.FoodOrder.client.http.HttpHeaders;
import org.FoodOrder.client.http.HttpMethod;
import org.FoodOrder.client.http.HttpResponse;
import org.FoodOrder.client.models.FoodItem;
import org.FoodOrder.client.models.Restaurant;
import org.FoodOrder.client.sessions.UserSession;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
public class FoodFormController {
    @FXML
    private VBox imagePlaceholder;
    @FXML
    private Button uploadImageBtn;
    @FXML
    private Button removeImageBtn;
    @FXML
    private FlowPane badgeContainer;
    @FXML
    private ListView<String> categoriesListView;
    @FXML
    private TextField nameInput;
    @FXML
    private String newBase64Image;
    @FXML
    private TextArea descriptionInput;
    @FXML
    private TextField priceInput;
    @FXML
    private TextField supplyInput;
    @FXML
    private TextField newCategoryField;
    @FXML
    private Button addCategoryBtn;
    @FXML
    private Button cancelBtn;
    @FXML
    private Button createBtn;
    @FXML
    private Label statusMessageLbl;
    @FXML
    private ImageView profileImageView;
    private Restaurant restaurant;
    private int restaurantId;
    public void setRestaurant(Restaurant restaurant){
        this.restaurant = restaurant;
    }
    @FXML
    void initialize() {
        addCategoryBtn.setOnAction(e -> showCategoryDialog());
    }
    private void showCategoryDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Add New Category");
        ButtonType addButtonType = new ButtonType("Add Category", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        TextField categoryInput = new TextField();
        categoryInput.setPromptText("Category name");
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
            if (!categoryName.isEmpty()) {
                addCategoryToList(categoryName);
                addCategoryBadge(categoryName);
            }
        });
    }

    private void addCategoryToList(String category) {
        ObservableList<String> current = categoriesListView.getItems();
        if (!current.contains(category)) {
            current.add(category);
        }
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
    private void addCategoryBadge(String categoryName) {
        Label badge = new Label(categoryName);
        badge.setStyle(
                "-fx-background-color: #6c5ce7; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 6 12; " +
                        "-fx-background-radius: 15;" +
                        "-fx-font-size: 12;" +
                        "-fx-font-weight: bold;"
        );
        badgeContainer.getChildren().add(badge);
    }

    @FXML
    void onCreateAction(ActionEvent event) {
        statusMessageLbl.setText("");
        String name = nameInput.getText().trim();
        String description = descriptionInput.getText().trim();
        String price = priceInput.getText().trim();
        String supply = supplyInput.getText().trim();
        if (name.isEmpty()) {
            statusMessageLbl.setText("❌ Food item name is required");
            statusMessageLbl.setStyle("-fx-text-fill: #ff4444;");
            nameInput.requestFocus();
            return;
        }
        if (description.isEmpty()) {
            statusMessageLbl.setText("❌ Food item description is required");
            statusMessageLbl.setStyle("-fx-text-fill: #ff4444;");
            descriptionInput.requestFocus();
            return;
        }
        if (price.isEmpty()) {
            statusMessageLbl.setText("❌ Food item price is required");
            statusMessageLbl.setStyle("-fx-text-fill: #ff4444;");
            priceInput.requestFocus();
            return;
        }
        if (supply.isEmpty()) {
            statusMessageLbl.setText("❌ Food item supply is required");
            statusMessageLbl.setStyle("-fx-text-fill: #ff4444;");
            supplyInput.requestFocus();
            return;
        }

        int priceValue, supplyValue;
        try {
            priceValue = Integer.parseInt(price);
            supplyValue = Integer.parseInt(supply);
            if (priceValue < 0) {
                statusMessageLbl.setText("❌ Price must be non-negative");
                statusMessageLbl.setStyle("-fx-text-fill: #ff4444;");
                return;
            }
            if (supplyValue <= 0) {
                statusMessageLbl.setText("❌ Supply must be positive");
                statusMessageLbl.setStyle("-fx-text-fill: #ff4444;");
                return;
            }
        } catch (NumberFormatException e) {
            statusMessageLbl.setText("❌ Invalid price or supply format");
            statusMessageLbl.setStyle("-fx-text-fill: #ff4444;");
            return;
        }

        FoodItem item = new FoodItem();
        item.setName(name);
        item.setDescription(description);
        item.setPrice(priceValue);
        item.setSupply(supplyValue);
        item.setCategories(new ArrayList<>(categoriesListView.getItems()));
        if (profileImageView.getImage() != null) {
            try {
                String base64Image = imageToBase64(profileImageView.getImage());
                if (base64Image != null && !base64Image.isEmpty()) {
                    item.setImageBase64(base64Image);
                }
            } catch (IOException e) {
                e.printStackTrace();
                statusMessageLbl.setText("❌ Error processing image");
                statusMessageLbl.setStyle("-fx-text-fill: #ff4444;");
                return;
            }
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonBody = mapper.writeValueAsString(item);
            System.out.println("Request Body: " + jsonBody);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Bearer " + UserSession.getToken());
            String request = "http://localhost:8082/restaurants/" + this.restaurant.getId() + "/item";
            HttpResponse response = HttpController.sendRequest(request, HttpMethod.POST, jsonBody, headers);
            System.out.println("Response Status: " + response.getStatusCode() + ", Body: " + response.getBody());

            if (response.getStatusCode() == 201) {
                ObjectMapper m = new ObjectMapper();
                FoodItem createdFoodItem = m.readValue(response.getBody(), FoodItem.class);
                statusMessageLbl.setText("✅ Food item created successfully! ID: " + createdFoodItem.getId());
                statusMessageLbl.setStyle("-fx-text-fill: #00cc66;");
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/FoodOrder/client/view/restaurantInfo.fxml"));
                    Parent profilePage = loader.load();
                    RestaurantInfoController controller = loader.getController();
                    controller.setRestaurant(this.restaurant);
                    Stage currentStage = (Stage) createBtn.getScene().getWindow();
                    currentStage.setScene(new Scene(profilePage));
                } catch (IOException e) {
                    e.printStackTrace();
                    statusMessageLbl.setText("❌ Navigation error");
                    statusMessageLbl.setStyle("-fx-text-fill: #ff4444;");
                }
            } else if (response.getStatusCode() == 400) {
                statusMessageLbl.setText("❌ Invalid input data: " + response.getBody());
                statusMessageLbl.setStyle("-fx-text-fill: #ff4444;");
            } else if (response.getStatusCode() == 409) {
                statusMessageLbl.setText("❌ Food item with this name already exists");
                statusMessageLbl.setStyle("-fx-text-fill: #ff4444;");
            } else if (response.getStatusCode() == 401) {
                statusMessageLbl.setText("❌ Authentication failed. Please login again");
                statusMessageLbl.setStyle("-fx-text-fill: #ff4444;");
            } else {
                statusMessageLbl.setText("❌ Failed to create food item (Error: " + response.getStatusCode() + ")");
                statusMessageLbl.setStyle("-fx-text-fill: #ff4444;");
            }
        } catch (IOException e) {
            e.printStackTrace();
            statusMessageLbl.setText("❌ Network error: " + e.getMessage());
            statusMessageLbl.setStyle("-fx-text-fill: #ff4444;");
        }
    }
    private String imageToBase64(Image image) throws IOException {
        if (image == null) return null;
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        byte[] bytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(bytes);
    }
}