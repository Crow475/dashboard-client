package org.dashboard.client.UIElements;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.beans.value.ObservableValue;

import org.dashboard.common.models.DashboardModel;
import org.dashboard.common.models.UserOfDashboard;

import org.dashboard.client.ServerConnector;
import org.dashboard.client.ServerConnector.UpdateUserOfDashboardResult;
import org.dashboard.client.controls.EditModeControl;
import org.dashboard.client.controls.LoginControl;
import org.dashboard.client.controls.UserViewControl;
import org.dashboard.client.providers.DialogProvider;
import org.dashboard.client.providers.NotificationProvider;

public class UserEditPane extends VBox {
    public UserEditPane(EditModeControl editModeControl, LoginControl loginControl, UserViewControl userViewControl, NotificationProvider notificationProvider, DialogProvider dialogProvider, ServerConnector serverConnector, DashboardModel dashboardModel, ObservableList<UserOfDashboard> people) {
        super();
        
        ObservableList<String> roles = FXCollections.observableArrayList("admin", "editor", "viewer");
        SimpleStringProperty initialRole = new SimpleStringProperty();

        this.setSpacing(5);
        this.setPadding(new Insets(20));
        
        Label usernameLabel = new Label();
        usernameLabel.setId("username-label");
        
        VBox roleVbox = new VBox();
        roleVbox.setSpacing(3);
        
        Label roleLabel = new Label("Role:");

        ComboBox<String> roleComboBox = new ComboBox<>(roles);

        HBox roleHbox = new HBox();
        roleHbox.setSpacing(5);

        Button confirmButton = new Button("Change");
        confirmButton.setId("primary");
        confirmButton.setDisable(true);

        roleHbox.getChildren().addAll(roleComboBox, confirmButton);
        
        roleVbox.getChildren().addAll(roleLabel, roleHbox);

        Region spacer = new Region();
        spacer.setPrefHeight(10);

        Button removeUserButton = new Button("Remove user");
        removeUserButton.setId("danger");
        
        this.getChildren().addAll(usernameLabel, roleVbox, spacer, removeUserButton);

        editModeControl.userToEditProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                usernameLabel.setText(newValue.getUsername());
                roleComboBox.setValue(newValue.getRole().toString());
                initialRole.set(newValue.getRole().toString());

                if (newValue.getUsername().equals(loginControl.getUsername())) {
                    removeUserButton.setText("Leave dashboard");
                    usernameLabel.setText(usernameLabel.getText() + " (you)");
                }
            }
        });

        roleComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && initialRole.get() != null) {
                if (newValue.equals(initialRole.get())) {
                    confirmButton.setDisable(true);
                } else {
                    confirmButton.setDisable(false);
                }
            }
        });

        confirmButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UpdateUserOfDashboardResult result = serverConnector.updateUserOfDashboard(dashboardModel.getOwnerUsername(), loginControl.getToken(), dashboardModel.getName(), editModeControl.userToEditProperty().get().getUsername(), roleComboBox.getValue());
                if (result.success) {
                    people.clear();

                    ServerConnector.GetDashboardUsersResult dashboardUsersResult = serverConnector.getDashboardUsers(dashboardModel.getOwnerUsername(), loginControl.getToken(), dashboardModel.getName());
                    if (dashboardUsersResult.success) {
                        people.addAll(dashboardUsersResult.users);
                        if (result.subjectUser.equals(loginControl.getUsername())) {
                            editModeControl.editModeProperty().set(false);
                            notificationProvider.addNotification(new NotificationProvider.Notification("Success", "You " + result.subjectUser + " are now " + roleComboBox.getValue()));
                        } else {
                            notificationProvider.addNotification(new NotificationProvider.Notification("Success", "User " + result.subjectUser + " is now " + roleComboBox.getValue()));
                        }
                    } else {
                        if (dashboardUsersResult.message.equals("Token expired")) {
                            loginControl.loginSuccessProperty().set(false);
                        }
                        notificationProvider.addNotification(new NotificationProvider.Notification("Error", dashboardUsersResult.message, Color.RED));
                    }
                } else {
                    if (result.message.equals("Token expired")) {
                        loginControl.loginSuccessProperty().set(false);
                    }
                    notificationProvider.addNotification(new NotificationProvider.Notification("Error", result.message, Color.RED));
                }
            }
        });

        removeUserButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String message;
                if (editModeControl.userToEditProperty().get().getUsername().equals(loginControl.getUsername())) {
                    message = "Are you sure you want to leave the dashboard " + dashboardModel.getName() + "?";
                } else {
                    message = "Are you sure you want to remove user " + editModeControl.userToEditProperty().get().getUsername() + " from the dashboard?";
                }
                dialogProvider.startDialog(new DialogProvider.Dialog("Remove user", message, DialogProvider.Dialog.Type.CONFIRMORDENY));

                ChangeListener<DialogProvider.DialogResult> listener = new ChangeListener<DialogProvider.DialogResult>() {
                    @Override
                    public void changed(ObservableValue<? extends DialogProvider.DialogResult> observable, DialogProvider.DialogResult oldValue, DialogProvider.DialogResult newValue) {
                        if (newValue != null) {
                            if (newValue.getConfirmation()) {
                                ServerConnector.RemoveUserOfDashboardResult result = serverConnector.removeUserOfDashboard(dashboardModel.getOwnerUsername(), loginControl.getToken(), dashboardModel.getName(), editModeControl.userToEditProperty().get().getUsername(), initialRole.get());
                                if (result.success) {
                                    people.clear();
                                    if (result.subjectUser.equals(loginControl.getUsername())) {
                                        userViewControl.goToDashboardList();
                                        notificationProvider.addNotification(new NotificationProvider.Notification("Success", "You have left the dashboard " + dashboardModel.getName()));
                                    } else {
                                        ServerConnector.GetDashboardUsersResult dashboardUsersResult = serverConnector.getDashboardUsers(dashboardModel.getOwnerUsername(), loginControl.getToken(), dashboardModel.getName());
                                        if (dashboardUsersResult.success) {
                                            people.addAll(dashboardUsersResult.users);
                                            
                                            notificationProvider.addNotification(new NotificationProvider.Notification("Success", "User " + editModeControl.userToEditProperty().get().getUsername() + " was removed"));
                                        } else {
                                            if (result.message.equals("Token expired")) {
                                                loginControl.loginSuccessProperty().set(false);
                                            }
                                            notificationProvider.addNotification(new NotificationProvider.Notification("Error", "Failed to get users of dashboard"));
                                        }
                                    }
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
}
