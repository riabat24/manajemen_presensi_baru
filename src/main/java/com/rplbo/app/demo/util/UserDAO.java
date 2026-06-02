package com.rplbo.app.demo.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    // Fungsi untuk memvalidasi login dan mengembalikan role pengguna
    public String authenticateUser(String username, String password) {
        // Asumsi kelas DatabaseConnection sudah dibuat untuk mengatur JDBC
        String query = "SELECT role FROM pengguna WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("role");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // Login gagal
    }
}