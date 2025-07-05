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

/**
 * Controller untuk halaman profil pelanggan.
 * Memungkinkan pengguna untuk mengupdate data diri dan mengubah password.
 */
public class ProfileController {

    // @FXML menghubungkan variabel ke komponen FXML.
    @FXML private TextField nameField; // Input untuk nama.
    @FXML private TextField emailField; // Input untuk email.
    @FXML private TextField usernameField; // Input untuk username.
    @FXML private Button updateButton; // Tombol untuk update profil.

    /**
     * Inisialisasi awal saat halaman profil dimuat.
     */
    @FXML
    public void initialize() {
        // Ambil data pengguna dari sesi.
        User currentUser = Session.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Isi field dengan data pengguna saat ini.
            nameField.setText(currentUser.getName());
            emailField.setText(currentUser.getEmail());
            usernameField.setText(currentUser.getUsername());
        }

        // Atur aksi untuk tombol update.
        updateButton.setOnAction(event -> updateProfile());
    }

    /**
     * Memproses pembaruan data profil.
     */
    public void updateProfile() {
        User currentUser = Session.getInstance().getCurrentUser();
        if (currentUser != null) {
            String oldUsername = currentUser.getUsername();
            String newUsername = usernameField.getText().trim();
            String newEmail = emailField.getText().trim();

            // Validasi format email.
            if (newEmail.isBlank() || !ValidationUtil.isEmailValid(newEmail)) {
                AlertHelper.showWarning("Input Tidak Valid", "Alamat email tidak boleh kosong dan harus dalam format yang benar.");
                return;
            }

            // Update objek User di memori (sesi).
            currentUser.setName(nameField.getText().trim());
            currentUser.setEmail(newEmail);
            currentUser.setUsername(newUsername);

            try {
                // Coba update data di database.
                boolean updated = com.hotelapp.dao.UserDAO.updateUser(currentUser);
                if (updated) {
                    AlertHelper.showInformation("Sukses", "Profil Anda berhasil diperbarui!");
                } else {
                    // Jika gagal (misal: username/email sudah ada), kembalikan data lama di sesi.
                    currentUser.setUsername(oldUsername);
                    currentUser.setEmail(UserDAO.getUserById(currentUser.getId()).getEmail());
                    AlertHelper.showError("Gagal", "Gagal memperbarui profil. Username atau email mungkin sudah digunakan.");
                }
            } catch (Exception e) {
                // Jika terjadi error sistem, kembalikan juga data lama.
                currentUser.setUsername(oldUsername);
                currentUser.setEmail(UserDAO.getUserById(currentUser.getId()).getEmail());
                AlertHelper.showError("Kesalahan Sistem", "Terjadi masalah saat mencoba memperbarui profil Anda.");
                System.err.println("Error updating profile: " + e.getMessage());
            }
        }
    }

    /**
     * Menangani aksi klik tombol "Ubah Password".
     * @param event Aksi klik tombol.
     */
    @FXML
    private void handleChangePassword(ActionEvent event) {
        try {
            // Muat FXML untuk dialog ubah password.
            URL fxmlUrl = getClass().getResource("/com/hotelapp/fxml/customer/ChangePassword.fxml");
            if (fxmlUrl == null) {
                System.err.println("FXML tidak ditemukan.");
                return;
            }
            Parent root = FXMLLoader.load(fxmlUrl);
            // Tampilkan dialog sebagai jendela modal.
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