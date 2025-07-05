package com.hotelapp.controller.customer;

import com.hotelapp.model.Facility;
import com.hotelapp.model.RoomType;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Controller untuk halaman detail tipe kamar.
 * Menampilkan informasi lengkap tentang sebuah tipe kamar.
 */
public class RoomDetailController {

    // @FXML menghubungkan variabel ke komponen FXML.
    @FXML private ImageView roomImage; // Gambar kamar (lebih besar).
    @FXML private Label roomTypeLabel; // Nama tipe kamar.
    @FXML private Label availabilityLabel; // Info ketersediaan.
    @FXML private Label priceLabel; // Harga per malam.
    @FXML private Label descriptionLabel; // Deskripsi lengkap.
    @FXML private FlowPane highlightsPane; // Pane untuk menampilkan chip fasilitas.
    @FXML private Button bookingButton; // Tombol untuk memesan.

    private RoomType roomType; // Data tipe kamar yang ditampilkan.
    private DashboardCustomerController dashboardController; // Referensi ke controller utama.

    /**
     * Menerima referensi controller dasbor utama.
     */
    public void setDashboardController(DashboardCustomerController dashboardController) {
        this.dashboardController = dashboardController;
    }

    /**
     * Menerima data tipe kamar dan memperbarui UI.
     * @param roomType Data tipe kamar yang akan ditampilkan.
     */
    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
        updateUI();
    }

    /**
     * Memperbarui semua komponen UI dengan data dari objek RoomType.
     */
    private void updateUI() {
        if (roomType == null) return;

        // Muat gambar.
        try {
            String imageUrl = roomType.getImageUrl();
            if (imageUrl != null && !imageUrl.isBlank()) {
                Image img = new Image(imageUrl, true);
                roomImage.setImage(img);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Isi semua label dengan data yang sesuai.
        roomTypeLabel.setText(roomType.getName());
        descriptionLabel.setText(roomType.getDescription());
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        priceLabel.setText(currencyFormat.format(roomType.getPrice()));
        availabilityLabel.setText(roomType.getAvailableRoomCount() + " kamar tersedia");

        // Hapus fasilitas lama dan buat chip fasilitas yang baru.
        highlightsPane.getChildren().clear();
        if (roomType.getFacilities() != null && !roomType.getFacilities().isEmpty()) {
            for (Facility facility : roomType.getFacilities()) {
                // Buat chip untuk setiap fasilitas dan tambahkan ke pane.
                highlightsPane.getChildren().add(createHighlightChip(facility.getName(), facility.getIconLiteral()));
            }
        } else {
            highlightsPane.getChildren().add(new Label("Tidak ada data fasilitas."));
        }
    }

    /**
     * Membuat sebuah "chip" (HBox dengan style) untuk menampilkan satu fasilitas.
     * @param text Nama fasilitas (e.g., "WiFi").
     * @param iconLiteral Kode ikon dari Ikonli (e.g., "fa-wifi").
     * @return HBox yang sudah di-style sebagai chip.
     */
    private HBox createHighlightChip(String text, String iconLiteral) {
        HBox chip = new HBox(8); // HBox dengan spasi 8px.
        chip.setAlignment(Pos.CENTER);
        chip.getStyleClass().add("highlight-chip"); // Terapkan style CSS.

        // Jika ada ikon, buat dan tambahkan ke chip.
        if (iconLiteral != null && !iconLiteral.isBlank()) {
            FontIcon icon = new FontIcon(iconLiteral);
            chip.getChildren().add(icon);
        }

        chip.getChildren().add(new Label(text));
        return chip;
    }

    /**
     * Aksi yang dijalankan saat tombol "Pesan Sekarang" diklik.
     */
    @FXML
    public void onBookingButtonAction() {
        if (dashboardController != null && roomType != null) {
            // Panggil metode openBooking di controller utama.
            dashboardController.openBooking(this.roomType);
            // Tutup jendela detail setelah tombol pesan diklik.
            closeWindow();
        }
    }

    /**
     * Metode pembantu untuk menutup jendela detail.
     */
    private void closeWindow() {
        Stage stage = (Stage) bookingButton.getScene().getWindow();
        stage.close();
    }
}