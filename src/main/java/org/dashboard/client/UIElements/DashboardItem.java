package org.dashboard.client.UIElements;

import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.event.EventHandler;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import org.dashboard.client.Icons;
import org.dashboard.client.ServerConnector;
import org.dashboard.client.Icons.Icon;
import org.dashboard.client.controls.LoginControl;
import org.dashboard.client.controls.UserViewControl;
import org.dashboard.client.providers.DialogProvider;
import org.dashboard.client.providers.NotificationProvider;
import org.dashboard.client.providers.DialogProvider.Dialog.Type;
import org.dashboard.common.Role;
import org.dashboard.common.models.DashboardModel;
import org.dashboard.common.models.UserOfDashboard;

public class DashboardItem extends VBox {
    private String name;
    private Date lastUpdate;
    private String owner;
    private UserOfDashboard userRalativeTo;
    
    public DashboardItem(DashboardModel dashboard, UserViewControl userViewControl, LoginControl loginControl, NotificationProvider notificationProvider, DialogProvider dialogProvider, ServerConnector serverConnector) {
        super();

        this.name = dashboard.getName();
        this.lastUpdate = dashboard.getUpdatedAt();
        this.owner = dashboard.getOwnerUsername();
        this.setSpacing(5);
        this.setPadding(new Insets(10));
        this.setBorder(new Border(new BorderStroke(Color.rgb(211, 212, 213), BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(1))));

        ServerConnector.UserOfDashboardResult userOfDashboardResult = serverConnector.getUserOfDashboard(loginControl.getUsername(), this.owner, loginControl.getToken(), dashboard.getName());

        if (userOfDashboardResult.success) {
            this.userRalativeTo = userOfDashboardResult.user;
        } else {
            if (userOfDashboardResult.message.equals("Token expired")) {
                loginControl.loginSuccessProperty().set(false);
            }
            notificationProvider.addNotification(new NotificationProvider.Notification("Error", userOfDashboardResult.message, Color.RED));
        }

        SVGPath deleteIcon = Icons.getIcon(Icon.DELETE, 15, 15);
        deleteIcon.setFill(Color.WHITE);
        SVGPath leaveIcon = Icons.getIcon(Icon.LOGOUT, 15, 15);
        leaveIcon.setFill(Color.WHITE);

        HBox topBar = new HBox();
        topBar.setSpacing(5);
        topBar.setAlignment(Pos.BASELINE_CENTER);
        
        Label nameLabel = new Label(this.name);
        nameLabel.setId("dashboard-item-name");

        Label lastUpdateLabel = new Label(new SimpleDateFormat("YYYY-MM-dd HH:mm").format(this.lastUpdate));
        lastUpdateLabel.setId("dashboard-item-secondary");

        Label ownerLabel = new Label(this.owner.equals(loginControl.getUsername()) ? "" : "@" + this.owner);
        ownerLabel.setId("dashboard-item-secondary");

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        topBar.getChildren().addAll(nameLabel, ownerLabel, topSpacer, lastUpdateLabel);

        HBox bottomBar = new HBox();
        bottomBar.setSpacing(5);
        bottomBar.setAlignment(Pos.BASELINE_CENTER);

        Button  openButton = new Button("Open");
        openButton.setId("primary");

        Button deleteButton = new Button("Delete");
        deleteButton.setId("danger");
        deleteButton.setGraphic(deleteIcon);
        deleteButton.setPadding(new Insets(2, 5, 2, 5));

        Button leaveButton = new Button("Leave");
        leaveButton.setId("danger");
        leaveButton.setGraphic(leaveIcon);
        leaveButton.setPadding(new Insets(2, 5, 2, 5));

        Button renameButton = new Button("Rename");

        Region bottomSpacer = new Region();
        HBox.setHgrow(bottomSpacer, Priority.ALWAYS);

        if (loginControl.getUsername().equals(this.owner)) {
            bottomBar.getChildren().addAll(openButton, renameButton, bottomSpacer, deleteButton);
        } else {
            bottomBar.getChildren().addAll(openButton, renameButton, bottomSpacer, leaveButton);
        }

        this.getChildren().addAll(topBar, bottomBar);

        openButton.setOnAction(e -> {
            userViewControl.goToDashboard(dashboard);
        });

        if (this.userRalativeTo.getRole() == Role.OWNER) {
            deleteButton.setVisible(true);
        } else {
            deleteButton.setVisible(false);
        }

        if (this.userRalativeTo.getRole() == Role.OWNER || this.userRalativeTo.getRole() == Role.ADMIN) {
            renameButton.setVisible(true);
        } else {
            renameButton.setVisible(false);
        }

        deleteButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dialogProvider.startDialog(new DialogProvider.Dialog("Delete dashboard '" + name + "'?", "Are you sure you want delete dashboard '" + name + "'?\nThis action is irreversible!", Type.CONFIRMORDENY));

                ChangeListener<DialogProvider.DialogResult> listener = new ChangeListener<DialogProvider.DialogResult>() {
                    @Override
                    public void changed(ObservableValue<? extends DialogProvider.DialogResult> observable, DialogProvider.DialogResult oldValue, DialogProvider.DialogResult newValue) {
                        if (newValue != null) {
                            if (newValue.getConfirmation()) {
                                ServerConnector.DashboardDeleteResult result = serverConnector.deleteDashboard(loginControl.getUsername(), loginControl.getToken(), dashboard.getName());
                                if (result.success) {
                                    userViewControl.resetView();
                                    userViewControl.goToDashboardList();

                                    notificationProvider.addNotification(new NotificationProvider.Notification("Success", "Dashboard '" + name + "' deleted successfully"));

                                } else {
                                    if (result.message.equals("Token expired")) {
                                        loginControl.loginSuccessProperty().set(false);
                                    }
                                    notificationProvider.addNotification(new NotificationProvider.Notification("Error", result.message, Color.RED));
                                }
                            }
                            dialogProvider.resultProperty().removeListener(this);
                        }
                    }
                };

                dialogProvider.resultProperty().addListener(listener);
            }
        });

        renameButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dialogProvider.startDialog(new DialogProvider.Dialog("Rename dashboard '" + name + "'", "Enter the new name for dashboard '" + name + "'", Type.TEXT));

                ChangeListener<DialogProvider.DialogResult> listener = new ChangeListener<DialogProvider.DialogResult>() {
                    @Override
                    public void changed(ObservableValue<? extends DialogProvider.DialogResult> observable, DialogProvider.DialogResult oldValue, DialogProvider.DialogResult newValue) {
                        if (newValue != null) {
                            if (newValue.getConfirmation()) {
                                if (newValue.getType() == DialogProvider.Dialog.Type.TEXT) {
                                    String text = newValue.getText();
                                    ServerConnector.DashboardRenameResult result = serverConnector.renameDashboard(loginControl.getUsername(), loginControl.getToken(), dashboard.getName(), text);
                                    if (result.success) {
                                        userViewControl.resetView();
                                        userViewControl.goToDashboardList();
    
                                        notificationProvider.addNotification(new NotificationProvider.Notification("Success", "Dashboard '" + name + "' renamed to '" + text + "' successfully"));
    
                                    } else {
                                        if (result.message.equals("Token expired")) {
                                            loginControl.loginSuccessProperty().set(false);
                                        }
                                        notificationProvider.addNotification(new NotificationProvider.Notification("Error", result.message, Color.RED));
                                    }
                                }
                            }
                            dialogProvider.resultProperty().removeListener(this);
                        }
                    }
                };

                dialogProvider.resultProperty().addListener(listener);
            }
        });
    
        leaveButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dialogProvider.startDialog(new DialogProvider.Dialog("Leave dashboard '" + name + "'?", "Are you sure you want leave dashboard '" + name + "'?", Type.CONFIRMORDENY));

                ChangeListener<DialogProvider.DialogResult> listener = new ChangeListener<DialogProvider.DialogResult>() {
                    @Override
                    public void changed(ObservableValue<? extends DialogProvider.DialogResult> observable, DialogProvider.DialogResult oldValue, DialogProvider.DialogResult newValue) {
                        if (newValue != null) {
                            if (newValue.getConfirmation()) {
                                ServerConnector.RemoveUserOfDashboardResult result = serverConnector.removeUserOfDashboard(dashboard.getOwnerUsername(), loginControl.getToken(), dashboard.getName(), loginControl.getUsername(), userOfDashboardResult.user.getRole().toString());
                                if (result.success) {
                                    userViewControl.resetView();
                                    userViewControl.goToDashboardList();

                                    notificationProvider.addNotification(new NotificationProvider.Notification("Success", "You have left the dashboard '" + name + "'"));

                                } else {
                                    if (result.message.equals("Token expired")) {
                                        loginControl.loginSuccessProperty().set(false);
                                    }
                                    notificationProvider.addNotification(new NotificationProvider.Notification("Error", result.message, Color.RED));
                                }
                            }
                            dialogProvider.resultProperty().removeListener(this);
                        }
                    }
                };

                dialogProvider.resultProperty().addListener(listener);
            }
        });
    }

    public String getName() {
        return this.name;
    }
}
