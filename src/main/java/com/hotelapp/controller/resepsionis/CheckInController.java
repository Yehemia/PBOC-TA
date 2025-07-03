package com.hotelapp.controller.resepsionis;

import com.hotelapp.dao.ReservationDAO;
import com.hotelapp.model.Reservation;
import com.hotelapp.service.BookingException;
import com.hotelapp.service.ReservationService;
import com.hotelapp.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class CheckInController {

    @FXML private TableView<Reservation> checkInTable;
    @FXML private TableColumn<Reservation, String> idColumn;
    @FXML private TableColumn<Reservation, String> nameColumn;
    @FXML private TableColumn<Reservation, Integer> roomColumn;
    @FXML private TableColumn<Reservation, String> checkInColumn;
    @FXML private TableColumn<Reservation, String> statusColumn;
    @FXML private TableColumn<Reservation, Void> actionColumn;

    private final ReservationService reservationService = new ReservationService();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("bookingCode"));
        idColumn.setText("Kode Booking");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("guestName"));
        roomColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
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
                checkInBtn.getStyleClass().add("button-primary");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : checkInBtn);
            }
        });
    }

    private void processCheckIn(Reservation reservation) {
        if (reservation == null) return;

        try {
            reservationService.processCheckIn(reservation.getId());
            AlertHelper.showInformation("Sukses", "Check-In berhasil untuk kamar " + reservation.getRoomId());
            refreshData();
        } catch (BookingException e) {
            AlertHelper.showWarning("Proses Dibatalkan", e.getMessage());
        } catch (Exception e) {
            AlertHelper.showError("Error", "Gagal melakukan Check-In karena masalah server.");
            e.printStackTrace();
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