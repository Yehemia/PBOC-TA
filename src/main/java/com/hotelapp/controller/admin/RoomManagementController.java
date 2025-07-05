package com.hotelapp.controller.admin;

import com.hotelapp.dao.RoomDAO;
import com.hotelapp.dao.RoomTypeDAO;
import com.hotelapp.model.Room;
import com.hotelapp.model.RoomType;
import com.hotelapp.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Controller untuk halaman Manajemen Kamar.
 * Mengelola dua bagian utama: Tipe Kamar dan Unit Kamar Fisik.
 */
public class RoomManagementController {

    // @FXML menghubungkan variabel ke komponen FXML.
    @FXML private TableView<RoomType> roomTypeTable; // Tabel untuk menampilkan Tipe Kamar.
    @FXML private TableColumn<RoomType, String> typeNameColumn; // Kolom Nama Tipe Kamar.
    @FXML private TableColumn<RoomType, Double> typePriceColumn; // Kolom Harga.
    @FXML private TableColumn<RoomType, Integer> typeGuestsColumn; // Kolom Kapasitas Tamu.
    @FXML private TableColumn<RoomType, String> typeBedInfoColumn; // Kolom Info Tempat Tidur.

    @FXML private TableView<Room> roomInstanceTable; // Tabel untuk menampilkan unit kamar fisik.
    @FXML private TableColumn<Room, Integer> roomNumberColumn; // Kolom Nomor Kamar.
    @FXML private TableColumn<Room, String> roomStatusColumn; // Kolom Status Kamar.

    @FXML private Label roomInstanceLabel; // Label judul untuk tabel unit kamar.
    @FXML private ProgressIndicator instanceLoadingIndicator; // Indikator loading.

