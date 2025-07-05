package com.hotelapp.dao;

import com.hotelapp.model.User;
import com.hotelapp.util.Database;
import com.hotelapp.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Ini adalah kelas DAO (Data Access Object) untuk Pengguna (User).
 * Mengelola semua interaksi dengan tabel 'users' di database.
 */
public class UserDAO {

    /**
     * Mendaftarkan pengguna baru ke database.
     * @param username
     * @param name
     * @param email
     * @param password Password mentah (belum di-hash).
     * @param role Peran pengguna.
     * @return true jika berhasil.
     */
    public static boolean registerUser(String username, String name, String email, String password, String role) {
        String query = "INSERT INTO users (username, name, email, password, role, account_status) VALUES (?, ?, ?, ?, ?, 'active')";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, name);
            stmt.setString(3, email);
            // PENTING: Jangan simpan password mentah! Hash dulu sebelum disimpan.
            stmt.setString(4, PasswordUtil.hashPassword(password));
            stmt.setString(5, role);

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            // Error ini biasanya terjadi jika ada UNIQUE constraint yang dilanggar (username/email sudah ada).
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Mencari pengguna berdasarkan username.
     * @param username Username yang dicari.
     * @return Objek User jika ditemukan, jika tidak null.
     */
    public static User getUserByUsername(String username) {
        String query = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Jika ditemukan, ubah hasilnya menjadi objek User.
                return mapUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Mencari pengguna berdasarkan ID-nya.
     * @param id ID pengguna.
     * @return Objek User jika ditemukan.
     */
    public static User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapUserFromResultSet(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Mengubah peran (role) seorang pengguna.
     * @param userId ID pengguna yang akan diubah.
     * @param newRole Peran baru (misal: dari 'PENDING' menjadi 'customer').
     * @return true jika berhasil.
     */
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

    /**
     * Mencari pengguna berdasarkan kata kunci di nama, username, atau email.
     * @param keyword Kata kunci pencarian.
     * @return Daftar pengguna yang cocok.
     */
    public static List<User> searchUsers(String keyword) {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE name LIKE ? OR username LIKE ? OR email LIKE ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    userList.add(mapUserFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userList;
    }

    /**
     * Mengautentikasi pengguna: mencari berdasarkan username dan memverifikasi passwordnya.
     * @param username
     * @param password Password mentah yang dimasukkan pengguna saat login.
     * @return Objek User jika login berhasil, atau null jika gagal.
     */
    public static User authenticate(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND account_status = 'active'";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Ambil password yang sudah di-hash dari database.
                String storedHash = rs.getString("password");
                // PENTING: Bandingkan password mentah dengan hash menggunakan fungsi verifikasi.
                // Jangan pernah membandingkan hash secara langsung.
                if (PasswordUtil.verifyPassword(password, storedHash)) {
                    // Jika cocok, buat objek User dan kembalikan.
                    return mapUserFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Login gagal.
    }

    /**
     * Menghitung jumlah total pengguna dengan peran 'customer'.
     * @return Jumlah total customer.
     */
    public static int getTotalCustomers() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'customer'";
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
     * Mengambil daftar semua pengguna dari database.
     * @return Daftar semua pengguna.
     */
    public static List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                userList.add(mapUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userList;
    }

    /**
     * Mengubah data pengguna yang sudah ada.
     * @param user Objek User dengan data baru.
     * @return true jika berhasil.
     */
    public static boolean updateUser(User user) {
        String sql = "UPDATE users SET username = ?, name = ?, email = ?, role = ?, account_status = ? WHERE id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getRole());
            ps.setString(5, user.getAccountStatus());
            ps.setInt(6, user.getId());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Menonaktifkan akun pengguna (soft delete).
     * @param userId ID pengguna yang akan dinonaktifkan.
     * @return true jika berhasil.
     */
    public static boolean deleteUser(int userId) {
        String sql = "UPDATE users SET account_status = 'inactive' WHERE id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Mengubah password pengguna.
     * @param userId ID pengguna.
     * @param oldPassword Password lama untuk verifikasi.
     * @param newPassword Password baru.
     * @return true jika berhasil.
     */
    public static boolean changePassword(int userId, String oldPassword, String newPassword) {
        String sqlUser = "SELECT password FROM users WHERE id = ?";
        String sqlUpdate = "UPDATE users SET password = ? WHERE id = ?";

        try (Connection conn = Database.getConnection()) {
            // Pertama, ambil password hash yang sekarang dari database.
            try (PreparedStatement psUser = conn.prepareStatement(sqlUser)) {
                psUser.setInt(1, userId);
                ResultSet rs = psUser.executeQuery();

                if (rs.next()) {
                    String currentHashedPassword = rs.getString("password");
                    // Verifikasi apakah password lama yang dimasukkan cocok.
                    if (PasswordUtil.verifyPassword(oldPassword, currentHashedPassword)) {
                        // Jika cocok, hash password baru.
                        String newHashedPassword = PasswordUtil.hashPassword(newPassword);

                        // Update password di database dengan hash yang baru.
                        try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate)) {
                            psUpdate.setString(1, newHashedPassword);
                            psUpdate.setInt(2, userId);
                            int rowsAffected = psUpdate.executeUpdate();
                            return rowsAffected > 0;
                        }
                    } else {
                        return false; // Password lama salah.
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Mencari pengguna berdasarkan alamat email.
     * @param email Email yang dicari.
     * @return Objek User jika ditemukan.
     */
    public static User getUserByEmail(String email) {
        String query = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Mengubah password pengguna (tanpa verifikasi password lama).
     * Digunakan untuk fitur 'lupa password'.
     * @param userId ID pengguna.
     * @param newHashedPassword Password baru yang sudah di-hash.
     * @return true jika berhasil.
     */
    public static boolean updatePassword(int userId, String newHashedPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newHashedPassword);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Fungsi pembantu untuk mengubah baris data dari ResultSet menjadi objek User.
     * Ini digunakan untuk menghindari penulisan kode yang berulang-ulang.
     * @param rs ResultSet yang berisi data pengguna dari database.
     * @return Objek User yang sudah terisi data.
     * @throws SQLException jika terjadi error saat membaca data dari ResultSet.
     */
    private static User mapUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password"), // Mengambil password hash (walaupun tidak selalu dipakai).
                rs.getString("role")
        );
        user.setAccountStatus(rs.getString("account_status"));
        return user;
    }
}