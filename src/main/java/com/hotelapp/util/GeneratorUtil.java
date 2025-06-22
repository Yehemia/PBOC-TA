package com.hotelapp.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class GeneratorUtil {
    public static String generateBookingCode() {
        // Format: INV-YYYYMMDD-XXXX
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String datePart = dateFormat.format(new Date());

        String randomPart = ThreadLocalRandom.current()
                .ints(4, 0,  36) // Menghasilkan 4 angka acak antara 0-35
                .mapToObj(i -> Integer.toString(i, 36))
                .collect(Collectors.joining())
                .toUpperCase();

        return "INV-" + datePart + "-" + randomPart;
    }
}
