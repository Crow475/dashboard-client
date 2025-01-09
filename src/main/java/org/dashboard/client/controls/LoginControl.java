package org.dashboard.client.controls;

import javafx.beans.property.BooleanProperty;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class LoginControl {
    private final SimpleStringProperty username = new SimpleStringProperty("");
    private final SimpleBooleanProperty loginSuccess = new SimpleBooleanProperty(false);
    private final SimpleStringProperty token = new SimpleStringProperty("");

    public LoginControl() {
        loginSuccess.addListener((observable, oldValue, successfull) -> {
            if (!successfull) {
                setUsername("");
                setToken("");
            }
        });
    }

    public String getUsername() {
        return this.username.get();
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public boolean isLoginSuccess() {
        return loginSuccess.get();
    }

    public void setLoginSuccess(boolean loginSuccess) {
        this.loginSuccess.set(loginSuccess);
    }

    public String getToken() {
        return token.get();
    }

    public void setToken(String token) {
        this.token.set(token);
    }

    public BooleanProperty loginSuccessProperty() {
        return loginSuccess;
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public StringProperty tokenProperty() {
        return token;
    }
}