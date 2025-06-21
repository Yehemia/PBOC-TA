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

public class RoomDetailController {

    @FXML private ImageView roomImage;
    @FXML private Label roomTypeLabel;
    @FXML private Label availabilityLabel; // Diubah dari roomNumberLabel
    @FXML private Label priceLabel;
    @FXML private Label descriptionLabel;
    @FXML private FlowPane highlightsPane;
    @FXML private Button bookingButton;

    private RoomType roomType; // Sekarang menyimpan RoomType
    private DashboardCustomerController dashboardController;

    public void setDashboardController(DashboardCustomerController dashboardController) {
        this.dashboardController = dashboardController;
    }

    // Metode ini sekarang menerima RoomType, bukan Room
    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
        updateUI();
    }

    private void updateUI() {
        if (roomType == null) return;

        // Set Gambar, Judul, Deskripsi, dan Harga dari RoomType
        try {
            String imageUrlPath = roomType.getImageUrl();
            if (imageUrlPath != null && !imageUrlPath.isBlank()) {
                String imagePath = imageUrlPath.startsWith("/") ? imageUrlPath : "/com/hotelapp/images/" + imageUrlPath;
                Image img = new Image(getClass().getResource(imagePath).toExternalForm());
                roomImage.setImage(img);
            }
        } catch (Exception e) { System.err.println("Gagal memuat gambar detail."); }

        roomTypeLabel.setText(roomType.getName());
        descriptionLabel.setText(roomType.getDescription());

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        priceLabel.setText(currencyFormat.format(roomType.getPrice()));

        // PERUBAHAN: Menampilkan jumlah kamar tersedia
        availabilityLabel.setText(roomType.getAvailableRoomCount() + " kamar tersedia");

        // PERUBAHAN: Menampilkan fasilitas dari database secara dinamis dengan ikon
        highlightsPane.getChildren().clear();
        if (roomType.getFacilities() != null) {
            for (Facility facility : roomType.getFacilities()) {
                highlightsPane.getChildren().add(createHighlightChip(facility.getName(), facility.getIconLiteral()));
            }
        }
    }

    // Metode bantuan untuk membuat "chip" fasilitas dengan ikon
    private HBox createHighlightChip(String text, String iconLiteral) {
        HBox chip = new HBox(10);
        chip.setAlignment(Pos.CENTER);
        chip.getStyleClass().add("highlight-chip");

        // Bagian ini yang membuat ikon
        if (iconLiteral != null && !iconLiteral.isBlank()) {
            FontIcon icon = new FontIcon(iconLiteral);
            chip.getChildren().add(icon);
        }

        chip.getChildren().add(new Label(text));
        return chip;
    }

    @FXML
    public void onBookingButtonAction() {
        if (dashboardController != null && roomType != null) {
            dashboardController.openBooking(this.roomType);
            closeWindow();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) bookingButton.getScene().getWindow();
        stage.close();
    }
}