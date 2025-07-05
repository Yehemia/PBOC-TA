package com.hotelapp.dao;

import com.hotelapp.model.Facility;
import com.hotelapp.util.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Ini adalah kelas DAO (Data Access Object) untuk Fasilitas.
 * Tugasnya khusus untuk mengambil data fasilitas (seperti WiFi, AC, dll.) dari database.
 */
public class FacilityDAO {

    /**
     * Mengambil semua data fasilitas yang ada di database.
     * @return Sebuah List (daftar) yang berisi objek-objek Facility.
     */
    public static List<Facility> getAllFacilities() {
        // Siapkan sebuah list kosong untuk menampung hasil dari database.
        List<Facility> facilities = new ArrayList<>();
        // Ini adalah perintah SQL untuk mengambil semua data dari tabel 'facilities' dan mengurutkannya berdasarkan nama.
        String sql = "SELECT * FROM facilities ORDER BY name";

        // 'try-with-resources' akan otomatis menutup koneksi database setelah selesai, ini sangat aman.
        try (Connection con = Database.getConnection(); // 1. Buka koneksi ke database.
             PreparedStatement ps = con.prepareStatement(sql); // 2. Siapkan perintah SQL.
             ResultSet rs = ps.executeQuery()) { // 3. Jalankan perintah SQL dan dapatkan hasilnya (ResultSet).

            // 'while (rs.next())' artinya: "selama masih ada baris data berikutnya di hasil query..."
            while (rs.next()) {
                // Untuk setiap baris, buat sebuah objek Facility baru.
                // Ambil data dari setiap kolom (id, name, icon_literal) dan masukkan ke objek.
                facilities.add(new Facility(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("icon_literal")
                ));
            }
        } catch (Exception e) {
            // Jika terjadi error (misal: database mati), cetak error tersebut ke konsol.
            e.printStackTrace();
        }
        // Kembalikan daftar fasilitas yang sudah terisi.
        return facilities;
    }
}