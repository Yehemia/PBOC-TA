package com.hotelapp.controller.customer;

import com.hotelapp.dao.RoomTypeDAO;
import com.hotelapp.model.RoomType;
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

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Controller untuk satu "kartu" kamar yang ditampilkan di dasbor pelanggan.
 * Setiap kartu merepresentasikan satu tipe kamar yang tersedia.
 */
public class RoomCellController {

    // @FXML menghubungkan variabel ke komponen FXML.
    @FXML private ImageView roomImage; // Gambar kamar.
    @FXML private Label roomTypeLabel; // Nama tipe kamar.
    @FXML private Label priceLabel; // Harga per malam.
    @FXML private Label availabilityLabel; // Info ketersediaan.
    @FXML private Button detailButton; // Tombol untuk melihat detail.
    @FXML private Button bookButton; // Tombol untuk memesan.

    private RoomType roomType; // Data tipe kamar untuk kartu ini.
    private DashboardCustomerController dashboardController; // Referensi ke controller dasbor utama.

    /**
     * Mengisi data ke dalam kartu kamar.
     * @param roomType Data tipe kamar yang akan ditampilkan.
     * @param dashboardController Referensi ke controller utama untuk navigasi.
     */
    public void setRoomTypeData(RoomType roomType, DashboardCustomerController dashboardController) {
        this.roomType = roomType;
        this.dashboardController = dashboardController;

        roomTypeLabel.setText(roomType.getName());

        // Format harga ke dalam format mata uang Rupiah.
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        priceLabel.setText(currencyFormat.format(roomType.getPrice()));

        // Tampilkan jumlah kamar tersedia dan ubah warna teks jika kamar hampir habis.
        availabilityLabel.setText(roomType.getAvailableRoomCount() + " kamar tersedia");
        if (roomType.getAvailableRoomCount() <= 3) {
            availabilityLabel.setStyle("-fx-text-fill: #E67E22;"); // Warna oranye.
        } else {
            availabilityLabel.setStyle("-fx-text-fill: #27AE60;"); // Warna hijau.
        }

        // Coba muat gambar dari URL. Jika gagal atau URL tidak ada, muat gambar default.
        try {
            String imageUrl = roomType.getImageUrl();
            if (imageUrl != null && !imageUrl.isBlank()) {
                Image img = new Image(imageUrl, true); // 'true' untuk loading di background.
                roomImage.setImage(img);
            } else {
                loadDefaultImage();
            }
        } catch (Exception e) {
            e.printStackTrace();
            loadDefaultImage();
        }

        // Atur aksi untuk tombol detail.
        detailButton.setOnAction(event -> openRoomDetail(roomType));

        // Atur aksi untuk tombol pesan.
        bookButton.setOnAction(event -> {
            // Panggil metode openBooking di controller dasbor utama.
            if (this.dashboardController != null) {
                this.dashboardController.openBooking(this.roomType);
            }
        });
    }


    /**
     * Membuka jendela detail untuk tipe kamar yang dipilih.
     * @param roomType Tipe kamar yang detailnya akan ditampilkan.
     */
    private void openRoomDetail(RoomType roomType) {
        try {
            // Ambil data lengkap tipe kamar, termasuk fasilitasnya.
            RoomType detailedRoomType = RoomTypeDAO.getRoomTypeWithFacilitiesById(roomType.getId());

            // Muat FXML untuk halaman detail.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelapp/fxml/customer/RoomDetail.fxml"));
            Parent detailRoot = loader.load();
            RoomDetailController detailController = loader.getController();

            // Kirim data ke controller detail.
            detailController.setRoomType(detailedRoomType);
            detailController.setDashboardController(this.dashboardController);

            // Tampilkan halaman detail sebagai dialog modal.
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Detail Tipe Kamar - " + detailedRoomType.getName());
            stage.setScene(new Scene(detailRoot));
            stage.showAndWait();

        } catch (Exception e) {
            System.err.println("Gagal memuat halaman detail kamar.");
            e.printStackTrace();
        }
    }

    /**
     * Memuat gambar default jika gambar dari URL tidak tersedia.
     */
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