package com.hotelapp.util;

import java.util.regex.Pattern;

/**
 * Kelas utilitas untuk fungsi-fungsi validasi input.
 */
public class ValidationUtil {

    // 'Pattern' ini adalah sebuah "Regular Expression" (Regex) untuk memeriksa format email.
    // Regex adalah pola teks yang sangat kuat untuk mencocokkan string.
    // Pola ini memeriksa apakah sebuah string memiliki format seperti "sesuatu@sesuatu.sesuatu".
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$"
    );

    /**
     * Memeriksa apakah sebuah string email memiliki format yang valid.
     * @param email String email yang akan diperiksa.
     * @return true jika valid, false jika tidak.
     */
    public static boolean isEmailValid(String email) {
        // Jika email null atau kosong, langsung kembalikan false.
        if (email == null || email.isBlank()) {
            return false;
        }
        // Gunakan pola Regex yang sudah dibuat untuk mencocokkan string email.
        return EMAIL_PATTERN.matcher(email).matches();
    }
}