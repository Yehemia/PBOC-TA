package com.hotelapp.controller.customer;

import com.hotelapp.model.Reservation;
import com.hotelapp.model.RoomType;
import com.hotelapp.model.User;
import com.hotelapp.service.BookingException;
import com.hotelapp.service.ReservationService;
import com.hotelapp.util.AlertHelper;
import com.hotelapp.util.Session;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Controller untuk form pemesanan kamar.
 * Mengelola pemilihan tanggal, metode pembayaran, dan proses pembuatan booking.
 */
public class BookingController {

    // @FXML menghubungkan variabel ke komponen FXML.
    @FXML private Label roomInfoLabel; // Menampilkan nama tipe kamar yang dipesan.
    @FXML private DatePicker checkInPicker; // Pilihan tanggal check-in.
    @FXML private DatePicker checkOutPicker; // Pilihan tanggal check-out.
    @FXML private ComboBox<String> paymentMethodComboBox; // Pilihan metode pembayaran.
    @FXML private Button confirmBookingButton; // Tombol untuk konfirmasi pemesanan.

    private RoomType selectedRoomType; // Menyimpan objek tipe kamar yang sedang dipesan.
    private final ReservationService reservationService = new ReservationService(); // Service untuk logika bisnis reservasi.

    /**
     * Inisialisasi awal saat form pemesanan dimuat.
     */
    @FXML
    public void initialize() {
        // Atur opsi metode pembayaran. Saat ini hanya "online".
        paymentMethodComboBox.setItems(FXCollections.observableArrayList("online"));
        // Atur validasi untuk form dan date picker.
        setupValidationAndDatePickers();
    }

    /**
     * Menerima dan menetapkan tipe kamar yang dipilih dari halaman sebelumnya.
     * @param roomType Tipe kamar yang akan dipesan.
     */
    public void setRoomType(RoomType roomType) {
        this.selectedRoomType = roomType;
        // Tampilkan nama tipe kamar di label.
        roomInfoLabel.setText("Pemesanan untuk Tipe Kamar: " + roomType.getName());
    }

    /**
     * Mengatur validasi input, terutama untuk tanggal check-in dan check-out.
     */
    private void setupValidationAndDatePickers() {
        final LocalDate today = LocalDate.now();

        // Atur DatePicker untuk check-in: nonaktifkan tanggal sebelum hari ini.
        checkInPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(today));
            }
        });

        // Atur DatePicker untuk check-out: nonaktifkan tanggal sebelum tanggal check-in.
        checkOutPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate checkInDate = checkInPicker.getValue();
                // Jika tanggal check-in belum dipilih, nonaktifkan tanggal sebelum besok.
                if (checkInDate == null) {
                    setDisable(empty || date.isBefore(today.plusDays(1)));
                } else {
                    // Jika sudah, nonaktifkan tanggal yang tidak setelah tanggal check-in.
                    setDisable(empty || !date.isAfter(checkInDate));
                }
            }
        });

        // Listener untuk tanggal check-in: jika diubah, reset tanggal check-out jika tidak valid.
        checkInPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && checkOutPicker.getValue() != null && !checkOutPicker.getValue().isAfter(newVal)) {
                checkOutPicker.setValue(null);
            }
        });

        // Buat sebuah binding untuk menonaktifkan tombol konfirmasi jika form tidak valid.
        BooleanBinding isInvalid = Bindings.createBooleanBinding(() ->
                        checkInPicker.getValue() == null ||
                                checkOutPicker.getValue() == null ||
                                paymentMethodComboBox.getValue() == null ||
                                !checkOutPicker.getValue().isAfter(checkInPicker.getValue()),
                checkInPicker.valueProperty(), checkOutPicker.valueProperty(), paymentMethodComboBox.valueProperty()
        );
        // Ikat properti 'disable' dari tombol ke binding yang telah dibuat.
        confirmBookingButton.disableProperty().bind(isInvalid);
    }

    /**
     * Memproses pemesanan saat tombol konfirmasi diklik.
     */
    @FXML
    public void processBooking() {
        // Ambil data pengguna yang sedang login dari sesi.
        User currentUser = Session.getInstance().getCurrentUser();
        if (currentUser == null) {
            AlertHelper.showError("Login Dibutuhkan", "Anda harus login untuk melakukan pemesanan.");
            return;
        }

        try {
            // Panggil service untuk membuat booking baru.
            Reservation newReservation = reservationService.createBooking(
                    currentUser, selectedRoomType, checkInPicker.getValue(),
                    checkOutPicker.getValue(), paymentMethodComboBox.getValue()
            );
            // Tutup jendela pemesanan.
            ((Stage) confirmBookingButton.getScene().getWindow()).close();
            // Arahkan ke halaman pembayaran.
            navigateToPayment(newReservation);

        } catch (BookingException e) {
            // Tangani error spesifik dari proses booking (misal: kamar penuh).
            AlertHelper.showWarning("Booking Gagal", e.getMessage());
        } catch (SQLException e) {
            // Tangani error koneksi database.
            AlertHelper.showError(
                    "Kesalahan Teknis",
                    "Terjadi masalah saat terhubung ke server. Pastikan Anda terhubung ke internet dan coba lagi."
            );
        } catch (Exception e) {
            // Tangani error tak terduga lainnya.
            AlertHelper.showError(
                    "Error Tidak Terduga",
                    "Terjadi kesalahan yang tidak terduga. Silakan coba lagi atau hubungi cshotelkenangan1@gmail.com."
            );
            System.err.println("Unexpected error during booking: " + e.getMessage());
        }
    }

    /**
     * Membuka halaman pembayaran setelah booking berhasil dibuat.
     * @param reservation Objek reservasi yang baru dibuat.
     */
    private void navigateToPayment(Reservation reservation) {
        try {
            // Muat FXML untuk halaman pembayaran.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelapp/fxml/customer/payment.fxml"));
            Parent root = loader.load();

            // Dapatkan controller halaman pembayaran dan kirim data reservasi ke sana.
            PaymentController paymentController = loader.getController();
            paymentController.setReservation(reservation);

            // Tampilkan halaman pembayaran sebagai dialog modal.
            Stage paymentStage = new Stage();
            paymentStage.setTitle("Pembayaran");
            paymentStage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root);
            paymentStage.setScene(scene);
            paymentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showError("Gagal", "Tidak dapat membuka halaman pembayaran.");
        }
    }
}