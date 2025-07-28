package org.FoodOrder.client.controllers;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.FoodOrder.client.http.HttpController;
import org.FoodOrder.client.http.HttpMethod;
import org.FoodOrder.client.http.HttpHeaders;
import org.FoodOrder.client.http.HttpResponse;
import org.FoodOrder.client.sessions.UserSession;
import org.FoodOrder.client.util.JWTController;
import org.FoodOrder.client.util.ValidityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

public class SignInController {
    private static final String NORMAL_STYLE = "-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 14;";
    private static final String ERROR_STYLE = "-fx-background-color: #fff5f5; -fx-border-color: #ff4444; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-effect: dropshadow(gaussian, rgba(255,68,68,0.3), 8, 0, 0, 0);";
    private static final String SUCCESS_STYLE = "-fx-background-color: #f0fff4; -fx-border-color: #4CAF50; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-effect: dropshadow(gaussian, rgba(76,175,80,0.3), 8, 0, 0, 0);";

    @FXML
    private TextField phoneInput;
    @FXML
    private Label phoneLabel;
    @FXML
    private Label phoneWarning;
    @FXML
    private PasswordField passwordInput;
    @FXML
    private Label passwordLabel;
    @FXML
    private Label passwordWarning;
    @FXML
    private Button loginButton;
    @FXML
    private Button signUpLink;
    @FXML
    private Label signUpLabel;
    @FXML
    private Label statusMessageLbl;

    private void setFieldError(TextField field, Label warning, String message) {
        field.setStyle(ERROR_STYLE);
        warning.setText("⚠ " + message);
        warning.setStyle("-fx-text-fill: #ff4444; -fx-background-color: #fff5f5; -fx-padding: 5 10; -fx-background-radius: 5; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(255,68,68,0.2), 4, 0, 0, 1);");
        warning.setVisible(true);
    }

    private void setPasswordFieldError(PasswordField field, Label warning, String message) {
        field.setStyle(ERROR_STYLE);
        warning.setText("⚠ " + message);
        warning.setStyle("-fx-text-fill: #ff4444; -fx-background-color: #fff5f5; -fx-padding: 5 10; -fx-background-radius: 5; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(255,68,68,0.2), 4, 0, 0, 1);");
        warning.setVisible(true);
    }

    private void setFieldSuccess(TextField field, Label warning) {
        field.setStyle(SUCCESS_STYLE);
        warning.setVisible(false);
    }

    private void setPasswordFieldSuccess(PasswordField field, Label warning) {
        field.setStyle(SUCCESS_STYLE);
        warning.setVisible(false);
    }

    private void setFieldNormal(TextField field, Label warning) {
        field.setStyle(NORMAL_STYLE);
        warning.setVisible(false);
    }

    private void setPasswordFieldNormal(PasswordField field, Label warning) {
        field.setStyle(NORMAL_STYLE);
        warning.setVisible(false);
    }

    @FXML
    void onSignUpLinkAction(ActionEvent event) {
        try {
            Parent loginPage = FXMLLoader.load(getClass().getResource("/org/FoodOrder/client/view/signUp.fxml"));
            Scene loginScene = new Scene(loginPage);
            Stage currentStage = (Stage) signUpLink.getScene().getWindow();
            currentStage.setScene(loginScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onLoginBtnAction(ActionEvent event) {
        clearWarnings();

        String phone = phoneInput.getText().trim();
        String password = passwordInput.getText();

        boolean hasError = false;

        if (!"admin".equals(phone)) {
            if (phone.isEmpty()) {
                setFieldError(phoneInput, phoneWarning, "Phone number cannot be empty");
                hasError = true;
            } else if (!ValidityUtils.isValidIranianPhone(phone)) {
                setFieldError(phoneInput, phoneWarning, "Invalid phone number format");
                hasError = true;
            } else {
                setFieldSuccess(phoneInput, phoneWarning);
            }

            if (password.isEmpty()) {
                setPasswordFieldError(passwordInput, passwordWarning, "Password cannot be empty");
                hasError = true;
            } else if (password.length() < 6) {
                setPasswordFieldError(passwordInput, passwordWarning, "Password must be at least 6 characters");
                hasError = true;
            } else {
                setPasswordFieldSuccess(passwordInput, passwordWarning);
            }

            if (hasError) {
                return;
            }
        } else {
            if (password.isEmpty()) {
                setPasswordFieldError(passwordInput, passwordWarning, "Password cannot be empty");
                return;
            }
            setFieldSuccess(phoneInput, phoneWarning);
            setPasswordFieldSuccess(passwordInput, passwordWarning);
        }

        try {
            String url = "http://localhost:8082/auth/login";

            ObjectMapper objectMapper = new ObjectMapper();
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "phone", phone,
                    "password", password
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpResponse response = HttpController.sendRequest(
                    url,
                    HttpMethod.POST,
                    requestBody,
                    headers
            );

            if (response.getStatusCode() == 200) {
                System.out.println("Login successful: " + response.getBody());
                showStatusSuccess("✅ Login successful!");
                Map<String, Object> jsonMap = objectMapper.readValue(response.getBody(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>(){});
                String token = (String) jsonMap.get("token");

                if (token != null && !token.isEmpty()) {
                    UserSession.setToken(token);
                }
                try {
                    String role = JWTController.extractRoleFromToken(token);
                    UserSession.setRole(role);
                    Parent homePage = FXMLLoader.load(getClass().getResource("/org/FoodOrder/client/view/home.fxml"));
                    Scene homeScene = new Scene(homePage);
                    Stage currentStage = (Stage) loginButton.getScene().getWindow();
                    currentStage.setScene(homeScene);
                } catch (IOException e) {
                    e.printStackTrace();
                    showStatusError("❌ Error navigating to home page");
                }
            } else if (response.getStatusCode() == 401) {
                showStatusError("❌ Invalid phone number or password");
            } else if (response.getStatusCode() == 404) {
                showStatusError("❌ User not found");
            } else {
                showStatusError("❌ Login failed. Please try again later");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showStatusError("❌ Network error. Please check your connection");
        } catch (Exception e) {
            e.printStackTrace();
            showStatusError("❌ An unexpected error occurred");
        }
    }

    private void clearWarnings() {
        if (phoneWarning != null) {
            phoneWarning.setVisible(false);
            phoneWarning.setText("");
        }

        if (passwordWarning != null) {
            passwordWarning.setVisible(false);
            passwordWarning.setText("");
        }

        if (statusMessageLbl != null) {
            statusMessageLbl.setVisible(false);
            statusMessageLbl.setText("");
        }

        setFieldNormal(phoneInput, phoneWarning);
        setPasswordFieldNormal(passwordInput, passwordWarning);
    }

    private void showStatusError(String message) {
        if (statusMessageLbl != null) {
            statusMessageLbl.setText(message);
            statusMessageLbl.setStyle("-fx-text-fill: #ff4444; -fx-background-color: #fff5f5; -fx-padding: 10; -fx-background-radius: 8; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(255,68,68,0.3), 8, 0, 0, 0);");
            statusMessageLbl.setVisible(true);
        }
    }

    private void showStatusSuccess(String message) {
        if (statusMessageLbl != null) {
            statusMessageLbl.setText(message);
            statusMessageLbl.setStyle("-fx-text-fill: #4CAF50; -fx-background-color: #f0fff4; -fx-padding: 10; -fx-background-radius: 8; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(76,175,80,0.3), 8, 0, 0, 0);");
            statusMessageLbl.setVisible(true);
        }
    }
}