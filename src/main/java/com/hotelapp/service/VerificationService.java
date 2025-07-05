package com.hotelapp.service;

import com.hotelapp.util.Database;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

/**
 * Ini adalah kelas Service untuk semua logika yang terkait dengan verifikasi.
 * Termasuk membuat, menyimpan, dan memeriksa token (kode acak) untuk verifikasi akun atau reset password.
 */
public class VerificationService {
    // Menentukan berapa lama sebuah token/kode verifikasi berlaku (di sini 15 menit).
    private static final Duration VALIDITY = Duration.ofMinutes(15);
    // Objek untuk menghasilkan angka acak.
    private static final Random random = new Random();

    /**
     * Membuat sebuah token/kode verifikasi baru, menyimpannya ke database, dan mengembalikannya.
     * @param userId ID pengguna yang akan diberi token.
     * @return String berisi kode 6 digit.
     * @throws SQLException Jika gagal menyimpan ke database.
     */
    public static String createAndSaveToken(int userId) throws SQLException {
        // Buat angka acak antara 100000 dan 999999.
        int codeInt = 100000 + random.nextInt(900000);
        String token = String.valueOf(codeInt);

        // Tentukan waktu kedaluwarsa (waktu sekarang + durasi validitas).
        LocalDateTime expires = LocalDateTime.now().plus(VALIDITY);
        try (Connection conn = Database.getConnection();
             PreparedStatement p = conn.prepareStatement(
                     "INSERT INTO verification_tokens (user_id, token, expires_at, used) VALUES (?, ?, ?, false)"
             )) {
            p.setInt(1, userId);
            p.setString(2, token);
            p.setTimestamp(3, Timestamp.valueOf(expires)); // Simpan waktu kedaluwarsa.
            p.executeUpdate();
        }
        return token;
    }

    /**
     * Mengambil waktu kedaluwarsa dari token terakhir yang aktif untuk seorang pengguna.
     * @param userId ID pengguna.
     * @return Waktu kedaluwarsa, atau null jika tidak ada token aktif.
     * @throws SQLException
     */
    public static LocalDateTime getTokenExpiry(int userId) throws SQLException {
        String sql = "SELECT expires_at FROM verification_tokens " +
                "WHERE user_id = ? AND used = false " + // Cari token yang belum dipakai.
                "ORDER BY created_at DESC LIMIT 1"; // Ambil yang paling baru.
        try (Connection conn = Database.getConnection();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, userId);
            ResultSet rs = p.executeQuery();
            if (rs.next()) {
                return rs.getTimestamp("expires_at").toLocalDateTime();
            }
        }
        return null;
    }

    /**
     * Memeriksa apakah token yang dimasukkan pengguna valid (cocok dan belum kedaluwarsa).
     * @param userId ID pengguna.
     * @param tokenInput Kode yang dimasukkan oleh pengguna.
     * @return true jika valid, false jika tidak.
     */
    public static boolean verifyToken(int userId, String tokenInput) {
        // Cari token yang cocok dengan user, kode, dan belum terpakai.
        String sql = "SELECT token, expires_at, used FROM verification_tokens " +
                "WHERE user_id = ? AND token = ? AND used = false";
        try (Connection conn = Database.getConnection();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, userId);
            p.setString(2, tokenInput);
            ResultSet rs = p.executeQuery();
            if (rs.next()) {
                // Jika token ditemukan di database...
                Timestamp ts = rs.getTimestamp("expires_at");
                LocalDateTime expiresAt = ts.toLocalDateTime();
                // Periksa apakah waktu sekarang masih sebelum waktu kedaluwarsa.
                if (LocalDateTime.now().isBefore(expiresAt)) {
                    return true; // Token valid!
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Token tidak valid (tidak ditemukan atau sudah kedaluwarsa).
    }

    /**
     * Menandai semua token yang aktif milik seorang pengguna menjadi sudah terpakai (used = true).
     * Ini dilakukan setelah verifikasi berhasil agar token tidak bisa dipakai lagi.
     * @param userId ID pengguna.
     */
    public static void markAllTokensUsed(int userId) {
        String sql = "UPDATE verification_tokens SET used = true WHERE user_id = ? AND used = false";
        try (Connection conn = Database.getConnection();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, userId);
            p.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Menyimpan log (catatan) setiap percobaan verifikasi.
     * Berguna untuk audit keamanan, meskipun tidak ditampilkan di UI saat ini.
     * @param userId ID pengguna yang mencoba.
     * @param tokenInput Kode yang dimasukkan.
     * @param success Apakah percobaan berhasil atau tidak.
     */
    public static void logVerificationAttempt(int userId, String tokenInput, boolean success) {
        String sql = "INSERT INTO verification_logs (user_id, token_input, success) VALUES (?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, userId);
            p.setString(2, tokenInput);
            p.setBoolean(3, success);
            p.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}