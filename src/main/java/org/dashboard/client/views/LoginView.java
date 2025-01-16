package org.dashboard.client.views;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import org.dashboard.common.Passwords;
import org.dashboard.client.ServerConnector;
import org.dashboard.client.controls.LoginControl;
import org.dashboard.client.providers.NotificationProvider;

public class LoginView {
    private LoginControl loginControl;
    private NotificationProvider notificationProvider;
    private ServerConnector serverConnector;
    
    public LoginView(LoginControl loginControl, NotificationProvider notificationProvider, ServerConnector serverConnector) {
        this.loginControl = loginControl;
        this.notificationProvider = notificationProvider;
        this.serverConnector = serverConnector;
    }

    public Region getRegion() {
        VBox root = new VBox();

        String largestFontStyle = "-fx-font-size: 30;";
        String largeFontStyle = "-fx-font-size: 18;";
        String mediumFontStyle = "-fx-font-size: 14;";

        Label welcomeLabel = new Label("Welcome to the Dashboard App!");
        Label loginLabel = new Label("Please log in to continue:");
        Region spacer = new Region();
        
        welcomeLabel.setStyle(largestFontStyle);
        loginLabel.setStyle(largeFontStyle);;

        spacer.setPrefHeight(20);
        
        GridPane loginForm = new GridPane();
        Label usernameLabel = new Label("Username:");
        Label passwordLabel = new Label("Password:");
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();

        Label loginMessageLabel = new Label("");
        
        HBox loginButtonBox = new HBox();

        Button loginButton = new Button("Login");
        loginButton.setId("primary");
        Button registerButton = new Button("Register");

        usernameLabel.setStyle(mediumFontStyle);
        passwordLabel.setStyle(mediumFontStyle);

        loginForm.add(usernameLabel, 0, 0);
        loginForm.add(usernameField, 1, 0);
        loginForm.add(passwordLabel, 0, 1);
        loginForm.add(passwordField, 1, 1);
        loginForm.add(loginMessageLabel, 0, 2, 2, 1);
        loginForm.add(loginButtonBox, 0, 3, 2, 1);
        
        loginForm.setHgap(7);
        loginForm.setVgap(7);
        loginForm.setAlignment(Pos.CENTER);
        
        loginButtonBox.getChildren().addAll(registerButton, loginButton);
        loginButtonBox.setSpacing(10);
        loginButtonBox.setAlignment(Pos.CENTER);

        GridPane registerForm = new GridPane();
        Label newUsernameLabel = new Label("New Username:");
        Label newPasswordLabel = new Label("New Password:");
        Label repeatPasswordLabel = new Label("Repeat Password:");
        TextField newUsernameField = new TextField();
        PasswordField newPasswordField = new PasswordField();
        PasswordField repeatPasswordField = new PasswordField();

        Label registerMessageLabel = new Label("");

        HBox registerButtonBox = new HBox();

        Button confirmButton = new Button("Confirm");
        confirmButton.setId("success");
        Button cancelButton = new Button("Cancel");
        cancelButton.setId("danger");

        newUsernameLabel.setStyle(mediumFontStyle);
        newPasswordLabel.setStyle(mediumFontStyle);
        repeatPasswordLabel.setStyle(mediumFontStyle);

        registerForm.add(newUsernameLabel, 0, 0);
        registerForm.add(newUsernameField, 1, 0);
        registerForm.add(newPasswordLabel, 0, 1);
        registerForm.add(newPasswordField, 1, 1);
        registerForm.add(repeatPasswordLabel, 0, 2);
        registerForm.add(repeatPasswordField, 1, 2);
        registerForm.add(registerMessageLabel, 0, 3, 2, 1);
        registerForm.add(registerButtonBox, 0, 4, 2, 1);

        registerForm.setHgap(7);
        registerForm.setVgap(7);
        registerForm.setAlignment(Pos.CENTER);

        registerButtonBox.getChildren().addAll(cancelButton, confirmButton);
        registerButtonBox.setSpacing(10);
        registerButtonBox.setAlignment(Pos.CENTER);

        class utils {
            public void clearAllFields() {
                newUsernameField.clear();
                newPasswordField.clear();
                repeatPasswordField.clear();

                usernameField.clear();
                passwordField.clear();
            }

            public void showRegister() {
                root.getChildren().remove(loginForm);
                root.getChildren().add(registerForm);

                loginLabel.setText("Register a new account:");
            }

            public void showLogin() {
                root.getChildren().remove(registerForm);
                root.getChildren().add(loginForm);

                loginLabel.setText("Please login to continue:");
            }
        }

        utils u = new utils();

        registerButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                u.clearAllFields();
                u.showRegister();
            }
        });

        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                u.clearAllFields();
                u.showLogin();
            }
        });

        confirmButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String newUsername = newUsernameField.getText();
                String newPassword = newPasswordField.getText();
                String repeatPassword = repeatPasswordField.getText();

                ServerConnector.UserLookupResult result = serverConnector.userExists(newUsername);

                if (!result.success) {
                    notificationProvider.addNotification(new NotificationProvider.Notification("Error", "An error occured while trying to register. Please try again.", Color.RED));
                    return;
                }

                if (newUsername.isEmpty()) {
                    registerMessageLabel.setText("Please enter a username.");
                    registerMessageLabel.setTextFill(Color.RED);
                    return;
                }

                if (newUsername.length() < 4) {
                    registerMessageLabel.setText("Username must be at least 4 characters long.");
                    registerMessageLabel.setTextFill(Color.RED);
                    return;
                }

                if (newUsername.length() > 200) {
                    registerMessageLabel.setText("Username is too long.");
                    registerMessageLabel.setTextFill(Color.RED);
                    return;
                }

                if (newUsername.contains(" ")) {
                    registerMessageLabel.setText("Username cannot contain spaces.");
                    registerMessageLabel.setTextFill(Color.RED);
                    return;
                }

                if (result.success && result.exists) {
                    registerMessageLabel.setText("Username already exists.");
                    registerMessageLabel.setTextFill(Color.RED);
                    return;
                }
                
                if (newPassword.isEmpty() || repeatPassword.isEmpty()) {
                    registerMessageLabel.setText("Please enter a password and repeat it.");
                    registerMessageLabel.setTextFill(Color.RED);
                    return;
                }

                if (newPassword.length() < 8) {
                    registerMessageLabel.setText("Password must be at least 8 characters long.");
                    registerMessageLabel.setTextFill(Color.RED);
                    return;
                }

                if (newPassword.length() > 200) {
                    registerMessageLabel.setText("Password is too long.");
                    registerMessageLabel.setTextFill(Color.RED);
                    return;
                }

                if (newPassword.contains(" ")) {
                    registerMessageLabel.setText("Password cannot contain spaces.");
                    registerMessageLabel.setTextFill(Color.RED);
                    return;
                }

                if (newPassword.contains(newUsername)) {
                    registerMessageLabel.setText("Password cannot contain the username.");
                    registerMessageLabel.setTextFill(Color.RED);
                    return;
                }

                Passwords.Password password = Passwords.encodeNew(newPassword);
                
                if (Passwords.verify(repeatPassword, password)) {
                    registerMessageLabel.setText("");
                    registerMessageLabel.setTextFill(Color.BLACK);
                    
                    ServerConnector.UserCreateResult createResult = serverConnector.createUser(newUsername, password);

                    if (createResult.success) {
                        LoginView.this.notificationProvider.addNotification(new NotificationProvider.Notification("Success!", "New account created successfully. Please login with your new account to continue."));
                        u.clearAllFields();
                        u.showLogin();
                    } else {
                        LoginView.this.notificationProvider.addNotification(new NotificationProvider.Notification("Error", createResult.message, Color.RED));
                        return;
                    }
                } else {
                    registerMessageLabel.setText("Passwords do not match.");
                    registerMessageLabel.setTextFill(Color.RED);
                    return;
                }
            }
        });
        
        loginButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String username = usernameField.getText();
                String password = passwordField.getText();

                if (username.isEmpty() || password.isEmpty()) {
                    loginMessageLabel.setText("Please enter a username and password.");
                    loginMessageLabel.setTextFill(Color.RED);
                    return;
                }

                ServerConnector.LoginRequestResult result = serverConnector.loginRequest(username, password);

                // if (result.success) {
                //     System.out.println(result.message);
                //     System.out.println(result.token);
                // } else {
                //     System.out.println(result.message);
                // }

                if (result.success) {
                    u.clearAllFields();
                    LoginView.this.loginControl.setUsername(username);
                    LoginView.this.loginControl.setToken(result.token);
                    LoginView.this.loginControl.setLoginSuccess(true);

                    loginMessageLabel.setText("");
                    loginMessageLabel.setTextFill(Color.BLACK);
                } else {
                    if (result.message.equals("Invalid request message")) {
                        notificationProvider.addNotification(new NotificationProvider.Notification("Error", "An error occured while trying to login. Please try again.", Color.RED));
                        return;
                    }
                    LoginView.this.loginControl.setLoginSuccess(false);
                    loginMessageLabel.setText(result.message);
                    loginMessageLabel.setTextFill(Color.RED);
                    return;
                }
            }
        });
        
        root.setSpacing(10);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);

        root.getChildren().addAll(welcomeLabel, spacer, loginLabel, loginForm);

        return root;
    }
}
