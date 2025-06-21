package com.hotelapp.util;

import com.hotelapp.dao.RoomDAO;
import com.hotelapp.dao.UserDAO;
import com.hotelapp.model.Reservation;
import com.hotelapp.model.Room;
import com.hotelapp.model.User;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class PDFGenerator {

    private static final Font FONT_THANKYOU = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.WHITE);
    private static final Font FONT_THANKYOU_BODY = FontFactory.getFont(FontFactory.HELVETICA, 11, new BaseColor(189, 195, 199));
    private static final Font FONT_SECTION_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.DARK_GRAY);
    private static final Font FONT_LABEL = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY);
    private static final Font FONT_VALUE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
    private static final Font FONT_TOTAL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
    private static final Font FONT_STATUS_LUNAS = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
    private static final Font FONT_BODY_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.BLACK);
    private static final Font FONT_BODY = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.BLACK);

    private static final BaseColor COLOR_STATUS_LUNAS = new BaseColor(39, 174, 96);
    private static final BaseColor COLOR_DARK_BLUE = new BaseColor(44, 62, 80);

    public static String generateInvoice(Reservation reservation) {
        User customer = UserDAO.getUserById(reservation.getUserId());
        Room room = RoomDAO.getRoomById(reservation.getRoomId());
        String filePath = "Bukti_Pemesanan_" + reservation.getId() + ".pdf";

        Document document = new Document(PageSize.A4, 30, 30, 30, 30);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            document.add(createThankYouCard());
            document.add(new Paragraph("\n"));
            document.add(createBookingDetailsCard(reservation, customer, room));

            document.close();
            return filePath;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private static PdfPTable createThankYouCard() throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(COLOR_DARK_BLUE);
        cell.setPadding(30);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        try {
            String logoPath = "/com/hotelapp/images/logo.png";
            Image logo = Image.getInstance(PDFGenerator.class.getResource(logoPath));

            logo.scaleToFit(130, 130);
            logo.setAlignment(Element.ALIGN_CENTER);

            cell.addElement(logo);
            cell.addElement(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 10)));

        } catch (Exception e) {
            System.err.println("Gagal memuat logo. Pastikan file logo ada di folder resources/images. Error: " + e.getMessage());
            Paragraph logoPlaceholder = new Paragraph("[Logo Hotel]", FONT_THANKYOU);
            logoPlaceholder.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(logoPlaceholder);
        }

        Paragraph p1 = new Paragraph("Thank you for booking with Kenangan Inn.", FONT_THANKYOU);
        p1.setAlignment(Element.ALIGN_CENTER);
        p1.setSpacingAfter(10f);
        cell.addElement(p1);

        Paragraph p2 = new Paragraph("Terima kasih banyak sudah memilih Kenangan Inn untuk menginap! Kami senang bisa jadi bagian dari perjalanan Anda, dan semoga pengalaman menginapnya nyaman dan menyenangkan.", FONT_THANKYOU_BODY);
        p2.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(p2);

        table.addCell(cell);
        return table;
    }

    private static PdfPTable createBookingDetailsCard(Reservation reservation, User customer, Room room) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(BaseColor.WHITE);
        cell.setPadding(30);
        cell.setBorder(Rectangle.NO_BORDER);

        cell.addElement(new Paragraph("BUKTI PEMESANAN KAMAR HOTEL", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
        cell.addElement(new Paragraph("\n"));

        String guestNameForPdf = customer != null ? customer.getName() : reservation.getGuestName();
        String emailForPdf = customer != null ? customer.getEmail() : "[tidak tersedia]";
        cell.addElement(createDetailRow("BOOKING ID", "INV-" + reservation.getId(), false));
        cell.addElement(createDetailRow("Tanggal Pemesanan", LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM uuuu", new Locale("id", "ID"))), false));
        cell.addElement(createDetailRow("Nama Tamu", guestNameForPdf, false));
        cell.addElement(createDetailRow("Email", emailForPdf, false));

        cell.addElement(new Chunk(new LineSeparator(0.5f, 100, BaseColor.LIGHT_GRAY, Element.ALIGN_CENTER, -5)));

        long nights = ChronoUnit.DAYS.between(reservation.getCheckIn(), reservation.getCheckOut());
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMMM uuuu (HH:mm 'WIB')", new Locale("id", "ID"));
        cell.addElement(new Paragraph("Detail Kamar", FONT_SECTION_TITLE));
        cell.addElement(createDetailRow("Tipe Kamar", room.getRoomType().getName(), false));
        cell.addElement(createDetailRow("Nomor Kamar", String.valueOf(room.getRoomNumber()), false));
        cell.addElement(createDetailRow("Check-in", reservation.getCheckIn().atTime(14, 0).format(dtf), false));
        cell.addElement(createDetailRow("Check-out", reservation.getCheckOut().atTime(12, 0).format(dtf), false));
        cell.addElement(createDetailRow("Durasi Menginap", nights + " malam", false));

        cell.addElement(new Chunk(new LineSeparator(0.5f, 100, BaseColor.LIGHT_GRAY, Element.ALIGN_CENTER, -5)));

        cell.addElement(new Paragraph("Rincian Biaya", FONT_SECTION_TITLE));
        cell.addElement(createCostTable(reservation, room, nights));

        cell.addElement(new Chunk(new LineSeparator(0.5f, 100, BaseColor.LIGHT_GRAY, Element.ALIGN_CENTER, -5)));

        cell.addElement(new Paragraph("Metode Pembayaran", FONT_SECTION_TITLE));
        cell.addElement(createDetailRow("Metode", reservation.getPaymentMethod() != null ? reservation.getPaymentMethod().toUpperCase() : "N/A", false));
        cell.addElement(createDetailRow("Status Pembayaran", "LUNAS", true)); // Argumen ketiga 'true' untuk status

        table.addCell(cell);
        return table;
    }

    private static PdfPTable createCostTable(Reservation reservation, Room room, long nights) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 2});

        table.addCell(createHeaderCell("Deskripsi"));
        table.addCell(createHeaderCell("Jumlah"));

        double roomSubtotal = room.getRoomType().getPrice() * nights;
        double total = reservation.getTotalPrice();

        table.addCell(createBodyCell("Tarif Kamar (" + nights + " malam)", Element.ALIGN_LEFT));
        table.addCell(createBodyCell(formatCurrency(roomSubtotal), Element.ALIGN_RIGHT));

        if(total > roomSubtotal) {
            double penalty = total - roomSubtotal;
            table.addCell(createBodyCell("Denda", Element.ALIGN_LEFT));
            table.addCell(createBodyCell(formatCurrency(penalty), Element.ALIGN_RIGHT));
        }

        table.addCell(createTotalCell("Total", FONT_TOTAL));
        table.addCell(createTotalCell(formatCurrency(total), FONT_TOTAL));
        return table;
    }

    private static PdfPTable createDetailRow(String label, String value, boolean isStatus) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        try { table.setWidths(new float[]{1, 2}); } catch (Exception ignored) {}

        table.addCell(createSimpleCell(label, FONT_LABEL, Element.ALIGN_LEFT));

        if(isStatus) {
            table.addCell(createStatusCell("LUNAS", COLOR_STATUS_LUNAS, FONT_STATUS_LUNAS));
        } else {
            table.addCell(createSimpleCell(value, FONT_VALUE, Element.ALIGN_LEFT));
        }
        return table;
    }

    private static PdfPCell createStatusCell(String text, BaseColor bgColor, Font font) {
        PdfPCell statusCell = new PdfPCell(new Phrase(text, font));
        statusCell.setBackgroundColor(bgColor);
        statusCell.setBorder(Rectangle.NO_BORDER);
        statusCell.setPaddingTop(5);
        statusCell.setPaddingBottom(7); // bottom
        statusCell.setPaddingLeft(10);
        statusCell.setPaddingRight(10);
        statusCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        statusCell.setUseDescender(true);

        PdfPTable wrapper = new PdfPTable(1);
        wrapper.addCell(statusCell);
        wrapper.setHorizontalAlignment(Element.ALIGN_LEFT);

        PdfPCell wrapperCell = new PdfPCell(wrapper);
        wrapperCell.setBorder(Rectangle.NO_BORDER);
        return wrapperCell;
    }

    private static PdfPCell createSimpleCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(alignment);
        cell.setPaddingBottom(8);
        return cell;
    }

    private static PdfPCell createHeaderCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_BODY_BOLD));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8);
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private static PdfPCell createBodyCell(String text, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_BODY));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(8);
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private static PdfPCell createTotalCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
        cell.setBorderWidthTop(0.5f);
        cell.setBorderColorTop(BaseColor.LIGHT_GRAY);
        cell.setBorderWidthLeft(0);
        cell.setBorderWidthRight(0);
        cell.setBorderWidthBottom(0);
        cell.setHorizontalAlignment(text.startsWith("Rp") ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
        return cell;
    }

    private static String formatCurrency(double amount) {
        return NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(amount);
    }
}