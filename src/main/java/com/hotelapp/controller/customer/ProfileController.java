package com.hotelapp.controller.customer;

import com.hotelapp.dao.UserDAO;
import com.hotelapp.model.User;
import com.hotelapp.util.AlertHelper;
import com.hotelapp.util.Session;
import com.hotelapp.util.ValidationUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class ProfileController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private Button updateButton;

    @FXML
    public void initialize() {
        User currentUser = Session.getInstance().getCurrentUser();
        if (currentUser != null) {
            nameField.setText(currentUser.getName());
            emailField.setText(currentUser.getEmail());
            usernameField.setText(currentUser.getUsername());
        }

        updateButton.setOnAction(event -> updateProfile());
    }

    public void updateProfile() {
        User currentUser = Session.getInstance().getCurrentUser();
        if (currentUser != null) {
            String oldUsername = currentUser.getUsername();
            String newUsername = usernameField.getText().trim();

            String newEmail = emailField.getText().trim();
            if (newEmail.isBlank() || !ValidationUtil.isEmailValid(newEmail)) {
                AlertHelper.showWarning("Input Tidak Valid", "Alamat email tidak boleh kosong dan harus dalam format yang benar.");
                return;
            }

            currentUser.setName(nameField.getText().trim());
            currentUser.setEmail(newEmail);
            currentUser.setUsername(newUsername);

            try {
                boolean updated = com.hotelapp.dao.UserDAO.updateUser(currentUser);
                if (updated) {
                    AlertHelper.showInformation("Sukses", "Profil Anda berhasil diperbarui!");
                } else {
                    currentUser.setUsername(oldUsername);
                    currentUser.setEmail(UserDAO.getUserById(currentUser.getId()).getEmail());
                    AlertHelper.showError("Gagal", "Gagal memperbarui profil. Username atau email mungkin sudah digunakan.");
                }
            } catch (Exception e) {
                currentUser.setUsername(oldUsername);
                // Kembalikan juga email lama jika gagal
                currentUser.setEmail(UserDAO.getUserById(currentUser.getId()).getEmail());
                AlertHelper.showError("Kesalahan Sistem", "Terjadi masalah saat mencoba memperbarui profil Anda.");
                System.err.println("Error updating profile: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleChangePassword(ActionEvent event) {
        try {
            URL fxmlUrl = getClass().getResource("/com/hotelapp/fxml/customer/ChangePassword.fxml");
            if (fxmlUrl == null) {
                System.err.println("Cannot find FXML file for change password dialog.");
                return;
            }
            Parent root = FXMLLoader.load(fxmlUrl);
            Stage stage = new Stage();
            stage.setTitle("Ubah Password");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}