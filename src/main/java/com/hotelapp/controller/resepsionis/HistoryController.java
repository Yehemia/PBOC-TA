package com.hotelapp.controller.resepsionis;

import com.hotelapp.dao.ReservationDAO;
import com.hotelapp.model.Reservation;
import com.hotelapp.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;

public class HistoryController {

    @FXML private TableView<Reservation> historyTable;
    @FXML private TableColumn<Reservation, String> idColumn;
    @FXML private TableColumn<Reservation, String> nameColumn;
    @FXML private TableColumn<Reservation, Integer> roomColumn;
    @FXML private TableColumn<Reservation, String> checkInColumn;
    @FXML private TableColumn<Reservation, String> checkOutColumn;
    @FXML private TableColumn<Reservation, String> statusColumn;
    @FXML private TextField searchField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Pagination pagination;

    private static final int ROWS_PER_PAGE = 25;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupPagination();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("bookingCode"));
        idColumn.setText("Kode Booking");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("guestName"));
        roomColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        checkInColumn.setCellValueFactory(new PropertyValueFactory<>("checkInTime"));
        checkOutColumn.setCellValueFactory(new PropertyValueFactory<>("checkOutTime"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void setupPagination() {
        String searchTerm = searchField.getText();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        int totalData = ReservationDAO.getReservationCount(searchTerm, startDate, endDate);
        int pageCount = (int) Math.ceil((double) totalData / ROWS_PER_PAGE);
        if (pageCount == 0) {
            pageCount = 1;
        }

        pagination.setPageCount(pageCount);
        pagination.setPageFactory(this::createPage);
    }

    @FXML
    private void handleFilter() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        if ((startDate != null && endDate == null) || (startDate == null && endDate != null)) {
            AlertHelper.showWarning("Filter Tidak Lengkap", "Harap isi kedua tanggal (Dari dan Sampai) untuk filter rentang waktu.");
            return;
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            AlertHelper.showWarning("Tanggal Tidak Valid", "Tanggal 'Dari' tidak boleh setelah tanggal 'Sampai'.");
            return;
        }
        setupPagination();
    }

    private Node createPage(int pageIndex) {
        String searchTerm = searchField.getText();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        List<Reservation> data = ReservationDAO.getReservationsByPage(pageIndex, ROWS_PER_PAGE, searchTerm, startDate, endDate);
        historyTable.setItems(FXCollections.observableArrayList(data));

        return new VBox();
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        setupPagination();
    }
}