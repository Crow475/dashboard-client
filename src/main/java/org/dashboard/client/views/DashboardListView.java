package org.dashboard.client.views;

import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.CornerRadii;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import org.dashboard.common.models.DashboardModel;
import org.dashboard.client.Icons;
import org.dashboard.client.ServerConnector;
import org.dashboard.client.Icons.Icon;
import org.dashboard.client.UIElements.DashboardItem;
import org.dashboard.client.controls.LoginControl;
import org.dashboard.client.controls.UserViewControl;
import org.dashboard.client.providers.NotificationProvider;
import org.dashboard.client.providers.DialogProvider;

public class DashboardListView {
    private LoginControl loginControl;
    private UserViewControl userViewControl;
    private NotificationProvider notificationProvider;
    private DialogProvider dialogProvider;
    private ObservableList<DashboardModel> dashboards = FXCollections.observableArrayList();
    private ServerConnector serverConnector;
    
    public DashboardListView(LoginControl loginControl, UserViewControl userViewControl, NotificationProvider notificationProvider, DialogProvider dialogProvider, ServerConnector serverConnector) {
        this.loginControl = loginControl;
        this.userViewControl = userViewControl;
        this.notificationProvider = notificationProvider;
        this.dialogProvider = dialogProvider;
        this.serverConnector = serverConnector;

        if (loginControl.isLoginSuccess() && loginControl.getUsername() != null) {
            ServerConnector.UserDashboardsResult result = serverConnector.getUserDashboards(loginControl.getUsername(), loginControl.getToken());
            if (result.success) {
                this.dashboards.addAll(result.dashboards);
            } else {
                if (result.message.equals("Token expired")) {
                    loginControl.loginSuccessProperty().set(false);
                }
                this.notificationProvider.addNotification(new NotificationProvider.Notification("Error", result.message, Color.RED));
            }
        }
    }

    public Region getRegion() {
        BorderPane root = new BorderPane();
        
        SVGPath settingsIcon = Icons.getIcon(Icon.USER_SETTINGS, 15, 15);
        settingsIcon.setFill(Color.WHITE);
        SVGPath newDashboardIcon = Icons.getIcon(Icon.ADD, 15, 15);
        newDashboardIcon.setFill(Color.WHITE);
        SVGPath logoutIcon = Icons.getIcon(Icon.LOGOUT, 15, 15);
        logoutIcon.setFill(Color.WHITE);
        
        VBox sideBar = new VBox();
        sideBar.setAlignment(Pos.TOP_CENTER);
        sideBar.setSpacing(10);
        sideBar.setPadding(new Insets(10, 5, 10, 5));
        sideBar.prefWidthProperty().bind(root.widthProperty().divide(4));
        sideBar.setId("dashboard-menu-left");
        sideBar.setMaxWidth(200);
        
        Label usernameLabel = new Label();
        usernameLabel.textProperty().bind(loginControl.usernameProperty());
        usernameLabel.setId("username-label");
        usernameLabel.setPadding(new Insets(0, 5, 0, 5));
        
        Button settingsButton = new Button("Settings");
        settingsButton.setGraphic(settingsIcon);
        settingsButton.setPadding(new Insets(2, 5, 2, 5));
        
        Button logoutButton = new Button("");
        logoutButton.setId("danger");
        logoutButton.setGraphic(logoutIcon);
        logoutButton.setPadding(new Insets(2, 5, 2, 5));
        
        GridPane userControl = new GridPane();
        userControl.setHgap(10);
        userControl.setVgap(5);
        userControl.setBorder(new Border(new BorderStroke(Color.rgb(211, 212, 213), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 0, 1, 0))));
        userControl.prefWidthProperty().bind(sideBar.widthProperty());
        userControl.setPadding(new Insets(0, 0, 5, 0));
        userControl.add(usernameLabel, 0, 0, 2, 1);
        userControl.add(settingsButton, 0, 1);
        userControl.add(logoutButton, 1, 1);
        
        GridPane.setHalignment(logoutButton, HPos.RIGHT);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button newDashboardButton = new Button("New Dashboard");
        newDashboardButton.setId("primary");
        newDashboardButton.setGraphic(newDashboardIcon);
        newDashboardButton.setPadding(new Insets(2, 5, 2, 5));

        sideBar.getChildren().addAll(userControl, spacer, newDashboardButton);
        
        logoutButton.setOnAction(event -> {
            ServerConnector.LogoutResult result = serverConnector.logout(loginControl.getUsername(), loginControl.getToken());
            if (result.success) {
                DashboardListView.this.notificationProvider.addNotification(new NotificationProvider.Notification("Logged out", "Logged out of the seession"));
            } else {
                this.notificationProvider.addNotification(new NotificationProvider.Notification("Error", result.message, Color.RED));
            }
            DashboardListView.this.loginControl.loginSuccessProperty().set(false);
        });

        
        VBox list = new VBox();
        list.setAlignment(Pos.TOP_LEFT);
        list.setSpacing(10);
        list.setPadding(new Insets(5));
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(list);
        scrollPane.setFitToWidth(true);

