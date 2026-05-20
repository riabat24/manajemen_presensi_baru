module com.rplbo.app.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.rplbo.app.demo to javafx.fxml;
    exports com.rplbo.app.demo;
}