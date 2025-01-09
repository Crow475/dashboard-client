package org.dashboard.client.views;

import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import org.dashboard.client.ServerConnector;
import org.dashboard.client.controls.LoginControl;
import org.dashboard.client.providers.DialogProvider;
import org.dashboard.client.providers.NotificationProvider;
import org.dashboard.client.controls.UserViewControl;

public class UserView {
    private LoginControl loginControl;
    private NotificationProvider notificationProvider;
    private DialogProvider dialogProvider;
    private ServerConnector serverConnector;
    
    public UserView(LoginControl loginControl, NotificationProvider notificationProvider, DialogProvider dialogProvider, ServerConnector serverConnector) {
        this.loginControl = loginControl;
        this.notificationProvider = notificationProvider;
        this.dialogProvider = dialogProvider;
        this.serverConnector = serverConnector;
    }
    
    public Region getRegion() {
        UserViewControl userViewControl = new UserViewControl();
        
        StackPane root = new StackPane();

        DashboardView dashboardView = new DashboardView(loginControl, userViewControl, notificationProvider, dialogProvider, serverConnector);
        DashboardListView dashboardListView = new DashboardListView(loginControl, userViewControl, notificationProvider, dialogProvider, serverConnector);
        SettingsView settingsView = new SettingsView(loginControl, userViewControl, notificationProvider, dialogProvider);

        userViewControl.viewModeProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue) {
                case DASHBOARD:
                    root.getChildren().clear();
                    root.getChildren().add(dashboardView.getRegion(userViewControl.getCurrentDashboard()));
                    break;
                case DASHBOARDLIST:
                    root.getChildren().clear();
                    root.getChildren().add(dashboardListView.getRegion());
                    break;
                case SETTINGS:
                    root.getChildren().clear();
                    root.getChildren().add(settingsView.getRegion());
                    break;
                case DFEFAULT:
                    root.getChildren().clear();
                    break;
            }
        });

        userViewControl.goToDashboardList();

        loginControl.loginSuccessProperty().addListener((observable, oldValue, isLoggedIn) -> {
            if (!isLoggedIn) {
                userViewControl.resetView();
            } else {
                userViewControl.goToDashboardList();
            }
        });

        return root;
    }
}
