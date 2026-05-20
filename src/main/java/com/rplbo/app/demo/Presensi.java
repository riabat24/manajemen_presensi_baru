package com.rplbo.app.demo;

public class Presensi extends PresensiController {

    private String tanggal;
    private String jamMasuk;
    private String jamKeluar;
    private String status;

    public Presensi(String tanggal,
                    String jamMasuk,
                    String jamKeluar,
                    String status) {

        this.tanggal = tanggal;
        this.jamMasuk = jamMasuk;
        this.jamKeluar = jamKeluar;
        this.status = status;
    }

    public String getTanggal() {
        return tanggal;
    }

    public String getJamMasuk() {
        return jamMasuk;
    }

    public String getJamKeluar() {
        return jamKeluar;
    }

    public String getStatus() {
        return status;
    }
}