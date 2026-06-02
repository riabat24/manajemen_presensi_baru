package com.rplbo.app.demo.controllers;

import com.rplbo.app.demo.util.DatabaseConnection;
import com.rplbo.app.demo.models.UserSession;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private Label lblJam, lblTanggal, lblNamaProfil, lblStatusDetail;
    @FXML private Label lblHadir, lblTerlambat, lblCuti, lblIzin;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initClock();
        lblNamaProfil.setText(UserSession.getInstance().getNamaKaryawan());
        loadUserData();
    }

    private void initClock() {
        Locale localeID = new Locale("id", "ID");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", localeID);
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            LocalDateTime now = LocalDateTime.now();
            lblJam.setText(now.format(timeFormatter));
            lblTanggal.setText(now.format(dateFormatter));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    private void loadUserData() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            LocalDate hariIni = LocalDate.now();
            int idAktif = UserSession.getInstance().getIdKaryawan();

            // Status Presensi Hari Ini
            PreparedStatement pstStatus = conn.prepareStatement("SELECT jam_masuk, jam_keluar FROM presensi WHERE tanggal = ? AND id_karyawan = ?");
            pstStatus.setDate(1, java.sql.Date.valueOf(hariIni));
            pstStatus.setInt(2, idAktif);
            ResultSet rsStatus = pstStatus.executeQuery();
            if (rsStatus.next()) {
                if (rsStatus.getString("jam_keluar") != null) {
                    lblStatusDetail.setText("✅ Presensi Selesai. Selamat Beristirahat.");
                    lblStatusDetail.setStyle("-fx-text-fill: #27C93F; -fx-font-weight: bold;");
                } else {
                    lblStatusDetail.setText("⏳ Sudah Clock-In. Jangan lupa Clock-Out nanti.");
                    lblStatusDetail.setStyle("-fx-text-fill: #1976D2; -fx-font-weight: bold;");
                }
            } else {
                lblStatusDetail.setText("Belum Absen Hari Ini. Silakan Clock-In.");
                lblStatusDetail.setStyle("-fx-text-fill: #D32F2F; -fx-font-weight: bold;");
            }

            // Hitung Kehadiran Bulan Ini
            PreparedStatement pstHadir = conn.prepareStatement("SELECT COUNT(*) as total FROM presensi WHERE id_karyawan = ? AND MONTH(tanggal) = MONTH(CURRENT_DATE()) AND YEAR(tanggal) = YEAR(CURRENT_DATE()) AND status_kehadiran = 'hadir'");
            pstHadir.setInt(1, idAktif);
            ResultSet rsHadir = pstHadir.executeQuery();
            if (rsHadir.next()) lblHadir.setText(rsHadir.getString("total") + " hari");

            // Hitung Keterlambatan Bulan Ini
            PreparedStatement pstTerlambat = conn.prepareStatement("SELECT COUNT(*) as total FROM presensi WHERE id_karyawan = ? AND MONTH(tanggal) = MONTH(CURRENT_DATE()) AND YEAR(tanggal) = YEAR(CURRENT_DATE()) AND status_waktu = 'terlambat'");
            pstTerlambat.setInt(1, idAktif);
            ResultSet rsTerlambat = pstTerlambat.executeQuery();
            if (rsTerlambat.next()) lblTerlambat.setText(rsTerlambat.getString("total") + " kali");

            // Hitung Cuti Disetujui
            PreparedStatement pstCuti = conn.prepareStatement("SELECT COUNT(*) as total FROM izin_cuti WHERE id_karyawan = ? AND jenis_izin = 'cuti' AND status_persetujuan = 'disetujui'");
            pstCuti.setInt(1, idAktif);
            ResultSet rsCuti = pstCuti.executeQuery();
            if (rsCuti.next()) lblCuti.setText(rsCuti.getString("total") + " hari");

            // PERBAIKAN: Hitung Izin yang berstatus 'disetujui' (Bukan 'pending')
            PreparedStatement pstIzin = conn.prepareStatement("SELECT COUNT(*) as total FROM izin_cuti WHERE id_karyawan = ? AND jenis_izin IN ('sakit', 'kepentingan lain') AND status_persetujuan = 'disetujui'");
            pstIzin.setInt(1, idAktif);
            ResultSet rsIzin = pstIzin.executeQuery();
            if (rsIzin.next()) lblIzin.setText(rsIzin.getString("total") + " hari");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private URL cariFileFxml(String fileName) {
        try {
            URL url = getClass().getResource("/com/rplbo/app/demo/views/" + fileName);
            if (url != null) return url;
            File srcFolder = new File("src");
            File found = lacakFile(srcFolder, fileName);
            if (found != null) return found.toURI().toURL();
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    private File lacakFile(File folder, String namaFile) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    File ketemu = lacakFile(f, namaFile);
                    if (ketemu != null) return ketemu;
                } else if (f.getName().equals(namaFile)) return f;
            }
        }
        return null;
    }

    @FXML private void handleMenuBeranda(ActionEvent event) {}
    @FXML private void handleMenuPresensi(ActionEvent event) { pindahHalaman(event, "presensi-view.fxml", "Manajemen Presensi - Presensi"); }
    @FXML private void handleMenuRiwayat(ActionEvent event) { pindahHalaman(event, "riwayat-view.fxml", "Manajemen Presensi - Riwayat"); }
    @FXML private void handleMenuCuti(ActionEvent event) { pindahHalaman(event, "cuti-view.fxml", "Manajemen Presensi - Cuti"); }
    @FXML private void handleLogout(ActionEvent event) {
        UserSession.getInstance().cleanUserSession();
        pindahHalaman(event, "login-view.fxml", "Manajemen Presensi - Login");
    }

    private void pindahHalaman(ActionEvent event, String fxmlFile, String title) {
        try {
            URL fxmlLocation = cariFileFxml(fxmlFile);
            if (fxmlLocation == null) return;
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 900, 600));
            stage.setTitle(title);
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }
}