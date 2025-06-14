package com.hotelapp.controller.resepsionis;

import com.hotelapp.dao.PenaltyDAO;
import com.hotelapp.model.Penalty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Optional;

public class PenaltyController {

    @FXML
    private TableView<Penalty> penaltyTable;
    @FXML
    private TableColumn<Penalty, Integer> idColumn;
    @FXML
    private TableColumn<Penalty, Integer> reservationIdColumn;
    @FXML
    private TableColumn<Penalty, String> reasonColumn;
    @FXML
    private TableColumn<Penalty, Double> amountColumn;
    @FXML
    private TableColumn<Penalty, String> statusColumn;

    @FXML
    public void initialize() {
        // Binding masing-masing kolom ke properti model Penalty
        idColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        reservationIdColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getReservationId()).asObject());
        reasonColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getReason()));
        amountColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getAmount()).asObject());
        statusColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPenaltyStatus()));

        // Ambil data awal
        refreshData();
    }

    @FXML
    public void refreshData() {
        Task<ObservableList<Penalty>> task = new Task<>() {
            @Override
            protected ObservableList<Penalty> call() {
                return FXCollections.observableArrayList(PenaltyDAO.getAllPenalties());
            }
        };
        task.setOnSucceeded(event -> penaltyTable.setItems(task.getValue()));
        new Thread(task).start();
    }

    /**
     * Method ini dipanggil ketika tombol "Tandai Dibayar" ditekan.
     * Akan muncul dialog untuk input pembayaran penalty.
     * Setelah divalidasi, status penalty akan diupdate menjadi "paid".
     */
    @FXML
    public void markPenaltyPaid() {
        Penalty selectedPenalty = penaltyTable.getSelectionModel().getSelectedItem();
        if (selectedPenalty == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Harap pilih data penalty yang ingin dikonfirmasi.");
            alert.showAndWait();
            return;
        }

        // Dialog input untuk konfirmasi pembayaran. Tambahkan input jika diperlukan, misalnya jumlah.
        TextInputDialog dialog = new TextInputDialog(String.valueOf(selectedPenalty.getAmount()));
        dialog.setTitle("Konfirmasi Pembayaran Penalty");
        dialog.setHeaderText("Masukkan jumlah pembayaran untuk penalty ini:");
        dialog.setContentText("Jumlah (harus sama atau lebih dari " + selectedPenalty.getAmount() + "):");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                double paidAmount = Double.parseDouble(result.get().trim());
                if (paidAmount < selectedPenalty.getAmount()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Jumlah pembayaran kurang dari jumlah penalty.");
                    alert.showAndWait();
                    return;
                }
                // Jika sudah valid, update status penalty menjadi "paid"
                boolean updated = PenaltyDAO.updatePenaltyStatus(selectedPenalty.getId(), "paid");
                if (updated) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Penalty telah dikonfirmasi dan ditandai sebagai 'paid'.");
                    alert.showAndWait();
                    refreshData();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Gagal mengupdate status penalty.");
                    alert.showAndWait();
                }
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Input tidak valid. Harap masukkan angka yang benar.");
                alert.showAndWait();
            }
        }
    }
}