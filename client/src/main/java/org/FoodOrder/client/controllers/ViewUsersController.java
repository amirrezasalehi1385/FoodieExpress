package org.FoodOrder.client.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.FoodOrder.client.http.HttpController;
import org.FoodOrder.client.http.HttpHeaders;
import org.FoodOrder.client.http.HttpMethod;
import org.FoodOrder.client.http.HttpResponse;
import org.FoodOrder.client.models.User;
import org.FoodOrder.client.enums.Role;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ViewUsersController {

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Long> idColumn;
    @FXML private TableColumn<User, String> fullNameColumn;
    @FXML private TableColumn<User, String> phoneColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, Role> roleColumn;
    @FXML private TableColumn<User, String> addressColumn;
    @FXML private TableColumn<User, String> approvalStatusColumn;
    @FXML private TableColumn<User, Void> actionColumn;
    @FXML private Button refreshButton;
    @FXML private Button backBtn;
    @FXML private Label totalUsersLabel;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private volatile boolean isLoading = false;

    @FXML
    private void onBackBtnAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/FoodOrder/client/view/home.fxml"));
            Parent page = loader.load();
            Scene scene = new Scene(page);
            Stage currentStage = (Stage) backBtn.getScene().getWindow();
            currentStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Failed to navigate back: " + e.getMessage());
        }
    }

    @FXML
    private void initialize() {
        setupTableColumns();
        setupActionColumn();
        setupTableStyling();
        loadUsersAsync();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        approvalStatusColumn.setCellValueFactory(new PropertyValueFactory<>("approvalStatus"));
        approvalStatusColumn.setCellFactory(column -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                    getStyleClass().removeAll("status-approved", "status-pending", "status-rejected");
                } else {
                    Label statusLabel = new Label(status.toUpperCase());
                    statusLabel.getStyleClass().removeAll("status-approved", "status-pending", "status-rejected");
                    switch (status.toLowerCase()) {
                        case "approved":
                            statusLabel.getStyleClass().add("status-approved");
                            statusLabel.setText("✅ APPROVED");
                            break;
                        case "rejected":
                            statusLabel.getStyleClass().add("status-rejected");
                            statusLabel.setText("❌ REJECTED");
                            break;
                        case "waiting":
                        default:
                            statusLabel.getStyleClass().add("status-pending");
                            statusLabel.setText("⏳ PENDING");
                            break;
                    }

                    setGraphic(statusLabel);
                    setText(null);
                }
            }
        });
    }

    private void setupActionColumn() {
        actionColumn.setCellFactory(new Callback<TableColumn<User, Void>, TableCell<User, Void>>() {
            @Override
            public TableCell<User, Void> call(TableColumn<User, Void> param) {
                return new TableCell<User, Void>() {
                    private final Button approveButton = createStyledButton("✅ Approve", "approve-button");
                    private final Button rejectButton = createStyledButton("❌ Reject", "reject-button");
                    private final HBox actionBox = new HBox(8, approveButton, rejectButton);

                    {
                        actionBox.setStyle("-fx-alignment: center;");

                        approveButton.setOnAction(event -> {
                            User user = getTableView().getItems().get(getIndex());
                            updateUserStatusAsync(user.getId(), "approved", user.getFullName());
                        });

                        rejectButton.setOnAction(event -> {
                            User user = getTableView().getItems().get(getIndex());
                            updateUserStatusAsync(user.getId(), "rejected", user.getFullName());
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || getIndex() >= getTableView().getItems().size()) {
                            setGraphic(null);
                        } else {
                            User user = getTableView().getItems().get(getIndex());
                            if ("WAITING".equalsIgnoreCase(user.getApprovalStatus())) {
                                setGraphic(actionBox);
                            } else {
                                setGraphic(null);
                            }
                        }
                    }
                };
            }
        });
    }

    private Button createStyledButton(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().addAll("button", styleClass);
        return button;
    }

    private void setupTableStyling() {
        usersTable.getStyleClass().add("table-view");
        Label emptyLabel = new Label("No users found\nClick refresh to load users");
        emptyLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 14px; -fx-text-alignment: center;");
        usersTable.setPlaceholder(emptyLabel);
    }

    @FXML
    private void refreshUsers() {
        loadUsersAsync();
    }

    private void loadUsersAsync() {
        if (isLoading) {
            showInfoAlert("Loading", "Please wait, users are already being loaded...");
            return;
        }

        isLoading = true;
        updateRefreshButton(true);

        CompletableFuture.supplyAsync(() -> {
            try {
                return fetchUsersFromServer();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(users -> {
            Platform.runLater(() -> {
                usersTable.setItems(FXCollections.observableArrayList(users));
                updateTotalUsersLabel(users.size());
                isLoading = false;
                updateRefreshButton(false);
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                usersTable.setItems(FXCollections.observableArrayList());
                updateTotalUsersLabel(0);
                showErrorAlert("Loading Error", "Failed to load users: " + throwable.getMessage());
                isLoading = false;
                updateRefreshButton(false);
            });
            return null;
        });
    }

    private List<User> fetchUsersFromServer() throws Exception {
        String url = "http://localhost:8082/admin/users";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        HttpResponse response = HttpController.sendRequest(url, HttpMethod.GET, null, headers);

        switch (response.getStatusCode()) {
            case 200:
                return objectMapper.readValue(response.getBody(), new TypeReference<List<User>>() {});
            case 401:
                throw new RuntimeException("Unauthorized access - Please login again");
            case 403:
                throw new RuntimeException("Forbidden - Admin role required");
            default:
                throw new RuntimeException("Server error: " + response.getStatusCode() + " - " + response.getBody());
        }
    }

    private void updateUserStatusAsync(Long userId, String status, String userName) {
        if (isLoading) {
            showInfoAlert("Please Wait", "Another operation is in progress...");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                updateUserStatusOnServer(userId, status);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenRun(() -> {
            Platform.runLater(() -> {
                showSuccessAlert("Success", String.format("User '%s' has been %s successfully!", userName, status));
                loadUsersAsync();
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                showErrorAlert("Update Error", "Failed to update user status: " + throwable.getMessage());
            });
            return null;
        });
    }

    private void updateUserStatusOnServer(Long userId, String status) throws Exception {
        String url = "http://localhost:8082/admin/users/" + userId + "/status";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("status", status);
        String jsonBody = objectMapper.writeValueAsString(requestBody);

        HttpResponse response = HttpController.sendRequest(url, HttpMethod.PUT, jsonBody, headers);

        if (response.getStatusCode() != 200) {
            throw new RuntimeException("Server responded with: " + response.getStatusCode() + " - " + response.getBody());
        }
    }

    private void updateRefreshButton(boolean loading) {
        if (refreshButton != null) {
            refreshButton.setDisable(loading);
            refreshButton.setText(loading ? "🔄 Loading..." : "🔄 Refresh Users");
        }
    }

    private void updateTotalUsersLabel(int count) {
        if (totalUsersLabel != null) {
            Platform.runLater(() ->
                    totalUsersLabel.setText(String.format("Total Users: %d", count))
            );
        }
    }
    private void showSuccessAlert(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }

    private void showErrorAlert(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
    }

    private void showInfoAlert(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/org/FoodOrder/client/view/view-users.css").toExternalForm()
        );

        alert.showAndWait();
    }
}