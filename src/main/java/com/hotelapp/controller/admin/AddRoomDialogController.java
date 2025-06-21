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

public class AddRoomDialogController {

    @FXML private TextField roomNumberField;
    @FXML private ComboBox<RoomType> roomTypeComboBox;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private Button saveButton;

    private Room roomToEdit = null;

    @FXML
    public void initialize() {
        loadRoomTypes();
        statusComboBox.setItems(FXCollections.observableArrayList("available", "maintenance", "booked"));
    }

    private void loadRoomTypes() {
        List<RoomType> roomTypes = RoomTypeDAO.getAllRoomTypes();
        roomTypeComboBox.setItems(FXCollections.observableArrayList(roomTypes));
        roomTypeComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(RoomType roomType) {
                return roomType == null ? "" : roomType.getName();
            }
            @Override
            public RoomType fromString(String string) { return null; }
        });
    }

    public void initData(Room room) {
        this.roomToEdit = room;
        roomNumberField.setText(String.valueOf(room.getRoomNumber()));
        statusComboBox.setValue(room.getStatus());
        roomTypeComboBox.setValue(room.getRoomType());
        saveButton.setText("Update");
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String roomNumberStr = roomNumberField.getText().trim();
        RoomType selectedRoomType = roomTypeComboBox.getValue();
        String selectedStatus = statusComboBox.getValue();

        if (roomNumberStr.isEmpty() || selectedRoomType == null || selectedStatus == null) {
            AlertHelper.showWarning("Input Tidak Lengkap", "Semua field harus diisi.");
            return;
        }

        try {
            int roomNumber = Integer.parseInt(roomNumberStr);
            boolean success;

            if (roomToEdit == null) {
                Room newRoom = new Room(0, roomNumber, selectedStatus, selectedRoomType);
                success = RoomDAO.createRoom(newRoom);
            } else {
                roomToEdit.setRoomNumber(roomNumber);
                roomToEdit.setStatus(selectedStatus);
                roomToEdit.setRoomType(selectedRoomType);
                success = RoomDAO.updateRoom(roomToEdit);
            }

            if (success) {
                AlertHelper.showInformation("Sukses", "Data kamar berhasil disimpan.");
                closeStage();
            } else {
                AlertHelper.showError("Gagal", "Gagal menyimpan data. Nomor kamar mungkin sudah ada.");
            }
        } catch (NumberFormatException e) {
            AlertHelper.showError("Error Format", "Nomor kamar harus berupa angka.");
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) { closeStage(); }

    private void closeStage() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}