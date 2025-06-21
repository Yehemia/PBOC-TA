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
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.controlsfx.control.CheckListView;
import java.sql.SQLException;

public class AddRoomTypeDialogController {

    @FXML private Label titleLabel;
    @FXML private TextField nameField, priceField, maxGuestsField, bedInfoField, imageUrlField;
    @FXML private TextArea descriptionArea;
    @FXML private CheckListView<Facility> facilitiesListView;
    @FXML private Button saveButton;

    private RoomType roomTypeToEdit = null;
    private final RoomTypeService roomTypeService = new RoomTypeService();

    @FXML
    public void initialize() {
        ObservableList<Facility> allFacilities = FXCollections.observableArrayList(FacilityDAO.getAllFacilities());
        facilitiesListView.setItems(allFacilities);
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
        imageUrlField.setText(roomType.getImageUrl());
        for (Facility facility : roomType.getFacilities()) {
            facilitiesListView.getCheckModel().check(facility);
        }
    }

    @FXML
    private void handleSave() {
        if (nameField.getText().isBlank() || priceField.getText().isBlank()) {
            AlertHelper.showWarning("Input Tidak Lengkap", "Nama dan Harga Tipe Kamar wajib diisi.");
            return;
        }

        try {
            String name = nameField.getText();
            double price = Double.parseDouble(priceField.getText());
            int maxGuests = Integer.parseInt(maxGuestsField.getText());
            String description = descriptionArea.getText();
            String bedInfo = bedInfoField.getText();
            String imageUrl = imageUrlField.getText();


            if (roomTypeToEdit == null) {
                roomTypeToEdit = new RoomType(0, name, price, description, maxGuests, bedInfo, imageUrl);
            } else {
                roomTypeToEdit.setName(name);
                roomTypeToEdit.setPrice(price);
            }
            ObservableList<Facility> selectedFacilities = facilitiesListView.getCheckModel().getCheckedItems();

            roomTypeService.saveRoomType(roomTypeToEdit, selectedFacilities);

            AlertHelper.showInformation("Sukses", "Data Tipe Kamar berhasil disimpan.");
            closeStage();

        } catch (NumberFormatException e) {
            AlertHelper.showError("Format Salah", "Harga dan Kapasitas Tamu harus berupa angka.");
        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Gagal menyimpan data ke database. Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() { closeStage(); }

    private void closeStage() {
        ((Stage) saveButton.getScene().getWindow()).close();
    }
}