package com.hotelapp.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class QRCodeGenerator {

    public static Image generateQRCode(String data, int width, int height) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height);
            WritableImage qrImage = new WritableImage(width, height);
            PixelWriter pixelWriter = qrImage.getPixelWriter();

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    pixelWriter.setColor(x, y, bitMatrix.get(x, y) ? javafx.scene.paint.Color.BLACK : javafx.scene.paint.Color.WHITE);
                }
            }
            return qrImage;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
}
