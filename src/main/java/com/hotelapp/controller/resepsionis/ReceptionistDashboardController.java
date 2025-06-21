package com.hotelapp.controller.resepsionis;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class ReceptionistDashboardController {

    @FXML private BorderPane mainPane;
    @FXML private AnchorPane contentPane;
    @FXML private Button checkInButton;
    @FXML private Button checkOutButton;
    @FXML private Button offlineReservationButton;
    @FXML private Button penaltyButton;
    @FXML private Button historyButton;
    @FXML private Button logoutBtn;

    private Button currentButton;

    @FXML
    public void initialize() {
        loadContent("/com/hotelapp/fxml/resepsionis/CheckInView.fxml");
        setActiveButton(checkInButton);

        logoutBtn.setOnAction(e -> performLogout());
    }

    @FXML
    private void handleMenuClick(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        if (clickedButton == currentButton) {
            return;
        }

        String fxmlPath = "";
        if (event.getSource() == checkInButton) {
            fxmlPath = "/com/hotelapp/fxml/resepsionis/CheckInView.fxml";
        } else if (event.getSource() == checkOutButton) {
            fxmlPath = "/com/hotelapp/fxml/resepsionis/CheckOutView.fxml";
        } else if (event.getSource() == offlineReservationButton) {
            fxmlPath = "/com/hotelapp/fxml/resepsionis/OfflineReservationView.fxml";
        } else if (event.getSource() == penaltyButton) {
            fxmlPath = "/com/hotelapp/fxml/resepsionis/PenaltyView.fxml";
        } else if (event.getSource() == historyButton) {
            fxmlPath = "/com/hotelapp/fxml/resepsionis/HistoryView.fxml";
        }

        if (!fxmlPath.isEmpty()) {
            loadContent(fxmlPath);
            setActiveButton(clickedButton);
        }
    }

    private void setActiveButton(Button button) {
        if (currentButton != null) {
            currentButton.getStyleClass().remove("nav-button-selected");
        }
        button.getStyleClass().add("nav-button-selected");
        currentButton = button;
    }

    private void loadContent(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentPane.getChildren().setAll(view);
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);

        } catch (Exception e) {
            System.err.println("Gagal memuat FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }

    private void performLogout() {
        try {
            Stage stage = (Stage) mainPane.getScene().getWindow();
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/com/hotelapp/fxml/login.fxml"));
            Scene scene = new Scene(loginRoot);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}