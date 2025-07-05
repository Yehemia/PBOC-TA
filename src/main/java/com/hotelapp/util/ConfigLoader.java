package com.hotelapp.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Kelas ini bertugas untuk memuat data konfigurasi dari file `config.properties`.
 * File `config.properties` berisi data sensitif atau data yang bisa berubah,
 * seperti username & password database, atau kredensial email.
 * Dengan memisahkannya, kita tidak perlu mengubah kode program jika data tersebut berubah.
 */
public final class ConfigLoader {

    // Objek Properties ini akan menyimpan semua data yang dibaca dari file.
    private static final Properties properties = new Properties();

    // Blok 'static' ini akan dijalankan HANYA SEKALI saat kelas ini pertama kali digunakan.
    static {
        // 'try-with-resources' untuk memastikan InputStream ditutup secara otomatis.
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                // Jika file tidak ditemukan, beri pesan error.
                System.out.println("Maaf, tidak dapat menemukan file config.properties");
            } else {
                // Jika file ditemukan, muat semua isinya ke dalam objek 'properties'.
                properties.load(input);
            }
        } catch (IOException ex) {
            // Tangani jika terjadi error saat membaca file.
            ex.printStackTrace();
        }
    }

    // Constructor private agar kelas ini tidak bisa dibuat objeknya.
    private ConfigLoader() {}

    /**
     * Mengambil sebuah nilai dari file konfigurasi berdasarkan kuncinya.
     * @param key Kunci dari data yang ingin diambil (misal: "db.url").
     * @return Nilai dari kunci tersebut (misal: "jdbc:mysql://...").
     */
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}