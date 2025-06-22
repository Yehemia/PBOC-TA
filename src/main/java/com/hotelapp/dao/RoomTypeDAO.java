package com.hotelapp.dao;

import com.hotelapp.model.Facility;
import com.hotelapp.model.RoomType;
import com.hotelapp.util.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomTypeDAO {
    public static List<RoomType> getRoomTypesWithAvailability() {
        List<RoomType> roomTypes = new ArrayList<>();
        String sql = "SELECT rt.*, COUNT(r.id) as available_rooms_count " +
                "FROM room_types rt " +
                "LEFT JOIN rooms r ON rt.id = r.room_type_id AND r.status = 'available' " +
                "GROUP BY rt.id";

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                RoomType roomType = new RoomType(
                        rs.getInt("id"), rs.getString("name"), rs.getDouble("price"),
                        rs.getString("description"), rs.getInt("max_guests"),
                        rs.getString("bed_info"), rs.getString("image_url")
                );
                roomType.setAvailableRoomCount(rs.getInt("available_rooms_count"));
                roomTypes.add(roomType);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return roomTypes;
    }

    public static RoomType getRoomTypeWithFacilitiesById(int roomTypeId) {
        String sql = "SELECT rt.*, f.id as facility_id, f.name as facility_name, f.icon_literal as facility_icon, " +
                "(SELECT COUNT(*) FROM rooms r WHERE r.room_type_id = rt.id AND r.status = 'available') AS available_rooms_count " +
                "FROM room_types rt " +
                "LEFT JOIN room_type_facilities rtf ON rt.id = rtf.room_type_id " +
                "LEFT JOIN facilities f ON rtf.facility_id = f.id " +
                "WHERE rt.id = ?";

        RoomType roomType = null;
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, roomTypeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (roomType == null) {
                    roomType = new RoomType(
                            rs.getInt("id"), rs.getString("name"), rs.getDouble("price"),
                            rs.getString("description"), rs.getInt("max_guests"),
                            rs.getString("bed_info"), rs.getString("image_url")
                    );
                    roomType.setAvailableRoomCount(rs.getInt("available_rooms_count"));
                }
                if (rs.getString("facility_name") != null) {
                    roomType.getFacilities().add(new Facility(
                            rs.getInt("facility_id"), rs.getString("facility_name"), rs.getString("facility_icon")
                    ));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return roomType;
    }

    public static List<RoomType> getAllRoomTypes() {
        List<RoomType> roomTypes = new ArrayList<>();
        String sql = "SELECT * FROM room_types";
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
        return -1;
    }

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

    public static void clearFacilitiesForRoomType(int roomTypeId, Connection con) throws SQLException {
        String sql = "DELETE FROM room_type_facilities WHERE room_type_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, roomTypeId);
            ps.executeUpdate();
        }
    }

    public static void linkFacilityToRoomType(int roomTypeId, int facilityId, Connection con) throws SQLException {
        String sql = "INSERT INTO room_type_facilities (room_type_id, facility_id) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, roomTypeId);
            ps.setInt(2, facilityId);
            ps.executeUpdate();
        }
    }
    public static boolean deleteRoomType(int roomTypeId) {
        String sql = "DELETE FROM room_types WHERE id = ?";
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, roomTypeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

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

    public static List<RoomType> findRoomTypesByFacilities(List<Integer> facilityIds) {
        if (facilityIds == null || facilityIds.isEmpty()) {
            return getAllRoomTypes();
        }

        List<RoomType> roomTypes = new ArrayList<>();
        String sql = "SELECT rt.* FROM room_types rt " +
                "JOIN room_type_facilities rtf ON rt.id = rtf.room_type_id " +
                "WHERE rtf.facility_id IN (" +
                String.join(",", java.util.Collections.nCopies(facilityIds.size(), "?")) +
                ") " +
                "GROUP BY rt.id " +
                "HAVING COUNT(DISTINCT rtf.facility_id) = ?";

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            int index = 1;
            for (Integer facilityId : facilityIds) {
                ps.setInt(index++, facilityId);
            }
            ps.setInt(index, facilityIds.size());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RoomType roomType = new RoomType(
                            rs.getInt("id"), rs.getString("name"), rs.getDouble("price"),
                            rs.getString("description"), rs.getInt("max_guests"),
                            rs.getString("bed_info"), rs.getString("image_url")
                    );
                    roomTypes.add(roomType);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return roomTypes;
    }
}