package com.hotelapp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Kelas utilitas sederhana untuk membuat koneksi ke database.
 * Kelas ini menggunakan data dari ConfigLoader untuk terhubung.
 */
public class Database {

    /**
     * Membuat dan mengembalikan sebuah koneksi ke database.
     * @return Objek Connection yang siap digunakan untuk query.
     * @throws SQLException Jika koneksi gagal (misal: password salah, server mati).
     */
    public static Connection getConnection() throws SQLException {
        // DriverManager.getConnection akan mencoba terhubung ke database
        // menggunakan URL, user, dan password yang didapat dari ConfigLoader.
        return DriverManager.getConnection(
                ConfigLoader.getProperty("db.url"),
                ConfigLoader.getProperty("db.user"),
                ConfigLoader.getProperty("db.password")
        );
    }
}