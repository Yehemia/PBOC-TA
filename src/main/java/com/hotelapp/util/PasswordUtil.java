package com.hotelapp.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Kelas utilitas untuk menangani password dengan aman menggunakan algoritma BCrypt.
 * BCrypt adalah metode hashing yang kuat dan direkomendasikan untuk menyimpan password.
 */
public class PasswordUtil {

    /**
     * Mengubah password teks biasa (plain text) menjadi sebuah "hash".
     * Hash adalah string acak yang tidak bisa dikembalikan menjadi password asli.
     * Setiap kali dijalankan, hash yang dihasilkan akan berbeda meskipun passwordnya sama,
     * ini karena BCrypt menambahkan "salt" (data acak) ke dalamnya.
     * @param plainTextPassword Password yang dimasukkan pengguna.
     * @return String hash yang aman untuk disimpan di database.
     */
    public static String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

    /**
     * Memverifikasi apakah password yang dimasukkan pengguna cocok dengan hash yang ada di database.
     * @param plainTextPassword Password yang dimasukkan saat login.
     * @param hashedPassword Hash yang diambil dari database.
     * @return true jika password cocok, false jika tidak.
     */
    public static boolean verifyPassword(String plainTextPassword, String hashedPassword) {
        // Cek dulu apakah hash-nya valid, untuk menghindari error.
        if (hashedPassword == null || !hashedPassword.startsWith("$2a$")) {
            return false;
        }
        // BCrypt akan membandingkan password dengan hash.
        return BCrypt.checkpw(plainTextPassword, hashedPassword);
    }
}