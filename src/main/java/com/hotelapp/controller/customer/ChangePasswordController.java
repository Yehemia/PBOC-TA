package com.hotelapp.controller.customer;

import com.hotelapp.dao.UserDAO;
import com.hotelapp.util.AlertHelper;
import com.hotelapp.util.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class ChangePasswordController {

    @FXML
    private PasswordField oldPasswordField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    void handleSave(ActionEvent event) {
        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            AlertHelper.showError("Error", "Semua field harus diisi.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            AlertHelper.showError( "Error", "Password baru dan konfirmasi tidak cocok.");
            return;
        }

        int userId = Session.getInstance().getCurrentUser().getId();
        boolean success = UserDAO.changePassword(userId, oldPassword, newPassword);

        if (success) {
            AlertHelper.showInformation("Sukses", "Password berhasil diubah.");
            closeStage();
        } else {
            AlertHelper.showError("Error", "Gagal mengubah password. Pastikan password lama Anda benar.");
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}