package com.hotelapp.controller.resepsionis;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

/**
 * Ini adalah "otak" untuk dialog (jendela pop-up) pembayaran tunai.
 * Tugasnya adalah menghitung kembalian dan mengonfirmasi pembayaran.
 */
public class CashPaymentController {

    // Variabel yang terhubung ke komponen desain FXML.
    @FXML private Label totalBillLabel; // Label untuk menampilkan total tagihan.
    @FXML private Label changeLabel; // Label untuk menampilkan uang kembalian.
    @FXML private TextField cashReceivedField; // Kotak untuk memasukkan uang yang diterima dari tamu.
    @FXML private Button confirmButton; // Tombol untuk konfirmasi.

    private double totalBill; // Menyimpan nilai total tagihan.
    private double cashReceived; // Menyimpan nilai uang yang diterima.
    // Optional digunakan untuk menandakan apakah user mengonfirmasi atau membatalkan.
    private Optional<Double> result = Optional.empty();
    // Formatter untuk mengubah angka menjadi format mata uang (misal: "Rp100.000").
    private final NumberFormat currencyFormatter = createCurrencyFormatter();

    /**
     * Fungsi yang berjalan otomatis saat dialog pembayaran tunai dibuka.
     */
    @FXML
    public void initialize() {
        // Awalnya, tombol konfirmasi dinonaktifkan.
        confirmButton.setDisable(true);

        // Tambahkan listener untuk memvalidasi input uang tunai.
        cashReceivedField.textProperty().addListener((obs, oldVal, newVal) -> {
            // Hanya izinkan input berupa angka.
            if (!newVal.matches("\\d*")) {
                // Jika input bukan angka, kembalikan ke nilai sebelumnya (batalkan input).
                cashReceivedField.setText(newVal.replaceAll("[^\\d]", ""));
            }
            // Setiap kali input berubah, hitung ulang kembalian.
            updateChange();
        });

        // Saat dialog terbuka, langsung fokuskan kursor ke field input uang.
        Platform.runLater(() -> cashReceivedField.requestFocus());
    }

    /**
     * Menerima dan mengatur total tagihan dari halaman sebelumnya.
     * @param totalBill Total tagihan yang harus dibayar.
     */
    public void setTotalBill(double totalBill) {
        this.totalBill = totalBill;
        // Tampilkan total tagihan dalam format mata uang.
        totalBillLabel.setText(currencyFormatter.format(totalBill));
    }

    /**
     * Menghitung dan memperbarui tampilan uang kembalian.
     */
    private void updateChange() {
        try {
            // Ubah teks input menjadi angka.
            cashReceived = Double.parseDouble(cashReceivedField.getText());
            // Jika uang yang diterima cukup atau lebih...
            if (cashReceived >= totalBill) {
                double change = cashReceived - totalBill;
                // Tampilkan kembaliannya.
                changeLabel.setText(currencyFormatter.format(change));
                // Aktifkan tombol konfirmasi.
                confirmButton.setDisable(false);
            } else {
                // Jika uang kurang, kembalian Rp0 dan tombol tetap nonaktif.
                changeLabel.setText("Rp0");
                confirmButton.setDisable(true);
            }
        } catch (NumberFormatException e) {
            // Jika input tidak bisa diubah menjadi angka (misal: kosong), anggap kembalian Rp0.
            changeLabel.setText("Rp0");
            confirmButton.setDisable(true);
        }
    }

    /**
     * Fungsi ini berjalan saat tombol "Konfirmasi" diklik.
     */
    @FXML
    private void handleConfirm() {
        // Simpan jumlah uang yang diterima ke dalam 'result'.
        this.result = Optional.of(this.cashReceived);
        // Tutup dialog.
        closeDialog();
    }

    /**
     * Fungsi ini dipanggil dari halaman sebelumnya untuk mendapatkan hasil dari dialog ini.
     * @return Optional berisi jumlah uang jika dikonfirmasi, atau Optional kosong jika dibatalkan.
     */
    public Optional<Double> getCashReceived() {
        return result;
    }

    /**
     * Fungsi pembantu untuk menutup jendela dialog.
     */
    private void closeDialog() {
        Stage stage = (Stage) confirmButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Membuat formatter mata uang untuk Rupiah (IDR).
     */
    private NumberFormat createCurrencyFormatter() {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        format.setMaximumFractionDigits(0); // Tidak menggunakan angka di belakang koma.
        return format;
    }
}