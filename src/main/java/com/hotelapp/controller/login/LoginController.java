package com.hotelapp.controller.login;

import com.hotelapp.dao.UserDAO;
import com.hotelapp.util.Session;
import com.hotelapp.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import java.io.IOException;


public class LoginController {
    @FXML
    private ImageView logoImageView;
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;
    @FXML private Hyperlink registerLink;

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isBlank() || password.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Input Tidak Valid", "Email dan Password tidak boleh kosong.");
            return;
        }

        User user = UserDAO.authenticate(username, password);
        if (user != null) {
            System.out.println("Login sukses! Role: " + user.getRole());
            Session.getInstance().setCurrentUser(user);

            redirectUser(user.getRole());
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Gagal", "Email atau Password yang Anda masukkan salah.");
        }
    }

    @FXML
    public void handleShowRegister(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/hotelapp/fxml/register.fxml")
            );
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 920, 710));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void redirectUser(String role) {
        try {
            String fxmlFile = switch (role.toLowerCase()) {
                case "customer" -> "/com/hotelapp/fxml/customer/dashboard_customer.fxml";
                case "receptionist" -> "/com/hotelapp/fxml/resepsionis/ReceptionistDashboard.fxml";
                case "admin" -> "/com/hotelapp/fxml/dashboard_admin.fxml";
                default -> null;
            };

            if (fxmlFile != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                Parent root = loader.load();
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
                stage.show();
            } else {
                System.out.println("Role tidak dikenali!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}