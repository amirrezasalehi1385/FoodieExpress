package org.FoodOrder.client.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import org.FoodOrder.client.http.HttpController;
import org.FoodOrder.client.http.HttpHeaders;
import org.FoodOrder.client.http.HttpMethod;
import org.FoodOrder.client.http.HttpResponse;
import org.FoodOrder.client.models.Restaurant;
import org.FoodOrder.client.sessions.UserSession;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SellerRestaurantController implements Initializable {
    @FXML private Button createButton;
    @FXML private Button backBtn;
    @FXML private BorderPane rootPane;
    @FXML private VBox restaurantListBox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/FoodOrder/client/view/footer.fxml"));
            Parent footer = loader.load();
            FooterController footerController = loader.getController();
            footerController.setActive("myRestaurant");
            rootPane.setBottom(footer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadRestaurants();
    }
    private void loadRestaurants() {
        new Thread(() -> {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + UserSession.getToken());
                headers.set("Content-Type", "application/json");
                HttpResponse response = HttpController.sendRequest(
                        "http://localhost:8082/restaurants/mine",
                        HttpMethod.GET,
                        null,
                        headers
                );
                if (response.getStatusCode() == 200) {
                    ObjectMapper mapper = new ObjectMapper();
                    List<Restaurant> restaurants = mapper.readValue(
                            response.getBody(),
                            new TypeReference<List<Restaurant>>() {}
                    );
                    javafx.application.Platform.runLater(() -> {
                        restaurantListBox.getChildren().clear();
                        createRestaurantGrid(restaurants);
                    });

                } else {
                    System.err.println("Failed to load restaurants. Status: " + response.getStatusCode());
                    System.err.println("Body: " + response.getBody());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void createRestaurantGrid(List<Restaurant> restaurants) {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(25);
        gridPane.setVgap(25);
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setPadding(new Insets(20));

        int col = 0;
        int row = 0;

        for (Restaurant restaurant : restaurants) {
            VBox restaurantCard = createRestaurantBox(restaurant);
            gridPane.add(restaurantCard, col, row);

            col++;
            if (col >= 2) {
                col = 0;
                row++;
            }
        }

        restaurantListBox.getChildren().add(gridPane);
    }

    private VBox createRestaurantBox(Restaurant restaurant) {
        VBox outerBox = new VBox(15);
        outerBox.setPrefWidth(380);
        outerBox.setMaxWidth(380);
        outerBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 15;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0.3, 0.0, 5.0);" +
                        "-fx-border-radius: 15;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;"
        );
        outerBox.setPadding(new Insets(20));
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        VBox logoContainer = new VBox();
        logoContainer.setAlignment(Pos.CENTER);

        ImageView logoView = new ImageView();
        logoView.setFitHeight(80);
        logoView.setFitWidth(80);
        logoView.setPreserveRatio(true);

        Circle clip = new Circle(40, 40, 40);
        logoView.setClip(clip);
        logoView.setStyle(
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0.5, 0.0, 2.0);"
        );

        if (restaurant.getLogoBase64() != null && !restaurant.getLogoBase64().isEmpty()) {
            try {
                byte[] decodedBytes = Base64.getDecoder().decode(restaurant.getLogoBase64());
                ByteArrayInputStream bis = new ByteArrayInputStream(decodedBytes);
                Image image = new Image(bis);
                logoView.setImage(image);
            } catch (Exception e) {
                setDefaultLogo(logoView);
            }
        } else {
            setDefaultLogo(logoView);
        }

        logoContainer.getChildren().add(logoView);
        VBox infoBox = new VBox(8);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        Label name = new Label("🏪 " + restaurant.getName());
        name.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #2c3e50;"
        );

        Label address = new Label("📍 " + restaurant.getAddress());
        address.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-text-fill: #7f8c8d;" +
                        "-fx-wrap-text: true;"
        );

        HBox ratingBox = new HBox(8);
        ratingBox.setAlignment(Pos.CENTER_LEFT);
        double ratingValue = restaurant.getAverageRating() != null ? restaurant.getAverageRating() : 0.0;
        StringBuilder stars = new StringBuilder();
        int fullStars = (int) ratingValue;
        for (int i = 0; i < fullStars && i < 5; i++) {
            stars.append("★");
        }
        for (int i = fullStars; i < 5; i++) {
            stars.append("☆");
        }

        Label starsLabel = new Label(stars.toString());
        starsLabel.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-text-fill: #f39c12;"
        );

        Label ratingText = new Label(String.format("%.1f", ratingValue));
        ratingText.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #f39c12;"
        );

        ratingBox.getChildren().addAll(starsLabel, ratingText);

        infoBox.getChildren().addAll(name, address, ratingBox);
        headerBox.getChildren().addAll(logoContainer, infoBox);
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));

        Button editBtn = new Button("✏ Edit");
        editBtn.setStyle(
                "-fx-background-color: #3498db;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 10 20;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(52, 152, 219, 0.3), 8, 0.5, 0.0, 2.0);"
        );

        Button infoBtn = new Button("ℹ Info");
        infoBtn.setStyle(
                "-fx-background-color: #e91e63;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 10 20;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(233, 30, 99, 0.3), 8, 0.5, 0.0, 2.0);"
        );
        editBtn.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/FoodOrder/client/view/editRestaurant.fxml"));
                Parent page = loader.load();
                EditRestaurantController controller = loader.getController();
                controller.setRestaurant(restaurant);
                Stage currentStage = (Stage) editBtn.getScene().getWindow();
                currentStage.setScene(new Scene(page));
                loadRestaurants();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        infoBtn.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/FoodOrder/client/view/restaurantInfo.fxml"));
                Parent page = loader.load();
                RestaurantInfoController controller = loader.getController();
                controller.setRestaurant(restaurant);
                Stage currentStage = (Stage) infoBtn.getScene().getWindow();
                currentStage.setScene(new Scene(page));
                loadRestaurants();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        editBtn.setOnMouseEntered(e -> editBtn.setStyle(editBtn.getStyle() + "-fx-background-color: #2980b9;"));
        editBtn.setOnMouseExited(e -> editBtn.setStyle(editBtn.getStyle().replace("-fx-background-color: #2980b9;", "-fx-background-color: #3498db;")));
        infoBtn.setOnMouseEntered(e -> infoBtn.setStyle(infoBtn.getStyle() + "-fx-background-color: #c2185b;"));
        infoBtn.setOnMouseExited(e -> infoBtn.setStyle(infoBtn.getStyle().replace("-fx-background-color: #c2185b;", "-fx-background-color: #e91e63;")));
        buttonBox.getChildren().addAll(editBtn, infoBtn);
        outerBox.getChildren().addAll(headerBox, buttonBox);
        outerBox.setOnMouseEntered(e -> {
            outerBox.setStyle(outerBox.getStyle().replace(
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0.3, 0.0, 5.0);",
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 20, 0.4, 0.0, 8.0);"
            ));
        });

        outerBox.setOnMouseExited(e -> {
            outerBox.setStyle(outerBox.getStyle().replace(
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 20, 0.4, 0.0, 8.0);",
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0.3, 0.0, 5.0);"
            ));
        });

        return outerBox;
    }

    private void setDefaultLogo(ImageView logoView) {
        try {
            logoView.setStyle(
                    "-fx-background-color: #ecf0f1;" +
                            "-fx-background-radius: 40;"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onCreateBtnAction(ActionEvent event) {
        try {
            Parent page = FXMLLoader.load(getClass().getResource("/org/FoodOrder/client/view/createRestaurant.fxml"));
            Stage stage = (Stage) createButton.getScene().getWindow();
            stage.setScene(new Scene(page));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    void onBackBtnAction(ActionEvent event) {
        try {
            Parent page = FXMLLoader.load(getClass().getResource("/org/FoodOrder/client/view/home.fxml"));
            Stage stage = (Stage) backBtn.getScene().getWindow();
            stage.setScene(new Scene(page));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}