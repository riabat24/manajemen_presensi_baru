package com.rplbo.app.demo.controllers;

import com.rplbo.app.demo.util.DatabaseConnection;
import com.rplbo.app.demo.models.UserSession;
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

import java.io.File;
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

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT p.id_karyawan, p.username, p.role, k.nama FROM pengguna p JOIN karyawan k ON p.id_karyawan = k.id_karyawan WHERE p.username = ? AND p.password = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, username);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                UserSession.getInstance().setUserSession(rs.getInt("id_karyawan"), rs.getString("username"), rs.getString("nama"), rs.getString("role"));
                isLoginBerhasil = true;
            } else {
                tampilkanError("Username atau Password salah!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            tampilkanError("Koneksi Database Error: " + e.getMessage());
            return;
        }

        if (isLoginBerhasil) {
            pindahKeDashboard(event);
        }
    }

    private void tampilkanError(String pesan) {
        lblError.setText(pesan);
        lblError.setVisible(true);
    }

    // =========================================================================
    // JURUS RADAR GOD-MODE: Mencari file langsung ke akar hardisk komputer Anda
    // =========================================================================
    private URL cariFileFxml(String fileName) {
        try {
            // Jalur 1: Normal
            URL url = getClass().getResource("/com/rplbo/app/demo/views/" + fileName);
            if (url != null) return url;

            // Jalur 2: Jika nama folder menggunakan TITIK (Ini penyakit IDE Anda)
            url = getClass().getResource("/com.rplbo.app.demo/views/" + fileName);
            if (url != null) return url;

            // Jalur 3: Radar Pelacak Otomatis (Akan memindai seluruh isi folder "src")
            File srcFolder = new File("src");
            File found = lacakFile(srcFolder, fileName);
            if (found != null) {
                return found.toURI().toURL(); // Langsung ambil dari Hardisk!
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Mesin pencari otomatis
    private File lacakFile(File folder, String namaFile) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    File ketemu = lacakFile(f, namaFile);
                    if (ketemu != null) return ketemu;
                } else if (f.getName().equals(namaFile)) {
                    return f;
                }
            }
        }
        return null;
    }

    private void pindahKeDashboard(ActionEvent event) {
        try {
            String role = UserSession.getInstance().getRole();
            String fileFxml = "admin".equalsIgnoreCase(role) ? "dashboard-admin-view.fxml" : "dashboard-karyawan-view.fxml";
            String judulHalaman = "admin".equalsIgnoreCase(role) ? "Manajemen Presensi - Dashboard Admin" : "Manajemen Presensi - Dashboard Karyawan";

            // Gunakan Radar untuk menemukan file!
            URL fxmlLocation = cariFileFxml(fileFxml);

            if (fxmlLocation == null) {
                tampilkanError("SUPER FATAL: File " + fileFxml + " benar-benar hilang dari komputer Anda!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();

            if ("admin".equalsIgnoreCase(role)) {
                stage.setScene(new Scene(root, 1150, 700));
            } else {
                stage.setScene(new Scene(root, 900, 600));
            }

            stage.setTitle(judulHalaman);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            tampilkanError("Gagal memuat halaman: " + e.getMessage());
        }
    }
}