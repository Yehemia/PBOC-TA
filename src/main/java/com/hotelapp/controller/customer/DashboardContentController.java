package com.hotelapp.controller.customer;

import com.hotelapp.dao.RoomTypeDAO;
import com.hotelapp.model.RoomType;
import com.hotelapp.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.FlowPane;
import java.io.IOException;
import java.util.List;

/**
 * Controller untuk konten utama dasbor pelanggan.
 * Bertanggung jawab untuk menampilkan daftar kamar yang tersedia.
 */
public class DashboardContentController {

    // @FXML menghubungkan variabel ke komponen FXML.
    @FXML private FlowPane roomFlowPane; // Layout untuk menampilkan kartu-kartu kamar.
    @FXML private ProgressIndicator loadingIndicator; // Indikator loading saat data diambil.

    // Referensi ke controller utama dasbor untuk navigasi.
    private DashboardCustomerController dashboardCustomerController;

    /**
     * Inisialisasi awal. (Kosong karena data dimuat setelah controller utama diset).
     */
    @FXML
    public void initialize() {
    }

    /**
     * Menerima referensi dari controller dasbor utama dan memulai pemuatan data.
     * @param dashboardCustomerController Controller utama dasbor pelanggan.
     */
    public void setDashboardCustomerController(DashboardCustomerController dashboardCustomerController) {
        this.dashboardCustomerController = dashboardCustomerController;
        loadAvailableRoomTypes();
    }

    /**
     * Memuat daftar tipe kamar yang tersedia dari database dan menampilkannya.
     */
    private void loadAvailableRoomTypes() {
        // Tampilkan indikator loading dan nonaktifkan flow pane.
        loadingIndicator.setVisible(true);
        roomFlowPane.setDisable(true);

        // Gunakan Task untuk menjalankan query database di background thread.
        // Ini mencegah UI menjadi tidak responsif (freeze).
        Task<List<RoomType>> loadTask = new Task<>() {
            @Override
            protected List<RoomType> call() throws Exception {
                // Panggil DAO untuk mendapatkan tipe kamar beserta info ketersediaannya.
                return RoomTypeDAO.getRoomTypesWithAvailability();
            }
        };

        // Setelah task berhasil (data diterima), update UI.
        loadTask.setOnSucceeded(e -> {
            roomFlowPane.getChildren().clear(); // Bersihkan konten lama.
            List<RoomType> roomTypes = loadTask.getValue(); // Ambil hasil dari task.

            for (RoomType rt : roomTypes) {
                // Hanya tampilkan tipe kamar yang memiliki setidaknya 1 kamar tersedia.
                if (rt.getAvailableRoomCount() > 0) {
                    try {
                        // Muat file FXML untuk satu kartu kamar (RoomCell.fxml).
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelapp/fxml/customer/RoomCell.fxml"));
                        Node roomCard = loader.load(); // Node adalah representasi dari FXML yang dimuat.
                        RoomCellController controller = loader.getController(); // Dapatkan controller dari kartu tersebut.

                        // Kirim data tipe kamar dan referensi controller dasbor ke controller kartu.
                        controller.setRoomTypeData(rt, this.dashboardCustomerController);

                        // Tambahkan kartu yang sudah jadi ke dalam FlowPane.
                        roomFlowPane.getChildren().add(roomCard);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
            // Sembunyikan indikator loading dan aktifkan kembali flow pane.
            loadingIndicator.setVisible(false);
            roomFlowPane.setDisable(false);
        });

        // Jika task gagal, tampilkan pesan error.
        loadTask.setOnFailed(e -> {
            loadingIndicator.setVisible(false);
            roomFlowPane.setDisable(false);
            AlertHelper.showError("Gagal Memuat Kamar", "Terjadi kesalahan saat mengambil daftar kamar dari server.");
            System.err.println("Failed to load available room types: " + loadTask.getException().getMessage());
        });

        // Jalankan task di thread baru.
        new Thread(loadTask).start();
    }
}