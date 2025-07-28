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
import javafx.scene.control.*;
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
import org.FoodOrder.client.sessions.UserSession;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class EditProfileController implements Initializable {
    @FXML
    private TextField fullNameInput;
    @FXML
    private TextField emailInput;
    @FXML
    private TextField phoneInput;
    @FXML
    private TextArea addressInput;
    @FXML
    private TextField bankNameInput;
    @FXML
    private TextField accountNumberInput;
    @FXML
    private Label statusMessageLbl;
    @FXML
    private Button cancelBtn;
    @FXML
    private Button saveProfileBtn;
    @FXML
    private StackPane selectImagePane;
    @FXML
    private ImageView profileImageView;
    @FXML
    private BorderPane rootPane;

    @FXML
    void onSaveAction(ActionEvent event) {
        String fullName = fullNameInput.getText().trim();
        String email = emailInput.getText().trim();
        String address = addressInput.getText().trim();
        String bankName = bankNameInput.getText().trim();
        String accountNumber = accountNumberInput.getText().trim();
        Map<String, Object> data = new HashMap<>();
        if (!fullName.isEmpty()) {
            data.put("full_name", fullName);
        }
        if (!address.isEmpty()) {
            data.put("address", address);
        }
        if (!email.isEmpty()) {
            data.put("email", email);
        }

        if (!bankName.isEmpty()) {
            data.put("bank_name", bankName);
        }

        if (!accountNumber.isEmpty()) {
            data.put("account_number", accountNumber);
        }

        if (profileImageView.getImage() != null) {
            try {
                String base64Image = imageToBase64(profileImageView.getImage());
                if (base64Image != null) {
                    data.put("profileImageBase64", base64Image);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonBody = mapper.writeValueAsString(data);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Bearer " + UserSession.getToken());

            HttpResponse response = HttpController.sendRequest(
                    "http://localhost:8082/auth/profile",
                    HttpMethod.PUT,
                    jsonBody,
                    headers
            );

            try {
                Parent profilePage = FXMLLoader.load(getClass().getResource("/org/FoodOrder/client/view/profile.fxml"));
                Scene profileScene = new Scene(profilePage);
                Stage currentStage = (Stage) saveProfileBtn.getScene().getWindow();
                currentStage.setScene(profileScene);
            } catch (IOException ex) {
                System.err.println("Error loading edit profile page: " + ex.getMessage());
                ex.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
            statusMessageLbl.setText("❌ Network error");
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
            javafx.scene.Parent profilePage = javafx.fxml.FXMLLoader.load(getClass().getResource("/org/FoodOrder/client/view/profile.fxml"));
            javafx.scene.Scene profileScene = new javafx.scene.Scene(profilePage);
            javafx.stage.Stage currentStage = (javafx.stage.Stage) fullNameInput.getScene().getWindow();
            currentStage.setScene(profileScene);
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

            footerController.setActive("profile");
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
