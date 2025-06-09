package com.hotelapp.controller;

import com.hotelapp.dao.UserDAO;
import com.hotelapp.model.User;
import com.hotelapp.service.VerificationService;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class VerifyController {
    @FXML private TextField codeField;
    @FXML private Label timerLabel, msgLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Hyperlink resendLink;

    private User user;
    private Timeline countdown;
    private LocalDateTime expiresAt;
    // Total waktu token adalah 15 menit (15 x 60 detik)
    private final long TOTAL_SECONDS = 15 * 60;

    public void setUser(User user) {
        this.user = user;
        initExpiry();
    }

    // Inisialisasi waktu kedaluwarsa dengan mengambil data dari token di DB
    private void initExpiry() {
        try {
            expiresAt = VerificationService.getTokenExpiry(user.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (expiresAt == null) return;

        if (countdown != null) {
            countdown.stop();
        }
        countdown = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateTimerAndProgress()));
        countdown.setCycleCount(Timeline.INDEFINITE);
        countdown.play();
    }

    private void updateTimerAndProgress() {
        long secondsLeft = LocalDateTime.now().until(expiresAt, ChronoUnit.SECONDS);
        if (secondsLeft <= 0) {
            timerLabel.setText("Expired");
            progressBar.setProgress(0);
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
        String input = codeField.getText().trim();
        // Cek token yang dimasukkan dan catat percobaan verifikasi
        boolean valid = VerificationService.verifyToken(user.getId(), input);
        VerificationService.logVerificationAttempt(user.getId(), input, valid);

        if (expiresAt == null || LocalDateTime.now().isAfter(expiresAt)) {
            msgLabel.setText("Kode expired, silakan kirim ulang.");
            return;
        }

        if (valid) {
            // Jika verifikasi berhasil, update role user dari PENDING -> customer
            UserDAO.updateRole(user.getId(), "customer");
            // Tandai semua token sebagai sudah dipakai
            VerificationService.markAllTokensUsed(user.getId());
            if (countdown != null) countdown.stop();
            msgLabel.setText("Verifikasi sukses!");
            redirectWithAnimation();
        } else {
            msgLabel.setText("Kode salah.");
        }
    }

    @FXML
    public void handleResend(ActionEvent event) {
        try {
            // Mulai cooldown: disable tombol resend selama 60 detik
            startResendCooldown();
            VerificationService.markAllTokensUsed(user.getId());
            String newToken = UUID.randomUUID().toString().replace("-", "");
            expiresAt = VerificationService.createAndSaveToken(user.getId(), newToken);
            new Thread(() -> com.hotelapp.util.EmailUtil.sendVerificationEmail(user.getEmail(), newToken)).start();
            msgLabel.setText("Kode baru dikirim. Cek email Anda.");
            if (countdown != null) countdown.stop();
            initExpiry();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void startResendCooldown() {
        resendLink.setDisable(true);
        PauseTransition pause = new PauseTransition(Duration.seconds(60));
        pause.setOnFinished(e -> resendLink.setDisable(false));
        pause.play();
    }

    private void redirectWithAnimation() {
        try {
            Parent dashboardRoot = FXMLLoader.load(getClass().getResource("/com/hotelapp/fxml/dashboard_customer.fxml"));
            Scene scene = new Scene(dashboardRoot);
            Stage stage = (Stage) codeField.getScene().getWindow();
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

