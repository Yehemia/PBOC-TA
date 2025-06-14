package com.hotelapp.controller.resepsionis;

import com.hotelapp.dao.PaymentDAO;
import com.hotelapp.dao.ReservationDAO;
import com.hotelapp.dao.RoomDAO;
import com.hotelapp.model.Payment;
import com.hotelapp.model.Reservation;
import com.hotelapp.model.Room;
import com.hotelapp.util.QRCodeGenerator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class OfflineReservationController {

    @FXML
    private TextField nameField;

    @FXML
    private DatePicker checkInDatePicker;

    @FXML
    private DatePicker checkOutDatePicker;

    @FXML
    private ComboBox<String> paymentMethodComboBox;

    @FXML
    private Button submitButton;

    // FlowPane untuk menampilkan kartu kamar (viewcard)
    @FXML
    private FlowPane roomFlowPane;

    // Properti untuk menyimpan kamar yang dipilih
    private Room selectedRoom;

    @FXML
    public void initialize() {
        // Inisialisasi pilihan metode pembayaran
        ObservableList<String> paymentOptions = FXCollections.observableArrayList("cash", "online", "pay_later");
        paymentMethodComboBox.setItems(paymentOptions);

        // Atur handler untuk tombol submit
        submitButton.setOnAction(e -> handleSubmit());

        // Muat data kamar yang tersedia ke dalam FlowPane sebagai kartu
        loadAvailableRooms();
    }

    private void loadAvailableRooms() {
        List<Room> availableRooms = RoomDAO.getAvailableRooms();
        roomFlowPane.getChildren().clear();

        for (Room room : availableRooms) {
            VBox card = createRoomCard(room);
            roomFlowPane.getChildren().add(card);
        }
    }

    private VBox createRoomCard(Room room) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-border-color: grey; -fx-border-width: 1; -fx-background-color: #f0f0f0;");
        card.setPrefWidth(150);

        // Informasi kamar yang ditampilkan
        Label roomNumberLabel = new Label("Kamar: " + room.getRoomNumber());
        Label roomTypeLabel = new Label("Tipe: " + room.getRoomType());
        Label priceLabel = new Label("Harga: Rp " + room.getPrice());

        // Event handler agar saat kartu diklik, tampilkan efek seleksi dan simpan selectedRoom
        card.setOnMouseClicked((MouseEvent event) -> {
            clearSelectedStyle();
            card.setStyle("-fx-border-color: blue; -fx-border-width: 2; -fx-background-color: #d0e6f7;");
            selectedRoom = room;
        });

        // Ubah cursor saat hover menjadi tangan
        card.setCursor(Cursor.HAND);

        card.getChildren().addAll(roomNumberLabel, roomTypeLabel, priceLabel);
        return card;
    }

    // Fungsi untuk mengembalikan style awal semua kartu kamar
    private void clearSelectedStyle() {
        roomFlowPane.getChildren().forEach(node -> {
            if (node instanceof VBox) {
                node.setStyle("-fx-border-color: grey; -fx-border-width: 1; -fx-background-color: #f0f0f0;");
            }
        });
    }

    private void handleSubmit() {
        String guestName = nameField.getText().trim();
        LocalDate checkInDate = checkInDatePicker.getValue();
        LocalDate checkOutDate = checkOutDatePicker.getValue();
        String paymentMethod = paymentMethodComboBox.getValue();

        // Validasi input wajib
        if (guestName.isEmpty() || selectedRoom == null || checkInDate == null ||
                checkOutDate == null || paymentMethod == null) {
            showMessage("Harap lengkapi semua data reservasi dan pilih kamar.");
            return;
        }

        // Validasi tanggal: Check-Out harus setelah Check-In
        if (checkOutDate.isBefore(checkInDate) || checkOutDate.isEqual(checkInDate)) {
            showMessage("Tanggal Check-Out harus setelah Check-In!");
            return;
        }

        // Hitung jumlah malam menginap
        long numberOfNights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        if (numberOfNights <= 0) {
            showMessage("Jumlah hari menginap tidak valid.");
            return;
        }

        // Hitung total price dari tarif kamar dan jumlah malam
        double totalPrice = numberOfNights * selectedRoom.getPrice();

        // Buat objek Reservation dengan user_id 0 untuk reservasi offline
        // (DAO nantinya akan menganggap nilai 0 sebagai offline dan meng-set NULL untuk kolom user_id)
        Reservation reservation = new Reservation(
                0,                     // userId; untuk offline, gunakan 0 sebagai indikator
                selectedRoom.getId(),  // ID kamar yang dipilih
                checkInDate,
                checkOutDate,
                paymentMethod,
                "offline",             // booking_type
                "pending",             // status awal
                totalPrice,            // total price yang dihitung
                guestName              // nama tamu
        );

        // Simpan reservasi ke database; DAO akan menangani generated key dan konversi user_id
        boolean success = ReservationDAO.createReservation(reservation);
        if (success) {
            showMessage("Reservasi offline berhasil disimpan. ID: " + reservation.getId());
            // Jika metode pembayaran yang dipilih adalah "online", tampilkan QR Code
            if (paymentMethod.equalsIgnoreCase("online")) {
                Payment payment = new Payment(reservation.getId(), reservation.getTotalPrice(), "unpaid");
                showQRCodePaymentScene(reservation, payment);
            } else {
                // Untuk "cash" atau "pay_later", proses pembayaran bisa diselesaikan secara manual
                showMessage("Silakan selesaikan pembayaran secara " + paymentMethod + ".");
            }
            clearForm();
        } else {
            showMessage("Gagal menyimpan reservasi offline.");
        }
    }

    // Metode untuk membuka tampilan QR Code pembayaran dalam jendela baru
    // Di sini kita menambahkan parameter Reservation untuk memperoleh ID dan total price
    private void showQRCodePaymentScene(Reservation reservation, Payment payment) {
        // Gunakan QRCodeGenerator sesuai permintaan Anda
        Image qrImage = QRCodeGenerator.generateQRCode("Booking ID: " + reservation.getId() + " | Total: Rp" + reservation.getTotalPrice(), 200, 200);
        ImageView qrImageView = new ImageView(qrImage);
        qrImageView.setFitWidth(200);
        qrImageView.setFitHeight(200);

        // Buat layout sederhana untuk menampilkan QR Code
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.getChildren().addAll(new Label("Silakan scan QR Code berikut untuk melakukan pembayaran:"), qrImageView);

        // Buat Scene dan Stage baru
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setTitle("Pembayaran Cashless");
        stage.initModality(Modality.APPLICATION_MODAL); // agar jendela modal
        stage.setScene(scene);
        stage.showAndWait();

        // Setelah jendela QR Code ditutup, anggap pembayaran telah dilakukan (atau nanti ada validasi tambahan)
        payment.setStatus("paid");
        PaymentDAO.createPayment(payment);
        showMessage("Pembayaran cashless berhasil. Transaksi selesai.");
    }

    // Fungsi untuk membersihkan form input dan mengembalikan selectedRoom ke null
    private void clearForm() {
        nameField.clear();
        checkInDatePicker.setValue(null);
        checkOutDatePicker.setValue(null);
        paymentMethodComboBox.getSelectionModel().clearSelection();
        selectedRoom = null;
        clearSelectedStyle();
    }

    // Metode pembantu untuk menampilkan pesan kepada pengguna (tanpa Alert)
    // Di sini sebagai contoh, kita menggunakan System.out.println. Anda dapat mengganti dengan penampilan Label status pada scene.
    private void showMessage(String message) {
        System.out.println(message);
    }
}