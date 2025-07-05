package com.hotelapp.dao;

import com.hotelapp.model.Reservation;
import com.hotelapp.util.Database;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Ini adalah kelas DAO (Data Access Object) untuk Reservasi.
 * Kelas ini sangat penting karena mengelola semua interaksi dengan tabel 'reservations'.
 */
public class ReservationDAO {

    /**
     * Membuat reservasi baru di database.
     * @param reservation Objek Reservasi yang berisi semua data.
     * @param con Koneksi database untuk transaksi.
     * @return true jika berhasil, false jika gagal.
     * @throws SQLException jika ada error SQL.
     */
    public static boolean createReservation(Reservation reservation, Connection con) throws SQLException {
        String sql = "INSERT INTO reservations (booking_code, user_id, room_id, check_in, check_out, payment_method, booking_type, status, total_price, created_at, guest_name) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, reservation.getBookingCode());
            ps.setInt(2, reservation.getUserId());
            ps.setInt(3, reservation.getRoomId());
            ps.setDate(4, java.sql.Date.valueOf(reservation.getCheckIn()));
            ps.setDate(5, java.sql.Date.valueOf(reservation.getCheckOut()));
            ps.setString(6, reservation.getPaymentMethod());
            ps.setString(7, reservation.getBookingType());
            ps.setString(8, reservation.getStatus());
            ps.setDouble(9, reservation.getTotalPrice());
            ps.setString(10, reservation.getGuestName());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        reservation.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Mengambil daftar riwayat reservasi untuk seorang pengguna.
     * @param userId ID pengguna yang riwayatnya ingin dilihat.
     * @return Sebuah daftar (List) berisi objek-objek Reservasi.
     */
    public static List<Reservation> getReservationsByUserId(int userId) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT res.id, res.user_id, res.booking_code, res.room_id, res.check_in, res.check_out, res.status, res.payment_status, " +
                "r.room_number, rt.name as room_type_name " +
                "FROM reservations res " +
                "JOIN rooms r ON res.room_id = r.id " +
                "JOIN room_types rt ON r.room_type_id = rt.id " +
                "WHERE res.user_id = ? ORDER BY res.created_at DESC";

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Reservation reservation = new Reservation(
                        rs.getInt("id"),
                        rs.getInt("room_id"),
                        rs.getDate("check_in").toLocalDate(),
                        rs.getDate("check_out").toLocalDate(),
                        rs.getString("status"),
                        rs.getString("room_type_name"),
                        rs.getInt("room_number")
                );
                reservation.setUserId(rs.getInt("user_id"));
                reservation.setBookingCode(rs.getString("booking_code"));
                reservation.setPaymentStatus(rs.getString("payment_status"));
                reservations.add(reservation);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reservations;
    }

