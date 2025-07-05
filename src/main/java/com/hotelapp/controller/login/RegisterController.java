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
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;

/**
 * Ini adalah "otak" dari halaman registrasi atau pendaftaran akun baru.
 */
public class RegisterController {

    // Variabel yang terhubung ke komponen di desain (register.fxml).
    @FXML private TextField usernameField, nameField, emailField; // Kotak input untuk data pengguna.
    @FXML private PasswordField passwordField; // Kotak input untuk password.
    @FXML private Hyperlink loginLink; // Link untuk kembali ke halaman login.


    /**
     * Fungsi ini berjalan otomatis saat halaman registrasi pertama kali dibuka.
     * Digunakan untuk menambahkan validasi email secara langsung.
     */
    @FXML
    public void initialize() {
        // Tambahkan "listener" ke kotak email. Listener ini akan memantau setiap kali teks di dalamnya berubah.
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Jika kotak email tidak kosong...
            if (!newValue.trim().isEmpty()) {
                // Periksa apakah format emailnya benar (ada '@' dan '.').
                if (ValidationUtil.isEmailValid(newValue)) {
                    // Jika benar, biarkan kotak email seperti biasa.
                    emailField.setStyle("");
                } else {
                    // Jika salah, beri garis merah di sekitar kotak email sebagai tanda error.
                    emailField.setStyle("-fx-border-color: red; -fx-border-width: 1.5px; -fx-border-radius: 10px;");
                }
            } else {
                // Jika kosong, hilangkan garis merah.
                emailField.setStyle("");
            }
        });
    }

    /**
     * Fungsi ini berjalan ketika tombol "Sign Up" ditekan.
     * @param event Informasi tentang tombol yang ditekan.
     */
    @FXML
    public void handleRegister(ActionEvent event) {
        // Ambil semua data yang diketik pengguna.
        String username = usernameField.getText();
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        // Cek apakah ada field yang masih kosong.
        if (username.isBlank() || name.isBlank() || email.isBlank() || password.isBlank()) {
            AlertHelper.showWarning( "Form Tidak Lengkap", "Semua field wajib diisi!");
            return; // Hentikan proses pendaftaran.
        }

        // Cek sekali lagi apakah format email sudah benar sebelum mengirim data.
        if (!ValidationUtil.isEmailValid(email)) {
            AlertHelper.showWarning("Format Email Salah", "Silakan masukkan alamat email yang valid.");
            return;
        }

        // Coba daftarkan pengguna baru ke database. Peran awalnya adalah "PENDING".
        boolean success = UserDAO.registerUser(username, name, email, password, "PENDING");

        if (success) {
            // Jika berhasil disimpan di database...
            // Ambil kembali data user yang baru dibuat untuk mendapatkan ID-nya.
            User user = UserDAO.getUserByUsername(username);
            if (user != null) {
                try {
                    // Buat kode verifikasi (token) acak untuk user ini.
                    String token = VerificationService.createAndSaveToken(user.getId());
                    // Kirim kode tersebut ke email pengguna di thread terpisah agar aplikasi tidak "freeze".
                    new Thread(() -> EmailUtil.sendVerificationEmail(user.getEmail(), token)).start();
                } catch (SQLException e) {
                    e.printStackTrace();
                    AlertHelper.showError("Kesalahan Database", "Gagal membuat token verifikasi.");
                }
                // Tampilkan jendela pop-up untuk memasukkan kode verifikasi.
                showVerificationDialog(user);
            }
        } else {
            // Jika pendaftaran gagal (kemungkinan username atau email sudah ada).
            AlertHelper.showError("Registrasi Gagal", "Username atau Email mungkin sudah terdaftar. Silakan gunakan yang lain.");
        }
    }

    /**
     * Menampilkan jendela pop-up (dialog) untuk verifikasi email.
     * @param user Pengguna yang baru saja mendaftar.
     */
    private void showVerificationDialog(User user) {
        try {
            // Muat desain untuk jendela verifikasi (verify.fxml).
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelapp/fxml/verify.fxml"));
            Parent root = loader.load();
            // Dapatkan "otak" (controller) dari jendela verifikasi.
            VerifyController vc = loader.getController();
            // Kirim data pengguna ke controller verifikasi.
            vc.setUser(user);

            // Buat sebuah jendela baru (Stage) untuk dialog.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Verifikasi Akun");
            // Modality.APPLICATION_MODAL artinya pengguna tidak bisa klik jendela utama sebelum dialog ini ditutup.
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner((Stage) usernameField.getScene().getWindow()); // Jadikan jendela registrasi sebagai "induk".

            Scene scene = new Scene(root);

            // Terapkan file CSS (styling) ke jendela verifikasi.
            URL cssUrl = getClass().getResource("/styles/verify-style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            dialogStage.setScene(scene);
            dialogStage.setResizable(false); // Jendela tidak bisa diubah ukurannya.
            dialogStage.showAndWait(); // Tampilkan jendela dan tunggu sampai ditutup.

            // Setelah jendela verifikasi ditutup, otomatis pindah ke halaman login.
            handleShowLogin(null);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fungsi untuk kembali ke halaman login.
     * @param event Informasi tentang link yang ditekan (bisa null jika dipanggil dari kode lain).
     */
    @FXML
    public void handleShowLogin(ActionEvent event) {
        try {
            // Dapatkan jendela saat ini.
            Stage stage = (Stage) (event != null ? ((Hyperlink) event.getSource()).getScene().getWindow() : usernameField.getScene().getWindow());
            // Muat desain halaman login.
            Parent root = FXMLLoader.load(getClass().getResource("/com/hotelapp/fxml/login.fxml"));
            Scene newScene = new Scene(root, stage.getWidth(), stage.getHeight());

            // Terapkan CSS ke halaman login.
            URL cssUrl = getClass().getResource("/styles/login-style.css");
            if (cssUrl != null) {
                newScene.getStylesheets().add(cssUrl.toExternalForm());
            }
            // Ganti isi jendela dengan halaman login.
            stage.setScene(newScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}