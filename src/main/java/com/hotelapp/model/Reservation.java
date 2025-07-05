package com.hotelapp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
/**
 * Ini adalah "cetakan" atau model untuk data Reservasi.
 * Kelas ini sangat penting karena menyimpan semua detail tentang sebuah pemesanan kamar,
 * mulai dari siapa yang memesan, kapan, hingga statusnya.
 */
public class Reservation {
    // Variabel-variabel (properti) untuk menyimpan data sebuah reservasi.
    // 'private' berarti hanya bisa diakses dari dalam kelas ini (melalui getter/setter).
    private int id; // ID unik untuk setiap reservasi di database.
    private String bookingCode; // Kode booking yang unik, dibuat otomatis (misal: INV-20230101-ABCD).
    private int userId; // ID pengguna yang melakukan reservasi. Jika 0, berarti reservasi offline oleh resepsionis.
    private int roomId; // ID kamar fisik yang dipesan.
    private LocalDate checkIn; // Tanggal rencana check-in.
    private LocalDate checkOut; // Tanggal rencana check-out.
    private LocalDateTime checkInTime; // Waktu dan tanggal PASTI saat tamu benar-benar check-in.
    private LocalDateTime checkOutTime; // Waktu dan tanggal PASTI saat tamu benar-benar check-out.
    private String paymentMethod; // Metode pembayaran (misal: 'online', 'cash').
    private String bookingType; // Jenis booking ('online' oleh customer, atau 'offline' oleh resepsionis).
    private String status; // Status reservasi saat ini ('pending', 'checked_in', 'checked_out', 'cancelled').
    private double totalPrice; // Total harga yang harus dibayar untuk reservasi ini.
    private String penaltyStatus; // Status denda ('pending', 'paid', atau null jika tidak ada denda).
    private String paymentStatus; // Status pembayaran ('pending', 'paid').
    private String guestName; // Nama tamu. Disimpan terpisah untuk fleksibilitas, terutama untuk tamu walk-in.


    // Properti tambahan ini tidak disimpan langsung di tabel 'reservations'.
    // Data ini diambil dari tabel lain melalui query JOIN untuk kemudahan saat menampilkan di UI.
    private String roomTypeName; // Nama tipe kamar (misal: "Deluxe").
    private int roomNumber; // Nomor kamar (misal: 101).


    /**
     * CONSTRUCTOR LENGKAP.
     * Digunakan saat mengambil data yang sudah ada dari database dimana semua kolom terisi.
     */
    public Reservation(int id, int userId, int roomId, LocalDate checkIn, LocalDate checkOut, LocalDateTime checkInTime, LocalDateTime checkOutTime, String paymentMethod, String bookingType, String status, double totalPrice, String penaltyStatus, String paymentStatus, String guestName) {
        this.id = id;
        this.userId = userId;
        this.roomId = roomId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.paymentMethod = paymentMethod;
        this.bookingType = bookingType;
        this.status = status;
        this.totalPrice = totalPrice;
        this.penaltyStatus = penaltyStatus;
        this.paymentStatus = paymentStatus;
        this.guestName = guestName;
    }

    /**
     * CONSTRUCTOR UNTUK MEMBUAT RESERVASI BARU (ONLINE/OFFLINE).
     * Digunakan saat pengguna baru akan membuat reservasi. Beberapa field seperti 'id' atau 'checkInTime'
     * belum ada nilainya saat ini.
     */
    public Reservation(int userId, int roomId, LocalDate checkIn, LocalDate checkOut,
                       String paymentMethod, String bookingType, String status, double totalPrice, String guestName) {
        this.userId = userId;
        this.roomId = roomId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.paymentMethod = paymentMethod;
        this.bookingType = bookingType;
        this.status = status;
        this.totalPrice = totalPrice;
        this.guestName = guestName;
    }

    /**
     * CONSTRUCTOR LAIN (OVERLOADING).
     * Java memperbolehkan nama constructor yang sama selama parameternya berbeda.
     * Ini memberikan fleksibilitas saat membuat objek.
     */
    public Reservation(int id, int userId, int roomId, LocalDate checkIn, LocalDate checkOut,
                       String paymentMethod, String bookingType, String status, double totalPrice, String guestName) {
        this.id = id;
        this.userId = userId;
        this.roomId = roomId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.paymentMethod = paymentMethod;
        this.bookingType = bookingType;
        this.status = status;
        this.totalPrice = totalPrice;
        this.guestName = guestName;
    }

    public Reservation(int id, int userId, int roomId, LocalDate checkIn, LocalDate checkOut,
                       String paymentMethod, String status, double totalPrice) {
        this.id = id;
        this.userId = userId;
        this.roomId = roomId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.totalPrice = totalPrice;
    }

    /**
     * CONSTRUCTOR UNTUK MENAMPILKAN DATA DI TABEL.
     * Terkadang kita hanya butuh beberapa data untuk ditampilkan, jadi kita buat constructor
     * yang lebih simpel untuk kebutuhan tersebut.
     */
    public Reservation(int id, LocalDate checkIn, LocalDate checkOut, String status, String roomTypeName, int roomNumber) {
        this.id = id;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.status = status;
        this.roomTypeName = roomTypeName;
        this.roomNumber = roomNumber;
    }

    public Reservation(int id, int roomId, LocalDate checkIn, LocalDate checkOut, String status, String roomTypeName, int roomNumber) {
        this.id = id;
        this.roomId = roomId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.status = status;
        this.roomTypeName = roomTypeName;
        this.roomNumber = roomNumber;
    }

    // -- GETTER DAN SETTER --
    // Ini adalah metode-metode standar untuk mengakses (get) dan mengubah (set) nilai dari variabel-variabel di atas.
    // Ini adalah praktik enkapsulasi yang baik dalam pemrograman berorientasi objek.

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getBookingCode() { return bookingCode; }
    public void setBookingCode(String bookingCode) { this.bookingCode = bookingCode; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public LocalDate getCheckIn() { return checkIn; }
    public void setCheckIn(LocalDate checkIn) { this.checkIn = checkIn; }

    public LocalDate getCheckOut() { return checkOut; }
    public void setCheckOut(LocalDate checkOut) { this.checkOut = checkOut; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBookingType() { return bookingType; }
    public void setBookingType(String bookingType) { this.bookingType = bookingType; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public LocalDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalDateTime checkInTime) { this.checkInTime = checkInTime; }

    public LocalDateTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(LocalDateTime checkOutTime) { this.checkOutTime = checkOutTime; }

    public String getPenaltyStatus() { return penaltyStatus; }
    public void setPenaltyStatus(String penaltyStatus) { this.penaltyStatus = penaltyStatus; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public LocalDateTime getExpectedCheckOutTime() {
        // Asumsi waktu checkout standar adalah jam 12 siang (NOON).
        return this.checkOut.atTime(LocalTime.NOON);
    }

    public String getRoomTypeName() { return roomTypeName; }
    public void setRoomTypeName(String roomTypeName) { this.roomTypeName = roomTypeName; }

    public int getRoomNumber() { return roomNumber; }
    public void setRoomNumber(int roomNumber) { this.roomNumber = roomNumber; }
}
