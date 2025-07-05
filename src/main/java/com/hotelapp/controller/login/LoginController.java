package com.hotelapp.controller.login;

import com.hotelapp.dao.UserDAO;
import com.hotelapp.util.AlertHelper;
import com.hotelapp.util.Session;
import com.hotelapp.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Ini adalah "otak" dari halaman login.
 * Tugasnya adalah memeriksa username dan password yang dimasukkan pengguna.
 */
public class LoginController {

    // @FXML artinya variabel ini terhubung ke sebuah komponen di file FXML (desain tampilan).
    @FXML private TextField usernameField;      // Kotak untuk mengetik username.
    @FXML private PasswordField passwordField;  // Kotak untuk mengetik password (teksnya akan jadi bintang-bintang).
    @FXML private Hyperlink registerLink;       // Link tulisan "Create Account" untuk pindah ke halaman daftar.
    @FXML private Hyperlink forgotPasswordLink; // Link untuk pindah ke halaman lupa password.

    /**
     * Fungsi ini akan berjalan ketika tombol "Sign In" ditekan.
     * @param event Informasi tentang tombol apa yang ditekan.
     */
    @FXML
    public void handleLogin(ActionEvent event) {
        // Ambil teks yang diketik pengguna dari kotak username dan password.
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Cek dulu, apakah pengguna sudah mengisi username dan password?
        if (username.isBlank() || password.isBlank()) {
            // Jika salah satu atau keduanya kosong, tampilkan pesan peringatan.
            AlertHelper.showWarning("Input Tidak Valid", "Username dan Password tidak boleh kosong.");
            return; // Hentikan fungsi ini, jangan lanjutkan proses login.
        }

        // Coba cari pengguna di database dengan username dan password yang cocok.
        User user = UserDAO.authenticate(username, password);

        // Cek hasil pencarian dari database.
        if (user != null) {
            // Jika user DITEMUKAN:
            // 1. Simpan data pengguna yang login ke dalam "Sesi". Anggap saja seperti menyimpan kartu identitas
            //    selama pengguna masih di dalam aplikasi.
            Session.getInstance().setCurrentUser(user);
            // 2. Arahkan pengguna ke halaman yang sesuai dengan perannya (admin, customer, atau receptionist).
            redirectUser(user.getRole());
        } else {
            // Jika user TIDAK DITEMUKAN (username/password salah atau akun tidak aktif):
            // Tampilkan pesan error.
            AlertHelper.showError("Login Gagal", "Username atau Password salah, atau akun Anda tidak aktif.");
        }
    }

    /**
     * Fungsi ini berjalan ketika link "Create Account" ditekan.
     * @param event Informasi tentang link apa yang ditekan.
     */
    @FXML
    public void handleShowRegister(ActionEvent event) {
        try {
            // Siapkan dan muat desain halaman registrasi (register.fxml).
            Parent root = FXMLLoader.load(getClass().getResource("/com/hotelapp/fxml/register.fxml"));
            // Ambil jendela (Stage) yang sedang aktif saat ini.
            Stage stage = (Stage) registerLink.getScene().getWindow();
            // Ganti isi jendela dengan halaman registrasi.
            stage.setScene(new Scene(root, 920, 710));
        } catch (IOException e) {
            // Jika file desainnya tidak ditemukan atau ada error, tampilkan pesan.
            AlertHelper.showError("Gagal Memuat Halaman", "Tidak dapat membuka halaman pendaftaran.");
            System.err.println("Gagal memuat register.fxml: " + e.getMessage());
        }
    }

    /**
     * Fungsi ini berjalan ketika link "Forgot Password?" ditekan.
     * @param event Informasi tentang link apa yang ditekan.
     */
    @FXML
    public void handleForgotPassword(ActionEvent event) {
        try {
            // Siapkan dan muat desain halaman lupa password (ForgotPassword.fxml).
            Parent root = FXMLLoader.load(getClass().getResource("/com/hotelapp/fxml/ForgotPassword.fxml"));
            // Ambil jendela yang sedang aktif.
            Stage stage = (Stage) forgotPasswordLink.getScene().getWindow();
            // Ganti isi jendela dengan halaman lupa password.
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            // Jika file desainnya tidak ditemukan, tampilkan pesan error.
            AlertHelper.showError("Gagal Memuat Halaman", "Tidak dapat membuka halaman lupa password.");
            System.err.println("Gagal memuat ForgotPassword.fxml: " + e.getMessage());
        }
    }

    /**
     * Fungsi ini bertugas mengarahkan pengguna ke halaman yang benar setelah login berhasil.
     * @param role Peran pengguna (misalnya "admin", "customer").
     */
    private void redirectUser(String role) {
        try {
            // Tentukan file desain (FXML) mana yang akan dibuka berdasarkan peran pengguna.
            String fxmlFile = switch (role.toLowerCase()) {
                case "customer" -> "/com/hotelapp/fxml/customer/dashboard_customer.fxml";
                case "receptionist" -> "/com/hotelapp/fxml/resepsionis/ReceptionistDashboard.fxml";
                case "admin" -> "/com/hotelapp/fxml/admin/AdminDashboard.fxml";
                default -> null; // Jika perannya tidak dikenal, jangan buka apa-apa.
            };

            // Jika file FXML ditentukan...
            if (fxmlFile != null) {
                // Muat file desain tersebut.
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                Parent root = loader.load();
                // Ambil jendela saat ini.
                Stage stage = (Stage) usernameField.getScene().getWindow();
                // Buat "Scene" baru (satu layar penuh) dengan desain yang sudah dimuat.
                Scene scene = new Scene(root);
                // Ganti isi jendela dengan scene baru.
                stage.setScene(scene);
                // Posisikan jendela di tengah layar.
                stage.centerOnScreen();
                stage.show(); // Tampilkan jendela.
            } else {
                // Jika peran pengguna tidak dikenali, tampilkan pesan error.
                AlertHelper.showError("Role Tidak Dikenal", "Peran pengguna '" + role + "' tidak valid.");
            }
        } catch (IOException e) {
            // Jika terjadi error saat memuat file FXML, tampilkan pesan.
            AlertHelper.showError("Gagal Memuat Dashboard", "Terjadi kesalahan saat memuat halaman utama Anda.");
            System.err.println("Gagal memuat dashboard FXML: " + e.getMessage());
        }
    }
}