package org.FoodOrder.client.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.FoodOrder.client.http.HttpController;
import org.FoodOrder.client.http.HttpHeaders;
import org.FoodOrder.client.http.HttpMethod;
import org.FoodOrder.client.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.FoodOrder.client.sessions.UserSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class WalletController {

    @FXML private Label cardNumberLabel;
    @FXML private Label balanceLabel;
    @FXML private Label bankNameLabel;
    @FXML private Label accountNumberLabel;
    @FXML private Button addFundsButton;
    @FXML private Button backBtn;
    @FXML private TextField amountField;
    @FXML private ComboBox<String> methodComboBox;
    @FXML private Button submitTopUpBtn;
    @FXML private Label statusMessageLbl;
    @FXML private VBox topUpFormSection;
    @FXML private TextField updateBalanceField;
    @FXML private Button updateBalanceButton;

    private static final String BASE_URL = "http://localhost:8082";
    private String authToken;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @FXML
    public void initialize() {
        authToken = UserSession.getToken();
        if (topUpFormSection == null || updateBalanceField == null || updateBalanceButton == null) {
            System.out.println("One or more FXML elements are null! Check FXML injection.");
        }
        loadWalletData();
        topUpFormSection.setVisible(false);
        System.out.println("WalletController initialized. topUpFormSection visible: " + topUpFormSection.isVisible());
    }

    private void loadWalletData() {
        new Thread(() -> {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + authToken);
                String url = BASE_URL + "/wallet/balance";
                HttpResponse response = HttpController.sendRequest(url, HttpMethod.GET, null, headers);

                if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                    Map<String, Object> data = objectMapper.readValue(response.getBody(), Map.class);
                    currentBalance = new BigDecimal(data.get("balance").toString());
                    Platform.runLater(() -> balanceLabel.setText("Balance: $" + currentBalance.setScale(2)));
                } else {
                    Platform.runLater(() -> balanceLabel.setText("Balance: Error loading (" + response.getStatusCode() + ")"));
                }
            } catch (Exception e) {
                Platform.runLater(() -> balanceLabel.setText("Balance: Network Error"));
                e.printStackTrace();
            }
        }).start();
    }

    public void updateBalance(double amount) {
        currentBalance = currentBalance.add(BigDecimal.valueOf(amount));
        Platform.runLater(() -> balanceLabel.setText("Balance: $" + currentBalance.setScale(2)));
        System.out.println("Wallet balance updated to: $" + currentBalance);
    }

    @FXML
    private void onAddFundsAction() {
        System.out.println("onAddFundsAction triggered. Current visibility: " + topUpFormSection.isVisible());
        Platform.runLater(() -> {
            topUpFormSection.setVisible(true);
            statusMessageLbl.setText("Enter amount and select method to proceed.");
            System.out.println("topUpFormSection set to visible: " + topUpFormSection.isVisible());
        });
    }

    @FXML
    private void onSubmitTopUpAction() {
        String amountStr = amountField.getText();
        String method = methodComboBox.getValue();
        if (amountStr != null && !amountStr.isEmpty() && method != null) {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount > 0) {
                    if ("online".equalsIgnoreCase(method)) {
                        openPaymentPage(amount);
                    } else if ("card".equalsIgnoreCase(method)) {
                        topUpWallet(amount, method);
                    }
                } else {
                    showStatusMessage("Amount must be positive!");
                }
            } catch (NumberFormatException e) {
                showStatusMessage("Invalid amount format!");
            }
        } else {
            showStatusMessage("Please enter amount and select method!");
        }
    }

    private void openPaymentPage(double amount) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/FoodOrder/client/view/payment.fxml"));
            Parent paymentPage = loader.load();

            PaymentController paymentController = loader.getController();
            paymentController.setAmount(amount);

            Scene paymentScene = new Scene(paymentPage);
            Stage stage = (Stage) addFundsButton.getScene().getWindow();
            stage.setScene(paymentScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void topUpWallet(double amount, String method) {
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
                        showStatusMessage("Top-up successful!");
                        loadWalletData();
                        amountField.clear();
                        topUpFormSection.setVisible(false);
                    });
                } else {
                    Platform.runLater(() -> showStatusMessage("Top-up failed: " + response.getBody()));
                }
            } catch (Exception e) {
                Platform.runLater(() -> showStatusMessage("Top-up error: " + e.getMessage()));
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void onUpdateBalanceAction() {
        String newBalanceStr = updateBalanceField.getText();
        if (newBalanceStr != null && !newBalanceStr.isEmpty()) {
            try {
                double newBalance = Double.parseDouble(newBalanceStr);
                if (newBalance >= 0) {
                    updateWalletBalance(newBalance);
                } else {
                    showStatusMessage("Balance must be non-negative!");
                }
            } catch (NumberFormatException e) {
                showStatusMessage("Invalid balance format!");
            }
        } else {
            showStatusMessage("Please enter a new balance!");
        }
    }

    private void updateWalletBalance(double newBalance) {
        new Thread(() -> {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + authToken);
                String url = BASE_URL + "/wallet/update-balance";
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("balance", newBalance);
                String body = objectMapper.writeValueAsString(bodyMap);

                HttpResponse response = HttpController.sendRequest(url, HttpMethod.PUT, body, headers);

                if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                    Map<String, Object> data = objectMapper.readValue(response.getBody(), Map.class);
                    String message = (String) data.get("message");
                    Platform.runLater(() -> {
                        showStatusMessage(message != null ? message : "Balance updated successfully!");
                        loadWalletData();
                        updateBalanceField.clear();
                    });
                } else {
                    Platform.runLater(() -> showStatusMessage("Update failed: " + response.getBody()));
                }
            } catch (Exception e) {
                Platform.runLater(() -> showStatusMessage("Update error: " + e.getMessage()));
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void onBackBtnAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/FoodOrder/client/view/home.fxml"));
            Parent homePage = loader.load();
            Scene homeScene = new Scene(homePage);
            Stage stage = (Stage) backBtn.getScene().getWindow();
            stage.setScene(homeScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showStatusMessage(String message) {
        Platform.runLater(() -> statusMessageLbl.setText(message));
    }
}