package org.FoodOrder.client.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import org.FoodOrder.client.sessions.UserSession;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private Parent root;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (root == null) {
            System.err.println("Error: root is null. Check fx:id in main.fxml");
            return;
        }

        try {
            Stage currentStage = (Stage) root.getScene().getWindow();

            String fxmlPath;
            if (UserSession.isLoggedIn()) {
                fxmlPath = "/org/FoodOrder/client/view/home.fxml";
            } else {
                fxmlPath = "/org/FoodOrder/client/view/signIn.fxml";
            }

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent newPage = fxmlLoader.load();
            Scene newScene = new Scene(newPage);

            Stage newStage = new Stage();
            newStage.getIcons().add(new Image(getClass().getResourceAsStream("/org/FoodOrder/client/view/images/9cdf7d81-8104-4d40-9b31-0ce28318dcce.png")));
            newStage.setTitle("FoodieExpress");
            newStage.setWidth(1200);
            newStage.setHeight(900);
            newStage.setResizable(false);
            newStage.centerOnScreen();

            newStage.setScene(newScene);
            newStage.show();

            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load FXML: " + e.getMessage());
        }
    }
}