        this.dashboards.addListener(new ListChangeListener<DashboardModel>() {
            @Override
            public void onChanged(Change<? extends DashboardModel> change) {
                while (change.next()) {
                    if (change.wasAdded()) {
                        for (DashboardModel dashboard : change.getAddedSubList()) {
                            DashboardItem item = new DashboardItem(
                                dashboard,
                                DashboardListView.this.userViewControl,
                                DashboardListView.this.loginControl,
                                DashboardListView.this.notificationProvider,
                                DashboardListView.this.dialogProvider,
                                DashboardListView.this.serverConnector
                            );
                            list.getChildren().add(item);
                        }
                    }
                    if (change.wasRemoved()) {
                        for (DashboardModel dashboard : change.getRemoved()) {
                            for (int i = 0; i < list.getChildren().size(); i++) {
                                if (list.getChildren().get(i) instanceof DashboardItem) {
                                    DashboardItem item = (DashboardItem) list.getChildren().get(i);
                                    if (item.getName().equals(dashboard.getName())) {
                                        list.getChildren().remove(i);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });

        root.setLeft(sideBar);
        root.setCenter(scrollPane);

        settingsButton.setOnAction(event -> {
            userViewControl.goToSettings();
        });

        newDashboardButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                DashboardListView.this.dialogProvider.startDialog(new DialogProvider.Dialog("New Dashboard", "Enter the name of the new dashboard", DialogProvider.Dialog.Type.TEXT));

                ChangeListener<DialogProvider.DialogResult> listener = new ChangeListener<DialogProvider.DialogResult>() {
                    @Override
                    public void changed(ObservableValue<? extends DialogProvider.DialogResult> observable, DialogProvider.DialogResult oldValue, DialogProvider.DialogResult newValue) {
                        if (newValue != null) {
                            if (newValue.getConfirmation()) {
                                if (newValue.getType() == DialogProvider.Dialog.Type.TEXT) {
                                    String text = newValue.getText();
                                    ServerConnector.DashboardCreateResult createResult = serverConnector.createDashboard(loginControl.getUsername(), loginControl.getToken(), text);
                                    if (createResult.success) {
                                        dashboards.clear();
                                        ServerConnector.UserDashboardsResult result = serverConnector.getUserDashboards(loginControl.getUsername(), loginControl.getToken());
                                        if (result.success) {
                                            dashboards.addAll(result.dashboards);
                                        } else {
                                            if (result.message.equals("Token expired")) {
                                                loginControl.loginSuccessProperty().set(false);
                                            }
                                            DashboardListView.this.notificationProvider.addNotification(new NotificationProvider.Notification("Error", result.message, Color.RED));
                                        }
                                        DashboardListView.this.notificationProvider.addNotification(new NotificationProvider.Notification("Success", "Dashboard " + createResult.message + " created"));
                                    } else {
                                        if (createResult.message.equals("Token expired")) {
                                            loginControl.loginSuccessProperty().set(false);
                                        }
                                        DashboardListView.this.notificationProvider.addNotification(new NotificationProvider.Notification("Error", createResult.message, Color.RED));
                                    }
                                }
                            }
                            DashboardListView.this.dialogProvider.resultProperty().removeListener(this);
                        }
                    }
                };

                DashboardListView.this.dialogProvider.resultProperty().addListener(listener);
                
            }
        });

        loginControl.loginSuccessProperty().addListener((observable, oldValue, isLoggedIn) -> {
            if (isLoggedIn && loginControl.getUsername() != null) {
                dashboards.clear();
                ServerConnector.UserDashboardsResult result = serverConnector.getUserDashboards(loginControl.getUsername(), loginControl.getToken());
                if (result.success) {
                    this.dashboards.addAll(result.dashboards);
                } else {
                    if (result.message.equals("Token expired")) {
                        loginControl.loginSuccessProperty().set(false);
                    }
                    this.notificationProvider.addNotification(new NotificationProvider.Notification("Error", result.message, Color.RED));
                }
            }
        });

        userViewControl.viewModeProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == UserViewControl.mode.DASHBOARDLIST) {
                dashboards.clear();
                ServerConnector.UserDashboardsResult result = serverConnector.getUserDashboards(loginControl.getUsername(), loginControl.getToken());
                if (result.success) {
                    this.dashboards.addAll(result.dashboards);
                } else {
                    if (result.message.equals("Token expired")) {
                        loginControl.loginSuccessProperty().set(false);
                    }
                    this.notificationProvider.addNotification(new NotificationProvider.Notification("Error", result.message, Color.RED));
                }
            }
        });

        return root;
    }
}
