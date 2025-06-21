package com.hotelapp.model;

public class Facility {
    private int id;
    private String name;
    private String iconLiteral;

    public Facility(int id, String name, String iconLiteral) {
        this.id = id;
        this.name = name;
        this.iconLiteral = iconLiteral;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIconLiteral() { return iconLiteral; }
    public void setIconLiteral(String iconLiteral) { this.iconLiteral = iconLiteral; }

    @Override
    public String toString() {
        return this.name;
    }
}