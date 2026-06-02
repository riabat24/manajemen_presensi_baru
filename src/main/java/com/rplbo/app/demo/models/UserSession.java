package com.rplbo.app.demo.models;

public class UserSession {
    private static UserSession instance;

    private int idKaryawan;
    private String username;
    private String namaKaryawan;
    private String role;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setUserSession(int idKaryawan, String username, String namaKaryawan, String role) {
        this.idKaryawan = idKaryawan;
        this.username = username;
        this.namaKaryawan = namaKaryawan;
        this.role = role;
    }

    public int getIdKaryawan() { return idKaryawan; }
    public String getUsername() { return username; }
    public String getNamaKaryawan() { return namaKaryawan; }
    public String getRole() { return role; }

    public void cleanUserSession() {
        idKaryawan = 0;
        username = null;
        namaKaryawan = null;
        role = null;
    }
}