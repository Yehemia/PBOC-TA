package com.hotelapp.util;

import com.hotelapp.dao.RoomDAO;
import com.hotelapp.model.Reservation;
import com.hotelapp.model.Room;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ReceiptPrinter {
    private static final String PRINTER_NAME = "POS-58";
    private static final int CHARS_PER_LINE = 32;

    private static final byte[] ESC_INIT = {0x1B, 0x40};
    private static final byte[] ESC_ALIGN_CENTER = {0x1B, 0x61, 1};
    private static final byte[] ESC_ALIGN_LEFT = {0x1B, 0x61, 0};
    private static final byte[] ESC_BOLD_ON = {0x1B, 0x45, 1};
    private static final byte[] ESC_BOLD_OFF = {0x1B, 0x45, 0};
    private static final byte[] ESC_DOUBLE_HEIGHT_WIDTH = {0x1B, 0x21, 0x30};
    private static final byte[] ESC_NORMAL_TEXT = {0x1B, 0x21, 0x00};
    private static final byte[] FEED_LINE = {10};
    private static final byte[] CUT_PAPER = {0x1D, 'V', 1};

    public static void print(Reservation reservation) {
        Room room = RoomDAO.getRoomById(reservation.getRoomId());
        if (room == null) {
            System.err.println("Gagal mencetak struk: Data kamar tidak ditemukan.");
            return;
        }

        PrintService printService = findPrintService(PRINTER_NAME);
        if (printService == null) {
            System.err.println("Printer '" + PRINTER_NAME + "' tidak ditemukan.");
            AlertHelper.showError("Printer Tidak Ditemukan", "Printer struk dengan nama '" + PRINTER_NAME + "' tidak terdeteksi.");
            return;
        }

        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            NumberFormat currencyFormat = getCurrencyFormatter();
            stream.write(ESC_INIT);

            // Header
            stream.write(ESC_ALIGN_CENTER);
            stream.write(ESC_BOLD_ON);
            stream.write("KENANGAN INN\n".getBytes());
            stream.write(ESC_BOLD_OFF);
            stream.write("Jl. Anaheim No. 2\n".getBytes());
            stream.write("Telp: 021-555-1234\n".getBytes());
            stream.write(ESC_ALIGN_LEFT);
            printLineSeparator(stream);

            // Info Reservasi
            stream.write(("ID Booking: ").getBytes("CP437"));
            stream.write(ESC_BOLD_ON);
            stream.write((reservation.getBookingCode() + "\n").getBytes("CP437"));
            stream.write(ESC_BOLD_OFF);
            stream.write(("Nama Tamu : ").getBytes("CP437"));
            stream.write(ESC_BOLD_ON);
            stream.write((reservation.getGuestName() + "\n").getBytes("CP437"));
            stream.write(ESC_BOLD_OFF);
            printLineSeparator(stream);

            // Info Kamar
            stream.write(("Kamar     : " + room.getRoomNumber() + " (" + room.getRoomType().getName() + ")\n").getBytes("CP437"));
            stream.write(("Check-in  : " + dtf.format(reservation.getCheckIn()) + "\n").getBytes("CP437"));
            stream.write(("Check-out : " + dtf.format(reservation.getCheckOut()) + "\n").getBytes("CP437"));
            printLineSeparator(stream);

            // Total Pembayaran
            stream.write(ESC_ALIGN_CENTER);
            stream.write("Total Bayar\n".getBytes("CP437"));
            stream.write(ESC_DOUBLE_HEIGHT_WIDTH);
            stream.write((currencyFormat.format(reservation.getTotalPrice()) + "\n").getBytes("CP437"));
            stream.write(ESC_NORMAL_TEXT);
            stream.write(ESC_ALIGN_LEFT);
            printLineSeparator(stream);

            // Footer
            printCentered(stream, "TERIMA KASIH");
            stream.write(FEED_LINE);
            printCentered(stream, "Selamat Menikmati Liburan");

            stream.write(FEED_LINE);
            stream.write(FEED_LINE);
            stream.write(CUT_PAPER);

            DocPrintJob job = printService.createPrintJob();
            Doc doc = new SimpleDoc(stream.toByteArray(), DocFlavor.BYTE_ARRAY.AUTOSENSE, null);
            job.print(doc, new HashPrintRequestAttributeSet());

            System.out.println("Struk berhasil dikirim ke printer: " + PRINTER_NAME);

        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Gagal Mencetak", "Terjadi kesalahan saat mengirim data ke printer.");
        }
    }

    private static void printCentered(ByteArrayOutputStream stream, String text) throws IOException {
        int padding = (CHARS_PER_LINE - text.length()) / 2;
        String paddedText = " ".repeat(Math.max(0, padding)) + text + "\n";
        stream.write(paddedText.getBytes("CP437"));
    }

    private static void printLineSeparator(ByteArrayOutputStream stream) throws IOException {
        stream.write(("-".repeat(CHARS_PER_LINE) + "\n").getBytes("CP437"));
    }

    private static NumberFormat getCurrencyFormatter() {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        currencyFormat.setMaximumFractionDigits(0);
        return currencyFormat;
    }

    private static PrintService findPrintService(String printerName) {
        for (PrintService service : PrintServiceLookup.lookupPrintServices(null, null)) {
            if (service.getName().equalsIgnoreCase(printerName)) {
                return service;
            }
        }
        return null;
    }
}