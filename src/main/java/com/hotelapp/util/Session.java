package com.hotelapp.util;

import com.hotelapp.model.User;

/**
 * Ini adalah kelas "Singleton" untuk mengelola Sesi Pengguna.
 * Singleton berarti di seluruh aplikasi, hanya akan ada SATU objek Session ini.
 * Tujuannya adalah untuk menyimpan data pengguna yang sedang login agar bisa diakses
 * dari halaman mana pun tanpa perlu mengirim data user dari satu controller ke controller lain.
 */
public class Session {
    // 'instance' adalah satu-satunya objek Session yang akan pernah ada.
    private static Session instance;
    // 'currentUser' menyimpan data pengguna yang sedang login.
    private User currentUser;

    /**
     * Constructor dibuat 'private' agar tidak ada yang bisa membuat objek Session baru
     * dari luar kelas ini. Ini adalah kunci dari pola Singleton.
     */
    private Session() {}

    /**
     * Ini adalah satu-satunya cara untuk mendapatkan objek Session.
     * Fungsi ini akan memeriksa, jika 'instance' belum ada, maka buat baru.
     * Jika sudah ada, kembalikan yang sudah ada itu.
     * @return Satu-satunya objek Session.
     */
    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    /**
     * Menyimpan data pengguna yang baru saja login.
     * @param user Objek User yang login.
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Mengambil data pengguna yang sedang login.
     * @return Objek User yang sedang login, atau null jika tidak ada.
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Menghapus data sesi. Dipanggil saat pengguna logout.
     */
    public void clearSession() {
        currentUser = null;
    }
}