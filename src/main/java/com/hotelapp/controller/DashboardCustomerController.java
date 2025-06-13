package com.hotelapp.controller;

import com.hotelapp.model.Room;
import com.hotelapp.model.User;
import com.hotelapp.util.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.io.IOException;

public class DashboardCustomerController {

    @FXML private Label welcomeLabel;
    @FXML private Button dashboardButton;
    @FXML private Button historyButton;
    @FXML private Button profileButton;
    @FXML private Button logoutButton;

    // Container center yang akan diisi secara dinamis
    @FXML private AnchorPane contentPane;

    @FXML
    public void initialize() {
        System.out.println("Initializing DashboardCustomerController");

        User currentUser = Session.getInstance().getCurrentUser();
        welcomeLabel.setText(currentUser != null ? "Selamat datang, " + currentUser.getName() : "Selamat datang, Tamu");

        dashboardButton.setOnAction(event -> loadDashboardContent());
        historyButton.setOnAction(event -> loadHistoryContent());
        profileButton.setOnAction(event -> loadProfileContent());
        logoutButton.setOnAction(event -> logout());

        // Tampilkan konten dashboard awal (misalnya, daftar kamar)
        loadDashboardContent();
    }

    // Memuat konten dashboard (daftar kamar) dari FXML terpisah
    private void loadDashboardContent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelapp/fxml/dashboardContent.fxml"));
            Parent dashboardContent = loader.load();
            // Dapatkan controller dari dashboardContent.fxml
            DashboardContentController contentController = loader.getController();
            // Set dashboard controller (this) ke controller konten
            contentController.setDashboardCustomerController(this);

            contentPane.getChildren().setAll(dashboardContent);
            AnchorPane.setTopAnchor(dashboardContent, 0.0);
            AnchorPane.setRightAnchor(dashboardContent, 0.0);
            AnchorPane.setBottomAnchor(dashboardContent, 0.0);
            AnchorPane.setLeftAnchor(dashboardContent, 0.0);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Metode untuk memuat tampilan history booking (sesuaikan jika sudah ada FXML untuk History)
    private void loadHistoryContent() {
        try {
            Parent historyContent = FXMLLoader.load(getClass().getResource("/com/hotelapp/fxml/history.fxml"));
            contentPane.getChildren().setAll(historyContent);
            AnchorPane.setTopAnchor(historyContent, 0.0);
            AnchorPane.setRightAnchor(historyContent, 0.0);
            AnchorPane.setBottomAnchor(historyContent, 0.0);
            AnchorPane.setLeftAnchor(historyContent, 0.0);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Metode untuk memuat tampilan profil (sesuaikan jika sudah ada FXML untuk Profile)
    private void loadProfileContent() {
        try {
            Parent profileContent = FXMLLoader.load(getClass().getResource("/com/hotelapp/fxml/profile.fxml"));
            contentPane.getChildren().setAll(profileContent);
            AnchorPane.setTopAnchor(profileContent, 0.0);
            AnchorPane.setRightAnchor(profileContent, 0.0);
            AnchorPane.setBottomAnchor(profileContent, 0.0);
            AnchorPane.setLeftAnchor(profileContent, 0.0);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Untuk logout, kita ganti keseluruhan scene ke halaman login
    private void logout() {
        try {
            // Bersihkan sesi
            Session.getInstance().setCurrentUser(null);
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/com/hotelapp/fxml/login.fxml"));
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(loginRoot));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Method openBooking yang dapat dipanggil dari RoomCell melalui DashboardContentController
    public void openBooking(Room selectedRoom) {
        try {
            System.out.println("✅ Navigasi ke booking.fxml dengan kamar: " + selectedRoom.getRoomNumber());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelapp/fxml/booking.fxml"));
            Parent root = loader.load();
            BookingController bookingController = loader.getController();
            bookingController.setRoom(selectedRoom);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("❌ Gagal memuat booking.fxml: " + e.getMessage());
        }
    }
}