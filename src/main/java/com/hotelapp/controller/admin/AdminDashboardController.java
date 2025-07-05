package com.hotelapp.controller.admin;

import com.hotelapp.util.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Controller utama untuk Dasbor Admin.
 * Bertindak sebagai kerangka utama yang mengatur navigasi dan konten.
 */
public class AdminDashboardController {

    // @FXML menghubungkan variabel ke komponen FXML.
    @FXML private BorderPane mainPane; // Layout utama dasbor.
    @FXML private AnchorPane contentPane; // Area di tengah untuk menampilkan konten dinamis.
    @FXML private Button dashboardBtn; // Tombol menu Dashboard.
    @FXML private Button usersBtn; // Tombol menu Users.
    @FXML private Button roomsBtn; // Tombol menu Rooms.

    // Menyimpan tombol yang sedang aktif/dipilih saat ini.
    private Button currentButton;

    /**
     * Inisialisasi awal saat dasbor admin dimuat.
     */
    @FXML
    public void initialize() {
        // Secara default, muat konten dashboard (grafik & statistik).
        loadContent("/com/hotelapp/fxml/admin/DashboardContent.fxml");
        // Tandai tombol "Dashboard" sebagai aktif.
        setActiveButton(dashboardBtn);
        currentButton = dashboardBtn;
    }

    /**
     * Menangani klik pada tombol-tombol menu di sidebar.
     * @param event Aksi klik tombol.
     */
    @FXML
    private void handleMenuClick(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        // Jika tombol yang diklik sudah aktif, jangan lakukan apa-apa.
        if (clickedButton == currentButton) {
            return;
        }

        String fxmlPath = "";
        // Tentukan file FXML mana yang akan dimuat berdasarkan tombol yang diklik.
        if (event.getSource() == dashboardBtn) {
            fxmlPath = "/com/hotelapp/fxml/admin/DashboardContent.fxml";
        } else if (event.getSource() == usersBtn) {
            fxmlPath = "/com/hotelapp/fxml/admin/UserManagement.fxml";
        } else if (event.getSource() == roomsBtn) {
            fxmlPath = "/com/hotelapp/fxml/admin/RoomManagement.fxml";
        }

        // Muat konten FXML yang dipilih dan tandai tombol sebagai aktif.
        loadContent(fxmlPath);
        setActiveButton(clickedButton);
    }

    /**
     * Mengubah style tombol yang aktif agar terlihat berbeda.
     * @param button Tombol yang akan diaktifkan.
     */
    private void setActiveButton(Button button) {
        // Hapus style 'selected' dari tombol yang aktif sebelumnya.
        if (currentButton != null) {
            currentButton.getStyleClass().remove("nav-button-selected");
        }
        // Tambahkan style 'selected' ke tombol yang baru diklik.
        button.getStyleClass().add("nav-button-selected");
        // Update tombol yang sedang aktif.
        currentButton = button;
    }

    /**
     * Memuat file FXML ke dalam area konten utama (contentPane).
     * @param fxmlPath Path ke file FXML yang akan dimuat.
     */
    private void loadContent(String fxmlPath) {
        try {
            // Muat file FXML.
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            // Tampilkan view tersebut di dalam contentPane.
            contentPane.getChildren().setAll(view);
            // Atur agar view mengisi seluruh area contentPane.
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);

        } catch (Exception e) {
            System.err.println("Gagal memuat FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }

    /**
     * Menangani proses logout.
     */
    @FXML
    private void handleLogout() {
        try {
            // Hapus data sesi user yang sedang login.
            Session.getInstance().clearSession();
            // Dapatkan stage saat ini dan muat halaman login.
            Stage stage = (Stage) mainPane.getScene().getWindow();
            URL loginFxml = getClass().getResource("/com/hotelapp/fxml/login.fxml");
            Parent loginRoot = FXMLLoader.load(loginFxml);
            Scene scene = new Scene(loginRoot);
            stage.setScene(scene);
            stage.setTitle("Sistem Reservasi Hotel - Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}