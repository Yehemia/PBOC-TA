package com.hotelapp.controller;

import com.hotelapp.model.Room;
import com.hotelapp.model.User;
import com.hotelapp.service.RoomService;
import com.hotelapp.util.RoomCell;
import com.hotelapp.util.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;

public class DashboardCustomerController {

    @FXML private Label welcomeLabel;
    @FXML private ListView<Room> roomList;
    @FXML private Button dashboardButton;
    @FXML private Button historyButton;
    @FXML private Button profileButton;
    @FXML private Button logoutButton;

    @FXML
    public void initialize() {
        System.out.println("Initializing DashboardCustomerController");

        // Tampilkan nama user di bagian selamat datang
        User currentUser = Session.getInstance().getCurrentUser();
        welcomeLabel.setText(currentUser != null ? "Selamat datang, " + currentUser.getName() : "Selamat datang, Tamu");

        // Ambil daftar kamar yang tersedia
        ObservableList<Room> availableRooms = FXCollections.observableArrayList(RoomService.getAvailableRooms());
        roomList.setItems(availableRooms);

        // Gunakan custom cell untuk menampilkan kamar
        roomList.setCellFactory(listView -> new RoomCell(this));



        // Tambahkan event handler untuk navigasi
        dashboardButton.setOnAction(event -> navigateTo("/com/hotelapp/fxml/dashboard.fxml"));
        historyButton.setOnAction(event -> navigateTo("/com/hotelapp/fxml/history_booking.fxml"));
        profileButton.setOnAction(event -> navigateTo("/com/hotelapp/fxml/profile.fxml"));
        logoutButton.setOnAction(event -> logout());
    }

    // Navigasi ke halaman lain
    private void navigateTo(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) dashboardButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Logout user dan kembali ke login screen
    private void logout() {
        Session.getInstance().setCurrentUser(null);
        navigateTo("/com/hotelapp/fxml/login.fxml");
    }

    // Metode untuk membuka booking form
    public void openBooking(Room selectedRoom) {
        try {
            System.out.println("✅ Navigasi ke booking.fxml dengan kamar: " + selectedRoom.getRoomNumber());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelapp/fxml/booking.fxml"));
            Parent root = loader.load();
            BookingController bookingController = loader.getController();
            bookingController.setRoom(selectedRoom);
            if (selectedRoom == null) {
                System.err.println("❌ selectedRoom masih null, tidak bisa reservasi!");
            } else {
                System.out.println("✅ Kamar yang dipilih: " + selectedRoom.getRoomNumber() + ", ID: " + selectedRoom.getId());
            }

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("❌ Gagal memuat booking.fxml: " + e.getMessage());
        }
    }

}