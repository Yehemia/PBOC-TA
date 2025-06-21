package com.hotelapp.service;

import com.hotelapp.dao.RoomDAO;
import com.hotelapp.model.Room;

import java.util.List;

public class RoomService {
    public static List<Room> getAvailableRooms() {
        return RoomDAO.getAvailableRooms();
    }
}