    /**
     * Inisialisasi awal saat halaman dimuat.
     */
    @FXML
    public void initialize() {
        if (instanceLoadingIndicator != null) instanceLoadingIndicator.setVisible(false);
        setupRoomTypeTable(); // Atur tabel tipe kamar.
        setupRoomInstanceTable(); // Atur tabel unit kamar.
        loadRoomTypes(); // Muat data awal untuk tipe kamar.

        // Tambahkan listener ke tabel tipe kamar.
        // Jika admin memilih sebuah baris, tabel unit kamar di bawahnya akan diperbarui.
        roomTypeTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        // Muat data unit kamar yang sesuai dengan tipe kamar yang dipilih.
                        loadRoomInstancesInBackground(newSelection);
                    } else {
                        // Jika tidak ada yang dipilih, kosongkan tabel unit kamar.
                        roomInstanceTable.getItems().clear();
                        roomInstanceLabel.setText("Daftar Kamar Fisik (Pilih Tipe Kamar di Atas)");
                    }
                }
        );
    }

    /**
     * Mengonfigurasi kolom-kolom pada tabel Tipe Kamar.
     */
    private void setupRoomTypeTable() {
        // Hubungkan setiap kolom dengan properti yang sesuai di objek RoomType.
        typeNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typePriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        typeGuestsColumn.setCellValueFactory(new PropertyValueFactory<>("maxGuests"));
        typeBedInfoColumn.setCellValueFactory(new PropertyValueFactory<>("bedInfo"));

        // Format kolom harga agar menampilkan format mata uang Rupiah.
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        typePriceColumn.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : currencyFormat.format(price));
            }
        });
    }

    /**
     * Mengonfigurasi kolom-kolom pada tabel Unit Kamar.
     */
    private void setupRoomInstanceTable() {
        roomNumberColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        roomStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    /**
     * Memuat data Tipe Kamar dari database secara asynchronous.
     */
    private void loadRoomTypes() {
        // Gunakan Task untuk menjalankan proses di background thread.
        Task<List<RoomType>> loadTypesTask = new Task<>() {
            @Override
            protected List<RoomType> call() {
                return RoomTypeDAO.getAllRoomTypes();
            }
        };
        // Setelah selesai, update tabel di JavaFX thread.
        loadTypesTask.setOnSucceeded(e -> roomTypeTable.setItems(FXCollections.observableArrayList(loadTypesTask.getValue())));
        loadTypesTask.setOnFailed(e -> {
            AlertHelper.showError("Gagal Memuat", "Tidak dapat mengambil data tipe kamar dari server.");
            System.err.println("Failed to load room types: " + loadTypesTask.getException().getMessage());
        });
        new Thread(loadTypesTask).start();
    }

    /**
     * Memuat data Unit Kamar berdasarkan Tipe Kamar yang dipilih, secara asynchronous.
     * @param roomType Tipe kamar yang unitnya akan ditampilkan.
     */
    private void loadRoomInstancesInBackground(RoomType roomType) {
        roomInstanceLabel.setText("Memuat kamar untuk tipe: " + roomType.getName() + "...");
        roomInstanceTable.setItems(FXCollections.emptyObservableList());
        if (instanceLoadingIndicator != null) instanceLoadingIndicator.setVisible(true);

        // Gunakan Task untuk query database di background.
        Task<ObservableList<Room>> loadRoomsTask = new Task<>() {
            @Override
            protected ObservableList<Room> call() {
                List<Room> rooms = RoomDAO.getRoomsByTypeId(roomType.getId());
                return FXCollections.observableArrayList(rooms);
            }
        };

        // Setelah selesai, update tabel dan UI lainnya.
        loadRoomsTask.setOnSucceeded(e -> {
            roomInstanceTable.setItems(loadRoomsTask.getValue());
            roomInstanceLabel.setText("Daftar Kamar untuk Tipe: " + roomType.getName());
            if (instanceLoadingIndicator != null) instanceLoadingIndicator.setVisible(false);
        });
        loadRoomsTask.setOnFailed(e -> {
            roomInstanceLabel.setText("Gagal memuat data kamar.");
            if (instanceLoadingIndicator != null) instanceLoadingIndicator.setVisible(false);
            AlertHelper.showError("Error", "Terjadi kesalahan saat mengambil data kamar dari server.");
            System.err.println("Failed to load room instances: " + loadRoomsTask.getException().getMessage());
        });

        new Thread(loadRoomsTask).start();
    }

    /**
     * Aksi untuk tombol "Tambah Tipe Kamar Baru".
     */
    @FXML
    private void handleAddNewRoomType(ActionEvent event) {
        openRoomTypeDialog(null); // Panggil dialog dalam mode "tambah baru".
    }

    /**
     * Aksi untuk tombol "Edit Tipe Kamar".
     */
    @FXML
    private void handleEditRoomType(ActionEvent event) {
        RoomType selectedType = roomTypeTable.getSelectionModel().getSelectedItem();
        if (selectedType == null) {
            AlertHelper.showWarning("Peringatan", "Pilih dulu sebuah Tipe Kamar dari tabel atas untuk diedit.");
            return;
        }
        // Ambil data lengkap (termasuk fasilitas) sebelum membuka dialog.
        RoomType detailedRoomType = RoomTypeDAO.getRoomTypeWithFacilitiesById(selectedType.getId());
        openRoomTypeDialog(detailedRoomType); // Panggil dialog dalam mode "edit".
    }

    /**
     * Aksi untuk tombol "Hapus Tipe Kamar".
     */
    @FXML
    private void handleDeleteRoomType(ActionEvent event) {
        RoomType selectedType = roomTypeTable.getSelectionModel().getSelectedItem();
        if (selectedType == null) {
            AlertHelper.showWarning("Peringatan", "Pilih tipe kamar yang ingin dinonaktifkan.");
            return;
        }
        // Tampilkan dialog konfirmasi.
        Optional<ButtonType> result = AlertHelper.showConfirmation("Konfirmasi Nonaktifkan",
                "Yakin ingin menonaktifkan tipe kamar '" + selectedType.getName() + "'? Tipe ini tidak akan bisa digunakan untuk reservasi baru.",
                ButtonType.OK, ButtonType.CANCEL);
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (RoomTypeDAO.deleteRoomType(selectedType.getId())) {
                AlertHelper.showInformation("Sukses", "Tipe kamar berhasil dinonaktifkan.");
                loadRoomTypes(); // Muat ulang data tabel.
            } else {
                AlertHelper.showError("Gagal", "Gagal menonaktifkan tipe kamar.");
            }
        }
    }

    /**
     * Aksi untuk tombol "Tambah Kamar Fisik".
     */
    @FXML
    private void handleAddNewRoom(ActionEvent event) {
        RoomType selectedType = roomTypeTable.getSelectionModel().getSelectedItem();
        if (selectedType == null) {
            AlertHelper.showWarning("Peringatan", "Pilih dulu Tipe Kamar di tabel atas sebelum menambah kamar fisik baru.");
            return;
        }
        openRoomDialog(null, selectedType); // Panggil dialog tambah kamar.
    }

    /**
     * Aksi untuk tombol "Edit Kamar Fisik".
     */
    @FXML
    private void handleEditRoom(ActionEvent event) {
        Room selectedRoom = roomInstanceTable.getSelectionModel().getSelectedItem();
        if (selectedRoom == null) {
            AlertHelper.showWarning("Peringatan", "Pilih kamar yang ingin diedit di tabel bawah.");
            return;
        }
        openRoomDialog(selectedRoom, selectedRoom.getRoomType()); // Panggil dialog edit kamar.
    }

    /**
     * Aksi untuk tombol "Hapus Kamar Fisik".
     */
    @FXML
    private void handleDeleteRoom(ActionEvent event) {
        Room selectedRoom = roomInstanceTable.getSelectionModel().getSelectedItem();
        if (selectedRoom == null) {
            AlertHelper.showWarning("Peringatan", "Pilih kamar yang ingin dinonaktifkan.");
            return;
        }
        // Tampilkan konfirmasi sebelum menonaktifkan.
        Optional<ButtonType> result = AlertHelper.showConfirmation("Konfirmasi Nonaktifkan",
                "Yakin ingin menonaktifkan kamar nomor " + selectedRoom.getRoomNumber() + "? Kamar ini tidak akan bisa dipesan lagi.",
                ButtonType.OK, ButtonType.CANCEL);
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (RoomDAO.deleteRoom(selectedRoom.getId())) {
                AlertHelper.showInformation("Sukses", "Kamar berhasil dinonaktifkan.");
                // Muat ulang data tabel unit kamar.
                loadRoomInstancesInBackground(selectedRoom.getRoomType());
            } else {
                AlertHelper.showError("Gagal", "Gagal menonaktifkan kamar.");
            }
        }
    }

    /**
     * Metode pembantu untuk membuka dialog tambah/edit Tipe Kamar.
     * @param roomType Data tipe kamar (null jika mode tambah, berisi data jika mode edit).
     */
    private void openRoomTypeDialog(RoomType roomType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelapp/fxml/admin/AddRoomTypeDialog.fxml"));
            Parent root = loader.load();
            AddRoomTypeDialogController controller = loader.getController();
            if (roomType != null) {
                controller.initData(roomType); // Kirim data jika mode edit.
            }
            Stage dialogStage = new Stage();
            dialogStage.setTitle(roomType == null ? "Tambah Tipe Kamar Baru" : "Edit Tipe Kamar");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait(); // Tunggu sampai dialog ditutup.
            loadRoomTypes(); // Muat ulang data setelah dialog ditutup.
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metode pembantu untuk membuka dialog tambah/edit Unit Kamar.
     * @param room Data kamar (null jika tambah, berisi data jika edit).
     * @param contextRoomType Tipe kamar konteks saat ini.
     */
    private void openRoomDialog(Room room, RoomType contextRoomType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelapp/fxml/admin/AddRoomDialog.fxml"));
            Parent root = loader.load();
            AddRoomDialogController controller = loader.getController();
            if (room != null) {
                controller.initData(room); // Mode edit.
            } else {
                controller.initData(contextRoomType); // Mode tambah.
            }
            Stage dialogStage = new Stage();
            dialogStage.setTitle(room == null ? "Tambah Kamar Baru untuk Tipe: " + contextRoomType.getName() : "Edit Kamar");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
            // Muat ulang data unit kamar setelah dialog ditutup.
            loadRoomInstancesInBackground(contextRoomType);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}