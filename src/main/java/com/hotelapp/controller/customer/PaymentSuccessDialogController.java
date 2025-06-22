package com.hotelapp.controller.customer;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class PaymentSuccessDialogController {

    @FXML
    private Label messageLabel;

    @FXML
    private Button doneButton;

    @FXML
    public void initialize() {
        doneButton.setOnAction(e -> {
            Stage stage = (Stage) doneButton.getScene().getWindow();
            stage.close();
        });
    }

    public void setMessage(String userEmail) {
        String fullMessage = "Detail pemesanan Anda, termasuk informasi kamar dan tanggal menginap, telah dikirim ke " + userEmail + ".\n" +
                "Anda juga dapat melihat rincian pemesanan di halaman \"Status Reservasi\" kapan saja.";
        messageLabel.setText(fullMessage);
    }
}