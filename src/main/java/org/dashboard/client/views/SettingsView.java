package org.dashboard.client.views;

import org.dashboard.client.Icons;
import org.dashboard.client.ServerConnector;
import org.dashboard.client.Icons.Icon;
import org.dashboard.client.UIElements.DeleteAccountForm;
import org.dashboard.client.controls.LoginControl;
import org.dashboard.client.controls.UserViewControl;
import org.dashboard.client.providers.DialogProvider;
import org.dashboard.client.providers.NotificationProvider;

import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.TextAlignment;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class SettingsView {
    private LoginControl loginControl;
    private UserViewControl userViewControl;
    private NotificationProvider notificationProvider;
    private DialogProvider dialogProvider;
    private ServerConnector serverConnector;

    public SettingsView(LoginControl loginControl, UserViewControl userViewControl, NotificationProvider notificationProvider, DialogProvider dialogProvider, ServerConnector serverConnector) {
        this.loginControl = loginControl;
        this.userViewControl = userViewControl;
        this.notificationProvider = notificationProvider;
        this.dialogProvider = dialogProvider;
        this.serverConnector = serverConnector;
    }

    public Region getRegion() {
        BorderPane root = new BorderPane();

        SVGPath backIcon = Icons.getIcon(Icon.BACK, 15, 15);
        backIcon.setFill(Color.WHITE);

        VBox sideBar = new VBox();
        sideBar.setAlignment(Pos.TOP_CENTER);
        sideBar.setSpacing(10);
        sideBar.setPadding(new Insets(10, 5, 10, 5));
        sideBar.prefWidthProperty().bind(root.widthProperty().divide(4));
        sideBar.setId("dashboard-menu-left");
        sideBar.setMaxWidth(200);

        HBox backBox = new HBox();
        backBox.setAlignment(Pos.CENTER_LEFT);

        Button backButton = new Button("Back");
        backButton.setGraphic(backIcon);

        backBox.getChildren().add(backButton);

        Label usernameLabel = new Label();
        usernameLabel.textProperty().bind(loginControl.usernameProperty());
        usernameLabel.setId("username-label");
        usernameLabel.setPadding(new Insets(0, 5, 0, 5));

        VBox alignToBottom = new VBox();
        alignToBottom.prefHeightProperty().bind(root.heightProperty());
        alignToBottom.setAlignment(Pos.BOTTOM_CENTER);

        Button deleteAccountButton = new Button("Delete Account");
        deleteAccountButton.setId("danger");

        alignToBottom.getChildren().add(deleteAccountButton);

        sideBar.getChildren().addAll(backBox, usernameLabel, alignToBottom);

        DeleteAccountForm deleteAccountPanel = new DeleteAccountForm(loginControl, notificationProvider, serverConnector);
        

        backButton.setOnAction(e -> userViewControl.goToDashboardList());

        deleteAccountButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                root.setCenter(deleteAccountPanel);
            }
        });

        root.setLeft(sideBar);

        return root;
    }
}
