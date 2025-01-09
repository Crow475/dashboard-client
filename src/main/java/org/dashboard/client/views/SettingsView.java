package org.dashboard.client.views;

import org.dashboard.client.controls.LoginControl;
import org.dashboard.client.controls.UserViewControl;
import org.dashboard.client.providers.DialogProvider;
import org.dashboard.client.providers.NotificationProvider;

import javafx.scene.layout.Region;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

public class SettingsView {
    private LoginControl loginControl;
    private UserViewControl userViewControl;
    private NotificationProvider notificationProvider;
    private DialogProvider dialogProvider;

    public SettingsView(LoginControl loginControl, UserViewControl userViewControl, NotificationProvider notificationProvider, DialogProvider dialogProvider) {
        this.loginControl = loginControl;
        this.userViewControl = userViewControl;
        this.notificationProvider = notificationProvider;
        this.dialogProvider = dialogProvider;
    }

    public Region getRegion() {
        BorderPane root = new BorderPane();

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> userViewControl.goToDashboardList());

        root.setTop(backButton);

        return root;
    }
}
