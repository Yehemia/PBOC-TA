package com.hotelapp.controller.customer;

import com.hotelapp.model.Reservation;
import com.hotelapp.dao.ReservationDAO;
import com.hotelapp.model.User;
import com.hotelapp.util.EmailUtil;
import com.hotelapp.util.PDFGenerator;
import com.hotelapp.util.QRCodeGenerator;
import com.hotelapp.util.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

public class PaymentController {

    @FXML private Label totalPriceLabel;
    @FXML private ImageView qrImageView;
    @FXML private Button confirmPaymentButton;

    private User customer;
    private Reservation reservation;

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;

        User currentUser = Session.getInstance().getCurrentUser(); // Ambil user dari sesi
        if (currentUser != null) {
            this.customer = currentUser;
        } else {
            System.err.println("❌ User tidak ditemukan!");
        }

        totalPriceLabel.setText("Total: Rp" + reservation.getTotalPrice());

        Image qrImage = QRCodeGenerator.generateQRCode("Booking ID: " + reservation.getId() + " | Total: Rp" + reservation.getTotalPrice(), 200, 200);
        qrImageView.setImage(qrImage);
    }


    @FXML
    public void initialize() {
        confirmPaymentButton.setOnAction(event -> processPayment());
    }

    private void processPayment() {
        if (reservation != null) {
            System.out.println("Processing payment for Reservation ID: " + reservation.getId());
            boolean success = ReservationDAO.updatePaymentStatus(reservation.getId(), "paid");
            if (success) {
                confirmPaymentButton.setText("Pembayaran Berhasil!");
                confirmPaymentButton.setDisable(true);

                String pdfPath = PDFGenerator.generateInvoice(reservation);

                if (customer != null && customer.getEmail() != null) {
                    EmailUtil.sendInvoiceEmailWithAttachment(customer.getEmail(), pdfPath);
                } else {
                    System.err.println("❌ Email customer tidak ditemukan!");
                }

            } else {
                System.err.println("Update Payment Status gagal untuk Reservation ID: " + reservation.getId());
                confirmPaymentButton.setText("Gagal!");
            }

        }
    }
}

