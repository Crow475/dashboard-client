package org.dashboard.client.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class EditModeControl {
    private final BooleanProperty editMode = new SimpleBooleanProperty(false);

    public EditModeControl() {}

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
}