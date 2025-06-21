package com.hotelapp.service;

import com.hotelapp.dao.PenaltyDAO;
import com.hotelapp.dao.ReservationDAO;
import com.hotelapp.dao.RoomDAO;
import com.hotelapp.model.*;
import com.hotelapp.util.Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ReservationService {

    public Reservation createBooking(User customer, RoomType roomType, LocalDate checkIn, LocalDate checkOut, String paymentMethod)
            throws SQLException, BookingException {

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        double totalPrice = roomType.getPrice() * nights;

        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            Room availableRoom = RoomDAO.findFirstAvailableRoom(roomType.getId(), conn);
            if (availableRoom == null) {
                throw new BookingException("Maaf, semua kamar tipe " + roomType.getName() + " sudah penuh dipesan.");
            }
            RoomDAO.updateRoomStatus(availableRoom.getId(), "booked", conn);
            Reservation reservation = new Reservation(
                    customer.getId(),
                    availableRoom.getId(),
                    checkIn, checkOut, paymentMethod, "online", "pending",
                    totalPrice, customer.getName()
            );
            ReservationDAO.createReservation(reservation, conn);
            conn.commit();
            return reservation;

        } catch (SQLException | BookingException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    public Reservation createOfflineBooking(RoomType roomType, String guestName, LocalDate checkIn, LocalDate checkOut, String paymentMethod) throws SQLException, BookingException {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        double totalPrice = roomType.getPrice() * nights;

        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);

            Room availableRoom = RoomDAO.findFirstAvailableRoom(roomType.getId(), conn);
            if (availableRoom == null) {
                throw new BookingException("Maaf, semua kamar tipe " + roomType.getName() + " sudah penuh.");
            }

            RoomDAO.updateRoomStatus(availableRoom.getId(), "booked", conn);
            Reservation reservation = new Reservation(
                    0,
                    availableRoom.getId(),
                    checkIn, checkOut, paymentMethod, "offline", "pending",
                    totalPrice, guestName
            );
            ReservationDAO.createReservation(reservation, conn);

            conn.commit();
            return reservation;

        } catch (SQLException | BookingException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    public void processCheckIn(int reservationId) throws SQLException, BookingException {
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            Reservation reservation = ReservationDAO.getReservationById(reservationId, conn);

            if (reservation == null) {
                throw new BookingException("Reservasi dengan ID " + reservationId + " tidak ditemukan.");
            }

            if (!"pending".equalsIgnoreCase(reservation.getStatus())) {
                throw new BookingException("Reservasi ini tidak bisa di-check-in (Status saat ini: " + reservation.getStatus() + ").");
            }

            boolean success = ReservationDAO.processCheckIn(reservationId, conn);
            if (!success) {
                throw new SQLException("Proses check-in gagal di level DAO.");
            }

            conn.commit();
            System.out.println("✅ Check-in untuk reservasi ID " + reservationId + " berhasil.");

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

    public void confirmPayment(int reservationId) throws SQLException {
        boolean success = ReservationDAO.updatePaymentStatus(reservationId, "paid");
        if (!success) {
            throw new SQLException("Gagal mengupdate status pembayaran untuk reservasi ID: " + reservationId);
        }
        System.out.println("✅ Pembayaran untuk reservasi ID " + reservationId + " telah dikonfirmasi (paid).");
    }

    public void processCheckOut(int reservationId, Penalty penalty) throws SQLException, BookingException {
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);

            Reservation reservation = ReservationDAO.getReservationById(reservationId, conn);
            if (reservation == null) {
                throw new BookingException("Reservasi dengan ID " + reservationId + " tidak ditemukan.");
            }
            if (!"checked_in".equalsIgnoreCase(reservation.getStatus())) {
                throw new BookingException("Hanya reservasi dengan status 'checked_in' yang bisa di-check-out.");
            }

            if (penalty != null) {
                penalty.setReservationId(reservationId);
                PenaltyDAO.addPenalty(penalty, conn);
                ReservationDAO.updatePenaltyStatus(reservationId, "pending", conn);
                System.out.println("✅ Denda untuk reservasi ID " + reservationId + " berhasil ditambahkan.");
            }

            boolean success = ReservationDAO.processCheckOut(reservationId, conn);
            if (!success) {
                throw new SQLException("Proses check-out gagal di level DAO.");
            }

            String newRoomStatus;
            if (penalty != null) {
                newRoomStatus = "maintenance";
                System.out.println("Check-out dengan denda. Status kamar ID " + reservation.getRoomId() + " diubah menjadi 'maintenance'.");
            } else {
                newRoomStatus = "available";
                System.out.println("Check-out bersih. Status kamar ID " + reservation.getRoomId() + " diubah menjadi 'available'.");
            }
            RoomDAO.updateRoomStatus(reservation.getRoomId(), newRoomStatus, conn);
            conn.commit();
            System.out.println("✅ Check-out untuk reservasi ID " + reservationId + " berhasil diproses.");

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


