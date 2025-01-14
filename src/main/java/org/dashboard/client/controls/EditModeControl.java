package org.dashboard.client.controls;

import org.dashboard.common.models.UserOfDashboard;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

public class EditModeControl {
    private final BooleanProperty editMode = new SimpleBooleanProperty(false);
    private final SimpleObjectProperty<Submode> submode = new SimpleObjectProperty<>(Submode.ELEMENTS);
    // private SimpleStringProperty userToEdit = new SimpleStringProperty("");
    private SimpleObjectProperty<UserOfDashboard> userToEdit = new SimpleObjectProperty<UserOfDashboard>(null);

    public enum Submode {
        ELEMENTS,
        USER_NEW,
        USER_EDIT,
        SETTINGS
    }

    public EditModeControl() {
        editMode.addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                setSubmode(Submode.ELEMENTS);
            }
        });
    }

    public boolean isEditMode() {
        return editMode.get();
    }

    public void setEditMode(boolean editMode) {
        this.editMode.set(editMode);
    }

    public void toggleEditMode() {
        this.editMode.set(!editMode.get());
    }

    public BooleanProperty editModeProperty() {
        return editMode;
    }

    public Submode getSubmode() {
        return submode.get();
    }

    private void setSubmode(Submode submode) {
        this.submode.set(submode);
    }

    public void goToElements() {
        setSubmode(Submode.ELEMENTS);
        this.userToEdit.set(null);
    }

    public void goToUserNew() {
        setSubmode(Submode.USER_NEW);
        this.userToEdit.set(null);
    }

    public void goToUserEdit(UserOfDashboard userToEdit) {
        this.userToEdit.set(userToEdit);
        setSubmode(Submode.USER_EDIT);
    }

    public void goToSettings() {
        setSubmode(Submode.SETTINGS);
        this.userToEdit.set(null);
    }

    public SimpleObjectProperty<Submode> submodeProperty() {
        return submode;
    }

    public UserOfDashboard getUserToEdit() {
        return (UserOfDashboard)userToEdit.get();
    }

    public SimpleObjectProperty<UserOfDashboard> userToEditProperty() {
        return userToEdit;
    }
}