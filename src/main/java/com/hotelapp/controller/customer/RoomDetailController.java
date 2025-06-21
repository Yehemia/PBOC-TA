package com.hotelapp.controller.customer;

import com.hotelapp.model.Facility;
import com.hotelapp.model.Room;
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

public class RoomDetailController {

    @FXML private ImageView roomImage;
    @FXML private Label roomTypeLabel;
    @FXML private Label roomNumberLabel;
    @FXML private Label priceLabel;
    @FXML private Label descriptionLabel;
    @FXML private FlowPane highlightsPane;
    @FXML private Button bookingButton;

    private Room room;
    private DashboardCustomerController dashboardController;

    public void setDashboardController(DashboardCustomerController dashboardController) {
        this.dashboardController = dashboardController;
    }

    // Metode ini menerima objek Room yang sudah lengkap dari DAO
    public void setRoom(Room room) {
        this.room = room;
        updateUI();
    }

    private void updateUI() {
        if (room == null || room.getRoomType() == null) {
            roomTypeLabel.setText("Data Kamar Tidak Ditemukan");
            return;
        }

        // Ambil objek RoomType untuk kemudahan
        RoomType roomType = room.getRoomType();

        // 1. Set Gambar (diambil dari RoomType)
        try {
            String imageUrlPath = roomType.getImageUrl();
            if (imageUrlPath != null && !imageUrlPath.isBlank()) {
                String imagePath = imageUrlPath.startsWith("/") ? imageUrlPath : "/com/hotelapp/images/" + imageUrlPath;
                Image img = new Image(getClass().getResource(imagePath).toExternalForm());
                roomImage.setImage(img);
            }
        } catch (Exception e) {
            System.err.println("Gagal memuat gambar detail: " + e.getMessage());
        }

        // 2. Set Teks (diambil dari Room dan RoomType)
        roomTypeLabel.setText(roomType.getName());
        roomNumberLabel.setText("Contoh Kamar: No. " + room.getRoomNumber()); // Hanya sebagai contoh nomor kamar
        descriptionLabel.setText(roomType.getDescription());

        // Format harga
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        priceLabel.setText(currencyFormat.format(roomType.getPrice()));

        // 3. Tampilkan fasilitas dari database secara dinamis
        highlightsPane.getChildren().clear();
        if (roomType.getFacilities() != null) {
            for (Facility facility : roomType.getFacilities()) {
                highlightsPane.getChildren().add(createHighlightChip(facility.getName(), facility.getIconLiteral()));
            }
        }
    }

    // Metode bantuan untuk membuat "chip" fasilitas
    private HBox createHighlightChip(String text, String iconLiteral) {
        HBox chip = new HBox(10);
        chip.setAlignment(Pos.CENTER);
        chip.getStyleClass().add("highlight-chip");

        if (iconLiteral != null && !iconLiteral.isBlank()) {
            FontIcon icon = new FontIcon(iconLiteral);
            chip.getChildren().add(icon);
        }

        chip.getChildren().add(new Label(text));
        return chip;
    }

    @FXML
    public void onBookingButtonAction() {
        // Karena sekarang kita booking berdasarkan Tipe Kamar, kita teruskan RoomType-nya
        if (dashboardController != null && room != null && room.getRoomType() != null) {
            dashboardController.openBooking(room.getRoomType());
            closeWindow();
        } else {
            System.err.println("Gagal memulai booking: data tidak lengkap.");
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) bookingButton.getScene().getWindow();
        stage.close();
    }
}