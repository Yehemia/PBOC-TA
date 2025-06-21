package com.hotelapp.controller.resepsionis;

import com.hotelapp.dao.PenaltyDAO;
import com.hotelapp.model.Penalty;
import com.hotelapp.service.BookingException;
import com.hotelapp.service.PenaltyService;
import com.hotelapp.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import java.sql.SQLException;


public class PenaltyController {

    @FXML private TableView<Penalty> penaltyTable;
    @FXML private TableColumn<Penalty, Integer> idColumn;
    @FXML private TableColumn<Penalty, Integer> reservationIdColumn;
    @FXML private TableColumn<Penalty, String> reasonColumn;
    @FXML private TableColumn<Penalty, Double> amountColumn;
    @FXML private TableColumn<Penalty, String> statusColumn;

    private final PenaltyService penaltyService = new PenaltyService();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        reservationIdColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getReservationId()).asObject());
        reasonColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getReason()));
        amountColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getAmount()).asObject());
        statusColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPenaltyStatus()));

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

    @FXML
    public void markPenaltyPaid() {
        Penalty selectedPenalty = penaltyTable.getSelectionModel().getSelectedItem();
        if (selectedPenalty == null) {
            AlertHelper.showWarning("Peringatan", "Harap pilih data penalty yang ingin dikonfirmasi.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(String.valueOf(selectedPenalty.getAmount()));
        dialog.setTitle("Konfirmasi Pembayaran Denda");
        dialog.setHeaderText("Masukkan jumlah pembayaran untuk denda ID: " + selectedPenalty.getId());
        dialog.setContentText("Jumlah:");

        dialog.showAndWait().ifPresent(amountStr -> {
            try {
                double paidAmount = Double.parseDouble(amountStr.trim());
                penaltyService.markPenaltyAsPaid(selectedPenalty.getId(), paidAmount);
                AlertHelper.showInformation("Sukses", "Status denda berhasil diubah menjadi 'paid'.");
                refreshData();
            } catch (NumberFormatException e) {
                AlertHelper.showError("Error Input", "Input tidak valid. Harap masukkan angka.");
            } catch (BookingException e) {
                AlertHelper.showWarning("Gagal", e.getMessage());
            } catch (SQLException e) {
                AlertHelper.showError("Error Database", "Gagal mengupdate status denda.");
                e.printStackTrace();
            }
        });
    }
}