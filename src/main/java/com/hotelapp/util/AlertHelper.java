package com.hotelapp.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Ini adalah kelas "pembantu" untuk menampilkan jendela peringatan (Alert) dengan lebih mudah.
 * Daripada menulis kode yang panjang untuk membuat Alert setiap kali dibutuhkan,
 * kita cukup memanggil satu fungsi dari kelas ini.
 * 'final' berarti kelas ini tidak bisa diwariskan/di-extend.
 */
public final class AlertHelper {

    /**
     * Constructor dibuat 'private' agar tidak ada yang bisa membuat objek dari kelas ini.
     * Ini adalah praktik umum untuk kelas yang isinya hanya fungsi-fungsi statis (static).
     */
    private AlertHelper() {}

    /**
     * Menampilkan dialog informasi sederhana (ikon 'i').
     * @param title Judul jendela dialog.
     * @param message Pesan yang ingin ditampilkan.
     */
    public static void showInformation(String title, String message) {
        // Buat objek Alert dengan tipe INFORMATION.
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null); // Header dikosongkan agar lebih simpel.
        alert.setContentText(message);
        alert.showAndWait(); // Tampilkan dialog dan tunggu sampai pengguna menutupnya.
    }

    /**
     * Menampilkan dialog peringatan (ikon segitiga kuning).
     * @param title Judul jendela dialog.
     * @param message Pesan peringatan.
     */
    public static void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Menampilkan dialog error (ikon silang merah).
     * @param title Judul jendela dialog.
     * @param message Pesan error.
     */
    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Menampilkan dialog konfirmasi yang meminta pilihan dari pengguna (misal: OK/Cancel, Yes/No).
     * @param title Judul jendela dialog.
     * @param message Pertanyaan konfirmasi.
     * @param buttons Tombol-tombol pilihan yang akan ditampilkan (misal: ButtonType.OK, ButtonType.CANCEL).
     * @return Sebuah Optional yang berisi tombol mana yang diklik oleh pengguna.
     */
    public static Optional<ButtonType> showConfirmation(String title, String message, ButtonType... buttons) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        // Atur tombol-tombol yang akan muncul di dialog.
        alert.getButtonTypes().setAll(buttons);
        // Kembalikan pilihan pengguna.
        return alert.showAndWait();
    }
}