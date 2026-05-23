package com.rplbo.app.demo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class CutiController implements Initializable {

    // Navigasi Atas & Panel Utama
    @FXML private Button btnAjukanBaru, btnRiwayatPengajuan, btnKembaliDashboard;
    @FXML private VBox paneForm, paneRiwayat;

    // Komponen Form Pengajuan
    @FXML private ToggleButton btnCutiTahunan, btnIzinSakit, btnKeperluanLain;
    @FXML private ToggleGroup jenisCutiGroup;
    @FXML private DatePicker dpTanggalMulai, dpTanggalSelesai;
    @FXML private TextArea txtKeterangan;
    @FXML private Label lblStatusPengajuan;

    // Komponen Tabel Riwayat
    @FXML private TableView<PengajuanCuti> tableRiwayatCuti;
    @FXML private TableColumn<PengajuanCuti, String> colJenis, colMulai, colSelesai, colAlasan, colStatus;
    private ObservableList<PengajuanCuti> listRiwayat = FXCollections.observableArrayList();

    // Style tombol menu atas
    private final String styleMenuAktif = "-fx-background-color: #D2E3FC; -fx-text-fill: #1967D2; -fx-border-color: #AECBFA; -fx-border-radius: 15; -fx-background-radius: 15; -fx-cursor: hand; -fx-font-weight: bold;";
    private final String styleMenuPasif = "-fx-background-color: #F1F3F4; -fx-text-fill: #5F6368; -fx-border-color: #BDC1C6; -fx-border-radius: 15; -fx-background-radius: 15; -fx-cursor: hand;";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Logika pewarnaan toggle button jenis izin
        String styleNetral = "-fx-background-color: #F1F3F4; -fx-text-fill: #5F6368; -fx-border-color: #BDC1C6; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;";
        String styleCuti = "-fx-background-color: #D2E3FC; -fx-text-fill: #1967D2; -fx-border-color: #AECBFA; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;";
        String styleSakit = "-fx-background-color: #FCE8E6; -fx-text-fill: #C5221F; -fx-border-color: #FAD2CF; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;";
        String styleLain = "-fx-background-color: #FEF7E0; -fx-text-fill: #B06000; -fx-border-color: #F5E3B3; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;";

        btnCutiTahunan.setStyle(styleCuti);
        btnIzinSakit.setStyle(styleNetral);
        btnKeperluanLain.setStyle(styleNetral);
        btnCutiTahunan.setSelected(true);

        jenisCutiGroup.selectedToggleProperty().addListener((obs, oldBtn, newBtn) -> {
            if (newBtn == null) {
                oldBtn.setSelected(true);
                return;
            }
            btnCutiTahunan.setStyle(styleNetral);
            btnIzinSakit.setStyle(styleNetral);
            btnKeperluanLain.setStyle(styleNetral);

            if (newBtn == btnCutiTahunan) btnCutiTahunan.setStyle(styleCuti);
            else if (newBtn == btnIzinSakit) btnIzinSakit.setStyle(styleSakit);
            else if (newBtn == btnKeperluanLain) btnKeperluanLain.setStyle(styleLain);
        });

        // Setup Kolom Tabel Riwayat
        colJenis.setCellValueFactory(new PropertyValueFactory<>("jenisIzin"));
        colMulai.setCellValueFactory(new PropertyValueFactory<>("tanggalMulai"));
        colSelesai.setCellValueFactory(new PropertyValueFactory<>("tanggalSelesai"));
        colAlasan.setCellValueFactory(new PropertyValueFactory<>("alasan"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusPersetujuan"));

        dpTanggalMulai.setValue(LocalDate.now());
        dpTanggalSelesai.setValue(LocalDate.now());
    }

    // --- LOGIKA PERPINDAHAN MENU (AJUKAN BARU vs RIWAYAT) ---

    @FXML
    void handleAjukanBaru(ActionEvent event) {
        paneForm.setVisible(true);
        paneForm.setManaged(true);
        paneRiwayat.setVisible(false);
        paneRiwayat.setManaged(false);

        btnAjukanBaru.setStyle(styleMenuAktif);
        btnRiwayatPengajuan.setStyle(styleMenuPasif);
    }

    @FXML
    void handleRiwayatPengajuan(ActionEvent event) {
        paneForm.setVisible(false);
        paneForm.setManaged(false);
        paneRiwayat.setVisible(true);
        paneRiwayat.setManaged(true);

        btnAjukanBaru.setStyle(styleMenuPasif);
        btnRiwayatPengajuan.setStyle(styleMenuAktif);

        loadRiwayatCuti(); // Otomatis ambil data dari MySQL saat tab riwayat dibuka
    }

    private void loadRiwayatCuti() {
        listRiwayat.clear();
        String sql = "SELECT * FROM izin_cuti WHERE id_karyawan = ? ORDER BY id_izin DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, UserSession.getInstance().getIdKaryawan());
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                listRiwayat.add(new PengajuanCuti(
                        rs.getInt("id_izin"),
                        rs.getInt("id_karyawan"),
                        UserSession.getInstance().getNamaKaryawan(),
                        rs.getString("jenis_izin").toUpperCase(),
                        rs.getString("tanggal_mulai"),
                        rs.getString("tanggal_selesai"),
                        rs.getString("alasan"),
                        rs.getString("status_persetujuan").toUpperCase()
                ));
            }
            tableRiwayatCuti.setItems(listRiwayat);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- LOGIKA KIRIM PENGAJUAN (INSERT KE DATABASE) ---

    @FXML
    private void handleKirimPengajuan(ActionEvent event) {
        ToggleButton selectedToggle = (ToggleButton) jenisCutiGroup.getSelectedToggle();
        if (selectedToggle == null) return;

        String jenisCutiUI = selectedToggle.getText();
        String jenisIzinDB = "cuti";

        if (jenisCutiUI.equalsIgnoreCase("Izin Sakit")) jenisIzinDB = "sakit";
        else if (jenisCutiUI.equalsIgnoreCase("Keperluan Lain")) jenisIzinDB = "kepentingan lain";

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

        String alasan = txtKeterangan.getText().trim();
        if (alasan.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Silakan tuliskan alasan pengajuan!");
            return;
        }

        // Simpan ke Database
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO izin_cuti (id_karyawan, jenis_izin, tanggal_mulai, tanggal_selesai, alasan, status_persetujuan) " +
                    "VALUES (?, ?, ?, ?, ?, 'pending')";

            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, UserSession.getInstance().getIdKaryawan());
            pst.setString(2, jenisIzinDB);
            pst.setDate(3, java.sql.Date.valueOf(tglMulai));
            pst.setDate(4, java.sql.Date.valueOf(tglSelesai));
            pst.setString(5, alasan);

            if (pst.executeUpdate() > 0) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
                // Label bawah sekarang otomatis berubah setelah berhasil disimpan
                lblStatusPengajuan.setText("⏳ " + jenisCutiUI + " " + tglMulai.format(formatter) + " — Menunggu Persetujuan");

                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Pengajuan berhasil dikirim ke Admin!");
                txtKeterangan.clear();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menyimpan pengajuan!\nDetail: " + e.getMessage());
        }
    }

    @FXML
    private void handleKembaliDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard-karyawan-view.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Manajemen Presensi - Dashboard Karyawan");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}