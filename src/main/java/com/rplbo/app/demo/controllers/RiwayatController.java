package com.rplbo.app.demo.controllers;

import com.rplbo.app.demo.util.DatabaseConnection;
import com.rplbo.app.demo.models.Presensi;
import com.rplbo.app.demo.models.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class RiwayatController implements Initializable {
    @FXML private TableView<Presensi> tableRiwayat;
    @FXML private TableColumn<Presensi, String> colTanggal, colMasuk, colKeluar, colStatus;
    @FXML private Label lblTotalHadir, lblTerlambat, lblIzin;
    @FXML private ComboBox<String> cmbFilter;

    private final ObservableList<Presensi> list = FXCollections.observableArrayList();
    private final DateTimeFormatter filterFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", new java.util.Locale("id", "ID"));

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        colMasuk.setCellValueFactory(new PropertyValueFactory<>("jamMasuk"));
        colKeluar.setCellValueFactory(new PropertyValueFactory<>("jamKeluar"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        isiFilterBulan();
        cmbFilter.setValue("Semua Data");
        cmbFilter.setOnAction(e -> { tampilData(); hitungRekap(); });
        tampilData();
        hitungRekap();
    }

    private void isiFilterBulan() {
        cmbFilter.getItems().add("Semua Data");
        for (int i = 0; i < 6; i++) cmbFilter.getItems().add(LocalDate.now().minusMonths(i).format(filterFormatter));
    }

    private void tampilData() {
        list.clear();
        String filter = cmbFilter.getValue();
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement pst;
            if (filter == null || filter.equals("Semua Data")) {
                pst = conn.prepareStatement("SELECT * FROM presensi WHERE id_karyawan = ? ORDER BY tanggal DESC");
                pst.setInt(1, UserSession.getInstance().getIdKaryawan());
            } else {
                pst = conn.prepareStatement("SELECT * FROM presensi WHERE id_karyawan = ? AND DATE_FORMAT(tanggal, '%M %Y') = ? ORDER BY tanggal DESC");
                pst.setInt(1, UserSession.getInstance().getIdKaryawan());
                pst.setString(2, filter);
            }
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                String statusK = rs.getString("status_kehadiran"), statusW = rs.getString("status_waktu");
                String status = statusK != null ? statusK : "-";
                if ("hadir".equals(statusK) && statusW != null) status = statusW.replace("_", " ");
                list.add(new Presensi(rs.getString("tanggal"), rs.getString("jam_masuk") != null ? rs.getString("jam_masuk") : "-", rs.getString("jam_keluar") != null ? rs.getString("jam_keluar") : "-", status));
            }
            tableRiwayat.setItems(list);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // PERBAIKAN: Memisahkan perhitungan Izin agar melihat ke tabel izin_cuti
    private void hitungRekap() {
        String filter = cmbFilter.getValue();
        String wherePresensi = (filter == null || filter.equals("Semua Data")) ? "WHERE id_karyawan = ?" : "WHERE id_karyawan = ? AND DATE_FORMAT(tanggal, '%M %Y') = ?";
        String whereCuti = (filter == null || filter.equals("Semua Data")) ? "WHERE id_karyawan = ? AND status_persetujuan = 'disetujui'" : "WHERE id_karyawan = ? AND status_persetujuan = 'disetujui' AND DATE_FORMAT(tanggal_mulai, '%M %Y') = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            // 1. Hitung Hadir (Dari tabel presensi)
            PreparedStatement pstHadir = conn.prepareStatement("SELECT COUNT(*) as total FROM presensi " + wherePresensi + " AND status_kehadiran = 'hadir' AND status_waktu = 'tepat_waktu'");
            pstHadir.setInt(1, UserSession.getInstance().getIdKaryawan());
            if (filter != null && !filter.equals("Semua Data")) pstHadir.setString(2, filter);
            ResultSet rsHadir = pstHadir.executeQuery();
            if (rsHadir.next()) lblTotalHadir.setText(rsHadir.getString("total"));

            // 2. Hitung Terlambat (Dari tabel presensi)
            PreparedStatement pstTerlambat = conn.prepareStatement("SELECT COUNT(*) as total FROM presensi " + wherePresensi + " AND status_waktu = 'terlambat'");
            pstTerlambat.setInt(1, UserSession.getInstance().getIdKaryawan());
            if (filter != null && !filter.equals("Semua Data")) pstTerlambat.setString(2, filter);
            ResultSet rsTerlambat = pstTerlambat.executeQuery();
            if (rsTerlambat.next()) lblTerlambat.setText(rsTerlambat.getString("total"));

            // 3. Hitung Izin/Cuti (Dari tabel izin_cuti)
            PreparedStatement pstIzin = conn.prepareStatement("SELECT COUNT(*) as total FROM izin_cuti " + whereCuti);
            pstIzin.setInt(1, UserSession.getInstance().getIdKaryawan());
            if (filter != null && !filter.equals("Semua Data")) pstIzin.setString(2, filter);
            ResultSet rsIzin = pstIzin.executeQuery();
            if (rsIzin.next()) lblIzin.setText(rsIzin.getString("total"));

        } catch (Exception e) { e.printStackTrace(); }
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