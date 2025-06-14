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

public class CheckInController {

    @FXML private TableView<Reservation> checkInTable;
    @FXML private TableColumn<Reservation, Integer> idColumn;
    @FXML private TableColumn<Reservation, String> nameColumn;
    @FXML private TableColumn<Reservation, Integer> roomColumn;
    @FXML private TableColumn<Reservation, String> checkInColumn;
    @FXML private TableColumn<Reservation, String> statusColumn;
    @FXML private TableColumn<Reservation, Void> actionColumn;

    private ObservableList<Reservation> reservationList = FXCollections.observableArrayList();



    @FXML
    public void initialize() {
        // Pastikan semua kolom memiliki pemetaan ke model Reservation
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("userId")); // Mungkin perlu diubah ke nama lengkap
        roomColumn.setCellValueFactory(new PropertyValueFactory<>("roomId"));
        checkInColumn.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        addActionButtons();
        refreshData();
    }

    private void addActionButtons() {
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button checkInBtn = new Button("Check-In");

            {
                checkInBtn.setOnAction(event -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    processCheckIn(reservation);
                });
                checkInBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : checkInBtn);
            }
        });
    }

    private void processCheckIn(Reservation reservation) {
        if (!reservation.getStatus().equalsIgnoreCase("pending")) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Reservasi ini sudah diproses sebelumnya.");
            alert.showAndWait();
            return;
        }

        boolean success = ReservationDAO.processCheckIn(reservation.getId());
        if (success) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Check-In berhasil untuk kamar " + reservation.getRoomId());
            alert.showAndWait();

            // **Tambahkan pemanggilan refresh statistik**
            ReceptionistDashboardController dashboardController = new ReceptionistDashboardController();
            dashboardController.loadStatistics();

            refreshData(); // Refresh daftar reservasi yang masih pending
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Gagal melakukan Check-In.");
            alert.showAndWait();
        }
    }


    @FXML
    public void refreshData() {
        Task<ObservableList<Reservation>> task = new Task<>() {
            @Override
            protected ObservableList<Reservation> call() {
                return FXCollections.observableArrayList(ReservationDAO.getReservationsForCheckIn());
            }
        };
        task.setOnSucceeded(e -> checkInTable.setItems(task.getValue()));
        new Thread(task).start();
    }
}

