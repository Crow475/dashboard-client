package org.dashboard.client.UIElements;

import javafx.event.EventHandler;

import org.dashboard.client.ServerConnector;
import org.dashboard.client.controls.LoginControl;
import org.dashboard.client.providers.NotificationProvider;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public class DeleteAccountForm extends VBox {
    public DeleteAccountForm(LoginControl loginControl, NotificationProvider notificationProvider, ServerConnector serverConnector) {
        super();

        this.setAlignment(Pos.CENTER);
        this.setSpacing(7);
        this.setPadding(new Insets(10));

        Label deleteAccountLabel = new Label("Are you sure you want to delete your account?\nThis action cannot be undone.");
        deleteAccountLabel.setId("warning-label");
        deleteAccountLabel.setTextAlignment(TextAlignment.CENTER);

        Label deleteAccountExplainerLabel = new Label("Enter your account information to confirm.");

        Label accountNameLabel = new Label("Account Name: ");
        TextField accountNameField = new TextField();

        Label passwordLabel = new Label("Password: ");
        PasswordField passwordField = new PasswordField();

        VBox formBox = new VBox();
        formBox.setSpacing(3);
        formBox.setMinWidth(300);
        formBox.setMaxWidth(300);

        formBox.getChildren().addAll(accountNameLabel, accountNameField, passwordLabel, passwordField);

        Button confirmDeleteButton = new Button("Confirm");
        confirmDeleteButton.setId("danger");
        
        this.getChildren().addAll(deleteAccountLabel, deleteAccountExplainerLabel, formBox, confirmDeleteButton);

        confirmDeleteButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!accountNameField.getText().isEmpty() && !passwordField.getText().isEmpty()) {
                    ServerConnector.DeleteAccountResult result = serverConnector.deleteAccount(loginControl.getUsername(), loginControl.getToken(), passwordField.getText());
                    if (result.success) {
                        loginControl.loginSuccessProperty().set(false);
                        notificationProvider.addNotification(new NotificationProvider.Notification("Success", "Account was deleted successfully"));
                    } else {
                        if (result.message.equals("Token expired")) {
                            loginControl.loginSuccessProperty().set(false);
                        }
                        notificationProvider.addNotification(new NotificationProvider.Notification("Error", result.message));
                    }
                }
            }
        });
    }
}
