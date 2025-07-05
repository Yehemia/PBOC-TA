package com.hotelapp.controller.resepsionis;

import com.hotelapp.dao.PenaltyDAO;
import com.hotelapp.model.Penalty;
import com.hotelapp.service.BookingException;
import com.hotelapp.service.PenaltyService;
import com.hotelapp.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import java.sql.SQLException;

/**
 * Ini adalah "otak" untuk halaman Manajemen Denda.
 * Menampilkan denda yang belum lunas dan memungkinkan resepsionis untuk melunasinya.
 */
public class PenaltyController {

    // Variabel yang terhubung ke komponen desain FXML.
    @FXML private TableView<Penalty> penaltyTable; // Tabel untuk menampilkan daftar denda.
    @FXML private TableColumn<Penalty, Integer> idColumn; // Kolom ID Denda.
    @FXML private TableColumn<Penalty, Integer> reservationIdColumn; // Kolom ID Reservasi terkait.
    @FXML private TableColumn<Penalty, String> reasonColumn; // Kolom Alasan Denda.
    @FXML private TableColumn<Penalty, Double> amountColumn; // Kolom Jumlah Denda.
    @FXML private TableColumn<Penalty, String> statusColumn; // Kolom Status Denda.

    // Service yang berisi logika bisnis untuk denda.
    private final PenaltyService penaltyService = new PenaltyService();

    /**
     * Fungsi yang berjalan otomatis saat halaman Denda dibuka.
     */
    @FXML
    public void initialize() {
        // Hubungkan setiap kolom tabel dengan data yang sesuai dari objek Penalty.
        idColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        reservationIdColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getReservationId()).asObject());
        reasonColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getReason()));
        amountColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getAmount()).asObject());
        statusColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPenaltyStatus()));

        refreshData(); // Muat data denda dari database.
    }

    /**
     * Memuat atau menyegarkan data denda di tabel.
     */
    @FXML
    public void refreshData() {
        // Gunakan Task untuk query database di background.
        Task<ObservableList<Penalty>> task = new Task<>() {
            @Override
            protected ObservableList<Penalty> call() {
                // Ambil semua data denda dari DAO.
                return FXCollections.observableArrayList(PenaltyDAO.getAllPenalties());
            }
        };
        // Setelah data didapat, tampilkan di tabel.
        task.setOnSucceeded(event -> penaltyTable.setItems(task.getValue()));
        new Thread(task).start();
    }

    /**
     * Fungsi ini berjalan saat tombol "Tandai Lunas" diklik.
     */
    @FXML
    public void markPenaltyPaid() {
        // Dapatkan data denda dari baris yang sedang dipilih di tabel.
        Penalty selectedPenalty = penaltyTable.getSelectionModel().getSelectedItem();
        // Jika tidak ada yang dipilih, tampilkan peringatan.
        if (selectedPenalty == null) {
            AlertHelper.showWarning("Peringatan", "Harap pilih data denda yang ingin dikonfirmasi lunas.");
            return;
        }

        // Tampilkan dialog input teks untuk memasukkan jumlah pembayaran.
        TextInputDialog dialog = new TextInputDialog(String.valueOf(selectedPenalty.getAmount()));
        dialog.setTitle("Konfirmasi Pembayaran Denda");
        dialog.setHeaderText("Masukkan jumlah pembayaran untuk denda ID: " + selectedPenalty.getId());
        dialog.setContentText("Jumlah:");

        // Tampilkan dialog dan tunggu input dari pengguna.
        dialog.showAndWait().ifPresent(amountStr -> {
            try {
                // Ubah input teks menjadi angka (double).
                double paidAmount = Double.parseDouble(amountStr.trim());
                // Panggil service untuk memproses pelunasan denda.
                penaltyService.markPenaltyAsPaid(selectedPenalty.getId(), paidAmount);
                AlertHelper.showInformation("Sukses", "Status denda berhasil diubah");
                refreshData(); // Muat ulang data tabel.
            } catch (NumberFormatException e) {
                AlertHelper.showError("Input Tidak Valid", "Input jumlah pembayaran harus berupa angka.");
            } catch (BookingException e) {
                AlertHelper.showWarning("Proses Gagal", e.getMessage());
            } catch (SQLException e) {
                AlertHelper.showError("Error Database", "Gagal mengupdate status denda karena masalah koneksi server.");
                System.err.println("SQL Error marking penalty as paid: " + e.getMessage());
            }
        });
    }
}