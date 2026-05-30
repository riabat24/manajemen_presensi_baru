package com.rplbo.app.demo.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Ganti 192.168.1.10 dengan IP Address asli komputer server database kamu
    private static final String URL = "jdbc:mysql://localhost:3306/manajemen_presensi";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}