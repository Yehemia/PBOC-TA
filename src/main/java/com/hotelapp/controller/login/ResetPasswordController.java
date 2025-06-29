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

public class ResetPasswordController {

    @FXML private Label infoLabel;
    @FXML private TextField tokenField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button resetPasswordButton;

    private User user;

    public void initData(User user) {
        this.user = user;
        infoLabel.setText("Kode verifikasi telah dikirim ke: " + user.getEmail());
    }

    @FXML
    void handleResetPassword(ActionEvent event) {
        String token = tokenField.getText().trim();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

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

        boolean isTokenValid = VerificationService.verifyToken(user.getId(), token);

        if (isTokenValid) {
            String hashedPassword = PasswordUtil.hashPassword(newPassword);

            try {
                if (UserDAO.updatePassword(user.getId(), hashedPassword)) {
                    VerificationService.markAllTokensUsed(user.getId());
                    AlertHelper.showInformation("Sukses", "Password Anda telah berhasil diubah. Silakan login kembali.");

                    try {
                        Parent root = FXMLLoader.load(getClass().getResource("/com/hotelapp/fxml/login.fxml"));
                        Stage stage = (Stage) resetPasswordButton.getScene().getWindow();
                        stage.setScene(new Scene(root));
                    } catch (IOException e) {
                        AlertHelper.showError("Gagal Navigasi", "Gagal kembali ke halaman login.");
                        System.err.println("Failed to load login.fxml after password reset: " + e.getMessage());
                    }
                } else {
                    AlertHelper.showError("Gagal", "Terjadi kesalahan saat mengupdate password.");
                }
            } catch (Exception e) {
                AlertHelper.showError("Kesalahan Database", "Gagal menyimpan password karena masalah pada server.");
                System.err.println("Error updating password: " + e.getMessage());
            }

        } else {
            AlertHelper.showError("Kode Salah", "Kode verifikasi yang Anda masukkan salah atau sudah kedaluwarsa.");
        }
    }
}