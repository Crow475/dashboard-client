package org.dashboard.client.providers;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import org.dashboard.client.Icons;

public class DialogProvider {
    public static class Dialog {
        public enum Type {
            CONFIRM,
            CONFIRMORDENY,
            TEXT
        }
        
        private String title;
        private String message;
        private Type type;

        public Dialog(String title, String message, Type type) {
            this.title = title;
            this.message = message;
            this.type = type;
        }
    }
    
    public static class DialogResult {
        private Dialog.Type type;
        private boolean confiramtion;
        private String text;

        private DialogResult(Dialog.Type type, boolean confiramtion, String text) {
            this.type = type;
            this.confiramtion = confiramtion;
            this.text = text;
        }

        public static DialogResult textConfirmResult(String text) {
            return new DialogResult(Dialog.Type.TEXT, true, text);
        }

        public static DialogResult textDenyResult() {
            return new DialogResult(Dialog.Type.TEXT, false, null);
        }

        public static DialogResult confirmDenyResult(boolean confirmation) {
            return new DialogResult(Dialog.Type.CONFIRMORDENY, confirmation, null);
        }

        public static DialogResult confirmResult() {
            return new DialogResult(Dialog.Type.CONFIRM, true, null);
        }

        public Dialog.Type getType() {
            return type;
        }

        public String getText() {
            if (type != Dialog.Type.TEXT) {
                throw new IllegalStateException("This dialog result is not of type TEXT");
            }
            return text;
        }

        public boolean getConfirmation() {
            return confiramtion;
        }
    }

    public static class DialogElement extends VBox {
        private Dialog dialog;
        private DialogProvider provider;
        private DialogResult result = null;

        public DialogElement(Dialog dialog, DialogProvider provider) {
            this.dialog = dialog;
            this.provider = provider;

            this.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(5, 5, 5, 5, false), null)));
            this.setBorder(new Border(new BorderStroke(Color.rgb(211, 212, 213), BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(1))));
            this.setMaxWidth(400);
            this.setMinWidth(400);

            HBox titleBar = new HBox();
            titleBar.setPadding(new Insets(5, 10, 5, 10));
            titleBar.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(5, 5, 0, 0, false), null)));
            titleBar.setBorder(new Border(new BorderStroke(Color.rgb(211, 212, 213), BorderStrokeStyle.SOLID, new CornerRadii(5, 5, 0, 0, false), new BorderWidths(0, 0, 1, 0))));
            titleBar.setSpacing(10);
            titleBar.setAlignment(Pos.CENTER_LEFT);

            Label titleLabel = new Label(this.dialog.title);
            titleLabel.setStyle("-fx-font-weight: bold;");

            Button closeButton = new Button();
            closeButton.setGraphic(Icons.getIcon(Icons.Icon.CLOSE, 20, 20));
            closeButton.setPadding(new Insets(0));
            closeButton.setId("transparent");

            HBox spacer = new HBox();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            VBox messageBox = new VBox();
            messageBox.setPadding(new Insets(10));
            messageBox.setSpacing(10);
            messageBox.setAlignment(Pos.TOP_LEFT);
            messageBox.setMaxWidth(400);
            messageBox.setMinHeight(50);

            VBox.setVgrow(messageBox, Priority.ALWAYS);

            Label messageLabel = new Label(this.dialog.message);
            messageLabel.setWrapText(true);

            TextField textField = new TextField();

            HBox buttonBar = new HBox();
            buttonBar.setPadding(new Insets(5, 10, 5, 10));
            buttonBar.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(0, 0, 5, 5, false), null)));
            buttonBar.setBorder(new Border(new BorderStroke(Color.rgb(211, 212, 213), BorderStrokeStyle.SOLID, new CornerRadii(0, 0, 5, 5, false), new BorderWidths(1, 0, 0, 0))));
            buttonBar.setSpacing(10);
            buttonBar.setAlignment(Pos.CENTER_RIGHT);

            Button confirmButton = new Button("Confirm");
            confirmButton.setId("primary");
            if (dialog.type == Dialog.Type.CONFIRM) {
                confirmButton.setText("Ok");
            }
            Button denyButton = new Button("Deny");
            if (dialog.type == Dialog.Type.TEXT) {
                denyButton.setText("Cancel");
            }

            titleBar.getChildren().addAll(titleLabel, spacer, closeButton);

            messageBox.getChildren().add(messageLabel);
            if (dialog.type == Dialog.Type.TEXT) {
                messageBox.getChildren().add(textField);
            }

            if (dialog.type == Dialog.Type.CONFIRMORDENY || dialog.type == Dialog.Type.TEXT) {
                buttonBar.getChildren().addAll(denyButton, confirmButton);
            } else {
                buttonBar.getChildren().add(confirmButton);
            }

            this.getChildren().addAll(titleBar, messageBox, buttonBar);

            closeButton.setOnAction(event -> {
                if (dialog.type == Dialog.Type.CONFIRM) {
                    result = DialogResult.confirmResult();
                } else if (dialog.type == Dialog.Type.CONFIRMORDENY) {
                    result = DialogResult.confirmDenyResult(false);
                } else {
                    result = DialogResult.textDenyResult();
                }
                this.provider.open.set(false);
                this.provider.result.set(result);
            });

            confirmButton.setOnAction(event -> {
                if (dialog.type == Dialog.Type.CONFIRM) {
                    result = DialogResult.confirmResult();
                } else if (dialog.type == Dialog.Type.CONFIRMORDENY) {
                    result = DialogResult.confirmDenyResult(true);
                } else {
                    if (textField.getText() == null) {
                        return;
                    }

                    String text = textField.getText().strip();

                    if (text.isEmpty()) {
                        return;
                    }
                    
                    result = DialogResult.textConfirmResult(textField.getText().strip());
                }
                this.provider.open.set(false);
                this.provider.result.set(result);
            });

            denyButton.setOnAction(event -> {
                if (dialog.type == Dialog.Type.CONFIRM) {
                    result = DialogResult.confirmDenyResult(false);
                } else if (dialog.type == Dialog.Type.CONFIRMORDENY) {
                    result = DialogResult.confirmDenyResult(false);
                } else {
                    result = DialogResult.textDenyResult();
                }
                this.provider.open.set(false);
                this.provider.result.set(result);
            });
        }
    }

    private VBox pane = new VBox();
    private DialogElement currentDialogElement;

    private SimpleBooleanProperty open = new SimpleBooleanProperty(false);
    private SimpleObjectProperty<DialogResult> result = new SimpleObjectProperty<>();

    public DialogProvider() {
        this.pane.setBackground(new Background(new BackgroundFill(Color.web("rgba(0,0,0,0.5)"), null, null)));
        pane.setAlignment(Pos.CENTER);
        pane.setVisible(false);

        open.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                pane.setVisible(true);
                pane.getChildren().add(currentDialogElement);
            } else {
                pane.setVisible(false);
                pane.getChildren().clear();
                currentDialogElement = null;
            }
        });
    }

    public void startDialog(Dialog dialog) {
        DialogElement dialogElement = new DialogElement(dialog, this);
        this.currentDialogElement = dialogElement;
        this.open.set(true);
    }

    public SimpleObjectProperty<DialogResult> resultProperty() {
        return result;
    }

    public VBox getPane() {
        return pane;
    }
}

