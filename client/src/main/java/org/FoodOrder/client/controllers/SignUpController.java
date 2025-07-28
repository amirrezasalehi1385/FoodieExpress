package org.FoodOrder.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.FoodOrder.client.models.User;
import org.FoodOrder.client.util.ValidityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

public class SignUpController implements Initializable {
    private static User user = new User();
    private static boolean isValidEmail = false;
    private static boolean isValidPass = false;
    private static final String NORMAL_STYLE = "-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 6; -fx-font-size: 14;";
    private static final String ERROR_STYLE = "-fx-background-color: #fff5f5; -fx-border-color: #ff4444; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 6; -fx-font-size: 14; -fx-effect: dropshadow(gaussian, rgba(255,68,68,0.3), 8, 0, 0, 0);";
    private static final String SUCCESS_STYLE = "-fx-background-color: #f0fff4; -fx-border-color: #4CAF50; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 6; -fx-font-size: 14; -fx-effect: dropshadow(gaussian, rgba(76,175,80,0.3), 8, 0, 0, 0);";

    public SignUpController() {
    }

    @FXML
    private TextField phoneInput;
    @FXML
    private Label phoneLabel;
    @FXML
    private Label phoneWarning;
    @FXML
    private TextField passwordInput;
    @FXML
    private Label passwordLabel;
    @FXML
    private Label passwordWarning;
    @FXML
    private TextField fullNameInput;
    @FXML
    private TextField addressInput;
    @FXML
    private Label addressLabel;
    @FXML
    private Label addressWarning;
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private Label roleWarning;
    @FXML
    private TextField emailInput;
    @FXML
    private Label emailWarning;
    @FXML
    private Label emailLabel;
    @FXML
    private Label bankInfoLabel;
    @FXML
    private Label bankNameLabel;
    @FXML
    private TextField bankNameInput;
    @FXML
    private TextField accountNumberInput;
    @FXML
    private Label accountNumberWarning;
    @FXML
    private Label accountNoLabel;
    @FXML
    private Button signUpButton;
    @FXML
    private Button loginLink;
    @FXML
    private Label statusMessageLbl;
    private void setFieldError(TextField field, Label warning, String message) {
        field.setStyle(ERROR_STYLE);
        warning.setText("⚠ " + message);
        warning.setStyle("-fx-text-fill: #ff4444; -fx-background-color: #fff5f5; -fx-padding: 5 10; -fx-background-radius: 5; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(255,68,68,0.2), 4, 0, 0, 1);");
        warning.setVisible(true);
    }
    private void setFieldSuccess(TextField field, Label warning) {
        field.setStyle(SUCCESS_STYLE);
        warning.setVisible(false);
    }
    private void setFieldNormal(TextField field, Label warning) {
        field.setStyle(NORMAL_STYLE);
        warning.setVisible(false);
    }

