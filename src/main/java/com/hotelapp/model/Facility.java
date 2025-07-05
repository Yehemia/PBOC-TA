package com.hotelapp.model;

/**
 * Ini adalah "cetakan" untuk data Fasilitas.
 * Sebuah fasilitas bisa berupa WiFi, AC, Kolam Renang, dll.
 * Kelas ini hanya menyimpan informasi dasar tentang sebuah fasilitas.
 */
public class Facility {
    // Variabel-variabel untuk menyimpan data. 'private' artinya hanya bisa diakses dari dalam kelas ini.
    private int id; // ID unik fasilitas di database (misal: 1).
    private String name; // Nama fasilitas (misal: "WiFi Gratis").
    private String iconLiteral; // Kode untuk menampilkan ikon (misal: "fa-wifi").

    /**
     * Ini adalah Constructor. Fungsinya seperti resep untuk membuat objek Facility baru.
     * Saat kita membuat objek Facility, kita harus memberikan id, nama, dan kode ikon.
     * @param id ID dari database.
     * @param name Nama fasilitas.
     * @param iconLiteral Kode ikon.
     */
    public Facility(int id, String name, String iconLiteral) {
        this.id = id;
        this.name = name;
        this.iconLiteral = iconLiteral;
    }

    // -- GETTER DAN SETTER --
    // Getter adalah fungsi untuk MENGAMBIL nilai dari sebuah variabel.
    // Setter adalah fungsi untuk MENGUBAH nilai dari sebuah variabel.
    // Ini adalah praktik standar dalam pemrograman Java untuk mengontrol akses ke data.

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIconLiteral() { return iconLiteral; }
    public void setIconLiteral(String iconLiteral) { this.iconLiteral = iconLiteral; }

    /**
     * Fungsi ini menentukan bagaimana objek Facility akan ditampilkan jika dicetak sebagai String.
     * Di sini, kita ingin agar yang muncul adalah namanya. Ini berguna saat menampilkan fasilitas di ComboBox atau CheckListView.
     * @return Nama fasilitas.
     */
    @Override
    public String toString() {
        return this.name;
    }
}