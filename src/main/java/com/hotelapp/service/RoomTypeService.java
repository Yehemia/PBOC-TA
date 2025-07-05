package com.hotelapp.service;

import com.hotelapp.dao.RoomTypeDAO;
import com.hotelapp.model.Facility;
import com.hotelapp.model.RoomType;
import com.hotelapp.util.Database;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Ini adalah kelas Service untuk logika bisnis yang terkait dengan Tipe Kamar.
 */
public class RoomTypeService {

    /**
     * Menyimpan data tipe kamar baru atau memperbarui yang sudah ada.
     * Ini adalah proses transaksional karena melibatkan beberapa tabel (room_types dan room_type_facilities).
     * * @param roomType Objek RoomType yang akan disimpan.
     * @param facilities Daftar fasilitas yang dipilih untuk tipe kamar ini.
     * @throws SQLException Jika ada error database.
     */
    public void saveRoomType(RoomType roomType, List<Facility> facilities) throws SQLException {
        Connection con = null;
        try {
            // 1. Buka koneksi dan mulai transaksi.
            con = Database.getConnection();
            con.setAutoCommit(false);

            // Cek apakah ini proses EDIT atau TAMBAH BARU.
            if (roomType.getId() > 0) { // Jika ID sudah ada, berarti ini proses EDIT.
                // a. Update data utama di tabel 'room_types'.
                RoomTypeDAO.updateRoomType(roomType, con);
                // b. Hapus semua data fasilitas lama untuk tipe kamar ini.
                RoomTypeDAO.clearFacilitiesForRoomType(roomType.getId(), con);
                // c. Masukkan kembali data fasilitas yang baru dipilih.
                for (Facility facility : facilities) {
                    if (facility != null) {
                        RoomTypeDAO.linkFacilityToRoomType(roomType.getId(), facility.getId(), con);
                    }
                }
            } else { // Jika ID 0 atau kurang, berarti ini proses TAMBAH BARU.
                // a. Buat data tipe kamar baru dan dapatkan ID-nya.
                int newRoomTypeId = RoomTypeDAO.createRoomType(roomType, con);
                if (newRoomTypeId == -1) throw new SQLException("Gagal membuat RoomType baru.");
                // b. Hubungkan fasilitas yang dipilih dengan ID tipe kamar yang baru.
                for (Facility facility : facilities) {
                    if (facility != null) {
                        RoomTypeDAO.linkFacilityToRoomType(newRoomTypeId, facility.getId(), con);
                    }
                }
            }

            // 2. Jika semua langkah berhasil, simpan perubahan.
            con.commit();

        } catch (SQLException e) {
            // Jika ada error, batalkan semua perubahan.
            if (con != null) con.rollback();
            throw e;
        } finally {
            // 3. Selalu tutup koneksi.
            if (con != null) {
                con.setAutoCommit(true);
                con.close();
            }
        }
    }
}