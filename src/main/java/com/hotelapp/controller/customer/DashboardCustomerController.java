package com.hotelapp.controller.customer;

import com.hotelapp.model.Room;
import com.hotelapp.model.RoomType;
import com.hotelapp.model.User;
import com.hotelapp.util.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller utama untuk dasbor pelanggan.
 * Bertindak sebagai kerangka yang mengatur navigasi antar menu (Dashboard, History, Profile).
 */
public class DashboardCustomerController {

    // @FXML menghubungkan variabel ke komponen FXML.
    @FXML private BorderPane mainPane; // Layout utama dasbor.
    @FXML private AnchorPane contentPane; // Area konten dinamis.
    @FXML private ImageView logoImageView; // Logo aplikasi.
    @FXML private Label welcomeLabel; // Label sambutan untuk pengguna.
    @FXML private Button dashboardButton; // Tombol menu Dashboard.
    @FXML private Button historyButton; // Tombol menu History.
    @FXML private Button profileButton; // Tombol menu Profile.
    @FXML private Button logoutButton; // Tombol Logout.

    // Menyimpan tombol yang sedang aktif.
    private Button currentButton;

    /**
     * Inisialisasi awal saat dasbor dimuat.
     */
    @FXML
    public void initialize() {
        loadWelcomeMessage(); // Tampilkan pesan selamat datang.
        loadDashboardContent(); // Muat konten dashboard (daftar kamar).
        setActiveButton(dashboardButton); // Tandai tombol dashboard sebagai aktif.
    }

    /**
     * Memuat pesan selamat datang dengan nama pengguna yang login.
     */
    private void loadWelcomeMessage() {
        // Ambil user dari sesi.
        User currentUser = Session.getInstance().getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Selamat datang,\n" + currentUser.getName());
        } else {
            welcomeLabel.setText("Selamat Datang!");
        }
    }

    /**
     * Menangani klik pada tombol menu sidebar.
     * @param event Aksi klik tombol.
     */
    @FXML
    private void handleMenuClick(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        // Jika tombol yang sama diklik lagi, jangan lakukan apa-apa.
        if (clickedButton == currentButton) return;

        // Tandai tombol yang diklik sebagai aktif.
        setActiveButton(clickedButton);

        // Muat konten yang sesuai berdasarkan tombol yang diklik.
        if (clickedButton == dashboardButton) {
            loadDashboardContent();
        } else if (clickedButton == historyButton) {
            loadContent("/com/hotelapp/fxml/customer/history.fxml");
        } else if (clickedButton == profileButton) {
            loadContent("/com/hotelapp/fxml/customer/profile.fxml");
        }
    }

    /**
     * Memuat konten dashboard utama (daftar kamar).
     * Metode ini khusus karena perlu mengirim referensi dirinya sendiri ke content controller.
     */
    private void loadDashboardContent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelapp/fxml/customer/DashboardContent.fxml"));
            Parent dashboardContent = loader.load();
            // Dapatkan controller dari konten yang dimuat.
            DashboardContentController contentController = loader.getController();
            // Berikan referensi controller ini ke content controller agar bisa melakukan navigasi (misal: buka booking).
            contentController.setDashboardCustomerController(this);
            // Tampilkan konten di pane utama.
            setPaneContent(dashboardContent);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Metode umum untuk memuat file FXML ke area konten.
     * @param fxmlPath Path ke file FXML.
     */
    private void loadContent(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            setPaneContent(view);
        } catch (Exception e) {
            System.err.println("Gagal memuat FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }

    /**
     * Mengatur konten yang akan ditampilkan di contentPane.
     * @param content Node (Parent) yang akan ditampilkan.
     */
    private void setPaneContent(Parent content) {
        contentPane.getChildren().setAll(content);
        // Pastikan konten mengisi seluruh area AnchorPane.
        AnchorPane.setTopAnchor(content, 0.0);
        AnchorPane.setBottomAnchor(content, 0.0);
        AnchorPane.setLeftAnchor(content, 0.0);
        AnchorPane.setRightAnchor(content, 0.0);
    }

    /**
     * Membuka jendela dialog untuk pemesanan kamar.
     * Metode ini dipanggil dari RoomCellController atau RoomDetailController.
     * @param selectedRoomType Tipe kamar yang dipilih untuk dipesan.
     */
    public void openBooking(RoomType selectedRoomType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelapp/fxml/customer/booking.fxml"));
            Parent root = loader.load();
            BookingController bookingController = loader.getController();
            // Kirim data tipe kamar yang dipilih ke booking controller.
            bookingController.setRoomType(selectedRoomType);

            // Tampilkan form pemesanan sebagai dialog modal.
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Form Pemesanan");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait(); // Tunggu sampai dialog ditutup.
        } catch (IOException e) {
            System.err.println("Gagal memuat booking.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Mengubah style tombol yang aktif secara visual.
     * @param button Tombol yang akan diaktifkan.
     */
    private void setActiveButton(Button button) {
        if (currentButton != null) {
            currentButton.getStyleClass().remove("sidebar-button-selected");
        }
        button.getStyleClass().add("sidebar-button-selected");
        currentButton = button;
    }

    /**
     * Menangani proses logout.
     */
    @FXML
    public void logout() {
        try {
            // Bersihkan sesi.
            Session.getInstance().clearSession();
            // Dapatkan stage saat ini dan arahkan kembali ke halaman login.
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/com/hotelapp/fxml/login.fxml"));
            Scene scene = new Scene(loginRoot);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}