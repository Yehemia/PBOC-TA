package com.hotelapp.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Kelas utilitas untuk membuat kode-kode acak yang unik.
 */
public class GeneratorUtil {

    /**
     * Membuat kode booking dengan format: INV-YYYYMMDD-XXXX
     * dimana XXXX adalah 4 karakter acak alfanumerik.
     * @return String kode booking yang unik.
     */
    public static String generateBookingCode() {
        // Buat bagian tanggal (misal: "20230101").
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String datePart = dateFormat.format(new Date());

        // Buat bagian acak 4 karakter.
        String randomPart = ThreadLocalRandom.current()
                .ints(4, 0,  36) // Hasilkan 4 angka acak antara 0-35.
                .mapToObj(i -> Integer.toString(i, 36)) // Ubah setiap angka menjadi karakter alfanumerik (0-9, a-z).
                .collect(Collectors.joining()) // Gabungkan menjadi satu string.
                .toUpperCase(); // Ubah menjadi huruf besar.

        // Gabungkan semua bagian.
        return "INV-" + datePart + "-" + randomPart;
    }
}