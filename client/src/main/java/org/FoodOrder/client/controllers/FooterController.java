package org.FoodOrder.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.FoodOrder.client.sessions.UserSession;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class FooterController implements Initializable {
    @FXML
    private Button profileBtn;
    @FXML
    private Button restaurantsBtn;
    @FXML
    private Button cartBtn;
    @FXML
    private Button homeBtn;
    @FXML
    private Button deliveryBtn;
    @FXML
    private Button manageMenuBtn;
    @FXML
    private Button manageUsersBtn;
    @FXML
    private Button myRestaurantBtn;
    @FXML
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        String role = UserSession.getRole();
        if (role != null) {
            switch (role.toUpperCase()) {
                case "BUYER":
                    homeBtn.setVisible(true);
                    homeBtn.setManaged(true);
                    restaurantsBtn.setVisible(true);
                    restaurantsBtn.setManaged(true);
                    cartBtn.setVisible(true);
                    cartBtn.setManaged(true);
                    break;
                case "SELLER":
                    myRestaurantBtn.setVisible(true);
                    myRestaurantBtn.setManaged(true);
                    break;
                case "COURIER":
                    deliveryBtn.setVisible(true);
                    deliveryBtn.setManaged(true);
                    break;
                case "ADMIN":
                    manageUsersBtn.setVisible(true);
                    manageUsersBtn.setManaged(true);
                    break;
            }
        }
    }

    @FXML
    private void onCartBtnAction(ActionEvent event) {
        try {
            Parent cartPage = FXMLLoader.load(getClass().getResource("/org/FoodOrder/client/view/cart.fxml"));
            Scene cartScene = new Scene(cartPage);
            Stage currentStage = (Stage) cartBtn.getScene().getWindow();
            currentStage.setScene(cartScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void onDeliveriesBtnAction(ActionEvent event) {
        try {
            Parent profilePage = FXMLLoader.load(getClass().getResource("/org/FoodOrder/client/view/availableDeliveries.fxml"));
            Scene profileScene = new Scene(profilePage);
            Stage currentStage = (Stage) deliveryBtn.getScene().getWindow();
            currentStage.setScene(profileScene);
        } catch (IOException ex) {
            System.err.println("Error loading create restaurant page: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    @FXML
    public void onProfileBtnAction(ActionEvent event) {
        try {
            Parent profilePage = FXMLLoader.load(getClass().getResource("/org/FoodOrder/client/view/profile.fxml"));
            Scene profileScene = new Scene(profilePage);
            Stage currentStage = (Stage) profileBtn.getScene().getWindow();
            currentStage.setScene(profileScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void onMyRestaurantBtnAction(ActionEvent event) {
        try {
            Parent profilePage = FXMLLoader.load(getClass().getResource("/org/FoodOrder/client/view/sellerRestaurant.fxml"));
            Scene profileScene = new Scene(profilePage);
            Stage currentStage = (Stage) profileBtn.getScene().getWindow();
            currentStage.setScene(profileScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void onSeeAllRestaurantsBtnAction(ActionEvent event) {
        try {
            Parent profilePage = FXMLLoader.load(getClass().getResource("/org/FoodOrder/client/view/buyerRestaurants.fxml"));
            Scene profileScene = new Scene(profilePage);
            Stage currentStage = (Stage) restaurantsBtn.getScene().getWindow();
            currentStage.setScene(profileScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void onHomeBtnAction(ActionEvent event) {
        try {
            Parent homePage = FXMLLoader.load(getClass().getResource("/org/FoodOrder/client/view/home.fxml"));
            Scene homeScene = new Scene(homePage);
            Stage currentStage = (Stage) homeBtn.getScene().getWindow();
            currentStage.setScene(homeScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void setActive(String activeButton) {
        String activeStyle = "-fx-background-color: rgba(52, 152, 219, 0.1);" +
                "-fx-text-fill: #3498db;" +
                "-fx-font-size: 17;" +
                "-fx-pref-width: 200;" +
                "-fx-pref-height: 70;" +
                "-fx-cursor: hand;" +
                "-fx-background-radius: 17;" +
                "-fx-border-radius: 17;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0.1, 0, 2);" +
                "-fx-font-weight: bold;";

        switch (activeButton) {
            case "home" -> homeBtn.setStyle(activeStyle);
            case "profile" -> profileBtn.setStyle(activeStyle);
            case "myRestaurant" -> myRestaurantBtn.setStyle(activeStyle);
            case "restaurant" -> restaurantsBtn.setStyle(activeStyle);
            case "cart" -> cartBtn.setStyle(activeStyle);
            case "deliveries" -> deliveryBtn.setStyle(activeStyle);
            case "manageUsers" -> manageUsersBtn.setStyle(activeStyle);
        }
        homeBtn.getStyleClass().remove("active");
        profileBtn.getStyleClass().remove("active");
        myRestaurantBtn.getStyleClass().remove("active");
        restaurantsBtn.getStyleClass().remove("active");
        cartBtn.getStyleClass().remove("active");
        manageUsersBtn.getStyleClass().remove("active");
    }
}
