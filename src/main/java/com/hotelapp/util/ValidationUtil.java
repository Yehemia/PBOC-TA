package com.hotelapp.util;

import java.util.regex.Pattern;

public class ValidationUtil {// Regex ini adalah standar industri (RFC 5322) untuk validasi format email.
    // Pola ini memastikan email memiliki format seperti 'nama@domain.com'.
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$"
    );

    public static boolean isEmailValid(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

}
