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

/**
 * Ini adalah "otak" untuk halaman Check-Out.
 * Tugasnya adalah menampilkan tamu yang sedang menginap dan memproses check-out mereka.
 */
public class CheckOutController {

    // Variabel yang terhubung ke komponen desain FXML.
    @FXML private TableView<Reservation> checkOutTable; // Tabel untuk menampilkan tamu yang akan check-out.
    @FXML private TableColumn<Reservation, String> idColumn;
    @FXML private TableColumn<Reservation, String> nameColumn;
    @FXML private TableColumn<Reservation, Integer> roomColumn;
    @FXML private TableColumn<Reservation, String> checkInTimeColumn; // Kolom waktu check-in.
    @FXML private TableColumn<Reservation, String> statusColumn;
    @FXML private TableColumn<Reservation, Void> actionColumn; // Kolom untuk tombol aksi.

    // Service yang berisi logika bisnis untuk reservasi.
    private final ReservationService reservationService = new ReservationService();

    /**
     * Fungsi yang berjalan otomatis saat halaman Check-Out dibuka.
     */
    @FXML
    public void initialize() {
        // Hubungkan kolom tabel dengan data dari objek Reservation.
        idColumn.setText("Kode Booking");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("bookingCode"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("guestName"));
        roomColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        checkInTimeColumn.setCellValueFactory(new PropertyValueFactory<>("checkInTime"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        addActionButtons(); // Tambahkan tombol "Check-Out" ke setiap baris.
        refreshData(); // Muat data tamu yang sedang menginap.
    }

    /**
     * Fungsi untuk menambahkan kolom "Aksi" yang berisi tombol "Check-Out".
     */
    private void addActionButtons() {
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button checkOutBtn = new Button("Check-Out");
            {
                checkOutBtn.setOnAction(event -> {
                    // Dapatkan data reservasi dari baris ini.
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    // Panggil fungsi untuk memulai proses check-out.
                    processCheckOut(reservation);
                });
                checkOutBtn.getStyleClass().add("button-danger"); // Beri warna merah pada tombol.
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : checkOutBtn);
            }
        });
    }

    /**
     * Memulai alur proses check-out.
     * Pertama, akan menanyakan apakah ada denda atau tidak.
     * @param reservation Reservasi yang akan di-check-out.
     */
    private void processCheckOut(Reservation reservation) {
        if (reservation == null) return;

        // Tampilkan dialog konfirmasi untuk menanyakan soal denda.
        Optional<ButtonType> response = AlertHelper.showConfirmation("Konfirmasi Denda",
                "Apakah ada denda yang ingin diterapkan pada reservasi ini?", ButtonType.YES, ButtonType.NO);

        // Jika resepsionis memilih "YES" (ada denda)...
        if (response.isPresent() && response.get() == ButtonType.YES) {
            // Tampilkan dialog untuk memasukkan detail denda.
            Optional<Penalty> penaltyResult = showPenaltyInputDialog();
            // Jika resepsionis mengisi denda dan menekan "Tambahkan"...
            if (penaltyResult.isPresent()) {
                // Lanjutkan proses check-out dengan data denda.
                executeFinalCheckOut(reservation, penaltyResult.get());
            } else {
                // Jika dialog denda dibatalkan.
                AlertHelper.showInformation("Dibatalkan", "Proses check-out dibatalkan oleh pengguna.");
            }
        }
        // Jika resepsionis memilih "NO" (tidak ada denda)...
        else if (response.isPresent() && response.get() == ButtonType.NO) {
            // Lanjutkan proses check-out tanpa denda (denda = null).
            executeFinalCheckOut(reservation, null);
        }
    }


    /**
     * Mengeksekusi proses check-out final di database.
     * @param reservation Reservasi yang akan di-check-out.
     * @param penalty Objek denda (bisa null jika tidak ada denda).
     */
    private void executeFinalCheckOut(Reservation reservation, Penalty penalty) {
        try {
            // Panggil service untuk memproses check-out.
            // Service akan menyimpan denda (jika ada) dan mengubah status reservasi & kamar.
            reservationService.processCheckOut(reservation.getId(), penalty);
            AlertHelper.showInformation("Sukses", "Check-Out berhasil diproses.");
            refreshData(); // Muat ulang data tabel.
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

    /**
     * Menampilkan dialog (jendela pop-up) untuk memasukkan detail denda.
     * @return Objek Penalty jika user menekan "Tambahkan", atau Optional.empty() jika dibatalkan.
     */
    private Optional<Penalty> showPenaltyInputDialog() {
        Dialog<Penalty> dialog = new Dialog<>();
        dialog.setTitle("Tambah Denda");
        dialog.setHeaderText("Masukkan detail denda yang akan diterapkan.");

        // Buat tombol "Tambahkan" dan "Batal".
        ButtonType addButtonType = new ButtonType("Tambahkan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Buat layout untuk form input denda.
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

        // Awalnya, tombol "Tambahkan" dinonaktifkan.
        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);

        // Tambahkan listener ke field jumlah.
        // Tombol "Tambahkan" hanya akan aktif jika field jumlah diisi dengan angka.
        amountField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isNumeric = newValue.matches("\\d*");
            boolean isValid = isNumeric && !newValue.trim().isEmpty();
            addButton.setDisable(!isValid);
            if (!isNumeric) {
                amountField.setText(oldValue); // Batalkan input jika bukan angka.
            }
        });

        // Tentukan apa yang harus dikembalikan saat dialog ditutup.
        dialog.setResultConverter(dialogButton -> {
            // Jika tombol "Tambahkan" yang ditekan...
            if (dialogButton == addButtonType) {
                try {
                    // Buat objek Penalty baru dari input form.
                    double amount = Double.parseDouble(amountField.getText());
                    String reason = reasonField.getText().isBlank() ? "Denda keterlambatan/kerusakan" : reasonField.getText().trim();
                    return new Penalty(0, amount, reason, "pending");
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null; // Kembalikan null jika tombol lain (misal: Batal) ditekan.
        });

        // Tampilkan dialog dan tunggu hasilnya.
        return dialog.showAndWait();
    }


    /**
     * Memuat atau menyegarkan data di tabel.
     * Mengambil daftar tamu yang sedang dalam status "checked_in".
     */
    @FXML
    public void refreshData() {
        Task<ObservableList<Reservation>> task = new Task<>() {
            @Override
            protected ObservableList<Reservation> call() throws Exception {
                // Panggil DAO untuk mengambil data.
                return FXCollections.observableArrayList(ReservationDAO.getReservationsForCheckOut());
            }
        };
        task.setOnSucceeded(e -> checkOutTable.setItems(task.getValue()));
        new Thread(task).start();
    }
}