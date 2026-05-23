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
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;

public class DashboardAdminController implements Initializable {

    // --- KOMPONEN NAVIGASI & PANEL ---
    @FXML public Button btnMenuKaryawan, btnMenuCuti, btnMenuPresensi, btnMenuLaporan;
    @FXML public VBox paneKaryawanUtama, paneCutiUtama, panePresensiUtama, paneLaporanUtama;
    @FXML public Label lblNamaAdmin;

    // --- FITUR 1: KARYAWAN ---
    @FXML public Label lblTotalKaryawan, lblKaryawanAktif, lblKaryawanNonaktif, lblJudulForm;
    @FXML public TextField txtCari, inputNama, inputNik, inputJabatan, inputDepartemen, inputEmail;
    @FXML public RadioButton rbAktif, rbNonaktif;
    @FXML public VBox paneForm;
    @FXML public TableView<Karyawan> tableKaryawan;
    @FXML public TableColumn<Karyawan, Integer> colId;
    @FXML public TableColumn<Karyawan, String> colNama, colNik, colJabatan, colEmail, colStatus;
    private ToggleGroup statusGroup;
    private ObservableList<Karyawan> listKaryawan = FXCollections.observableArrayList();
    private boolean isEditMode = false;
    private int selectedIdKaryawan = -1;

    // --- FITUR 2: CUTI ---
    @FXML public TableView<PengajuanCuti> tableCuti;
    @FXML public TableColumn<PengajuanCuti, Integer> colCutiId;
    @FXML public TableColumn<PengajuanCuti, String> colCutiNama, colCutiJenis, colCutiMulai, colCutiSelesai, colCutiAlasan, colCutiStatus;
    private ObservableList<PengajuanCuti> listCuti = FXCollections.observableArrayList();

    // --- FITUR 3: PRESENSI ---
    @FXML public DatePicker dpFilterPresensi;
    @FXML public TableView<RekamanPresensi> tableRekamanPresensi;
    @FXML public TableColumn<RekamanPresensi, Integer> colPresensiId;
    @FXML public TableColumn<RekamanPresensi, String> colPresensiNama, colPresensiTanggal, colPresensiMasuk, colPresensiKeluar, colPresensiStatus;
    @FXML public VBox paneFormPresensi;
    @FXML public Label lblEditNamaKaryawan, lblEditTanggal;
    @FXML public TextField inputJamMasuk, inputJamKeluar;
    @FXML public ComboBox<String> comboStatusKehadiran, comboStatusWaktu;
    private ObservableList<RekamanPresensi> listPresensi = FXCollections.observableArrayList();
    private int selectedIdPresensi = -1;

    // --- FITUR 4: LAPORAN ---
    @FXML public TableView<LaporanRekap> tableLaporan;
    @FXML public TableColumn<LaporanRekap, String> colLapNama, colLapJabatan;
    @FXML public TableColumn<LaporanRekap, Integer> colLapHadir, colLapTerlambat, colLapCuti, colLapIzin, colLapAlpa;
    private ObservableList<LaporanRekap> listLaporan = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (UserSession.getInstance().getNamaKaryawan() != null) {
            lblNamaAdmin.setText(UserSession.getInstance().getNamaKaryawan());
        }

