package com.rplbo.app.demo;

public class Karyawan {
    private int idKaryawan;
    private String nama;
    private String nik;
    private String jabatan;
    private String email;
    private String status;

    public Karyawan(int idKaryawan, String nama, String nik, String jabatan, String email, String status) {
        this.idKaryawan = idKaryawan;
        this.nama = nama;
        this.nik = nik;
        this.jabatan = jabatan;
        this.email = email;
        this.status = status;
    }

    public int getIdKaryawan() { return idKaryawan; }
    public String getNama() { return nama; }
    public String getNik() { return nik; }
    public String getJabatan() { return jabatan; }
    public String getEmail() { return email; }
    public String getStatus() { return status; }
}