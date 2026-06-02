package com.rplbo.app.demo.util;

import java.sql.Connection;
import java.sql.SQLException;

public class TestConnection {
    public static void main(String[] args) {
        System.out.println("Mencoba menghubungkan ke database...");

        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                System.out.println("BERHASIL! Java sudah terhubung dengan database mahasiswa_presensi.");
            }
        } catch (SQLException e) {
            System.out.println("GAGAL! Koneksi database bermasalah.");
            e.printStackTrace();
        }
    }
}