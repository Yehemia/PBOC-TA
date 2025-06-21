package com.hotelapp.service;

import com.hotelapp.dao.RoomTypeDAO;
import com.hotelapp.model.Facility;
import com.hotelapp.model.RoomType;
import com.hotelapp.util.Database;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class RoomTypeService {
    public void saveRoomType(RoomType roomType, List<Facility> facilities) throws SQLException {
        Connection con = null;
        try {
            con = Database.getConnection();
            con.setAutoCommit(false);

            if (roomType.getId() > 0) {
                RoomTypeDAO.updateRoomType(roomType, con);
                RoomTypeDAO.clearFacilitiesForRoomType(roomType.getId(), con);
                for (Facility facility : facilities) {
                    RoomTypeDAO.linkFacilityToRoomType(roomType.getId(), facility.getId(), con);
                }
            } else {
                int newRoomTypeId = RoomTypeDAO.createRoomType(roomType, con);
                if (newRoomTypeId == -1) throw new SQLException("Gagal membuat RoomType baru.");
                for (Facility facility : facilities) {
                    RoomTypeDAO.linkFacilityToRoomType(newRoomTypeId, facility.getId(), con);
                }
            }

            con.commit();
        } catch (SQLException e) {
            if (con != null) con.rollback();
            throw e;
        } finally {
            if (con != null) {
                con.setAutoCommit(true);
                con.close();
            }
        }
    }
}