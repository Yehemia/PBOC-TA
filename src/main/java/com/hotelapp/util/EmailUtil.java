package com.hotelapp.util;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.MimeMultipart;

public class EmailUtil {
    public static void sendVerificationEmail(String toEmail, String token) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("cshotelkenangan1@gmail.com", "zjuu zoqd wyca awqn");
            }
        });

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("cshotelkenangan1@gmail.com"));
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
        final String senderEmail = "cshotelkenangan1@gmail.com";
        final String senderPassword = "zjuu zoqd wyca awqn";

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

            // ✅ Tambahkan teks email
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText("Terima kasih atas reservasi Anda! Invoice terlampir.");

            // ✅ Tambahkan lampiran PDF
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
}
