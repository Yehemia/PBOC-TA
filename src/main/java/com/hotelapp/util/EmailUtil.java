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

public class EmailUtil {
    public static void sendVerificationEmail(String toEmail, String token) {
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
            msg.setSubject("Kode Verifikasi Akun");
            msg.setText("Terima kasih telah mendaftar.\n\nKode verifikasi Anda: "
                    + token + "\nKode ini berlaku 15 menit.");
            Transport.send(msg);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public static void sendInvoiceEmailWithAttachment(String customerEmail, String pdfPath) {
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

            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText("Terima kasih atas reservasi Anda! Invoice terlampir.");

            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.setDataHandler(new DataHandler(new FileDataSource(pdfPath)));
            attachmentPart.setFileName("Invoice.pdf");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            multipart.addBodyPart(attachmentPart);
            message.setContent(multipart);

            Transport.send(message);
            System.out.println("✅ Email invoice dengan PDF berhasil dikirim!");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public static void sendPasswordResetEmail(String toEmail, String token) {
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

    public static void sendCancellationEmail(String toEmail, Reservation reservation, Room room) {
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

            String emailContent = "Halo,\n\n"
                    + "Kami mengonfirmasi bahwa reservasi Anda dengan detail berikut telah berhasil dibatalkan:\n\n"
                    + "ID Booking: " + reservation.getBookingCode() + "\n"
                    + "Tipe Kamar: " + room.getRoomType().getName() + "\n"
                    + "Check-in: " + reservation.getCheckIn().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")) + "\n"
                    + "Check-out: " + reservation.getCheckOut().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")) + "\n\n";

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
