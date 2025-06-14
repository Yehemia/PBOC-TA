package com.hotelapp.controller.login;

import com.hotelapp.dao.UserDAO;
import com.hotelapp.model.User;
import com.hotelapp.service.VerificationService;
import com.hotelapp.util.EmailUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert; // <-- IMPORT BARU
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL; // <-- IMPORT BARU
import java.sql.SQLException;

public class RegisterController {
    @FXML private TextField usernameField, nameField, emailField;
    @FXML private PasswordField passwordField;
    @FXML private Hyperlink loginLink;

    @FXML
    public void handleRegister(ActionEvent event) {
        String username = usernameField.getText();
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        // VALIDASI BARU: Cek apakah ada field yang kosong atau hanya berisi spasi
        if (username.isBlank() || name.isBlank() || email.isBlank() || password.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Form Tidak Lengkap", "Semua field wajib diisi!");
            return; // Hentikan eksekusi jika ada field yang kosong
        }

        // Registrasi user dengan role "PENDING"
        boolean success = UserDAO.registerUser(name, email, username, password, "PENDING");

        if (success) {
            User user = UserDAO.getUserByUsername(username);
            if (user != null) {
                try {
                    String token = VerificationService.createAndSaveToken(user.getId());
                    new Thread(() -> EmailUtil.sendVerificationEmail(user.getEmail(), token)).start();
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Kesalahan Database", "Gagal membuat token verifikasi.");
                }
                showVerificationDialog(user);
            }
        } else {
            // DIUBAH: Tampilkan alert jika registrasi gagal
            showAlert(Alert.AlertType.ERROR, "Registrasi Gagal", "Username atau Email mungkin sudah terdaftar. Silakan gunakan yang lain.");
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
            // Inisialisasi owner stage agar dialog modal muncul di atas jendela utama
            dialogStage.initOwner((Stage) usernameField.getScene().getWindow());

            Scene scene = new Scene(root);
            // Terapkan CSS ke dialog verifikasi
            URL cssUrl = getClass().getResource("/styles/verify-style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            dialogStage.setScene(scene);
            dialogStage.setResizable(false); // Dialog verifikasi sebaiknya tidak bisa di-resize
            dialogStage.showAndWait(); // Gunakan showAndWait agar jendela utama tidak bisa diklik

            // Setelah dialog ditutup, kembali ke halaman login
            handleShowLogin(null);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleShowLogin(ActionEvent event) {
        try {
            // Ambil stage saat ini dari komponen mana pun
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

    // Metode bantuan untuk menampilkan alert
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}