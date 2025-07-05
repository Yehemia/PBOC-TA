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
import java.time.temporal.ChronoUnit;
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
            stream.write(ESC_INIT);
            //HEADER
            printCentered(stream, ESC_BOLD_ON, "KENANGAN INN");
            printCentered(stream, ESC_NORMAL_TEXT, "Jl. IN DULU No. 123, Temben");
            printCentered(stream, ESC_NORMAL_TEXT, "Telp: 0812-3456-7890");
            stream.write(FEED_LINE);

            //DETAIL RESERVASI
            printLineSeparator(stream, '=');
            printKeyValuePair(stream, "Booking ID", reservation.getBookingCode());
            printKeyValuePair(stream, "Nama Tamu", reservation.getGuestName());
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            printKeyValuePair(stream, "Waktu Cetak", dtf.format(java.time.LocalDateTime.now()));
            printLineSeparator(stream, '-');

            // --- DETAIL MENGINAP ---
            long nights = ChronoUnit.DAYS.between(reservation.getCheckIn(), reservation.getCheckOut());
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            printKeyValuePair(stream, "Check-in", dateFormat.format(reservation.getCheckIn()));
            printKeyValuePair(stream, "Check-out", dateFormat.format(reservation.getCheckOut()));
            printKeyValuePair(stream, "Kamar", room.getRoomNumber() + " (" + room.getRoomType().getName() + ")");
            printKeyValuePair(stream, "Durasi", nights + " malam");
            printLineSeparator(stream, '-');

            //RINCIAN BIAYA
            NumberFormat currencyFormat = getCurrencyFormatter();
            double pricePerNight = room.getRoomType().getPrice();

            stream.write("Rincian Biaya:\n".getBytes("CP437"));
            printKeyValuePair(stream, nights + " malam x " + currencyFormat.format(pricePerNight), currencyFormat.format(pricePerNight * nights));

            printLineSeparator(stream, '-');

            //TOTAL
            stream.write(ESC_BOLD_ON);
            printKeyValuePair(stream, "TOTAL BAYAR", currencyFormat.format(reservation.getTotalPrice()));
            stream.write(ESC_BOLD_OFF);
            printLineSeparator(stream, '=');
            stream.write(FEED_LINE);

            // --- FOOTER ---
            printCentered(stream, ESC_NORMAL_TEXT, "Terima kasih atas kunjungan Anda");
            printCentered(stream, ESC_NORMAL_TEXT, "Selamat Menikmati Liburan");

            // Feed and Cut
            stream.write(FEED_LINE);
            stream.write(FEED_LINE);
            stream.write(CUT_PAPER);

            // Kirim ke Printer
            DocPrintJob job = printService.createPrintJob();
            Doc doc = new SimpleDoc(stream.toByteArray(), DocFlavor.BYTE_ARRAY.AUTOSENSE, null);
            job.print(doc, new HashPrintRequestAttributeSet());
            System.out.println("Struk berhasil dikirim ke printer: " + PRINTER_NAME);

        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Gagal Mencetak", "Terjadi kesalahan saat mengirim data ke printer.");
        }
    }

    private static void printKeyValuePair(ByteArrayOutputStream stream, String key, String value) throws IOException {//Rata KIrir kanan
        int remainingSpace = CHARS_PER_LINE - key.length() - value.length();
        String padding = " ".repeat(Math.max(1, remainingSpace));
        stream.write((key + padding + value + "\n").getBytes("CP437"));
    }

    private static void printCentered(ByteArrayOutputStream stream, byte[] style, String text) throws IOException {
        int padding = (CHARS_PER_LINE - text.length()) / 2;
        String paddedText = " ".repeat(Math.max(0, padding)) + text + "\n";
        stream.write(style);
        stream.write(paddedText.getBytes("CP437"));
    }

    private static void printLineSeparator(ByteArrayOutputStream stream, char character) throws IOException {
        stream.write((String.valueOf(character).repeat(CHARS_PER_LINE) + "\n").getBytes("CP437"));
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