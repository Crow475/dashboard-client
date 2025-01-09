package org.dashboard.client.dashboardElements;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;

import org.dashboard.client.controls.EditModeControl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties({"name"})
public class ClockElement extends AbstractElement {
    private boolean showDate;
    private boolean showSeconds;
    private int[] bgColorValues = {255, 255, 255};
    private double bgOpacity = 0.0;
    
    public ClockElement(HashMap<String, String> clockProperties, EditModeControl editModeControl) {
        super(Type.CLOCK, clockProperties, editModeControl);

        this.showDate = clockProperties.containsKey("showDate") && clockProperties.get("showDate").equals("true");
        this.updateProperty("showDate", Boolean.toString(this.showDate));
        this.showSeconds = clockProperties.containsKey("showSeconds") && clockProperties.get("showSeconds").equals("true");
        this.updateProperty("showSeconds", Boolean.toString(this.showSeconds));
        
        if (clockProperties.containsKey("backgroundColor")) {
            String[] bgColorValuesStr = clockProperties.get("backgroundColor").split(" ");
            this.bgColorValues = new int[bgColorValuesStr.length];
            for (int i = 0; i < bgColorValuesStr.length - 1; i++) {
                this.bgColorValues[i] = Integer.parseInt(bgColorValuesStr[i]);
            }
            this.bgOpacity = Double.parseDouble(bgColorValuesStr[3]);
        }

        this.updateProperty("backgroundColor", bgColorValues[0] + " " + bgColorValues[1] + " " + bgColorValues[2] + " " + bgOpacity);
    }

    @JsonCreator
    public ClockElement(@JsonProperty("properties") HashMap<String, String> properties) {
        super(Type.CLOCK, properties, null);
    }

    public void setShowDate(boolean showDate) {
        this.showDate = showDate;
        this.updateProperty("showDate", Boolean.toString(this.showDate));
    }

    public void setShowSeconds(boolean showSeconds) {
        this.showSeconds = showSeconds;
        this.updateProperty("showSeconds", Boolean.toString(this.showSeconds));
    }

    public void setBackgroundColor(int[] bgColorValues) {
        this.bgColorValues = bgColorValues;
        this.updateProperty("backgroundColor", bgColorValues[0] + " " + bgColorValues[1] + " " + bgColorValues[2] + " " + bgOpacity);
    }

    @Override
    void updateAllProperties() {
        this.setShowDate(this.getProperties().containsKey("showDate") && this.getProperties().get("showDate").equals("true"));
        this.setShowSeconds(this.getProperties().containsKey("showSeconds") && this.getProperties().get("showSeconds").equals("true"));
        if (this.getProperties().containsKey("backgroundColor")) {
            String[] bgColorValuesStr = this.getProperties().get("backgroundColor").split(" ");
            this.bgColorValues = new int[bgColorValuesStr.length];
            for (int i = 0; i < bgColorValuesStr.length - 1; i++) {
                this.bgColorValues[i] = Integer.parseInt(bgColorValuesStr[i]);
            }
            this.bgOpacity = Double.parseDouble(bgColorValuesStr[3]);
        }

        this.updateProperty("backgroundColor", bgColorValues[0] + " " + bgColorValues[1] + " " + bgColorValues[2] + " " + bgOpacity);
    }

    @Override
    Region getBaseNode() {
        VBox clockContainer = new VBox();
        Label timeLabel = new Label();
        Label dateLabel = new Label();

        clockContainer.setBackground(new Background(new BackgroundFill(Color.rgb(bgColorValues[0], bgColorValues[1], bgColorValues[2], bgOpacity), new CornerRadii(5), null)));

        timeLabel.setText("00:00:00");
        timeLabel.fontProperty().bind(Bindings.createObjectBinding(() -> 
            new Font(clockContainer.getWidth() / 5),
            clockContainer.widthProperty()
        ));
        dateLabel.fontProperty().bind(Bindings.createObjectBinding(() -> {
            double fontSize = clockContainer.getWidth() / 10;
            double minFontSize = 10; 
            double maxFontSize = 20;
            if (fontSize < minFontSize) {
                fontSize = minFontSize;
            } else if (fontSize > maxFontSize) {
                fontSize = maxFontSize;
            }
            return new Font(fontSize);
        },
            clockContainer.widthProperty()
        ));

        clockContainer.setAlignment(Pos.CENTER);
        
        Timeline clock = new Timeline(new KeyFrame(
            Duration.ZERO,
            e -> {
                Date date = new Date();
                if (this.showSeconds) {
                    timeLabel.setText(new SimpleDateFormat("HH:mm:ss").format(date));
                } else {
                    timeLabel.setText(new SimpleDateFormat("HH:mm").format(date));
                }
                dateLabel.setText(new SimpleDateFormat("EEEE, dd MMM yyyy").format(date));
            }),
            new KeyFrame(Duration.seconds(1))
        );
        
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
        
        if (!this.showDate) {
            clockContainer.getChildren().addAll(timeLabel);
            return clockContainer;
        }
        clockContainer.getChildren().addAll(timeLabel, dateLabel);

        return clockContainer;
    }

    @Override
    Region getSettingsNode() {
        GridPane settingsContainer = new GridPane();
        Label showDateLabel = new Label("Show Date");
        Label showSecondsLabel = new Label("Show Seconds");
        Label backgrouundColorLabel = new Label("Background Color");
        Label nameLabel = new Label(this.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        CheckBox showDateCheckBox = new CheckBox();
        CheckBox showSecondsCheckBox = new CheckBox();
        ColorPicker backgroundColorPicker = new ColorPicker(Color.rgb(bgColorValues[0], bgColorValues[1], bgColorValues[2], bgOpacity));

        Button resetColorButton = new Button("Reset");
        resetColorButton.setId("danger");
        
        showDateCheckBox.setSelected(this.showDate);
        showSecondsCheckBox.setSelected(this.showSeconds);
        
        showDateCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            this.setShowDate(newValue);
        });
        
        showSecondsCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            this.setShowSeconds(newValue);
        });
        
        backgroundColorPicker.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Color bgColor = backgroundColorPicker.getValue();
                int[] bgColorValues = {(int) (bgColor.getRed() * 255), (int) (bgColor.getGreen() * 255), (int) (bgColor.getBlue() * 255)};
                bgOpacity = bgColor.getOpacity();
                setBackgroundColor(bgColorValues);
            }
        });

        resetColorButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                int[] bgColorValues = {255, 255, 255};
                bgOpacity = 0.0;
                setBackgroundColor(bgColorValues);
                backgroundColorPicker.setValue(Color.rgb(bgColorValues[0], bgColorValues[1], bgColorValues[2], bgOpacity));
            }
        });

        settingsContainer.add(nameLabel, 0, 0);
        settingsContainer.add(showDateLabel, 0, 1);
        settingsContainer.add(showDateCheckBox, 1, 1);
        settingsContainer.add(showSecondsLabel, 0, 2);
        settingsContainer.add(showSecondsCheckBox, 1, 2);
        settingsContainer.add(backgrouundColorLabel, 0, 3);
        settingsContainer.add(backgroundColorPicker, 1, 3);
        settingsContainer.add(resetColorButton, 2, 3);

        settingsContainer.setPadding(new Insets(5));
        settingsContainer.setHgap(5);
        settingsContainer.setVgap(5);

        return settingsContainer;
    }
}

