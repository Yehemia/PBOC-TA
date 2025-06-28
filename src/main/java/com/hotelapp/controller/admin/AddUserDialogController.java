package com.hotelapp.controller.admin;

import com.hotelapp.dao.UserDAO;
import com.hotelapp.model.User;
import com.hotelapp.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AddUserDialogController {

    @FXML private TextField usernameField;
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Button saveButton;
    private User userToEdit = null;

    @FXML
    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList("customer", "receptionist", "admin"));
    }

    public void initData(User user) {
        this.userToEdit = user;

        usernameField.setText(user.getUsername());
        nameField.setText(user.getName());
        emailField.setText(user.getEmail());
        roleComboBox.setValue(user.getRole());

        passwordField.setPromptText("Kosongkan jika tidak ingin diubah");
        passwordField.setDisable(true);
        passwordField.setManaged(false);
        passwordField.setVisible(false);

        saveButton.setText("Update User");
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String username = usernameField.getText().trim();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String role = roleComboBox.getValue();

        if (username.isEmpty() || name.isEmpty() || email.isEmpty() || role == null) {
            AlertHelper.showWarning("Input Tidak Lengkap", "Semua field (kecuali password) wajib diisi.");
            return;
        }

        boolean success;
        if (userToEdit == null) {
            String password = passwordField.getText();
            if (password.isEmpty()) {
                AlertHelper.showWarning("Input Tidak Lengkap", "Password wajib diisi untuk user baru.");
                return;
            }
            success = UserDAO.registerUser(username, name, email, password, role);
        } else {
            userToEdit.setUsername(username);
            userToEdit.setName(name);
            userToEdit.setEmail(email);
            userToEdit.setRole(role);
            success = UserDAO.updateUser(userToEdit);
        }

        if (success) {
            AlertHelper.showInformation("Sukses", "Data user berhasil disimpan.");
            closeStage();
        } else {
            AlertHelper.showError("Gagal", "Gagal menyimpan data. Username atau email mungkin sudah terdaftar.");
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
}