package com.hotelapp.controller.customer;

import com.hotelapp.model.Room;
import com.hotelapp.model.RoomType;
import com.hotelapp.model.User;
import com.hotelapp.util.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class DashboardCustomerController {

    @FXML private BorderPane mainPane;
    @FXML private AnchorPane contentPane;
    @FXML private ImageView logoImageView;
    @FXML private Label welcomeLabel;
    @FXML private Button dashboardButton;
    @FXML private Button historyButton;
    @FXML private Button profileButton;
    @FXML private Button logoutButton;
    private Button currentButton;

    @FXML
    public void initialize() {
        loadWelcomeMessage();
        loadDashboardContent();
        setActiveButton(dashboardButton);
    }

    private void loadWelcomeMessage() {
        User currentUser = Session.getInstance().getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Selamat datang,\n" + currentUser.getName());
        } else {
            welcomeLabel.setText("Selamat Datang!");
        }
    }

    @FXML
    private void handleMenuClick(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        if (clickedButton == currentButton) return;

        setActiveButton(clickedButton);

        if (clickedButton == dashboardButton) {
            loadDashboardContent();
        } else if (clickedButton == historyButton) {
            loadContent("/com/hotelapp/fxml/customer/history.fxml");
        } else if (clickedButton == profileButton) {
            loadContent("/com/hotelapp/fxml/customer/profile.fxml");
        }
    }

    private void loadDashboardContent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelapp/fxml/customer/DashboardContent.fxml"));
            Parent dashboardContent = loader.load();
            DashboardContentController contentController = loader.getController();
            contentController.setDashboardCustomerController(this);
            setPaneContent(dashboardContent);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void loadContent(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            setPaneContent(view);
        } catch (Exception e) {
            System.err.println("Gagal memuat FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }

    private void setPaneContent(Parent content) {
        contentPane.getChildren().setAll(content);
        AnchorPane.setTopAnchor(content, 0.0);
        AnchorPane.setBottomAnchor(content, 0.0);
        AnchorPane.setLeftAnchor(content, 0.0);
        AnchorPane.setRightAnchor(content, 0.0);
    }

    public void openBooking(RoomType selectedRoomType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelapp/fxml/customer/booking.fxml"));
            Parent root = loader.load();
            BookingController bookingController = loader.getController();
            bookingController.setRoomType(selectedRoomType);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Form Pemesanan");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException e) {
            System.err.println("Gagal memuat booking.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button button) {
        if (currentButton != null) {
            currentButton.getStyleClass().remove("sidebar-button-selected");
        }

        button.getStyleClass().add("sidebar-button-selected");
        currentButton = button;
    }

    @FXML
    public void logout() {
        try {
            Session.getInstance().clearSession();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/com/hotelapp/fxml/login.fxml"));
            Scene scene = new Scene(loginRoot);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}