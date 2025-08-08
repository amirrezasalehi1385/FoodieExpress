package org.FoodOrder.client.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.FoodOrder.client.http.HttpController;
import org.FoodOrder.client.http.HttpHeaders;
import org.FoodOrder.client.http.HttpMethod;
import org.FoodOrder.client.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.FoodOrder.client.sessions.UserSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PaymentController {

    @FXML private TextField cardNumberField;
    @FXML private TextField expiryDateField;
    @FXML private TextField cvvField;
    @FXML private Button payButton;
    @FXML private Button backButton;
    private double amount = 0.0;

    public void setAmount(double amount) {
        this.amount = amount;
    }

    private static final String BASE_URL = "http://localhost:8082";
    private String authToken;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    public void initialize() {
        authToken = UserSession.getToken();
        if (authToken == null || authToken.isEmpty()) {
            System.out.println("Auth token is invalid!");
        }
    }

    @FXML
    private void onPayAction() {
        String cardNumber = cardNumberField.getText();
        String expiryDate = expiryDateField.getText();
        String cvv = cvvField.getText();

        if (isValidInput(cardNumber, expiryDate, cvv)) {
            processPayment(amount, "online"); // فراخوانی متد جدید برای ارتباط با سرور
        } else {
            showErrorMessage("Invalid payment details!");
        }
    }

    private boolean isValidInput(String cardNumber, String expiryDate, String cvv) {
        return cardNumber != null && cardNumber.length() >= 16 &&
                expiryDate != null && expiryDate.matches("\\d{2}/\\d{2}") &&
                cvv != null && cvv.length() == 3;
    }

    private void processPayment(double amount, String method) {
        new Thread(() -> {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + authToken);
                String url = BASE_URL + "/wallet/top-up";
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("method", method.toLowerCase());
                bodyMap.put("amount", amount);
                String body = objectMapper.writeValueAsString(bodyMap);

                HttpResponse response = HttpController.sendRequest(url, HttpMethod.POST, body, headers);

                if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                    Platform.runLater(() -> {
                        showSuccessMessage();
                        goBackToWallet();
                    });
                } else {
                    Platform.runLater(() -> showErrorMessage("Payment failed: " + response.getBody()));
                }
            } catch (Exception e) {
                Platform.runLater(() -> showErrorMessage("Payment error: " + e.getMessage()));
                e.printStackTrace();
            }
        }).start();
    }

    private void showSuccessMessage() {
        Platform.runLater(() -> System.out.println("Payment successful!"));
    }

    private void showErrorMessage(String message) {
        Platform.runLater(() -> System.out.println(message));
    }

    @FXML
    private void onBackAction() {
        goBackToWallet();
    }

    private void goBackToWallet() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/FoodOrder/client/view/wallet.fxml"));
            Parent walletPage = loader.load();
            Scene walletScene = new Scene(walletPage);
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(walletScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}