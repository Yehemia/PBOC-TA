package com.hotelapp.dao;

import com.hotelapp.model.Penalty;
import com.hotelapp.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Ini adalah kelas DAO (Data Access Object) untuk Denda (Penalty).
 * Kelas ini mengelola semua interaksi dengan tabel 'penalties' di database.
 */
public class PenaltyDAO {

    /**
     * Menambahkan data denda baru ke database.
     * @param penalty Objek Penalty yang berisi data denda baru.
     * @param con Koneksi database (digunakan untuk transaksi agar semua proses berhasil atau semua gagal).
     * @return true jika berhasil, false jika gagal.
     * @throws SQLException jika ada error SQL.
     */
    public static boolean addPenalty(Penalty penalty, Connection con) throws SQLException {
        // Perintah SQL untuk memasukkan data baru ke tabel 'penalties'.
        // INSERT INTO penalties(...) : "Masukkan satu baris data baru ke tabel 'penalties'".
        // VALUES (?, ?, ?, ?, NOW()): "Isi kolom-kolom tersebut dengan nilai ini".
        // ?                         : Placeholder yang akan diisi oleh Java untuk keamanan.
        // NOW()                     : Fungsi database untuk mengisi waktu saat ini secara otomatis.
        // Tanda tanya (?) adalah placeholder yang akan diisi nanti.
        String sql = "INSERT INTO penalties (reservation_id, amount, reason, penalty_status, created_at) VALUES (?, ?, ?, ?, NOW())";

        // 'Statement.RETURN_GENERATED_KEYS' digunakan agar kita bisa mendapatkan ID yang baru dibuat oleh database.
        try (PreparedStatement ps = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            // Isi placeholder (?) dengan data dari objek Penalty.
            ps.setInt(1, penalty.getReservationId());
            ps.setDouble(2, penalty.getAmount());
            ps.setString(3, penalty.getReason());
            ps.setString(4, penalty.getPenaltyStatus());

            // Jalankan perintah SQL untuk memasukkan data.
            int affectedRows = ps.executeUpdate();

            // Jika ada baris yang berhasil ditambahkan...
            if (affectedRows > 0) {
                // Ambil ID yang baru saja dibuat oleh database.
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        // Set ID pada objek Penalty yang kita punya.
                        penalty.setId(generatedKeys.getInt(1));
                    }
                }
                return true; // Berhasil!
            }
        }
        return false; // Gagal.
    }

    /**
     * Mengambil semua data denda dari database.
     * @return Sebuah daftar (List) yang berisi semua objek Penalty.
     */
    public static List<Penalty> getAllPenalties() {
        List<Penalty> penalties = new ArrayList<>();
        String sql = "SELECT * FROM penalties";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // Untuk setiap baris data, buat objek Penalty.
                Penalty penalty = new Penalty();
                penalty.setId(rs.getInt("id"));
                penalty.setReservationId(rs.getInt("reservation_id"));
                penalty.setAmount(rs.getDouble("amount"));
                penalty.setReason(rs.getString("reason"));
                penalty.setPenaltyStatus(rs.getString("penalty_status"));

                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) {
                    penalty.setCreatedAt(ts.toLocalDateTime());
                }

                // Tambahkan objek ke dalam daftar.
                penalties.add(penalty);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return penalties;
    }

    /**
     * Mencari satu data denda berdasarkan ID-nya.
     * @param penaltyId ID denda yang dicari.
     * @param conn Koneksi database untuk transaksi.
     * @return Objek Penalty jika ditemukan, atau null jika tidak.
     * @throws SQLException jika ada error SQL.
     */
    public static Penalty getPenaltyById(int penaltyId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM penalties WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, penaltyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Jika data ditemukan, buat objek Penalty dan isi datanya.
                    Penalty penalty = new Penalty();
                    penalty.setId(rs.getInt("id"));
                    penalty.setReservationId(rs.getInt("reservation_id"));
                    penalty.setAmount(rs.getDouble("amount"));
                    penalty.setReason(rs.getString("reason"));
                    penalty.setPenaltyStatus(rs.getString("penalty_status"));
                    return penalty;
                }
            }
        }
        return null; // Kembalikan null jika tidak ada data yang ditemukan.
    }

    /**
     * Menghitung total pendapatan dari semua denda yang sudah dibayar ('paid').
     * @return Total pendapatan dari denda.
     */
    public static double getTotalPaidPenalties() {
        // Perintah SQL untuk menjumlahkan (SUM) kolom 'amount'
        // SELECT SUM(amount)      : "Hitung TOTAL JUMLAH dari semua nilai di kolom 'amount'".
        // FROM penalties          : Dari tabel 'penalties'.
        // WHERE penalty_status = 'paid': "Tapi, hanya hitung baris yang statusnya 'paid'".
        // hanya untuk baris yang statusnya 'paid'.
        String sql = "SELECT SUM(amount) FROM penalties WHERE penalty_status = 'paid'";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                // Hasil SUM ada di kolom pertama.
                return rs.getDouble(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0; // Kembalikan 0 jika tidak ada denda yang dibayar atau terjadi error.
    }

    /**
     * Mengubah status sebuah denda (misalnya dari 'pending' menjadi 'paid').
     * @param penaltyId ID denda yang akan diubah.
     * @param newStatus Status baru.
     * @param conn Koneksi database untuk transaksi.
     * @return true jika berhasil, false jika gagal.
     * @throws SQLException jika ada error SQL.
     */
    public static boolean updatePenaltyStatus(int penaltyId, String newStatus, Connection conn) throws SQLException {
        // Perintah SQL untuk mengubah (UPDATE) data.
        String sql = "UPDATE penalties SET penalty_status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, penaltyId);
            // 'executeUpdate()' mengembalikan jumlah baris yang terpengaruh.
            // Jika > 0, berarti update berhasil.
            return ps.executeUpdate() > 0;
        }
    }
}