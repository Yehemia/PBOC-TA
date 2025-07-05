package com.hotelapp.controller.admin;

import com.hotelapp.dao.UserDAO;
import com.hotelapp.model.User;
import com.hotelapp.util.AlertHelper;
import com.hotelapp.util.Session;
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

/**
 * Controller untuk halaman Manajemen Pengguna.
 * Mengelola tampilan, pencarian, tambah, edit, dan nonaktifkan pengguna.
 */
public class UserManagementController {

    // @FXML menghubungkan variabel ke komponen FXML.
    @FXML private Button addUserButton; // Tombol untuk menambah pengguna baru.
    @FXML private TextField searchField; // Kotak pencarian pengguna.
    @FXML private TableView<User> usersTable; // Tabel untuk menampilkan daftar pengguna.
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> accountStatusColumn;
    @FXML private TableColumn<User, Void> actionColumn; // Kolom untuk tombol aksi (edit/hapus).

    /**
     * Inisialisasi awal saat halaman dimuat.
     */
    @FXML
    public void initialize() {
        setupTableColumns(); // Atur konfigurasi tabel.
        addUserButton.setOnAction(event -> handleAddNewUser()); // Atur aksi tombol tambah user.

        // Tambahkan listener ke kotak pencarian.
        // Setiap kali teks berubah, panggil metode filterUsers.
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterUsers(newValue);

        });
        loadUsers(); // Muat semua pengguna saat pertama kali halaman dibuka.
    }

    /**
     * Memfilter atau memuat pengguna berdasarkan kata kunci dari kotak pencarian.
     * @param keyword Kata kunci untuk pencarian.
     */
    private void filterUsers(String keyword) {
        // Gunakan Task untuk menjalankan query di background thread.
        Task<ObservableList<User>> task = new Task<>() {
            @Override
            protected ObservableList<User> call() throws Exception {
                try {
                    // Jika kata kunci kosong, ambil semua pengguna.
                    if (keyword == null || keyword.trim().isEmpty()) {
                        return FXCollections.observableArrayList(UserDAO.getAllUsers());
                    } else {
                        // Jika ada kata kunci, cari pengguna yang sesuai.
                        return FXCollections.observableArrayList(UserDAO.searchUsers(keyword));
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Gagal memuat data pengguna", e);
                }
            }
        };

        // Setelah task selesai, update tabel dengan hasilnya.
        task.setOnSucceeded(event -> usersTable.setItems(task.getValue()));
        task.setOnFailed(event -> {
            AlertHelper.showError("Gagal Memuat Data", "Tidak dapat mengambil data pengguna dari server.");
            System.err.println("Failed to filter/load users: " + task.getException().getMessage());
        });
        new Thread(task).start();
    }

    /**
     * Mengonfigurasi semua kolom pada tabel pengguna.
     */
    private void setupTableColumns() {
        // Hubungkan setiap kolom dengan properti yang sesuai di objek User.
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        accountStatusColumn.setCellValueFactory(new PropertyValueFactory<>("accountStatus"));
        // Tambahkan kolom kustom untuk tombol aksi.
        addActionButtonsToTable();
    }

    /**
     * Menambahkan kolom "Aksi" yang berisi tombol Edit dan Hapus (nonaktifkan).
     */
    private void addActionButtonsToTable() {
        actionColumn.setCellFactory(param -> new TableCell<>() {
            // Buat tombol dengan ikon dari FontAwesome.
            private final Button editBtn = new Button("", new FontIcon("fa-pencil"));
            private final Button deleteBtn = new Button("", new FontIcon("fa-trash"));
            // Gabungkan tombol dalam satu HBox.
            private final HBox pane = new HBox(5, editBtn, deleteBtn);
            {
                pane.setAlignment(Pos.CENTER);
                editBtn.getStyleClass().add("edit-button");
                deleteBtn.getStyleClass().add("delete-button");
                // Atur aksi untuk setiap tombol.
                editBtn.setOnAction(event -> handleEditUser(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(event -> handleDeleteUser(getTableView().getItems().get(getIndex())));
            }

            // Metode ini dipanggil untuk setiap sel di kolom.
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    // Tombol hapus (nonaktifkan) hanya terlihat jika akun pengguna aktif.
                    deleteBtn.setVisible("active".equalsIgnoreCase(user.getAccountStatus()));
                    setGraphic(pane);
                }
            }
        });
    }

    /**
     * Memuat daftar semua pengguna (wrapper untuk filterUsers dengan string kosong).
     */
    private void loadUsers() {
        filterUsers("");
    }

    /**
     * Menangani aksi klik tombol "Tambah User Baru".
     */
    private void handleAddNewUser() {
        openUserDialog(null); // Buka dialog dalam mode tambah baru.
    }

    /**
     * Menangani aksi klik tombol "Edit" pada baris tabel.
     * @param user Pengguna dari baris yang dipilih.
     */
    private void handleEditUser(User user) {
        openUserDialog(user); // Buka dialog dalam mode edit dengan data pengguna.
    }

    /**
     * Metode pembantu untuk membuka dialog tambah/edit pengguna.
     * @param user Data pengguna (null jika tambah, berisi data jika edit).
     */
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
            dialogStage.showAndWait(); // Tunggu sampai dialog ditutup.
            loadUsers(); // Muat ulang data pengguna setelah dialog ditutup.
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Menangani aksi klik tombol "Hapus" (nonaktifkan) pada baris tabel.
     * @param user Pengguna yang akan dinonaktifkan.
     */
    private void handleDeleteUser(User user) {
        // Jika user sudah tidak aktif, jangan lakukan apa-apa.
        if ("inactive".equalsIgnoreCase(user.getAccountStatus())) {
            AlertHelper.showInformation("Informasi", "User ini sudah berstatus tidak aktif.");
            return;
        }
        // Admin tidak bisa menonaktifkan dirinya sendiri.
        User currentUser = Session.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getId() == user.getId()) {
            AlertHelper.showError("Aksi Ditolak", "Anda tidak dapat menonaktifkan akun Anda sendiri.");
            return;
        }

        // Logika untuk mencegah admin terakhir dinonaktifkan.
        if ("admin".equalsIgnoreCase(user.getRole())) {
            long activeAdminCount = usersTable.getItems().stream()
                    .filter(u -> "admin".equalsIgnoreCase(u.getRole()) && "active".equalsIgnoreCase(u.getAccountStatus()))
                    .count();

            if (activeAdminCount <= 1) {
                AlertHelper.showError("Aksi Ditolak", "Tidak dapat menonaktifkan satu-satunya akun admin yang aktif.");
                return;
            }
        }

        // Tampilkan dialog konfirmasi.
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Nonaktifkan User");
        alert.setHeaderText("Anda akan menonaktifkan user: " + user.getName() + " (Peran: " + user.getRole() + ")");
        alert.setContentText("User ini tidak akan bisa login atau melakukan transaksi lagi. Lanjutkan?");
        alert.initOwner(usersTable.getScene().getWindow());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Jika dikonfirmasi, panggil DAO untuk mengubah status akun menjadi 'inactive'.
            if (UserDAO.deleteUser(user.getId())) {
                AlertHelper.showInformation("Sukses", "User berhasil dinonaktifkan.");
                loadUsers(); // Muat ulang data.
            } else {
                AlertHelper.showError("Gagal", "Terjadi kesalahan saat menonaktifkan user.");
            }
        }
    }
}