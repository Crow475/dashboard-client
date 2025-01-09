package org.dashboard.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.EventHandler;
import javafx.stage.WindowEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.IOException;

import org.dashboard.client.providers.DialogProvider;
import org.dashboard.client.providers.NotificationProvider;
import org.dashboard.client.controls.LoginControl;
import org.dashboard.client.views.LoginView;
import org.dashboard.client.views.UserView;

public class App extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        LoginControl loginControl = new LoginControl();

        ServerConnector serverConnector = new ServerConnector();
        serverConnector.connect();

        NotificationProvider notificationProvider = new NotificationProvider();
        VBox notificationPane = notificationProvider.getPane();

        DialogProvider dialogProvider = new DialogProvider();
        VBox dialogPane = dialogProvider.getPane();

        LoginView loginView = new LoginView(loginControl, notificationProvider, serverConnector);
        UserView userView = new UserView(loginControl, notificationProvider, dialogProvider, serverConnector);

        Region loginRegion = loginView.getRegion();
        Region userRegion = userView.getRegion();

        AnchorPane root = new AnchorPane();
        root.getChildren().addAll(loginRegion, notificationPane, dialogPane);

        AnchorPane.setTopAnchor(loginRegion, 0.0);
        AnchorPane.setBottomAnchor(loginRegion, 0.0);
        AnchorPane.setLeftAnchor(loginRegion, 0.0);
        AnchorPane.setRightAnchor(loginRegion, 0.0);

        AnchorPane.setTopAnchor(userRegion, 0.0);
        AnchorPane.setBottomAnchor(userRegion, 0.0);
        AnchorPane.setLeftAnchor(userRegion, 0.0);
        AnchorPane.setRightAnchor(userRegion, 0.0);

        AnchorPane.setRightAnchor(notificationPane, 10.0);
        AnchorPane.setBottomAnchor(notificationPane, 10.0);

        AnchorPane.setTopAnchor(dialogPane, 0.0);
        AnchorPane.setBottomAnchor(dialogPane, 0.0);
        AnchorPane.setLeftAnchor(dialogPane, 0.0);
        AnchorPane.setRightAnchor(dialogPane, 0.0);

        loginControl.loginSuccessProperty().addListener((observable, oldValue, isLoggedIn) -> {
            root.getChildren().clear();
            if (isLoggedIn) {
                root.getChildren().add(userRegion);
            } else {
                root.getChildren().add(loginRegion);
            }
            root.getChildren().add(notificationPane);
            root.getChildren().add(dialogPane);
        });

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                serverConnector.disconnect();
            }
        });

        Scene scene = new Scene(root, 640, 480);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm()); 

        stage.setTitle("Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}