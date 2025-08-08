package org.FoodOrder.client.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import org.FoodOrder.client.sessions.UserSession;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class HomeController implements Initializable {
    @FXML
    private BorderPane rootPane;
    @FXML
    private VBox seeRestaurantVBox;
    @FXML
    private VBox myRestaurantVBox;
    @FXML
    private Button myRestaurantButton;
    @FXML
    private VBox myOrderVBox;
    @FXML
    private VBox myFavouritesVBox;
    @FXML
    private Button myFavouritesButton;
    @FXML
    private Button orderBtn;
    @FXML
    private VBox restaurantOrdersVBox;
    @FXML
    private Button restaurantOrderBtn;
    @FXML
    private Button seeRestaurantBtn;
    @FXML
    private Button availableDeliveriesBtn;
    @FXML
    private Button deliveryHistoryBtn;
    @FXML
    private VBox availableDeliveriesVBox;
    @FXML
    private VBox deliveryHistoryVBox;
    @FXML
    private VBox adminUserVBox;
    @FXML
    private Button adminUserButton;
    @FXML
    private Button adminOrdersBtn;
    @FXML
    private VBox adminOrdersVBox;
    @FXML
    private Button walletBalanceBtn;
    @FXML
    private VBox walletBalanceVBox;
    @FXML
    private Label welcomeLabel;
    @FXML
    private Label welcomeSubLabel;
    @FXML
    private Button createCouponBtn;
    @FXML
    private VBox createCouponVBox;
    @FXML
    private Button adminRestaurantsBtn;
    @FXML
    private VBox adminRestaurantsVBox;

    @FXML
    public void onAvailableDeliveriesBtnAction(ActionEvent event) {
        try {
            Parent profilePage = FXMLLoader.load(getClass().getResource("/org/FoodOrder/client/view/availableDeliveries.fxml"));
            Scene profileScene = new Scene(profilePage);
            Stage currentStage = (Stage) availableDeliveriesBtn.getScene().getWindow();
            currentStage.setScene(profileScene);
        } catch (IOException ex) {
            System.err.println("Error loading create restaurant page: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    @FXML
    public void onCreateCouponBtnAction(ActionEvent event) {
        try {
            Parent profilePage = FXMLLoader.load(getClass().getResource("/org/FoodOrder/client/view/createCoupon.fxml"));
            Scene profileScene = new Scene(profilePage);
            Stage currentStage = (Stage) createCouponBtn.getScene().getWindow();
            currentStage.setScene(profileScene);
        } catch (IOException ex) {
            System.err.println("Error loading create restaurant page: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    @FXML
    public void onAdminOrdersBtnAction(ActionEvent event) {
        try {
            Parent profilePage = FXMLLoader.load(getClass().getResource("/org/FoodOrder/client/view/adminOrders.fxml"));
            Scene profileScene = new Scene(profilePage);
            Stage currentStage = (Stage) adminOrdersBtn.getScene().getWindow();
            currentStage.setScene(profileScene);
        } catch (IOException ex) {
            System.err.println("Error loading create restaurant page: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    @FXML
    public void onAdminUserBtnAction(ActionEvent event) {
        try {
            Parent profilePage = FXMLLoader.load(getClass().getResource("/org/FoodOrder/client/view/viewUsers.fxml"));
            Scene profileScene = new Scene(profilePage);
            Stage currentStage = (Stage) adminUserButton.getScene().getWindow();
            currentStage.setScene(profileScene);
        } catch (IOException ex) {
            System.err.println("Error loading create restaurant page: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    @FXML
    public void onAdminRestaurantsBtnAction(ActionEvent event) {
        try {
            Parent profilePage = FXMLLoader.load(getClass().getResource("/org/FoodOrder/client/view/adminRestaurants.fxml"));
            Scene profileScene = new Scene(profilePage);
            Stage currentStage = (Stage) adminRestaurantsBtn.getScene().getWindow();
            currentStage.setScene(profileScene);
        } catch (IOException ex) {
            System.err.println("Error loading create restaurant page: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    @FXML
    public void onRestaurantOrdersBtnAction(ActionEvent event) {
        try {
            Parent profilePage = FXMLLoader.load(getClass().getResource("/org/FoodOrder/client/view/restaurantOrders.fxml"));
            Scene profileScene = new Scene(profilePage);
            Stage currentStage = (Stage) restaurantOrderBtn.getScene().getWindow();
            currentStage.setScene(profileScene);
        } catch (IOException ex) {
            System.err.println("Error loading create restaurant page: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    @FXML
    public void onDeliveryHistoryBtnAction(ActionEvent event) {
        try {
            Parent profilePage = FXMLLoader.load(getClass().getResource("/org/FoodOrder/client/view/deliveryHistory.fxml"));
            Scene profileScene = new Scene(profilePage);
            Stage currentStage = (Stage) deliveryHistoryBtn.getScene().getWindow();
            currentStage.setScene(profileScene);
        } catch (IOException ex) {
            System.err.println("Error loading create restaurant page: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    @FXML
    public void onSeeRestaurantBtnAction(ActionEvent event) {
        try {
            Parent profilePage = FXMLLoader.load(getClass().getResource("/org/FoodOrder/client/view/buyerRestaurants.fxml"));
            Scene profileScene = new Scene(profilePage);
            Stage currentStage = (Stage) seeRestaurantBtn.getScene().getWindow();
            currentStage.setScene(profileScene);
        } catch (IOException ex) {
            System.err.println("Error loading create restaurant page: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    @FXML
    public void onWalletBalanceBtnAction(ActionEvent event) {
        try {
            Parent profilePage = FXMLLoader.load(getClass().getResource("/org/FoodOrder/client/view/wallet.fxml"));
            Scene profileScene = new Scene(profilePage);
            Stage currentStage = (Stage) walletBalanceBtn.getScene().getWindow();
            currentStage.setScene(profileScene);
        } catch (IOException ex) {
            System.err.println("Error loading create restaurant page: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    @FXML
    public void onMyRestaurantBtnAction(ActionEvent event) {
        try {
            Parent profilePage = FXMLLoader.load(getClass().getResource("/org/FoodOrder/client/view/sellerRestaurant.fxml"));
            Scene profileScene = new Scene(profilePage);
            Stage currentStage = (Stage) myRestaurantButton.getScene().getWindow();
            currentStage.setScene(profileScene);
        } catch (IOException ex) {
            System.err.println("Error loading create restaurant page: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    @FXML
    public void onMyFavouritesBtnAction(ActionEvent event) {
        try {
            Parent profilePage = FXMLLoader.load(getClass().getResource("/org/FoodOrder/client/view/favoriteRestaurants.fxml"));
            Scene profileScene = new Scene(profilePage);
            Stage currentStage = (Stage) myFavouritesButton.getScene().getWindow();
            currentStage.setScene(profileScene);
        } catch (IOException ex) {
            System.err.println("Error loading create restaurant page: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    public void onOrderBtnAction(ActionEvent event) {
        try {
            Parent profilePage = FXMLLoader.load(getClass().getResource("/org/FoodOrder/client/view/orderHistory.fxml"));
            Scene profileScene = new Scene(profilePage);
            Stage currentStage = (Stage) orderBtn.getScene().getWindow();
            currentStage.setScene(profileScene);
        } catch (IOException ex) {
            System.err.println("Error loading create restaurant page: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String role = UserSession.getRole();
        if (role != null) {
            switch (role.toUpperCase()) {
                case "BUYER":
                    seeRestaurantVBox.setVisible(true);
                    seeRestaurantVBox.setManaged(true);
                    myOrderVBox.setVisible(true);
                    myOrderVBox.setManaged(true);
                    myFavouritesVBox.setVisible(true);
                    myFavouritesVBox.setManaged(true);
                    walletBalanceVBox.setVisible(true);
                    walletBalanceVBox.setManaged(true);
                    welcomeLabel.setText("Welcome Back!");
                    welcomeSubLabel.setText("Discover delicious meals from your favorite restaurants");
                    break;

                case "SELLER":
                    myRestaurantVBox.setVisible(true);
                    myRestaurantVBox.setManaged(true);
                    restaurantOrdersVBox.setVisible(true);
                    restaurantOrdersVBox.setManaged(true);
                    welcomeLabel.setText("Welcome Back!");
                    welcomeSubLabel.setText("Manage your menu, orders, and reach more customers");
                    break;

                case "COURIER":
                    availableDeliveriesVBox.setVisible(true);
                    availableDeliveriesVBox.setManaged(true);
                    deliveryHistoryVBox.setVisible(true);
                    deliveryHistoryVBox.setManaged(true);
                    welcomeLabel.setText("Ready to Deliver?");
                    welcomeSubLabel.setText("Find nearby orders and start earning now");
                    break;

                case "ADMIN":
                    adminUserVBox.setVisible(true);
                    adminUserVBox.setManaged(true);
                    adminOrdersVBox.setVisible(true);
                    adminOrdersVBox.setManaged(true);
                    welcomeLabel.setText("Welcome Admin");
                    welcomeSubLabel.setText("Monitor platform activity and manage the ecosystem");
                    createCouponVBox.setVisible(true);
                    createCouponVBox.setManaged(true);
                    adminRestaurantsVBox.setVisible(true);
                    adminRestaurantsVBox.setManaged(true);
                    break;

                default:
                    welcomeLabel.setText("Welcome Back!");
                    welcomeSubLabel.setText("Enjoy seamless food ordering experience");
                    break;
            }
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/FoodOrder/client/view/footer.fxml"));
            Parent footer = loader.load();
            FooterController footerController = loader.getController();
            footerController.setActive("home");
            rootPane.setBottom(footer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
