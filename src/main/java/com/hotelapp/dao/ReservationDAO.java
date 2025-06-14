package com.hotelapp.dao;

import com.hotelapp.model.Reservation;
import com.hotelapp.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    public static boolean createReservation(Reservation reservation) {
        String sql = "INSERT INTO reservations (user_id, room_id, check_in, check_out, payment_method, booking_type, status, total_price, created_at, guest_name) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), ?)";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Jika offline, user_id bisa diset null (asumsikan reservation.getUserId() mengembalikan 0 untuk offline)
            if (reservation.getUserId() == 0) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, reservation.getUserId());
            }
            ps.setInt(2, reservation.getRoomId());
            ps.setDate(3, java.sql.Date.valueOf(reservation.getCheckIn()));
            ps.setDate(4, java.sql.Date.valueOf(reservation.getCheckOut()));
            ps.setString(5, reservation.getPaymentMethod());
            ps.setString(6, reservation.getBookingType());
            ps.setString(7, reservation.getStatus());
            ps.setDouble(8, reservation.getTotalPrice());
            ps.setString(9, reservation.getGuestName());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        reservation.setId(id);
                        System.out.println("✅ Reservasi tersimpan dengan ID: " + id);
                    }
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<Reservation> getReservationsByUserId(int userId) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT id, user_id, room_id, check_in, check_out, payment_method, status, total_price FROM reservations WHERE user_id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Reservation reservation = new Reservation(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("room_id"),
                        rs.getDate("check_in").toLocalDate(),
                        rs.getDate("check_out").toLocalDate(),
                        rs.getString("payment_method"),
                        rs.getString("status"),
                        rs.getDouble("total_price")
                );
                reservations.add(reservation);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return reservations;
    }

    public static boolean updatePaymentStatus(int reservationId, String status) {
        String sql = "UPDATE reservations SET payment_status = ? WHERE id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, reservationId);

            int rowsAffected = ps.executeUpdate();
            System.out.println("Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<Reservation> getAllReservations() {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservations";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapReservation(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Reservation> searchReservations(String keyword) {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservations WHERE user_id LIKE ? OR id LIKE ?";
        // Jika kamu memiliki kolom nama tamu, ganti query sesuai
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);  // Sesuaikan jika ingin mencari berdasarkan ID atau kolom lainnya
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapReservation(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean processCheckIn(int reservationId) {
        String sql = "UPDATE reservations SET status = 'checked_in', check_in_time = NOW() WHERE id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static int getJumlahReservasiHariIni() {
        String sql = "SELECT COUNT(*) FROM reservations WHERE DATE(created_at) = CURDATE()";
        return getCount(sql);
    }

    public static int getJumlahCheckInHariIni() {
        String sql = "SELECT COUNT(*) FROM reservations WHERE status = 'checked_in' AND DATE(check_in_time) = CURDATE()";
        return getCount(sql);
    }

    public static int getJumlahCheckOutHariIni() {
        String sql = "SELECT COUNT(*) FROM reservations WHERE status = 'checked_out' AND DATE(check_out_time) = CURDATE()";
        return getCount(sql);
    }

    private static int getCount(String sql) {
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static List<Reservation> getReservationsForCheckIn() {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservations WHERE status = 'pending' AND DATE(check_in) = CURDATE()";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapReservation(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Reservation> getReservationsForCheckOut() {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservations WHERE status = 'checked_in'";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapReservation(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Proses check-out secara langsung tanpa penalty.
     * Perbaharui status menjadi 'checked_out' dan catat waktu check_out.
     */
    public static boolean processCheckOut(int reservationId) {
        String sql = "UPDATE reservations SET status = 'checked_out', check_out_time = NOW(), penalty_status = 'paid' WHERE id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Mengaplikasikan penalty ke dalam reservasi dengan menambahkan penalty fee ke total_price
     * dan mengubah penalty_status menjadi 'pending'.
     */
    public static boolean applyPenalty(int reservationId, double penaltyAmount) {
        String sql = "UPDATE reservations SET penalty_status = 'pending', total_price = total_price + ? WHERE id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, penaltyAmount);
            ps.setInt(2, reservationId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Menambahkan entri penalty ke tabel penalties (jika Anda menggunakan tabel penalties terpisah).
     */
    public static boolean addPenalty(int reservationId, String reason, double amount) {
        String sql = "INSERT INTO penalties (reservation_id, reason, amount, penalty_status, created_at) VALUES (?, ?, ?, 'pending', NOW())";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            ps.setString(2, reason);
            ps.setDouble(3, amount);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static Reservation mapReservation(ResultSet rs) throws SQLException {
        return new Reservation(
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
    }
}