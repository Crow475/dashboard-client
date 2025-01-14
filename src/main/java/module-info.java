module org.dashboard.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires dashboard.common.v03b;
    requires com.fasterxml.jackson.databind;
    requires io.github.cdimascio.dotenv.java;
    requires java.sql;


    opens org.dashboard.client to javafx.fxml, com.fasterxml.jackson.databind;
//    opens org.dashboard.client to com.fasterxml.jackson.databind;
    exports org.dashboard.client;
    exports org.dashboard.client.dashboardElements;
}