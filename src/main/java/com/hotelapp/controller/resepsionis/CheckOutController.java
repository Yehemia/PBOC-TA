package com.hotelapp.controller.resepsionis;

import com.hotelapp.dao.ReservationDAO;
import com.hotelapp.model.Penalty;
import com.hotelapp.model.Reservation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class CheckOutController {

    @FXML private TableView<Reservation> checkOutTable;
    @FXML private TableColumn<Reservation, Integer> idColumn;
    @FXML private TableColumn<Reservation, String> nameColumn;
    @FXML private TableColumn<Reservation, Integer> roomColumn;
    @FXML private TableColumn<Reservation, String> checkInTimeColumn;
    @FXML private TableColumn<Reservation, String> statusColumn;
    @FXML private TableColumn<Reservation, Void> actionColumn;

    // Menyimpan data reservasi yang akan ditampilkan di TableView.
    private ObservableList<Reservation> reservationList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Binding properti dari model Reservation ke TableView
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));      // Ganti menjadi "guestName" jika diperlukan.
        roomColumn.setCellValueFactory(new PropertyValueFactory<>("roomId"));
        checkInTimeColumn.setCellValueFactory(new PropertyValueFactory<>("checkInTime"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        addActionButtons();
        refreshData();
    }

    /**
     * Menambahkan tombol aksi "Check-Out" untuk setiap baris pada TableView.
     */
    private void addActionButtons() {
        actionColumn.setCellFactory(col -> new TableCell<Reservation, Void>() {
            private final Button checkOutBtn = new Button("Check-Out");

            {
                checkOutBtn.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white;");
                checkOutBtn.setOnAction(event -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    processCheckOut(reservation);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : checkOutBtn);
            }
        });
    }

    /**
     * Proses check-out untuk reservasi yang dipilih.
     * - Validasi bahwa status reservasi adalah "checked_in".
     * - Tanyakan apakah akan menerapkan denda.
     * - Jika ya, tampilkan dialog untuk input denda dan proses melalui DAO.
     * - Lanjutkan proses check-out.
     */
    private void processCheckOut(Reservation reservation) {
        if (!reservation.getStatus().equalsIgnoreCase("checked_in")) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Reservasi ini belum di-check-in!");
            alert.showAndWait();
            return;
        }

        // Tanyakan apakah akan menambahkan denda
        Alert confirmAlert = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Apakah ada denda yang ingin diterapkan pada reservasi ini?",
                ButtonType.YES,
                ButtonType.NO
        );
        Optional<ButtonType> confirm = confirmAlert.showAndWait();
        if (confirm.isPresent() && confirm.get() == ButtonType.YES) {
            boolean penaltyApplied = handleAddPenalty(reservation);
            if (!penaltyApplied) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Gagal menerapkan denda.");
                alert.showAndWait();
                return;
            }
        }

        // Proses check-out reservasi
        boolean success = ReservationDAO.processCheckOut(reservation.getId());
        if (success) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION,
                    "Check-Out berhasil untuk kamar " + reservation.getRoomId());
            alert.showAndWait();
            refreshData();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Gagal melakukan Check-Out.");
            alert.showAndWait();
        }
    }

    /**
     * Menampilkan dialog input denda dan menerapkan denda melalui DAO.
     * Method internal yang menerima parameter Reservation.
     */
    private boolean handleAddPenalty(Reservation reservation) {
        Dialog<Penalty> dialog = new Dialog<>();
        dialog.setTitle("Tambah Denda untuk Reservasi " + reservation.getId());
        dialog.setHeaderText("Masukkan detail denda:");

        // Tambahkan tombol "Tambahkan" dan Cancel.
        ButtonType addButtonType = new ButtonType("Tambahkan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Buat GridPane sebagai layout input.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField amountField = new TextField();
        amountField.setPromptText("Jumlah denda");
        TextField reasonField = new TextField();
        reasonField.setPromptText("Alasan denda");

        grid.add(new Label("Jumlah:"), 0, 0);
        grid.add(amountField, 1, 0);
        grid.add(new Label("Alasan:"), 0, 1);
        grid.add(reasonField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Hanya aktifkan tombol "Tambahkan" jika field jumlah tidak kosong.
        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);
        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
            addButton.setDisable(newVal.trim().isEmpty());
        });

        // Konversi input dialog menjadi objek Penalty.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    double amount = Double.parseDouble(amountField.getText().trim());
                    String reason = reasonField.getText().trim();
                    // Status denda default misalnya "pending"
                    return new Penalty(reservation.getId(), amount, reason, "pending");
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        Optional<Penalty> result = dialog.showAndWait();
        if (result.isPresent() && result.get() != null) {
            Penalty penalty = result.get();
            // Pertama, simpan detail denda ke database via PenaltyDAO.
            boolean penaltyRecorded = com.hotelapp.dao.PenaltyDAO.addPenalty(penalty);
            if (!penaltyRecorded) {
                System.err.println("Gagal menyimpan detail penalty ke tabel penalties");
                return false;
            }
            // Kedua, update data reservasi (misalnya total_price dan status denda)
            boolean reservationUpdated = ReservationDAO.applyPenalty(reservation.getId(), penalty.getAmount());
            return reservationUpdated;
        }
        return false;
    }

    /**
     * Memuat ulang data reservasi untuk check-out.
     * Proses pengambilan data dijalankan pada thread terpisah agar UI tidak terblokir.
     */
    @FXML
    public void refreshData() {
        Task<ObservableList<Reservation>> task = new Task<ObservableList<Reservation>>() {
            @Override
            protected ObservableList<Reservation> call() throws Exception {
                return FXCollections.observableArrayList(ReservationDAO.getReservationsForCheckOut());
            }
        };
        task.setOnSucceeded(e -> checkOutTable.setItems(task.getValue()));
        new Thread(task).start();
    }
}