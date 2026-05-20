package com.presensi.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    // Fungsi untuk memvalidasi login dan mengembalikan role pengguna
    public String authenticateUser(String username, String password) {
        // Asumsi kelas DatabaseConnection sudah dibuat untuk mengatur JDBC
        String query = "SELECT role FROM tb_pengguna WHERE username = ? AND password = ?";

        try (Connection conn = com.rplbo.app.demo.DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            // Catatan: Sesuai proposal, password idealnya diverifikasi dengan BCrypt di tahap produksi
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("role"); // Mengembalikan 'admin' atau 'karyawan'
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // Login gagal
    }
}