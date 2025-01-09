package org.dashboard.client.views;

import java.util.HashMap;

import javafx.beans.binding.Bindings;
import javafx.collections.MapChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.dashboard.common.Pair;
import org.dashboard.common.models.DashboardElementModel;
import org.dashboard.common.models.DashboardModel;

import org.dashboard.client.controls.EditModeControl;
import org.dashboard.client.controls.LoginControl;
import org.dashboard.client.controls.UserViewControl;
import org.dashboard.client.providers.DialogProvider;
import org.dashboard.client.providers.NotificationProvider;
import org.dashboard.client.dashboardElements.AbstractElement;
import org.dashboard.client.dashboardElements.ClockElement;
import org.dashboard.client.dashboardElements.ElementInPanel;
import org.dashboard.client.Icons;
import org.dashboard.client.ObservableDashboardModel;
import org.dashboard.client.ServerConnector;
import org.dashboard.client.Icons.Icon;
import org.dashboard.client.UIElements.DeleteZone;

public class DashboardView {
    private LoginControl loginControl;
    private UserViewControl userViewControl;
    private NotificationProvider notificationProvider;
    private DialogProvider dialogProvider;
    private ServerConnector serverConnector;

    private ObservableDashboardModel dashboardModelProperty;

    public DashboardView(LoginControl loginControl, UserViewControl userViewControl, NotificationProvider notificationProvider, DialogProvider dialogProvider, ServerConnector serverConnector) {
        this.loginControl = loginControl;
        this.userViewControl = userViewControl;
        this.notificationProvider = notificationProvider;
        this.dialogProvider = dialogProvider;
        this.serverConnector = serverConnector;
    }
    
    public Region getRegion(DashboardModel dashboardModel) {
        this.dashboardModelProperty = new ObservableDashboardModel(dashboardModel);
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            System.out.println(objectMapper.writeValueAsString(dashboardModelProperty.getDashboardModel()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        BorderPane root = new BorderPane();
        HBox topBar = new HBox();
        VBox toolPane = new VBox();
        toolPane.setId("dashboard-menu-left");
        ScrollPane scroll = new ScrollPane();
        scroll.fitToWidthProperty().set(true);
        GridPane dashboardGrid = new GridPane();

        SVGPath cancelEditIcon = Icons.getIcon(Icon.CLOSE, 15, 15);
        cancelEditIcon.setFill(Color.WHITE);
        SVGPath confirmEditIcon = Icons.getIcon(Icon.CONFIRM, 15, 15);
        confirmEditIcon.setFill(Color.WHITE);

        Button dumpJSONButton = new Button("dump JSON");

        HBox editButtonsContainer = new HBox();

        Button editDashboardButton = new Button("Edit");

        Button confirmEditButton = new Button("Confirm");
        confirmEditButton.setId("success");
        confirmEditButton.setGraphic(confirmEditIcon);

        Button cancelEditButton = new Button("Cancel");
        cancelEditButton.setId("danger");
        cancelEditButton.setGraphic(cancelEditIcon);

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        Button exitButton = new Button("exit");

        Label nameLabel = new Label(dashboardModel.getName());
        nameLabel.setId("dashboard-item-name");
        
        EditModeControl editModeControl = new EditModeControl();

        HashMap<String, String> clockProperties = new HashMap<>();
        clockProperties.put("showDate", "true");

        ElementInPanel clockElement = new ElementInPanel(new ClockElement(clockProperties, editModeControl), "Clock");

        for (int i = 0; i < this.dashboardModelProperty.getProperties().getSizeX(); i++) {
            for (int j = 0; j < this.dashboardModelProperty.getProperties().getSizeY(); j++) {
                final int x = i;
                final int y = j;
                StackPane containerPane = new StackPane();
                containerPane.setPadding(new Insets(5));

                containerPane.prefWidthProperty().bind(dashboardGrid.widthProperty().divide(4));
                containerPane.prefHeightProperty().bind(containerPane.widthProperty().divide(1.2));
                // containerPane.prefHeightProperty().bind(dashboardGrid.heightProperty().divide(4));

                containerPane.setBackground(null);
                containerPane.borderProperty().bind(Bindings.createObjectBinding(
                    () -> editModeControl.isEditMode() ? 
                    new Border(new BorderStroke(Color.DARKGREY, BorderStrokeStyle.DASHED, new CornerRadii(5), new BorderWidths(2)))  :
                    null, 
                    editModeControl.editModeProperty()
                ));

                containerPane.setOnDragOver(new EventHandler<DragEvent>() {
                    public void handle(DragEvent event) {
                        if (editModeControl.isEditMode()) {
                            if (event.getGestureSource() != containerPane) {
                                if (event.getDragboard().hasContent(AbstractElement.DATA_FORMAT)) {
                                    if (containerPane.getChildren().isEmpty()) {
                                        event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                                    }
                                    else {
                                        event.acceptTransferModes(TransferMode.COPY);
                                    }
    
                                    containerPane.setBackground(new Background(new BackgroundFill(Color.rgb(200, 200, 200, 0.5), new CornerRadii(5), null)));
                                }
                            }
                        }
                    }
                });

                containerPane.setOnDragExited(new EventHandler<DragEvent>() {
                    @Override
                    public void handle(DragEvent event) {
                        containerPane.setBackground(null);

                        event.consume();
                    }
                });

                containerPane.setOnDragDropped(new EventHandler<DragEvent>() {
                    @Override
                    public void handle(DragEvent event) {
                        AbstractElement dashboardElement = (AbstractElement) event.getDragboard().getContent(AbstractElement.DATA_FORMAT);
                        dashboardElement.setDashboardModel(dashboardModelProperty);
                        
                        containerPane.getChildren().clear();
                        containerPane.getChildren().add(dashboardElement.construct(editModeControl));
                        dashboardModelProperty.setElement(x, y, dashboardElement);

                        event.setDropCompleted(true);
                        event.consume();
                    }
                });

                if (dashboardModelProperty.getProperties().getElement(i, j) != null) {
                    System.out.println(dashboardModelProperty.getProperties().getElement(i, j).getProperties());
                    AbstractElement tempElement = (AbstractElement)dashboardModelProperty.getProperties().getElement(i, j);
                    tempElement.setDashboardModel(dashboardModelProperty);
                    containerPane.getChildren().add(tempElement.construct(editModeControl));
                }

                dashboardGrid.add(containerPane, i, j);
            }
        }

        dashboardGrid.setHgap(5);
        dashboardGrid.setVgap(5);

        VBox alignToBottom = new VBox();
        alignToBottom.prefHeightProperty().bind(root.heightProperty().subtract(topBar.heightProperty()));
        alignToBottom.setAlignment(Pos.BOTTOM_CENTER);

        DeleteZone deleteZone = new DeleteZone();

        alignToBottom.getChildren().add(deleteZone);

        root.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                if (event.getDragboard().hasContent(AbstractElement.DATA_FORMAT) && event.getTransferMode() == TransferMode.MOVE) {
                    deleteZone.setVisible(true);
                }
                event.consume();
            }
        });

