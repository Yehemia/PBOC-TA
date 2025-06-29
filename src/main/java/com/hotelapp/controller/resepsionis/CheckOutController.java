package com.hotelapp.controller.resepsionis;

import com.hotelapp.dao.ReservationDAO;
import com.hotelapp.model.Penalty;
import com.hotelapp.model.Reservation;
import com.hotelapp.service.BookingException;
import com.hotelapp.service.ReservationService;
import com.hotelapp.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.sql.SQLException;
import java.util.Optional;

public class CheckOutController {

    @FXML private TableView<Reservation> checkOutTable;
    @FXML private TableColumn<Reservation, String> idColumn;
    @FXML private TableColumn<Reservation, String> nameColumn;
    @FXML private TableColumn<Reservation, Integer> roomColumn;
    @FXML private TableColumn<Reservation, String> checkInTimeColumn;
    @FXML private TableColumn<Reservation, String> statusColumn;
    @FXML private TableColumn<Reservation, Void> actionColumn;

    private final ReservationService reservationService = new ReservationService();

    @FXML
    public void initialize() {
        idColumn.setText("Kode Booking");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("bookingCode"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("guestName"));
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
                checkOutBtn.getStyleClass().add("button-danger");
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : checkOutBtn);
            }
        });
    }

    private void processCheckOut(Reservation reservation) {
        if (reservation == null) return;

        Optional<ButtonType> response = AlertHelper.showConfirmation("Konfirmasi Denda",
                "Apakah ada denda yang ingin diterapkan pada reservasi ini?", ButtonType.YES, ButtonType.NO);
        if (response.isPresent() && response.get() == ButtonType.YES) {
            Optional<Penalty> penaltyResult = showPenaltyInputDialog();
            if (penaltyResult.isPresent()) {
                executeFinalCheckOut(reservation, penaltyResult.get());
            } else {
                AlertHelper.showInformation("Dibatalkan", "Proses check-out dibatalkan oleh pengguna.");
            }
        } else if (response.isPresent() && response.get() == ButtonType.NO) {
            executeFinalCheckOut(reservation, null);
        }
    }


    private void executeFinalCheckOut(Reservation reservation, Penalty penalty) {
        try {
            reservationService.processCheckOut(reservation.getId(), penalty);
            AlertHelper.showInformation("Sukses", "Check-Out berhasil diproses.");
            refreshData();
        } catch (BookingException e) {
            AlertHelper.showWarning("Gagal", e.getMessage());
        } catch (SQLException e) {
            AlertHelper.showError("Error Database", "Terjadi masalah saat memproses check-out di database.");
            System.err.println("SQL error during checkout: " + e.getMessage());
        } catch (Exception e) {
            AlertHelper.showError("Error Tidak Terduga", "Gagal memproses check-out karena masalah pada sistem.");
            System.err.println("Unexpected error during checkout: " + e.getMessage());
        }
    }

    private Optional<Penalty> showPenaltyInputDialog() {
        Dialog<Penalty> dialog = new Dialog<>();
        dialog.setTitle("Tambah Denda");
        dialog.setHeaderText("Masukkan detail denda yang akan diterapkan.");

        ButtonType addButtonType = new ButtonType("Tambahkan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField amountField = new TextField();
        amountField.setPromptText("Contoh: 50000");
        TextArea reasonField = new TextArea();
        reasonField.setPromptText("Contoh: Kunci kamar hilang atau kerusakan properti.");
        reasonField.setWrapText(true);

        grid.add(new Label("Jumlah (Rp):"), 0, 0);
        grid.add(amountField, 1, 0);
        grid.add(new Label("Alasan:"), 0, 1);
        grid.add(reasonField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);

        amountField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isNumeric = newValue.matches("\\d*");
            boolean isValid = isNumeric && !newValue.trim().isEmpty();
            addButton.setDisable(!isValid);
            if (!isNumeric) {
                amountField.setText(oldValue);
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    double amount = Double.parseDouble(amountField.getText());
                    String reason = reasonField.getText().isBlank() ? "Denda keterlambatan/kerusakan" : reasonField.getText().trim();
                    return new Penalty(0, amount, reason, "pending");
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        return dialog.showAndWait();
    }


    @FXML
    public void refreshData() {
        Task<ObservableList<Reservation>> task = new Task<>() {
            @Override
            protected ObservableList<Reservation> call() throws Exception {
                return FXCollections.observableArrayList(ReservationDAO.getReservationsForCheckOut());
            }
        };
        task.setOnSucceeded(e -> checkOutTable.setItems(task.getValue()));
        new Thread(task).start();
    }
}