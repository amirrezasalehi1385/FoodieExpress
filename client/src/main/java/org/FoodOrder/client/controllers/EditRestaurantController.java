package org.FoodOrder.client.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.FoodOrder.client.http.HttpController;
import org.FoodOrder.client.http.HttpHeaders;
import org.FoodOrder.client.http.HttpMethod;
import org.FoodOrder.client.http.HttpResponse;
import org.FoodOrder.client.models.Restaurant;
import org.FoodOrder.client.sessions.UserSession;
import javafx.embed.swing.SwingFXUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

public class EditRestaurantController {
    @FXML private TextField nameInput;
    @FXML private TextArea addressInput;
    @FXML private TextField phoneInput;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;
    @FXML private Label statusMessageLbl;
    @FXML private StackPane selectImagePane;
    @FXML private ImageView profileImageView;
    @FXML private BorderPane rootPane;
    private Restaurant restaurant;
    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
        nameInput.setText(restaurant.getName());
        addressInput.setText(restaurant.getAddress());
        phoneInput.setText(restaurant.getPhone());
        if (restaurant.getLogoBase64() != null && !restaurant.getLogoBase64().isEmpty()) {
            byte[] decodedBytes = Base64.getDecoder().decode(restaurant.getLogoBase64());
            Image image = new Image(new ByteArrayInputStream(decodedBytes));
            profileImageView.setImage(image);
            setupCircularClipping();
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
    @FXML
    void onSaveBtnAction(ActionEvent event) {
        String name = nameInput.getText().trim();
        String address = addressInput.getText().trim();
        String phone = phoneInput.getText().trim();

        if (name.isEmpty()) {
            statusMessageLbl.setText("❌ Restaurant name is required");
            statusMessageLbl.setStyle("-fx-text-fill: #ff4444;");
            nameInput.requestFocus();
            return;
        }
        if (address.isEmpty()) {
            statusMessageLbl.setText("❌ Restaurant address is required");
            statusMessageLbl.setStyle("-fx-text-fill: #ff4444;");
            addressInput.requestFocus();
            return;
        }
        if (phone.isEmpty()) {
            statusMessageLbl.setText("❌ Phone number is required");
            statusMessageLbl.setStyle("-fx-text-fill: #ff4444;");
            phoneInput.requestFocus();
            return;
        }
        if (!phone.matches("\\d{10,15}")) {
            statusMessageLbl.setText("❌ Please enter a valid phone number (10-15 digits)");
            statusMessageLbl.setStyle("-fx-text-fill: #ff4444;");
            phoneInput.requestFocus();
            return;
        }


        restaurant.setName(name);
        restaurant.setAddress(address);
        restaurant.setPhone(phone);
        saveBtn.setDisable(true);
        statusMessageLbl.setText("Updating restaurant...");
        statusMessageLbl.setStyle("-fx-text-fill: #0066cc;");
        if (profileImageView.getImage() != null) {
            try {
                String base64Image = imageToBase64(profileImageView.getImage());
                if (base64Image != null) {
                    restaurant.setLogoBase64(base64Image);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        new Thread(() -> {
            try {
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(restaurant);

                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + UserSession.getToken());
                headers.set("Content-Type", "application/json");

                HttpResponse response = HttpController.sendRequest(
                        "http://localhost:8082/restaurants/" + restaurant.getId(),
                        HttpMethod.PUT,
                        json,
                        headers
                );

                Platform.runLater(() -> {
                    try {
                        if (response.getStatusCode() == 200) {
                            statusMessageLbl.setText("✅ Restaurant updated successfully!");
                            statusMessageLbl.setStyle("-fx-text-fill: #00cc66;");

                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/FoodOrder/client/view/sellerRestaurant.fxml"));
                            Parent homePage = loader.load();
                            Stage currentStage = (Stage) saveBtn.getScene().getWindow();
                            currentStage.setScene(new Scene(homePage));
                        } else {
                            String message;
                            switch (response.getStatusCode()) {
                                case 400:
                                    message = "❌ Invalid input data";
                                    break;
                                case 401:
                                    message = "❌ Authentication error. Please log in again";
                                    break;
                                case 403:
                                    message = "❌ Unauthorized access";
                                    break;
                                case 404:
                                    message = "❌ Restaurant not found";
                                    break;
                                case 409:
                                    message = "❌ Restaurant with this phone number already exists";
                                    break;
                                default:
                                    message = "❌ Error updating restaurant (code: " + response.getStatusCode() + ")";
                            }
                            statusMessageLbl.setText(message);
                            statusMessageLbl.setStyle("-fx-text-fill: #ff4444;");
                        }
                    } catch (IOException e) {
                        statusMessageLbl.setText("❌ Error navigating to main page");
                        statusMessageLbl.setStyle("-fx-text-fill: #ff4444;");
                    } finally {
                        saveBtn.setDisable(false);
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    statusMessageLbl.setText("❌ Network error. Please check your connection");
                    statusMessageLbl.setStyle("-fx-text-fill: #ff4444;");
                    saveBtn.setDisable(false);
                });
            }
        }).start();
    }

    @FXML
    private void onSelectImageClick(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Logo Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(selectImagePane.getScene().getWindow());
        if (selectedFile != null) {
            try {
                Image image = new Image(selectedFile.toURI().toString());
                profileImageView.setImage(image);
                setupCircularClipping();
            } catch (Exception e) {
                statusMessageLbl.setText("❌ Error processing image");
                statusMessageLbl.setStyle("-fx-text-fill: #ff4444;");
            }
        }
    }


    @FXML
    void onCancelBtnAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/FoodOrder/client/view/sellerRestaurant.fxml"));
            Parent homePage = loader.load();
            Stage currentStage = (Stage) cancelBtn.getScene().getWindow();
            currentStage.setScene(new Scene(homePage));
        } catch (IOException e) {
            statusMessageLbl.setText("❌ Error returning to main page");
            statusMessageLbl.setStyle("-fx-text-fill: #ff4444;");
        }
    }

    private void setupCircularClipping() {
        Circle clip = new Circle(40, 40, 40);
        profileImageView.setClip(clip);
        profileImageView.setFitWidth(80);
        profileImageView.setFitHeight(80);
        profileImageView.setPreserveRatio(false);
        profileImageView.setSmooth(true);
    }
}