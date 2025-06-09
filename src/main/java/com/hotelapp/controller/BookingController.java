package com.hotelapp.controller;

import com.hotelapp.dao.ReservationDAO;
import com.hotelapp.model.Reservation;
import com.hotelapp.model.Room;
import com.hotelapp.model.User;
import com.hotelapp.util.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BookingController {

    @FXML private DatePicker checkInPicker;
    @FXML private DatePicker checkOutPicker;
    @FXML private ComboBox<String> paymentMethodComboBox;
    @FXML private Button confirmBookingButton;

    private Room selectedRoom;

    public void setRoom(Room room) {
        this.selectedRoom = room;
    }

    @FXML
    public void initialize() {
        confirmBookingButton.setOnAction(event -> processBooking());
    }

    private void navigateToPayment(Reservation reservation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelapp/fxml/payment.fxml"));
            Parent root = loader.load();

            PaymentController paymentController = loader.getController();
            paymentController.setReservation(reservation); // Kirim data reservasi ke halaman pembayaran

            Stage stage = (Stage) confirmBookingButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("❌ Gagal memuat halaman pembayaran: " + e.getMessage());
        }
    }

    private void processBooking() {
        LocalDate checkInDate = checkInPicker.getValue();
        LocalDate checkOutDate = checkOutPicker.getValue();
        String paymentMethod = paymentMethodComboBox.getValue();

        if (checkInDate == null || checkOutDate == null || paymentMethod == null) {
            showAlert("Error", "Harap isi semua field sebelum melanjutkan.");
            return;
        }

        if (checkOutDate.isBefore(checkInDate)) {
            showAlert("Error", "Tanggal check-out harus lebih lambat dari check-in.");
            return;
        }

        User currentUser = Session.getInstance().getCurrentUser();
        if (currentUser == null) {
            showAlert("Error", "Anda harus login untuk melakukan pemesanan.");
            return;
        }

        // ✅ Hitung jumlah malam & total harga
        long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        double totalPrice = selectedRoom.getPrice() * nights;

        // Buat objek reservasi dengan total harga yang diperhitungkan
        Reservation reservation = new Reservation(
                currentUser.getId(),
                selectedRoom.getId(),
                checkInDate,
                checkOutDate,
                paymentMethod,
                "pending",
                totalPrice
        );


        boolean success = ReservationDAO.saveReservation(reservation);
        if (success) {
            System.out.println("✅ Reservasi berhasil! Mengalihkan ke menu pembayaran...");
            navigateToPayment(reservation);
        } else {
            showAlert("Error", "Terjadi kesalahan saat menyimpan pemesanan.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
