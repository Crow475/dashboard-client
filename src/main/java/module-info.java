module org.dashboard.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires dashboard.common.v03e;
    requires com.fasterxml.jackson.databind;
    requires io.github.cdimascio.dotenv.java;
    requires java.sql;
    requires java.desktop;
    requires javafx.web;


    opens org.dashboard.client to javafx.fxml, com.fasterxml.jackson.databind;
    //    opens org.dashboard.client to com.fasterxml.jackson.databind;
    exports org.dashboard.client;
    exports org.dashboard.client.dashboardElements;
    exports org.dashboard.client.util;
    opens org.dashboard.client.util to com.fasterxml.jackson.databind, javafx.fxml;
}