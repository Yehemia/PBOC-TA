package com.hotelapp.util;

import com.hotelapp.model.Reservation;
import com.hotelapp.model.Room;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.MimeMultipart;

/**
 * Kelas utilitas untuk mengirim berbagai jenis email menggunakan JavaMail API.
 * Menggunakan kredensial dari `config.properties` untuk terhubung ke server email.
 */
public class EmailUtil {

    /**
     * Mengirim email berisi kode verifikasi untuk pendaftaran akun baru.
     * @param toEmail Alamat email tujuan.
     * @param token Kode verifikasi 6 digit yang akan dikirim.
     */
    public static void sendVerificationEmail(String toEmail, String token) {
        // Ambil username dan password email pengirim dari file konfigurasi.
        final String senderEmail = ConfigLoader.getProperty("email.username");
        final String senderPassword = ConfigLoader.getProperty("email.password");

        // Atur properti untuk koneksi ke server SMTP Gmail.
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com"); // Server SMTP Google
        props.put("mail.smtp.port", "587"); // Port untuk koneksi aman TLS
        props.put("mail.smtp.auth", "true"); // Server memerlukan autentikasi (login)
        props.put("mail.smtp.starttls.enable", "true"); // Mengaktifkan enkripsi STARTTLS

        // Buat sesi email dengan autentikasi. Di sini kita menyediakan username dan password pengirim.
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            // Buat objek pesan email.
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(senderEmail)); // Atur alamat email pengirim.
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail)); // Atur alamat email penerima.
            msg.setSubject("Kode Verifikasi Akun"); // Atur subjek email.

            // Atur isi pesan teks.
            msg.setText("Terima kasih telah mendaftar.\n\nKode verifikasi Anda: "
                    + token + "\nKode ini berlaku 15 menit.");

            // Kirim pesan email melalui jaringan.
            Transport.send(msg);

        } catch (MessagingException e) {
            // Jika terjadi error saat mengirim email (misal: koneksi gagal, alamat salah), cetak errornya.
            e.printStackTrace();
        }
    }

    /**
     * Mengirim email berisi invoice/faktur dalam bentuk lampiran PDF.
     * @param customerEmail Alamat email pelanggan.
     * @param pdfPath Lokasi file PDF di komputer yang akan dilampirkan.
     */
    public static void sendInvoiceEmailWithAttachment(String customerEmail, String pdfPath) {
        // Pengaturan properti dan sesi email sama seperti fungsi sebelumnya.
        final String senderEmail = ConfigLoader.getProperty("email.username");
        final String senderPassword = ConfigLoader.getProperty("email.password");
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(customerEmail));
            message.setSubject("Invoice Reservasi Hotel Anda");

            // Karena email ini memiliki teks dan lampiran, kita gunakan "Multipart".
            // Anggap saja seperti amplop yang bisa diisi beberapa jenis surat.

            // Bagian pertama: isi teks email biasa.
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText("Terima kasih atas reservasi Anda! Invoice terlampir.");

            // Bagian kedua: lampiran file.
            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.setDataHandler(new DataHandler(new FileDataSource(pdfPath)));
            attachmentPart.setFileName("Invoice.pdf"); // Nama file yang akan terlihat oleh penerima.

            // Gabungkan kedua bagian tersebut menjadi satu kesatuan.
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            multipart.addBodyPart(attachmentPart);

            // Masukkan gabungan tersebut sebagai konten utama email.
            message.setContent(multipart);

            Transport.send(message);
            System.out.println("✅ Email invoice dengan PDF berhasil dikirim!");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Mengirim email berisi kode verifikasi untuk proses lupa password.
     * @param toEmail Alamat email tujuan.
     * @param token Kode verifikasi yang akan dikirim.
     */
    public static void sendPasswordResetEmail(String toEmail, String token) {
        // Pengaturan properti dan sesi email sama.
        final String senderEmail = ConfigLoader.getProperty("email.username");
        final String senderPassword = ConfigLoader.getProperty("email.password");
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(senderEmail));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            msg.setSubject("Instruksi Reset Password Akun Kenangan Inn");
            msg.setText("Anda menerima email ini karena ada permintaan untuk mengatur ulang password akun Anda.\n\n"
                    + "Gunakan kode verifikasi di bawah ini untuk melanjutkan:\n\n"
                    + "Kode Verifikasi: " + token + "\n\n"
                    + "Kode ini hanya berlaku selama 15 menit. Jika Anda tidak merasa meminta reset password, abaikan email ini.");
            Transport.send(msg);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Mengirim email konfirmasi setelah sebuah reservasi dibatalkan.
     * @param toEmail Alamat email tujuan.
     * @param reservation Objek reservasi yang dibatalkan.
     * @param room Objek kamar dari reservasi tersebut.
     */
    public static void sendCancellationEmail(String toEmail, Reservation reservation, Room room) {
        // Pengaturan properti dan sesi email sama.
        final String senderEmail = ConfigLoader.getProperty("email.username");
        final String senderPassword = ConfigLoader.getProperty("email.password");
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(senderEmail));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            msg.setSubject("Konfirmasi Pembatalan Reservasi " + reservation.getBookingCode());

            // Buat isi email secara dinamis.
            String emailContent = "Halo,\n\n"
                    + "Kami mengonfirmasi bahwa reservasi Anda dengan detail berikut telah berhasil dibatalkan:\n\n"
                    + "ID Booking: " + reservation.getBookingCode() + "\n"
                    + "Tipe Kamar: " + room.getRoomType().getName() + "\n"
                    + "Check-in: " + reservation.getCheckIn().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")) + "\n"
                    + "Check-out: " + reservation.getCheckOut().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")) + "\n\n";

            // Jika reservasi yang dibatalkan sudah lunas, tambahkan informasi tentang proses refund.
            if ("paid".equalsIgnoreCase(reservation.getPaymentStatus())) {
                emailContent += "Karena reservasi ini sudah lunas, proses pengembalian dana (refund) akan kami proses secara manual dalam 1-3 hari kerja. Anda akan dihubungi oleh tim kami untuk konfirmasi lebih lanjut.\n\n";
            }

            emailContent += "Terima kasih telah menggunakan layanan Kenangan Inn.\n";

            msg.setText(emailContent);
            Transport.send(msg);
            System.out.println("✅ Email konfirmasi pembatalan berhasil dikirim!");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}