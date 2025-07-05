package com.hotelapp.service;

import com.hotelapp.dao.PenaltyDAO;
import com.hotelapp.dao.ReservationDAO;
import com.hotelapp.dao.RoomDAO;
import com.hotelapp.model.Penalty;
import com.hotelapp.model.Reservation;
import com.hotelapp.util.Database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Ini adalah kelas Service untuk logika bisnis yang berkaitan dengan Denda.
 * Tugasnya mengoordinasikan beberapa DAO untuk menyelesaikan satu proses bisnis.
 */
public class PenaltyService {

    /**
     * Memproses pelunasan denda. Ini adalah proses transaksional,
     * yang berarti semua langkah di dalamnya harus berhasil, atau jika satu saja gagal,
     * semua perubahan akan dibatalkan (rollback).
     * * @param penaltyId ID denda yang akan dilunasi.
     * @param paidAmount Jumlah uang yang dibayarkan.
     * @throws SQLException Jika ada masalah dengan database.
     * @throws BookingException Jika ada masalah bisnis (misal: denda sudah lunas).
     */
    public void markPenaltyAsPaid(int penaltyId, double paidAmount) throws SQLException, BookingException {
        Connection conn = null; // Siapkan variabel koneksi di luar try-catch.
        try {
            // 1. Dapatkan koneksi ke database.
            conn = Database.getConnection();
            // 2. Matikan auto-commit. Ini adalah kunci dari transaksi.
            //    Perubahan tidak akan disimpan permanen sampai kita panggil conn.commit().
            conn.setAutoCommit(false);

            // 3. Ambil data denda dari database untuk divalidasi.
            Penalty penalty = PenaltyDAO.getPenaltyById(penaltyId, conn);
            if (penalty == null) {
                throw new BookingException("Data denda dengan ID " + penaltyId + " tidak ditemukan.");
            }
            if (!"pending".equalsIgnoreCase(penalty.getPenaltyStatus())) {
                throw new BookingException("Denda ini sudah lunas atau statusnya tidak valid.");
            }
            if (paidAmount < penalty.getAmount()) {
                throw new BookingException("Jumlah pembayaran kurang dari total denda.");
            }

            // 4. Update status denda menjadi 'paid' di tabel 'penalties'.
            boolean success = PenaltyDAO.updatePenaltyStatus(penaltyId, "paid", conn);
            if (!success) {
                throw new SQLException("Gagal mengupdate status denda di database.");
            }

            // 5. Update status denda di tabel 'reservations' juga menjadi 'paid'.
            ReservationDAO.updatePenaltyStatus(penalty.getReservationId(), "paid", conn);

            // 6. Ambil data reservasi untuk mendapatkan ID kamar.
            Reservation reservation = ReservationDAO.getReservationById(penalty.getReservationId(), conn);
            if (reservation != null) {
                // 7. Karena denda sudah lunas, ubah status kamar dari 'maintenance' menjadi 'available'.
                RoomDAO.updateRoomStatus(reservation.getRoomId(), "available", conn);
                System.out.println("Denda lunas. Status kamar ID " + reservation.getRoomId() + " dikembalikan menjadi 'available'.");
            }

            // 8. Jika semua langkah di atas berhasil tanpa error, simpan semua perubahan secara permanen.
            conn.commit();
            System.out.println("✅ Denda ID " + penaltyId + " berhasil ditandai lunas.");

        } catch (SQLException | BookingException e) {
            // Jika terjadi error di salah satu langkah di dalam blok 'try'...
            if (conn != null) {
                // 9. Batalkan semua perubahan yang sudah dilakukan (rollback).
                conn.rollback();
            }
            // Lempar kembali errornya agar bisa ditangani oleh controller.
            throw e;
        } finally {
            // Blok 'finally' akan selalu dijalankan, baik ada error maupun tidak.
            if (conn != null) {
                // 10. Kembalikan mode auto-commit ke kondisi normal dan tutup koneksi.
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
}