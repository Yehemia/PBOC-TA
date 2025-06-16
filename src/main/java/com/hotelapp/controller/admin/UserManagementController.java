package com.hotelapp.controller.admin;

import com.hotelapp.dao.UserDAO;
import com.hotelapp.model.User;
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

public class UserManagementController {

    @FXML private Button addUserButton;
    @FXML private TextField searchField; // Pastikan fx:id ini ada di FXML
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, Void> actionColumn;

    @FXML
    public void initialize() {
        setupTableColumns();
        addUserButton.setOnAction(event -> handleAddNewUser());

        // --- LOGIKA PENCARIAN DITAMBAHKAN DI SINI ---
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterUsers(newValue);
        });

        loadUsers();
    }

    private void filterUsers(String keyword) {
        Task<ObservableList<User>> task = new Task<>() {
            @Override
            protected ObservableList<User> call() {
                if (keyword == null || keyword.trim().isEmpty()) {
                    // Jika keyword kosong, tampilkan semua user
                    return FXCollections.observableArrayList(UserDAO.getAllUsers());
                } else {
                    // Jika ada keyword, panggil metode search
                    return FXCollections.observableArrayList(UserDAO.searchUsers(keyword));
                }
            }
        };

        task.setOnSucceeded(event -> usersTable.setItems(task.getValue()));
        task.setOnFailed(event -> event.getSource().getException().printStackTrace());

        new Thread(task).start();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
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
                editBtn.setOnAction(event -> handleEditUser(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(event -> handleDeleteUser(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadUsers() {
        // Panggil filterUsers dengan string kosong untuk memuat semua data awal
        filterUsers("");
    }

    private void handleAddNewUser() {
        openUserDialog(null);
    }

    private void handleEditUser(User user) {
        openUserDialog(user);
    }

    private void openUserDialog(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelapp/fxml/admin/AddUserDialog.fxml"));
            Parent root = loader.load();
            AddUserDialogController controller = loader.getController();
            if (user != null) {
                controller.initData(user);
            }
            Stage dialogStage = new Stage();
            dialogStage.setTitle(user == null ? "Tambah User Baru" : "Edit User");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(usersTable.getScene().getWindow());
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/hotelapp/styles/admin-style.css").toExternalForm());
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            loadUsers();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Hapus");
        alert.setHeaderText("Anda akan menghapus user: " + user.getName());
        alert.setContentText("Apakah Anda yakin?");
        alert.initOwner(usersTable.getScene().getWindow());
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (UserDAO.deleteUser(user.getId())) {
                loadUsers();
            }
        }
    }
}