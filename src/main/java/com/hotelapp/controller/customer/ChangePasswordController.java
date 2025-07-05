package com.hotelapp.controller.customer;

import com.hotelapp.dao.UserDAO;
import com.hotelapp.util.AlertHelper;
import com.hotelapp.util.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

/**
 * Controller untuk dialog (jendela) ubah password.
 */
public class ChangePasswordController {

    // @FXML menghubungkan variabel ke komponen FXML.
    @FXML private PasswordField oldPasswordField; // Input untuk password lama.
    @FXML private PasswordField newPasswordField; // Input untuk password baru.
    @FXML private PasswordField confirmPasswordField; // Input untuk konfirmasi password baru.
    @FXML private Button saveButton; // Tombol untuk menyimpan perubahan.
    @FXML private Button cancelButton; // Tombol untuk batal.

    /**
     * Menangani aksi saat tombol 'Simpan' diklik.
     * @param event Aksi klik tombol.
     */
    @FXML
    void handleSave(ActionEvent event) {
        // Ambil semua input dari form.
        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validasi dasar: pastikan semua field terisi.
        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            AlertHelper.showError("Error", "Semua field harus diisi.");
            return;
        }

        // Validasi: pastikan password baru dan konfirmasinya cocok.
        if (!newPassword.equals(confirmPassword)) {
            AlertHelper.showError("Error", "Password baru dan konfirmasi tidak cocok.");
            return;
        }

        // Dapatkan ID pengguna dari sesi yang sedang login.
        int userId = Session.getInstance().getCurrentUser().getId();

        try {
            // Panggil DAO untuk mengubah password di database.
            // DAO akan memverifikasi password lama sebelum menggantinya.
            boolean success = UserDAO.changePassword(userId, oldPassword, newPassword);

            if (success) {
                AlertHelper.showInformation("Sukses", "Password berhasil diubah.");
                closeStage(); // Tutup jendela jika berhasil.
            } else {
                AlertHelper.showError("Gagal", "Gagal mengubah password. Pastikan password lama Anda benar.");
            }
        } catch (Exception e) {
            AlertHelper.showError("Kesalahan Sistem", "Terjadi masalah saat mencoba mengubah password Anda.");
            System.err.println("Error changing password: " + e.getMessage());
        }
    }

    /**
     * Menangani aksi saat tombol 'Batal' diklik.
     * @param event Aksi klik tombol.
     */
    @FXML
    void handleCancel(ActionEvent event) {
        closeStage();
    }

    /**
     * Metode pembantu untuk menutup jendela dialog.
     */
    private void closeStage() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}