        root.setOnDragExited(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                deleteZone.setVisible(false);
                event.consume();
            }
        });

        editButtonsContainer.getChildren().addAll(editDashboardButton);
        editButtonsContainer.setSpacing(5);

        topBar.getChildren().addAll(exitButton, dumpJSONButton, nameLabel, topSpacer, editButtonsContainer);
        topBar.setId("dashboard-menu-top");
        topBar.setSpacing(5);
        topBar.setPadding(new Insets(5));
        topBar.setAlignment(Pos.CENTER);

        toolPane.getChildren().addAll(clockElement, alignToBottom);
        toolPane.setSpacing(5);
        toolPane.setPadding(new Insets(5));

        root.setTop(topBar);
        root.setLeft(null);
        scroll.setContent(dashboardGrid);
        root.setCenter(scroll);

        exitButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                userViewControl.goToDashboardList();
            }
        });

        cancelEditButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ServerConnector.DashboardGetResult result = serverConnector.getDashboard(loginControl.getUsername(), loginControl.getToken(), dashboardModel.getName());
                if (result.success) {
                    userViewControl.goToDashboardList();
                    userViewControl.goToDashboard(result.dashboard);
                } else {
                    if (result.message.equals("Token expired")) {
                        loginControl.loginSuccessProperty().set(false);
                    }
                    notificationProvider.addNotification(new NotificationProvider.Notification("Error", result.message, Color.RED));
                }
            }
        });

        confirmEditButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                editModeControl.toggleEditMode();
            }
        });
        
        editDashboardButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (editModeControl.isEditMode()) {
                    editModeControl.toggleEditMode();
                } else {
                    editModeControl.toggleEditMode();
                }
            }
        });

        dumpJSONButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    System.out.println(objectMapper.writeValueAsString(dashboardModelProperty.getDashboardModel()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        dashboardModelProperty.elementsProperty().addListener(new MapChangeListener<Pair<Integer, Integer>, DashboardElementModel>() {
            @Override
            public void onChanged(Change<? extends Pair<Integer, Integer>, ? extends DashboardElementModel> change) {
                try {
                    System.out.println(objectMapper.writeValueAsString(dashboardModelProperty.getDashboardModel()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        editModeControl.editModeProperty().addListener((observable, oldValue, isEditMode) -> {
            if (!isEditMode) {
                try {
                    System.out.println(objectMapper.writeValueAsString(dashboardModelProperty.getDashboardModel()));
                    System.out.println(dashboardModelProperty.getDashboardModel().getProperties().toJSONString());
                    ServerConnector.DashboardUpdateResult result = serverConnector.updateDashboard(loginControl.getUsername(), loginControl.getToken(), dashboardModelProperty.getDashboardModel());
                    if (result.success) {
                        notificationProvider.addNotification(new NotificationProvider.Notification("Success", "Dashboard updated"));
                    } else {
                        if (result.message.equals("Token expired")) {
                            loginControl.loginSuccessProperty().set(false);
                        }
                        notificationProvider.addNotification(new NotificationProvider.Notification("Error", result.message, Color.RED));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        editModeControl.editModeProperty().addListener((observable, oldValue, isEditMode) -> {
            if (isEditMode) {
                root.setLeft(toolPane);
                editButtonsContainer.getChildren().remove(editDashboardButton);
                editButtonsContainer.getChildren().addAll(confirmEditButton, cancelEditButton);
            } else {
                root.setLeft(null);
                editButtonsContainer.getChildren().removeAll(confirmEditButton, cancelEditButton);
                editButtonsContainer.getChildren().add(editDashboardButton);
            }
        });

        return root;
    }
}
