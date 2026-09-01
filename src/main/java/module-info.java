module com.example.chatdesktop {

    requires javafx.controls;
    requires javafx.fxml;

    requires java.net.http;
    requires java.sql;
    requires java.prefs;

    requires com.google.gson;

    exports com.example.chatdesktop;

    opens com.example.chatdesktop.controller
            to javafx.fxml;
}