package org.dashboard.client.dashboardElements;

import java.util.ArrayList;
import java.util.HashMap;
import java.awt.Desktop;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.IOException;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import org.dashboard.client.util.LinkWithLabel;
import org.dashboard.client.Icons.Icon;
import org.dashboard.client.controls.EditModeControl;
import org.dashboard.client.controls.LoginControl;
import org.dashboard.client.providers.NotificationProvider;
import org.dashboard.client.Icons;
import org.dashboard.client.util.Checker;

@JsonIgnoreProperties({"name"})
public class LinksElement extends AbstractElement {
    private String header;
    private String links;
    private Boolean bordered;
    
    public LinksElement(HashMap<String, String> linksProperties, EditModeControl editModeControl) {
        super(Type.LINKS, linksProperties, editModeControl);

        if (linksProperties.containsKey("header")) {
            this.header = linksProperties.get("header");
        } else {
            this.header = "";
        }
        this.updateProperty("header", this.header);

        this.bordered = linksProperties.containsKey("bordered") && linksProperties.get("bordered").equals("true");
        this.updateProperty("bordered", Boolean.toString(this.bordered));

        if (linksProperties.containsKey("links")) {
            this.links = linksProperties.get("links");
        } else {
            this.links = "";
        }
        this.updateProperty("links", this.links);
        
    }

    @JsonCreator
    public LinksElement(@JsonProperty("properties") HashMap<String, String> properties) {
        super(Type.LINKS, properties, null);
    }

    public void setHeader(String header) {
        this.header = header;
        this.updateProperty("header", this.header);
    }

    public void setLinks(String links) {
        this.links = links;
        this.updateProperty("links", this.links);
    }

    public void setBordered(Boolean bordered) {
        this.bordered = bordered;
        this.updateProperty("bordered", Boolean.toString(this.bordered));
    }

    @Override
    void updateAllProperties() {
        this.setHeader(this.getProperties().get("header"));
        this.setLinks(this.getProperties().get("links"));
        this.setBordered(this.getProperties().get("bordered").equals("true"));            
    }

    @Override
    Region getBaseNode(EditModeControl editModeControl, LoginControl loginControl) {
        BorderPane rootPane = new BorderPane();

        Label headerLabel = new Label(this.header);
        headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        ScrollPane scrollCenterPane = new ScrollPane();
        scrollCenterPane.setFitToWidth(true);

        VBox centerPane = new VBox();
        centerPane.setSpacing(5);
        centerPane.setPadding(new Insets(5));

        ArrayList<LinkWithLabel> linksList = new ArrayList<LinkWithLabel>();
        try {
            linksList = new ObjectMapper().readValue(links, new TypeReference<ArrayList<LinkWithLabel>>(){});

            for (LinkWithLabel link : linksList) {
                Hyperlink hyperlink = new Hyperlink(link.getLabel());
                hyperlink.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        try {
                            Desktop.getDesktop().browse(new URI(link.getLink()));
                        } catch (IOException | URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                });

                Tooltip tooltip = new Tooltip(link.getLink());
                hyperlink.setTooltip(tooltip);

                centerPane.getChildren().add(hyperlink);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        scrollCenterPane.setContent(centerPane);

        if (this.bordered) {
            rootPane.setStyle("-fx-border-color: rgb(211, 212, 213); -fx-border-width: 1; -fx-border-radius: 5;");
        } else {
            rootPane.setStyle("-fx-border-color: transparent; -fx-border-width: 1; -fx-border-radius: 5;");
        }

        rootPane.setPadding(new Insets(10));
        rootPane.setCenter(scrollCenterPane);
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

        Label borderedLabel = new Label("Border");

        CheckBox borderedCheckbox = new CheckBox();
        borderedCheckbox.setSelected(this.bordered);

        borderedCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            this.setBordered(newValue);
        });
        
        ObservableList<LinkWithLabel> linksList = FXCollections.observableArrayList();
        
