package com.rplbo.app.demo;

import java.sql.Connection;
import java.sql.SQLException;

public class TestConnection {
    public static void main(String[] args) {
        System.out.println("Mencoba menghubungkan ke database...");

        try {
            Connection conn = com.rplbo.app.demo.DatabaseConnection.getConnection();
            if (conn != null) {
                System.out.println("BERHASIL! Java sudah terhubung dengan database mahasiswa_presensi.");
            }
        } catch (SQLException e) {
            System.out.println("GAGAL! Koneksi database bermasalah.");
            e.printStackTrace();
        }
    }
}