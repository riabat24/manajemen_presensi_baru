package com.rplbo.app.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class HelloController {
    public Label jamLabel;
    public Label tanggalLabel;
    public Label clockInLabel;
    public Label statusMasuk;
    public Label clockOutLabel;
    public Label statusKeluar;
    public Button btnPresensi;
    public Label lblStatus;
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    public void handlePresensi(ActionEvent event) {
    }
}
