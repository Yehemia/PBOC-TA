package com.hotelapp.controller.admin;

import com.hotelapp.dao.RoomDAO;
import com.hotelapp.dao.RoomTypeDAO;
import com.hotelapp.model.Room;
import com.hotelapp.model.RoomType;
import com.hotelapp.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.util.List;

/**
 * Controller untuk dialog (jendela pop-up) tambah atau edit data kamar fisik.
 * Mengelola logika untuk menyimpan atau memperbarui informasi kamar.
 */
public class AddRoomDialogController {

    // @FXML menandakan bahwa variabel ini terhubung ke komponen di file .fxml.
    @FXML private TextField roomNumberField; // Input untuk nomor kamar.
    @FXML private ComboBox<RoomType> roomTypeComboBox; // Pilihan dropdown untuk tipe kamar.
    @FXML private ComboBox<String> statusComboBox; // Pilihan dropdown untuk status kamar.
    @FXML private Button saveButton; // Tombol untuk menyimpan data.

    // Variabel untuk menyimpan data kamar yang sedang diedit. Jika null, berarti sedang menambah kamar baru.
    private Room roomToEdit = null;

    /**
     * Metode ini dijalankan secara otomatis saat dialog pertama kali dimuat.
     * Fungsinya untuk melakukan inisialisasi awal komponen.
     */
    @FXML
    public void initialize() {
        // Memuat daftar tipe kamar dari database untuk ditampilkan di ComboBox.
        loadRoomTypes();
        // Mengisi ComboBox status dengan pilihan yang sudah ditentukan secara manual.
        statusComboBox.setItems(FXCollections.observableArrayList("available", "maintenance", "booked"));
    }

    /**
     * Memuat tipe-tipe kamar dari DAO dan menampilkannya di ComboBox.
     */
    private void loadRoomTypes() {
        // Mengambil semua data RoomType dari database.
        List<RoomType> roomTypes = RoomTypeDAO.getAllRoomTypes();
        // Mengisi ComboBox dengan data yang telah diambil.
        roomTypeComboBox.setItems(FXCollections.observableArrayList(roomTypes));
        // Mengatur bagaimana objek RoomType akan ditampilkan sebagai teks di dalam ComboBox.
        roomTypeComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(RoomType roomType) {
                // Jika objeknya ada, tampilkan namanya. Jika tidak, tampilkan string kosong.
                return roomType == null ? "" : roomType.getName();
            }
            @Override
            public RoomType fromString(String string) {
                // Konversi dari String kembali ke objek tidak diperlukan dalam kasus ini.
                return null;
            }
        });
    }

    /**
     * Metode ini dipanggil untuk mengisi form saat admin ingin MENGEDIT kamar yang ada.
     * @param room Objek kamar yang akan diedit.
     */
    public void initData(Room room) {
        this.roomToEdit = room; // Simpan objek kamar yang akan diedit.
        roomNumberField.setText(String.valueOf(room.getRoomNumber())); // Isi nomor kamar.
        statusComboBox.setValue(room.getStatus()); // Pilih status yang sesuai.
        roomTypeComboBox.setValue(room.getRoomType()); // Pilih tipe kamar yang sesuai.
        saveButton.setText("Update"); // Ubah teks tombol menjadi "Update".
    }

    /**
     * Metode ini dipanggil untuk mengisi form saat admin ingin MENAMBAH kamar baru.
     * @param preselectedRoomType Tipe kamar yang sudah dipilih dari halaman sebelumnya.
     */
    public void initData(RoomType preselectedRoomType) {
        this.roomToEdit = null; // Pastikan roomToEdit null karena ini adalah operasi tambah baru.
        roomTypeComboBox.setValue(preselectedRoomType); // Langsung pilih tipe kamar yang diberikan.
        roomTypeComboBox.setDisable(true); // Nonaktifkan ComboBox agar tipe kamar tidak bisa diubah.
        saveButton.setText("Simpan"); // Atur teks tombol menjadi "Simpan".
    }


    /**
     * Metode ini dieksekusi ketika tombol 'Simpan' atau 'Update' di-klik.
     * @param event Aksi klik tombol.
     */
    @FXML
    private void handleSave(ActionEvent event) {
        // Mengambil nilai dari setiap input field dan menghapus spasi di awal/akhir.
        String roomNumberStr = roomNumberField.getText().trim();
        RoomType selectedRoomType = roomTypeComboBox.getValue();
        String selectedStatus = statusComboBox.getValue();

        // Validasi: periksa apakah semua field sudah diisi.
        if (roomNumberStr.isEmpty() || selectedRoomType == null || selectedStatus == null) {
            AlertHelper.showWarning("Input Tidak Lengkap", "Semua field harus diisi.");
            return; // Hentikan proses jika ada yang kosong.
        }

        try {
            // Ubah input nomor kamar dari String menjadi integer.
            int roomNumber = Integer.parseInt(roomNumberStr);
            boolean success; // Variabel untuk menampung status keberhasilan operasi database.

            // Cek apakah ini operasi TAMBAH BARU atau EDIT.
            if (roomToEdit == null) { // Jika TAMBAH BARU
                // Periksa apakah nomor kamar sudah ada di database untuk mencegah duplikasi.
                if (RoomDAO.roomNumberExists(roomNumber)) {
                    AlertHelper.showError("Gagal", "Nomor kamar " + roomNumber + " sudah ada.");
                    return; // Hentikan proses.
                }
                // Buat objek Room baru dari data input.
                Room newRoom = new Room(0, roomNumber, selectedStatus, selectedRoomType);
                // Panggil DAO untuk menyimpan kamar baru ke database.
                success = RoomDAO.createRoom(newRoom);
            } else { // Jika EDIT
                // Jika nomor kamar diubah, memeriksa apakah nomor baru sudah digunakan kamar lain.
                if (roomNumber != roomToEdit.getRoomNumber() && RoomDAO.roomNumberExists(roomNumber)) {
                    AlertHelper.showError("Gagal", "Nomor kamar " + roomNumber + " sudah digunakan oleh kamar lain.");
                    return; // Hentikan proses.
                }
                // Update data pada objek roomToEdit yang sudah ada.
                roomToEdit.setRoomNumber(roomNumber);
                roomToEdit.setStatus(selectedStatus);
                roomToEdit.setRoomType(selectedRoomType);
                // Memanggil DAO untuk memperbarui data di database.
                success = RoomDAO.updateRoom(roomToEdit);
            }

            // Jika operasi database berhasil (create atau update).
            if (success) {
                AlertHelper.showInformation("Sukses", "Data kamar berhasil disimpan.");
                closeStage(); // Tutup jendela dialog.
            } else {
                AlertHelper.showError("Gagal", "Gagal menyimpan data ke database.");
            }
        } catch (NumberFormatException e) {
            // Tangani jika input nomor kamar bukan angka.
            AlertHelper.showError("Format Salah", "Input untuk 'Nomor Kamar' harus berupa angka.");
        }
    }

    /**
     * Metode yang dijalankan saat tombol 'Batal' di-klik.
     * @param event Aksi klik tombol.
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        closeStage(); // Langsung tutup jendela.
    }

    /**
     * Metode pembantu untuk menutup jendela dialog saat ini.
     */
    private void closeStage() {
        // Mengambil stage (jendela) dari tombol, lalu tutup.
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}