package com.hotelapp.controller.resepsionis;

import com.hotelapp.dao.ReservationDAO;
import com.hotelapp.model.Reservation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class HistoryController {

    @FXML private TableView<Reservation> historyTable;
    @FXML private TableColumn<Reservation, Integer> idColumn;
    @FXML private TableColumn<Reservation, String> nameColumn;
    @FXML private TableColumn<Reservation, Integer> roomColumn;
    @FXML private TableColumn<Reservation, String> reservationDateColumn;
    @FXML private TableColumn<Reservation, String> checkInColumn;
    @FXML private TableColumn<Reservation, String> checkOutColumn;
    @FXML private TableColumn<Reservation, String> statusColumn;
    @FXML private TextField searchField;

    private ObservableList<Reservation> historyList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Inisialisasi kolom tabel
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        roomColumn.setCellValueFactory(new PropertyValueFactory<>("roomId"));
        reservationDateColumn.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
        checkInColumn.setCellValueFactory(new PropertyValueFactory<>("checkInTime"));
        checkOutColumn.setCellValueFactory(new PropertyValueFactory<>("checkOutTime"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));


        refreshData();
    }

    @FXML
    private void refreshData() {
        Task<List<Reservation>> task = new Task<>() {
            @Override
            protected List<Reservation> call() throws Exception {
                return ReservationDAO.getAllReservations(); // Pastikan method ini mengembalikan semua transaksi historis
            }
        };
        task.setOnSucceeded(e -> {
            historyList = FXCollections.observableArrayList(task.getValue());
            historyTable.setItems(historyList);
        });
        new Thread(task).start();
    }

    @FXML
    private void performSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            refreshData();
            return;
        }
        Task<List<Reservation>> task = new Task<>() {
            @Override
            protected List<Reservation> call() throws Exception {
                return ReservationDAO.searchReservations(keyword);
            }
        };
        task.setOnSucceeded(e -> {
            historyList = FXCollections.observableArrayList(task.getValue());
            historyTable.setItems(historyList);
        });
        new Thread(task).start();
    }
}