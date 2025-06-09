package com.hotelapp.service;

import com.hotelapp.dao.RoomDAO;
import com.hotelapp.model.Room;

import java.util.List;

public class RoomService {

    /**
     * Mengambil daftar kamar yang tersedia dari database.
     * @return daftar Room yang statusnya available.
     */
    public static List<Room> getAvailableRooms() {
        // Kamu dapat menambahkan logika tambahan di sini jika diperlukan
        return RoomDAO.getAvailableRooms();
    }
}

