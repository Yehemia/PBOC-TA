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
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Parent;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

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

    @FXML
    public void handleSubmit() {
        String guestName = nameField.getText().trim();
        LocalDate checkInDate = checkInDatePicker.getValue();
        LocalDate checkOutDate = checkOutDatePicker.getValue();
        String paymentMethod = paymentMethodComboBox.getValue();

        if (guestName.isEmpty() || selectedRoomType == null || checkInDate == null || checkOutDate == null || paymentMethod == null) {
            AlertHelper.showWarning("Input Tidak Valid", "Harap lengkapi semua data dan pilih tipe kamar.");
            return;
        }

        try {
            if ("cash".equalsIgnoreCase(paymentMethod)) {
                long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
                double totalPrice = selectedRoomType.getPrice() * nights;

                Optional<Double> cashReceivedOpt = showCashPaymentDialog(totalPrice);

                if (cashReceivedOpt.isPresent()) {
                    processAndFinalizeBooking(guestName, checkInDate, checkOutDate, paymentMethod);
                }
            } else {
                processAndFinalizeBooking(guestName, checkInDate, checkOutDate, paymentMethod);
            }
        } catch (Exception e) {
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

    private void processAndFinalizeBooking(String guestName, LocalDate checkIn, LocalDate checkOut, String paymentMethod) throws SQLException, BookingException {
        Reservation newReservation = reservationService.createOfflineBooking(
                selectedRoomType, guestName, checkIn, checkOut, paymentMethod
        );
        reservationService.confirmPayment(newReservation.getId());

        if ("online".equalsIgnoreCase(paymentMethod)) {
            showQRCodePaymentScene(newReservation);
        }

        AlertHelper.showInformation("Sukses", "Reservasi untuk " + guestName + " berhasil dibuat dan dicatat lunas.");
        ReceiptPrinter.print(newReservation);

        clearForm();
        loadAvailableRoomTypes();
    }

    private Optional<Double> showCashPaymentDialog(double totalBill) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelapp/fxml/resepsionis/CashPayment.fxml"));
            Parent root = loader.load();

            CashPaymentController controller = loader.getController();
            controller.setTotalBill(totalBill);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Konfirmasi Pembayaran Tunai");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(submitButton.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);

            dialogStage.showAndWait();

            return controller.getCashReceived();
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
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