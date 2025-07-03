package com.hotelapp.dao;

import com.hotelapp.model.Room;
import com.hotelapp.model.RoomType;
import com.hotelapp.util.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {
    public static Room findFirstAvailableRoom(int roomTypeId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM rooms WHERE room_type_id = ? AND status = 'available' AND is_active = TRUE LIMIT 1 FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomTypeId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                RoomType roomType = RoomTypeDAO.getRoomTypeById(roomTypeId);
                return new Room(
                        rs.getInt("id"),
                        rs.getInt("room_number"),
                        rs.getString("status"),
                        roomType
                );
            }
        }
        return null;
    }
    public static Room getRoomById(int roomId) {
        String sql = "SELECT * FROM rooms WHERE id = ?";
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int roomTypeId = rs.getInt("room_type_id");
                RoomType roomType = RoomTypeDAO.getRoomTypeWithFacilitiesById(roomTypeId);
                if (roomType != null) {
                    return new Room(
                            rs.getInt("id"),
                            rs.getInt("room_number"),
                            rs.getString("status"),
                            roomType
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Room> getAvailableRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms WHERE status = 'available' AND is_active = TRUE";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int roomTypeId = rs.getInt("room_type_id");
                RoomType roomType = RoomTypeDAO.getRoomTypeWithFacilitiesById(roomTypeId);

                if (roomType != null) {
                    Room room = new Room(
                            rs.getInt("id"),
                            rs.getInt("room_number"),
                            rs.getString("status"),
                            roomType
                    );
                    rooms.add(room);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return rooms;
    }

    public static int getTotalRooms() {
        String sql = "SELECT COUNT(*) FROM rooms";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static List<Room> getRoomsByTypeId(int typeId) {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms WHERE room_type_id = ? AND is_active = TRUE";
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, typeId);
            ResultSet rs = ps.executeQuery();
            RoomType roomType = RoomTypeDAO.getRoomTypeById(typeId);
            while (rs.next()) {
                if (roomType != null) {
                    rooms.add(new Room(
                            rs.getInt("id"),
                            rs.getInt("room_number"),
                            rs.getString("status"),
                            roomType
                    ));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return rooms;
    }
    public static boolean createRoom(Room room) {
        String sql = "INSERT INTO rooms (room_number, status, room_type_id) VALUES (?, ?, ?)";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, room.getRoomNumber());
            ps.setString(2, room.getStatus());
            ps.setInt(3, room.getRoomType().getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean updateRoom(Room room) {
        String sql = "UPDATE rooms SET room_number = ?, status = ?, room_type_id = ? WHERE id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, room.getRoomNumber());
            ps.setString(2, room.getStatus());
            ps.setInt(3, room.getRoomType().getId());
            ps.setInt(4, room.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean deleteRoom(int roomId) {
        String sql = "UPDATE rooms SET is_active = FALSE WHERE id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public static boolean roomNumberExists(int roomNumber) {
        String sql = "SELECT COUNT(*) FROM rooms WHERE room_number = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, roomNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static List<Room> searchRooms(String keyword) {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT r.* FROM rooms r " +
                "JOIN room_types rt ON r.room_type_id = rt.id " +
                "WHERE r.room_number LIKE ? OR rt.name LIKE ?";

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            ps.setString(1, searchPattern); // r.room_number
            ps.setString(2, searchPattern); // rt.name

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int roomTypeId = rs.getInt("room_type_id");
                    RoomType roomType = RoomTypeDAO.getRoomTypeWithFacilitiesById(roomTypeId);

                    if (roomType != null) {
                        Room room = new Room(
                                rs.getInt("id"),
                                rs.getInt("room_number"),
                                rs.getString("status"),
                                roomType
                        );
                        rooms.add(room);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rooms;
    }

    public static boolean updateRoomStatus(int roomId, String newStatus, Connection conn) throws SQLException {
        String sql = "UPDATE rooms SET status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, roomId);
            return ps.executeUpdate() > 0;
        }
    }
}