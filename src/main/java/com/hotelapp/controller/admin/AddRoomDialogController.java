package com.hotelapp.controller.admin;

import com.hotelapp.dao.RoomDAO;
import com.hotelapp.model.Room;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AddRoomDialogController {

    @FXML private TextField roomNumberField;
    @FXML private ComboBox<String> roomTypeComboBox;
    @FXML private TextField priceField;
    @FXML private TextField imageUrlField;
    @FXML private Button saveButton;

    // Variabel untuk membedakan mode Add atau Edit
    private Room roomToEdit = null;

    @FXML
    public void initialize() {
        roomTypeComboBox.setItems(FXCollections.observableArrayList("Standard", "Deluxe", "Suite"));
    }

    // Metode ini akan dipanggil untuk mengisi form saat mode Edit
    public void initData(Room room) {
        this.roomToEdit = room;

        roomNumberField.setText(String.valueOf(room.getRoomNumber()));
        roomTypeComboBox.setValue(room.getRoomType());
        priceField.setText(String.valueOf(room.getPrice()));
        imageUrlField.setText(room.getImageUrl());

        saveButton.setText("Update Kamar");
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String roomNumberStr = roomNumberField.getText().trim();
        String priceStr = priceField.getText().trim();
        String roomType = roomTypeComboBox.getValue();
        String imageUrl = imageUrlField.getText().trim();

        if (roomNumberStr.isEmpty() || priceStr.isEmpty() || roomType == null) {
            showAlert(Alert.AlertType.ERROR, "Error Validasi", "Nomor kamar, tipe, dan harga harus diisi.");
            return;
        }

        try {
            int roomNumber = Integer.parseInt(roomNumberStr);
            double price = Double.parseDouble(priceStr);

            boolean success;

            if (roomToEdit == null) {
                // --- Mode Tambah Kamar Baru ---
                if (RoomDAO.roomNumberExists(roomNumber)) {
                    showAlert(Alert.AlertType.ERROR, "Error Duplikat", "Nomor kamar " + roomNumber + " sudah terdaftar.");
                    return;
                }
                Room newRoom = new Room(roomNumber, roomType, price, imageUrl);
                success = RoomDAO.createRoom(newRoom);
            } else {
                // --- Mode Edit Kamar ---
                // Cek jika nomor kamar diubah dan nomor baru itu sudah ada (milik kamar lain)
                if (roomNumber != roomToEdit.getRoomNumber() && RoomDAO.roomNumberExists(roomNumber)) {
                    showAlert(Alert.AlertType.ERROR, "Error Duplikat", "Nomor kamar " + roomNumber + " sudah digunakan oleh kamar lain.");
                    return;
                }
                roomToEdit.setRoomNumber(roomNumber);
                roomToEdit.setRoomType(roomType);
                roomToEdit.setPrice(price);
                roomToEdit.setImageUrl(imageUrl);
                success = RoomDAO.updateRoom(roomToEdit);
            }


            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data kamar berhasil disimpan.");
                closeStage();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error Database", "Gagal menyimpan data kamar.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error Format", "Nomor kamar dan harga harus berupa angka.");
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}