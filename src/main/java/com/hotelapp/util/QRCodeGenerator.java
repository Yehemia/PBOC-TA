package com.hotelapp.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

/**
 * Kelas utilitas untuk membuat gambar QR Code.
 * Menggunakan library Google ZXing ("Zebra Crossing").
 */
public class QRCodeGenerator {

    /**
     * Membuat sebuah gambar QR Code dari data teks.
     * @param data Teks yang akan di-encode menjadi QR Code (misal: ID booking).
     * @param width Lebar gambar QR Code.
     * @param height Tinggi gambar QR Code.
     * @return Objek Image dari JavaFX yang bisa langsung ditampilkan di ImageView.
     */
    public static Image generateQRCode(String data, int width, int height) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            // 1. Encode data teks menjadi sebuah BitMatrix.
            //    BitMatrix adalah representasi 2D dari titik-titik hitam dan putih QR code.
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height);

            // 2. Buat gambar kosong yang bisa ditulis (WritableImage).
            WritableImage qrImage = new WritableImage(width, height);
            PixelWriter pixelWriter = qrImage.getPixelWriter();

            // 3. Gambar ulang BitMatrix ke WritableImage.
            //    Looping melalui setiap koordinat (x,y).
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    // Jika titik di BitMatrix itu 'true', warnai piksel dengan warna hitam.
                    // Jika 'false', warnai dengan putih.
                    pixelWriter.setColor(x, y, bitMatrix.get(x, y) ? javafx.scene.paint.Color.BLACK : javafx.scene.paint.Color.WHITE);
                }
            }
            return qrImage; // Kembalikan gambar yang sudah jadi.
        } catch (WriterException e) {
            e.printStackTrace();
            return null; // Kembalikan null jika terjadi error.
        }
    }
}