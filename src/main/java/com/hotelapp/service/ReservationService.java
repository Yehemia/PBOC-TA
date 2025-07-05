package com.hotelapp.service;

import com.hotelapp.dao.PenaltyDAO;
import com.hotelapp.dao.ReservationDAO;
import com.hotelapp.dao.RoomDAO;
import com.hotelapp.dao.UserDAO;
import com.hotelapp.model.*;
import com.hotelapp.util.Database;
import com.hotelapp.util.EmailUtil;
import com.hotelapp.util.GeneratorUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Ini adalah kelas Service utama untuk semua logika bisnis yang terkait dengan Reservasi.
 * Kelas ini mengoordinasikan berbagai DAO untuk melakukan tugas-tugas kompleks seperti
 * membuat booking, check-in, dan check-out dalam satu transaksi yang aman.
 */
public class ReservationService {

    /**
     * Membuat booking baru untuk customer (online).
     * Ini adalah proses transaksional, artinya semua langkah harus berhasil,
     * jika satu gagal, semua akan dibatalkan (rollback).
     * @param customer Pengguna yang memesan.
     * @param roomType Tipe kamar yang dipilih.
     * @param checkIn Tanggal check-in.
     * @param checkOut Tanggal check-out.
     * @param paymentMethod Metode pembayaran.
     * @return Objek Reservasi yang baru dibuat.
     * @throws SQLException Jika ada error database.
     * @throws BookingException Jika ada error bisnis (misal: kamar penuh).
     */
    public Reservation createBooking(User customer, RoomType roomType, LocalDate checkIn, LocalDate checkOut, String paymentMethod)
            throws SQLException, BookingException {

        // Hitung durasi menginap dan total harga.
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        double totalPrice = roomType.getPrice() * nights;

        Connection conn = null; // Siapkan variabel koneksi di luar try-catch.
        try {
            // 1. Dapatkan koneksi ke database.
            conn = Database.getConnection();
            // 2. Matikan auto-commit untuk memulai mode transaksi.
            //    Perubahan tidak akan disimpan permanen sampai kita panggil conn.commit().
            conn.setAutoCommit(false);

            // 3. Cari satu kamar fisik yang tersedia dari tipe yang dipilih.
            Room availableRoom = RoomDAO.findFirstAvailableRoom(roomType.getId(), conn);
            if (availableRoom == null) {
                // Jika tidak ada kamar, lempar error bisnis yang spesifik.
                throw new BookingException("Maaf, semua kamar tipe " + roomType.getName() + " sudah penuh dipesan.");
            }

            // 4. Jika kamar ada, langsung ubah statusnya menjadi 'booked' agar tidak bisa dipesan orang lain.
            RoomDAO.updateRoomStatus(availableRoom.getId(), "booked", conn);

            // 5. Buat kode booking unik menggunakan utilitas.
            String bookingCode = GeneratorUtil.generateBookingCode();

            // 6. Siapkan objek Reservasi baru dengan semua data yang diperlukan.
            Reservation reservation = new Reservation(
                    customer.getId(),
                    availableRoom.getId(),
                    checkIn, checkOut, paymentMethod, "online", "pending",
                    totalPrice, customer.getName()
            );
            reservation.setBookingCode(bookingCode);

            // 7. Simpan data reservasi yang baru ke database.
            ReservationDAO.createReservation(reservation, conn);

            // 8. Jika semua langkah di atas berhasil tanpa error, simpan semua perubahan secara permanen.
            conn.commit();
            return reservation; // Kembalikan objek reservasi yang baru dibuat.

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

    /**
     * Membuat booking baru untuk tamu walk-in (offline) oleh resepsionis.
     * Prosesnya mirip dengan createBooking, namun beberapa data berbeda (misal: userId=0).
     */
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
            String bookingCode = GeneratorUtil.generateBookingCode();
            Reservation reservation = new Reservation(
                    0, // userId di-set 0 untuk menandakan ini bukan booking oleh customer terdaftar.
                    availableRoom.getId(),
                    checkIn, checkOut, paymentMethod, "offline", "pending",
                    totalPrice, guestName
            );
            reservation.setBookingCode(bookingCode);
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

    /**
     * Memproses Check-In untuk sebuah reservasi.
     * @param reservationId ID reservasi yang akan di-check-in.
     * @throws SQLException
     * @throws BookingException
     */
    public void processCheckIn(int reservationId) throws SQLException, BookingException {
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);

            // Ambil data reservasi untuk validasi.
            Reservation reservation = ReservationDAO.getReservationById(reservationId, conn);

            if (reservation == null) {
                throw new BookingException("Reservasi dengan ID " + reservationId + " tidak ditemukan.");
            }
            // Hanya reservasi dengan status 'pending' yang bisa di-check-in.
            if (!"pending".equalsIgnoreCase(reservation.getStatus())) {
                throw new BookingException("Reservasi ini tidak bisa di-check-in (Status saat ini: " + reservation.getStatus() + ").");
            }

            // Panggil DAO untuk mengubah status dan mencatat waktu check-in.
            boolean success = ReservationDAO.processCheckIn(reservationId, conn);
            if (!success) {
                throw new SQLException("Proses check-in gagal di level DAO.");
            }

            conn.commit();
            System.out.println("✅ Check-in untuk reservasi ID " + reservationId + " berhasil.");

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

    /**
     * Mengonfirmasi pembayaran untuk sebuah reservasi.
     * Proses ini sederhana, hanya satu kali update, jadi tidak memerlukan transaksi.
     * @param reservationId ID reservasi.
     * @throws SQLException
     */
    public void confirmPayment(int reservationId) throws SQLException {
        // Panggil DAO untuk mengubah status pembayaran menjadi 'paid'.
        boolean success = ReservationDAO.updatePaymentStatus(reservationId, "paid");
        if (!success) {
            throw new SQLException("Gagal mengupdate status pembayaran untuk reservasi ID: " + reservationId);
        }
        System.out.println("✅ Pembayaran untuk reservasi ID " + reservationId + " telah dikonfirmasi (paid).");
    }

    /**
     * Memproses Check-Out, termasuk penambahan denda jika ada.
     * Ini adalah proses transaksional yang kompleks.
     * @param reservationId ID reservasi.
     * @param penalty Objek denda (bisa null jika tidak ada denda).
     * @throws SQLException
     * @throws BookingException
     */
    public void processCheckOut(int reservationId, Penalty penalty) throws SQLException, BookingException {
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false); // Mulai transaksi.

            // Validasi reservasi.
            Reservation reservation = ReservationDAO.getReservationById(reservationId, conn);
            if (reservation == null) {
                throw new BookingException("Reservasi dengan ID " + reservationId + " tidak ditemukan.");
            }
            if (!"checked_in".equalsIgnoreCase(reservation.getStatus())) {
                throw new BookingException("Hanya reservasi dengan status 'checked_in' yang bisa di-check-out.");
            }

            // Jika ada denda yang diberikan...
            if (penalty != null) {
                penalty.setReservationId(reservationId);
                // 1. Simpan data denda baru ke tabel 'penalties'.
                PenaltyDAO.addPenalty(penalty, conn);
                // 2. Update status denda di reservasi terkait menjadi 'pending'.
                ReservationDAO.updatePenaltyStatus(reservationId, "pending", conn);
                System.out.println("✅ Denda untuk reservasi ID " + reservationId + " berhasil ditambahkan.");
            }

            // 3. Proses check-out utama: ubah status reservasi menjadi 'checked_out'.
            boolean success = ReservationDAO.processCheckOut(reservationId, conn);
            if (!success) {
                throw new SQLException("Proses check-out gagal di level DAO.");
            }

            // 4. Tentukan status kamar baru setelah check-out.
            String newRoomStatus;
            if (penalty != null) {
                // Jika ada denda, kamar mungkin perlu diperiksa/dibersihkan, jadi statusnya 'maintenance'.
                newRoomStatus = "maintenance";
                System.out.println("Check-out dengan denda. Status kamar ID " + reservation.getRoomId() + " diubah menjadi 'maintenance'.");
            } else {
                // Jika tidak ada denda, kamar langsung tersedia lagi ('available').
                newRoomStatus = "available";
                System.out.println("Check-out bersih. Status kamar ID " + reservation.getRoomId() + " diubah menjadi 'available'.");
            }
            // 5. Update status kamar di database.
            RoomDAO.updateRoomStatus(reservation.getRoomId(), newRoomStatus, conn);

            conn.commit(); // Simpan semua perubahan.
            System.out.println("✅ Check-out untuk reservasi ID " + reservationId + " berhasil diproses.");

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

    /**
     * Membatalkan sebuah reservasi. Ini juga proses transaksional.
     * @param reservation Objek reservasi yang akan dibatalkan.
     * @throws SQLException
     */
    public void cancelReservation(Reservation reservation) throws SQLException {
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);

            // 1. Ubah status reservasi menjadi 'cancelled'.
            ReservationDAO.updateStatus(reservation.getId(), "cancelled", conn);
            // 2. Kembalikan status kamar yang dipesan menjadi 'available' agar bisa dipesan orang lain.
            RoomDAO.updateRoomStatus(reservation.getRoomId(), "available", conn);

            conn.commit(); // Simpan perubahan.

            // Setelah transaksi database selesai, kirim email konfirmasi pembatalan.
            // Ini dilakukan di luar transaksi agar tidak memperlambat database.
            User customer = UserDAO.getUserById(reservation.getUserId());
            Room room = RoomDAO.getRoomById(reservation.getRoomId());
            if (customer != null && customer.getEmail() != null && !customer.getEmail().isBlank() && room != null) {
                // Email dikirim di thread terpisah agar tidak memblokir aplikasi.
                new Thread(() -> EmailUtil.sendCancellationEmail(customer.getEmail(), reservation, room)).start();
            }
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            throw e;
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
}