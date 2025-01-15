package org.dashboard.client.dashboardElements;

import java.util.HashMap;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.dashboard.client.Icons;
import org.dashboard.client.Icons.Icon;
import org.dashboard.client.controls.EditModeControl;
import org.dashboard.client.controls.LoginControl;

@JsonIgnoreProperties({"name"})
public class AnnouncementElement extends AbstractElement {
    private String header;
    private boolean bordered;
    private String announcementText;
    private int[] textColorValues = {0, 0, 0};
    private double textOpacity = 1.0;
    
    public AnnouncementElement(HashMap<String, String> announcementProperties, EditModeControl editModeControl) {
        super(Type.ANNOUNCEMENT, announcementProperties, editModeControl);

        if (announcementProperties.containsKey("header")) {
            this.header = announcementProperties.get("header");
        } else {
            this.header = "";
        }
        this.updateProperty("header", this.header);

        this.bordered = announcementProperties.containsKey("bordered") && announcementProperties.get("bordered").equals("true");
        this.updateProperty("bordered", Boolean.toString(this.bordered));

        if (announcementProperties.containsKey("announcementText")) {
            this.announcementText = announcementProperties.get("announcementText");
        } else {
            this.announcementText = "";
        }
        this.updateProperty("announcementText", this.announcementText);

        if (announcementProperties.containsKey("textColor")) {
            String[] textColorValuesStr = announcementProperties.get("textColor").split(" ");
            this.textColorValues = new int[textColorValuesStr.length];
            for (int i = 0; i < textColorValuesStr.length - 1; i++) {
                this.textColorValues[i] = Integer.parseInt(textColorValuesStr[i]);
            }
            this.textOpacity = Double.parseDouble(textColorValuesStr[3]);
        }
        this.updateProperty("textColor", textColorValues[0] + " " + textColorValues[1] + " " + textColorValues[2] + " " + textOpacity);
    }

    @JsonCreator
    public AnnouncementElement(@JsonProperty("properties") HashMap<String, String> properties) {
        super(Type.ANNOUNCEMENT, properties, null);
    }

    public void setHeader(String header) {
        this.header = header;
        this.updateProperty("header", this.header);
    }

    public void setBordered(boolean bordered) {
        this.bordered = bordered;
        this.updateProperty("bordered", Boolean.toString(this.bordered));
    }

    public void setAnnouncementText(String announcementText) {
        this.announcementText = announcementText;
        this.updateProperty("announcementText", this.announcementText);
    }

    public void setTextColor(int[] textColorValues) {
        this.textColorValues = textColorValues;
        this.updateProperty("textColor", textColorValues[0] + " " + textColorValues[1] + " " + textColorValues[2] + " " + textOpacity);
    }

    @Override
    void updateAllProperties() {
        this.setHeader(this.getProperties().get("header"));
        this.setBordered(this.getProperties().get("bordered").equals("true"));
        this.setAnnouncementText(this.getProperties().get("announcementText"));

        if (this.getProperties().containsKey("textColor")) {
            String[] textColorValuesStr = this.getProperties().get("textColor").split(" ");
            this.textColorValues = new int[textColorValuesStr.length];
            for (int i = 0; i < textColorValuesStr.length - 1; i++) {
                this.textColorValues[i] = Integer.parseInt(textColorValuesStr[i]);
            }
            this.textOpacity = Double.parseDouble(textColorValuesStr[3]);
        }

        this.updateProperty("textColor", textColorValues[0] + " " + textColorValues[1] + " " + textColorValues[2] + " " + textOpacity);
    }