        if (links != null && !links.isEmpty()) {
            try {
                ArrayList<LinkWithLabel> templist = new ObjectMapper().readValue(links, new TypeReference<ArrayList<LinkWithLabel>>(){});
                linksList.addAll(templist);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ScrollPane linksScrollPane = new ScrollPane();
        linksScrollPane.setFitToWidth(true);

        VBox linksBox = new VBox();
        linksBox.setSpacing(5);
        linksBox.setPadding(new Insets(3));

        Button saveChangedLinksButton = new Button("Save Changes");
        GridPane.setHalignment(saveChangedLinksButton, HPos.RIGHT);

        for (LinkWithLabel link : linksList) {
            SVGPath acceptIcon = Icons.getIcon(Icon.CONFIRM, 15, 15);
            acceptIcon.setFill(Color.WHITE);

            SVGPath deleteIcon = Icons.getIcon(Icon.DELETE, 15, 15);
            deleteIcon.setFill(Color.WHITE);
            
            TextField linkField = new TextField();
            linkField.setText(link.getLink());
            linkField.setPromptText("https://example.com");

            TextField labelField = new TextField();
            labelField.setText(link.getLabel());
            labelField.setPromptText("Example");

            Button confirmButton = new Button("");
            confirmButton.setGraphic(acceptIcon);
            confirmButton.setPadding(new Insets(5));

            Button deleteButton = new Button("");
            deleteButton.setGraphic(deleteIcon);
            deleteButton.setPadding(new Insets(2, 5, 2, 5));
            deleteButton.setId("danger");

            linkField.textProperty().addListener((observable, oldValue, newValue) -> {
                confirmButton.setDisable(false);
            });

            labelField.textProperty().addListener((observable, oldValue, newValue) -> {
                confirmButton.setDisable(false);
            });

            confirmButton.setOnAction(event -> {
                if (!linkField.getText().isEmpty() && !labelField.getText().isEmpty() && Checker.checkURL(linkField.getText())) {
                    confirmButton.setDisable(true);
                    link.setLink(linkField.getText());
                    link.setLabel(labelField.getText());
                    saveChangedLinksButton.setDisable(false);    
                } else {
                    notificationProvider.addNotification(new NotificationProvider.Notification("Error", "Invalid URL"));
                }
            });

            deleteButton.setOnAction(event -> {
                linksList.remove(link);
                saveChangedLinksButton.setDisable(false);
            });

            confirmButton.setDisable(true);

            HBox linkBox = new HBox();
            linkBox.setSpacing(3);
            linkBox.getChildren().addAll(labelField, linkField, confirmButton, deleteButton);

            linksBox.getChildren().add(linkBox);
        }

        Button addLinkButton = new Button("Add Link");
        GridPane.setHalignment(addLinkButton, HPos.CENTER);

        addLinkButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                linksList.add(new LinkWithLabel("", ""));
            }
        });


        saveChangedLinksButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    saveChangedLinksButton.setDisable(true);
                    links = new ObjectMapper().writeValueAsString(linksList);
                    setLinks(links);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        linksList.addListener((ListChangeListener.Change<? extends LinkWithLabel> c) -> {
            while (c.next()) {
                linksBox.getChildren().clear();

                for (LinkWithLabel link : linksList) {
                    SVGPath acceptIcon = Icons.getIcon(Icon.CONFIRM, 15, 15);
                    acceptIcon.setFill(Color.WHITE);
        
                    SVGPath deleteIcon = Icons.getIcon(Icon.DELETE, 15, 15);
                    deleteIcon.setFill(Color.WHITE);
                    
                    TextField linkField = new TextField();
                    linkField.setText(link.getLink());
                    linkField.setPromptText("https://example.com");
        
                    TextField labelField = new TextField();
                    labelField.setText(link.getLabel());
                    labelField.setPromptText("Example");
        
                    Button confirmButton = new Button("");
                    confirmButton.setGraphic(acceptIcon);
                    confirmButton.setPadding(new Insets(5));
        
                    Button deleteButton = new Button("");
                    deleteButton.setGraphic(deleteIcon);
                    deleteButton.setPadding(new Insets(2, 5, 2, 5));
                    deleteButton.setId("danger");
        
                    linkField.textProperty().addListener((observable, oldValue, newValue) -> {
                        confirmButton.setDisable(false);
                    });
        
                    labelField.textProperty().addListener((observable, oldValue, newValue) -> {
                        confirmButton.setDisable(false);
                    });
        
                    confirmButton.setOnAction(event -> {
                        if (!linkField.getText().isEmpty() && !labelField.getText().isEmpty() && Checker.checkURL(linkField.getText())) {
                            confirmButton.setDisable(true);
                            link.setLink(linkField.getText());
                            link.setLabel(labelField.getText());
                            saveChangedLinksButton.setDisable(false);    
                        } else {
                            notificationProvider.addNotification(new NotificationProvider.Notification("Error", "Invalid URL"));
                        }
                    });
        
                    deleteButton.setOnAction(event -> {
                        linksList.remove(link);
                        saveChangedLinksButton.setDisable(false);
                    });
        
                    confirmButton.setDisable(true);
        
                    HBox linkBox = new HBox();
                    linkBox.setSpacing(3);
                    linkBox.getChildren().addAll(labelField, linkField, confirmButton, deleteButton);
        
                    linksBox.getChildren().add(linkBox);
                }
            }
        });

        saveChangedLinksButton.setDisable(true);

        linksScrollPane.setContent(linksBox);

        settingsPane.setHgap(5);
        settingsPane.setVgap(6);
        settingsPane.setPadding(new Insets(5));

        settingsPane.add(nameLabel, 0, 0, 2, 1);
        settingsPane.add(headerLabel, 0, 1, 1, 1);
        settingsPane.add(headerField, 1, 1, 1, 1);
        settingsPane.add(borderedLabel, 0, 2, 1, 1);
        settingsPane.add(borderedCheckbox, 1, 2, 1, 1);
        settingsPane.add(linksScrollPane, 0, 3, 2, 1);
        settingsPane.add(addLinkButton, 0, 4, 2, 1);
        settingsPane.add(saveChangedLinksButton, 0, 5, 2, 1);

        return settingsPane;
    }

}
