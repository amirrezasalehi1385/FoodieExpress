package org.FoodOrder.client.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.FoodOrder.client.http.HttpController;
import org.FoodOrder.client.http.HttpHeaders;
import org.FoodOrder.client.http.HttpMethod;
import org.FoodOrder.client.http.HttpResponse;
import org.FoodOrder.client.sessions.UserSession;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class CouponController implements Initializable {

    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField valueField;
    @FXML private TextField scopeField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button createCouponButton;
    @FXML private Button backBtn;
    @FXML private Label statusMessageLbl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        createCouponButton.setDisable(true);
        setupFormValidation();
    }

    private void setupFormValidation() {
        valueField.textProperty().addListener((obs, oldValue, newValue) -> checkFormValidity());
        scopeField.textProperty().addListener((obs, oldValue, newValue) -> checkFormValidity());
        typeComboBox.valueProperty().addListener((obs, oldValue, newValue) -> checkFormValidity());
        startDatePicker.valueProperty().addListener((obs, oldValue, newValue) -> checkFormValidity());
        endDatePicker.valueProperty().addListener((obs, oldValue, newValue) -> checkFormValidity());
    }

    private void checkFormValidity() {
        boolean isValid = typeComboBox.getValue() != null &&
                !valueField.getText().trim().isEmpty() &&
                !scopeField.getText().trim().isEmpty() &&
                startDatePicker.getValue() != null &&
                endDatePicker.getValue() != null &&
                endDatePicker.getValue().isAfter(startDatePicker.getValue());
        createCouponButton.setDisable(!isValid);
    }

    @FXML
    private void onCreateCouponAction(ActionEvent event) {
        String type = typeComboBox.getValue();
        String value = valueField.getText();
        String scope = scopeField.getText();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        Map<String, Object> couponData = new HashMap<>();
        couponData.put("type", type);
        couponData.put("value", Long.parseLong(value));
        couponData.put("scope", scope);
        couponData.put("startDate", startDate.toString());
        couponData.put("endDate", endDate.toString());

        try {
            String requestBody = objectMapper.writeValueAsString(couponData);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + UserSession.getToken());
            headers.set("Content-Type", "application/json");

            HttpResponse response = HttpController.sendRequest(
                    "http://localhost:8082/admin/discounts",
                    HttpMethod.POST,
                    requestBody,
                    headers
            );

            if (response.getStatusCode() == 201) {
                showStatusMessage("✅ Coupon created successfully!", "#27ae60", "#f0fff4");
                clearForm();
            } else {
                showStatusMessage("❌ Failed to create coupon: " + response.getBody(), "#e74c3c", "#fff5f5");
            }
        } catch (Exception e) {
            showStatusMessage("❌ Error: " + e.getMessage(), "#e74c3c", "#fff5f5");
            e.printStackTrace();
        }
    }

    @FXML
    private void onBackBtnAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/FoodOrder/client/view/home.fxml"));
            Parent dashboardPage = loader.load();
            Scene dashboardScene = new Scene(dashboardPage);
            Stage currentStage = (Stage) backBtn.getScene().getWindow();
            currentStage.setScene(dashboardScene);
        } catch (IOException e) {
            showStatusMessage("❌ Error navigating back", "#e74c3c", "#fff5f5");
            e.printStackTrace();
        }
    }

    private void clearForm() {
        typeComboBox.setValue(null);
        valueField.clear();
        scopeField.clear();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
    }

    private void showStatusMessage(String message, String textColor, String backgroundColor) {
        Platform.runLater(() -> {
            statusMessageLbl.setText(message);
            statusMessageLbl.setStyle(
                    "-fx-font-size: 14px; " +
                            "-fx-text-fill: " + textColor + "; " +
                            "-fx-background-color: " + backgroundColor + "; " +
                            "-fx-padding: 10; " +
                            "-fx-background-radius: 8; " +
                            "-fx-font-weight: bold;"
            );
            statusMessageLbl.setVisible(true);

            PauseTransition hideMessage = new PauseTransition(Duration.seconds(3));
            hideMessage.setOnFinished(e -> statusMessageLbl.setVisible(false));
            hideMessage.play();
        });
    }
}