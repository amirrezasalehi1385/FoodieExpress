package org.FoodOrder.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import org.FoodOrder.client.sessions.UserSession;

import java.io.IOException;

public class Client extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            String fxmlPath;
            if (UserSession.isLoggedIn()) {
                fxmlPath = "/org/FoodOrder/client/view/home.fxml";
            } else {
                fxmlPath = "/org/FoodOrder/client/view/signIn.fxml";
            }

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);

            stage.getIcons().add(new Image(getClass().getResourceAsStream("/org/FoodOrder/client/view/images/9cdf7d81-8104-4d40-9b31-0ce28318dcce.png")));
            stage.setTitle("FoodieExpress");
            stage.setWidth(1200);
            stage.setHeight(900);
            stage.setResizable(false);
            stage.centerOnScreen();

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}