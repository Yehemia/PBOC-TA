package com.hotelapp.controller.customer;

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
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class PaymentController {

    @FXML private Label roomTypeLabel, datesLabel, nightsLabel, totalPriceLabel, timerLabel;
    @FXML private ImageView qrImageView;
    @FXML private Button confirmPaymentButton;

    private Reservation reservation;
    private Room room;
    private User customer;

    private Timer timer;
    private final AtomicInteger timeSeconds = new AtomicInteger(10 * 60);
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM uuuu", new Locale("id", "ID"));
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
        if (timer != null) {
            timer.cancel();
        }
        timeSeconds.set(10 * 60);
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                int currentSeconds = timeSeconds.decrementAndGet();
                Platform.runLater(() -> {
                    if (currentSeconds > 0) {
                        int minutes = currentSeconds / 60;
                        int seconds = currentSeconds % 60;
                        timerLabel.setText(String.format("Selesaikan dalam %02d:%02d", minutes, seconds));
                    } else {
                        timerLabel.setText("Waktu Habis!");
                        confirmPaymentButton.setDisable(true);
                        stopTimer();
                        AlertHelper.showWarning("Waktu Habis", "Waktu pembayaran telah habis.");
                    }
                });
            }
        }, 1000, 1000);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @FXML
    private void processPayment() {
        stopTimer();

        confirmPaymentButton.setDisable(true);
        confirmPaymentButton.setText("Memproses...");

        Task<Void> paymentTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                reservationService.confirmPayment(reservation.getId());
                String pdfPath = PDFGenerator.generateInvoice(reservation);
                if (pdfPath != null && customer != null && customer.getEmail() != null) {
                    EmailUtil.sendInvoiceEmailWithAttachment(customer.getEmail(), pdfPath);
                }
                return null;
            }
        };

        paymentTask.setOnSucceeded(e -> {
            closeWindow();
            showSuccessDialog();
        });

        paymentTask.setOnFailed(e -> {
            confirmPaymentButton.setDisable(false);
            confirmPaymentButton.setText("Saya Sudah Bayar");
            AlertHelper.showError("Proses Gagal", "Terjadi kesalahan saat memproses pembayaran.");
            paymentTask.getException().printStackTrace();
            startTimer();
        });

        new Thread(paymentTask).start();
    }

    private void showSuccessDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelapp/fxml/customer/PaymentSuccessDialog.fxml"));
            Parent root = loader.load();
            PaymentSuccessDialogController controller = loader.getController();
            if (customer != null) {
                controller.setMessage(customer.getEmail());
            }
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) confirmPaymentButton.getScene().getWindow();
        if (stage != null) {
            stopTimer();
            stage.close();
        }
    }
}