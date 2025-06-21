package com.hotelapp.controller.customer;

import com.hotelapp.model.User;
import com.hotelapp.util.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class ProfileController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private Button updateButton;

    @FXML
    public void initialize() {
        User currentUser = Session.getInstance().getCurrentUser();
        if (currentUser != null) {
            nameField.setText(currentUser.getName());
            emailField.setText(currentUser.getEmail());
            usernameField.setText(currentUser.getUsername());
        }

        updateButton.setOnAction(event -> updateProfile());
    }

    private void updateProfile() {
        User currentUser = Session.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUser.setName(nameField.getText());
            currentUser.setEmail(emailField.getText());
            currentUser.setUsername(usernameField.getText());

            boolean updated = com.hotelapp.dao.UserDAO.updateUser(currentUser);
            if (updated) {
                System.out.println("Profile berhasil diperbarui!");
            } else {
                System.err.println("Gagal memperbarui profile.");
            }
        }
    }
}