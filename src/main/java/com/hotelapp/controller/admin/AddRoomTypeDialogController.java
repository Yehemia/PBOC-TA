package com.hotelapp.controller.admin;

import com.hotelapp.dao.FacilityDAO;
import com.hotelapp.model.Facility;
import com.hotelapp.model.RoomType;
import com.hotelapp.service.RoomTypeService;
import com.hotelapp.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.controlsfx.control.CheckListView;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller untuk dialog tambah atau edit Tipe Kamar.
 * Mengelola input data, pemilihan fasilitas, dan upload gambar.
 */
public class AddRoomTypeDialogController {

    // @FXML menghubungkan variabel dengan komponen dari file FXML.
    @FXML private Label titleLabel; // Judul dialog, e.g., "Tambah Tipe Kamar".
    @FXML private TextField nameField, priceField, maxGuestsField, bedInfoField; // Input untuk detail tipe kamar.
    @FXML private TextArea descriptionArea; // Input untuk deskripsi panjang.
    @FXML private CheckListView<Facility> facilitiesListView; // Daftar centang untuk memilih fasilitas.
    @FXML private Button saveButton; // Tombol simpan.
    @FXML private Label imageNameLabel; // Label untuk menampilkan nama file gambar yang dipilih.

    private RoomType roomTypeToEdit = null; // Menyimpan objek RoomType saat mode edit.
    private final RoomTypeService roomTypeService = new RoomTypeService(); // Service untuk logika bisnis tipe kamar.
    private File selectedImageFile; // Menyimpan file gambar yang dipilih oleh admin.

    /**
     * Inisialisasi awal saat dialog dimuat.
     */
    @FXML
    public void initialize() {
        // Mengambil semua fasilitas dari database.
        List<Facility> allFacilities = FacilityDAO.getAllFacilities();
        if (allFacilities != null) {
            // Menampilkan semua fasilitas dalam CheckListView.
            facilitiesListView.setItems(FXCollections.observableArrayList(allFacilities));
        }
    }

    /**
     * Mengisi form dengan data saat mode EDIT.
     * @param roomType Objek tipe kamar yang akan diedit.
     */
    public void initData(RoomType roomType) {
        this.roomTypeToEdit = roomType;
        titleLabel.setText("Edit Tipe Kamar"); // Ubah judul.
        saveButton.setText("Update"); // Ubah teks tombol.

        // Isi semua field dengan data yang ada.
        nameField.setText(roomType.getName());
        priceField.setText(String.valueOf(roomType.getPrice()));
        descriptionArea.setText(roomType.getDescription());
        maxGuestsField.setText(String.valueOf(roomType.getMaxGuests()));
        bedInfoField.setText(roomType.getBedInfo());

        // Tampilkan nama file dari URL gambar yang sudah ada.
        if (roomType.getImageUrl() != null && !roomType.getImageUrl().isEmpty()) {
            try {
                URI uri = new URI(roomType.getImageUrl());
                String path = uri.getPath();
                imageNameLabel.setText(path.substring(path.lastIndexOf('/') + 1));
            } catch (Exception e) {
                imageNameLabel.setText(roomType.getImageUrl());
            }
        }

        // Centang fasilitas yang sudah dimiliki oleh tipe kamar ini.
        if (roomType.getFacilities() != null) {
            for (Facility facility : roomType.getFacilities()) {
                facilitiesListView.getCheckModel().check(facility);
            }
        }
    }

