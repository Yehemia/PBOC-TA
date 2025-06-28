package com.hotelapp.controller.admin;

import com.hotelapp.dao.UserDAO;
import com.hotelapp.model.User;
import com.hotelapp.util.AlertHelper;
import com.hotelapp.util.Session; // Pastikan import ini ditambahkan
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
    @FXML private TextField searchField;
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> accountStatusColumn;
    @FXML private TableColumn<User, Void> actionColumn;

    @FXML
    public void initialize() {
        setupTableColumns();
        addUserButton.setOnAction(event -> handleAddNewUser());
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
                    return FXCollections.observableArrayList(UserDAO.getAllUsers());
                } else {
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
        accountStatusColumn.setCellValueFactory(new PropertyValueFactory<>("accountStatus"));
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
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    deleteBtn.setVisible("active".equalsIgnoreCase(user.getAccountStatus()));
                    setGraphic(pane);
                }
            }
        });
    }

    private void loadUsers() {
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
        // jika user sudah tidak aktif, tidk akan diproses
        if ("inactive".equalsIgnoreCase(user.getAccountStatus())) {
            AlertHelper.showInformation("Informasi", "User ini sudah berstatus tidak aktif.");
            return;
        }

        // tidak mengizinkan admin menonaktifkan akunnya sendiri
        User currentUser = Session.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getId() == user.getId()) {
            AlertHelper.showError("Aksi Ditolak", "Anda tidak dapat menonaktifkan akun Anda sendiri.");
            return;
        }

        // menonaktifkan admin dan memastikan bukan admin terakhir
        if ("admin".equalsIgnoreCase(user.getRole())) {
            long activeAdminCount = usersTable.getItems().stream()
                    .filter(u -> "admin".equalsIgnoreCase(u.getRole()) && "active".equalsIgnoreCase(u.getAccountStatus()))
                    .count();

            if (activeAdminCount <= 1) {
                AlertHelper.showError("Aksi Ditolak", "Tidak dapat menonaktifkan satu-satunya akun admin yang aktif.");
                return;
            }
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Nonaktifkan User");
        alert.setHeaderText("Anda akan menonaktifkan user: " + user.getName() + " (Peran: " + user.getRole() + ")");
        alert.setContentText("User ini tidak akan bisa login atau melakukan transaksi lagi. Lanjutkan?");
        alert.initOwner(usersTable.getScene().getWindow());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (UserDAO.deleteUser(user.getId())) {
                AlertHelper.showInformation("Sukses", "User berhasil dinonaktifkan.");
                loadUsers();
            } else {
                AlertHelper.showError("Gagal", "Terjadi kesalahan saat menonaktifkan user.");
            }
        }
    }
}