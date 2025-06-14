package com.hotelapp.controller.customer;

import com.hotelapp.model.User;
import com.hotelapp.util.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class ProfileController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private Button updateButton;

    // Jika perlu, tambahkan mekanisme notifikasi (misalnya, label untuk pesan) atau alert
    @FXML
    public void initialize() {
        // Ambil user dari sesi
        User currentUser = Session.getInstance().getCurrentUser();
        if (currentUser != null) {
            nameField.setText(currentUser.getName());
            emailField.setText(currentUser.getEmail());
            usernameField.setText(currentUser.getUsername());
        }

        // Set aksi untuk tombol update
        updateButton.setOnAction(event -> updateProfile());
    }

    private void updateProfile() {
        User currentUser = Session.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Ambil nilai dari field dan update objek user
            currentUser.setName(nameField.getText());
            currentUser.setEmail(emailField.getText());
            currentUser.setUsername(usernameField.getText());

            // Lakukan update ke database melalui DAO atau service
            boolean updated = com.hotelapp.dao.UserDAO.updateUser(currentUser);
            if (updated) {
                System.out.println("Profile berhasil diperbarui!");
            } else {
                System.err.println("Gagal memperbarui profile.");
            }
        }
    }
}