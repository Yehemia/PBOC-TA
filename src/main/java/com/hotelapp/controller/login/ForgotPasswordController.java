package com.hotelapp.controller.login;

import com.hotelapp.dao.UserDAO;
import com.hotelapp.model.User;
import com.hotelapp.service.VerificationService;
import com.hotelapp.util.AlertHelper;
import com.hotelapp.util.EmailUtil;
import com.hotelapp.util.ValidationUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Ini adalah "otak" dari langkah pertama proses lupa password.
 * Tugasnya adalah mengambil email pengguna dan mengirimkan kode reset.
 */
public class ForgotPasswordController {

    // Variabel yang terhubung ke komponen desain.
    @FXML private TextField emailField; // Kotak untuk memasukkan email.
    @FXML private Button sendCodeButton; // Tombol untuk mengirim kode.
    @FXML private Hyperlink backToLoginLink; // Link untuk kembali ke halaman login.

    /**
     * Fungsi ini berjalan saat tombol "Send Code" ditekan.
     * @param event Informasi tentang tombol yang ditekan.
     */
    @FXML
    void handleSendCode(ActionEvent event) {
        // Ambil email dari kotak input.
        String email = emailField.getText().trim();
        // Cek apakah email kosong atau formatnya salah.
        if (email.isEmpty() || !ValidationUtil.isEmailValid(email)) {
            AlertHelper.showWarning("Email Tidak Valid", "Harap masukkan alamat email yang valid.");
            return;
        }

        // Cari pengguna di database berdasarkan email.
        User user = UserDAO.getUserByEmail(email);

        // Penting: Demi keamanan, kita tidak memberi tahu penyerang apakah sebuah email terdaftar atau tidak.
        // Jadi, meskipun email tidak ditemukan atau akunnya tidak aktif, kita tetap menampilkan pesan yang sama.
        if (user == null || !"active".equalsIgnoreCase(user.getAccountStatus())) {
            AlertHelper.showInformation("Informasi", "Jika email terdaftar dan aktif, instruksi reset password akan dikirimkan.");
            return;
        }

        // Jika email ditemukan dan akunnya aktif...
        try {
            // Buat kode verifikasi (token) baru.
            String token = VerificationService.createAndSaveToken(user.getId());
            // Kirim email berisi kode tersebut.
            new Thread(() -> EmailUtil.sendPasswordResetEmail(user.getEmail(), token)).start();
            // Pindahkan pengguna ke halaman berikutnya untuk memasukkan kode dan password baru.
            navigateToResetPassword(user);
        } catch (SQLException e) {
            AlertHelper.showError("Kesalahan Teknis", "Gagal membuat kode verifikasi karena masalah pada server.");
            System.err.println("Database Error on token creation: " + e.getMessage());
        }
    }

    /**
     * Membuka halaman untuk memasukkan kode dan password baru.
     * @param user Pengguna yang sedang melakukan reset password.
     */
    private void navigateToResetPassword(User user) {
        try {
            // Muat desain untuk halaman ResetPassword.fxml.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelapp/fxml/ResetPassword.fxml"));
            Parent root = loader.load();

            // Dapatkan controller dari halaman tersebut.
            ResetPasswordController controller = loader.getController();
            // Kirim data pengguna ke controller tersebut.
            controller.initData(user);

            // Ganti isi jendela saat ini dengan halaman reset password.
            Stage stage = (Stage) sendCodeButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fungsi ini berjalan saat link "Back to Login" ditekan.
     * @param event Informasi tentang link yang ditekan.
     */
    @FXML
    void handleBackToLogin(ActionEvent event) {
        try {
            // Muat desain halaman login dan kembali ke sana.
            Parent root = FXMLLoader.load(getClass().getResource("/com/hotelapp/fxml/login.fxml"));
            Stage stage = (Stage) backToLoginLink.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}