package com.hotelapp.dao;

import com.hotelapp.model.Payment;
import com.hotelapp.util.Database;

import java.sql.*;

public class PaymentDAO {

    public static boolean createPayment(Payment payment) {
        String sql = "INSERT INTO payments (reservation_id, amount, status) VALUES (?, ?, ?)";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, payment.getReservationId());
            ps.setDouble(2, payment.getAmount());
            ps.setString(3, payment.getStatus()); // misalnya "paid" atau "unpaid"

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        payment.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}