package com.hotelapp.controller.resepsionis;

import com.hotelapp.dao.ReservationDAO;
import com.hotelapp.model.Reservation;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class CheckOutController {

    @FXML private TableView<Reservation> checkOutTable;
    @FXML private TableColumn<Reservation, Integer> idColumn;
    @FXML private TableColumn<Reservation, String> nameColumn;
    @FXML private TableColumn<Reservation, Integer> roomColumn;
    @FXML private TableColumn<Reservation, String> checkInTimeColumn;
    @FXML private TableColumn<Reservation, String> statusColumn;
    @FXML private TableColumn<Reservation, Void> actionColumn;

    @FXML private TextField penaltyAmountField;
    @FXML private Button applyPenaltyButton;

    private ObservableList<Reservation> reservationList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        roomColumn.setCellValueFactory(new PropertyValueFactory<>("roomId"));
        checkInTimeColumn.setCellValueFactory(new PropertyValueFactory<>("checkInTime"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        addActionButtons();
        refreshData();
    }

    private void addActionButtons() {
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button checkOutBtn = new Button("Check-Out");

            {
                checkOutBtn.setOnAction(event -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    processCheckOut(reservation);
                });
                checkOutBtn.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : checkOutBtn);
            }
        });
    }

    private void processCheckOut(Reservation reservation) {
        if (!reservation.getStatus().equalsIgnoreCase("checked_in")) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Reservasi ini belum di-check-in!");
            alert.showAndWait();
            return;
        }

        double penaltyFee = penaltyAmountField.getText().isEmpty() ? 0 : Double.parseDouble(penaltyAmountField.getText());

        boolean success = ReservationDAO.processCheckOut(reservation.getId(), penaltyFee);
        if (success) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Check-Out berhasil untuk kamar " + reservation.getRoomId());
            alert.showAndWait();

            // **Tambahkan pemanggilan refresh statistik**
            ReceptionistDashboardController dashboardController = new ReceptionistDashboardController();
            dashboardController.loadStatistics();

            refreshData(); // Refresh daftar tamu yang masih menginap
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Gagal melakukan Check-Out.");
            alert.showAndWait();
        }
    }


    @FXML
    private void applyPenalty() {
        String penaltyInput = penaltyAmountField.getText();
        if (penaltyInput.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Harap masukkan jumlah denda sebelum menerapkan!");
            alert.showAndWait();
            return;
        }

        try {
            double penaltyAmount = Double.parseDouble(penaltyInput);
            if (penaltyAmount <= 0) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Jumlah denda harus lebih dari 0.");
                alert.showAndWait();
                return;
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Denda sebesar " + penaltyAmount + " telah diterapkan.");
            alert.showAndWait();
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Format denda tidak valid. Masukkan angka yang benar.");
            alert.showAndWait();
        }
    }

    @FXML
    public void refreshData() {
        Task<ObservableList<Reservation>> task = new Task<>() {
            @Override
            protected ObservableList<Reservation> call() {
                return FXCollections.observableArrayList(ReservationDAO.getReservationsForCheckOut());
            }
        };
        task.setOnSucceeded(e -> checkOutTable.setItems(task.getValue()));
        new Thread(task).start();
    }
}