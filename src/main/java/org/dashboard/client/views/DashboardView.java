package org.dashboard.client.views;

import java.util.HashMap;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.dashboard.common.Pair;
import org.dashboard.common.Role;
import org.dashboard.common.models.DashboardElementModel;
import org.dashboard.common.models.DashboardModel;
import org.dashboard.common.models.UserOfDashboard;
import org.dashboard.client.controls.EditModeControl;
import org.dashboard.client.controls.LoginControl;
import org.dashboard.client.controls.UserViewControl;
import org.dashboard.client.providers.DialogProvider;
import org.dashboard.client.providers.NotificationProvider;
import org.dashboard.client.dashboardElements.AbstractElement;
import org.dashboard.client.dashboardElements.AnnouncementElement;
import org.dashboard.client.dashboardElements.ClockElement;
import org.dashboard.client.dashboardElements.ElementInPanel;
import org.dashboard.client.dashboardElements.EmbedElement;
import org.dashboard.client.dashboardElements.LinksElement;
import org.dashboard.client.dashboardElements.NotesElement;
import org.dashboard.client.dashboardElements.TasksElement;
import org.dashboard.client.Icons;
import org.dashboard.client.ObservableDashboardModel;
import org.dashboard.client.ServerConnector;
import org.dashboard.client.Icons.Icon;
import org.dashboard.client.UIElements.DashboardGrid;
import org.dashboard.client.UIElements.DeleteZone;
import org.dashboard.client.UIElements.PersonItem;
import org.dashboard.client.UIElements.UserAddPane;
import org.dashboard.client.UIElements.UserEditPane;

public class DashboardView {
    private LoginControl loginControl;
    private UserViewControl userViewControl;
    private NotificationProvider notificationProvider;
    private DialogProvider dialogProvider;
    private ServerConnector serverConnector;
    private UserOfDashboard userOfDashboard;

    private ObservableDashboardModel dashboardModelProperty;

    public DashboardView(LoginControl loginControl, UserViewControl userViewControl, NotificationProvider notificationProvider, DialogProvider dialogProvider, ServerConnector serverConnector) {
        this.loginControl = loginControl;
        this.userViewControl = userViewControl;
        this.notificationProvider = notificationProvider;
        this.dialogProvider = dialogProvider;
        this.serverConnector = serverConnector;
    }

    private void updatePeople(DashboardModel dashboardModel, ObservableList<UserOfDashboard> people) {
        people.clear();

        ServerConnector.GetDashboardUsersResult dashboardUsersResult = serverConnector.getDashboardUsers(dashboardModel.getOwnerUsername(), loginControl.getToken(), dashboardModel.getName());
        if (dashboardUsersResult.success) {
            people.addAll(dashboardUsersResult.users);
        } else {
            if (dashboardUsersResult.message.equals("Token expired")) {
                loginControl.loginSuccessProperty().set(false);
            }
            notificationProvider.addNotification(new NotificationProvider.Notification("Error", dashboardUsersResult.message, Color.RED));
        }
    }
    
