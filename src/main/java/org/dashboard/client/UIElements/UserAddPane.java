package org.dashboard.client.UIElements;

import java.util.ArrayList;

import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

import org.dashboard.common.models.DashboardModel;
import org.dashboard.common.models.UserOfDashboard;

import org.dashboard.client.controls.LoginControl;
import org.dashboard.client.providers.NotificationProvider;
import org.dashboard.client.ServerConnector;
import org.dashboard.client.ServerConnector.AddUserOfDashboardResult;

public class UserAddPane extends VBox {
    public UserAddPane(LoginControl loginControl, ServerConnector serverConnector, NotificationProvider notificationProvider, DashboardModel dashboardModel, ObservableList<UserOfDashboard> people) {
        super();

        ObservableList<String> roles = FXCollections.observableArrayList("admin", "editor", "viewer");
        ArrayList<String> users = new ArrayList<>();
        for (UserOfDashboard user : people) {
            users.add(user.getUsername());
        }

        this.setSpacing(5);
        this.setPadding(new Insets(20));
        this.setAlignment(Pos.CENTER);

        Label headerLabel = new Label("Add User");
        headerLabel.setId("username-label");
        headerLabel.setMinWidth(300);
        headerLabel.setMaxWidth(300);
        headerLabel.setAlignment(Pos.CENTER_LEFT);

        HBox searchBox = new HBox();
        searchBox.setAlignment(Pos.CENTER);
        searchBox.setSpacing(5);
        searchBox.setMinWidth(300);
        searchBox.setMaxWidth(300);
        
        TextField searchField = new TextField();
        Button searchButton = new Button("Find");
        searchButton.setId("primary");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        searchBox.getChildren().addAll(searchField, searchButton);

        ScrollPane searchResultPane = new ScrollPane();
        searchResultPane.setFitToWidth(true);
        searchResultPane.setMaxWidth(300);
        searchResultPane.setMinWidth(300);
        searchResultPane.setPrefHeight(200);
        searchResultPane.setBorder(new Border(new BorderStroke(Color.rgb(211, 212, 213), BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(1))));

        VBox searchResultInnerPane = new VBox();
        searchResultInnerPane.setSpacing(3);
        searchResultInnerPane.setPadding(new Insets(5));

        searchResultPane.setContent(searchResultInnerPane);

        Label roleLabel = new Label("Role:");
        roleLabel.setMinWidth(300);
        roleLabel.setMaxWidth(300);
        roleLabel.setAlignment(Pos.CENTER_LEFT);

        ToggleGroup userToggle = new ToggleGroup();

        ComboBox<String> roleComboBox = new ComboBox<>(roles);
        roleComboBox.setStyle("-fx-min-width: 300px; -fx-max-width: 300px;");

        Region spacer = new Region();
        spacer.setPrefHeight(10);

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setSpacing(5);
        buttonBox.setMinWidth(300);
        buttonBox.setMaxWidth(300);

        Button confirmButton = new Button("Add");
        confirmButton.setId("success");

        buttonBox.getChildren().add(confirmButton);

        this.getChildren().addAll(headerLabel, searchBox, searchResultPane, roleLabel, roleComboBox, spacer, buttonBox);

        searchButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String searchTerm = searchField.getText().trim();
                if (!searchTerm.isEmpty()) {
                    ServerConnector.SearchForUserResult result = serverConnector.searchForUser(searchTerm, loginControl.getToken());
                    if (result.success) {
                        searchResultInnerPane.getChildren().clear();
                        if (result.users != null) {
                            for (String user : result.users) {
                                if (users.contains(user)) {
                                    continue;
                                }
                                RadioButton userButton = new RadioButton(user);
                                userButton.setToggleGroup(userToggle);
                                searchResultInnerPane.getChildren().add(userButton);
                            }
                        } else {
                            Label noResultsLabel = new Label("No matching users found");
                            searchResultInnerPane.getChildren().add(noResultsLabel);
                        }
                    } else {
                        if (result.message.equals("Token expired")) {
                            loginControl.loginSuccessProperty().set(false);
                        }
                        notificationProvider.addNotification(new NotificationProvider.Notification("Error", "Failed to search for user"));
                    }
                }
            }
        });

        confirmButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                RadioButton selectedUserButton = (RadioButton) userToggle.getSelectedToggle();
                String selectedRole = roleComboBox.getValue();
                if (selectedUserButton != null && selectedRole != null) {
                    AddUserOfDashboardResult result = serverConnector.addUserOfDashboard(dashboardModel.getOwnerUsername(), loginControl.getToken(), dashboardModel.getName(), selectedUserButton.getText(), selectedRole);
                    if (result.success) {
                        people.clear();

                        ServerConnector.GetDashboardUsersResult dashboardUsersResult = serverConnector.getDashboardUsers(dashboardModel.getOwnerUsername(), loginControl.getToken(), dashboardModel.getName());
                        if (dashboardUsersResult.success) {
                            people.addAll(dashboardUsersResult.users);

                            notificationProvider.addNotification(new NotificationProvider.Notification("Success", "User " + selectedUserButton.getText() + " added as " + selectedRole));
                        } else {
                            if (result.message.equals("Token expired")) {
                                loginControl.loginSuccessProperty().set(false);
                            }
                            notificationProvider.addNotification(new NotificationProvider.Notification("Error", "Failed to get users of dashboard"));
                        }
                    } else {
                        notificationProvider.addNotification(new NotificationProvider.Notification("Error", "Failed to add user to dashboard"));
                    }
                } else {
                    notificationProvider.addNotification(new NotificationProvider.Notification("Error", "Please select a user and a role", Color.RED));
                }
            }
        });
    }
}
