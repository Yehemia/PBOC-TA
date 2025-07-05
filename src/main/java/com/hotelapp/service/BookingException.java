package com.hotelapp.service;

/**
 * Ini adalah kelas "Exception" kustom kita sendiri.
 * Exception adalah cara Java untuk menangani error.
 * * Kita membuat kelas ini agar bisa melempar (throw) error yang spesifik
 * untuk masalah-masalah dalam proses booking. Misalnya, saat kamar penuh.
 * Dengan begitu, kita bisa menangkap error ini secara khusus dan menampilkan
 * pesan yang lebih ramah kepada pengguna, daripada pesan error teknis yang membingungkan.
 */
public class BookingException extends Exception {

    /**
     * Constructor untuk membuat objek BookingException baru.
     * @param message Pesan error yang ingin kita sampaikan (misalnya, "Maaf, kamar sudah penuh.").
     */
    public BookingException(String message) {
        // 'super(message)' artinya kita meneruskan pesan ini ke "induk"-nya, yaitu kelas Exception bawaan Java.
        super(message);
    }
}