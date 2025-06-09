package com.hotelapp.dao;

import com.hotelapp.model.User;
import com.hotelapp.util.Database;
import com.hotelapp.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public static boolean registerUser(String username, String name, String email, String password, String role) {
        String query = "INSERT INTO users (username, name, email, password, role) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, name);
            stmt.setString(3, email);
            stmt.setString(4, PasswordUtil.hashPassword(password));
            stmt.setString(5, role);

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Ambil data user berdasarkan username (digunakan untuk verifikasi registrasi)
    public static User getUserByUsername(String username) {
        String query = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Mengembalikan objek User tanpa memasukkan password, agar lebih aman.
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("role")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static User getUserById(int id) {
        String sql = "SELECT id, username, name, email, role FROM users WHERE id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // Mengambil data dari kolom database sesuai dengan model User kamu
                int userId = rs.getInt("id");
                String username = rs.getString("username");
                String name = rs.getString("name");
                String email = rs.getString("email");
                String role = rs.getString("role");
                return new User(userId, username, name, email, role);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Jika user tidak ditemukan atau terjadi error
    }

    // Metode untuk memperbarui role user, misalnya dari PENDING menjadi customer
    public static boolean updateRole(int userId, String newRole) {
        String query = "UPDATE users SET role = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newRole);
            stmt.setInt(2, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    // Metode autentikasi login, menggunakan hash untuk verifikasi password.
    public static User authenticate(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && PasswordUtil.verifyPassword(password, rs.getString("password"))) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("role")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}