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

public class RoomManagementController {

    @FXML private TableView<RoomType> roomTypeTable;
    @FXML private TableColumn<RoomType, String> typeNameColumn;
    @FXML private TableColumn<RoomType, Double> typePriceColumn;
    @FXML private TableColumn<RoomType, Integer> typeGuestsColumn;
    @FXML private TableColumn<RoomType, String> typeBedInfoColumn;
    @FXML private TableView<Room> roomInstanceTable;
    @FXML private TableColumn<Room, Integer> roomNumberColumn;
    @FXML private TableColumn<Room, String> roomStatusColumn;
    @FXML private Label roomInstanceLabel;
    @FXML private ProgressIndicator instanceLoadingIndicator; // WAJIB: Tambahkan fx:id ini di FXML Anda

    @FXML
    public void initialize() {
        if (instanceLoadingIndicator != null) {
            instanceLoadingIndicator.setVisible(false);
        }

        setupRoomTypeTable();
        setupRoomInstanceTable();
        loadRoomTypes();

        roomTypeTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        loadRoomInstancesInBackground(newSelection);
                    } else {
                        roomInstanceTable.getItems().clear();
                        roomInstanceLabel.setText("Daftar Kamar Fisik (Pilih Tipe Kamar di Atas)");
                    }
                }
        );
    }

    private void setupRoomTypeTable() {
        typeNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typePriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        typeGuestsColumn.setCellValueFactory(new PropertyValueFactory<>("maxGuests"));
        typeBedInfoColumn.setCellValueFactory(new PropertyValueFactory<>("bedInfo"));
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        typePriceColumn.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : currencyFormat.format(price));
            }
        });
    }

    private void setupRoomInstanceTable() {
        roomNumberColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        roomStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadRoomTypes() {
        Task<List<RoomType>> loadTypesTask = new Task<>() {
            @Override
            protected List<RoomType> call() {
                return RoomTypeDAO.getAllRoomTypes();
            }
        };
        loadTypesTask.setOnSucceeded(e -> roomTypeTable.setItems(FXCollections.observableArrayList(loadTypesTask.getValue())));
        loadTypesTask.setOnFailed(e -> {
            e.getSource().getException().printStackTrace();
            AlertHelper.showError("Gagal Memuat", "Tidak dapat mengambil data tipe kamar.");
        });
        new Thread(loadTypesTask).start();
    }

    private void loadRoomInstancesInBackground(RoomType roomType) {
        roomInstanceLabel.setText("Memuat kamar untuk tipe: " + roomType.getName() + "...");
        roomInstanceTable.setItems(FXCollections.emptyObservableList()); // Kosongkan tabel
        if (instanceLoadingIndicator != null) {
            instanceLoadingIndicator.setVisible(true);
        }

        Task<ObservableList<Room>> loadRoomsTask = new Task<>() {
            @Override
            protected ObservableList<Room> call() {
                List<Room> rooms = RoomDAO.getRoomsByTypeId(roomType.getId());
                return FXCollections.observableArrayList(rooms);
            }
        };

        loadRoomsTask.setOnSucceeded(e -> {
            roomInstanceTable.setItems(loadRoomsTask.getValue());
            roomInstanceLabel.setText("Daftar Kamar untuk Tipe: " + roomType.getName());
            if (instanceLoadingIndicator != null) {
                instanceLoadingIndicator.setVisible(false);
            }
        });

        loadRoomsTask.setOnFailed(e -> {
            e.getSource().getException().printStackTrace();
            roomInstanceLabel.setText("Gagal memuat data kamar.");
            if (instanceLoadingIndicator != null) {
                instanceLoadingIndicator.setVisible(false);
            }
            AlertHelper.showError("Error", "Terjadi kesalahan saat mengambil data kamar.");
        });

        new Thread(loadRoomsTask).start();
    }

    @FXML
    private void handleAddNewRoomType(ActionEvent event) {
        openRoomTypeDialog(null);
    }

    @FXML
    private void handleEditRoomType(ActionEvent event) {
        RoomType selectedType = roomTypeTable.getSelectionModel().getSelectedItem();
        if (selectedType == null) {
            AlertHelper.showWarning("Peringatan", "Pilih dulu sebuah Tipe Kamar dari tabel atas untuk diedit.");
            return;
        }
        RoomType detailedRoomType = RoomTypeDAO.getRoomTypeWithFacilitiesById(selectedType.getId());
        openRoomTypeDialog(detailedRoomType);
    }

    @FXML
    private void handleDeleteRoomType(ActionEvent event) {
        RoomType selectedType = roomTypeTable.getSelectionModel().getSelectedItem();
        if (selectedType == null) {
            AlertHelper.showWarning("Peringatan", "Pilih tipe kamar yang ingin dinonaktifkan.");
            return;
        }
        Optional<ButtonType> result = AlertHelper.showConfirmation("Konfirmasi Nonaktifkan",
                "Yakin ingin menonaktifkan tipe kamar '" + selectedType.getName() + "'? Tipe ini tidak akan bisa digunakan untuk reservasi baru.",
                ButtonType.OK, ButtonType.CANCEL);
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (RoomTypeDAO.deleteRoomType(selectedType.getId())) {
                AlertHelper.showInformation("Sukses", "Tipe kamar berhasil dinonaktifkan.");
                loadRoomTypes();
            } else {
                AlertHelper.showError("Gagal", "Gagal menonaktifkan tipe kamar.");
            }
        }
    }

    @FXML
    private void handleAddNewRoom(ActionEvent event) {
        RoomType selectedType = roomTypeTable.getSelectionModel().getSelectedItem();
        if (selectedType == null) {
            AlertHelper.showWarning("Peringatan", "Pilih dulu Tipe Kamar di tabel atas sebelum menambah kamar fisik baru.");
            return;
        }
        openRoomDialog(null, selectedType);
    }

    @FXML
    private void handleEditRoom(ActionEvent event) {
        Room selectedRoom = roomInstanceTable.getSelectionModel().getSelectedItem();
        if (selectedRoom == null) {
            AlertHelper.showWarning("Peringatan", "Pilih kamar yang ingin diedit di tabel bawah.");
            return;
        }
        openRoomDialog(selectedRoom, selectedRoom.getRoomType());
    }

    @FXML
    private void handleDeleteRoom(ActionEvent event) {
        Room selectedRoom = roomInstanceTable.getSelectionModel().getSelectedItem();
        if (selectedRoom == null) {
            AlertHelper.showWarning("Peringatan", "Pilih kamar yang ingin dinonaktifkan.");
            return;
        }
        Optional<ButtonType> result = AlertHelper.showConfirmation("Konfirmasi Nonaktifkan",
                "Yakin ingin menonaktifkan kamar nomor " + selectedRoom.getRoomNumber() + "? Kamar ini tidak akan bisa dipesan lagi.",
                ButtonType.OK, ButtonType.CANCEL);
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (RoomDAO.deleteRoom(selectedRoom.getId())) {
                AlertHelper.showInformation("Sukses", "Kamar berhasil dinonaktifkan.");
                loadRoomInstancesInBackground(selectedRoom.getRoomType());
            } else {
                AlertHelper.showError("Gagal", "Gagal menonaktifkan kamar.");
            }
        }
    }

    private void openRoomTypeDialog(RoomType roomType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelapp/fxml/admin/AddRoomTypeDialog.fxml"));
            Parent root = loader.load();
            AddRoomTypeDialogController controller = loader.getController();
            if (roomType != null) {
                controller.initData(roomType);
            }
            Stage dialogStage = new Stage();
            dialogStage.setTitle(roomType == null ? "Tambah Tipe Kamar Baru" : "Edit Tipe Kamar");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
            loadRoomTypes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openRoomDialog(Room room, RoomType contextRoomType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelapp/fxml/admin/AddRoomDialog.fxml"));
            Parent root = loader.load();
            AddRoomDialogController controller = loader.getController();
            if (room != null) {
                controller.initData(room);
            } else {
                controller.initData(contextRoomType);
            }
            Stage dialogStage = new Stage();
            dialogStage.setTitle(room == null ? "Tambah Kamar Baru untuk Tipe: " + contextRoomType.getName() : "Edit Kamar");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
            loadRoomInstancesInBackground(contextRoomType);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}