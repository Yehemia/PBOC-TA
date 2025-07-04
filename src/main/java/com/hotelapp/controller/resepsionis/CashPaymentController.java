package com.hotelapp.controller.resepsionis;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

public class CashPaymentController {

    @FXML private Label totalBillLabel;
    @FXML private Label changeLabel;
    @FXML private TextField cashReceivedField;
    @FXML private Button confirmButton;

    private double totalBill;
    private double cashReceived;
    private Optional<Double> result = Optional.empty();
    private final NumberFormat currencyFormatter = createCurrencyFormatter();

    @FXML
    public void initialize() {
        confirmButton.setDisable(true);
        // Hanya izinkan input angka
        cashReceivedField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                cashReceivedField.setText(newVal.replaceAll("[^\\d]", ""));
            }
            updateChange();
        });

        Platform.runLater(() -> cashReceivedField.requestFocus());
    }

    public void setTotalBill(double totalBill) {
        this.totalBill = totalBill;
        totalBillLabel.setText(currencyFormatter.format(totalBill));
    }

    private void updateChange() {
        try {
            cashReceived = Double.parseDouble(cashReceivedField.getText());
            if (cashReceived >= totalBill) {
                double change = cashReceived - totalBill;
                changeLabel.setText(currencyFormatter.format(change));
                confirmButton.setDisable(false);
            } else {
                changeLabel.setText("Rp0");
                confirmButton.setDisable(true);
            }
        } catch (NumberFormatException e) {
            changeLabel.setText("Rp0");
            confirmButton.setDisable(true);
        }
    }

    @FXML
    private void handleConfirm() {
        this.result = Optional.of(this.cashReceived);
        closeDialog();
    }

    public Optional<Double> getCashReceived() {
        return result;
    }

    private void closeDialog() {
        Stage stage = (Stage) confirmButton.getScene().getWindow();
        stage.close();
    }

    private NumberFormat createCurrencyFormatter() {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        format.setMaximumFractionDigits(0);
        return format;
    }
}