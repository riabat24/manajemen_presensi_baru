package com.rplbo.app.demo.models;

public class RekamanPresensi {
    private int idPresensi;
    private String namaKaryawan;
    private int idKaryawan;
    private String tanggal;
    private String jamMasuk;
    private String jamKeluar;
    private String status;

    public RekamanPresensi(int idPresensi, String namaKaryawan, int idKaryawan, String tanggal, String jamMasuk, String jamKeluar, String status) {
        this.idPresensi = idPresensi;
        this.namaKaryawan = namaKaryawan;
        this.idKaryawan = idKaryawan;
        this.tanggal = tanggal;
        this.jamMasuk = jamMasuk;
        this.jamKeluar = jamKeluar;
        this.status = status;
    }

    public int getIdPresensi() { return idPresensi; }
    public String getNamaKaryawan() { return namaKaryawan; }
    public int getIdKaryawan() { return idKaryawan; }
    public String getTanggal() { return tanggal; }
    public String getJamMasuk() { return jamMasuk; }
    public String getJamKeluar() { return jamKeluar; }
    public String getStatus() { return status; }
}