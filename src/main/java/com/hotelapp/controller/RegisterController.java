package com.hotelapp.controller;

import com.hotelapp.dao.UserDAO;

import com.hotelapp.model.User;
import com.hotelapp.service.VerificationService;
import com.hotelapp.util.EmailUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

public class RegisterController {
    @FXML private TextField usernameField, nameField, emailField;
    @FXML private PasswordField passwordField;
    @FXML private Hyperlink loginLink;

    @FXML
    public void handleRegister(ActionEvent event) {
        // Registrasi user dengan role "PENDING"
        boolean success = UserDAO.registerUser(
                usernameField.getText(),
                nameField.getText(),
                emailField.getText(),
                passwordField.getText(),
                "PENDING"
        );
        if (success) {
            // Ambil data user yang baru didaftarkan
            User user = UserDAO.getUserByUsername(usernameField.getText());
            if (user != null) {
                try {
                    // Generate token verifikasi
                    String token = UUID.randomUUID().toString().replace("-", "");
                    VerificationService.createAndSaveToken(user.getId(), token);
                    // Kirim email secara asynchronous
                    new Thread(() -> EmailUtil.sendVerificationEmail(user.getEmail(), token)).start();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                // Tampilkan dialog verifikasi
                showVerificationDialog(user);
            }
        } else {
            System.out.println("Registrasi gagal! Username atau Email mungkin sudah digunakan.");
        }
    }

    private void showVerificationDialog(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelapp/fxml/verify.fxml"));
            Parent root = loader.load();
            VerifyController vc = loader.getController();
            vc.setUser(user);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Verifikasi Kode");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleShowLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/hotelapp/fxml/login.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
