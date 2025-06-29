package com.hotelapp.controller.customer;

import com.hotelapp.model.Reservation;
import com.hotelapp.model.RoomType;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;


public class BookingController {

    @FXML private Label roomInfoLabel;
    @FXML private DatePicker checkInPicker;
    @FXML private DatePicker checkOutPicker;
    @FXML private ComboBox<String> paymentMethodComboBox;
    @FXML private Button confirmBookingButton;

    private RoomType selectedRoomType;
    private final ReservationService reservationService = new ReservationService();

    @FXML
    public void initialize() {
        paymentMethodComboBox.setItems(FXCollections.observableArrayList("online"));
        setupValidationAndDatePickers();
    }

    public void setRoomType(RoomType roomType) {
        this.selectedRoomType = roomType;
        roomInfoLabel.setText("Pemesanan untuk Tipe Kamar: " + roomType.getName());
    }

    private void setupValidationAndDatePickers() {
        final LocalDate today = LocalDate.now();

        checkInPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(today));
            }
        });

        checkOutPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate checkInDate = checkInPicker.getValue();
                if (checkInDate == null) {
                    setDisable(empty || date.isBefore(today.plusDays(1)));
                } else {
                    setDisable(empty || !date.isAfter(checkInDate));
                }
            }
        });

        checkInPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && checkOutPicker.getValue() != null && !checkOutPicker.getValue().isAfter(newVal)) {
                checkOutPicker.setValue(null);
            }
        });

        BooleanBinding isInvalid = Bindings.createBooleanBinding(() ->
                        checkInPicker.getValue() == null ||
                                checkOutPicker.getValue() == null ||
                                paymentMethodComboBox.getValue() == null ||
                                !checkOutPicker.getValue().isAfter(checkInPicker.getValue()),
                checkInPicker.valueProperty(), checkOutPicker.valueProperty(), paymentMethodComboBox.valueProperty()
        );
        confirmBookingButton.disableProperty().bind(isInvalid);
    }

    @FXML
    public void processBooking() {
        User currentUser = Session.getInstance().getCurrentUser();
        if (currentUser == null) {
            AlertHelper.showError("Login Dibutuhkan", "Anda harus login untuk melakukan pemesanan.");
            return;
        }

        try {
            Reservation newReservation = reservationService.createBooking(
                    currentUser, selectedRoomType, checkInPicker.getValue(),
                    checkOutPicker.getValue(), paymentMethodComboBox.getValue()
            );
            ((Stage) confirmBookingButton.getScene().getWindow()).close();
            navigateToPayment(newReservation);

        } catch (BookingException e) {
            AlertHelper.showWarning("Booking Gagal", e.getMessage());

        } catch (SQLException e) {
            AlertHelper.showError(
                    "Kesalahan Teknis",
                    "Terjadi masalah saat terhubung ke server. Pastikan Anda terhubung ke internet dan coba lagi."
            );
            //System.err.println("SQL Error during booking process: " + e.getMessage());

        } catch (Exception e) {
            AlertHelper.showError(
                    "Error Tidak Terduga",
                    "Terjadi kesalahan yang tidak terduga. Silakan coba lagi atau hubungi cshotelkenangan1@gmail.com."
            );
            System.err.println("Unexpected error during booking: " + e.getMessage());
        }
    }

    private void navigateToPayment(Reservation reservation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelapp/fxml/customer/payment.fxml"));
            Parent root = loader.load();

            PaymentController paymentController = loader.getController();
            paymentController.setReservation(reservation);

            Stage paymentStage = new Stage();
            paymentStage.setTitle("Pembayaran");
            paymentStage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root);
            paymentStage.setScene(scene);
            paymentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showError("Gagal", "Tidak dapat membuka halaman pembayaran.");
        }
    }
}