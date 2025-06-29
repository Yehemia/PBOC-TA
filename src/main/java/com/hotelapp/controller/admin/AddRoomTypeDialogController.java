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

public class AddRoomTypeDialogController {

    @FXML private Label titleLabel;
    @FXML private TextField nameField, priceField, maxGuestsField, bedInfoField;
    @FXML private TextArea descriptionArea;
    @FXML private CheckListView<Facility> facilitiesListView;
    @FXML private Button saveButton;
    @FXML private Label imageNameLabel;

    private RoomType roomTypeToEdit = null;
    private final RoomTypeService roomTypeService = new RoomTypeService();
    private File selectedImageFile;

    @FXML
    public void initialize() {
        List<Facility> allFacilities = FacilityDAO.getAllFacilities();
        if (allFacilities != null) {
            facilitiesListView.setItems(FXCollections.observableArrayList(allFacilities));
        }
    }

    public void initData(RoomType roomType) {
        this.roomTypeToEdit = roomType;
        titleLabel.setText("Edit Tipe Kamar");
        saveButton.setText("Update");

        nameField.setText(roomType.getName());
        priceField.setText(String.valueOf(roomType.getPrice()));
        descriptionArea.setText(roomType.getDescription());
        maxGuestsField.setText(String.valueOf(roomType.getMaxGuests()));
        bedInfoField.setText(roomType.getBedInfo());

        if (roomType.getImageUrl() != null && !roomType.getImageUrl().isEmpty()) {
            try {
                URI uri = new URI(roomType.getImageUrl());
                String path = uri.getPath();
                imageNameLabel.setText(path.substring(path.lastIndexOf('/') + 1));
            } catch (Exception e) {
                imageNameLabel.setText(roomType.getImageUrl());
            }
        }
        if (roomType.getFacilities() != null) {
            for (Facility facility : roomType.getFacilities()) {
                facilitiesListView.getCheckModel().check(facility);
            }
        }
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih Gambar Kamar");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(saveButton.getScene().getWindow());
        if (file != null) {
            selectedImageFile = file;
            imageNameLabel.setText(selectedImageFile.getName());
        }
    }

    @FXML
    private void handleSave() {
        if (nameField.getText().isBlank() || priceField.getText().isBlank() || maxGuestsField.getText().isBlank()) {
            AlertHelper.showWarning("Input Tidak Lengkap", "Nama, Harga, dan Kapasitas Tamu wajib diisi.");
            return;
        }

        try {
            double price = Double.parseDouble(priceField.getText());
            int maxGuests = Integer.parseInt(maxGuestsField.getText());

            String imageUrlForDatabase = null;
            if (selectedImageFile != null) {
                imageUrlForDatabase = uploadImageToServer(selectedImageFile);
            } else if (roomTypeToEdit != null) {
                imageUrlForDatabase = roomTypeToEdit.getImageUrl();
            }

            RoomType roomTypeToSave = (roomTypeToEdit == null) ? new RoomType(0, null, 0, null, 0, null, null) : roomTypeToEdit;

            roomTypeToSave.setName(nameField.getText());
            roomTypeToSave.setPrice(price);
            roomTypeToSave.setDescription(descriptionArea.getText());
            roomTypeToSave.setMaxGuests(maxGuests);
            roomTypeToSave.setBedInfo(bedInfoField.getText());
            roomTypeToSave.setImageUrl(imageUrlForDatabase);

            ObservableList<Facility> selectedFacilities = facilitiesListView.getCheckModel().getCheckedItems();
            roomTypeService.saveRoomType(roomTypeToSave, selectedFacilities);

            AlertHelper.showInformation("Sukses", "Data Tipe Kamar berhasil disimpan.");
            closeStage();

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

    private String uploadImageToServer(File imageFile) throws IOException, InterruptedException {
        String uploadUrl = "https://agaress.xyz/upload_handler.php";
        HttpClient client = HttpClient.newHttpClient();
        String boundary = "Boundary-" + System.currentTimeMillis();
        Path path = imageFile.toPath();
        String mimeType = Files.probeContentType(path);
        byte[] fileBytes = Files.readAllBytes(path);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uploadUrl))
                .header("Content-Type", "multipart/form-data;boundary=" + boundary)
                .POST(ofMimeMultipartData(imageFile.getName(), mimeType, fileBytes, boundary))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            JSONObject jsonResponse = new JSONObject(response.body());
            if ("success".equals(jsonResponse.optString("status"))) {
                return jsonResponse.optString("url");
            } else {
                System.err.println("Server mengembalikan error: " + jsonResponse.optString("message"));
            }
        } else {
            System.err.println("Gagal upload, status code: " + response.statusCode());
        }
        return null;
    }

    private HttpRequest.BodyPublisher ofMimeMultipartData(String fileName, String mimeType, byte[] fileBytes, String boundary) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String CRLF = "\r\n";
        String twoHyphens = "--";
        baos.write((twoHyphens + boundary + CRLF).getBytes(StandardCharsets.UTF_8));
        baos.write(("Content-Disposition: form-data; name=\"imageFile\"; filename=\"" + fileName + "\"" + CRLF)
                .getBytes(StandardCharsets.UTF_8));
        baos.write(("Content-Type: " + mimeType + CRLF).getBytes(StandardCharsets.UTF_8));
        baos.write(CRLF.getBytes(StandardCharsets.UTF_8));
        baos.write(fileBytes);
        baos.write(CRLF.getBytes(StandardCharsets.UTF_8));
        baos.write((twoHyphens + boundary + twoHyphens + CRLF).getBytes(StandardCharsets.UTF_8));
        return HttpRequest.BodyPublishers.ofByteArray(baos.toByteArray());
    }

    @FXML
    private void handleCancel() {
        closeStage();
    }

    private void closeStage() {
        ((Stage) saveButton.getScene().getWindow()).close();
    }
}