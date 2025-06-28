package com.hotelapp.model;

public class User {
    private int id;
    private String username;
    private String name;
    private String email;
    private String password;
    private String role;
    private String accountStatus;

    public User(int id, String username, String name, String email, String role) {
        this(id, username, name, email, null, role, "active");
    }

    public User(int id, String username, String name, String email, String password, String role) {
        this(id, username, name, email, password, role, "active");
    }

    // Konstruktor untuk DAO
    public User(int id, String username, String name, String email, String password, String role, String accountStatus) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.accountStatus = accountStatus;
    }


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
    public boolean isAdmin() { return "admin".equalsIgnoreCase(role); }
    public boolean isReceptionist() { return "receptionist".equalsIgnoreCase(role); }
    public boolean isCustomer() { return "customer".equalsIgnoreCase(role); }
}