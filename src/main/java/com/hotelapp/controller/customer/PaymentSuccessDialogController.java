package com.hotelapp.controller.customer;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Controller untuk dialog yang muncul setelah pembayaran berhasil.
 */
public class PaymentSuccessDialogController {

    @FXML private Label messageLabel; // Label untuk menampilkan pesan konfirmasi.
    @FXML private Button doneButton; // Tombol untuk menutup dialog.

    /**
     * Inisialisasi awal.
     */
    @FXML
    public void initialize() {
        // Atur aksi untuk tombol "Selesai".
        doneButton.setOnAction(e -> {
            // Dapatkan stage dari tombol dan tutup.
            Stage stage = (Stage) doneButton.getScene().getWindow();
            stage.close();
        });
    }

    /**
     * Mengatur pesan yang akan ditampilkan di dialog.
     * @param userEmail Email pengguna untuk ditampilkan dalam pesan.
     */
    public void setMessage(String userEmail) {
        String fullMessage = "Detail pemesanan Anda, termasuk informasi kamar dan tanggal menginap, telah dikirim ke " + userEmail + ".\n" +
                "Anda juga dapat melihat rincian pemesanan di halaman \"Status Reservasi\" kapan saja.";
        messageLabel.setText(fullMessage);
    }
}