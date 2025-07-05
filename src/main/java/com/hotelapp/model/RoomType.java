package com.hotelapp.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Ini adalah "cetakan" untuk data Tipe Kamar.
 * Merepresentasikan sebuah kategori kamar, seperti "Deluxe", "Standard", atau "Suite".
 */
public class RoomType {
    // Variabel untuk menyimpan data tipe kamar.
    private int id; // ID unik tipe kamar.
    private String name; // Nama tipe kamar (misal: "Deluxe Room").
    private double price; // Harga per malam.
    private String description; // Deskripsi lengkap tentang tipe kamar.
    private int maxGuests; // Kapasitas maksimal tamu.
    private String bedInfo; // Informasi tempat tidur (misal: "1 King Bed").
    private String imageUrl; // URL gambar untuk tipe kamar ini.

    // Properti ini tidak disimpan di database, tapi dihitung saat mengambil data.
    private int availableRoomCount; // Jumlah kamar fisik dari tipe ini yang sedang tersedia.

    // Sebuah tipe kamar bisa memiliki banyak fasilitas. Ini adalah hubungan "one-to-many".
    private List<Facility> facilities; // Daftar fasilitas yang dimiliki tipe kamar ini.

    /**
     * Constructor untuk membuat objek RoomType baru.
     */
    public RoomType(int id, String name, double price, String description, int maxGuests, String bedInfo, String imageUrl) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.maxGuests = maxGuests;
        this.bedInfo = bedInfo;
        this.imageUrl = imageUrl;
        // Saat objek baru dibuat, siapkan list kosong untuk fasilitas.
        this.facilities = new ArrayList<>();
    }

    // -- GETTER DAN SETTER --
    // Fungsi untuk mengambil dan mengubah nilai setiap variabel.

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public List<Facility> getFacilities() { return facilities; }
    public void setFacilities(List<Facility> facilities) { this.facilities = facilities; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getMaxGuests() { return maxGuests; }
    public void setMaxGuests(int maxGuests) { this.maxGuests = maxGuests; }

    public String getBedInfo() { return bedInfo; }
    public void setBedInfo(String bedInfo) { this.bedInfo = bedInfo; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getAvailableRoomCount() { return availableRoomCount; }
    public void setAvailableRoomCount(int availableRoomCount) { this.availableRoomCount = availableRoomCount; }
}