package com.hotelapp.controller.resepsionis;

import com.hotelapp.dao.RoomTypeDAO;
import com.hotelapp.model.Reservation;
import com.hotelapp.model.RoomType;
import com.hotelapp.service.BookingException;
import com.hotelapp.service.ReservationService;
import com.hotelapp.util.AlertHelper;
import com.hotelapp.util.ReceiptPrinter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Ini adalah "otak" untuk halaman Reservasi Offline (untuk tamu walk-in).
 */
public class OfflineReservationController {

    // Variabel yang terhubung ke komponen desain FXML.
    @FXML private TextField nameField; // Input nama tamu.
    @FXML private DatePicker checkInDatePicker;
    @FXML private DatePicker checkOutDatePicker;
    @FXML private ComboBox<String> paymentMethodComboBox; // Pilihan metode bayar (cash/online).
    @FXML private Button submitButton; // Tombol untuk membuat reservasi.
    @FXML private FlowPane roomFlowPane; // Area untuk menampilkan kartu-kartu tipe kamar.

    private RoomType selectedRoomType; // Menyimpan tipe kamar yang dipilih resepsionis.
    private final ReservationService reservationService = new ReservationService();

    /**
     * Fungsi yang berjalan otomatis saat halaman ini dibuka.
     */
    @FXML
    public void initialize() {
        // Isi pilihan metode pembayaran.
        ObservableList<String> paymentOptions = FXCollections.observableArrayList("cash", "online");
        paymentMethodComboBox.setItems(paymentOptions);

        setupValidationAndListeners(); // Atur semua validasi form.
        loadAvailableRoomTypes(); // Muat daftar kamar yang tersedia.
    }

