package com.rplbo.app.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class CutiController implements Initializable {

    // Navigasi Atas Form
    @FXML private Button btnAjukanBaru;
    @FXML private Button btnRiwayatPengajuan;
    @FXML private Button btnKembaliDashboard;

    // Jenis Ketidakhadiran (Menggunakan ToggleButton agar eksklusif salah satu)
    @FXML private ToggleButton btnCutiTahunan;
    @FXML private ToggleButton btnIzinSakit;
    @FXML private ToggleButton btnKeperluanLain;
    private ToggleGroup jenisCutiGroup;

    // Input Tanggal dan Keterangan
    @FXML private DatePicker dpTanggalMulai;
    @FXML private DatePicker dpTanggalSelesai;
    @FXML private TextArea txtKeterangan;

    // Aksi Tombol Kirim & Label Status di bawah
    @FXML private Button btnKirimPengajuan;
    @FXML private Label lblStatusPengajuan;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Mengelompokkan ToggleButton supaya hanya bisa pilih satu jenis ketidakhadiran
        jenisCutiGroup = new ToggleGroup();
        btnCutiTahunan.setToggleGroup(jenisCutiGroup);
        btnIzinSakit.setToggleGroup(jenisCutiGroup);
        btnKeperluanLain.setToggleGroup(jenisCutiGroup);

        // Nilai default awal
        btnCutiTahunan.setSelected(true);
        dpTanggalMulai.setValue(LocalDate.now());
        dpTanggalSelesai.setValue(LocalDate.now());

        // Menyembunyikan atau mengosongkan status bawah jika belum ada data awal
        lblStatusPengajuan.setText("⏳ Cuti 06 Apr 2026 — Menunggu Persetujuan");
    }

    /**
     * Menangani aksi klik tombol "Kirim Pengajuan"
     */
    @FXML
    private void handleKirimPengajuan(ActionEvent event) {
        // 1. Validasi Jenis Ketidakhadiran
        ToggleButton selectedToggle = (ToggleButton) jenisCutiGroup.getSelectedToggle();
        if (selectedToggle == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Silakan pilih jenis ketidakhadiran terlebih dahulu!");
            return;
        }
        String jenisCuti = selectedToggle.getText();

        // 2. Validasi Tanggal
        LocalDate tglMulai = dpTanggalMulai.getValue();
        LocalDate tglSelesai = dpTanggalSelesai.getValue();

        if (tglMulai == null || tglSelesai == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Tanggal Mulai dan Tanggal Selesai harus diisi!");
            return;
        }

        if (tglSelesai.isBefore(tglMulai)) {
            showAlert(Alert.AlertType.ERROR, "Kesalahan", "Tanggal Selesai tidak boleh mendahului Tanggal Mulai!");
            return;
        }

        // 3. Validasi Keterangan/Alasan
        String alasan = txtKeterangan.getText().trim();
        if (alasan.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Silakan tuliskan alasan pengajuan secara detail!");
            return;
        }

        // 4. Proses Simulasi Berhasil Kirim
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        String tglMulaiStr = tglMulai.format(formatter);

        // Update label status di bagian bawah halaman sesuai input terbaru
        lblStatusPengajuan.setText("⏳ " + jenisCuti + " " + tglMulaiStr + " — Menunggu Persetujuan");

        // Notifikasi sukses ke user
        showAlert(Alert.AlertType.INFORMATION, "Sukses", "Pengajuan " + jenisCuti + " berhasil dikirim!");

        // Bersihkan form setelah kirim
        txtKeterangan.clear();
    }

    /**
     * Tombol Navigasi Kembali ke Dashboard Utama Lama
     * Menangani onAction dari fxml (#handleKembaliKeDashboard)
     */
    @FXML
    private void handleKembaliKeDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 900, 600));
            stage.setTitle("Manajemen Presensi - Dashboard");
            stage.show();
        } catch (IOException e) {
            System.err.println("Gagal kembali ke Dashboard. Memeriksa file alternatif...");
            e.printStackTrace();
        }
    }

    /**
     * KEMBALI DASHBOARD BARU (Menuju dashboard-karyawan-view.fxml)
     * Menangani onAction dari fxml (#handleKembaliDashboard)
     */
    @FXML
    private void handleKembaliDashboard(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard-karyawan-view.fxml"));

        Scene scene = new Scene(loader.load());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        stage.setScene(scene);

        stage.setTitle("Dashboard");

        stage.show();
    }

    /**
     * Helper membuat Alert Dialog JavaFX
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}