    @FXML
    void onLoginLinkAction(ActionEvent event) {
        try {
            Parent loginPage = FXMLLoader.load(getClass().getResource("/org/FoodOrder/client/view/signIn.fxml"));
            Scene loginScene = new Scene(loginPage);
            Stage currentStage = (Stage) loginLink.getScene().getWindow();
            currentStage.setScene(loginScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onRegisterButtonAction(ActionEvent event) {
        try {
            boolean hasError = false;
            if (phoneInput.getText().isEmpty()) {
                setFieldError(phoneInput, phoneWarning, "Phone number cannot be empty");
                hasError = true;
            } else if (!ValidityUtils.isValidIranianPhone(phoneInput.getText())) {
                setFieldError(phoneInput, phoneWarning, "Invalid phone number format");
                hasError = true;
            } else {
                setFieldSuccess(phoneInput, phoneWarning);
            }
            if (passwordInput.getText().isEmpty()) {
                setFieldError(passwordInput, passwordWarning, "Password cannot be empty");
                hasError = true;
            } else if (!ValidityUtils.isValidPassword(passwordInput.getText())) {
                setFieldError(passwordInput, passwordWarning, "Password must be at least 8 characters");
                hasError = true;
            } else {
                setFieldSuccess(passwordInput, passwordWarning);
            }
            if (!emailInput.getText().isEmpty() && !ValidityUtils.isValidEmail(emailInput.getText())) {
                setFieldError(emailInput, emailWarning, "Invalid email format");
                hasError = true;
            } else {
                setFieldSuccess(emailInput, emailWarning);
            }
            if (roleComboBox.getValue() == null || roleComboBox.getValue().trim().isEmpty()) {
                roleWarning.setText("⚠ Please select a role");
                roleWarning.setStyle("-fx-text-fill: #ff4444; -fx-background-color: #fff5f5; -fx-padding: 5 10; -fx-background-radius: 5; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(255,68,68,0.2), 4, 0, 0, 1);");
                roleWarning.setVisible(true);
                hasError = true;
            } else {
                roleWarning.setVisible(false);
            }
            if (addressInput.getText().isEmpty()) {
                setFieldError(addressInput, addressWarning, "Address cannot be empty");
                hasError = true;
            } else {
                setFieldSuccess(addressInput, addressWarning);
            }
            if (!accountNumberInput.getText().isEmpty() && accountNumberInput.getText().length() < 10) {
                setFieldError(accountNumberInput, accountNumberWarning, "Account number must be at least 10 digits");
                hasError = true;
            } else {
                setFieldSuccess(accountNumberInput, accountNumberWarning);
            }
            if (hasError) {
                return;
            }
            HashMap<String, Object> userData = new HashMap<>();
            userData.put("full_name", fullNameInput.getText());
            userData.put("phone", phoneInput.getText());
            userData.put("email", emailInput.getText());
            userData.put("password", passwordInput.getText());
            userData.put("role", roleComboBox.getValue().toLowerCase());
            userData.put("address", addressInput.getText());
            userData.put("profileImageBase64", "");

            HashMap<String, String> bankInfo = new HashMap<>();
            bankInfo.put("bank_name", bankNameInput.getText());
            bankInfo.put("account_number", accountNumberInput.getText());
            userData.put("bank_info", bankInfo);

            ObjectMapper objectMapper = new ObjectMapper();
            String bodyRequest = objectMapper.writeValueAsString(userData);

            URL url = new URL("http://127.0.0.1:8082/auth/register");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(bodyRequest.getBytes());
            outputStream.flush();
            outputStream.close();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                statusMessageLbl.setText("✅ User created successfully");
                statusMessageLbl.setStyle("-fx-text-fill: #4CAF50; -fx-background-color: #f0fff4; -fx-padding: 10; -fx-background-radius: 8; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(76,175,80,0.3), 8, 0, 0, 0);");
                statusMessageLbl.setVisible(true);
                clearAllFields();
                Parent loginPage = FXMLLoader.load(getClass().getResource("/org/FoodOrder/client/view/signIn.fxml"));
                Scene loginScene = new Scene(loginPage);
                Stage currentStage = (Stage) loginLink.getScene().getWindow();
                currentStage.setScene(loginScene);
            } else {
                statusMessageLbl.setText("❌ Registration failed");
                statusMessageLbl.setStyle("-fx-text-fill: #ff4444; -fx-background-color: #fff5f5; -fx-padding: 10; -fx-background-radius: 8; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(255,68,68,0.3), 8, 0, 0, 0);");
                statusMessageLbl.setVisible(true);
            }
            connection.disconnect();

        } catch (Exception e) {
            statusMessageLbl.setText("❌ Error: " + e.getMessage());
            statusMessageLbl.setStyle("-fx-text-fill: #ff4444; -fx-background-color: #fff5f5; -fx-padding: 10; -fx-background-radius: 8; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(255,68,68,0.3), 8, 0, 0, 0);");
            statusMessageLbl.setVisible(true);
        }
    }
    private void clearAllFields() {
        phoneInput.setText("");
        fullNameInput.setText("");
        emailInput.setText("");
        passwordInput.setText("");
        addressInput.setText("");
        bankNameInput.setText("");
        accountNumberInput.setText("");
        roleComboBox.setValue(null);

        // Reset all field styles to normal
        setFieldNormal(phoneInput, phoneWarning);
        setFieldNormal(passwordInput, passwordWarning);
        setFieldNormal(emailInput, emailWarning);
        setFieldNormal(addressInput, addressWarning);
        setFieldNormal(accountNumberInput, accountNumberWarning);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        roleComboBox.getItems().addAll("buyer", "seller", "courier");
    }
}