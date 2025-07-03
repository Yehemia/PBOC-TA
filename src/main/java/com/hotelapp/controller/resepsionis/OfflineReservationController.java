package com.hotelapp.controller.resepsionis;

import com.hotelapp.dao.RoomTypeDAO;
import com.hotelapp.model.Reservation;
import com.hotelapp.model.RoomType;
import com.hotelapp.service.BookingException;
import com.hotelapp.service.ReservationService;
import com.hotelapp.util.AlertHelper;
import com.hotelapp.util.PDFGenerator;
import com.hotelapp.util.QRCodeGenerator;
import com.hotelapp.util.ReceiptPrinter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class OfflineReservationController {

    @FXML private TextField nameField;
    @FXML private DatePicker checkInDatePicker;
    @FXML private DatePicker checkOutDatePicker;
    @FXML private ComboBox<String> paymentMethodComboBox;
    @FXML private Button submitButton;
    @FXML private FlowPane roomFlowPane;

    private RoomType selectedRoomType;
    private final ReservationService reservationService = new ReservationService();

    @FXML
    public void initialize() {
        ObservableList<String> paymentOptions = FXCollections.observableArrayList("cash", "online");
        paymentMethodComboBox.setItems(paymentOptions);

        setupValidationAndListeners();
        loadAvailableRoomTypes();
    }

    private void setupValidationAndListeners() {
        submitButton.setDisable(true);

        final LocalDate today = LocalDate.now();

        checkInDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(today));
            }
        });

        checkOutDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate checkInDate = checkInDatePicker.getValue();
                if (checkInDate == null) {
                    setDisable(empty || date.isBefore(today.plusDays(1)));
                } else {
                    setDisable(empty || !date.isAfter(checkInDate));
                }
            }
        });

        checkInDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && checkOutDatePicker.getValue() != null && !checkOutDatePicker.getValue().isAfter(newVal)) {
                checkOutDatePicker.setValue(null);
            }
            validateForm();
        });

        nameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        checkOutDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
        paymentMethodComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
    }

    private void loadAvailableRoomTypes() {
        roomFlowPane.setDisable(true);
        roomFlowPane.getChildren().clear();

        Task<List<RoomType>> task = new Task<>() {
            @Override
            protected List<RoomType> call() throws Exception {
                return RoomTypeDAO.getRoomTypesWithAvailability();
            }
        };

        task.setOnSucceeded(e -> {
            List<RoomType> availableRoomTypes = task.getValue();
            for (RoomType roomType : availableRoomTypes) {
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

    private VBox createRoomTypeCard(RoomType roomType) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: #F8F9F9; -fx-border-color: #D5D8DC; -fx-border-width: 1; -fx-background-radius: 8px; -fx-border-radius: 8px;");
        card.setPrefWidth(200);

        Label roomTypeNameLabel = new Label(roomType.getName());
        Label availabilityLabel = new Label(roomType.getAvailableRoomCount() + " kamar tersedia");
        Label priceLabel = new Label("Harga: Rp " + String.format("%,.0f", roomType.getPrice()));

        roomTypeNameLabel.setStyle("-fx-text-fill: #2C3E50; -fx-font-weight: bold;");
        availabilityLabel.setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;");
        priceLabel.setStyle("-fx-text-fill: #2980B9;");

        card.setOnMouseClicked((MouseEvent event) -> {
            clearSelectedStyle();
            card.setStyle("-fx-background-color: #EAF2F8; -fx-border-color: #3498DB; -fx-border-width: 2; -fx-background-radius: 8px; -fx-border-radius: 8px;");
            selectedRoomType = roomType;
            validateForm();
        });

        card.setCursor(Cursor.HAND);
        card.getChildren().addAll(roomTypeNameLabel, availabilityLabel, priceLabel);
        return card;
    }

    private void validateForm() {
        boolean isNameEmpty = nameField.getText().trim().isEmpty();
        boolean isRoomNotSelected = selectedRoomType == null;
        boolean isCheckInEmpty = checkInDatePicker.getValue() == null;
        boolean isCheckOutEmpty = checkOutDatePicker.getValue() == null;
        boolean isPaymentEmpty = paymentMethodComboBox.getValue() == null;

        boolean isDateInvalid = false;
        if (!isCheckInEmpty && !isCheckOutEmpty) {
            isDateInvalid = !checkOutDatePicker.getValue().isAfter(checkInDatePicker.getValue());
        }

        boolean isFormInvalid = isNameEmpty || isRoomNotSelected || isCheckInEmpty || isCheckOutEmpty || isPaymentEmpty || isDateInvalid;
        submitButton.setDisable(isFormInvalid);
    }

    private void clearSelectedStyle() {
        roomFlowPane.getChildren().forEach(node -> {
            node.setStyle("-fx-background-color: #F8F9F9; -fx-border-color: #D5D8DC; -fx-border-width: 1; -fx-background-radius: 8px; -fx-border-radius: 8px;");
        });
    }

    public void handleSubmit() {
        String guestName = nameField.getText().trim();
        LocalDate checkInDate = checkInDatePicker.getValue();
        LocalDate checkOutDate = checkOutDatePicker.getValue();
        String paymentMethod = paymentMethodComboBox.getValue();

        if (guestName.isEmpty() || selectedRoomType == null || checkInDate == null ||
                checkOutDate == null || paymentMethod == null) {
            AlertHelper.showWarning("Input Tidak Valid", "Harap lengkapi semua data dan pilih tipe kamar.");
            return;
        }

        try {
            Reservation newReservation = reservationService.createOfflineBooking(
                    selectedRoomType, guestName, checkInDate, checkOutDate, paymentMethod
            );

            String successMessage = "Reservasi berhasil dibuat.";
            if ("cash".equalsIgnoreCase(paymentMethod)) {
                reservationService.confirmPayment(newReservation.getId());
                successMessage = "Reservasi tunai berhasil disimpan dan dicatat lunas.";
            } else if ("online".equalsIgnoreCase(paymentMethod)) {
                showQRCodePaymentScene(newReservation);
                reservationService.confirmPayment(newReservation.getId());
                successMessage = "Pembayaran cashless berhasil dikonfirmasi lunas.";
            }

            AlertHelper.showInformation("Sukses", successMessage);
            ReceiptPrinter.print(newReservation);

            clearForm();
            loadAvailableRoomTypes();

        } catch (BookingException e) {
            AlertHelper.showWarning("Booking Gagal", e.getMessage());
        } catch (SQLException e) {
            AlertHelper.showError("Error Database", "Gagal menyimpan reservasi ke database. Periksa koneksi ke server.");
            System.err.println("SQL Error during offline booking: " + e.getMessage());
        }
    }

    private void clearForm() {
        nameField.clear();
        checkInDatePicker.setValue(null);
        checkOutDatePicker.setValue(null);
        paymentMethodComboBox.getSelectionModel().clearSelection();
        selectedRoomType = null;
        clearSelectedStyle();
        submitButton.setDisable(true);
    }

    private void showQRCodePaymentScene(Reservation reservation) {
        Image qrImage = QRCodeGenerator.generateQRCode("Booking ID: " + reservation.getId() + " | Total: Rp" + reservation.getTotalPrice(), 200, 200);
        ImageView qrImageView = new ImageView(qrImage);
        VBox root = new VBox(10, new Label("Silakan scan QR Code berikut:"), qrImageView);
        root.setPadding(new Insets(20));
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Pembayaran Cashless");
        stage.setScene(new Scene(root));
        stage.showAndWait();
    }
}