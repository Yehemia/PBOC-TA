package com.hotelapp.controller.customer;

import com.hotelapp.dao.RoomDAO;
import com.hotelapp.model.Room;
import com.hotelapp.model.RoomType;
import com.hotelapp.util.AlertHelper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

public class RoomCellController {

    @FXML private ImageView roomImage;
    @FXML private Label roomTypeLabel;
    @FXML private Label priceLabel;
    @FXML private Label availabilityLabel;
    @FXML private Button detailButton; // <-- TAMBAHKAN DEKLARASI INI
    @FXML private Button bookButton;

    private RoomType roomType;
    private DashboardCustomerController dashboardController;

    public void setRoomTypeData(RoomType roomType, DashboardCustomerController dashboardController) {
        this.roomType = roomType;
        this.dashboardController = dashboardController;

        roomTypeLabel.setText(roomType.getName());

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        priceLabel.setText(currencyFormat.format(roomType.getPrice()) + " / malam");

        availabilityLabel.setText(roomType.getAvailableRoomCount() + " kamar tersedia");
        if (roomType.getAvailableRoomCount() <= 3) {
            availabilityLabel.setStyle("-fx-text-fill: #E67E22;");
        } else {
            availabilityLabel.setStyle("-fx-text-fill: #27AE60;");
        }

        try {
            String imageUrlPath = roomType.getImageUrl();
            if (imageUrlPath != null && !imageUrlPath.isBlank()) {
                String imagePath = imageUrlPath.startsWith("/") ? imageUrlPath : "/com/hotelapp/images/" + imageUrlPath;
                Image img = new Image(getClass().getResource(imagePath).toExternalForm());
                roomImage.setImage(img);
            } else {
                loadDefaultImage();
            }
        } catch (Exception e) {
            loadDefaultImage();
        }

        // --- TAMBAHKAN AKSI UNTUK TOMBOL DETAIL DI SINI ---
        detailButton.setOnAction(event -> openRoomDetail(roomType));

        bookButton.setOnAction(event -> {
            if (this.dashboardController != null) {
                this.dashboardController.openBooking(this.roomType);
            }
        });
    }

    /**
     * Metode baru untuk membuka halaman detail.
     * Ia akan mencari satu contoh kamar yang tersedia dari tipe ini untuk ditampilkan.
     */
    private void openRoomDetail(RoomType roomType) {
        try {
            // --- PERBAIKAN DI SINI: Panggil metode DAO baru yang aman ---
            Room sampleRoom = RoomDAO.findSampleAvailableRoomByType(roomType.getId());

            // Jika tidak ada kamar yang 'available', mungkin kita bisa ambil kamar manapun
            // dari tipe itu hanya untuk menampilkan detail tipe kamarnya.
            if (sampleRoom == null) {
                // Ini memerlukan metode DAO lain, untuk sekarang kita beri pesan saja.
                AlertHelper.showInformation("Info", "Saat ini tidak ada kamar tersedia untuk tipe ini, namun detail tipe kamar tetap ditampilkan.");
                // Kita buat "dummy" room object dengan nomor 0
                sampleRoom = new Room(0, 0, "N/A", roomType);
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelapp/fxml/customer/RoomDetail.fxml"));
            Parent detailRoot = loader.load();

            RoomDetailController detailController = loader.getController();
            detailController.setRoom(sampleRoom); // Kirim objek Room yang sudah lengkap
            detailController.setDashboardController(this.dashboardController);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Detail Tipe Kamar - " + roomType.getName());
            stage.setScene(new Scene(detailRoot));
            stage.showAndWait();
        } catch (Exception e) {
            System.err.println("Gagal memuat halaman detail kamar.");
            e.printStackTrace();
        }
    }

    private void loadDefaultImage() {
        try {
            String defaultPath = "/com/hotelapp/images/default_room.png";
            Image defaultImg = new Image(getClass().getResource(defaultPath).toExternalForm());
            roomImage.setImage(defaultImg);
        } catch (Exception e) {
            System.err.println("Gagal memuat gambar default.");
        }
    }
}