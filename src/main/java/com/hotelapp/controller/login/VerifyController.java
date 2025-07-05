package com.hotelapp.controller.login;

import com.hotelapp.dao.UserDAO;
import com.hotelapp.model.User;
import com.hotelapp.service.VerificationService;
import com.hotelapp.util.EmailUtil;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

/**
 * Ini adalah "otak" untuk jendela verifikasi kode OTP (One-Time Password).
 */
public class VerifyController {

    // Variabel yang terhubung ke komponen desain.
    @FXML private Label timerLabel, msgLabel; // Label untuk timer dan pesan (e.g., "kode salah").
    @FXML private ProgressBar progressBar; // Progress bar yang menunjukkan sisa waktu.
    @FXML private Hyperlink resendLink; // Link untuk kirim ulang kode.
    @FXML private TextField field1, field2, field3, field4, field5, field6; // 6 kotak untuk kode OTP.

    private List<TextField> fields; // Daftar semua kotak OTP untuk kemudahan akses.
    private User user; // Pengguna yang sedang diverifikasi.
    private Timeline countdown; // Objek untuk membuat timer yang berjalan.
    private LocalDateTime expiresAt; // Waktu kedaluwarsa kode.
    private final long TOTAL_SECONDS = 15 * 60; // Total waktu validitas kode (15 menit).

    /**
     * Menerima data pengguna dari halaman registrasi.
     * @param user Pengguna yang akan diverifikasi.
     */
    public void setUser(User user) {
        this.user = user;
        initExpiry(); // Setelah user di-set, mulai timer.
    }

    /**
     * Fungsi yang berjalan otomatis saat jendela verifikasi dibuka.
     */
    @FXML
    public void initialize() {
        // Masukkan semua kotak OTP ke dalam sebuah list.
        fields = Arrays.asList(field1, field2, field3, field4, field5, field6);
        // Atur agar kursor otomatis pindah ke kotak selanjutnya setelah satu angka diketik.
        for (int i = 0; i < fields.size(); i++) {
            TextField currentField = fields.get(i);
            int nextIndex = i + 1;

            // Tambahkan listener untuk memantau perubahan teks.
            currentField.textProperty().addListener((obs, oldVal, newVal) -> {
                // Jika teks yang baru dimasukkan adalah angka...
                if (newVal != null && newVal.matches("\\d")) {
                    // dan panjangnya 1, pindahkan fokus ke kotak berikutnya.
                    if (newVal.length() == 1 && nextIndex < fields.size()) {
                        fields.get(nextIndex).requestFocus();
                    }
                    // Jika pengguna paste lebih dari 1 angka, ambil hanya angka pertama.
                    else if (newVal.length() > 1) {
                        currentField.setText(newVal.substring(0, 1));
                    }
                }
                // Jika yang dimasukkan bukan angka, kembalikan ke nilai lama (batalkan input).
                else if (newVal != null && !newVal.isEmpty()) {
                    currentField.setText(oldVal);
                }
            });

            // Tambahkan listener untuk tombol keyboard.
            currentField.setOnKeyPressed(event -> handleBackspace(event, currentField));
        }

        // Saat jendela pertama kali muncul, langsung fokuskan kursor ke kotak pertama.
        Platform.runLater(() -> field1.requestFocus());
    }

    /**
     * Menangani logika saat tombol BACKSPACE ditekan.
     * Jika kotak saat ini kosong, fokus akan pindah ke kotak sebelumnya.
     */
    private void handleBackspace(KeyEvent event, TextField currentField) {
        if (event.getCode() == KeyCode.BACK_SPACE && currentField.getText().isEmpty()) {
            int currentIndex = fields.indexOf(currentField);
            if (currentIndex > 0) {
                fields.get(currentIndex - 1).requestFocus();
            }
        }
    }

