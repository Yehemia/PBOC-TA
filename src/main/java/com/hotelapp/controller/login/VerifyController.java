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

public class VerifyController {

    // --- FXML Fields (Gabungan dari UI baru dan logika lama) ---
    @FXML private Label timerLabel, msgLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Hyperlink resendLink;

    // Field baru untuk 6 kotak OTP
    @FXML private TextField field1, field2, field3, field4, field5, field6;
    private List<TextField> fields;

    // --- Properti Logika dari Controller Lama Anda (DIPERTAHANKAN) ---
    private User user;
    private Timeline countdown;
    private LocalDateTime expiresAt;
    private final long TOTAL_SECONDS = 15 * 60; // 15 menit

    /**
     * DIPERTAHANKAN: Metode untuk menerima User dari halaman registrasi.
     */
    public void setUser(User user) {
        this.user = user;
        // Panggil initExpiry setelah user di-set, sama seperti kode lama Anda.
        initExpiry();
    }

    @FXML
    public void initialize() {
        // Gabungkan semua field ke dalam satu list untuk kemudahan
        fields = Arrays.asList(field1, field2, field3, field4, field5, field6);

        // Tambahkan listener ke setiap field untuk auto-tab dan backspace (LOGIKA UI BARU)
        for (int i = 0; i < fields.size(); i++) {
            TextField currentField = fields.get(i);
            int nextIndex = i + 1;

            currentField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal.matches("\\d")) {
                    if (newVal.length() == 1 && nextIndex < fields.size()) {
                        fields.get(nextIndex).requestFocus();
                    } else if (newVal.length() > 1) {
                        currentField.setText(newVal.substring(0, 1));
                    }
                } else if (newVal != null && !newVal.isEmpty()) {
                    currentField.setText(oldVal);
                }
            });

            currentField.setOnKeyPressed(event -> handleBackspace(event, currentField));
        }

        // Fokus pada field pertama saat halaman dimuat
        Platform.runLater(() -> field1.requestFocus());
    }

    private void handleBackspace(KeyEvent event, TextField currentField) {
        if (event.getCode() == KeyCode.BACK_SPACE && currentField.getText().isEmpty()) {
            int currentIndex = fields.indexOf(currentField);
            if (currentIndex > 0) {
                fields.get(currentIndex - 1).requestFocus();
            }
        }
    }

    /**
     * DIPERTAHANKAN: Semua logika timer canggih Anda.
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

        if (countdown != null) countdown.stop();
        countdown = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateTimerAndProgress()));
        countdown.setCycleCount(Timeline.INDEFINITE);
        countdown.play();
    }

    private void updateTimerAndProgress() {
        long secondsLeft = LocalDateTime.now().until(expiresAt, ChronoUnit.SECONDS);
        if (secondsLeft <= 0) {
            timerLabel.setText("Expired");
            progressBar.setProgress(0);
            resendLink.setDisable(false);
            countdown.stop();
        } else {
            long minutes = secondsLeft / 60;
            long seconds = secondsLeft % 60;
            timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
            double progress = (double) secondsLeft / TOTAL_SECONDS;
            progressBar.setProgress(progress);
        }
    }

    @FXML
    public void handleVerify(ActionEvent event) {
        // DIUBAH: Menggabungkan kode dari 6 field menjadi satu string
        StringBuilder sb = new StringBuilder();
        for (TextField field : fields) {
            sb.append(field.getText());
        }
        String inputCode = sb.toString();

        if (inputCode.length() < 6) {
            msgLabel.setText("Kode harus 6 digit.");
            return;
        }

        // Sisa logika verifikasi Anda DIPERTAHANKAN
        boolean valid = VerificationService.verifyToken(user.getId(), inputCode);
        VerificationService.logVerificationAttempt(user.getId(), inputCode, valid);

        if (expiresAt == null || LocalDateTime.now().isAfter(expiresAt)) {
            msgLabel.setText("Kode expired, silakan kirim ulang.");
            return;
        }

        if (valid) {
            UserDAO.updateRole(user.getId(), "customer");
            VerificationService.markAllTokensUsed(user.getId());
            if (countdown != null) countdown.stop();
            msgLabel.setText("Verifikasi sukses! Anda akan dialihkan...");
            msgLabel.setTextFill(javafx.scene.paint.Color.GREEN);
            redirectWithAnimation();
        } else {
            msgLabel.setText("Kode salah.");
            msgLabel.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    @FXML
    public void handleResend(ActionEvent event) {
        try {
            startResendCooldown();
            VerificationService.markAllTokensUsed(user.getId());

            // DIUBAH: Panggil metode baru yang menghasilkan 6-digit token
            String newToken = VerificationService.createAndSaveToken(user.getId());
            expiresAt = VerificationService.getTokenExpiry(user.getId()); // Ambil expiry yang baru

            new Thread(() -> EmailUtil.sendVerificationEmail(user.getEmail(), newToken)).start();
            msgLabel.setText("Kode baru dikirim. Cek email Anda.");
            msgLabel.setTextFill(javafx.scene.paint.Color.BLACK);

            if (countdown != null) countdown.stop();
            initExpiry(); // Mulai ulang timer dengan waktu expiry yang baru
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * DIPERTAHANKAN: Semua metode bantuan Anda.
     */
    private void startResendCooldown() {
        resendLink.setDisable(true);
        PauseTransition pause = new PauseTransition(Duration.seconds(60));
        pause.setOnFinished(e -> resendLink.setDisable(false));
        pause.play();
    }

    private void redirectWithAnimation() {
        try {
            // DIUBAH: Ambil stage dari salah satu field yang ada
            Stage stage = (Stage) field1.getScene().getWindow();

            // Logika animasi Anda tetap sama
            Parent dashboardRoot = FXMLLoader.load(getClass().getResource("/com/hotelapp/fxml/customer/dashboard_customer.fxml"));
            Scene scene = new Scene(dashboardRoot, stage.getWidth(), stage.getHeight()); // Gunakan ukuran stage saat ini

            FadeTransition fade = new FadeTransition(Duration.seconds(1), dashboardRoot);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.setOnFinished(event -> stage.setScene(scene));
            fade.play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}