package com.hotelapp.controller.customer;

import com.hotelapp.dao.ReservationDAO;
import com.hotelapp.model.Reservation;
import com.hotelapp.model.User;
import com.hotelapp.service.ReservationService;
import com.hotelapp.util.AlertHelper;
import com.hotelapp.util.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class HistoryBookingController {

    @FXML private TableView<Reservation> historyTable;
    @FXML private TableColumn<Reservation, String> idColumn;
    @FXML private TableColumn<Reservation, Integer> roomNumberColumn;
    @FXML private TableColumn<Reservation, String> roomTypeColumn;
    @FXML private TableColumn<Reservation, LocalDate> checkInColumn;
    @FXML private TableColumn<Reservation, LocalDate> checkOutColumn;
    @FXML private TableColumn<Reservation, String> statusColumn;

    private ReservationService reservationService;

    @FXML
    public void initialize() {
        reservationService = new ReservationService();
        setupTableColumns();
        addCancelButtonToTable();
        loadHistoryData();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("bookingCode"));
        idColumn.setText("Kode Booking");
        roomNumberColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        roomTypeColumn.setCellValueFactory(new PropertyValueFactory<>("roomTypeName"));
        checkInColumn.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
        checkOutColumn.setCellValueFactory(new PropertyValueFactory<>("checkOut"));
        if (statusColumn != null) {
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        }
    }

    private void loadHistoryData() {
        User currentUser = Session.getInstance().getCurrentUser();
        if (currentUser != null) {
            List<Reservation> reservations = ReservationDAO.getReservationsByUserId(currentUser.getId());
            historyTable.getItems().setAll(reservations);
        }
    }

    private void addCancelButtonToTable() {
        TableColumn<Reservation, Void> actionColumn = new TableColumn<>("Aksi");

        Callback<TableColumn<Reservation, Void>, TableCell<Reservation, Void>> cellFactory = param -> new TableCell<>() {
            private final Button btn = new Button("Batal");

            {
                btn.setOnAction((ActionEvent event) -> {
                    Reservation reservationData = getTableView().getItems().get(getIndex());
                    String message = "Apakah Anda yakin ingin membatalkan pesanan ini?";
                    if ("paid".equalsIgnoreCase(reservationData.getPaymentStatus())) {
                        message += "\n\nKarena pesanan ini sudah dibayar, proses pengembalian dana (refund) akan dilakukan secara manual oleh staf kami dalam 1-3 hari kerja.";
                    }

                    Optional<ButtonType> result = AlertHelper.showConfirmation("Konfirmasi Pembatalan", message, ButtonType.OK, ButtonType.CANCEL);

                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        try {
                            reservationService.cancelReservation(reservationData);
                            AlertHelper.showInformation("Sukses", "Reservasi berhasil dibatalkan.");
                            loadHistoryData();
                        } catch (SQLException e) {
                            AlertHelper.showError("Error", "Gagal membatalkan reservasi karena masalah koneksi ke server.");
                            System.err.println("Failed to cancel reservation due to SQL error: " + e.getMessage());
                        }
                    }
                });
            }

            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    boolean isCancellable = ("pending".equalsIgnoreCase(reservation.getStatus()) || "paid".equalsIgnoreCase(reservation.getStatus()))
                            && !reservation.getCheckIn().isBefore(LocalDate.now());

                    if (isCancellable) {
                        setGraphic(btn);
                        btn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-cursor: hand;");
                    } else {
                        setGraphic(null);
                    }
                }
            }
        };

        actionColumn.setCellFactory(cellFactory);
        historyTable.getColumns().add(actionColumn);
    }
}