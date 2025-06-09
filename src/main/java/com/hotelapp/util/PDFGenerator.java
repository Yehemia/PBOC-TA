package com.hotelapp.util;

import com.hotelapp.dao.UserDAO;
import com.hotelapp.model.Reservation;
import com.hotelapp.model.User;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;

public class PDFGenerator {

    public static String generateInvoice(Reservation reservation) {
        User customer = UserDAO.getUserById(reservation.getUserId());

        String filePath = "Invoice_" + reservation.getId() + ".pdf";
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            Font boldFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);

            document.add(new Paragraph("Hotel Reservation Invoice", boldFont));
            document.add(new Paragraph("Booking ID: " + reservation.getId(), normalFont));
            if (customer != null) {
                // Tampilkan nama customer berdasarkan data yang diambil dari database
                document.add(new Paragraph("Nama Customer: " + customer.getName(), normalFont));
            } else {
                document.add(new Paragraph("Nama Customer: " + reservation.getUserId(), normalFont));
            }

            document.add(new Paragraph("Kamar: " + reservation.getRoomId(), normalFont));
            document.add(new Paragraph("Check-in: " + reservation.getCheckIn(), normalFont));
            document.add(new Paragraph("Check-out: " + reservation.getCheckOut(), normalFont));
            document.add(new Paragraph("Total Harga: Rp" + reservation.getTotalPrice(), normalFont));
            document.add(new Paragraph("Metode Pembayaran: " + reservation.getPaymentMethod(), normalFont));
            document.add(new Paragraph("Status: " + reservation.getStatus(), normalFont));
            document.add(new Paragraph("Terima kasih atas reservasi Anda!", normalFont));

            document.close();
            return filePath;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
