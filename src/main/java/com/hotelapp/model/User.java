package com.hotelapp.model;

/**
 * Ini adalah "cetakan" atau model untuk data Pengguna (User).
 * Merepresentasikan siapa saja yang bisa login ke sistem, baik itu customer, resepsionis, atau admin.
 */
public class User {
    // Variabel (properti) untuk menyimpan data seorang pengguna.
    private int id; // ID unik pengguna di database.
    private String username; // Username yang digunakan untuk login.
    private String name; // Nama lengkap pengguna.
    private String email; // Alamat email pengguna.
    private String password; // Password yang disimpan di sini adalah password yang SUDAH DI-HASH, bukan password asli.
    private String role; // Peran pengguna di sistem ('customer', 'receptionist', 'admin', atau 'PENDING' untuk yang belum verifikasi).
    private String accountStatus; // Status akun ('active' atau 'inactive').

    /**
     * CONSTRUCTOR 1: Digunakan saat membuat objek User dengan beberapa data saja.
     * Berguna saat kita tidak butuh semua info, misalnya untuk menampilkan daftar nama.
     * Menggunakan 'this(...)' untuk memanggil constructor lain yang lebih lengkap.
     * @param id
     * @param username
     * @param name
     * @param email
     * @param role
     */
    public User(int id, String username, String name, String email, String role) {
        // Memanggil constructor di bawahnya dengan password=null dan status='active' sebagai default.
        this(id, username, name, email, null, role, "active");
    }

    /**
     * CONSTRUCTOR 2: Mirip seperti di atas, tapi menyertakan password.
     * @param id
     * @param username
     * @param name
     * @param email
     * @param password Password yang sudah di-hash.
     * @param role
     */
    public User(int id, String username, String name, String email, String password, String role) {
        // Memanggil constructor paling lengkap dengan status='active' sebagai default.
        this(id, username, name, email, password, role, "active");
    }

    /**
     * CONSTRUCTOR 3: Ini adalah constructor paling lengkap.
     * Biasanya digunakan oleh kelas DAO (Data Access Object) saat membaca data dari database
     * dan membuat objek User yang utuh.
     * @param id
     * @param username
     * @param name
     * @param email
     * @param password Password yang sudah di-hash.
     * @param role
     * @param accountStatus
     */
    public User(int id, String username, String name, String email, String password, String role, String accountStatus) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.accountStatus = accountStatus;
    }

    // -- GETTER DAN SETTER --
    // Kumpulan fungsi untuk MENGAMBIL (get) dan MENGATUR (set) nilai dari setiap variabel di atas.
    // Ini adalah praktik standar dalam Java untuk mengontrol bagaimana data diakses.

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    // -- FUNGSI BANTU (HELPER METHODS) --
    // Fungsi-fungsi kecil ini dibuat untuk mempermudah pengecekan.
    // Daripada menulis `user.getRole().equalsIgnoreCase("admin")`, kita bisa tulis `user.isAdmin()`.

    public boolean isAdmin() { return "admin".equalsIgnoreCase(role); }
    public boolean isReceptionist() { return "receptionist".equalsIgnoreCase(role); }
    public boolean isCustomer() { return "customer".equalsIgnoreCase(role); }
}