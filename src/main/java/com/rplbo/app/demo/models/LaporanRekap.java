package com.rplbo.app.demo.models;

public class LaporanRekap {
    private String namaKaryawan;
    private String jabatan;
    private int hadir;
    private int terlambat;
    private int cuti;
    private int izin;
    private int alpa;

    public LaporanRekap(String namaKaryawan, String jabatan, int hadir, int terlambat, int cuti, int izin, int alpa) {
        this.namaKaryawan = namaKaryawan;
        this.jabatan = jabatan;
        this.hadir = hadir;
        this.terlambat = terlambat;
        this.cuti = cuti;
        this.izin = izin;
        this.alpa = alpa;
    }

    public String getNamaKaryawan() { return namaKaryawan; }
    public String getJabatan() { return jabatan; }
    public int getHadir() { return hadir; }
    public int getTerlambat() { return terlambat; }
    public int getCuti() { return cuti; }
    public int getIzin() { return izin; }
    public int getAlpa() { return alpa; }
}