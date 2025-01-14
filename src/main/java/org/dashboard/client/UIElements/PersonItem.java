package org.dashboard.client.UIElements;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import org.dashboard.common.models.UserOfDashboard;
import org.dashboard.common.Role;

import org.dashboard.client.Icons;
import org.dashboard.client.controls.EditModeControl;

public class PersonItem extends HBox {
    public PersonItem(UserOfDashboard person, UserOfDashboard userOfDashboard, EditModeControl editModeControl) {
        super();
        
        this.setSpacing(5);
        this.setAlignment(Pos.CENTER_LEFT);

        Label personLabel = new Label(person.getUsername());
        personLabel.setId("person-item-name");

        Label roleLabel = new Label();
        roleLabel.setId("person-item-role");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        SVGPath editIcon = Icons.getIcon(Icons.Icon.SETTINGS, 10, 10);
        editIcon.setFill(Color.WHITE);
        
        Button editButton = new Button();
        editButton.setGraphic(editIcon);
        editButton.setPadding(new Insets(0));

        if (userOfDashboard.getRole() == Role.OWNER || userOfDashboard.getRole() == Role.ADMIN) {
            editButton.setVisible(true);
        }

        if (person.getRole() == Role.OWNER) {
            editButton.setVisible(false);
        }

        if (person.getRole() == Role.OWNER) {
            roleLabel.setText("Owner");
        } else if (person.getRole() == Role.ADMIN) {
            roleLabel.setText("Admin");
        } else if (person.getRole() == Role.EDITOR) {
            roleLabel.setText("Editor");
        } else {
            roleLabel.setText("");
        }

        this.getChildren().addAll(personLabel, roleLabel, spacer, editButton);

        editButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                editModeControl.goToUserEdit(person);
            }
        });
    }
}
