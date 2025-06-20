package com.hotelapp.service;

import com.hotelapp.dao.PenaltyDAO;
import com.hotelapp.dao.ReservationDAO;
import com.hotelapp.dao.RoomDAO;
import com.hotelapp.model.Penalty;
import com.hotelapp.model.Reservation;
import com.hotelapp.model.Room;
import com.hotelapp.model.User;
import com.hotelapp.util.Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ReservationService {

    public Reservation createBooking(User customer, Room room, LocalDate checkIn, LocalDate checkOut, String paymentMethod)
            throws SQLException, BookingException {
        if (checkIn == null || checkOut == null || paymentMethod == null) {
            throw new BookingException("Semua field wajib diisi.");
        }
        if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
            throw new BookingException("Tanggal Check-Out harus setelah tanggal Check-In.");
        }

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights <= 0) {
            throw new BookingException("Durasi menginap tidak valid.");
        }

        double totalPrice = room.getRoomType().getPrice() * nights;
        Reservation reservation = new Reservation(
                customer.getId(),
                room.getId(),
                checkIn,
                checkOut,
                paymentMethod,
                "online",
                "pending",
                totalPrice,
                customer.getName()
        );
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            ReservationDAO.createReservation(reservation, conn);
            RoomDAO.updateRoomStatus(room.getId(), "booked", conn);

            conn.commit();
            System.out.println("✅ Booking berhasil dibuat & status kamar diupdate, ID: " + reservation.getId());
            return reservation;}
        catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw new SQLException("Gagal menyimpan reservasi ke database.", e);
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    public Reservation createOfflineBooking(Room room, String guestName, LocalDate checkIn, LocalDate checkOut, String paymentMethod)
            throws SQLException, BookingException {

        if (guestName == null || guestName.isBlank()) {
            throw new BookingException("Nama tamu wajib diisi.");
        }
        if (room == null) {
            throw new BookingException("Kamar harus dipilih.");
        }

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        double totalPrice = room.getRoomType().getPrice() * nights;

        Reservation reservation = new Reservation(
                0,
                room.getId(),
                checkIn,
                checkOut,
                paymentMethod,
                "offline",
                "pending",
                totalPrice,
                guestName
        );

        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            ReservationDAO.createReservation(reservation, conn);
            RoomDAO.updateRoomStatus(room.getId(), "booked", conn);

            conn.commit();
            System.out.println("✅ Booking offline berhasil dibuat & status kamar diupdate, ID: " + reservation.getId());
            return reservation;}
        catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw new SQLException("Gagal menyimpan reservasi offline ke database.", e);
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


