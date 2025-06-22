package com.hotelapp.controller.customer;

import com.hotelapp.dao.ReservationDAO;
import com.hotelapp.dao.RoomDAO;
import com.hotelapp.model.Reservation;
import com.hotelapp.model.Room;
import com.hotelapp.model.User;
import com.hotelapp.service.ReservationService;
import com.hotelapp.util.AlertHelper;
import com.hotelapp.util.EmailUtil;
import com.hotelapp.util.PDFGenerator;
import com.hotelapp.util.QRCodeGenerator;
import com.hotelapp.util.Session;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class PaymentController {

    @FXML private Label roomTypeLabel, datesLabel, nightsLabel, totalPriceLabel, timerLabel;
    @FXML private ImageView qrImageView;
    @FXML private Button confirmPaymentButton;

    private Reservation reservation;
    private Room room;
    private User customer;

    private Timeline timeline;
    private final IntegerProperty timeSeconds = new SimpleIntegerProperty(10 * 60);
    private final ReservationService reservationService = new ReservationService();

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
        this.room = RoomDAO.getRoomById(reservation.getRoomId());
        this.customer = Session.getInstance().getCurrentUser();

        updateUI();
        startTimer();
    }

    private void updateUI() {
        if (reservation == null || room == null) return;
        roomTypeLabel.setText(room.getRoomType().getName());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", new Locale("id", "ID"));
        String checkIn = reservation.getCheckIn().format(formatter);
        String checkOut = reservation.getCheckOut().format(formatter);
        datesLabel.setText(checkIn + " - " + checkOut);

        long nights = ChronoUnit.DAYS.between(reservation.getCheckIn(), reservation.getCheckOut());
        nightsLabel.setText(nights + " malam");

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        totalPriceLabel.setText(currencyFormat.format(reservation.getTotalPrice()));

        String qrContent = "Booking ID: " + reservation.getId() + " | Total: " + reservation.getTotalPrice();
        Image qrImage = QRCodeGenerator.generateQRCode(qrContent, 200, 200);
        qrImageView.setImage(qrImage);
    }

    private void startTimer() {
        timerLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            int totalSeconds = timeSeconds.get();
            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;
            return String.format("Selesaikan dalam %02d:%02d", minutes, seconds);
        }, timeSeconds));

        if (timeline != null) {
            timeline.stop();
        }
        timeSeconds.set(10 * 60); // 10 menit
        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(1), e -> {
                    int seconds = timeSeconds.get();
                    timeSeconds.set(seconds - 1);
                    if (seconds <= 0) {
                        timeline.stop();
                        timerLabel.setText("Waktu Habis!");
                        confirmPaymentButton.setDisable(true);
                        AlertHelper.showWarning("Waktu Habis", "Waktu pembayaran telah habis. Silakan coba booking kembali.");
                    }
                })
        );
        timeline.play();
    }

    @FXML
    private void processPayment() {
        if (timeline != null) timeline.stop();

        try {
            reservationService.confirmPayment(reservation.getId());
            confirmPaymentButton.setText("Pembayaran Berhasil!");
            confirmPaymentButton.setDisable(true);

            String pdfPath = PDFGenerator.generateInvoice(reservation);
            if (pdfPath != null && customer != null && customer.getEmail() != null) {
                EmailUtil.sendInvoiceEmailWithAttachment(customer.getEmail(), pdfPath);
            }

            closeWindowAfterDelay();

        } catch (SQLException e) {
            AlertHelper.showError("Gagal", "Gagal mengupdate status pembayaran.");
            e.printStackTrace();
        }
    }

    private void closeWindowAfterDelay() {
        Timeline closeTimer = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
            Stage stage = (Stage) confirmPaymentButton.getScene().getWindow();
            stage.close();
        }));
        closeTimer.play();
    }
}