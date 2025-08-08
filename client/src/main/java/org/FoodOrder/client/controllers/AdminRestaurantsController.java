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
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.FoodOrder.client.http.HttpController;
import org.FoodOrder.client.http.HttpHeaders;
import org.FoodOrder.client.http.HttpMethod;
import org.FoodOrder.client.http.HttpResponse;
import org.FoodOrder.client.models.Restaurant;
import org.FoodOrder.client.models.Vendor;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import javafx.beans.property.SimpleStringProperty;
import org.FoodOrder.client.sessions.UserSession;

public class AdminRestaurantsController implements Initializable {

    @FXML private TableView<Restaurant> restaurantsTable;
    @FXML private TableColumn<Restaurant, Integer> idColumn;
    @FXML private TableColumn<Restaurant, String> nameColumn;
    @FXML private TableColumn<Restaurant, String> addressColumn;
    @FXML private TableColumn<Restaurant, String> phoneColumn;
    @FXML private TableColumn<Restaurant, String> sellerColumn;
    @FXML private TableColumn<Restaurant, Double> averageRatingColumn;
    @FXML private TableColumn<Restaurant, Void> actionColumn;
    @FXML private Button refreshButton;
    @FXML private Button backBtn;
    @FXML private Label totalRestaurantsLabel;
    @FXML private BorderPane rootPane;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private volatile boolean isLoading = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/FoodOrder/client/view/footer.fxml"));
            Parent footer = loader.load();
            FooterController footerController = loader.getController();
            footerController.setActive("manageRestaurants");
            rootPane.setBottom(footer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setupTableColumns();
        setupActionColumn();
        setupTableStyling();
        loadRestaurantsAsync();
    }

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

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        sellerColumn.setCellValueFactory(cellData -> {
            Restaurant restaurant = cellData.getValue();
            return restaurant.getSeller() != null ? new SimpleStringProperty(restaurant.getSeller().getFullName()) : new SimpleStringProperty("N/A");
        });
        averageRatingColumn.setCellValueFactory(new PropertyValueFactory<>("averageRating"));
    }

    private void setupActionColumn() {
        actionColumn.setCellFactory(param -> new TableCell<Restaurant, Void>() {
            private final Button editButton = createStyledButton("✏️ Edit", "edit-button");
            private final Button deleteButton = createStyledButton("❌ Delete", "delete-button");
            private final HBox actionBox = new HBox(8, editButton, deleteButton);

            {
                actionBox.setStyle("-fx-alignment: center;");

                editButton.setOnAction(event -> {
                    Restaurant restaurant = getTableView().getItems().get(getIndex());
                    // TODO: Implement edit functionality (e.g., open edit dialog)
                    showInfoAlert("Edit", "Edit functionality for restaurant ID " + restaurant.getId() + " not implemented yet.");
                });

                deleteButton.setOnAction(event -> {
                    Restaurant restaurant = getTableView().getItems().get(getIndex());
                    deleteRestaurantAsync(restaurant.getId());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    setGraphic(actionBox);
                }
            }
        });
    }

    private Button createStyledButton(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().addAll("button", styleClass);
        return button;
    }

    private void setupTableStyling() {
        restaurantsTable.getStyleClass().add("table-view");
        Label emptyLabel = new Label("No restaurants found\nClick refresh to load restaurants");
        emptyLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 14px; -fx-text-alignment: center;");
        restaurantsTable.setPlaceholder(emptyLabel);
    }

    @FXML
    private void refreshRestaurants() {
        loadRestaurantsAsync();
    }

    private void loadRestaurantsAsync() {
        if (isLoading) {
            showInfoAlert("Loading", "Please wait, restaurants are already being loaded...");
            return;
        }

        isLoading = true;
        updateRefreshButton(true);

        CompletableFuture.supplyAsync(() -> {
            try {
                return fetchRestaurantsFromServer();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(restaurants -> {
            Platform.runLater(() -> {
                restaurantsTable.setItems(FXCollections.observableArrayList(restaurants));
                updateTotalRestaurantsLabel(restaurants.size());
                isLoading = false;
                updateRefreshButton(false);
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                restaurantsTable.setItems(FXCollections.observableArrayList());
                updateTotalRestaurantsLabel(0);
                showErrorAlert("Loading Error", "Failed to load restaurants: " + throwable.getMessage());
                isLoading = false;
                updateRefreshButton(false);
            });
            return null;
        });
    }

    private List<Restaurant> fetchRestaurantsFromServer() throws Exception {
        String url = "http://localhost:8082/admin/restaurants";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + UserSession.getToken());
        HttpResponse response = HttpController.sendRequest(url, HttpMethod.GET, null, headers);

        switch (response.getStatusCode()) {
            case 200:
                return objectMapper.readValue(response.getBody(), new TypeReference<List<Restaurant>>() {});
            case 401:
                throw new RuntimeException("Unauthorized access - Please login again");
            case 403:
                throw new RuntimeException("Forbidden - Admin role required");
            default:
                throw new RuntimeException("Server error: " + response.getStatusCode() + " - " + response.getBody());
        }
    }

    private void deleteRestaurantAsync(int restaurantId) {
        if (isLoading) {
            showInfoAlert("Please Wait", "Another operation is in progress...");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                deleteRestaurantOnServer(restaurantId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenRun(() -> {
            Platform.runLater(() -> {
                showSuccessAlert("Success", "Restaurant with ID " + restaurantId + " deleted successfully!");
                loadRestaurantsAsync();
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                showErrorAlert("Delete Error", "Failed to delete restaurant: " + throwable.getMessage());
            });
            return null;
        });
    }

    private void deleteRestaurantOnServer(int restaurantId) throws Exception {
        String url = "http://localhost:8082/admin/restaurants/" + restaurantId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + UserSession.getToken());

        HttpResponse response = HttpController.sendRequest(url, HttpMethod.DELETE, null, headers);

        if (response.getStatusCode() != 200) {
            throw new RuntimeException("Server responded with: " + response.getStatusCode() + " - " + response.getBody());
        }
    }

    private void updateRefreshButton(boolean loading) {
        if (refreshButton != null) {
            refreshButton.setDisable(loading);
            refreshButton.setText(loading ? "🔄 Loading..." : "🔄 Refresh Restaurants");
        }
    }

    private void updateTotalRestaurantsLabel(int count) {
        if (totalRestaurantsLabel != null) {
            Platform.runLater(() ->
                    totalRestaurantsLabel.setText(String.format("Total Restaurants: %d", count))
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
                getClass().getResource("/org/FoodOrder/client/view/admin-restaurants.css").toExternalForm()
        );
        alert.showAndWait();
    }
}