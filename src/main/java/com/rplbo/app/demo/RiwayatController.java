package com.rplbo.app.demo;

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

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class RiwayatController implements Initializable {

    @FXML private TableView<Presensi> tableRiwayat;
    @FXML private TableColumn<Presensi, String> colTanggal;
    @FXML private TableColumn<Presensi, String> colMasuk;
    @FXML private TableColumn<Presensi, String> colKeluar;
    @FXML private TableColumn<Presensi, String> colStatus;
    @FXML private Label lblTotalHadir;
    @FXML private Label lblTerlambat;
    @FXML private Label lblIzin;
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
        cmbFilter.setOnAction(e -> {
            tampilData();
            hitungRekap();
        });

        tampilData();
        hitungRekap();
    }

    private void isiFilterBulan() {
        cmbFilter.getItems().add("Semua Data");
        LocalDate now = LocalDate.now();
        for (int i = 0; i < 6; i++) {
            cmbFilter.getItems().add(now.minusMonths(i).format(filterFormatter));
        }
    }

    private void tampilData() {
        list.clear();
        int idKaryawan = UserSession.getInstance().getIdKaryawan();
        String filter = cmbFilter.getValue();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql;
            PreparedStatement pst;

            if (filter == null || filter.equals("Semua Data")) {
                sql = "SELECT tanggal, jam_masuk, jam_keluar, status_kehadiran, status_waktu FROM presensi WHERE id_karyawan = ? ORDER BY tanggal DESC";
                pst = conn.prepareStatement(sql);
                pst.setInt(1, idKaryawan);
            } else {
                sql = "SELECT tanggal, jam_masuk, jam_keluar, status_kehadiran, status_waktu FROM presensi WHERE id_karyawan = ? AND DATE_FORMAT(tanggal, '%M %Y') = ? ORDER BY tanggal DESC";
                pst = conn.prepareStatement(sql);
                pst.setInt(1, idKaryawan);
                pst.setString(2, filter);
            }

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                String statusKehadiran = rs.getString("status_kehadiran");
                String statusWaktu = rs.getString("status_waktu");
                String status = statusKehadiran != null ? statusKehadiran : "-";
                if ("hadir".equals(statusKehadiran) && statusWaktu != null) {
                    status = statusWaktu.replace("_", " ");
                }
                list.add(new Presensi(
                        rs.getString("tanggal"),
                        rs.getString("jam_masuk") != null ? rs.getString("jam_masuk") : "-",
                        rs.getString("jam_keluar") != null ? rs.getString("jam_keluar") : "-",
                        status
                ));
            }
            tableRiwayat.setItems(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hitungRekap() {
        int idKaryawan = UserSession.getInstance().getIdKaryawan();
        String filter = cmbFilter.getValue();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String where = (filter == null || filter.equals("Semua Data"))
                    ? "WHERE id_karyawan = ?"
                    : "WHERE id_karyawan = ? AND DATE_FORMAT(tanggal, '%M %Y') = ?";

            String[] labels = {"hadir", "terlambat", "izin"};
            String[] conditions = {
                "status_kehadiran = 'hadir' AND status_waktu = 'tepat_waktu'",
                "status_waktu = 'terlambat'",
                "status_kehadiran IN ('izin', 'sakit')"
            };
            Label[] lbls = {lblTotalHadir, lblTerlambat, lblIzin};

            for (int i = 0; i < 3; i++) {
                String sql = "SELECT COUNT(*) as total FROM presensi " + where + " AND " + conditions[i];
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setInt(1, idKaryawan);
                if (filter != null && !filter.equals("Semua Data")) pst.setString(2, filter);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) lbls[i].setText(rs.getString("total"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleKembaliDashboard(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard-karyawan-view.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(loader.load(), 900, 600));
        stage.setTitle("Manajemen Presensi - Dashboard");
        stage.show();
    }
}
