package com.hotelapp.dao;

import com.hotelapp.model.Reservation;
import com.hotelapp.util.Database;

import java.sql.*;
import java.util.*;

public class ReservationDAO {

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

    public static List<Reservation> getReservationsByUserId(int userId) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT res.id, res.booking_code, res.room_id, res.check_in, res.check_out, res.status, " +
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
                // Ambil dan set kode booking
                reservation.setBookingCode(rs.getString("booking_code"));
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
        String sql = "SELECT res.*, r.room_number " +
                "FROM reservations res " +
                "JOIN rooms r ON res.room_id = r.id " +
                "ORDER BY res.id DESC";

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

    public static boolean processCheckIn(int reservationId, Connection conn) throws SQLException {
        String sql = "UPDATE reservations SET status = 'checked_in', check_in_time = NOW() WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            return ps.executeUpdate() > 0;
        }
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

    public static int getReservationCount(String searchTerm, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM reservations res WHERE (res.guest_name LIKE ? OR res.booking_code LIKE ?)");

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
                ps.setDate(paramIndex++, java.sql.Date.valueOf(endDate));
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

    public static List<Reservation> getReservationsByPage(int pageIndex, int rowsPerPage, String searchTerm, java.time.LocalDate startDate, java.time.LocalDate endDate) {
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
                Reservation reservation = mapReservation(rs);
                reservation.setRoomNumber(rs.getInt("room_number"));
                list.add(reservation);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
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

    public static boolean processCheckOut(int reservationId, Connection conn) throws SQLException {
        String sql = "UPDATE reservations SET status = 'checked_out', check_out_time = NOW() WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            return ps.executeUpdate() > 0;
        }
    }

    public static boolean updatePenaltyStatus(int reservationId, String status, Connection conn) throws SQLException {
        String sql = "UPDATE reservations SET penalty_status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, reservationId);
            return ps.executeUpdate() > 0;
        }
    }

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
        // Tambahkan baris ini
        reservation.setBookingCode(rs.getString("booking_code"));
        return reservation;
    }

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

    public static int getTotalReservations() {
        String sql = "SELECT COUNT(*) FROM reservations";
        return getCount(sql);
    }

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

    public static Map<String, Double> getDailyRevenueTrend(int limit) {
        Map<String, Double> dailyRevenue = new LinkedHashMap<>();
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

    public static void updateStatus(int reservationId, String newStatus, Connection conn) throws SQLException {
        String sql = "UPDATE reservations SET status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, reservationId);
            ps.executeUpdate();
        }
    }
}