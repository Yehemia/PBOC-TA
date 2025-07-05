package com.hotelapp.controller.login;

import com.hotelapp.dao.UserDAO;
import com.hotelapp.model.User;
import com.hotelapp.service.VerificationService;
import com.hotelapp.util.AlertHelper;
import com.hotelapp.util.PasswordUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Ini adalah "otak" dari halaman akhir proses lupa password.
 * Di sini pengguna memasukkan kode verifikasi dan password baru.
 */
public class ResetPasswordController {

    // Variabel yang terhubung ke komponen desain.
    @FXML private Label infoLabel; // Label untuk info (misal: "Kode dikirim ke email...").
    @FXML private TextField tokenField; // Kotak untuk memasukkan kode verifikasi.
    @FXML private PasswordField newPasswordField; // Kotak untuk password baru.
    @FXML private PasswordField confirmPasswordField; // Kotak untuk konfirmasi password baru.
    @FXML private Button resetPasswordButton; // Tombol untuk menyelesaikan proses.

    private User user; // Pengguna yang passwordnya akan di-reset.

    /**
     * Menerima data pengguna dari halaman sebelumnya.
     * @param user Pengguna yang sedang dalam proses reset.
     */
    public void initData(User user) {
        this.user = user;
        // Tampilkan pesan informasi yang menyertakan email pengguna (disamarkan sebagian jika perlu).
        infoLabel.setText("Kode verifikasi telah dikirim ke: " + user.getEmail());
    }

    /**
     * Fungsi ini berjalan saat tombol "Reset Password" ditekan.
     * @param event Informasi tentang tombol yang ditekan.
     */
    @FXML
    void handleResetPassword(ActionEvent event) {
        // Ambil semua data dari form.
        String token = tokenField.getText().trim();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Lakukan validasi dasar.
        if (token.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            AlertHelper.showWarning("Input Tidak Lengkap", "Semua field wajib diisi.");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            AlertHelper.showError("Password Tidak Cocok", "Password baru dan konfirmasi tidak sama.");
            return;
        }
        if (token.length() != 6) {
            AlertHelper.showWarning("Kode Tidak Valid", "Kode verifikasi harus 6 digit.");
            return;
        }

        // Periksa apakah kode yang dimasukkan benar dan belum kedaluwarsa.
        boolean isTokenValid = VerificationService.verifyToken(user.getId(), token);

        if (isTokenValid) {
            // Jika kode benar:
            // 1. "Hash" password baru. Hashing adalah proses mengubah password menjadi string acak
            //    yang tidak bisa dikembalikan, ini adalah cara aman menyimpan password.
            String hashedPassword = PasswordUtil.hashPassword(newPassword);

            try {
                // 2. Update password di database dengan password yang sudah di-hash.
                if (UserDAO.updatePassword(user.getId(), hashedPassword)) {
                    // 3. Tandai semua token lama sebagai sudah terpakai.
                    VerificationService.markAllTokensUsed(user.getId());
                    AlertHelper.showInformation("Sukses", "Password Anda telah berhasil diubah. Silakan login kembali.");

                    // 4. Arahkan pengguna kembali ke halaman login.
                    try {
                        Parent root = FXMLLoader.load(getClass().getResource("/com/hotelapp/fxml/login.fxml"));
                        Stage stage = (Stage) resetPasswordButton.getScene().getWindow();
                        stage.setScene(new Scene(root));
                    } catch (IOException e) {
                        AlertHelper.showError("Gagal Navigasi", "Gagal kembali ke halaman login.");
                        System.err.println("Gagal memuat login.fxml setelah reset password: " + e.getMessage());
                    }
                } else {
                    AlertHelper.showError("Gagal", "Terjadi kesalahan saat mengupdate password.");
                }
            } catch (Exception e) {
                AlertHelper.showError("Kesalahan Database", "Gagal menyimpan password karena masalah pada server.");
                System.err.println("Error updating password: " + e.getMessage());
            }

        } else {
            // Jika kode salah atau sudah kedaluwarsa.
            AlertHelper.showError("Kode Salah", "Kode verifikasi yang Anda masukkan salah atau sudah kedaluwarsa.");
        }
    }
}