        colId.setCellValueFactory(new PropertyValueFactory<>("idKaryawan"));
        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colNik.setCellValueFactory(new PropertyValueFactory<>("nik"));
        colJabatan.setCellValueFactory(new PropertyValueFactory<>("jabatan"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colCutiId.setCellValueFactory(new PropertyValueFactory<>("idIzin"));
        colCutiNama.setCellValueFactory(new PropertyValueFactory<>("namaKaryawan"));
        colCutiJenis.setCellValueFactory(new PropertyValueFactory<>("jenisIzin"));
        colCutiMulai.setCellValueFactory(new PropertyValueFactory<>("tanggalMulai"));
        colCutiSelesai.setCellValueFactory(new PropertyValueFactory<>("tanggalSelesai"));
        colCutiAlasan.setCellValueFactory(new PropertyValueFactory<>("alasan"));
        colCutiStatus.setCellValueFactory(new PropertyValueFactory<>("statusPersetujuan"));

        colPresensiId.setCellValueFactory(new PropertyValueFactory<>("idPresensi"));
        colPresensiNama.setCellValueFactory(new PropertyValueFactory<>("namaKaryawan"));
        colPresensiTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        colPresensiMasuk.setCellValueFactory(new PropertyValueFactory<>("jamMasuk"));
        colPresensiKeluar.setCellValueFactory(new PropertyValueFactory<>("jamKeluar"));
        colPresensiStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colLapNama.setCellValueFactory(new PropertyValueFactory<>("namaKaryawan"));
        colLapJabatan.setCellValueFactory(new PropertyValueFactory<>("jabatan"));
        colLapHadir.setCellValueFactory(new PropertyValueFactory<>("hadir"));
        colLapTerlambat.setCellValueFactory(new PropertyValueFactory<>("terlambat"));
        colLapCuti.setCellValueFactory(new PropertyValueFactory<>("cuti"));
        colLapIzin.setCellValueFactory(new PropertyValueFactory<>("izin"));
        colLapAlpa.setCellValueFactory(new PropertyValueFactory<>("alpa"));

        comboStatusKehadiran.setItems(FXCollections.observableArrayList("hadir", "izin", "sakit", "alpha"));
        comboStatusWaktu.setItems(FXCollections.observableArrayList("tepat_waktu", "terlambat", "pulang_cepat"));

        statusGroup = new ToggleGroup();
        rbAktif.setToggleGroup(statusGroup);
        rbNonaktif.setToggleGroup(statusGroup);

        dpFilterPresensi.setValue(LocalDate.now());

        loadStatistik();
        loadDataKaryawan("");
    }

    private void resetMenuColor() {
        btnMenuKaryawan.setStyle("-fx-background-color: transparent; -fx-text-fill: #495057;");
        btnMenuCuti.setStyle("-fx-background-color: transparent; -fx-text-fill: #495057;");
        btnMenuPresensi.setStyle("-fx-background-color: transparent; -fx-text-fill: #495057;");
        btnMenuLaporan.setStyle("-fx-background-color: transparent; -fx-text-fill: #495057;");

        paneKaryawanUtama.setVisible(false); paneKaryawanUtama.setManaged(false);
        paneCutiUtama.setVisible(false); paneCutiUtama.setManaged(false);
        panePresensiUtama.setVisible(false); panePresensiUtama.setManaged(false);
        paneLaporanUtama.setVisible(false); paneLaporanUtama.setManaged(false);

        paneForm.setVisible(false); paneForm.setManaged(false);
        paneFormPresensi.setVisible(false); paneFormPresensi.setManaged(false);
    }

    @FXML public void handleMenuKaryawan(ActionEvent event) { resetMenuColor(); btnMenuKaryawan.setStyle("-fx-background-color: #E9ECEF; -fx-text-fill: #0D6EFD; -fx-font-weight: bold;"); paneKaryawanUtama.setVisible(true); paneKaryawanUtama.setManaged(true); loadDataKaryawan(""); }
    @FXML public void handleMenuCuti(ActionEvent event) { resetMenuColor(); btnMenuCuti.setStyle("-fx-background-color: #E9ECEF; -fx-text-fill: #0D6EFD; -fx-font-weight: bold;"); paneCutiUtama.setVisible(true); paneCutiUtama.setManaged(true); loadDataCuti(); }
    @FXML public void handleMenuPresensi(ActionEvent event) { resetMenuColor(); btnMenuPresensi.setStyle("-fx-background-color: #E9ECEF; -fx-text-fill: #0D6EFD; -fx-font-weight: bold;"); panePresensiUtama.setVisible(true); panePresensiUtama.setManaged(true); loadDataPresensi(dpFilterPresensi.getValue()); }
    @FXML public void handleMenuLaporan(ActionEvent event) { resetMenuColor(); btnMenuLaporan.setStyle("-fx-background-color: #E9ECEF; -fx-text-fill: #0D6EFD; -fx-font-weight: bold;"); paneLaporanUtama.setVisible(true); paneLaporanUtama.setManaged(true); loadDataLaporan(); }

    // PERBAIKAN: Menggunakan KeyEvent untuk onKeyReleased
    @FXML public void handleCari(KeyEvent event) { loadDataKaryawan(txtCari.getText().trim()); }

    @FXML public void handleTambah(ActionEvent event) { isEditMode = false; lblJudulForm.setText("Formulir Karyawan Baru"); bersihkanForm(); paneForm.setVisible(true); paneForm.setManaged(true); }

    // PERBAIKAN: Menggunakan MouseEvent untuk onMouseClicked
    @FXML public void handleKlikTabel(MouseEvent event) {
        Karyawan selected = tableKaryawan.getSelectionModel().getSelectedItem();
        if (selected != null) {
            isEditMode = true; selectedIdKaryawan = selected.getIdKaryawan();
            lblJudulForm.setText("Ubah Data Karyawan");
            inputNama.setText(selected.getNama()); inputNik.setText(selected.getNik());
            inputJabatan.setText(selected.getJabatan()); inputEmail.setText(selected.getEmail());
            if ("aktif".equalsIgnoreCase(selected.getStatus())) rbAktif.setSelected(true); else rbNonaktif.setSelected(true);
            paneForm.setVisible(true); paneForm.setManaged(true);
        }
    }

    @FXML public void handleSimpan(ActionEvent event) {
        String nama = inputNama.getText().trim(), nik = inputNik.getText().trim(), txtJabatan = inputJabatan.getText().trim(), txtDept = inputDepartemen.getText().trim(), email = inputEmail.getText().trim(), status = rbAktif.isSelected() ? "aktif" : "nonaktif";
        if (nama.isEmpty() || nik.isEmpty() || txtJabatan.isEmpty() || txtDept.isEmpty()) { showAlert(Alert.AlertType.WARNING, "Peringatan", "Semua kolom wajib diisi!"); return; }
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (isEditMode) {
                PreparedStatement pst = conn.prepareStatement("UPDATE karyawan SET nama=?, nik=?, id_jabatan=?, id_departemen=?, email=?, status=? WHERE id_karyawan=?");
                pst.setString(1, nama); pst.setString(2, nik); pst.setInt(3, Integer.parseInt(txtJabatan)); pst.setInt(4, Integer.parseInt(txtDept)); pst.setString(5, email); pst.setString(6, status); pst.setInt(7, selectedIdKaryawan); pst.executeUpdate();
            } else {
                PreparedStatement pst = conn.prepareStatement("INSERT INTO karyawan (nama, nik, id_jabatan, id_departemen, email, status, tanggal_masuk, no_telepon) VALUES (?, ?, ?, ?, ?, ?, CURRENT_DATE(), '-')");
                pst.setString(1, nama); pst.setString(2, nik); pst.setInt(3, Integer.parseInt(txtJabatan)); pst.setInt(4, Integer.parseInt(txtDept)); pst.setString(5, email); pst.setString(6, status); pst.executeUpdate();
            }
            loadStatistik(); loadDataKaryawan(""); handleBatal(null); showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data disimpan!");
        } catch (Exception e) { showAlert(Alert.AlertType.ERROR, "Error", e.getMessage()); }
    }

