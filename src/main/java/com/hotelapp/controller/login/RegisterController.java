package com.hotelapp.controller.login;

import com.hotelapp.dao.UserDAO;
import com.hotelapp.model.User;
import com.hotelapp.service.VerificationService;
import com.hotelapp.util.AlertHelper;
import com.hotelapp.util.EmailUtil;
import com.hotelapp.util.ValidationUtil; // <-- Import ValidationUtil
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
import java.net.URL;
import java.sql.SQLException;

public class RegisterController {
    @FXML private TextField usernameField, nameField, emailField;
    @FXML private PasswordField passwordField;
    @FXML private Hyperlink loginLink;


    @FXML
    public void initialize() {
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.trim().isEmpty()) {
                if (ValidationUtil.isEmailValid(newValue)) {
                    emailField.setStyle("");
                } else {
                    emailField.setStyle("-fx-border-color: red; -fx-border-width: 1.5px; -fx-border-radius: 10px;");
                }
            } else {
                emailField.setStyle("");
            }
        });
    }

    @FXML
    public void handleRegister(ActionEvent event) {
        String username = usernameField.getText();
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        if (username.isBlank() || name.isBlank() || email.isBlank() || password.isBlank()) {
            AlertHelper.showWarning( "Form Tidak Lengkap", "Semua field wajib diisi!");
            return;
        }

        // Cek validasi terakhir sebelum submit
        if (!ValidationUtil.isEmailValid(email)) {
            AlertHelper.showWarning("Format Email Salah", "Silakan masukkan alamat email yang valid.");
            return;
        }

        boolean success = UserDAO.registerUser(username, name, email, password, "PENDING");

        if (success) {
            User user = UserDAO.getUserByUsername(username);
            if (user != null) {
                try {
                    String token = VerificationService.createAndSaveToken(user.getId());
                    new Thread(() -> EmailUtil.sendVerificationEmail(user.getEmail(), token)).start();
                } catch (SQLException e) {
                    e.printStackTrace();
                    AlertHelper.showError("Kesalahan Database", "Gagal membuat token verifikasi.");
                }
                showVerificationDialog(user);
            }
        } else {
            AlertHelper.showError("Registrasi Gagal", "Username atau Email mungkin sudah terdaftar. Silakan gunakan yang lain.");
        }
    }

    private void showVerificationDialog(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelapp/fxml/verify.fxml"));
            Parent root = loader.load();
            VerifyController vc = loader.getController();
            vc.setUser(user);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Verifikasi Akun");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner((Stage) usernameField.getScene().getWindow());

            Scene scene = new Scene(root);

            URL cssUrl = getClass().getResource("/styles/verify-style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            dialogStage.showAndWait();
            handleShowLogin(null);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleShowLogin(ActionEvent event) {
        try {
            Stage stage = (Stage) (event != null ? ((Hyperlink) event.getSource()).getScene().getWindow() : usernameField.getScene().getWindow());
            Parent root = FXMLLoader.load(getClass().getResource("/com/hotelapp/fxml/login.fxml"));
            Scene newScene = new Scene(root, stage.getWidth(), stage.getHeight());

            URL cssUrl = getClass().getResource("/styles/login-style.css");
            if (cssUrl != null) {
                newScene.getStylesheets().add(cssUrl.toExternalForm());
            }
            stage.setScene(newScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}