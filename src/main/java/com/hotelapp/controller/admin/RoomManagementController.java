package com.hotelapp.controller.admin;

import com.hotelapp.dao.RoomDAO;
import com.hotelapp.model.Room;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.util.Optional;

public class RoomManagementController {

    @FXML private Button addRoomButton;
    @FXML private TextField searchField; // Pastikan fx:id ini ada di FXML
    @FXML private TableView<Room> roomsTable;
    @FXML private TableColumn<Room, Integer> idColumn;
    @FXML private TableColumn<Room, Integer> roomNumberColumn;
    @FXML private TableColumn<Room, String> roomTypeColumn;
    @FXML private TableColumn<Room, Double> priceColumn;
    @FXML private TableColumn<Room, String> statusColumn;
    @FXML private TableColumn<Room, Void> actionColumn;

    @FXML
    public void initialize() {
        setupTableColumns();
        addRoomButton.setOnAction(event -> handleAddNewRoom());

        // --- LOGIKA PENCARIAN DITAMBAHKAN DI SINI ---
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterRooms(newValue);
        });

        loadRooms();
    }

    private void filterRooms(String keyword) {
        Task<ObservableList<Room>> task = new Task<>() {
            @Override
            protected ObservableList<Room> call() {
                if (keyword == null || keyword.trim().isEmpty()) {
                    return FXCollections.observableArrayList(RoomDAO.getAllRooms());
                } else {
                    return FXCollections.observableArrayList(RoomDAO.searchRooms(keyword));
                }
            }
        };

        task.setOnSucceeded(event -> roomsTable.setItems(task.getValue()));
        task.setOnFailed(event -> event.getSource().getException().printStackTrace());

        new Thread(task).start();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        roomNumberColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        roomTypeColumn.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        addActionButtonsToTable();
    }

    private void addActionButtonsToTable() {
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("", new FontIcon("fa-pencil"));
            private final Button deleteBtn = new Button("", new FontIcon("fa-trash"));
            private final HBox pane = new HBox(5, editBtn, deleteBtn);
            {
                pane.setAlignment(Pos.CENTER);
                editBtn.getStyleClass().add("edit-button");
                deleteBtn.getStyleClass().add("delete-button");
                editBtn.setOnAction(event -> handleEditRoom(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(event -> handleDeleteRoom(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadRooms() {
        // Panggil filterRooms dengan string kosong untuk memuat semua data awal
        filterRooms("");
    }

    private void handleAddNewRoom() {
        openRoomDialog(null);
    }

    private void handleEditRoom(Room room) {
        openRoomDialog(room);
    }

    private void openRoomDialog(Room room) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelapp/fxml/admin/AddRoomDialog.fxml"));
            Parent root = loader.load();
            AddRoomDialogController controller = loader.getController();
            if (room != null) {
                controller.initData(room);
            }
            Stage dialogStage = new Stage();
            dialogStage.setTitle(room == null ? "Tambah Kamar Baru" : "Edit Kamar");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(roomsTable.getScene().getWindow());
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/hotelapp/styles/admin-style.css").toExternalForm());
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            loadRooms();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteRoom(Room room) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Hapus");
        alert.setHeaderText("Anda akan menghapus Kamar Nomor: " + room.getRoomNumber());
        alert.setContentText("Apakah Anda yakin?");
        alert.initOwner(roomsTable.getScene().getWindow());
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (RoomDAO.deleteRoom(room.getId())) {
                loadRooms();
            }
        }
    }
}