package com.hotelapp.controller.admin;

import com.hotelapp.dao.UserDAO;
import com.hotelapp.model.User;
import com.hotelapp.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 * Controller untuk dialog tambah atau edit data pengguna.
 */
public class AddUserDialogController {

    // @FXML menghubungkan variabel ke komponen FXML.
    @FXML private TextField usernameField; // Input username.
    @FXML private TextField nameField; // Input nama lengkap.
    @FXML private TextField emailField; // Input email.
    @FXML private PasswordField passwordField; // Input password.
    @FXML private ComboBox<String> roleComboBox; // Pilihan peran (role).
    @FXML private Button saveButton; // Tombol simpan.

    // Variabel untuk menyimpan user yang akan diedit. Jika null, berarti mode tambah baru.
    private User userToEdit = null;

    /**
     * Inisialisasi awal saat dialog dimuat.
     */
    @FXML
    public void initialize() {
        // Mengisi ComboBox peran dengan opsi yang tersedia.
        roleComboBox.setItems(FXCollections.observableArrayList("customer", "receptionist", "admin"));
    }

    /**
     * Mengisi form dengan data user saat mode EDIT.
     * @param user Objek user yang datanya akan ditampilkan.
     */
    public void initData(User user) {
        this.userToEdit = user;

        // Isi field dengan data yang ada.
        usernameField.setText(user.getUsername());
        nameField.setText(user.getName());
        emailField.setText(user.getEmail());
        roleComboBox.setValue(user.getRole());

        // Sembunyikan field password karena password tidak diedit di sini.
        // Password hanya di-set saat membuat user baru.
        passwordField.setPromptText("Kosongkan jika tidak ingin diubah");
        passwordField.setDisable(true);
        passwordField.setManaged(false);
        passwordField.setVisible(false);

        // Ubah teks tombol menjadi "Update User".
        saveButton.setText("Update User");
    }

    /**
     * Dijalankan saat tombol Simpan/Update diklik.
     * @param event Aksi klik tombol.
     */
    @FXML
    private void handleSave(ActionEvent event) {
        // Ambil data dari semua field.
        String username = usernameField.getText().trim();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String role = roleComboBox.getValue();

        // Validasi dasar: pastikan field utama terisi.
        if (username.isEmpty() || name.isEmpty() || email.isEmpty() || role == null) {
            AlertHelper.showWarning("Input Tidak Lengkap", "Semua field (kecuali password) wajib diisi.");
            return;
        }

        boolean success;
        // Cek apakah mode tambah baru atau edit.
        if (userToEdit == null) { // Mode TAMBAH BARU.
            String password = passwordField.getText();
            // Password wajib diisi untuk user baru.
            if (password.isEmpty()) {
                AlertHelper.showWarning("Input Tidak Lengkap", "Password wajib diisi untuk user baru.");
                return;
            }
            // Panggil DAO untuk mendaftarkan user baru.
            success = UserDAO.registerUser(username, name, email, password, role);
        } else { // Mode EDIT.
            // Update properti objek userToEdit.
            userToEdit.setUsername(username);
            userToEdit.setName(name);
            userToEdit.setEmail(email);
            userToEdit.setRole(role);
            // Panggil DAO untuk memperbarui data user.
            success = UserDAO.updateUser(userToEdit);
        }

        // Tampilkan notifikasi berdasarkan hasil operasi.
        if (success) {
            AlertHelper.showInformation("Sukses", "Data user berhasil disimpan.");
            closeStage(); // Tutup dialog jika sukses.
        } else {
            AlertHelper.showError("Gagal", "Gagal menyimpan data. Username atau email mungkin sudah terdaftar.");
        }
    }

    /**
     * Dijalankan saat tombol Batal diklik.
     * @param event Aksi klik tombol.
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        closeStage();
    }

    /**
     * Metode pembantu untuk menutup dialog.
     */
    private void closeStage() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}