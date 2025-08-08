package org.FoodOrder.client.controllers;

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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.FoodOrder.client.http.HttpController;
import org.FoodOrder.client.http.HttpHeaders;
import org.FoodOrder.client.http.HttpMethod;
import org.FoodOrder.client.http.HttpResponse;
import org.FoodOrder.client.sessions.UserSession;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {
    @FXML
    private Button cancleBtn;
    @FXML
    private Button editBtn;
    @FXML
    private Label fullNameLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label phoneLabel;
    @FXML
    private Label addressLabel;
    @FXML
    private Label bankNameLabel;
    @FXML
    private Label accountNumberLabel;
    @FXML
    private ImageView profileImageView;
    @FXML
    private Button logoutButton;
    @FXML
    private Label userNameDisplay;
    @FXML
    private BorderPane rootPane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupCircularClipping();
        fetchAndDisplayProfile();
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

    private void fetchAndDisplayProfile() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + UserSession.getToken());

            HttpResponse response = HttpController.sendRequest(
                    "http://localhost:8082/auth/profile",
                    HttpMethod.GET,
                    null,
                    headers
            );

            if (response.getStatusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());

                fullNameLabel.setText(getTextSafe(root, "full_name"));
                userNameDisplay.setText(getTextSafe(root, "full_name"));
                emailLabel.setText(getTextSafe(root, "email"));
                phoneLabel.setText(getTextSafe(root, "phoneNumber"));
                addressLabel.setText(getTextSafe(root, "address"));
                bankNameLabel.setText(getTextSafe(root, "bank_name"));
                accountNumberLabel.setText(getTextSafe(root, "account_number"));

                String base64Image = getTextSafe(root, "profileImageBase64");
                if (!base64Image.equals("-") && base64Image.length() > 0) {
                    byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                    Image image = new Image(new ByteArrayInputStream(imageBytes));
                    profileImageView.setImage(image);
                    setupCircularClipping();
                }

            } else if (response.getStatusCode() == 401) {
                System.out.println("Unauthorized: Please login again.");
            } else {
                System.out.println("Unexpected error: " + response.getStatusCode());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onCancleBtnAction(ActionEvent event) {
        System.out.println("Cancle button clicked!");
        Timeline delay = new Timeline(
                new KeyFrame(Duration.millis(150), e -> {
                    try {
                        Parent homePage = FXMLLoader.load(getClass().getResource("/org/FoodOrder/client/view/home.fxml"));
                        Scene homeScene = new Scene(homePage);
                        Stage currentStage = (Stage) cancleBtn.getScene().getWindow();
                        currentStage.setScene(homeScene);
                    } catch (IOException ex) {
                        System.err.println("Error loading home page: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                })
        );
        delay.play();
    }

    public void onEditAction(ActionEvent event) {
        System.out.println("Edit Profile button clicked!");
        Timeline delay = new Timeline(
                new KeyFrame(Duration.millis(150), e -> {
                    try {
                        Parent editPage = FXMLLoader.load(getClass().getResource("/org/FoodOrder/client/view/editProfile.fxml"));
                        Scene editScene = new Scene(editPage);
                        Stage currentStage = (Stage) editBtn.getScene().getWindow();
                        currentStage.setScene(editScene);
                    } catch (IOException ex) {
                        System.err.println("Error loading edit profile page: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                })
        );
        delay.play();
    }

    private String getTextSafe(JsonNode node, String fieldName) {
        JsonNode valueNode = node.get(fieldName);
        return (valueNode != null && !valueNode.isNull()) ? valueNode.asText() : "-";
    }
    @FXML
    private Button logoutBtn;

    public void onLogoutAction(ActionEvent event) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + UserSession.getToken());

            HttpResponse response = HttpController.sendRequest(
                    "http://localhost:8082/auth/logout",
                    HttpMethod.POST,
                    "",
                    headers
            );


            if (response.getStatusCode() == 200) {
                UserSession.clear();

                Parent loginPage = FXMLLoader.load(getClass().getResource("/org/FoodOrder/client/view/signIn.fxml"));
                Scene loginScene = new Scene(loginPage);
                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                currentStage.setScene(loginScene);

            } else if (response.getStatusCode() == 401) {
                System.out.println("Unauthorized: Please login again.");
            } else {
                System.out.println("Logout failed with status: " + response.getStatusCode());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}