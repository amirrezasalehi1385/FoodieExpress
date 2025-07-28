package org.FoodOrder.client.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.ResourceBundle;

public class CreateRestaurantController implements Initializable {
    @FXML
    private TextField nameInput;
    @FXML
    private TextField phoneInput;
    @FXML
    private TextArea addressInput;
    @FXML
    private Label statusMessageLbl;
    @FXML
    private Button cancelBtn;
    @FXML
    private Button saveRestaurantBtn;
    @FXML
    private StackPane selectImagePane;
    @FXML
    private ImageView profileImageView;
    @FXML
    private BorderPane rootPane;

    @FXML
    void onSaveAction(ActionEvent event) {
        statusMessageLbl.setText("");

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
            statusMessageLbl.setText("❌ Restaurant phone is required");
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
        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setAddress(address);
        restaurant.setPhone(phone);
        if (profileImageView.getImage() != null) {
            try {
                String base64Image = imageToBase64(profileImageView.getImage());
                if (base64Image != null && !base64Image.isEmpty()) {
                    restaurant.setLogoBase64(base64Image);
                }
            } catch (IOException e) {
                e.printStackTrace();
                statusMessageLbl.setText("❌ Error processing image");
                statusMessageLbl.setStyle("-fx-text-fill: #ff4444;");
                return;
            }
        }
        statusMessageLbl.setText("Creating restaurant...");
        statusMessageLbl.setStyle("-fx-text-fill: #0066cc;");
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonBody = mapper.writeValueAsString(restaurant);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Bearer " + UserSession.getToken());
            HttpResponse response = HttpController.sendRequest(
                    "http://localhost:8082/restaurants",
                    HttpMethod.POST,
                    jsonBody,
                    headers
            );
            System.out.println(response.getBody());

            if (response.getStatusCode() == 201) {
                ObjectMapper m = new ObjectMapper();
                Restaurant createdRestaurant = m.readValue(response.getBody(), Restaurant.class);
                statusMessageLbl.setText("✅ Restaurant created successfully! ID: " + createdRestaurant.getId());
                statusMessageLbl.setStyle("-fx-text-fill: #00cc66;");

                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/FoodOrder/client/view/sellerRestaurant.fxml"));
                    Parent profilePage = loader.load();
                    Stage currentStage = (Stage) saveRestaurantBtn.getScene().getWindow();
                    currentStage.setScene(new Scene(profilePage));
                } catch (IOException e) {
                    e.printStackTrace();
                    statusMessageLbl.setText("❌ Navigation error");
                    statusMessageLbl.setStyle("-fx-text-fill: #ff4444;");
                }
            } else if (response.getStatusCode() == 400) {
                statusMessageLbl.setText("❌ Invalid input data");
                statusMessageLbl.setStyle("-fx-text-fill: #ff4444;");
            } else if (response.getStatusCode() == 409) {
                statusMessageLbl.setText("❌ Restaurant with this phone number already exists");
                statusMessageLbl.setStyle("-fx-text-fill: #ff4444;");
            } else if (response.getStatusCode() == 401) {
                statusMessageLbl.setText("❌ Authentication failed. Please login again");
                statusMessageLbl.setStyle("-fx-text-fill: #ff4444;");
            } else {
                statusMessageLbl.setText("❌ Failed to create restaurant (Error: " + response.getStatusCode() + ")");
                statusMessageLbl.setStyle("-fx-text-fill: #ff4444;");
            }

        } catch (IOException e) {
            e.printStackTrace();
            statusMessageLbl.setText("❌ Network error. Please check your connection");
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
    @FXML
    void onCancelAction(ActionEvent event) {
        try {
            javafx.scene.Parent page = javafx.fxml.FXMLLoader.load(getClass().getResource("/org/FoodOrder/client/view/sellerRestaurant.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(page);
            javafx.stage.Stage currentStage = (javafx.stage.Stage) nameInput.getScene().getWindow();
            currentStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        profileImageView.setPreserveRatio(true);
        profileImageView.setSmooth(true);
        StackPane.setAlignment(profileImageView, Pos.CENTER);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/FoodOrder/client/view/footer.fxml"));
            Parent footer = loader.load();
            FooterController footerController = loader.getController();

            footerController.setActive("myRestaurant");
            rootPane.setBottom(footer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onSelectImageClick(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(selectImagePane.getScene().getWindow());
        if (selectedFile != null) {
            Image image = new Image(selectedFile.toURI().toString());
            profileImageView.setImage(image);
            setupCircularClipping();
        }
    }

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
