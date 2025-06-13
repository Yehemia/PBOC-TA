package com.hotelapp.controller;

import com.hotelapp.model.Room;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;

public class RoomCellController {

    @FXML private ImageView roomImage;
    @FXML private Label roomNumberLabel;
    @FXML private Label roomTypeLabel;
    @FXML private Label priceLabel;
    @FXML private Button bookButton;

    private Room room;
    private DashboardCustomerController dashboardController;
    /**
     * Metode untuk mengatur data kamar ke tampilan UI
     * @param room objek Room yang akan ditampilkan
     */
    public void setRoomData(Room room, DashboardCustomerController dashboardController) {
        this.room = room;
        this.dashboardController = dashboardController;

        roomNumberLabel.setText("Nomor: " + room.getRoomNumber());
        roomTypeLabel.setText("Tipe: " + room.getRoomType());
        priceLabel.setText("Harga: $" + room.getPrice());

        try {
            // Debug: Cetak path gambar sebelum memuat
            System.out.println("Original image URL: " + room.getImageUrl());

            // Format path yang benar
            String imagePath = room.getImageUrl().startsWith("/")
                    ? room.getImageUrl()
                    : "/com/hotelapp/images/" + room.getImageUrl();

            System.out.println("Final image path: " + imagePath);

            URL imageUrl = getClass().getResource(imagePath);
            if (imageUrl == null) {
                System.err.println("Image not found: " + imagePath);
                loadDefaultImage();
                return;
            }

            Image img = new Image(imageUrl.toExternalForm());
            roomImage.setImage(img);
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
            loadDefaultImage();
        }

        bookButton.setOnAction(event -> {
            System.out.println("✅ Tombol 'Pesan Sekarang' ditekan!");
            if (dashboardController != null) {
                System.out.println("✅ Navigasi ke booking.fxml dengan kamar: " + room.getRoomNumber());
                dashboardController.openBooking(room);
            } else {
                System.err.println("❌ Gagal mendapatkan DashboardCustomerController!");
            }
        });
    }

    private void loadDefaultImage() {
        try {
            String defaultPath = "/com/hotelapp/images/default_room.jpeg";
            URL defaultUrl = getClass().getResource(defaultPath);
            if (defaultUrl == null) {
                System.err.println("Default image not found: " + defaultPath);
                return;
            }
            roomImage.setImage(new Image(defaultUrl.toExternalForm()));
        } catch (Exception e) {
            System.err.println("Failed to load default image: " + e.getMessage());
        }
    }

}

