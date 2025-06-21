package com.hotelapp.dao;

import com.hotelapp.model.Penalty;
import com.hotelapp.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PenaltyDAO {
    public static boolean addPenalty(Penalty penalty, Connection con) throws SQLException {
        String sql = "INSERT INTO penalties (reservation_id, amount, reason, penalty_status, created_at) VALUES (?, ?, ?, ?, NOW())";
        try (PreparedStatement ps = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, penalty.getReservationId());
            ps.setDouble(2, penalty.getAmount());
            ps.setString(3, penalty.getReason());
            ps.setString(4, penalty.getPenaltyStatus());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        penalty.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public static List<Penalty> getAllPenalties() {
        List<Penalty> penalties = new ArrayList<>();
        String sql = "SELECT * FROM penalties";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
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

                penalties.add(penalty);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return penalties;
    }

    public static Penalty getPenaltyById(int penaltyId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM penalties WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, penaltyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
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
        return null;
    }

    public static double getTotalPaidPenalties() {
        String sql = "SELECT SUM(amount) FROM penalties WHERE penalty_status = 'paid'";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public static boolean updatePenaltyStatus(int penaltyId, String newStatus, Connection conn) throws SQLException {
        String sql = "UPDATE penalties SET penalty_status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, penaltyId);
            return ps.executeUpdate() > 0;
        }
    }

}