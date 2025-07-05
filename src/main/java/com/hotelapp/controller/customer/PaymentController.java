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
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controller untuk halaman pembayaran.
 * Menampilkan detail, QR code, dan timer untuk pembayaran.
 */
public class PaymentController {

    // @FXML menghubungkan variabel ke komponen FXML.
    @FXML private Label roomTypeLabel, datesLabel, nightsLabel, totalPriceLabel, timerLabel;
    @FXML private ImageView qrImageView;
    @FXML private Button confirmPaymentButton;

    private Reservation reservation; // Data reservasi yang akan dibayar.
    private Room room; // Data kamar dari reservasi.
    private User customer; // Data pelanggan yang melakukan reservasi.

    private Timer timer; // Objek Timer untuk countdown.
    private final AtomicInteger timeSeconds = new AtomicInteger(10 * 60); // Waktu 10 menit dalam detik.
    private final ReservationService reservationService = new ReservationService();

    /**
     * Menerima data reservasi dari halaman sebelumnya dan memulai proses.
     * @param reservation Objek reservasi.
     */
    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
        this.room = RoomDAO.getRoomById(reservation.getRoomId());
        this.customer = Session.getInstance().getCurrentUser();
        updateUI(); // Tampilkan detail di UI.
        startTimer(); // Mulai countdown.
    }

    /**
     * Mengisi semua komponen UI dengan data dari reservasi.
     */
    private void updateUI() {
        if (reservation == null || room == null) return;

        // Tampilkan info tipe kamar, tanggal, dan durasi menginap.
        roomTypeLabel.setText(room.getRoomType().getName());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM uuuu", new Locale("id", "ID"));
        String checkIn = reservation.getCheckIn().format(formatter);
        String checkOut = reservation.getCheckOut().format(formatter);
        datesLabel.setText(checkIn + " - " + checkOut);
        long nights = ChronoUnit.DAYS.between(reservation.getCheckIn(), reservation.getCheckOut());
        nightsLabel.setText(nights + " malam");

        // Tampilkan total harga dalam format mata uang.
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        totalPriceLabel.setText(currencyFormat.format(reservation.getTotalPrice()));

        // Buat konten untuk QR code dan generate gambarnya.
        String qrContent = "Booking ID: " + reservation.getBookingCode() + " | Total: " + reservation.getTotalPrice();
        Image qrImage = QRCodeGenerator.generateQRCode(qrContent, 200, 200);
        qrImageView.setImage(qrImage);
    }

    /**
     * Memulai timer countdown untuk pembayaran.
     */
    private void startTimer() {
        if (timer != null) timer.cancel(); // Hentikan timer lama jika ada.
        timeSeconds.set(10 * 60); // Reset waktu ke 10 menit.
        timer = new Timer(true); // 'true' agar thread timer menjadi daemon.

        // Jadwalkan tugas yang akan dijalankan setiap 1 detik.
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                int currentSeconds = timeSeconds.decrementAndGet(); // Kurangi waktu 1 detik.
                // Update UI di JavaFX Application Thread.
                Platform.runLater(() -> {
                    if (currentSeconds > 0) {
                        int minutes = currentSeconds / 60;
                        int seconds = currentSeconds % 60;
                        timerLabel.setText(String.format("Selesaikan dalam %02d:%02d", minutes, seconds));
                    } else {
                        // Jika waktu habis.
                        timerLabel.setText("Waktu Habis!");
                        confirmPaymentButton.setDisable(true);
                        stopTimer();
                        handlePaymentTimeout(); // Panggil metode untuk menangani timeout.
                    }
                });
            }
        }, 1000, 1000); // Mulai setelah 1 detik, ulangi setiap 1 detik.
    }

    /**
     * Menangani kejadian ketika waktu pembayaran habis.
     * Reservasi akan dibatalkan secara otomatis.
     */
    private void handlePaymentTimeout() {
        try {
            reservationService.cancelReservation(reservation);
            AlertHelper.showWarning("Waktu Habis", "Waktu pembayaran telah habis. Pesanan Anda telah dibatalkan secara otomatis.");
            closeWindow();
        } catch (SQLException e) {
            AlertHelper.showError("Error", "Gagal membatalkan pesanan secara otomatis setelah waktu habis.");
            System.err.println("Failed to auto-cancel reservation: " + e.getMessage());
        }
    }

    /**
     * Menghentikan timer.
     */
    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /**
     * Memproses pembayaran saat tombol "Saya Sudah Bayar" diklik.
     */
    @FXML
    private void processPayment() {
        stopTimer();
        confirmPaymentButton.setDisable(true);
        confirmPaymentButton.setText("Memproses...");

        // Gunakan Task untuk menjalankan proses konfirmasi pembayaran di background thread.
        Task<String> paymentTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                // Konfirmasi pembayaran di database.
                reservationService.confirmPayment(reservation.getId());
                // Generate invoice PDF.
                String pdfPath = PDFGenerator.generateInvoice(reservation);
                // Jika PDF berhasil dibuat dan customer punya email, kirim email.
                if (pdfPath != null && customer != null && customer.getEmail() != null) {
                    EmailUtil.sendInvoiceEmailWithAttachment(customer.getEmail(), pdfPath);
                }
                return pdfPath;
            }
        };

        // Setelah task berhasil...
        paymentTask.setOnSucceeded(e -> {
            closeWindow();
            showSuccessDialog(); // Tampilkan dialog sukses.
        });

        // Jika task gagal...
        paymentTask.setOnFailed(e -> {
            confirmPaymentButton.setDisable(false);
            confirmPaymentButton.setText("Saya Sudah Bayar");
            Throwable ex = paymentTask.getException();
            AlertHelper.showError("Proses Gagal", "Terjadi kesalahan saat memproses pembayaran.");
            System.err.println("Error during payment processing: " + ex.getMessage());
            startTimer(); // Mulai ulang timer jika gagal.
        });

        new Thread(paymentTask).start();
    }

    /**
     * Menampilkan dialog konfirmasi pembayaran sukses.
     */
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
            dialogStage.initStyle(StageStyle.TRANSPARENT); // Dialog tanpa border default.
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT); // Latar belakang transparan.
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metode pembantu untuk menutup jendela pembayaran.
     */
    private void closeWindow() {
        Stage stage = (Stage) confirmPaymentButton.getScene().getWindow();
        if (stage != null) {
            stopTimer();
            stage.close();
        }
    }
}