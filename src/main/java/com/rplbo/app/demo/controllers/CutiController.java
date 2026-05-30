package com.rplbo.app.demo.controllers;

import com.rplbo.app.demo.util.DatabaseConnection;
import com.rplbo.app.demo.models.PengajuanCuti;
import com.rplbo.app.demo.models.UserSession;
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

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class CutiController implements Initializable {
    @FXML private Button btnAjukanBaru, btnRiwayatPengajuan;
    @FXML private VBox paneForm, paneRiwayat;
    @FXML private ToggleButton btnCutiTahunan, btnIzinSakit, btnKeperluanLain;
    @FXML private ToggleGroup jenisCutiGroup;
    @FXML private DatePicker dpTanggalMulai, dpTanggalSelesai;
    @FXML private TextArea txtKeterangan;
    @FXML private Label lblStatusPengajuan;
    @FXML private TableView<PengajuanCuti> tableRiwayatCuti;
    @FXML private TableColumn<PengajuanCuti, String> colJenis, colMulai, colSelesai, colAlasan, colStatus;
    private ObservableList<PengajuanCuti> listRiwayat = FXCollections.observableArrayList();

    private final String styleMenuAktif = "-fx-background-color: #D2E3FC; -fx-text-fill: #1967D2; -fx-border-color: #AECBFA; -fx-border-radius: 15; -fx-background-radius: 15; -fx-cursor: hand; -fx-font-weight: bold;";
    private final String styleMenuPasif = "-fx-background-color: #F1F3F4; -fx-text-fill: #5F6368; -fx-border-color: #BDC1C6; -fx-border-radius: 15; -fx-background-radius: 15; -fx-cursor: hand;";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String styleNetral = "-fx-background-color: #F1F3F4; -fx-text-fill: #5F6368; -fx-border-color: #BDC1C6; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;";
        String styleCuti = "-fx-background-color: #D2E3FC; -fx-text-fill: #1967D2; -fx-border-color: #AECBFA; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;";
        String styleSakit = "-fx-background-color: #FCE8E6; -fx-text-fill: #C5221F; -fx-border-color: #FAD2CF; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;";
        String styleLain = "-fx-background-color: #FEF7E0; -fx-text-fill: #B06000; -fx-border-color: #F5E3B3; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;";

        btnCutiTahunan.setStyle(styleCuti); btnIzinSakit.setStyle(styleNetral); btnKeperluanLain.setStyle(styleNetral); btnCutiTahunan.setSelected(true);
        jenisCutiGroup.selectedToggleProperty().addListener((obs, oldBtn, newBtn) -> {
            if (newBtn == null) { oldBtn.setSelected(true); return; }
            btnCutiTahunan.setStyle(styleNetral); btnIzinSakit.setStyle(styleNetral); btnKeperluanLain.setStyle(styleNetral);
            if (newBtn == btnCutiTahunan) btnCutiTahunan.setStyle(styleCuti);
            else if (newBtn == btnIzinSakit) btnIzinSakit.setStyle(styleSakit);
            else if (newBtn == btnKeperluanLain) btnKeperluanLain.setStyle(styleLain);
        });

        colJenis.setCellValueFactory(new PropertyValueFactory<>("jenisIzin")); colMulai.setCellValueFactory(new PropertyValueFactory<>("tanggalMulai"));
        colSelesai.setCellValueFactory(new PropertyValueFactory<>("tanggalSelesai")); colAlasan.setCellValueFactory(new PropertyValueFactory<>("alasan"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusPersetujuan"));
        dpTanggalMulai.setValue(LocalDate.now()); dpTanggalSelesai.setValue(LocalDate.now());
    }

    @FXML void handleAjukanBaru(ActionEvent event) {
        paneForm.setVisible(true); paneForm.setManaged(true); paneRiwayat.setVisible(false); paneRiwayat.setManaged(false);
        btnAjukanBaru.setStyle(styleMenuAktif); btnRiwayatPengajuan.setStyle(styleMenuPasif);
    }

    @FXML void handleRiwayatPengajuan(ActionEvent event) {
        paneForm.setVisible(false); paneForm.setManaged(false); paneRiwayat.setVisible(true); paneRiwayat.setManaged(true);
        btnAjukanBaru.setStyle(styleMenuPasif); btnRiwayatPengajuan.setStyle(styleMenuAktif);
        loadRiwayatCuti();
    }

    private void loadRiwayatCuti() {
        listRiwayat.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement("SELECT * FROM izin_cuti WHERE id_karyawan = ? ORDER BY id_izin DESC")) {
            pst.setInt(1, UserSession.getInstance().getIdKaryawan());
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                listRiwayat.add(new PengajuanCuti(
                        rs.getInt("id_izin"), rs.getInt("id_karyawan"), UserSession.getInstance().getNamaKaryawan(),
                        rs.getString("jenis_izin").toUpperCase(), rs.getString("tanggal_mulai"), rs.getString("tanggal_selesai"), rs.getString("alasan"), rs.getString("status_persetujuan").toUpperCase()
                ));
            }
            tableRiwayatCuti.setItems(listRiwayat);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleKirimPengajuan(ActionEvent event) {
        ToggleButton selectedToggle = (ToggleButton) jenisCutiGroup.getSelectedToggle();
        if (selectedToggle == null) return;
        String jenisCutiUI = selectedToggle.getText();
        String jenisIzinDB = jenisCutiUI.equalsIgnoreCase("Izin Sakit") ? "sakit" : (jenisCutiUI.equalsIgnoreCase("Keperluan Lain") ? "kepentingan lain" : "cuti");
        LocalDate tglMulai = dpTanggalMulai.getValue(), tglSelesai = dpTanggalSelesai.getValue();

        if (tglMulai == null || tglSelesai == null) { showAlert(Alert.AlertType.WARNING, "Peringatan", "Tanggal harus diisi!"); return; }
        if (tglSelesai.isBefore(tglMulai)) { showAlert(Alert.AlertType.ERROR, "Kesalahan", "Tanggal Selesai salah!"); return; }
        if (txtKeterangan.getText().trim().isEmpty()) { showAlert(Alert.AlertType.WARNING, "Peringatan", "Isi alasan!"); return; }

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement pst = conn.prepareStatement("INSERT INTO izin_cuti (id_karyawan, jenis_izin, tanggal_mulai, tanggal_selesai, alasan, status_persetujuan) VALUES (?, ?, ?, ?, ?, 'pending')");
            pst.setInt(1, UserSession.getInstance().getIdKaryawan()); pst.setString(2, jenisIzinDB); pst.setDate(3, java.sql.Date.valueOf(tglMulai)); pst.setDate(4, java.sql.Date.valueOf(tglSelesai)); pst.setString(5, txtKeterangan.getText().trim());
            if (pst.executeUpdate() > 0) {
                lblStatusPengajuan.setText("⏳ " + jenisCutiUI + " — Menunggu Persetujuan");
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Berhasil dikirim!"); txtKeterangan.clear();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
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