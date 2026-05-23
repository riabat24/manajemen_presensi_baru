package com.rplbo.app.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AuthController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Label lblError;

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            tampilkanError("Username dan Password tidak boleh kosong!");
            return;
        }

        boolean isLoginBerhasil = false;

        // --- BLOK KHUSUS DATABASE ---
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT p.id_karyawan, p.username, p.role, k.nama " +
                    "FROM pengguna p " +
                    "JOIN karyawan k ON p.id_karyawan = k.id_karyawan " +
                    "WHERE p.username = ? AND p.password = ?";

            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, username);
            pst.setString(2, password);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                UserSession.getInstance().setUserSession(
                        rs.getInt("id_karyawan"),
                        rs.getString("username"),
                        rs.getString("nama"),
                        rs.getString("role")
                );
                isLoginBerhasil = true; // Tandai bahwa login sukses!
            } else {
                tampilkanError("Username atau Password salah!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Jika masuk ke sini, ini BENAR-BENAR error database
            tampilkanError("Koneksi Database Error: " + e.getMessage());
            return;
        }

        // --- BLOK KHUSUS PINDAH HALAMAN (DI LUAR TRY-CATCH DATABASE) ---
        if (isLoginBerhasil) {
            pindahKeDashboard(event);
        }
    }

    private void tampilkanError(String pesan) {
        lblError.setText(pesan);
        lblError.setVisible(true);
    }

    private void pindahKeDashboard(ActionEvent event) {
        try {
            String role = UserSession.getInstance().getRole();
            String fileFxml = "admin".equalsIgnoreCase(role) ? "dashboard-admin-view.fxml" : "dashboard-karyawan-view.fxml";
            String judulHalaman = "admin".equalsIgnoreCase(role) ? "Manajemen Presensi - Dashboard Admin" : "Manajemen Presensi - Dashboard Karyawan";

            // Perbaikan rute pemanggilan FXML menggunakan path absolut
            URL fxmlLocation = getClass().getResource("/com/rplbo/app/demo/" + fileFxml);
            if (fxmlLocation == null) {
                // Fallback jika path absolut gagal
                fxmlLocation = getClass().getResource(fileFxml);
            }

            if (fxmlLocation == null) {
                tampilkanError("Error UI: File " + fileFxml + " tidak ditemukan!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root, 900, 600));
            stage.setTitle(judulHalaman);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            // Menampilkan error asli jika FXML-nya yang bermasalah, bukan menyalahkan database
            tampilkanError("Gagal memuat halaman: " + e.getMessage());
        }
    }
}