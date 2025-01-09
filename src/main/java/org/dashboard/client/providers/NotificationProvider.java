package org.dashboard.client.providers;

import javafx.scene.layout.VBox;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import org.dashboard.client.Icons;

public class NotificationProvider {
    public static class Notification {
        private String message;
        private String title;
        private Color color;

        public Notification(String title, String message, Color color) {
            this.title = title;
            this.message = message;
            this.color = color;
        }

        public Notification(String title, String message) {
            this(title, message, Color.rgb(11, 96, 219));
        }
    }

    private static class NotificationElement extends VBox {
        private Notification notification;
        private NotificationProvider provider;

        public NotificationElement(Notification notification, NotificationProvider provider) {
            this.notification = notification;
            this.provider = provider;

            DropShadow shadow = new DropShadow();
            shadow.setRadius(10);
            shadow.setColor(Color.rgb(0, 0, 0, 0.2));
            shadow.setOffsetX(3);
            shadow.setOffsetY(3);

            this.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, new CornerRadii(5), null)));
            this.setBorder(new Border(new BorderStroke(Color.rgb(211, 212, 213), BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(1))));
            this.setMaxWidth(400);
            this.setPrefWidth(400);
            this.setEffect(shadow);

            HBox titleBar = new HBox();
            titleBar.setPadding(new Insets(5, 10, 5, 10));
            titleBar.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(5, 5, 0, 0, false), null)));
            titleBar.setSpacing(10);
            titleBar.setAlignment(Pos.CENTER_LEFT);

            Pane indicator = new Pane();
            indicator.setMinSize(20, 20);
            indicator.setMaxSize(20, 20);
            indicator.setBackground(new Background(new BackgroundFill(this.notification.color, new CornerRadii(5), null)));

            Label titleLabel = new Label(this.notification.title);
            titleLabel.setStyle("-fx-font-weight: bold;");
            

            Button closeButton = new Button();
            closeButton.setGraphic(Icons.getIcon(Icons.Icon.CLOSE, 20, 20));
            closeButton.setPadding(new Insets(0));
            closeButton.setId("transparent");

            HBox spacer = new HBox();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            VBox messageBox = new VBox();
            messageBox.setPadding(new Insets(0, 0, 10, 0));
            messageBox.setBackground(new Background(new BackgroundFill(Color.web("rgba(255,255,255,0.9)"), new CornerRadii(0, 0, 5, 5, false), null)));
            messageBox.setSpacing(10);
            messageBox.setAlignment(Pos.TOP_LEFT);
            messageBox.setMaxWidth(400);
            messageBox.setMinHeight(50);

            Label messageLabel = new Label(this.notification.message);
            messageLabel.setPadding(new Insets(0, 10, 0, 10));
            messageLabel.setWrapText(true);

            ProgressBar progress = new ProgressBar();
            progress.setProgress(0);
            progress.setMinWidth(400);
            progress.setMaxWidth(400);
            progress.setId("notification");

            messageBox.getChildren().addAll(progress, messageLabel);

            titleBar.getChildren().addAll(indicator, titleLabel, spacer, closeButton);

            this.getChildren().addAll(titleBar, messageBox);

            closeButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    provider.removeNotification(NotificationElement.this.notification);
                }
            });

            Timeline timeline = new Timeline();
            KeyFrame frame = new KeyFrame(Duration.seconds(0.01), new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    progress.setProgress(progress.getProgress() + 0.002);
                }
            });
            timeline.getKeyFrames().add(frame);
            timeline.setCycleCount(500);
            timeline.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    PauseTransition delay = new PauseTransition(Duration.seconds(0.2));
                    delay.setOnFinished(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            NotificationElement.this.provider.removeNotification(NotificationElement.this.notification);
                        }
                    });

                    delay.play();
                }
            });

            timeline.play();
        }
    }
    
    private ObservableList<NotificationElement> notificationList = FXCollections.observableArrayList();
    private VBox pane = new VBox();
    
    public NotificationProvider() {
        pane.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
        pane.setSpacing(10);
        pane.setPadding(new Insets(0));
        pane.setAlignment(Pos.BOTTOM_RIGHT);
        pane.setPickOnBounds(false);

        notificationList.addListener(new ListChangeListener<NotificationElement>() {
            @Override
            public void onChanged(Change<? extends NotificationElement> change) {
                while (change.next()) {
                    if (change.wasAdded()) {
                        for (NotificationElement notificationElement : change.getAddedSubList()) {
                            pane.getChildren().add(notificationElement);
                        }
                    } else if (change.wasRemoved()) {
                        for (NotificationElement notificationElement : change.getRemoved()) {
                            pane.getChildren().remove(notificationElement);
                        }
                    }
                }
            }
        });
    }

    public void addNotification(Notification notification) {
        notificationList.add(new NotificationElement(notification, this));
    }

    public void removeNotification(Notification notification) {
        notificationList.stream().filter(element -> element.notification.equals(notification)).findFirst().ifPresent(element -> {
            notificationList.remove(element);
        });
    }

    public VBox getPane() {
        return pane;
    }
}
