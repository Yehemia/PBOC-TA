package com.hotelapp.controller.customer;

import com.hotelapp.dao.ReservationDAO;
import com.hotelapp.model.Reservation;
import com.hotelapp.model.Room;
import com.hotelapp.model.User;
import com.hotelapp.service.BookingException;
import com.hotelapp.service.ReservationService;
import com.hotelapp.util.AlertHelper;
import com.hotelapp.util.Session;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import net.synedra.validatorfx.Validator;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BookingController {

    @FXML private DatePicker checkInPicker;
    @FXML private DatePicker checkOutPicker;
    @FXML private ComboBox<String> paymentMethodComboBox;
    @FXML private Button confirmBookingButton;
    private Room selectedRoom;
    private final Validator validator = new Validator();
    private ReservationService reservationService = new ReservationService();
    @FXML private Label roomInfoLabel;




    @FXML
    public void initialize() {
        paymentMethodComboBox.setItems(FXCollections.observableArrayList("online", "pay_later"));
        confirmBookingButton.setOnAction(event -> processBooking());
        setupValidation();
    }

    public void setRoom(Room room) {
        this.selectedRoom = room;
        roomInfoLabel.setText("untuk Kamar " + room.getRoomType().getName() + " - " + room.getRoomNumber());
    }

    private void setupValidation() {
        BooleanBinding isFormInvalid = Bindings.createBooleanBinding(() -> {
                    LocalDate checkIn = checkInPicker.getValue();
                    LocalDate checkOut = checkOutPicker.getValue();
                    String paymentMethod = paymentMethodComboBox.getValue();

                    if (checkIn == null) return true;
                    if (checkOut == null) return true;
                    if (paymentMethod == null) return true;
                    if (!checkOut.isAfter(checkIn)) return true;
                    return false;

                },

                checkInPicker.valueProperty(),
                checkOutPicker.valueProperty(),
                paymentMethodComboBox.valueProperty());

        confirmBookingButton.disableProperty().bind(isFormInvalid);
    }

    private void navigateToPayment(Reservation reservation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelapp/fxml/customer/payment.fxml"));
            Parent root = loader.load();

            PaymentController paymentController = loader.getController();
            paymentController.setReservation(reservation);

            Stage stage = (Stage) confirmBookingButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("❌ Gagal memuat halaman pembayaran: " + e.getMessage());
        }
    }

    public void processBooking() {
        LocalDate checkInDate = checkInPicker.getValue();
        LocalDate checkOutDate = checkOutPicker.getValue();
        String paymentMethod = paymentMethodComboBox.getValue();
        User currentUser = Session.getInstance().getCurrentUser();
        if (currentUser == null) {
            AlertHelper.showError("Login Dibutuhkan", "Anda harus login untuk melakukan pemesanan.");
            return;
        }

        try {
            Reservation newReservation = reservationService.createBooking(
                    currentUser, selectedRoom, checkInPicker.getValue(),
                    checkOutPicker.getValue(), paymentMethodComboBox.getValue()
            );
            navigateToPayment(newReservation);
        } catch (BookingException e) {
            AlertHelper.showWarning("Booking Gagal", e.getMessage());
        } catch (SQLException e) {
            AlertHelper.showError("Error Database", "Gagal menyimpan pemesanan.");
            e.printStackTrace();
        }
    }
}
