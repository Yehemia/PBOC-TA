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

public class ForgotPasswordController {

    @FXML private TextField emailField;
    @FXML private Button sendCodeButton;
    @FXML private Hyperlink backToLoginLink;

    @FXML
    void handleSendCode(ActionEvent event) {
        String email = emailField.getText().trim();
        if (email.isEmpty() || !ValidationUtil.isEmailValid(email)) {
            AlertHelper.showWarning("Email Tidak Valid", "Harap masukkan alamat email yang valid.");
            return;
        }

        User user = UserDAO.getUserByEmail(email);

        if (user == null || !"active".equalsIgnoreCase(user.getAccountStatus())) {
            AlertHelper.showInformation("Informasi", "Jika email terdaftar dan aktif, instruksi reset password akan dikirimkan.");
            return;
        }

        try {
            String token = VerificationService.createAndSaveToken(user.getId());
            new Thread(() -> EmailUtil.sendPasswordResetEmail(user.getEmail(), token)).start();
            navigateToResetPassword(user);
        } catch (SQLException e) {
            AlertHelper.showError("Kesalahan Teknis", "Gagal membuat kode verifikasi karena masalah pada server.");
            System.err.println("Database Error on token creation: " + e.getMessage());
        }
    }

    private void navigateToResetPassword(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelapp/fxml/ResetPassword.fxml"));
            Parent root = loader.load();

            ResetPasswordController controller = loader.getController();
            controller.initData(user);

            Stage stage = (Stage) sendCodeButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleBackToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/hotelapp/fxml/login.fxml"));
            Stage stage = (Stage) backToLoginLink.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}