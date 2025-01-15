package org.dashboard.client.dashboardElements;

import java.util.HashMap;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.input.MouseEvent;

import org.dashboard.common.models.DashboardElementModel;

import org.dashboard.client.Icons;
import org.dashboard.client.ObservableDashboardModel;
import org.dashboard.client.ServerConnector;
import org.dashboard.client.controls.EditModeControl;
import org.dashboard.client.controls.LoginControl;
import org.dashboard.client.providers.NotificationProvider;

public abstract class AbstractElement extends DashboardElementModel {
    public static final DataFormat DATA_FORMAT = new DataFormat("dashboard.element");

    protected transient ObservableDashboardModel dashboardModel;
    protected transient EditModeControl editModeControl;
    protected transient NotificationProvider notificationProvider;
    protected transient ServerConnector serverConnector;

    public AbstractElement(DashboardElementModel.Type type, HashMap<String, String> elementProperties, EditModeControl editModeControl) {
        super(type, elementProperties);

        this.editModeControl = editModeControl;
    }

    public void setDashboardModel(ObservableDashboardModel dashboardModel) {
        this.dashboardModel = dashboardModel;
    }

    abstract Region getBaseNode(EditModeControl editModeControl, LoginControl loginControl);

    abstract Region getSettingsNode(EditModeControl editModeControl, LoginControl loginControl);

    abstract void updateAllProperties();

    public Region construct(EditModeControl editModeControl, LoginControl loginControl, NotificationProvider notificationProvider, ServerConnector serverConnector) {
        class Update {
            public Region node(EditModeControl editModeControl, LoginControl loginControl) {
                Region node = getBaseNode(editModeControl, loginControl);
                AnchorPane.setTopAnchor(node, 0.0);
                AnchorPane.setRightAnchor(node, 0.0);
                AnchorPane.setBottomAnchor(node, 0.0);
                AnchorPane.setLeftAnchor(node, 0.0);

                return node;
            }
        }
        
        Update update = new Update();
        this.updateAllProperties();

        AnchorPane container = new AnchorPane();
        Region node = update.node(editModeControl, loginControl);
        Region settingsNode = getSettingsNode(editModeControl, loginControl);

        this.editModeControl = editModeControl;
        this.notificationProvider = notificationProvider;
        this.serverConnector = serverConnector;

        SVGPath settingsIcon = Icons.getIcon(Icons.Icon.SETTINGS, 13, 13);
        settingsIcon.setFill(Color.WHITE);

        Button settingsButton = new Button("", settingsIcon);
        settingsButton.setPadding(new Insets(2));
        settingsButton.visibleProperty().bind(this.editModeControl.editModeProperty());

        AnchorPane.setTopAnchor(settingsNode, 0.0);
        AnchorPane.setRightAnchor(settingsNode, 0.0);
        AnchorPane.setBottomAnchor(settingsNode, 0.0);
        AnchorPane.setLeftAnchor(settingsNode, 0.0);

        AnchorPane.setTopAnchor(settingsButton, 0.0);
        AnchorPane.setRightAnchor(settingsButton, 0.0);

        settingsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (container.getChildren().contains(settingsNode)) {
                    container.getChildren().clear();
                    container.getChildren().add(update.node(editModeControl, loginControl));
                    container.getChildren().add(settingsButton);
                } else {
                    container.getChildren().clear();
                    container.getChildren().add(settingsNode);
                    container.getChildren().add(settingsButton);
                }
            }
        });

        editModeControl.editModeProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                container.getChildren().clear();
                container.getChildren().add(update.node(editModeControl, loginControl));
                container.getChildren().add(settingsButton);
            }
        });

        container.setOnDragDetected(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                Dragboard db;
                if (event.isControlDown()) {
                    db = container.startDragAndDrop(TransferMode.COPY);
                } else {
                    db = container.startDragAndDrop(TransferMode.MOVE);
                }
                
                db.setDragView(container.snapshot(null, null));
                
                ClipboardContent content = new ClipboardContent();
                content.put(DATA_FORMAT, AbstractElement.this);
                
                db.setContent(content);
                event.consume();
            }
        });
        
        container.setOnDragDone(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                if (event.getTransferMode() == TransferMode.MOVE) {
                    Parent parent = container.getParent();
                    if (parent instanceof StackPane) {
                        ((StackPane)parent).getChildren().clear();
                        if (AbstractElement.this.dashboardModel != null) {
                            dashboardModel.removeElement(AbstractElement.this);
                        }
                    }
                }
                event.consume();
            }
        });
        
        container.getChildren().addAll(node, settingsButton);
        return container;
    }
}
