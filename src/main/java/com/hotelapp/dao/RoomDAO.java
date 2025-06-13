package com.hotelapp.dao;

import com.hotelapp.model.Room;
import com.hotelapp.util.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    public static List<Room> getAvailableRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT id,room_number, room_type, price, image_url FROM rooms WHERE status = 'available'";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String imageUrl = formatImageUrl(rs.getString("image_url"));

                Room room = new Room(
                        rs.getInt("id"),
                        rs.getInt("room_number"),
                        rs.getString("room_type"),
                        rs.getDouble("price"),
                        imageUrl
                );

                System.out.println("Formatted image URL: " + imageUrl); // Debug
                rooms.add(room);
            }
        } catch (Exception e) {
            System.err.println("Error in getAvailableRooms: " + e.getMessage());
            e.printStackTrace();
        }
        return rooms;
    }

    private static String formatImageUrl(String dbImageUrl) {
        if (dbImageUrl == null || dbImageUrl.trim().isEmpty()) {
            return "/com/hotelapp/images/default_room.jpeg";
        }
        String normalizedUrl = dbImageUrl.trim();
        if (normalizedUrl.startsWith("/com/hotelapp/images/")) {
            return normalizedUrl;
        }
        if (!normalizedUrl.contains("/")) {
            return "/com/hotelapp/images/" + normalizedUrl;
        }
        return normalizedUrl;
    }
}
