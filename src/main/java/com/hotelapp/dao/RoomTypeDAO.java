package com.hotelapp.dao;

import com.hotelapp.model.Facility;
import com.hotelapp.model.RoomType;
import com.hotelapp.util.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Ini adalah kelas DAO (Data Access Object) untuk Tipe Kamar.
 * Mengelola semua interaksi dengan tabel 'room_types' dan tabel penghubung 'room_type_facilities'.
 */
public class RoomTypeDAO {

    /**
     * Mengambil semua tipe kamar beserta informasi jumlah kamar yang tersedia saat ini.
     * @return Sebuah daftar (List) yang berisi objek-objek RoomType.
     */
    public static List<RoomType> getRoomTypesWithAvailability() {
        List<RoomType> roomTypes = new ArrayList<>();
        // Perintah SQL ini sedikit rumit:
        // 1. Mengambil semua data dari 'room_types' yang statusnya 'active'.
        // 2. Menggabungkan (LEFT JOIN) dengan tabel 'rooms' untuk menghitung (COUNT) kamar yang statusnya 'available' dan aktif.
        // 3. Mengelompokkan (GROUP BY) hasilnya berdasarkan ID tipe kamar agar hitungan kamar per tipe akurat.
        String sql = "SELECT rt.*, COUNT(r.id) as available_rooms_count " +
                "FROM room_types rt " +
                "LEFT JOIN rooms r ON rt.id = r.room_type_id AND r.status = 'available' AND r.is_active = TRUE " +
                "WHERE rt.status = 'active' " +
                "GROUP BY rt.id";

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // Buat objek RoomType dari hasil query.
                RoomType roomType = new RoomType(
                        rs.getInt("id"), rs.getString("name"), rs.getDouble("price"),
                        rs.getString("description"), rs.getInt("max_guests"),
                        rs.getString("bed_info"), rs.getString("image_url")
                );
                // Set jumlah kamar yang tersedia dari hasil COUNT.
                roomType.setAvailableRoomCount(rs.getInt("available_rooms_count"));
                roomTypes.add(roomType);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return roomTypes;
    }

