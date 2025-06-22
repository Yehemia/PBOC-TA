package com.hotelapp.service;

import com.hotelapp.dao.PenaltyDAO;
import com.hotelapp.dao.ReservationDAO;
import com.hotelapp.dao.RoomDAO;
import com.hotelapp.model.Penalty;
import com.hotelapp.model.Reservation;
import com.hotelapp.util.Database;

import java.sql.Connection;
import java.sql.SQLException;

public class PenaltyService {

    public void markPenaltyAsPaid(int penaltyId, double paidAmount) throws SQLException, BookingException {
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);

            Penalty penalty = PenaltyDAO.getPenaltyById(penaltyId, conn);
            if (penalty == null) {
                throw new BookingException("Data denda dengan ID " + penaltyId + " tidak ditemukan.");
            }
            if (!"pending".equalsIgnoreCase(penalty.getPenaltyStatus())) {
                throw new BookingException("Denda ini sudah lunas atau statusnya tidak valid.");
            }
            if (penalty != null) {
                ReservationDAO.updatePenaltyStatus(penalty.getReservationId(), "paid", conn);
            }
            if (paidAmount < penalty.getAmount()) {
                throw new BookingException("Jumlah pembayaran kurang dari total denda.");
            }

            boolean success = PenaltyDAO.updatePenaltyStatus(penaltyId, "paid", conn);
            if (!success) {
                throw new SQLException("Gagal mengupdate status denda di database.");
            }

            Reservation reservation = ReservationDAO.getReservationById(penalty.getReservationId(), conn);
            if (reservation != null) {
                RoomDAO.updateRoomStatus(reservation.getRoomId(), "available", conn);
                System.out.println("Denda lunas. Status kamar ID " + reservation.getRoomId() + " dikembalikan menjadi 'available'.");
            }


            conn.commit();
            System.out.println("✅ Denda ID " + penaltyId + " berhasil ditandai lunas.");

        } catch (SQLException | BookingException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
}