    @FXML public void handleBatal(ActionEvent event) { paneForm.setVisible(false); paneForm.setManaged(false); bersihkanForm(); }

    // PERBAIKAN: Menggunakan MouseEvent untuk onMouseClicked
    @FXML public void handleKlikTabelCuti(MouseEvent event) {
        PengajuanCuti selected = tableCuti.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (!"pending".equalsIgnoreCase(selected.getStatusPersetujuan())) {
            showAlert(Alert.AlertType.INFORMATION, "Informasi", "Pengajuan ini sudah berstatus: " + selected.getStatusPersetujuan().toUpperCase());
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Persetujuan");
        alert.setHeaderText("Pengajuan: " + selected.getJenisIzin().toUpperCase() + " - " + selected.getNamaKaryawan());
        alert.setContentText("Apakah Anda menyetujui permohonan ini?");

        ButtonType btnSetuju = new ButtonType("✔️ Setujui");
        ButtonType btnTolak = new ButtonType("❌ Tolak", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType btnBatal = new ButtonType("Batal", ButtonBar.ButtonData.HELP_2);

        alert.getButtonTypes().setAll(btnSetuju, btnTolak, btnBatal);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() != btnBatal) {
            String statusBaru = (result.get() == btnSetuju) ? "disetujui" : "ditolak";
            String sqlUpdate = "UPDATE izin_cuti SET status_persetujuan = ? WHERE id_izin = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pst = conn.prepareStatement(sqlUpdate)) {
                pst.setString(1, statusBaru); pst.setInt(2, selected.getIdIzin()); pst.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Status berhasil diubah!");
                loadDataCuti();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    @FXML public void handleFilterPresensi(ActionEvent event) { loadDataPresensi(dpFilterPresensi.getValue()); }

    // PERBAIKAN: Menggunakan MouseEvent untuk onMouseClicked
    @FXML public void handleKlikTabelPresensi(MouseEvent event) {
        RekamanPresensi sel = tableRekamanPresensi.getSelectionModel().getSelectedItem();
        if (sel != null) {
            selectedIdPresensi = sel.getIdPresensi();
            lblEditNamaKaryawan.setText(sel.getNamaKaryawan());
            lblEditTanggal.setText(sel.getTanggal());
            inputJamMasuk.setText(sel.getJamMasuk().equals("-") ? "" : sel.getJamMasuk());
            inputJamKeluar.setText(sel.getJamKeluar().equals("-") ? "" : sel.getJamKeluar());
            paneFormPresensi.setVisible(true); paneFormPresensi.setManaged(true);
        }
    }

    @FXML public void handleSimpanPresensi(ActionEvent event) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement pst = conn.prepareStatement("UPDATE presensi SET jam_masuk=?, jam_keluar=?, status_kehadiran=?, status_waktu=? WHERE id_presensi=?");
            pst.setString(1, inputJamMasuk.getText()); pst.setString(2, inputJamKeluar.getText()); pst.setString(3, comboStatusKehadiran.getValue()); pst.setString(4, comboStatusWaktu.getValue()); pst.setInt(5, selectedIdPresensi); pst.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data diperbarui!"); handleBatalPresensi(null); loadDataPresensi(dpFilterPresensi.getValue());
        } catch (Exception e) { showAlert(Alert.AlertType.ERROR, "Error", e.getMessage()); }
    }

    @FXML public void handleBatalPresensi(ActionEvent event) { paneFormPresensi.setVisible(false); paneFormPresensi.setManaged(false); }

    @FXML public void handleUnduhExcel(ActionEvent event) {
        File file = new File("Laporan_Presensi.csv");
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("Nama Karyawan,Jabatan,Hadir,Terlambat,Cuti,Izin,Alpa");
            for (LaporanRekap lap : listLaporan) writer.println(lap.getNamaKaryawan() + "," + lap.getJabatan() + "," + lap.getHadir() + "," + lap.getTerlambat() + "," + lap.getCuti() + "," + lap.getIzin() + "," + lap.getAlpa());
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Laporan berhasil diunduh sebagai Laporan_Presensi.csv");
        } catch (Exception e) { showAlert(Alert.AlertType.ERROR, "Error", e.getMessage()); }
    }

