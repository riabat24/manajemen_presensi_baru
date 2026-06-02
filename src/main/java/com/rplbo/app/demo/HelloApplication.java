package com.rplbo.app.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Sistem otomatis mencari di 3 kemungkinan path agar pasti ketemu
        URL url = getClass().getResource("/com/rplbo/app/demo/views/login-view.fxml");
        if (url == null) url = getClass().getResource("views/login-view.fxml");
        if (url == null) url = getClass().getResource("/views/login-view.fxml");

        // Jika IntelliJ masih nge-bug belum menyalin file ke target
        if (url == null) {
            throw new RuntimeException("\n\n[INFO PENTING] File FXML belum tersalin ke memori! \nSOLUSI: Klik ikon PALU (Build Project) hijau di bagian atas IntelliJ, lalu Run lagi.\n");
        }

        FXMLLoader fxmlLoader = new FXMLLoader(url);
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);
        stage.setTitle("Manajemen Presensi - Login");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}