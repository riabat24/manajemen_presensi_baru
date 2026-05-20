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

import java.io.IOException;
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

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT p.id_karyawan, p.username, p.role, k.nama " +
                    "FROM tb_pengguna p " +
                    "JOIN tb_karyawan k ON p.id_karyawan = k.id_karyawan " +
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

                pindahKeDashboard(event);
            } else {
                tampilkanError("Username atau Password salah!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            tampilkanError("Gagal terhubung ke database!");
        }
    }

    private void tampilkanError(String pesan) {
        lblError.setText(pesan);
        lblError.setVisible(true);
    }

    private void pindahKeDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard-karyawan-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 900, 600));
            stage.setTitle("Manajemen Presensi - Dashboard");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            tampilkanError("Gagal memuat halaman Dashboard.");
        }
    }
}