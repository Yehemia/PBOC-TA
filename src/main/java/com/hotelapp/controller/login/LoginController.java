package com.hotelapp.controller.login;

import com.hotelapp.dao.UserDAO;
import com.hotelapp.util.AlertHelper;
import com.hotelapp.util.Session;
import com.hotelapp.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Hyperlink registerLink;
    @FXML private Hyperlink forgotPasswordLink;

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isBlank() || password.isBlank()) {
            AlertHelper.showWarning("Input Tidak Valid", "Username dan Password tidak boleh kosong.");
            return;
        }

        User user = UserDAO.authenticate(username, password);
        if (user != null) {
            Session.getInstance().setCurrentUser(user);
            redirectUser(user.getRole());
        } else {
            AlertHelper.showError("Login Gagal", "Username atau Password salah, atau akun Anda tidak aktif.");
        }
    }

    @FXML
    public void handleShowRegister(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/hotelapp/fxml/register.fxml"));
            Stage stage = (Stage) registerLink.getScene().getWindow();
            stage.setScene(new Scene(root, 920, 710));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleForgotPassword(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/hotelapp/fxml/ForgotPassword.fxml"));
            Stage stage = (Stage) forgotPasswordLink.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void redirectUser(String role) {
        try {
            String fxmlFile = switch (role.toLowerCase()) {
                case "customer" -> "/com/hotelapp/fxml/customer/dashboard_customer.fxml";
                case "receptionist" -> "/com/hotelapp/fxml/resepsionis/ReceptionistDashboard.fxml";
                case "admin" -> "/com/hotelapp/fxml/admin/AdminDashboard.fxml";
                default -> null;
            };

            if (fxmlFile != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                Parent root = loader.load();
                Stage stage = (Stage) usernameField.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.centerOnScreen();
                stage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}