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

/**
 * Controller untuk halaman riwayat pemesanan pelanggan.
 * Menampilkan semua reservasi yang pernah dibuat oleh pengguna.
 */
public class HistoryBookingController {

    // @FXML menghubungkan variabel ke komponen FXML.
    @FXML private TableView<Reservation> historyTable; // Tabel untuk menampilkan riwayat.
    @FXML private TableColumn<Reservation, String> idColumn; // Kolom Kode Booking.
    @FXML private TableColumn<Reservation, Integer> roomNumberColumn; // Kolom Nomor Kamar.
    @FXML private TableColumn<Reservation, String> roomTypeColumn; // Kolom Tipe Kamar.
    @FXML private TableColumn<Reservation, LocalDate> checkInColumn; // Kolom Tanggal Check-in.
    @FXML private TableColumn<Reservation, LocalDate> checkOutColumn; // Kolom Tanggal Check-out.
    @FXML private TableColumn<Reservation, String> statusColumn; // Kolom Status Reservasi.

    private ReservationService reservationService; // Service untuk logika pembatalan.

    /**
     * Inisialisasi awal saat halaman dimuat.
     */
    @FXML
    public void initialize() {
        reservationService = new ReservationService();
        setupTableColumns(); // Atur koneksi kolom dengan data model.
        addCancelButtonToTable(); // Tambahkan kolom aksi dengan tombol batal.
        loadHistoryData(); // Muat data riwayat.
    }

    /**
     * Menghubungkan setiap kolom di tabel dengan properti yang sesuai di objek Reservation.
     */
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

    /**
     * Memuat data riwayat pemesanan untuk pengguna yang sedang login.
     */
    private void loadHistoryData() {
        User currentUser = Session.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Ambil data dari DAO berdasarkan ID pengguna.
            List<Reservation> reservations = ReservationDAO.getReservationsByUserId(currentUser.getId());
            // Tampilkan data di tabel.
            historyTable.getItems().setAll(reservations);
        }
    }

    /**
     * Menambahkan kolom "Aksi" ke tabel yang berisi tombol "Batal".
     */
    private void addCancelButtonToTable() {
        TableColumn<Reservation, Void> actionColumn = new TableColumn<>("Aksi");

        // CellFactory bertanggung jawab untuk membuat sel (dalam hal ini, sel dengan tombol).
        Callback<TableColumn<Reservation, Void>, TableCell<Reservation, Void>> cellFactory = param -> new TableCell<>() {
            private final Button btn = new Button("Batal");

            {
                // Aksi yang dijalankan saat tombol "Batal" diklik.
                btn.setOnAction((ActionEvent event) -> {
                    // Dapatkan objek Reservation dari baris tempat tombol ini berada.
                    Reservation reservationData = getTableView().getItems().get(getIndex());

                    // Siapkan pesan konfirmasi.
                    String message = "Apakah Anda yakin ingin membatalkan pesanan ini?";
                    // Jika pesanan sudah dibayar, tambahkan informasi tentang refund.
                    if ("paid".equalsIgnoreCase(reservationData.getPaymentStatus())) {
                        message += "\n\nKarena pesanan ini sudah dibayar, proses pengembalian dana (refund) akan dilakukan secara manual oleh staf kami dalam 1-3 hari kerja.";
                    }

                    // Tampilkan dialog konfirmasi.
                    Optional<ButtonType> result = AlertHelper.showConfirmation("Konfirmasi Pembatalan", message, ButtonType.OK, ButtonType.CANCEL);

                    // Jika pengguna menekan OK...
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        try {
                            // Panggil service untuk membatalkan reservasi.
                            reservationService.cancelReservation(reservationData);
                            AlertHelper.showInformation("Sukses", "Reservasi berhasil dibatalkan.");
                            loadHistoryData(); // Muat ulang data tabel untuk menampilkan perubahan.
                        } catch (SQLException e) {
                            AlertHelper.showError("Error", "Gagal membatalkan reservasi karena masalah koneksi ke server.");
                            System.err.println("Failed to cancel reservation due to SQL error: " + e.getMessage());
                        }
                    }
                });
            }

            /**
             * Metode ini dipanggil setiap kali sel perlu di-update.
             * Di sini kita menentukan apakah tombol "Batal" harus ditampilkan atau tidak.
             */
            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null); // Jika baris kosong, jangan tampilkan apa-apa.
                } else {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    // Logika: tombol batal hanya muncul jika statusnya "pending" atau "paid"
                    // DAN tanggal check-in belum lewat.
                    boolean isCancellable = ("pending".equalsIgnoreCase(reservation.getStatus()) || "paid".equalsIgnoreCase(reservation.getStatus()))
                            && !reservation.getCheckIn().isBefore(LocalDate.now());

                    if (isCancellable) {
                        setGraphic(btn); // Tampilkan tombol.
                        btn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-cursor: hand;");
                    } else {
                        setGraphic(null); // Sembunyikan tombol jika tidak bisa dibatalkan.
                    }
                }
            }
        };

        actionColumn.setCellFactory(cellFactory);
        historyTable.getColumns().add(actionColumn); // Tambahkan kolom baru ke tabel.
    }
}