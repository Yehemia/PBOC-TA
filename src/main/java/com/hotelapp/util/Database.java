package com.hotelapp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                ConfigLoader.getProperty("db.url"),
                ConfigLoader.getProperty("db.user"),
                ConfigLoader.getProperty("db.password")
        );
    }
}