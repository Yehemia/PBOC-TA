package com.hotelapp.controller.admin;

import com.hotelapp.util.Session;
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
import java.net.URL;

public class AdminDashboardController {

    @FXML
    private BorderPane mainPane;

    @FXML
    private AnchorPane contentPane;

    @FXML
    private Button dashboardBtn;

    @FXML
    private Button usersBtn;

    @FXML
    private Button roomsBtn;

    private Button currentButton;

    @FXML
    public void initialize() {
        loadContent("/com/hotelapp/fxml/admin/DashboardContent.fxml");
        setActiveButton(dashboardBtn);
        currentButton = dashboardBtn;
    }

    @FXML
    private void handleMenuClick(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        if (clickedButton == currentButton) {
            return;
        }

        String fxmlPath = "";
        if (event.getSource() == dashboardBtn) {
            fxmlPath = "/com/hotelapp/fxml/admin/DashboardContent.fxml";
        } else if (event.getSource() == usersBtn) {
            fxmlPath = "/com/hotelapp/fxml/admin/UserManagement.fxml";
        } else if (event.getSource() == roomsBtn) {
            fxmlPath = "/com/hotelapp/fxml/admin/RoomManagement.fxml";

        }

        loadContent(fxmlPath);
        setActiveButton(clickedButton);
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

    @FXML
    private void handleLogout() {
        try {
            Session.getInstance().clearSession();
            Stage stage = (Stage) mainPane.getScene().getWindow();
            URL loginFxml = getClass().getResource("/com/hotelapp/fxml/login.fxml");
            Parent loginRoot = FXMLLoader.load(loginFxml);
            Scene scene = new Scene(loginRoot);
            stage.setScene(scene);
            stage.setTitle("Sistem Reservasi Hotel - Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}