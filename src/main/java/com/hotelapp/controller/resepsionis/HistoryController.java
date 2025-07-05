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

/**
 * Ini adalah "otak" untuk halaman Riwayat Reservasi.
 * Menampilkan semua data reservasi dengan fitur filter dan pagination.
 */
public class HistoryController {

    // Variabel yang terhubung ke komponen desain FXML.
    @FXML private TableView<Reservation> historyTable; // Tabel untuk menampilkan riwayat.
    @FXML private TableColumn<Reservation, String> idColumn;
    @FXML private TableColumn<Reservation, String> nameColumn;
    @FXML private TableColumn<Reservation, Integer> roomColumn;
    @FXML private TableColumn<Reservation, String> checkInColumn;
    @FXML private TableColumn<Reservation, String> checkOutColumn;
    @FXML private TableColumn<Reservation, String> statusColumn;

    @FXML private TextField searchField; // Kotak pencarian.
    @FXML private DatePicker startDatePicker; // Pilihan tanggal awal untuk filter.
    @FXML private DatePicker endDatePicker; // Pilihan tanggal akhir untuk filter.
    @FXML private Pagination pagination; // Komponen untuk navigasi halaman.

    // Menentukan jumlah baris data yang ditampilkan per halaman.
    private static final int ROWS_PER_PAGE = 25;

    /**
     * Fungsi yang berjalan otomatis saat halaman Riwayat dibuka.
     */
    @FXML
    public void initialize() {
        setupTableColumns(); // Atur konfigurasi kolom tabel.
        setupPagination(); // Atur pagination.
    }

    /**
     * Menghubungkan setiap kolom tabel dengan data yang sesuai dari objek Reservation.
     */
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("bookingCode"));
        idColumn.setText("Kode Booking");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("guestName"));
        roomColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        checkInColumn.setCellValueFactory(new PropertyValueFactory<>("checkInTime"));
        checkOutColumn.setCellValueFactory(new PropertyValueFactory<>("checkOutTime"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    /**
     * Mengatur atau menginisialisasi ulang komponen Pagination.
     */
    private void setupPagination() {
        // Ambil nilai dari filter saat ini.
        String searchTerm = searchField.getText();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        // Hitung total data yang cocok dengan filter dari database.
        int totalData = ReservationDAO.getReservationCount(searchTerm, startDate, endDate);
        // Hitung berapa banyak halaman yang dibutuhkan.
        int pageCount = (int) Math.ceil((double) totalData / ROWS_PER_PAGE);
        // Jika tidak ada data, tetap tampilkan 1 halaman kosong.
        if (pageCount == 0) {
            pageCount = 1;
        }

        pagination.setPageCount(pageCount); // Atur jumlah halaman pada komponen pagination.
        // Tentukan fungsi mana yang akan dipanggil setiap kali pengguna berpindah halaman.
        // Di sini, kita memanggil createPage().
        pagination.setPageFactory(this::createPage);
    }

    /**
     * Fungsi ini dipanggil setiap kali tombol "Filter" diklik.
     */
    @FXML
    private void handleFilter() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        // Validasi: jika salah satu tanggal diisi, keduanya harus diisi.
        if ((startDate != null && endDate == null) || (startDate == null && endDate != null)) {
            AlertHelper.showWarning("Filter Tidak Lengkap", "Harap isi kedua tanggal (Dari dan Sampai) untuk filter rentang waktu.");
            return;
        }
        // Validasi: tanggal awal tidak boleh setelah tanggal akhir.
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            AlertHelper.showWarning("Tanggal Tidak Valid", "Tanggal 'Dari' tidak boleh setelah tanggal 'Sampai'.");
            return;
        }
        // Setelah validasi, atur ulang pagination dengan filter baru.
        setupPagination();
    }

    /**
     * Ini adalah "pabrik halaman" untuk pagination.
     * Fungsi ini dipanggil oleh komponen Pagination setiap kali halaman perlu dibuat/ditampilkan.
     * @param pageIndex Nomor halaman yang akan ditampilkan (dimulai dari 0).
     * @return Node (dalam kasus ini, VBox kosong) yang akan ditampilkan. Data tabel sudah diperbarui di dalam fungsi ini.
     */
    private Node createPage(int pageIndex) {
        // Ambil nilai dari filter saat ini.
        String searchTerm = searchField.getText();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        // Ambil data dari database untuk halaman yang spesifik dan dengan filter yang ada.
        List<Reservation> data = ReservationDAO.getReservationsByPage(pageIndex, ROWS_PER_PAGE, searchTerm, startDate, endDate);
        // Tampilkan data tersebut di tabel.
        historyTable.setItems(FXCollections.observableArrayList(data));

        // Pagination memerlukan sebuah Node untuk dikembalikan, meskipun kita tidak menampilkannya.
        // Perubahan utama terjadi pada tabel.
        return new VBox();
    }

    /**
     * Fungsi ini berjalan saat tombol "Refresh" diklik.
     * Tujuannya adalah untuk menghapus semua filter dan kembali ke tampilan awal.
     */
    @FXML
    private void handleRefresh() {
        searchField.clear(); // Hapus teks pencarian.
        startDatePicker.setValue(null); // Hapus filter tanggal.
        endDatePicker.setValue(null);
        setupPagination(); // Atur ulang pagination tanpa filter.
    }
}