    @FXML public void handleLogout(ActionEvent event) {
        UserSession.getInstance().cleanUserSession();
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/com/rplbo/app/demo/login-view.fxml"))));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    // --- FUNGSI LOAD DATA (DIKEMBALIKAN KE LOGIKA ASLI ANDA) ---
    private void loadStatistik() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT COUNT(*) as total, SUM(CASE WHEN status = 'aktif' THEN 1 ELSE 0 END) as aktif, SUM(CASE WHEN status = 'nonaktif' THEN 1 ELSE 0 END) as nonaktif FROM karyawan";
            ResultSet rs = conn.prepareStatement(sql).executeQuery();
            if (rs.next()) {
                lblTotalKaryawan.setText(String.valueOf(rs.getInt("total")));
                lblKaryawanAktif.setText(String.valueOf(rs.getInt("aktif")));
                lblKaryawanNonaktif.setText(String.valueOf(rs.getInt("nonaktif")));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadDataKaryawan(String keyword) {
        listKaryawan.clear();
        String sql = "SELECT * FROM karyawan WHERE nama LIKE ? ORDER BY id_karyawan DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, "%" + keyword + "%");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                listKaryawan.add(new Karyawan(rs.getInt("id_karyawan"), rs.getString("nama"), rs.getString("nik"), rs.getString("id_jabatan"), rs.getString("email"), rs.getString("status")));
            }
            tableKaryawan.setItems(listKaryawan);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadDataCuti() {
        listCuti.clear();
        String sql = "SELECT ic.*, k.nama FROM izin_cuti ic JOIN karyawan k ON ic.id_karyawan = k.id_karyawan ORDER BY ic.id_izin DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                listCuti.add(new PengajuanCuti(rs.getInt("id_izin"), rs.getInt("id_karyawan"), rs.getString("nama"), rs.getString("jenis_izin"), rs.getString("tanggal_mulai"), rs.getString("tanggal_selesai"), rs.getString("alasan"), rs.getString("status_persetujuan")));
            }
            tableCuti.setItems(listCuti);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadDataPresensi(LocalDate filterDate) {
        listPresensi.clear();
        String sql = "SELECT p.*, k.nama FROM presensi p JOIN karyawan k ON p.id_karyawan = k.id_karyawan ";
        if (filterDate != null) sql += "WHERE p.tanggal = ? ";
        sql += "ORDER BY p.id_presensi DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            if (filterDate != null) {
                pst.setDate(1, java.sql.Date.valueOf(filterDate));
            }
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                String gabungStatus = rs.getString("status_kehadiran");
                if ("hadir".equals(gabungStatus) && rs.getString("status_waktu") != null) {
                    gabungStatus += " (" + rs.getString("status_waktu") + ")";
                }
                listPresensi.add(new RekamanPresensi(rs.getInt("id_presensi"), rs.getString("nama"), rs.getInt("id_karyawan"), rs.getString("tanggal"), rs.getString("jam_masuk") != null ? rs.getString("jam_masuk") : "-", rs.getString("jam_keluar") != null ? rs.getString("jam_keluar") : "-", gabungStatus));
            }
            tableRekamanPresensi.setItems(listPresensi);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadDataLaporan() {
        listLaporan.clear();
        String sql = "SELECT k.nama, j.nama_jabatan, " +
                "SUM(CASE WHEN p.status_kehadiran = 'hadir' THEN 1 ELSE 0 END) as hadir, " +
                "SUM(CASE WHEN p.status_waktu = 'terlambat' THEN 1 ELSE 0 END) as terlambat, " +
                "SUM(CASE WHEN p.status_kehadiran = 'alpha' THEN 1 ELSE 0 END) as alpa, " +
                "(SELECT COUNT(*) FROM izin_cuti ic WHERE ic.id_karyawan = k.id_karyawan AND ic.jenis_izin = 'cuti' AND ic.status_persetujuan = 'disetujui') as cuti, " +
                "(SELECT COUNT(*) FROM izin_cuti ic WHERE ic.id_karyawan = k.id_karyawan AND ic.jenis_izin IN ('sakit', 'kepentingan lain') AND ic.status_persetujuan = 'disetujui') as izin " +
                "FROM karyawan k LEFT JOIN jabatan j ON k.id_jabatan = j.id_jabatan LEFT JOIN presensi p ON k.id_karyawan = p.id_karyawan GROUP BY k.id_karyawan";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {
            while (rs.next()) listLaporan.add(new LaporanRekap(rs.getString("nama"), rs.getString("nama_jabatan"), rs.getInt("hadir"), rs.getInt("terlambat"), rs.getInt("cuti"), rs.getInt("izin"), rs.getInt("alpa")));
            tableLaporan.setItems(listLaporan);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void bersihkanForm() { inputNama.clear(); inputNik.clear(); inputJabatan.clear(); inputDepartemen.clear(); inputEmail.clear(); rbAktif.setSelected(true); selectedIdKaryawan = -1; }
    private void showAlert(Alert.AlertType t, String tit, String c) { Alert a = new Alert(t); a.setTitle(tit); a.setHeaderText(null); a.setContentText(c); a.showAndWait(); }
}