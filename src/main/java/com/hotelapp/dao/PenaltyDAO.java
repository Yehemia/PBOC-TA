package com.hotelapp.dao;

import com.hotelapp.model.Penalty;
import com.hotelapp.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PenaltyDAO {

    // Menambahkan penalty baru ke dalam tabel penalties.
    public static boolean addPenalty(Penalty penalty) {
        String sql = "INSERT INTO penalties (reservation_id, amount, reason, penalty_status, created_at) VALUES (?, ?, ?, ?, NOW())";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

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
        } catch (Exception e) {
            e.printStackTrace();
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

    // Metode untuk memperbarui status penalty, misalnya dari "pending" menjadi "paid"
    public static boolean updatePenaltyStatus(int penaltyId, String newStatus) {
        String sql = "UPDATE penalties SET penalty_status = ? WHERE id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setInt(2, penaltyId);
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}