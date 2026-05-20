package com.rplbo.app.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // 1. Panggil halaman login terlebih dahulu
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));

        // 2. Ukuran form login cukup 400x400 atau 400x500
        Scene scene = new Scene(fxmlLoader.load(), 400, 500);

        stage.setTitle("Aplikasi Manajemen Presensi - Login");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}