    /**
     * Mengambil waktu kedaluwarsa token dari database dan memulai timer.
     */
    private void initExpiry() {
        try {
            expiresAt = VerificationService.getTokenExpiry(user.getId());
        } catch (SQLException e) {
            e.printStackTrace();
            msgLabel.setText("Error membaca data verifikasi.");
            return;
        }

        if (expiresAt == null) {
            msgLabel.setText("Tidak ada kode aktif untuk user ini.");
            return;
        }

        // Hentikan timer lama jika ada, lalu buat yang baru.
        if (countdown != null) countdown.stop();
        // Timeline akan menjalankan sebuah aksi setiap 1 detik.
        countdown = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateTimerAndProgress()));
        countdown.setCycleCount(Timeline.INDEFINITE); // Ulangi terus-menerus.
        countdown.play(); // Mulai timer.
    }

    /**
     * Memperbarui tampilan timer dan progress bar setiap detik.
     */
    private void updateTimerAndProgress() {
        // Hitung sisa waktu dalam detik.
        long secondsLeft = LocalDateTime.now().until(expiresAt, ChronoUnit.SECONDS);
        if (secondsLeft <= 0) {
            // Jika waktu habis...
            timerLabel.setText("Expired");
            progressBar.setProgress(0);
            resendLink.setDisable(false); // Aktifkan link kirim ulang.
            countdown.stop(); // Hentikan timer.
        } else {
            // Jika masih ada waktu, format dan tampilkan sisa waktu.
            long minutes = secondsLeft / 60;
            long seconds = secondsLeft % 60;
            timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
            // Update progress bar.
            double progress = (double) secondsLeft / TOTAL_SECONDS;
            progressBar.setProgress(progress);
        }
    }

    /**
     * Fungsi yang berjalan saat tombol "Verify" ditekan.
     */
    @FXML
    public void handleVerify(ActionEvent event) {
        // Gabungkan semua angka dari 6 kotak OTP menjadi satu string.
        StringBuilder sb = new StringBuilder();
        for (TextField field : fields) {
            sb.append(field.getText());
        }
        String inputCode = sb.toString();

        if (inputCode.length() < 6) {
            msgLabel.setText("Kode harus 6 digit.");
            return;
        }

        // Periksa apakah kode yang dimasukkan cocok dengan yang ada di database.
        boolean valid = VerificationService.verifyToken(user.getId(), inputCode);

        // Cek apakah kode sudah kedaluwarsa.
        if (expiresAt == null || LocalDateTime.now().isAfter(expiresAt)) {
            msgLabel.setText("Kode expired, silakan kirim ulang.");
            return;
        }

        if (valid) {
            // Jika kode benar:
            // 1. Ubah peran pengguna dari "PENDING" menjadi "customer".
            UserDAO.updateRole(user.getId(), "customer");
            // 2. Tandai semua token lama sebagai sudah digunakan.
            VerificationService.markAllTokensUsed(user.getId());
            if (countdown != null) countdown.stop();
            msgLabel.setText("Verifikasi sukses! Anda akan dialihkan ke halaman login...");
            msgLabel.setTextFill(javafx.scene.paint.Color.GREEN);
            // Nonaktifkan semua input agar tidak bisa diubah lagi.
            for (TextField field : fields) field.setDisable(true);
            resendLink.setDisable(true);
            ((Button) event.getSource()).setDisable(true);
            // Tunggu 2 detik, lalu pindah ke halaman login.
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(e -> redirectToLogin());
            pause.play();
        } else {
            // Jika kode salah, tampilkan pesan error.
            msgLabel.setText("Kode salah.");
            msgLabel.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    /**
     * Fungsi yang berjalan saat link "Resend Code" ditekan.
     */
    @FXML
    public void handleResend(ActionEvent event) {
        try {
            startResendCooldown(); // Mulai cooldown agar tidak bisa di-spam.
            VerificationService.markAllTokensUsed(user.getId()); // Nonaktifkan kode lama.
            String newToken = VerificationService.createAndSaveToken(user.getId()); // Buat kode baru.
            expiresAt = VerificationService.getTokenExpiry(user.getId()); // Dapatkan waktu kedaluwarsa baru.

            // Kirim email dengan kode baru.
            new Thread(() -> EmailUtil.sendVerificationEmail(user.getEmail(), newToken)).start();
            msgLabel.setText("Kode baru dikirim. Cek email Anda.");
            msgLabel.setTextFill(javafx.scene.paint.Color.BLACK);

            // Mulai ulang timer.
            if (countdown != null) countdown.stop();
            initExpiry();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Menonaktifkan link "Resend Code" selama 60 detik.
     */
    private void startResendCooldown() {
        resendLink.setDisable(true);
        PauseTransition pause = new PauseTransition(Duration.seconds(60));
        pause.setOnFinished(e -> resendLink.setDisable(false)); // Aktifkan lagi setelah 60 detik.
        pause.play();
    }

    /**
     * Pindah ke halaman login.
     */
    private void redirectToLogin() {
        try {
            Stage stage = (Stage) field1.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/com/hotelapp/fxml/login.fxml"));
            Scene newScene = new Scene(root);
            stage.setScene(newScene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            msgLabel.setText("Gagal memuat halaman login.");
        }
    }
}