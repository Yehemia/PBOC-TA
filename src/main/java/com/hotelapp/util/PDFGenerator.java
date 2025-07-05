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

/**
 * Kelas utilitas untuk membuat file PDF, khususnya untuk invoice atau bukti pemesanan.
 * Kelas ini menggunakan library iTextPDF (versi 5, yang merupakan versi lawas tapi masih fungsional)
 * untuk membuat dan menata elemen-elemen di dalam dokumen PDF.
 */
public class PDFGenerator {

    // --- DEFINISI FONT DAN WARNA ---
    // Mendefinisikan semua Font dan Warna sebagai variabel static final (konstanta).
    // Ini membuat kode lebih rapi, mudah dibaca, dan mudah diubah jika ingin mengganti tema/style PDF.
    private static final Font FONT_THANKYOU = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.WHITE);
    private static final Font FONT_THANKYOU_BODY = FontFactory.getFont(FontFactory.HELVETICA, 11, new BaseColor(189, 195, 199)); // Abu-abu terang
    private static final Font FONT_SECTION_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.DARK_GRAY);
    private static final Font FONT_LABEL = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY);
    private static final Font FONT_VALUE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
    private static final Font FONT_TOTAL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
    private static final Font FONT_STATUS_LUNAS = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
    private static final Font FONT_BODY_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.BLACK);
    private static final Font FONT_BODY = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.BLACK);

    private static final BaseColor COLOR_STATUS_LUNAS = new BaseColor(39, 174, 96); // Warna hijau
    private static final BaseColor COLOR_DARK_BLUE = new BaseColor(44, 62, 80); // Warna biru tua

    /**
     * Fungsi utama untuk membuat file PDF invoice dari sebuah data reservasi.
     * @param reservation Objek reservasi yang akan dibuatkan invoicenya.
     * @return Path (lokasi) dari file PDF yang baru dibuat, atau null jika gagal.
     */
    public static String generateInvoice(Reservation reservation) {
        // Ambil data tambahan yang dibutuhkan (info customer dan kamar) dari DAO.
        User customer = UserDAO.getUserById(reservation.getUserId());
        Room room = RoomDAO.getRoomById(reservation.getRoomId());
        // Tentukan nama file PDF, dibuat unik berdasarkan ID reservasi.
        String filePath = "Bukti_Pemesanan_" + reservation.getId() + ".pdf";

        // 1. Buat objek Dokumen baru dengan ukuran A4 dan margin 30 poin di setiap sisi.
        Document document = new Document(PageSize.A4, 30, 30, 30, 30);
        try {
            // 2. Buat objek PdfWriter yang akan menulis semua konten ke file yang ditentukan.
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            // 3. Buka dokumen. Ini seperti membuka file baru di editor teks, siap untuk ditulis.
            document.open();

            // 4. Tambahkan elemen-elemen ke dalam dokumen satu per satu, dari atas ke bawah.
            document.add(createThankYouCard()); // Tambahkan kartu ucapan terima kasih.
            document.add(new Paragraph("\n")); // Tambah spasi (baris baru).
            document.add(createBookingDetailsCard(reservation, customer, room)); // Tambahkan kartu detail booking.

            // 5. Tutup dokumen. Ini adalah langkah penting yang akan menyimpan semua perubahan ke file PDF.
            document.close();
            return filePath; // Kembalikan lokasi file jika berhasil.
        } catch (Exception e) {
            e.printStackTrace(); // Jika terjadi error, cetak errornya ke konsol.
            return null; // Kembalikan null jika gagal.
        }
    }


    /**
     * Membuat bagian (tabel) kartu ucapan terima kasih di bagian atas PDF.
     * @return Sebuah objek PdfPTable yang siap ditambahkan ke dokumen.
     * @throws DocumentException
     */
    private static PdfPTable createThankYouCard() throws DocumentException {
        // Gunakan tabel dengan 1 kolom agar mudah diberi warna latar belakang dan padding.
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100); // Lebar tabel 100% dari lebar halaman.
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(COLOR_DARK_BLUE);
        cell.setPadding(30);
        cell.setBorder(Rectangle.NO_BORDER); // Hilangkan garis border sel.
        cell.setHorizontalAlignment(Element.ALIGN_CENTER); // Semua isi sel rata tengah.

        try {
            // Coba muat gambar logo dari folder resources.
            String logoPath = "/com/hotelapp/images/logo.png";
            Image logo = Image.getInstance(PDFGenerator.class.getResource(logoPath));
            logo.scaleToFit(130, 130); // Atur ukuran logo agar tidak terlalu besar.
            logo.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(logo); // Tambahkan logo ke dalam sel.
            cell.addElement(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 10))); // Spasi
        } catch (Exception e) {
            // Jika logo gagal dimuat, tampilkan teks pengganti agar PDF tetap bisa dibuat.
            System.err.println("Gagal memuat logo. Error: " + e.getMessage());
            Paragraph logoPlaceholder = new Paragraph("[Logo Hotel]", FONT_THANKYOU);
            logoPlaceholder.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(logoPlaceholder);
        }

        // Tambahkan teks ucapan terima kasih.
        Paragraph p1 = new Paragraph("Thankyou for booking with Kenangan Inn", FONT_THANKYOU);
        p1.setAlignment(Element.ALIGN_CENTER);
        p1.setSpacingAfter(10f); // Beri jarak 10 poin setelah paragraf ini.
        cell.addElement(p1);

        Paragraph p2 = new Paragraph("Terima kasih banyak sudah memilih Kenangan Inn untuk menginap! Kami senang bisa jadi bagian dari perjalanan Anda, dan semoga pengalaman menginapnya nyaman dan menyenangkan.", FONT_THANKYOU_BODY);
        p2.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(p2);

        table.addCell(cell);
        return table;
    }

    /**
     * Membuat bagian (tabel) yang berisi semua detail pemesanan.
     * @param reservation
     * @param customer
     * @param room
     * @return Sebuah PdfPTable yang berisi semua detail.
     * @throws DocumentException
     */
    private static PdfPTable createBookingDetailsCard(Reservation reservation, User customer, Room room) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(BaseColor.WHITE);
        cell.setPadding(30);
        cell.setBorder(Rectangle.NO_BORDER);

        // Tambahkan judul utama.
        cell.addElement(new Paragraph("BUKTI PEMESANAN KAMAR HOTEL", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
        cell.addElement(new Paragraph("\n"));

        // Tambahkan detail-detail pemesan menggunakan fungsi pembantu createDetailRow.
        cell.addElement(createDetailRow("BOOKING ID",  reservation.getBookingCode(), false));
        cell.addElement(createDetailRow("Tanggal Pemesanan", LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM uuuu", new Locale("id", "ID"))), false));
        cell.addElement(createDetailRow("Nama Tamu", customer != null ? customer.getName() : reservation.getGuestName(), false));
        cell.addElement(createDetailRow("Email", customer != null ? customer.getEmail() : "[tidak tersedia]", false));

        // Tambahkan garis pemisah horizontal.
        cell.addElement(new Chunk(new LineSeparator(0.5f, 100, BaseColor.LIGHT_GRAY, Element.ALIGN_CENTER, -5)));

        // Tambahkan detail kamar dan durasi menginap.
        long nights = ChronoUnit.DAYS.between(reservation.getCheckIn(), reservation.getCheckOut());
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMMM uuuu (HH:mm 'WIB')", new Locale("id", "ID"));
        cell.addElement(new Paragraph("Detail Kamar", FONT_SECTION_TITLE));
        cell.addElement(createDetailRow("Tipe Kamar", room.getRoomType().getName(), false));
        cell.addElement(createDetailRow("Nomor Kamar", String.valueOf(room.getRoomNumber()), false));
        cell.addElement(createDetailRow("Check-in", reservation.getCheckIn().atTime(14, 0).format(dtf), false));
        cell.addElement(createDetailRow("Check-out", reservation.getCheckOut().atTime(12, 0).format(dtf), false));
        cell.addElement(createDetailRow("Durasi Menginap", nights + " malam", false));

        cell.addElement(new Chunk(new LineSeparator(0.5f, 100, BaseColor.LIGHT_GRAY, Element.ALIGN_CENTER, -5)));

        // Tambahkan tabel rincian biaya.
        cell.addElement(new Paragraph("Rincian Biaya", FONT_SECTION_TITLE));
        cell.addElement(createCostTable(reservation, room, nights));

        cell.addElement(new Chunk(new LineSeparator(0.5f, 100, BaseColor.LIGHT_GRAY, Element.ALIGN_CENTER, -5)));

        // Tambahkan detail pembayaran.
        cell.addElement(new Paragraph("Metode Pembayaran", FONT_SECTION_TITLE));
        cell.addElement(createDetailRow("Metode", reservation.getPaymentMethod() != null ? reservation.getPaymentMethod().toUpperCase() : "N/A", false));
        // Argumen ketiga 'true' akan membuat status ini memiliki style khusus (latar hijau).
        cell.addElement(createDetailRow("Status Pembayaran", "LUNAS", true));

        table.addCell(cell);
        return table;
    }

    /**
     * Membuat tabel untuk rincian biaya (tarif, denda, total).
     */
    private static PdfPTable createCostTable(Reservation reservation, Room room, long nights) throws DocumentException {
        PdfPTable table = new PdfPTable(2); // Tabel 2 kolom.
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 2}); // Kolom deskripsi lebih lebar dari kolom jumlah.

        // Buat header tabel.
        table.addCell(createHeaderCell("Deskripsi"));
        table.addCell(createHeaderCell("Jumlah"));

        double roomSubtotal = room.getRoomType().getPrice() * nights;
        double total = reservation.getTotalPrice();

        // Tambahkan baris untuk tarif kamar.
        table.addCell(createBodyCell("Tarif Kamar (" + nights + " malam)", Element.ALIGN_LEFT));
        table.addCell(createBodyCell(formatCurrency(roomSubtotal), Element.ALIGN_RIGHT));

        // Jika total harga lebih besar dari subtotal (artinya ada denda), tambahkan baris denda.
        if(total > roomSubtotal) {
            double penalty = total - roomSubtotal;
            table.addCell(createBodyCell("Denda", Element.ALIGN_LEFT));
            table.addCell(createBodyCell(formatCurrency(penalty), Element.ALIGN_RIGHT));
        }

        // Tambahkan baris total.
        table.addCell(createTotalCell("Total", FONT_TOTAL));
        table.addCell(createTotalCell(formatCurrency(total), FONT_TOTAL));
        return table;
    }

    /**
     * Membuat satu baris detail (Label: Value), seperti "Nama: John Doe".
     */
    private static PdfPTable createDetailRow(String label, String value, boolean isStatus) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        try { table.setWidths(new float[]{1, 2}); } catch (Exception ignored) {}

        table.addCell(createSimpleCell(label, FONT_LABEL, Element.ALIGN_LEFT));

        // Jika ini adalah baris status, panggil createStatusCell untuk style khusus.
        if(isStatus) {
            table.addCell(createStatusCell("LUNAS", COLOR_STATUS_LUNAS, FONT_STATUS_LUNAS));
        } else {
            table.addCell(createSimpleCell(value, FONT_VALUE, Element.ALIGN_LEFT));
        }
        return table;
    }

    /**
     * Membuat sel dengan background berwarna untuk status "LUNAS".
     */
    private static PdfPCell createStatusCell(String text, BaseColor bgColor, Font font) {
        PdfPCell statusCell = new PdfPCell(new Phrase(text, font));
        statusCell.setBackgroundColor(bgColor);
        statusCell.setBorder(Rectangle.NO_BORDER);
        statusCell.setPaddingTop(5);
        statusCell.setPaddingBottom(7);
        statusCell.setPaddingLeft(10);
        statusCell.setPaddingRight(10);
        statusCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        statusCell.setUseDescender(true);

        // Dibungkus dengan tabel lagi agar bisa mengatur lebar cell status ini.
        PdfPTable wrapper = new PdfPTable(1);
        wrapper.addCell(statusCell);
        wrapper.setHorizontalAlignment(Element.ALIGN_LEFT);

        PdfPCell wrapperCell = new PdfPCell(wrapper);
        wrapperCell.setBorder(Rectangle.NO_BORDER);
        return wrapperCell;
    }

    // -- FUNGSI-FUNGSI PEMBANTU UNTUK STYLING SEL --

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
        cell.setBorderWidthTop(0.5f); // Hanya beri border atas.
        cell.setBorderColorTop(BaseColor.LIGHT_GRAY);
        cell.setBorderWidthLeft(0);
        cell.setBorderWidthRight(0);
        cell.setBorderWidthBottom(0);
        cell.setHorizontalAlignment(text.startsWith("Rp") ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
        return cell;
    }

    /**
     * Mengubah angka double menjadi format mata uang Rupiah (Rp123.456).
     */
    private static String formatCurrency(double amount) {
        return NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(amount);
    }
}