package com.hotelapp.controller.customer;
import com.hotelapp.model.Room;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class RoomDetailController {

    @FXML private ImageView roomImage;
    @FXML private Label roomNumberLabel;
    @FXML private Label roomTypeLabel;
    @FXML private Label priceLabel;
    @FXML private Label roomDescLabel;

    @FXML private Button bookingButton;
    @FXML private Button closeButton;

    private Room room;
    // Referensi ke dashboard contoh: DashboardCustomerController
    private DashboardCustomerController dashboardController;

    /**
     * Mengatur data kamar ke tampilan detail.
     */
    public void setRoom(Room room) {
        this.room = room;
        roomNumberLabel.setText("Nomor: " + room.getRoomNumber());
        roomTypeLabel.setText("Tipe: " + room.getRoomType());
        priceLabel.setText("Harga: $" + room.getPrice());

        try {
            String imagePath = room.getImageUrl().startsWith("/")
                    ? room.getImageUrl()
                    : "/com/hotelapp/images/" + room.getImageUrl();
            Image img = new Image(getClass().getResource(imagePath).toExternalForm());
            roomImage.setImage(img);
        } catch (Exception e) {
            System.err.println("Error loading room image: " + e.getMessage());
        }
    }

    /**
     * Setter untuk menginjeksi referensi DashboardCustomerController.
     */
    public void setDashboardController(DashboardCustomerController dashboardController) {
        this.dashboardController = dashboardController;
    }

    /**
     * Ditrigger ketika tombol "Pesan Kamar" ditekan.
     */
    @FXML
    public void onBookingButtonAction() {
        System.out.println("Memulai proses booking untuk kamar: " + room.getRoomNumber());
        if (dashboardController != null && room != null) {
            dashboardController.openBooking(room);
            closeWindow();
        } else {
            System.err.println("Gagal: dashboardController atau room tidak tersedia.");
        }
    }

    /**
     * Menutup jendela detail.
     */
    @FXML
    public void closeWindow() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
