package com.hotelapp.dao;

import com.hotelapp.model.Facility;
import com.hotelapp.util.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FacilityDAO {
    public static List<Facility> getAllFacilities() {
        List<Facility> facilities = new ArrayList<>();
        String sql = "SELECT * FROM facilities ORDER BY name";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                facilities.add(new Facility(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("icon_literal")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return facilities;
    }
}