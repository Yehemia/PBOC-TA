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

/**
 * Ini adalah "otak" untuk halaman Check-In.
 * Tugasnya adalah menampilkan daftar tamu yang akan check-in dan memprosesnya.
 */
public class CheckInController {
    // Variabel yang terhubung ke komponen desain FXML.
    @FXML private TextField searchField; // Kotak untuk mencari reservasi.
    @FXML private TableView<Reservation> checkInTable; // Tabel untuk menampilkan daftar reservasi.
    @FXML private TableColumn<Reservation, String> idColumn; // Kolom Kode Booking.
    @FXML private TableColumn<Reservation, String> nameColumn; // Kolom Nama Tamu.
    @FXML private TableColumn<Reservation, Integer> roomColumn; // Kolom Nomor Kamar.
    @FXML private TableColumn<Reservation, String> checkInColumn; // Kolom Tanggal Check-in.
    @FXML private TableColumn<Reservation, String> statusColumn; // Kolom Status.
    @FXML private TableColumn<Reservation, Void> actionColumn; // Kolom untuk tombol aksi.

    // Service yang berisi logika bisnis untuk reservasi.
    private final ReservationService reservationService = new ReservationService();

    /**
     * Fungsi yang berjalan otomatis saat halaman Check-In dibuka.
     */
    @FXML
    public void initialize() {
        // Hubungkan setiap kolom di tabel dengan data yang sesuai dari objek Reservation.
        idColumn.setCellValueFactory(new PropertyValueFactory<>("bookingCode"));
        idColumn.setText("Kode Booking");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("guestName"));
        roomColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        checkInColumn.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        addActionButtons(); // Tambahkan tombol "Check-In" ke setiap baris tabel.
        refreshData(); // Muat data reservasi yang siap untuk check-in.

        // Tambahkan "listener" ke kotak pencarian.
        // Setiap kali teks di dalamnya berubah, panggil fungsi refreshData.
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            refreshData();
        });
    }

    /**
     * Fungsi untuk menambahkan kolom "Aksi" yang berisi tombol "Check-In".
     */
    private void addActionButtons() {
        // CellFactory adalah "pabrik" yang membuat sel untuk setiap baris di kolom ini.
        actionColumn.setCellFactory(col -> new TableCell<>() {
            // Buat satu tombol untuk setiap sel.
            private final Button checkInBtn = new Button("Check-In");
            {
                // Atur apa yang terjadi ketika tombol ini diklik.
                checkInBtn.setOnAction(event -> {
                    // Dapatkan data reservasi dari baris tempat tombol ini berada.
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    // Panggil fungsi untuk memproses check-in.
                    processCheckIn(reservation);
                });
                checkInBtn.getStyleClass().add("button-primary"); // Beri style agar tombol berwarna biru.
            }

            // Fungsi ini dipanggil untuk menggambar sel.
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                // Jika barisnya kosong, jangan tampilkan apa-apa.
                // Jika tidak kosong, tampilkan tombol check-in.
                setGraphic(empty ? null : checkInBtn);
            }
        });
    }

    /**
     * Memproses check-in untuk sebuah reservasi.
     * @param reservation Reservasi yang akan di-check-in.
     */
    private void processCheckIn(Reservation reservation) {
        if (reservation == null) return; // Jika tidak ada reservasi yang dipilih, hentikan.

        try {
            // Panggil service untuk melakukan proses check-in (mengubah status di database).
            reservationService.processCheckIn(reservation.getId());
            AlertHelper.showInformation("Sukses", "Check-In berhasil untuk kamar " + reservation.getRoomNumber());
            refreshData(); // Muat ulang data tabel untuk menghilangkan reservasi yang sudah di-check-in.
        } catch (BookingException e) {
            // Jika ada error bisnis (misal: reservasi tidak valid), tampilkan peringatan.
            AlertHelper.showWarning("Proses Dibatalkan", e.getMessage());
        } catch (Exception e) {
            // Jika ada error teknis lainnya, tampilkan pesan error umum.
            AlertHelper.showError("Error", "Gagal melakukan Check-In karena masalah server.");
            e.printStackTrace();
        }
    }

    /**
     * Memuat atau menyegarkan data di tabel.
     * Mengambil daftar reservasi yang siap check-in dari database.
     */
    @FXML
    public void refreshData() {
        // Ambil kata kunci dari kotak pencarian.
        String keyword = searchField.getText();
        // Gunakan "Task" untuk menjalankan query database di thread terpisah (agar UI tidak macet).
        Task<ObservableList<Reservation>> task = new Task<>() {
            @Override
            protected ObservableList<Reservation> call() {
                // Panggil DAO untuk mencari reservasi yang statusnya 'pending',
                // tanggal check-innya hari ini atau sebelumnya, dan cocok dengan kata kunci.
                return FXCollections.observableArrayList(ReservationDAO.getReservationsForCheckIn(keyword));
            }
        };
        // Setelah data berhasil didapat, masukkan ke dalam tabel.
        task.setOnSucceeded(e -> checkInTable.setItems(task.getValue()));
        // Jalankan task.
        new Thread(task).start();
    }
}