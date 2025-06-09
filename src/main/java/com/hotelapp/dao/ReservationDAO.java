package com.hotelapp.dao;

import com.hotelapp.model.Reservation;
import com.hotelapp.util.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class ReservationDAO {

    public static boolean saveReservation(Reservation reservation) {
        String sql = "INSERT INTO reservations (user_id, room_id, check_in, check_out, payment_method, status, total_price, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";

        try (Connection con = Database.getConnection();
             // Pastikan menyatakan bahwa kita ingin generated keys
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, reservation.getUserId());
            ps.setInt(2, reservation.getRoomId());
            ps.setDate(3, java.sql.Date.valueOf(reservation.getCheckIn()));
            ps.setDate(4, java.sql.Date.valueOf(reservation.getCheckOut()));
            ps.setString(5, reservation.getPaymentMethod());
            ps.setString(6, reservation.getStatus());
            ps.setDouble(7, reservation.getTotalPrice());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        reservation.setId(id);  // Pastikan Reservation memiliki setter untuk ID
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


//    public static Reservation getReservationById(int id) {
//        String sql = "SELECT * FROM reservations WHERE id = ?";
//
//        try (Connection con = Database.getConnection();
//             PreparedStatement ps = con.prepareStatement(sql)) {
//            ps.setInt(1, id);
//            ResultSet rs = ps.executeQuery();
//
//            if (rs.next()) {
//                return new Reservation(
//                        rs.getInt("id"),
//                        rs.getInt("user_id"),
//                        rs.getInt("room_id"),
//                        rs.getDate("check_in").toLocalDate(),
//                        rs.getDate("check_out").toLocalDate(),
//                        rs.getString("payment_method"),
//                        rs.getString("status"),
//                        rs.getDouble("total_price") // ✅ Baca total harga
//                );
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

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


}

