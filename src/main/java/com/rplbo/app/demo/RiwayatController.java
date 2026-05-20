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
import java.util.ResourceBundle;

public class RiwayatController implements Initializable {

    // TABLE
    @FXML
    private TableView<PresensiController> tableRiwayat;

    // COLUMN
    @FXML
    private TableColumn<PresensiController, String> colTanggal;

    @FXML
    private TableColumn<PresensiController, String> colMasuk;

    @FXML
    private TableColumn<PresensiController, String> colKeluar;

    @FXML
    private TableColumn<PresensiController, String> colStatus;

    // LABEL REKAP
    @FXML
    private Label lblTotalHadir;

    @FXML
    private Label lblTerlambat;

    @FXML
    private Label lblIzin;

    // FILTER
    @FXML
    private ComboBox<String> cmbFilter;

    // LIST DATA
    private ObservableList<PresensiController> list =
            FXCollections.observableArrayList();

    @Override
    public void initialize(URL location,
                           ResourceBundle resources) {

        // SET COLUMN
        colTanggal.setCellValueFactory(
                new PropertyValueFactory<>("tanggal")
        );

        colMasuk.setCellValueFactory(
                new PropertyValueFactory<>("jamMasuk")
        );

        colKeluar.setCellValueFactory(
                new PropertyValueFactory<>("jamKeluar")
        );

        colStatus.setCellValueFactory(
                new PropertyValueFactory<>("status")
        );

        // FILTER BULAN
        cmbFilter.getItems().addAll(
                "Semua Data",
                "April 2026",
                "Mei 2026"
        );

        cmbFilter.setValue("Semua Data");

        // LOAD DATA
        tampilData();

        // HITUNG REKAP
        hitungRekap();
    }

    // TAMPILKAN DATA
    private void tampilData() {

        list.clear();

        try {

            Connection conn =
                    DatabaseConnection.getConnection();

            String sql =
                    "SELECT * FROM tb_presensi " +
                            "ORDER BY tanggal DESC";

            PreparedStatement pst =
                    conn.prepareStatement(sql);

            ResultSet rs =
                    pst.executeQuery();

            while (rs.next()) {

                list.add(

                        new Presensi(

                                rs.getString("tanggal"),

                                rs.getString("jam_masuk"),

                                rs.getString("jam_keluar"),

                                rs.getString("status")
                        )
                );
            }

            tableRiwayat.setItems(list);

            rs.close();
            pst.close();
            conn.close();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // HITUNG REKAP
    private void hitungRekap() {

        try {

            Connection conn =
                    DatabaseConnection.getConnection();

            // TOTAL HADIR
            String hadirSql =
                    "SELECT COUNT(*) as total " +
                            "FROM tb_presensi " +
                            "WHERE status='Tepat Waktu'";

            PreparedStatement hadirPst =
                    conn.prepareStatement(hadirSql);

            ResultSet hadirRs =
                    hadirPst.executeQuery();

            if (hadirRs.next()) {

                lblTotalHadir.setText(
                        hadirRs.getString("total")
                );
            }

            // TOTAL TERLAMBAT
            String terlambatSql =
                    "SELECT COUNT(*) as total " +
                            "FROM tb_presensi " +
                            "WHERE status='Terlambat'";

            PreparedStatement terlambatPst =
                    conn.prepareStatement(terlambatSql);

            ResultSet terlambatRs =
                    terlambatPst.executeQuery();

            if (terlambatRs.next()) {

                lblTerlambat.setText(
                        terlambatRs.getString("total")
                );
            }

            // TOTAL IZIN
            String izinSql =
                    "SELECT COUNT(*) as total " +
                            "FROM tb_presensi " +
                            "WHERE status='Izin'";

            PreparedStatement izinPst =
                    conn.prepareStatement(izinSql);

            ResultSet izinRs =
                    izinPst.executeQuery();

            if (izinRs.next()) {

                lblIzin.setText(
                        izinRs.getString("total")
                );
            }

            hadirRs.close();
            hadirPst.close();

            terlambatRs.close();
            terlambatPst.close();

            izinRs.close();
            izinPst.close();

            conn.close();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // KEMBALI DASHBOARD
    @FXML
    private void handleKembaliDashboard(ActionEvent event)
            throws IOException {

        FXMLLoader loader =
                new FXMLLoader(
                        getClass().getResource(
                                "dashboard-karyawan-view.fxml"
                        )
                );

        Scene scene =
                new Scene(loader.load());

        Stage stage =
                (Stage)((Node)event.getSource())
                        .getScene()
                        .getWindow();

        stage.setScene(scene);

        stage.setTitle("Dashboard");

        stage.show();
    }
}