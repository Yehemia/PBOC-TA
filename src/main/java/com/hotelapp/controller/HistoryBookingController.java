package com.hotelapp.controller;

import com.hotelapp.dao.ReservationDAO;
import com.hotelapp.model.Reservation;
import com.hotelapp.model.User;
import com.hotelapp.util.Session;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.util.List;

public class HistoryBookingController {
    @FXML private TableView<Reservation> historyTable;
    @FXML private TableColumn<Reservation, Integer> idColumn;
    @FXML private TableColumn<Reservation, LocalDate> checkInColumn;
    @FXML private TableColumn<Reservation, LocalDate> checkOutColumn;
    @FXML private TableColumn<Reservation, String> statusColumn;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        checkInColumn.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
        checkOutColumn.setCellValueFactory(new PropertyValueFactory<>("checkOut"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        loadHistory();
    }

    private void loadHistory() {
        User currentUser = Session.getInstance().getCurrentUser();
        if (currentUser != null) {
            List<Reservation> reservations = ReservationDAO.getReservationsByUserId(currentUser.getId());
            historyTable.getItems().setAll(reservations);
        }
    }
}

