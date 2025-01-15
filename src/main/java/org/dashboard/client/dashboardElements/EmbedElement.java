package org.dashboard.client.dashboardElements;

import java.util.HashMap;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.shape.SVGPath;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.scene.paint.Color;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.dashboard.client.Icons;
import org.dashboard.client.Icons.Icon;
import org.dashboard.client.controls.EditModeControl;
import org.dashboard.client.controls.LoginControl;
import org.dashboard.client.providers.NotificationProvider;
import org.dashboard.client.util.Checker;

@JsonIgnoreProperties({"name"})
public class EmbedElement extends AbstractElement {
    private String header;
    private boolean bordered;
    private String url;
    private Double zoom;

    public EmbedElement(HashMap<String, String> embedProperties, EditModeControl editModeControl) {
        super(Type.EMBED, embedProperties, editModeControl);

        if (embedProperties.containsKey("header")) {
            this.header = embedProperties.get("header");
        } else {
            this.header = "";
        }
        this.updateProperty("header", this.header);
        
        if (embedProperties.containsKey("url")) {
            this.url = embedProperties.get("url");
        } else {
            this.url = "";
        }

        this.updateProperty("url", this.url);

        if (embedProperties.containsKey("zoom")) {
            this.zoom = Double.parseDouble(embedProperties.get("zoom"));
        } else {
            this.zoom = 1.0;
        }

        this.updateProperty("zoom", this.zoom.toString());

        this.bordered = embedProperties.containsKey("bordered") && embedProperties.get("bordered").equals("true");
        this.updateProperty("bordered", Boolean.toString(this.bordered));
    }

    @JsonCreator
    public EmbedElement(@JsonProperty("properties") HashMap<String, String> properties) {
        super(Type.LINKS, properties, null);
    }

    public void setHeader(String header) {
        this.header = header;
        this.updateProperty("header", this.header);
    }

    public void setUrl(String url) {
        this.url = url;
        this.updateProperty("url", this.url);
    }

    public void setZoom(Double zoom) {
        this.zoom = zoom;
        this.updateProperty("zoom", this.zoom.toString());
    }

    public void setBordered(boolean bordered) {
        this.bordered = bordered;
        this.updateProperty("bordered", Boolean.toString(this.bordered));
    }

    @Override
    void updateAllProperties() {
        this.setHeader(this.getProperties().get("header"));
        this.setUrl(this.getProperties().get("url"));
        this.setZoom(Double.parseDouble(this.getProperties().get("zoom")));
        this.setBordered(this.getProperties().get("bordered").equals("true"));
    }

    @Override
    Region getBaseNode(EditModeControl editModeControl, LoginControl loginControl) {
        BorderPane rootPane = new BorderPane();

        Label headerLabel = new Label(this.header);
        headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        headerLabel.setPadding(new Insets(10));

        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();

        webView.setZoom(this.zoom);
                
        webEngine.load(this.url);
        rootPane.setCenter(webView);

        if (this.bordered) {
            rootPane.setStyle("-fx-border-color: rgb(211, 212, 213); -fx-border-width: 1; -fx-border-radius: 5;");
        } else {
            rootPane.setStyle("-fx-border-color: transparent; -fx-border-width: 1; -fx-border-radius: 5;");
        }
                
        rootPane.setPadding(new Insets(1));
        rootPane.setTop(headerLabel);

        return rootPane;
    }

    @Override
    Region getSettingsNode(EditModeControl editModeControl, LoginControl loginControl) {
        GridPane settingsPane = new GridPane();

        SVGPath URLAcceptIcon = Icons.getIcon(Icon.CONFIRM, 15, 15);
        URLAcceptIcon.setFill(Color.WHITE);

        SVGPath zoomAcceptIcon = Icons.getIcon(Icon.CONFIRM, 15, 15);
        zoomAcceptIcon.setFill(Color.WHITE);

        class check {
            public static boolean zoom(String zoom) {
                try {
                    Double.parseDouble(zoom);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }

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


        Label urlLabel = new Label("URL");

        TextField urlField = new TextField();
        urlField.setText(this.url);

        Button urlAcceptButton = new Button();
        urlAcceptButton.setGraphic(URLAcceptIcon);
        urlAcceptButton.setPadding(new Insets(5));

        urlField.textProperty().addListener((observable, oldValue, newValue) -> {
            urlAcceptButton.setDisable(false);
        });

        urlAcceptButton.setOnAction(e -> {
            urlAcceptButton.setDisable(true);
            if (Checker.checkURL(urlField.getText())) {
                urlField.setStyle("-fx-text-fill: black;");
                this.setUrl(urlField.getText());
            } else {
                urlField.setStyle("-fx-text-fill: red;");
                notificationProvider.addNotification(new NotificationProvider.Notification("Error", "Malformed URL: '" + urlField.getText() + "'. Using the previous URL.", Color.RED));
            }
        });

        Label zoomLabel = new Label("Zoom");

        TextField zoomField = new TextField();
        zoomField.setText(this.zoom.toString());

        Button zoomAcceptButton = new Button();
        zoomAcceptButton.setGraphic(zoomAcceptIcon);
        zoomAcceptButton.setPadding(new Insets(5));

        zoomField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (check.zoom(newValue)) {
                Double.parseDouble(newValue);
                zoomField.setStyle("-fx-text-fill: black;");
            } else {
                zoomField.setStyle("-fx-text-fill: red;");
            }
            zoomAcceptButton.setDisable(false);
        });

        zoomAcceptButton.setOnAction(e -> {
            zoomAcceptButton.setDisable(true);
            if (check.zoom(zoomField.getText())) {
                this.setZoom(Double.parseDouble(zoomField.getText()));
            } else {
                notificationProvider.addNotification(new NotificationProvider.Notification("Error", "Invalid zoom value: '" + zoomField.getText() + "'. Using the previous zoom value.", Color.RED));
            }
        });

        urlAcceptButton.setDisable(true);
        zoomAcceptButton.setDisable(true);

        settingsPane.setHgap(5);
        settingsPane.setVgap(5);
        settingsPane.setPadding(new Insets(5));

        settingsPane.add(nameLabel, 0, 0, 2, 1);
        settingsPane.add(headerLabel, 0, 1, 1, 1);
        settingsPane.add(headerField, 1, 1, 1, 1);
        settingsPane.add(borderedLabel, 0, 2, 1, 1);
        settingsPane.add(borderedCheckBox, 1, 2, 1, 1);
        settingsPane.add(urlLabel, 0, 3, 1, 1);
        settingsPane.add(urlField, 1, 3, 1, 1);
        settingsPane.add(urlAcceptButton, 2, 3, 1, 1);
        settingsPane.add(zoomLabel, 0, 4, 1, 1);
        settingsPane.add(zoomField, 1, 4, 1, 1);
        settingsPane.add(zoomAcceptButton, 2, 4, 1, 1);

        return settingsPane;
    }
}
