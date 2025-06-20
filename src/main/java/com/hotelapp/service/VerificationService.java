package com.hotelapp.service;

import com.hotelapp.util.Database;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

public class VerificationService {
    private static final Duration VALIDITY = Duration.ofMinutes(15);
    private static final Random random = new Random();

    public static String createAndSaveToken(int userId) throws SQLException {
        int codeInt = 100000 + random.nextInt(900000);
        String token = String.valueOf(codeInt);

        LocalDateTime expires = LocalDateTime.now().plus(VALIDITY);
        try (Connection conn = Database.getConnection();
             PreparedStatement p = conn.prepareStatement(
                     "INSERT INTO verification_tokens (user_id, token, expires_at, used) VALUES (?, ?, ?, false)"
             )) {
            p.setInt(1, userId);
            p.setString(2, token);
            p.setTimestamp(3, Timestamp.valueOf(expires));
            p.executeUpdate();
        }
        return token;
    }

    public static LocalDateTime getTokenExpiry(int userId) throws SQLException {
        String sql = "SELECT expires_at FROM verification_tokens " +
                "WHERE user_id = ? AND used = false " +
                "ORDER BY created_at DESC LIMIT 1";
        try (Connection conn = Database.getConnection();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, userId);
            ResultSet rs = p.executeQuery();
            if (rs.next()) {
                return rs.getTimestamp("expires_at").toLocalDateTime();
            }
        }
        return null;
    }

    public static boolean verifyToken(int userId, String tokenInput) {
        String sql = "SELECT token, expires_at, used FROM verification_tokens " +
                "WHERE user_id = ? AND token = ? AND used = false";
        try (Connection conn = Database.getConnection();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, userId);
            p.setString(2, tokenInput);
            ResultSet rs = p.executeQuery();
            if (rs.next()) {
                Timestamp ts = rs.getTimestamp("expires_at");
                LocalDateTime expiresAt = ts.toLocalDateTime();
                if (LocalDateTime.now().isBefore(expiresAt)) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void markAllTokensUsed(int userId) {
        String sql = "UPDATE verification_tokens SET used = true WHERE user_id = ? AND used = false";
        try (Connection conn = Database.getConnection();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, userId);
            p.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void logVerificationAttempt(int userId, String tokenInput, boolean success) {
        String sql = "INSERT INTO verification_logs (user_id, token_input, success) VALUES (?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, userId);
            p.setString(2, tokenInput);
            p.setBoolean(3, success);
            p.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