    /**
     * Mengatur semua validasi form dan listener untuk setiap input field.
     */
    private void setupValidationAndListeners() {
        // Awalnya, tombol submit dinonaktifkan.
        submitButton.setDisable(true);
        final LocalDate today = LocalDate.now();

        // Validasi nama: hanya boleh huruf, spasi, dan titik.
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.matches("[a-zA-Z .]*")) {
                nameField.setText(oldValue); // Batalkan input jika mengandung karakter selain yang diizinkan.
            }
        });

        // Validasi tanggal check-in: tidak boleh tanggal yang sudah lewat.
        checkInDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(today));
            }
        });

        // Validasi tanggal check-out: harus setelah tanggal check-in.
        checkOutDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate checkInDate = checkInDatePicker.getValue();
                setDisable(checkInDate == null || empty || !date.isAfter(checkInDate));
            }
        });

        // Jika tanggal check-in diubah, panggil validasi form.
        checkInDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
        // Tambahkan listener ke semua field lainnya untuk memanggil validasi form setiap ada perubahan.
        nameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        checkOutDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
        paymentMethodComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
    }

    /**
     * Memuat daftar tipe kamar yang tersedia dari database.
     */
    private void loadAvailableRoomTypes() {
        roomFlowPane.setDisable(true);
        roomFlowPane.getChildren().clear();

        // Gunakan Task untuk query database di background.
        Task<List<RoomType>> task = new Task<>() {
            @Override
            protected List<RoomType> call() throws Exception {
                return RoomTypeDAO.getRoomTypesWithAvailability();
            }
        };

        // Setelah data didapat, buat kartu untuk setiap tipe kamar.
        task.setOnSucceeded(e -> {
            List<RoomType> availableRoomTypes = task.getValue();
            for (RoomType roomType : availableRoomTypes) {
                // Hanya tampilkan jika ada kamar yang tersedia.
                if (roomType.getAvailableRoomCount() > 0) {
                    VBox card = createRoomTypeCard(roomType);
                    roomFlowPane.getChildren().add(card);
                }
            }
            roomFlowPane.setDisable(false);
        });
        task.setOnFailed(e -> {
            roomFlowPane.setDisable(false);
            AlertHelper.showError("Gagal Memuat", "Tidak dapat memuat daftar tipe kamar.");
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }

    /**
     * Membuat satu "kartu" (VBox) yang menampilkan informasi tipe kamar.
     * @param roomType Data tipe kamar.
     * @return VBox yang berfungsi sebagai kartu.
     */
    private VBox createRoomTypeCard(RoomType roomType) {
        VBox card = new VBox(5);
        // Atur tampilan kartu dengan style CSS.
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: #F8F9F9; -fx-border-color: #D5D8DC; -fx-border-width: 1; -fx-background-radius: 8px; -fx-border-radius: 8px;");
        card.setPrefWidth(200);

        Label roomTypeNameLabel = new Label(roomType.getName());
        Label availabilityLabel = new Label(roomType.getAvailableRoomCount() + " kamar tersedia");
        Label priceLabel = new Label("Harga: Rp " + String.format("%,.0f", roomType.getPrice()));

        // Beri style pada label-label di dalam kartu.
        roomTypeNameLabel.setStyle("-fx-text-fill: #2C3E50; -fx-font-weight: bold;");
        availabilityLabel.setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;");
        priceLabel.setStyle("-fx-text-fill: #2980B9;");

        // Atur apa yang terjadi saat kartu diklik.
        card.setOnMouseClicked((MouseEvent event) -> {
            clearSelectedStyle(); // Hapus style "terpilih" dari semua kartu lain.
            // Beri style "terpilih" (border biru) pada kartu yang diklik.
            card.setStyle("-fx-background-color: #EAF2F8; -fx-border-color: #3498DB; -fx-border-width: 2; -fx-background-radius: 8px; -fx-border-radius: 8px;");
            selectedRoomType = roomType; // Simpan tipe kamar yang dipilih.
            validateForm(); // Periksa kembali validasi form.
        });

        card.setCursor(Cursor.HAND); // Ubah kursor menjadi tangan saat di atas kartu.
        card.getChildren().addAll(roomTypeNameLabel, availabilityLabel, priceLabel);
        return card;
    }

    /**
     * Memeriksa semua input dan mengaktifkan/menonaktifkan tombol submit.
     */
    private void validateForm() {
        boolean isNameEmpty = nameField.getText().trim().isEmpty();
        boolean isRoomNotSelected = selectedRoomType == null;
        boolean isCheckInEmpty = checkInDatePicker.getValue() == null;
        boolean isCheckOutEmpty = checkOutDatePicker.getValue() == null;
        boolean isPaymentEmpty = paymentMethodComboBox.getValue() == null;

        // Tombol submit akan dinonaktifkan jika salah satu kondisi ini terpenuhi.
        submitButton.setDisable(isNameEmpty || isRoomNotSelected || isCheckInEmpty || isCheckOutEmpty || isPaymentEmpty);
    }

    /**
     * Menghapus style "terpilih" dari semua kartu kamar.
     */
    private void clearSelectedStyle() {
        roomFlowPane.getChildren().forEach(node -> {
            node.setStyle("-fx-background-color: #F8F9F9; -fx-border-color: #D5D8DC; -fx-border-width: 1; -fx-background-radius: 8px; -fx-border-radius: 8px;");
        });
    }

    /**
     * Fungsi yang berjalan saat tombol "Submit Reservasi" diklik.
     */
    @FXML
    public void handleSubmit() {
        String guestName = nameField.getText().trim();
        LocalDate checkInDate = checkInDatePicker.getValue();
        LocalDate checkOutDate = checkOutDatePicker.getValue();
        String paymentMethod = paymentMethodComboBox.getValue();

        // Validasi terakhir sebelum memproses.
        if (guestName.isEmpty() || selectedRoomType == null || checkInDate == null || checkOutDate == null || paymentMethod == null) {
            AlertHelper.showWarning("Input Tidak Valid", "Harap lengkapi semua data dan pilih tipe kamar.");
            return;
        }

        try {
            // Jika metode pembayaran adalah tunai (cash)...
            if ("cash".equalsIgnoreCase(paymentMethod)) {
                long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
                double totalPrice = selectedRoomType.getPrice() * nights;

                // Tampilkan dialog pembayaran tunai.
                Optional<Double> cashReceivedOpt = showCashPaymentDialog(totalPrice);

                // Jika pembayaran tunai dikonfirmasi...
                if (cashReceivedOpt.isPresent()) {
                    // Lanjutkan proses booking.
                    processAndFinalizeBooking(guestName, checkInDate, checkOutDate, paymentMethod);
                }
            } else {
                // Jika metode pembayaran lain (online), langsung proses.
                processAndFinalizeBooking(guestName, checkInDate, checkOutDate, paymentMethod);
            }
        } catch (Exception e) {
            // Tangani berbagai jenis error yang mungkin terjadi.
            if (e instanceof BookingException) {
                AlertHelper.showWarning("Booking Gagal", e.getMessage());
            } else if (e instanceof SQLException) {
                AlertHelper.showError("Error Database", "Gagal menyimpan reservasi ke database.");
                e.printStackTrace();
            } else {
                AlertHelper.showError("Error Tidak Terduga", "Terjadi kesalahan yang tidak terduga.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Fungsi final untuk membuat reservasi, mengonfirmasi pembayaran, dan mencetak struk.
     */
    private void processAndFinalizeBooking(String guestName, LocalDate checkIn, LocalDate checkOut, String paymentMethod) throws SQLException, BookingException {
        // Buat booking offline melalui service.
        Reservation newReservation = reservationService.createOfflineBooking(
                selectedRoomType, guestName, checkIn, checkOut, paymentMethod
        );
        // Langsung konfirmasi pembayaran karena ini reservasi di tempat.
        reservationService.confirmPayment(newReservation.getId());

        AlertHelper.showInformation("Sukses", "Reservasi untuk " + guestName + " berhasil dibuat dan dicatat lunas.");
        // Cetak struk pembayaran.
        ReceiptPrinter.print(newReservation);

        // Bersihkan form untuk reservasi berikutnya.
        clearForm();
        loadAvailableRoomTypes();
    }

    /**
     * Menampilkan dialog untuk pembayaran tunai.
     * @param totalBill Total tagihan yang harus dibayar.
     * @return Uang tunai yang diterima dari tamu.
     */
    private Optional<Double> showCashPaymentDialog(double totalBill) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelapp/fxml/resepsionis/CashPayment.fxml"));
            Parent root = loader.load();

            CashPaymentController controller = loader.getController();
            controller.setTotalBill(totalBill); // Kirim total tagihan ke controller dialog.

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Konfirmasi Pembayaran Tunai");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(submitButton.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);

            dialogStage.showAndWait(); // Tampilkan dialog dan tunggu sampai ditutup.

            return controller.getCashReceived(); // Kembalikan jumlah uang yang diterima.
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Membersihkan semua input di form.
     */
    private void clearForm() {
        nameField.clear();
        checkInDatePicker.setValue(null);
        checkOutDatePicker.setValue(null);
        paymentMethodComboBox.getSelectionModel().clearSelection();
        selectedRoomType = null;
        clearSelectedStyle();
        submitButton.setDisable(true);
    }
}