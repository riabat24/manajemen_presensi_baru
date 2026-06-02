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
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

public class PresensiController implements Initializable {
    @FXML private Label lblJamDigital, lblTanggal, lblJamMasuk, lblJamKeluar, lblStatusMasuk, lblStatusKeluar;
    @FXML private Button btnPresensi;

    private boolean isClockedIn = false;
    private final LocalTime BATAS_AWAL_MASUK = LocalTime.of(7, 0);
    private final LocalTime JAM_MASUK_NORMAL = LocalTime.of(8, 30);
    // Batas akhir masuk (10:00) tetap dideklarasikan namun pemeriksaannya kita matikan di bawah
    private final LocalTime BATAS_AKHIR_MASUK = LocalTime.of(10, 0);
    private final LocalTime BATAS_AWAL_KELUAR = LocalTime.of(8, 0);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        startClock();
        loadPresensiHariIni();
    }

    private void startClock() {
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            lblJamDigital.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            lblTanggal.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", new Locale("id", "ID"))));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    @FXML
    private void handlePresensi(ActionEvent event) {
        LocalTime now = LocalTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");

        if (!isClockedIn) {
            // Hanya tolak jika absen terlalu pagi (sebelum jam 7 pagi)
            if (now.isBefore(BATAS_AWAL_MASUK)) {
                tampilkanAlert(Alert.AlertType.WARNING, "Gagal Clock-In", "Belum waktunya presensi.");
                return;
            }

            // LOGIKA BARU: Aturan blokir absen di atas jam 10:00 SUDAH DIHAPUS di sini.
            // Sekarang jam berapapun Karyawan datang di atas jam 08:30 akan langsung dihitung "Terlambat".

            String statusWaktuDB = now.isAfter(JAM_MASUK_NORMAL) ? "terlambat" : "tepat_waktu";
            lblJamMasuk.setText(now.format(dtf));
            lblStatusMasuk.setText(now.isAfter(JAM_MASUK_NORMAL) ? "Terlambat" : "Tepat Waktu");

            lblStatusMasuk.setStyle(statusWaktuDB.equals("terlambat") ? "-fx-background-color: #FFCDD2; -fx-text-fill: #C62828; -fx-padding: 5 15 5 15; -fx-background-radius: 20; -fx-font-weight: bold;" : "-fx-background-color: #C8E6C9; -fx-text-fill: #2E7D32; -fx-padding: 5 15 5 15; -fx-background-radius: 20; -fx-font-weight: bold;");

            btnPresensi.setText("🚪 Clock-Out Sekarang");
            btnPresensi.setStyle("-fx-background-color: #D32F2F; -fx-background-radius: 10; -fx-text-fill: white; -fx-font-size: 18px;");
            simpanClockIn(now, statusWaktuDB);
            isClockedIn = true;

        } else {
            // LOGIKA CLOCK-OUT
            if (now.isBefore(BATAS_AWAL_KELUAR)) {
                tampilkanAlert(Alert.AlertType.WARNING, "Gagal Clock-Out", "Belum waktunya pulang.");
                return;
            }
            lblJamKeluar.setText(now.format(dtf));
            lblStatusKeluar.setText("Selesai Shift");
            btnPresensi.setDisable(true);
            btnPresensi.setText("Presensi Hari Ini Selesai");
            btnPresensi.setStyle("-fx-background-color: #9E9E9E; -fx-background-radius: 10; -fx-text-fill: white; -fx-font-size: 18px;");
            simpanClockOut(now);
        }
    }

    private void simpanClockIn(LocalTime jamMasuk, String statusWaktu) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement pst = conn.prepareStatement("INSERT INTO presensi (id_karyawan, tanggal, jam_masuk, status_kehadiran, status_waktu) VALUES (?, ?, ?, ?, ?)");
            pst.setInt(1, UserSession.getInstance().getIdKaryawan()); pst.setDate(2, java.sql.Date.valueOf(LocalDate.now())); pst.setTime(3, java.sql.Time.valueOf(jamMasuk)); pst.setString(4, "hadir"); pst.setString(5, statusWaktu);
            pst.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void simpanClockOut(LocalTime jamKeluar) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement pst = conn.prepareStatement("UPDATE presensi SET jam_keluar = ? WHERE tanggal = ? AND id_karyawan = ?");
            pst.setTime(1, java.sql.Time.valueOf(jamKeluar)); pst.setDate(2, java.sql.Date.valueOf(LocalDate.now())); pst.setInt(3, UserSession.getInstance().getIdKaryawan());
            pst.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadPresensiHariIni() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement pst = conn.prepareStatement("SELECT * FROM presensi WHERE tanggal = ? AND id_karyawan = ?");
            pst.setDate(1, java.sql.Date.valueOf(LocalDate.now())); pst.setInt(2, UserSession.getInstance().getIdKaryawan());
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                lblJamMasuk.setText(rs.getString("jam_masuk"));
                if ("terlambat".equals(rs.getString("status_waktu"))) {
                    lblStatusMasuk.setText("Terlambat"); lblStatusMasuk.setStyle("-fx-background-color: #FFCDD2; -fx-text-fill: #C62828; -fx-padding: 5 15 5 15; -fx-background-radius: 20; -fx-font-weight: bold;");
                } else {
                    lblStatusMasuk.setText("Tepat Waktu"); lblStatusMasuk.setStyle("-fx-background-color: #C8E6C9; -fx-text-fill: #2E7D32; -fx-padding: 5 15 5 15; -fx-background-radius: 20; -fx-font-weight: bold;");
                }
                isClockedIn = true;
                if (rs.getString("jam_keluar") != null) {
                    lblJamKeluar.setText(rs.getString("jam_keluar")); lblStatusKeluar.setText("Selesai Shift"); btnPresensi.setDisable(true); btnPresensi.setText("Presensi Hari Ini Selesai"); btnPresensi.setStyle("-fx-background-color: #9E9E9E; -fx-background-radius: 10; -fx-text-fill: white; -fx-font-size: 18px;");
                } else {
                    btnPresensi.setText("🚪 Clock-Out Sekarang"); btnPresensi.setStyle("-fx-background-color: #D32F2F; -fx-background-radius: 10; -fx-text-fill: white; -fx-font-size: 18px;");
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void tampilkanAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait();
    }

    // =========================================================================
    // MESIN RADAR PELACAK FILE
    // =========================================================================
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

    @FXML
    private void handleKembaliDashboard(ActionEvent event) {
        try {
            URL fxmlLocation = cariFileFxml("dashboard-karyawan-view.fxml");
            if (fxmlLocation == null) return;
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(loader.load(), 900, 600);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Manajemen Presensi - Dashboard");
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }
}