    public Region getRegion(DashboardModel dashboardModel) {
        this.dashboardModelProperty = new ObservableDashboardModel(dashboardModel);
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            System.out.println(objectMapper.writeValueAsString(dashboardModelProperty.getDashboardModel()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        ObservableList<UserOfDashboard> people = FXCollections.observableArrayList();

        updatePeople(dashboardModel, people);

        ServerConnector.UserOfDashboardResult userOfDashboardResult = serverConnector.getUserOfDashboard(loginControl.getUsername(), dashboardModel.getOwnerUsername(), loginControl.getToken(), dashboardModel.getName());
        if (userOfDashboardResult.success) {
            this.userOfDashboard = userOfDashboardResult.user;
        } else {
            if (userOfDashboardResult.message.equals("Token expired")) {
                loginControl.loginSuccessProperty().set(false);
            }
            notificationProvider.addNotification(new NotificationProvider.Notification("Error", userOfDashboardResult.message, Color.RED));
        }

        EditModeControl editModeControl = new EditModeControl();
        editModeControl.userEditingProperty().set(userOfDashboard);

        HashMap<String, String> clockProperties = new HashMap<>();
        clockProperties.put("showDate", "true");

        HashMap<String, String> linksProperties = new HashMap<>();
        linksProperties.put("header", "Useful links");
        linksProperties.put("links", "[{\"label\":\"Example\",\"link\":\"https://example.com\"}]");

        HashMap<String, String> embedProperties = new HashMap<>();
        embedProperties.put("header", "Embed");
        embedProperties.put("zoom", "0.5");
        embedProperties.put("url", "https://example.com");

        HashMap<String, String> announcementProperties = new HashMap<>();
        announcementProperties.put("header", "Announcement");

        HashMap<String, String> notesPropertiesPublic = new HashMap<>();
        notesPropertiesPublic.put("header", "Notes");
        notesPropertiesPublic.put("publicText", "This is an example public note");
        notesPropertiesPublic.put("publicAccess", "true");

        HashMap<String, String> notesPropertiesPrivate = new HashMap<>();
        notesPropertiesPrivate.put("header", "Notes");
        notesPropertiesPublic.put("publicAccess", "false");

        HashMap<String, String> tasksProperties = new HashMap<>();
        tasksProperties.put("header", "Tasks");

        BorderPane root = new BorderPane();

        // top
        HBox topBar = new HBox();
        topBar.setId("dashboard-menu-top");
        topBar.setSpacing(5);
        topBar.setPadding(new Insets(5));
        topBar.setAlignment(Pos.CENTER);

        SVGPath exitIcon = Icons.getIcon(Icon.BACK, 15, 15);
        exitIcon.setFill(Color.WHITE);
        SVGPath cancelEditIcon = Icons.getIcon(Icon.CLOSE, 15, 15);
        cancelEditIcon.setFill(Color.WHITE);
        SVGPath confirmEditIcon = Icons.getIcon(Icon.CONFIRM, 15, 15);
        confirmEditIcon.setFill(Color.WHITE);

        Button exitButton = new Button("Exit");
        exitButton.setGraphic(exitIcon);

        Button dumpJSONButton = new Button("dump JSON");

        Label nameLabel = new Label(dashboardModel.getName());
        nameLabel.setId("dashboard-item-name");

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);
        
        HBox editButtonsContainer = new HBox();
        editButtonsContainer.setSpacing(5);
        
        Button editDashboardButton = new Button("Edit");
        editDashboardButton.setVisible(this.userOfDashboard.getRole() == Role.OWNER || this.userOfDashboard.getRole() == Role.ADMIN);
        
        Button confirmEditButton = new Button("Confirm");
        confirmEditButton.setId("success");
        confirmEditButton.setGraphic(confirmEditIcon);
        
        Button cancelEditButton = new Button("Cancel");
        cancelEditButton.setId("danger");
        cancelEditButton.setGraphic(cancelEditIcon);
        
        editButtonsContainer.getChildren().addAll(editDashboardButton);
        topBar.getChildren().addAll(exitButton, dumpJSONButton, nameLabel, topSpacer, editButtonsContainer);

        // left
        VBox editPane = new VBox();
        editPane.setId("dashboard-menu-left");

        SVGPath addPersonIcon = Icons.getIcon(Icon.ADD, 15, 15);
        addPersonIcon.setFill(Color.WHITE);

        Accordion editAccordion = new Accordion();

        TitledPane elementsTitledPane = new TitledPane();
        elementsTitledPane.setText("Elements");
        
        VBox elementsInnerPane = new VBox();
        elementsInnerPane.setSpacing(5);
        elementsInnerPane.setPadding(new Insets(5));
        
        ElementInPanel clockElement = new ElementInPanel(new ClockElement(clockProperties, editModeControl), "Clock");
        ElementInPanel linksElement = new ElementInPanel(new LinksElement(linksProperties, editModeControl), "Links");
        ElementInPanel embedElement = new ElementInPanel(new EmbedElement(embedProperties, editModeControl), "Embed");
        ElementInPanel announcementElement = new ElementInPanel(new AnnouncementElement(announcementProperties, editModeControl), "Announcement");
        ElementInPanel notesElementPublic = new ElementInPanel(new NotesElement(notesPropertiesPublic, editModeControl), "Notes (Public)");
        ElementInPanel notesElementPrivate = new ElementInPanel(new NotesElement(notesPropertiesPrivate, editModeControl), "Notes (Private)");
        ElementInPanel tasksElement = new ElementInPanel(new TasksElement(tasksProperties, editModeControl), "Tasks");

        VBox alignToBottomInElements = new VBox();
        alignToBottomInElements.prefHeightProperty().bind(root.heightProperty().subtract(topBar.heightProperty()));
        alignToBottomInElements.setAlignment(Pos.BOTTOM_CENTER);

        DeleteZone deleteZone = new DeleteZone();

        alignToBottomInElements.getChildren().add(deleteZone);
        elementsInnerPane.getChildren().addAll(clockElement, linksElement, embedElement, announcementElement, notesElementPublic, notesElementPrivate, tasksElement, alignToBottomInElements);
        elementsTitledPane.setContent(elementsInnerPane);

        TitledPane peopleTitledPane = new TitledPane();
        peopleTitledPane.setText("People");

        VBox peopleInnerPane = new VBox();
        peopleInnerPane.setSpacing(5);
        peopleInnerPane.setPadding(new Insets(5));

        for (UserOfDashboard person : people) {
            PersonItem personItem = new PersonItem(person, userOfDashboard, editModeControl);
            peopleInnerPane.getChildren().add(personItem);
        }

        VBox alignToBottomInPeople = new VBox();
        alignToBottomInPeople.prefHeightProperty().bind(root.heightProperty().subtract(topBar.heightProperty()));
        alignToBottomInPeople.setAlignment(Pos.BOTTOM_CENTER);

        Button addPersonButton = new Button("Add");
        addPersonButton.setGraphic(addPersonIcon);
        addPersonButton.setId("primary");

        alignToBottomInPeople.getChildren().add(addPersonButton);

        peopleInnerPane.getChildren().add(alignToBottomInPeople);

        peopleTitledPane.setContent(peopleInnerPane);

        TitledPane settingsTitledPane = new TitledPane();
        settingsTitledPane.setText("Settings");

        editAccordion.getPanes().addAll(elementsTitledPane, peopleTitledPane, settingsTitledPane);

        editPane.getChildren().add(editAccordion);
        
        DashboardGrid dashboardGrid = new DashboardGrid(dashboardModelProperty, editModeControl, loginControl, notificationProvider, serverConnector);
        UserEditPane userEditPane = new UserEditPane(editModeControl, loginControl, userViewControl, notificationProvider, dialogProvider, serverConnector, dashboardModel, people);
        UserAddPane userAddPane = new UserAddPane(loginControl, serverConnector, notificationProvider, dashboardModel, people);

        root.setTop(topBar);
        root.setLeft(null);
        root.setCenter(dashboardGrid);
        
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

        exitButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                userViewControl.goToDashboardList();
            }
        });

        cancelEditButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ServerConnector.DashboardGetResult result = serverConnector.getDashboard(dashboardModel.getOwnerUsername(), loginControl.getToken(), dashboardModel.getName());
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

        addPersonButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                editModeControl.goToUserNew();
            }
        });

        elementsTitledPane.expandedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                editModeControl.goToElements();
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
                    ServerConnector.DashboardUpdateResult result = serverConnector.updateDashboard(dashboardModel.getOwnerUsername(), loginControl.getToken(), dashboardModelProperty.getDashboardModel());
                    if (result.success) {
                        notificationProvider.addNotification(new NotificationProvider.Notification("Success", "Dashboard updated"));
                    } else {
                        if (result.message.equals("Token expired")) {
                            loginControl.loginSuccessProperty().set(false);
                        }
                        notificationProvider.addNotification(new NotificationProvider.Notification("Error", result.message, Color.RED));
                        ServerConnector.DashboardGetResult resultGet = serverConnector.getDashboard(dashboardModel.getOwnerUsername(), loginControl.getToken(), dashboardModel.getName());
                        if (resultGet.success) {
                            userViewControl.goToDashboardList();
                            userViewControl.goToDashboard(resultGet.dashboard);
                        } else {
                            if (resultGet.message.equals("Token expired")) {
                                loginControl.loginSuccessProperty().set(false);
                            }
                            notificationProvider.addNotification(new NotificationProvider.Notification("Error", resultGet.message, Color.RED));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        editModeControl.editModeProperty().addListener((observable, oldValue, isEditMode) -> {
            if (isEditMode) {
                root.setLeft(editPane);
                editButtonsContainer.getChildren().remove(editDashboardButton);
                editButtonsContainer.getChildren().addAll(confirmEditButton, cancelEditButton);
            } else {
                root.setLeft(null);
                editButtonsContainer.getChildren().removeAll(confirmEditButton, cancelEditButton);
                editButtonsContainer.getChildren().add(editDashboardButton);
            }
        });

        editModeControl.submodeProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == EditModeControl.Submode.ELEMENTS) {
                root.setCenter(dashboardGrid);
            }
            if (newValue == EditModeControl.Submode.USER_NEW) {
                root.setCenter(userAddPane);
            }
            if (newValue == EditModeControl.Submode.USER_EDIT) {
                root.setCenter(userEditPane);
            }
            if (newValue == EditModeControl.Submode.SETTINGS) {
                root.setCenter(null);
            }
        });

        people.addListener(new ListChangeListener<UserOfDashboard>() {
            @Override
            public void onChanged(Change<? extends UserOfDashboard> change) {
                while (change.next()) {
                    if (change.wasAdded()) {
                        people.sort((a, b) -> {
                            if (a.getRole() == Role.OWNER) {
                                return 1;
                            } else if (b.getRole() == Role.OWNER) {
                                return -1;
                            } else {
                                if (a.getRole() == Role.ADMIN) {
                                    if (b.getRole() == Role.EDITOR || b.getRole() == Role.VIEWER) {
                                        return 1;
                                    } else {
                                        return 0;
                                    }
                                }
                                if (a.getRole() == Role.EDITOR) {
                                    if (b.getRole() == Role.VIEWER) {
                                        return 1;
                                    } else if (b.getRole() == Role.ADMIN) {
                                        return -1;
                                    } else {
                                        return 0;
                                    }
                                }
                                if (a.getRole() == Role.VIEWER) {
                                    if (b.getRole() == Role.EDITOR || b.getRole() == Role.ADMIN) {
                                        return -1;
                                    } else {
                                        return 0;
                                    }
                                }
                            }
                            return 0;
                        });
                        peopleInnerPane.getChildren().clear();
                        for (UserOfDashboard person : people) {
                            PersonItem personItem = new PersonItem(person, userOfDashboard, editModeControl);
                            peopleInnerPane.getChildren().add(personItem);
                        }
                        peopleInnerPane.getChildren().add(alignToBottomInPeople);
                    } else if (change.wasRemoved()) {
                        peopleInnerPane.getChildren().clear();
                        for (UserOfDashboard person : people) {
                            PersonItem personItem = new PersonItem(person, userOfDashboard, editModeControl);
                            peopleInnerPane.getChildren().add(personItem);
                        }
                        peopleInnerPane.getChildren().add(alignToBottomInPeople);
                    }
                }
                
            }
        });

        return root;
    }
}