    /**
     * Mengubah status pembayaran sebuah reservasi (misal: dari 'pending' menjadi 'paid').
     * @param reservationId ID reservasi yang akan diubah.
     * @param status Status pembayaran baru.
     * @return true jika berhasil, false jika gagal.
     */
    public static boolean updatePaymentStatus(int reservationId, String status) {
        String sql = "UPDATE reservations SET payment_status = ? WHERE id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, reservationId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Memproses check-in di database: mengubah status dan mencatat waktu check-in.
     * @param reservationId ID reservasi.
     * @param conn Koneksi database untuk transaksi.
     * @return true jika berhasil.
     * @throws SQLException
     */
    public static boolean processCheckIn(int reservationId, Connection conn) throws SQLException {
        String sql = "UPDATE reservations SET status = 'checked_in', check_in_time = NOW() WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Menghitung jumlah total reservasi yang cocok dengan kriteria filter.
     * Digunakan untuk pagination di halaman riwayat resepsionis.
     * @param searchTerm Kata kunci pencarian (nama atau kode booking).
     * @param startDate Tanggal awal filter.
     * @param endDate Tanggal akhir filter.
     * @return Jumlah total data.
     */
    public static int getReservationCount(String searchTerm, LocalDate startDate, LocalDate endDate) {
        // StringBuilder digunakan untuk membuat query SQL secara dinamis.
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM reservations res WHERE (res.guest_name LIKE ? OR res.booking_code LIKE ?)");

        // Jika filter tanggal ada, tambahkan kondisi WHERE untuk tanggal.
        if (startDate != null && endDate != null) {
            sql.append(" AND res.check_in BETWEEN ? AND ?");
        }

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            String searchPattern = "%" + searchTerm + "%";
            ps.setString(paramIndex++, searchPattern);
            ps.setString(paramIndex++, searchPattern);

            if (startDate != null && endDate != null) {
                ps.setDate(paramIndex++, java.sql.Date.valueOf(startDate));
                ps.setDate(paramIndex, java.sql.Date.valueOf(endDate));
            }

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Mengambil data reservasi per halaman (untuk pagination).
     * @param pageIndex Halaman ke berapa yang mau diambil (mulai dari 0).
     * @param rowsPerPage Jumlah data per halaman.
     * @param searchTerm Kata kunci pencarian.
     * @param startDate Tanggal awal filter.
     * @param endDate Tanggal akhir filter.
     * @return Daftar reservasi untuk halaman tersebut.
     */
    public static List<Reservation> getReservationsByPage(int pageIndex, int rowsPerPage, String searchTerm, LocalDate startDate, LocalDate endDate) {
        List<Reservation> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT res.*, r.room_number " +
                        "FROM reservations res " +
                        "JOIN rooms r ON res.room_id = r.id " +
                        "WHERE (res.guest_name LIKE ? OR res.booking_code LIKE ?)"
        );

        if (startDate != null && endDate != null) {
            sql.append(" AND res.check_in BETWEEN ? AND ?");
        }

        // 'LIMIT ? OFFSET ?' adalah kunci untuk pagination di SQL.
        // LIMIT menentukan berapa banyak data, OFFSET menentukan mulai dari data ke berapa.
        sql.append(" ORDER BY res.id DESC LIMIT ? OFFSET ?");

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            String searchPattern = "%" + searchTerm + "%";
            ps.setString(paramIndex++, searchPattern);
            ps.setString(paramIndex++, searchPattern);

            if (startDate != null && endDate != null) {
                ps.setDate(paramIndex++, java.sql.Date.valueOf(startDate));
                ps.setDate(paramIndex++, java.sql.Date.valueOf(endDate));
            }

            ps.setInt(paramIndex++, rowsPerPage);
            ps.setInt(paramIndex, pageIndex * rowsPerPage);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                // Ubah setiap baris hasil menjadi objek Reservation.
                Reservation reservation = mapReservation(rs);
                reservation.setRoomNumber(rs.getInt("room_number"));
                list.add(reservation);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Mengambil daftar reservasi yang siap untuk di-check-in.
     * Kriterianya: status 'pending' dan tanggal check-in adalah hari ini atau sebelumnya.
     * @param keyword Kata kunci pencarian berdasarkan kode booking.
     * @return Daftar reservasi yang siap check-in.
     */
    public static List<Reservation> getReservationsForCheckIn(String keyword) {
        List<Reservation> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT res.*, r.room_number FROM reservations res " +
                        "JOIN rooms r ON res.room_id = r.id " +
                        "WHERE res.status = 'pending' AND DATE(res.check_in) <= CURDATE()"
        );

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND res.booking_code LIKE ?");
        }

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            if (keyword != null && !keyword.isBlank()) {
                ps.setString(1, "%" + keyword + "%");
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Reservation reservation = mapReservation(rs);
                reservation.setRoomNumber(rs.getInt("room_number"));
                list.add(reservation);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Mengambil daftar reservasi yang sedang dalam status 'checked_in'.
     * Ini digunakan untuk halaman Check-Out.
     * @return Daftar reservasi yang sedang menginap.
     */
    public static List<Reservation> getReservationsForCheckOut() {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT res.*, r.room_number FROM reservations res " +
                "JOIN rooms r ON res.room_id = r.id " +
                "WHERE res.status = 'checked_in'";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Reservation reservation = mapReservation(rs);
                reservation.setRoomNumber(rs.getInt("room_number"));
                list.add(reservation);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Memproses check-out di database: mengubah status dan mencatat waktu check-out.
     * @param reservationId ID reservasi.
     * @param conn Koneksi database untuk transaksi.
     * @return true jika berhasil.
     * @throws SQLException
     */
    public static boolean processCheckOut(int reservationId, Connection conn) throws SQLException {
        String sql = "UPDATE reservations SET status = 'checked_out', check_out_time = NOW() WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Mengubah status denda pada sebuah reservasi.
     * @param reservationId ID reservasi.
     * @param status Status denda baru ('pending' atau 'paid').
     * @param conn Koneksi database untuk transaksi.
     * @return true jika berhasil.
     * @throws SQLException
     */
    public static boolean updatePenaltyStatus(int reservationId, String status, Connection conn) throws SQLException {
        String sql = "UPDATE reservations SET penalty_status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, reservationId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Menghitung total pendapatan dari semua reservasi yang sudah lunas.
     * @return Total pendapatan.
     */
    public static double getTotalRevenue() {
        String sql = "SELECT SUM(total_price) FROM reservations WHERE payment_status = 'paid' AND status != 'cancelled'";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Menghitung jumlah total reservasi yang pernah ada.
     * @return Jumlah total reservasi.
     */
    public static int getTotalReservations() {
        String sql = "SELECT COUNT(*) FROM reservations";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Menghitung jumlah reservasi untuk setiap tipe kamar.
     * Digunakan untuk membuat grafik di dasbor admin.
     * @return Sebuah Map dimana key adalah nama tipe kamar dan value adalah jumlah reservasi.
     */
    public static Map<String, Integer> getRoomTypeReservationCount() {
        Map<String, Integer> roomTypeCounts = new HashMap<>();
        String sql = "SELECT rt.name, COUNT(res.id) AS reservation_count " +
                "FROM reservations res " +
                "JOIN rooms r ON res.room_id = r.id " +
                "JOIN room_types rt ON r.room_type_id = rt.id " +
                "GROUP BY rt.name";

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String roomType = rs.getString("name");
                int count = rs.getInt("reservation_count");
                roomTypeCounts.put(roomType, count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return roomTypeCounts;
    }

    /**
     * Mengambil data tren pendapatan harian untuk beberapa hari terakhir.
     * Digunakan untuk grafik garis di dasbor admin.
     * @param limit Berapa hari terakhir yang ingin ditampilkan.
     * @return Sebuah Map dimana key adalah tanggal (String) dan value adalah total pendapatan hari itu.
     */
    public static Map<String, Double> getDailyRevenueTrend(int limit) {
        Map<String, Double> dailyRevenue = new LinkedHashMap<>(); // LinkedHashMap menjaga urutan data.
        String sql = "SELECT DATE_FORMAT(created_at, '%Y-%m-%d') AS day, SUM(total_price) AS daily_total " +
                "FROM reservations " +
                "WHERE payment_status = 'paid' " +
                "GROUP BY DATE(created_at) " +
                "ORDER BY DATE(created_at) DESC " +
                "LIMIT ?";

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                dailyRevenue.put(rs.getString("day"), rs.getDouble("daily_total"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dailyRevenue;
    }

    /**
     * Mencari satu reservasi berdasarkan ID-nya.
     * @param reservationId ID reservasi yang dicari.
     * @param conn Koneksi database untuk transaksi.
     * @return Objek Reservation jika ditemukan.
     * @throws SQLException
     */
    public static Reservation getReservationById(int reservationId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM reservations WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapReservation(rs);
                }
            }
        }
        return null;
    }

    /**
     * Mengubah status sebuah reservasi (misal: dari 'pending' menjadi 'cancelled').
     * @param reservationId ID reservasi.
     * @param newStatus Status baru.
     * @param conn Koneksi database untuk transaksi.
     * @throws SQLException
     */
    public static void updateStatus(int reservationId, String newStatus, Connection conn) throws SQLException {
        String sql = "UPDATE reservations SET status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, reservationId);
            ps.executeUpdate();
        }
    }

    /**
     * Fungsi pembantu untuk mengubah baris data dari ResultSet menjadi objek Reservation.
     * Ini dilakukan agar tidak ada duplikasi kode.
     * @param rs ResultSet yang berisi data dari satu baris tabel.
     * @return Objek Reservation yang sudah terisi data.
     * @throws SQLException jika ada error saat membaca data.
     */
    private static Reservation mapReservation(ResultSet rs) throws SQLException {
        Reservation reservation = new Reservation(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getInt("room_id"),
                rs.getDate("check_in").toLocalDate(),
                rs.getDate("check_out").toLocalDate(),
                rs.getTimestamp("check_in_time") != null ? rs.getTimestamp("check_in_time").toLocalDateTime() : null,
                rs.getTimestamp("check_out_time") != null ? rs.getTimestamp("check_out_time").toLocalDateTime() : null,
                rs.getString("payment_method"),
                rs.getString("booking_type"),
                rs.getString("status"),
                rs.getDouble("total_price"),
                rs.getString("penalty_status"),
                rs.getString("payment_status"),
                rs.getString("guest_name")
        );
        reservation.setBookingCode(rs.getString("booking_code"));
        return reservation;
    }
}