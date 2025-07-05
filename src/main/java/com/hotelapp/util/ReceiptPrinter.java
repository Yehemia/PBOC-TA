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

/**
 * Kelas utilitas untuk mencetak struk ke printer kasir (printer thermal).
 * Kelas ini menggunakan perintah-perintah khusus (ESC/POS commands) untuk
 * mengatur format teks seperti bold, rata tengah, dan potong kertas.
 */
public class ReceiptPrinter {
    // Nama printer kasir yang ter-install di sistem. Ini harus sesuai persis.
    private static final String PRINTER_NAME = "POS-58";
    // Lebar kertas printer dalam satuan karakter (untuk printer 58mm biasanya 32 karakter).
    private static final int CHARS_PER_LINE = 32;

    // Definisikan perintah-perintah ESC/POS dalam bentuk byte array.
    // Ini adalah kode standar yang dipahami oleh printer thermal.
    private static final byte[] ESC_INIT = {0x1B, 0x40}; // Inisialisasi/reset printer.
    private static final byte[] ESC_ALIGN_CENTER = {0x1B, 0x61, 1}; // Rata tengah.
    private static final byte[] ESC_ALIGN_LEFT = {0x1B, 0x61, 0}; // Rata kiri.
    private static final byte[] ESC_BOLD_ON = {0x1B, 0x45, 1}; // Aktifkan mode tebal.
    private static final byte[] ESC_BOLD_OFF = {0x1B, 0x45, 0}; // Matikan mode tebal.
    private static final byte[] ESC_DOUBLE_HEIGHT_WIDTH = {0x1B, 0x21, 0x30}; // Teks ukuran ganda.
    private static final byte[] ESC_NORMAL_TEXT = {0x1B, 0x21, 0x00}; // Teks ukuran normal.
    private static final byte[] FEED_LINE = {10}; // Perintah ganti baris (Line Feed).
    private static final byte[] CUT_PAPER = {0x1D, 'V', 1}; // Perintah potong kertas (partial cut).

    /**
     * Fungsi utama untuk mencetak struk dari data reservasi.
     * @param reservation Objek reservasi yang akan dicetak.
     */
    public static void print(Reservation reservation) {
        Room room = RoomDAO.getRoomById(reservation.getRoomId());
        if (room == null) {
            System.err.println("Gagal mencetak struk: Data kamar tidak ditemukan.");
            return;
        }

        // Cari printer dengan nama yang sudah ditentukan.
        PrintService printService = findPrintService(PRINTER_NAME);
        if (printService == null) {
            System.err.println("Printer '" + PRINTER_NAME + "' tidak ditemukan.");
            AlertHelper.showError("Printer Tidak Ditemukan", "Printer struk dengan nama '" + PRINTER_NAME + "' tidak terdeteksi.");
            return;
        }

        // ByteArrayOutputStream digunakan untuk membangun seluruh data struk di memori sebelum dikirim ke printer.
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            // -- MULAI MEMBUAT STRUK --
            stream.write(ESC_INIT); // Reset printer ke kondisi awal.
            // HEADER
            printCentered(stream, ESC_BOLD_ON, "KENANGAN INN");
            printCentered(stream, ESC_NORMAL_TEXT, "Jl. IN DULU No. 123, Temben");
            printCentered(stream, ESC_NORMAL_TEXT, "Telp: 0812-3456-7890");
            stream.write(FEED_LINE);

            // DETAIL RESERVASI
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

            // RINCIAN BIAYA
            NumberFormat currencyFormat = getCurrencyFormatter();
            double pricePerNight = room.getRoomType().getPrice();
            stream.write("Rincian Biaya:\n".getBytes("CP437")); // 'CP437' adalah character set standar untuk printer thermal.
            printKeyValuePair(stream, nights + " malam x " + currencyFormat.format(pricePerNight), currencyFormat.format(pricePerNight * nights));
            printLineSeparator(stream, '-');

            // TOTAL
            stream.write(ESC_BOLD_ON);
            printKeyValuePair(stream, "TOTAL BAYAR", currencyFormat.format(reservation.getTotalPrice()));
            stream.write(ESC_BOLD_OFF);
            printLineSeparator(stream, '=');
            stream.write(FEED_LINE);

            // --- FOOTER ---
            printCentered(stream, ESC_NORMAL_TEXT, "Terima kasih atas kunjungan Anda");
            printCentered(stream, ESC_NORMAL_TEXT, "Selamat Menikmati Liburan");

            // Perintah akhir: feed beberapa baris lalu potong kertas.
            stream.write(FEED_LINE);
            stream.write(FEED_LINE);
            stream.write(CUT_PAPER);

            // -- KIRIM KE PRINTER --
            // Buat "pekerjaan cetak" (PrintJob).
            DocPrintJob job = printService.createPrintJob();
            // Bungkus data byte kita menjadi sebuah "dokumen" (Doc).
            Doc doc = new SimpleDoc(stream.toByteArray(), DocFlavor.BYTE_ARRAY.AUTOSENSE, null);
            // Jalankan pekerjaan cetak.
            job.print(doc, new HashPrintRequestAttributeSet());
            System.out.println("Struk berhasil dikirim ke printer: " + PRINTER_NAME);

        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Gagal Mencetak", "Terjadi kesalahan saat mengirim data ke printer.");
        }
    }

    /**
     * Mencetak sepasang data (kunci dan nilai) dengan format rata kiri-kanan.
     */
    private static void printKeyValuePair(ByteArrayOutputStream stream, String key, String value) throws IOException {
        int remainingSpace = CHARS_PER_LINE - key.length() - value.length();
        String padding = " ".repeat(Math.max(1, remainingSpace)); // Buat spasi di tengah.
        stream.write((key + padding + value + "\n").getBytes("CP437"));
    }

    /**
     * Mencetak teks dengan format rata tengah.
     */
    private static void printCentered(ByteArrayOutputStream stream, byte[] style, String text) throws IOException {
        int padding = (CHARS_PER_LINE - text.length()) / 2;
        String paddedText = " ".repeat(Math.max(0, padding)) + text + "\n";
        stream.write(style);
        stream.write(paddedText.getBytes("CP437"));
    }

    /**
     * Mencetak satu baris penuh dengan karakter tertentu sebagai garis pemisah.
     */
    private static void printLineSeparator(ByteArrayOutputStream stream, char character) throws IOException {
        stream.write((String.valueOf(character).repeat(CHARS_PER_LINE) + "\n").getBytes("CP437"));
    }

    /**
     * Membuat formatter untuk mata uang Rupiah.
     */
    private static NumberFormat getCurrencyFormatter() {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        currencyFormat.setMaximumFractionDigits(0);
        return currencyFormat;
    }

    /**
     * Mencari PrintService yang ter-install di komputer berdasarkan namanya.
     */
    private static PrintService findPrintService(String printerName) {
        for (PrintService service : PrintServiceLookup.lookupPrintServices(null, null)) {
            if (service.getName().equalsIgnoreCase(printerName)) {
                return service;
            }
        }
        return null;
    }
}