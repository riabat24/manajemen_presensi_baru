package com.rplbo.app.demo;

public class PengajuanCuti {
    private int idIzin;
    private int idKaryawan;
    private String namaKaryawan;
    private String jenisIzin;
    private String tanggalMulai;
    private String tanggalSelesai;
    private String alasan;
    private String statusPersetujuan;

    public PengajuanCuti(int idIzin, int idKaryawan, String namaKaryawan, String jenisIzin, String tanggalMulai, String tanggalSelesai, String alasan, String statusPersetujuan) {
        this.idIzin = idIzin;
        this.idKaryawan = idKaryawan;
        this.namaKaryawan = namaKaryawan;
        this.jenisIzin = jenisIzin;
        this.tanggalMulai = tanggalMulai;
        this.tanggalSelesai = tanggalSelesai;
        this.alasan = alasan;
        this.statusPersetujuan = statusPersetujuan;
    }

    public int getIdIzin() { return idIzin; }
    public int getIdKaryawan() { return idKaryawan; }
    public String getNamaKaryawan() { return namaKaryawan; }
    public String getJenisIzin() { return jenisIzin; }
    public String getTanggalMulai() { return tanggalMulai; }
    public String getTanggalSelesai() { return tanggalSelesai; }
    public String getAlasan() { return alasan; }
    public String getStatusPersetujuan() { return statusPersetujuan; }
}