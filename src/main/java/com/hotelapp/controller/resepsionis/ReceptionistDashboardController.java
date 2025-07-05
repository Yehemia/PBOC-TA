package com.hotelapp.controller.resepsionis;

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

/**
 * Ini adalah "otak" atau kerangka utama dari dasbor Resepsionis.
 * Tugasnya adalah mengatur tampilan menu dan konten yang ada di tengah layar.
 */
public class ReceptionistDashboardController {

    // Variabel yang terhubung ke komponen di desain FXML.
    @FXML private BorderPane mainPane; // Layout utama yang membagi layar (kiri untuk menu, tengah untuk konten).
    @FXML private AnchorPane contentPane; // Area di tengah yang isinya bisa berubah-ubah.
    @FXML private Button checkInButton; // Tombol menu Check-In.
    @FXML private Button checkOutButton; // Tombol menu Check-Out.
    @FXML private Button offlineReservationButton; // Tombol menu untuk reservasi langsung (walk-in).
    @FXML private Button penaltyButton; // Tombol menu untuk melihat denda.
    @FXML private Button historyButton; // Tombol menu untuk riwayat.
    @FXML private Button logoutBtn; // Tombol untuk keluar.

    // Variabel untuk mengingat tombol mana yang sedang aktif/dipilih.
    private Button currentButton;

    /**
     * Fungsi ini berjalan otomatis saat dasbor resepsionis pertama kali dibuka.
     */
    @FXML
    public void initialize() {
        // Saat pertama kali buka, langsung tampilkan halaman Check-In.
        loadContent("/com/hotelapp/fxml/resepsionis/CheckInView.fxml");
        // Tandai tombol Check-In sebagai tombol yang aktif.
        setActiveButton(checkInButton);
        // Atur agar tombol logout menjalankan fungsi performLogout saat diklik.
        logoutBtn.setOnAction(e -> performLogout());
    }

    /**
     * Fungsi ini berjalan setiap kali salah satu tombol menu di sebelah kiri diklik.
     * @param event Informasi tentang tombol mana yang diklik.
     */
    @FXML
    private void handleMenuClick(ActionEvent event) {
        // Dapatkan tombol yang baru saja diklik.
        Button clickedButton = (Button) event.getSource();
        // Jika tombol yang diklik adalah tombol yang sudah aktif, jangan lakukan apa-apa.
        if (clickedButton == currentButton) {
            return;
        }

        String fxmlPath = "";
        // Tentukan file desain (FXML) mana yang harus dibuka berdasarkan tombol yang diklik.
        if (event.getSource() == checkInButton) {
            fxmlPath = "/com/hotelapp/fxml/resepsionis/CheckInView.fxml";
        } else if (event.getSource() == checkOutButton) {
            fxmlPath = "/com/hotelapp/fxml/resepsionis/CheckOutView.fxml";
        } else if (event.getSource() == offlineReservationButton) {
            fxmlPath = "/com/hotelapp/fxml/resepsionis/OfflineReservationView.fxml";
        } else if (event.getSource() == penaltyButton) {
            fxmlPath = "/com/hotelapp/fxml/resepsionis/PenaltyView.fxml";
        } else if (event.getSource() == historyButton) {
            fxmlPath = "/com/hotelapp/fxml/resepsionis/HistoryView.fxml";
        }

        // Jika path FXML sudah ditentukan (artinya bukan tombol logout)...
        if (!fxmlPath.isEmpty()) {
            loadContent(fxmlPath); // Muat konten baru.
            setActiveButton(clickedButton); // Tandai tombol yang diklik sebagai aktif.
        }
    }

    /**
     * Fungsi untuk mengubah tampilan tombol yang aktif.
     * Tombol yang aktif akan diberi style berbeda agar menonjol.
     * @param button Tombol yang akan diaktifkan.
     */
    private void setActiveButton(Button button) {
        // Jika sebelumnya ada tombol yang aktif, hapus style 'selected' darinya.
        if (currentButton != null) {
            currentButton.getStyleClass().remove("nav-button-selected");
        }
        // Tambahkan style 'selected' ke tombol yang baru diklik.
        button.getStyleClass().add("nav-button-selected");
        // Update tombol yang sedang aktif.
        currentButton = button;
    }

    /**
     * Fungsi untuk memuat file FXML dan menampilkannya di area konten tengah.
     * @param fxmlPath Lokasi file FXML yang akan dimuat.
     */
    private void loadContent(String fxmlPath) {
        try {
            // Muat file desain FXML.
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            // Ganti seluruh isi contentPane dengan desain yang baru dimuat.
            contentPane.getChildren().setAll(view);
            // Atur agar konten memenuhi seluruh area contentPane.
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
     * Fungsi untuk melakukan logout.
     */
    private void performLogout() {
        try {
            // Dapatkan jendela (Stage) saat ini.
            Stage stage = (Stage) mainPane.getScene().getWindow();
            // Muat desain halaman login.
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/com/hotelapp/fxml/login.fxml"));
            // Buat Scene baru dengan desain login.
            Scene scene = new Scene(loginRoot);
            // Ganti isi jendela dengan Scene login.
            stage.setScene(scene);
            // Posisikan jendela di tengah layar.
            stage.centerOnScreen();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}