    /**
     * Mengambil satu tipe kamar berdasarkan ID, lengkap dengan fasilitas-fasilitasnya.
     * @param roomTypeId ID tipe kamar yang dicari.
     * @return Objek RoomType yang sudah lengkap dengan daftar fasilitas.
     */
    public static RoomType getRoomTypeWithFacilitiesById(int roomTypeId) {
        // SQL ini menggabungkan 3 tabel: room_types, room_type_facilities (tabel penghubung), dan facilities.
        // Ini dilakukan untuk mengambil data tipe kamar dan semua fasilitasnya dalam satu kali query.
        String sql = "SELECT rt.*, f.id as facility_id, f.name as facility_name, f.icon_literal as facility_icon, " +
                "(SELECT COUNT(*) FROM rooms r WHERE r.room_type_id = rt.id AND r.status = 'available' AND r.is_active = TRUE) AS available_rooms_count " +
                "FROM room_types rt " +
                "LEFT JOIN room_type_facilities rtf ON rt.id = rtf.room_type_id " +
                "LEFT JOIN facilities f ON rtf.facility_id = f.id " +
                "WHERE rt.id = ?";

        RoomType roomType = null;
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, roomTypeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                // Objek RoomType hanya dibuat sekali saat pertama kali menemukan baris data.
                if (roomType == null) {
                    roomType = new RoomType(
                            rs.getInt("id"), rs.getString("name"), rs.getDouble("price"),
                            rs.getString("description"), rs.getInt("max_guests"),
                            rs.getString("bed_info"), rs.getString("image_url")
                    );
                    roomType.setAvailableRoomCount(rs.getInt("available_rooms_count"));
                }
                // Karena ada JOIN, satu tipe kamar bisa muncul di beberapa baris jika punya banyak fasilitas.
                // Jadi, untuk setiap baris, kita hanya perlu menambahkan fasilitasnya ke dalam list.
                if (rs.getString("facility_name") != null) {
                    roomType.getFacilities().add(new Facility(
                            rs.getInt("facility_id"), rs.getString("facility_name"), rs.getString("facility_icon")
                    ));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return roomType;
    }

    /**
     * Mengambil semua tipe kamar yang aktif tanpa informasi ketersediaan atau fasilitas.
     * @return Daftar Tipe Kamar.
     */
    public static List<RoomType> getAllRoomTypes() {
        List<RoomType> roomTypes = new ArrayList<>();
        String sql = "SELECT * FROM room_types WHERE status = 'active'";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                roomTypes.add(new RoomType(
                        rs.getInt("id"), rs.getString("name"), rs.getDouble("price"),
                        rs.getString("description"), rs.getInt("max_guests"),
                        rs.getString("bed_info"), rs.getString("image_url")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return roomTypes;
    }

    /**
     * Membuat tipe kamar baru di database.
     * @param roomType Objek RoomType yang akan disimpan.
     * @param con Koneksi database untuk transaksi.
     * @return ID dari tipe kamar yang baru dibuat.
     * @throws SQLException
     */
    public static int createRoomType(RoomType roomType, Connection con) throws SQLException {
        String sql = "INSERT INTO room_types (name, price, description, max_guests, bed_info, image_url) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, roomType.getName());
            ps.setDouble(2, roomType.getPrice());
            ps.setString(3, roomType.getDescription());
            ps.setInt(4, roomType.getMaxGuests());
            ps.setString(5, roomType.getBedInfo());
            ps.setString(6, roomType.getImageUrl());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1; // Mengembalikan -1 jika gagal mendapatkan ID.
    }

    /**
     * Mengubah data tipe kamar yang sudah ada.
     * @param roomType Objek RoomType dengan data baru.
     * @param con Koneksi database untuk transaksi.
     * @return true jika berhasil.
     * @throws SQLException
     */
    public static boolean updateRoomType(RoomType roomType, Connection con) throws SQLException {
        String sql = "UPDATE room_types SET name=?, price=?, description=?, max_guests=?, bed_info=?, image_url=? WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, roomType.getName());
            ps.setDouble(2, roomType.getPrice());
            ps.setString(3, roomType.getDescription());
            ps.setInt(4, roomType.getMaxGuests());
            ps.setString(5, roomType.getBedInfo());
            ps.setString(6, roomType.getImageUrl());
            ps.setInt(7, roomType.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Menghapus semua hubungan antara sebuah tipe kamar dengan fasilitas.
     * Ini dipanggil sebelum memperbarui fasilitas agar tidak ada data lama yang tersisa.
     * @param roomTypeId ID tipe kamar.
     * @param con Koneksi database untuk transaksi.
     * @throws SQLException
     */
    public static void clearFacilitiesForRoomType(int roomTypeId, Connection con) throws SQLException {
        String sql = "DELETE FROM room_type_facilities WHERE room_type_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, roomTypeId);
            ps.executeUpdate();
        }
    }

    /**
     * Membuat hubungan baru antara tipe kamar dan fasilitas di tabel penghubung.
     * @param roomTypeId ID tipe kamar.
     * @param facilityId ID fasilitas.
     * @param con Koneksi database untuk transaksi.
     * @throws SQLException
     */
    public static void linkFacilityToRoomType(int roomTypeId, int facilityId, Connection con) throws SQLException {
        String sql = "INSERT INTO room_type_facilities (room_type_id, facility_id) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, roomTypeId);
            ps.setInt(2, facilityId);
            ps.executeUpdate();
        }
    }

    /**
     * Menonaktifkan tipe kamar (soft delete).
     * @param roomTypeId ID tipe kamar yang akan dinonaktifkan.
     * @return true jika berhasil.
     */
    public static boolean deleteRoomType(int roomTypeId) {
        String sql = "UPDATE room_types SET status = 'inactive' WHERE id = ?";
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, roomTypeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Mencari satu tipe kamar berdasarkan ID-nya (tanpa fasilitas).
     * @param roomTypeId ID tipe kamar.
     * @return Objek RoomType jika ditemukan.
     */
    public static RoomType getRoomTypeById(int roomTypeId) {
        String sql = "SELECT * FROM room_types WHERE id = ?";
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, roomTypeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new RoomType(
                        rs.getInt("id"), rs.getString("name"), rs.getDouble("price"),
                        rs.getString("description"), rs.getInt("max_guests"),
                        rs.getString("bed_info"), rs.getString("image_url")
                );
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
}