    @Override
    Region getBaseNode(EditModeControl editModeControl, LoginControl loginControl) {
        BorderPane rootPane = new BorderPane();

        SVGPath editButtonIcon = Icons.getIcon(Icon.SETTINGS, 13, 13);
        editButtonIcon.setFill(Color.WHITE);
        
        SVGPath announcementIcon = Icons.getIcon(Icon.WARNING, 40, 40);
        announcementIcon.setFill(Color.rgb(textColorValues[0], textColorValues[1], textColorValues[2], textOpacity));

        SimpleBooleanProperty editorAccessMode = new SimpleBooleanProperty(false);
        
        Label headerLabel = new Label(this.header);
        headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        headerLabel.setPadding(new Insets(10));

        VBox centerPane = new VBox();
        centerPane.setPadding(new Insets(10));
        centerPane.setAlignment(Pos.CENTER);
        centerPane.setSpacing(10);


        Label announcementLabel = new Label(this.announcementText);
        announcementLabel.setWrapText(true);
        announcementLabel.setStyle("-fx-font-size: 18;");
        announcementLabel.setTextFill(Color.rgb(textColorValues[0], textColorValues[1], textColorValues[2], textOpacity));

        if (!this.announcementText.equals("")) {
            centerPane.getChildren().addAll(announcementIcon, announcementLabel);
        }

        Button editButton = new Button("");
        editButton.setPadding(new Insets(2));
        editButton.setGraphic(editButtonIcon);
        
        editButton.setOnAction(event -> {
            editorAccessMode.set(!editorAccessMode.get());
        });
        
        if (editModeControl != null) {
            System.out.println("editModeControl is not null");
            if (editModeControl.isAtLeastEditor()) {
                rootPane.setBottom(editButton);
            } else {
                rootPane.setBottom(null);
            }
        }
        
        if (this.bordered) {
            rootPane.setStyle("-fx-border-color: rgb(211, 212, 213); -fx-border-width: 1; -fx-border-radius: 5;");
        } else {
            rootPane.setStyle("-fx-border-color: transparent; -fx-border-width: 1; -fx-border-radius: 5;");
        }

        GridPane editorAccessPane = new GridPane();
        editorAccessPane.setHgap(5);
        editorAccessPane.setVgap(5);
        editorAccessPane.setPadding(new Insets(5));
        Label editorAccessLabel = new Label("Edit announcement");

        Label announcementTextLabel = new Label("Text");
        TextField announcementTextField = new TextField(this.announcementText);
        announcementTextField.setPromptText("Announcement Text");
        
        Label textColorLabel = new Label("Color");
        ColorPicker textColorPicker = new ColorPicker(Color.rgb(textColorValues[0], textColorValues[1], textColorValues[2], textOpacity));

        Button saveButton = new Button("Save");
        
        editorAccessPane.add(editorAccessLabel, 0, 0, 2, 1);
        editorAccessPane.add(announcementTextLabel, 0, 1, 1, 1);
        editorAccessPane.add(announcementTextField, 1, 1, 1, 1);
        editorAccessPane.add(textColorLabel, 0, 2, 1, 1);
        editorAccessPane.add(textColorPicker, 1, 2, 1, 1);
        editorAccessPane.add(saveButton, 0, 3, 1, 1);
        
        saveButton.setOnAction(event -> {
            this.setAnnouncementText(announcementTextField.getText());
            editModeControl.toggleEditMode();
            editModeControl.toggleEditMode();
        });

        textColorPicker.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Color textColor = textColorPicker.getValue();
                int[] textColorValues = {(int) (textColor.getRed() * 255), (int) (textColor.getGreen() * 255), (int) (textColor.getBlue() * 255)};
                textOpacity = textColor.getOpacity();
                setTextColor(textColorValues);
            }
        });

        editorAccessMode.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                rootPane.setCenter(editorAccessPane);
            } else {
                rootPane.setCenter(centerPane);
            }
        });

        rootPane.setCenter(centerPane);
        rootPane.setTop(headerLabel);
        return rootPane;
    }

    @Override
    Region getSettingsNode(EditModeControl editModeControl, LoginControl loginControl) {
        GridPane settingsPane = new GridPane();

        Label nameLabel = new Label(this.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        Label headerLabel = new Label("Header");

        TextField headerField = new TextField();
        headerField.setText(this.header);

        headerField.textProperty().addListener((observable, oldValue, newValue) -> {
            this.setHeader(newValue);
        });

        Label borderedLabel = new Label("Borders");
        CheckBox borderedCheckBox = new CheckBox();
        borderedCheckBox.setSelected(this.bordered);

        borderedCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            this.setBordered(newValue);
        });

        settingsPane.setHgap(5);
        settingsPane.setVgap(5);
        settingsPane.setPadding(new Insets(5));

        settingsPane.add(nameLabel, 0, 0, 2, 1);
        settingsPane.add(headerLabel, 0, 1, 1, 1);
        settingsPane.add(headerField, 1, 1, 1, 1);
        settingsPane.add(borderedLabel, 0, 2, 1, 1);
        settingsPane.add(borderedCheckBox, 1, 2, 1, 1);

        return settingsPane;
    }
}
