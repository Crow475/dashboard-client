package org.dashboard.client.dashboardElements;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.dashboard.client.Icons;
import org.dashboard.client.Icons.Icon;
import org.dashboard.client.controls.EditModeControl;
import org.dashboard.client.controls.LoginControl;

@JsonIgnoreProperties({"name"})
public class NotesElement extends AbstractElement {
    private String header;
    private boolean bordered;
    private String publicText;
    private boolean publicAccess;

    public NotesElement(HashMap<String, String> notesProperties, EditModeControl editModeControl) {
        super(Type.NOTES, notesProperties, editModeControl);

        if (notesProperties.containsKey("header")) {
            this.header = notesProperties.get("header");
        } else {
            this.header = "";
        }
        this.updateProperty("header", this.header);

        this.bordered = notesProperties.containsKey("bordered") && notesProperties.get("bordered").equals("true");
        this.updateProperty("bordered", Boolean.toString(this.bordered));

        if(notesProperties.containsKey("publicText")) {
            this.publicText = notesProperties.get("publicText");
        } else {
            this.publicText = "";
        }
        this.updateProperty("publicText", this.publicText);

        this.publicAccess = notesProperties.containsKey("publicAccess") && notesProperties.get("publicAccess").equals("true");
        this.updateProperty("publicAccess", Boolean.toString(this.publicAccess));
    }

    @JsonCreator
    public NotesElement(@JsonProperty("properties") HashMap<String, String> properties) {
        super(Type.NOTES, properties, null);
    }

    public void setHeader(String header) {
        this.header = header;
        this.updateProperty("header", this.header);
    }

    public void setBordered(boolean bordered) {
        this.bordered = bordered;
        this.updateProperty("bordered", Boolean.toString(this.bordered));
    }

    public void setPublicText(String publicText) {
        this.publicText = publicText;
        this.updateProperty("publicText", this.publicText);
    }

    public void setPublicAccess(boolean publicAccess) {
        this.publicAccess = publicAccess;
        this.updateProperty("publicAccess", Boolean.toString(this.publicAccess));
    }

    @Override
    void updateAllProperties() {
        this.setHeader(this.getProperties().get("header"));
        this.setBordered(this.getProperties().get("bordered").equals("true"));
        this.setPublicText(this.getProperties().get("publicText"));
        this.setPublicAccess(this.getProperties().get("publicAccess").equals("true"));
    }

    @Override
    Region getBaseNode(EditModeControl editModeControl, LoginControl loginControl) {
        BorderPane rootPane = new BorderPane();

        SVGPath editButtonIcon = Icons.getIcon(Icon.SETTINGS, 13, 13);
        editButtonIcon.setFill(Color.WHITE);

        SimpleBooleanProperty editorAccessMode = new SimpleBooleanProperty(false);
        SimpleStringProperty notesJSON = new SimpleStringProperty("");

        File notesFile = new File("PrivateNotes-" + loginControl.getUsername() + ".json");
        try {
            if (notesFile.createNewFile()) {
                Files.writeString(notesFile.toPath(), "{}");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        HBox topbar = new HBox();
        topbar.setPadding(new Insets(10));
        topbar.setSpacing(5);
        topbar.setAlignment(Pos.BASELINE_LEFT);

        Label headerLabel = new Label(this.header);
        headerLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label privacyLabel = new Label();
        if (this.publicAccess) {
            privacyLabel.setText("(Public)");
        } else {
            privacyLabel.setText("(Private)");
        }
        privacyLabel.setId("dashboard-item-secondary");

        topbar.getChildren().addAll(headerLabel, privacyLabel);

        TextArea textArea = new TextArea();
        if (this.publicAccess) {
            textArea.setText(this.publicText);
        } else {
            try{
                notesJSON.set(Files.readString(notesFile.toPath()));

                String note = "";
                JsonNode jsonNode = new ObjectMapper().readTree(notesJSON.get());
                if (jsonNode.has(dashboardModel.getOwnerUsername() + ":" + dashboardModel.getName())) {
                    note = jsonNode.get(dashboardModel.getOwnerUsername() + ":" + dashboardModel.getName()).asText();
                }

                textArea.setText(note);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        textArea.setEditable(!this.publicAccess);
        textArea.prefHeightProperty().bind(rootPane.heightProperty());
        textArea.prefWidthProperty().bind(rootPane.widthProperty());

        VBox textAreaContainer = new VBox();
        textAreaContainer.setPadding(new Insets(0));

        textAreaContainer.getChildren().add(textArea);

        rootPane.setCenter(textAreaContainer);

        if (this.bordered) {
            rootPane.setStyle("-fx-border-color: rgb(211, 212, 213); -fx-border-width: 1; -fx-border-radius: 5;");
        } else {
            rootPane.setStyle("-fx-border-color: transparent; -fx-border-width: 1; -fx-border-radius: 5;");
        }

        Button editButton = new Button("");
        editButton.setPadding(new Insets(2));
        editButton.setGraphic(editButtonIcon);

        Button saveFileButton = new Button("Save");

        saveFileButton.setOnAction(event -> {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(notesJSON.get());
                ((ObjectNode) jsonNode).put(dashboardModel.getOwnerUsername() + ":" + dashboardModel.getName(), textArea.getText());

                String result = mapper.writeValueAsString(jsonNode);

                Files.writeString(notesFile.toPath(), result);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        editButton.setOnAction(event -> {
            editorAccessMode.set(!editorAccessMode.get());
        });

        if (editModeControl != null) {
            if (editModeControl.isAtLeastEditor()  && this.publicAccess) {
                rootPane.setBottom(editButton);
            } else {
                rootPane.setBottom(saveFileButton);
            }
        }

        VBox editorPane = new VBox();
        editorPane.setPadding(new Insets(10));
        editorPane.setSpacing(5);

        Label editorLabel = new Label("Public note");
        TextArea editorTextArea = new TextArea();
        Button saveButton = new Button("Save");

        editorTextArea.setText(this.publicText);

        editorPane.getChildren().addAll(editorLabel, editorTextArea, saveButton);

        editorAccessMode.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                rootPane.setCenter(editorPane);
            } else {
                rootPane.setCenter(textAreaContainer);
            }
        });

        saveButton.setOnAction(event -> {
            this.setPublicText(editorTextArea.getText());
            editModeControl.toggleEditMode();
            editModeControl.toggleEditMode();
        });

        rootPane.setTop(topbar);

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

        Label publicLabel = new Label("Public");
        CheckBox publicCheckBox = new CheckBox();
        publicCheckBox.setSelected(this.publicAccess);

        publicCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            this.setPublicAccess(newValue);
        });

        settingsPane.setHgap(5);
        settingsPane.setVgap(5);
        settingsPane.setPadding(new Insets(5));

        settingsPane.add(nameLabel, 0, 0, 2, 1);
        settingsPane.add(headerLabel, 0, 1, 1, 1);
        settingsPane.add(headerField, 1, 1, 1, 1);
        settingsPane.add(borderedLabel, 0, 2, 1, 1);
        settingsPane.add(borderedCheckBox, 1, 2, 1, 1);
        settingsPane.add(publicLabel, 0, 3, 1, 1);
        settingsPane.add(publicCheckBox, 1, 3, 1, 1);

        return settingsPane;
    }
}