    /**
     * Membuka dialog FileChooser untuk memilih gambar.
     */
    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih Gambar Kamar");
        // Batasi pilihan file hanya untuk format gambar.
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(saveButton.getScene().getWindow());
        if (file != null) {
            selectedImageFile = file; // Simpan file yang dipilih.
            imageNameLabel.setText(selectedImageFile.getName()); // Tampilkan nama filenya.
        }
    }

    /**
     * Dijalankan saat tombol Simpan/Update diklik.
     */
    @FXML
    private void handleSave() {
        // Validasi input dasar.
        if (nameField.getText().isBlank() || priceField.getText().isBlank() || maxGuestsField.getText().isBlank()) {
            AlertHelper.showWarning("Input Tidak Lengkap", "Nama, Harga, dan Kapasitas Tamu wajib diisi.");
            return;
        }

        try {
            // Konversi input teks menjadi tipe data yang benar.
            double price = Double.parseDouble(priceField.getText());
            int maxGuests = Integer.parseInt(maxGuestsField.getText());

            String imageUrlForDatabase = null;
            // Jika ada gambar baru yang dipilih, upload ke server.
            if (selectedImageFile != null) {
                imageUrlForDatabase = uploadImageToServer(selectedImageFile);
            } else if (roomTypeToEdit != null) {
                // Jika tidak ada gambar baru (mode edit), gunakan URL gambar yang lama.
                imageUrlForDatabase = roomTypeToEdit.getImageUrl();
            }

            // Tentukan apakah membuat objek baru atau menggunakan yang sudah ada.
            RoomType roomTypeToSave = (roomTypeToEdit == null) ? new RoomType(0, null, 0, null, 0, null, null) : roomTypeToEdit;

            // Set semua properti objek dengan data dari form.
            roomTypeToSave.setName(nameField.getText());
            roomTypeToSave.setPrice(price);
            roomTypeToSave.setDescription(descriptionArea.getText());
            roomTypeToSave.setMaxGuests(maxGuests);
            roomTypeToSave.setBedInfo(bedInfoField.getText());
            roomTypeToSave.setImageUrl(imageUrlForDatabase);

            // Ambil daftar fasilitas yang dicentang.
            ObservableList<Facility> selectedFacilities = facilitiesListView.getCheckModel().getCheckedItems();
            // Panggil service untuk menyimpan data tipe kamar beserta fasilitasnya.
            roomTypeService.saveRoomType(roomTypeToSave, selectedFacilities);

            AlertHelper.showInformation("Sukses", "Data Tipe Kamar berhasil disimpan.");
            closeStage(); // Tutup dialog jika berhasil.

        } catch (NumberFormatException e) {
            AlertHelper.showError("Format Salah", "Input untuk 'Harga' dan 'Kapasitas Tamu' harus berupa angka.");
        } catch (ConnectException | HttpTimeoutException e) {
            AlertHelper.showError("Upload Error", "Tidak dapat terhubung ke server upload gambar. Periksa koneksi internet Anda.");
            System.err.println("Image upload connection error: " + e.getMessage());
        } catch (IOException | InterruptedException e) {
            AlertHelper.showError("Upload Gagal", "Terjadi kesalahan saat meng-upload gambar: " + e.getMessage());
            System.err.println("Image upload failed: " + e.getMessage());
        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Gagal menyimpan data ke database. Pastikan tidak ada data duplikat.");
            System.err.println("Database error on save room type: " + e.getMessage());
        }
    }

    /**
     * Mengirim file gambar ke server eksternal (PHP script) menggunakan HTTP POST.
     * @param imageFile File gambar yang akan di-upload.
     * @return URL dari gambar yang telah di-upload.
     * @throws IOException, InterruptedException Jika terjadi error saat upload.
     */
    private String uploadImageToServer(File imageFile) throws IOException, InterruptedException {
        String uploadUrl = "https://agaress.xyz/upload_handler.php"; // URL script upload.
        HttpClient client = HttpClient.newHttpClient();
        String boundary = "Boundary-" + System.currentTimeMillis(); // Boundary unik untuk request multipart.
        Path path = imageFile.toPath();
        String mimeType = Files.probeContentType(path); // Deteksi tipe MIME file.
        byte[] fileBytes = Files.readAllBytes(path); // Baca file menjadi byte array.

        // Buat HTTP Request dengan header dan body yang sesuai untuk upload file.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uploadUrl))
                .header("Content-Type", "multipart/form-data;boundary=" + boundary)
                .POST(ofMimeMultipartData(imageFile.getName(), mimeType, fileBytes, boundary))
                .build();

        // Kirim request dan dapatkan response dari server.
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Jika response sukses (kode 200), proses JSON yang diterima.
        if (response.statusCode() == 200) {
            JSONObject jsonResponse = new JSONObject(response.body());
            if ("success".equals(jsonResponse.optString("status"))) {
                return jsonResponse.optString("url"); // Kembalikan URL gambar.
            } else {
                System.err.println("Server mengembalikan error: " + jsonResponse.optString("message"));
            }
        } else {
            System.err.println("Gagal upload, status code: " + response.statusCode());
        }
        return null;
    }

    /**
     * Membangun body request untuk pengiriman data multipart (file).
     */
    private HttpRequest.BodyPublisher ofMimeMultipartData(String fileName, String mimeType, byte[] fileBytes, String boundary) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String CRLF = "\r\n";
        String twoHyphens = "--";
        // Bagian header untuk data file.
        baos.write((twoHyphens + boundary + CRLF).getBytes(StandardCharsets.UTF_8));
        baos.write(("Content-Disposition: form-data; name=\"imageFile\"; filename=\"" + fileName + "\"" + CRLF)
                .getBytes(StandardCharsets.UTF_8));
        baos.write(("Content-Type: " + mimeType + CRLF).getBytes(StandardCharsets.UTF_8));
        baos.write(CRLF.getBytes(StandardCharsets.UTF_8));
        // Bagian data file itu sendiri.
        baos.write(fileBytes);
        baos.write(CRLF.getBytes(StandardCharsets.UTF_8));
        // Bagian akhir dari request.
        baos.write((twoHyphens + boundary + twoHyphens + CRLF).getBytes(StandardCharsets.UTF_8));
        return HttpRequest.BodyPublishers.ofByteArray(baos.toByteArray());
    }

    /**
     * Menangani aksi klik tombol Batal.
     */
    @FXML
    private void handleCancel() {
        closeStage();
    }

    /**
     * Metode pembantu untuk menutup dialog.
     */
    private void closeStage() {
        ((Stage) saveButton.getScene().getWindow()).close();
    }
}