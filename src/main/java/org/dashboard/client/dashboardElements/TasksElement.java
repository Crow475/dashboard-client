package org.dashboard.client.dashboardElements;

import java.util.HashMap;
import java.io.IOException;
import java.util.ArrayList;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.util.Callback;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.dashboard.common.models.UserOfDashboard;

import org.dashboard.client.Icons;
import org.dashboard.client.ServerConnector;
import org.dashboard.client.Icons.Icon;
import org.dashboard.client.controls.EditModeControl;
import org.dashboard.client.controls.LoginControl;
import org.dashboard.client.providers.NotificationProvider;
import org.dashboard.client.util.Task;

@JsonIgnoreProperties({"name"})
public class TasksElement extends AbstractElement {
    
    private String header;
    private boolean bordered;
    private String tasks;

    public TasksElement(HashMap<String, String> tasksProperties, EditModeControl editModeControl) {
        super(Type.TASKS, tasksProperties, editModeControl);

        if (tasksProperties.containsKey("header")) {
            this.header = tasksProperties.get("header");
        } else {
            this.header = "";
        }
        this.updateProperty("header", this.header);

        this.bordered = tasksProperties.containsKey("bordered") && tasksProperties.get("bordered").equals("true");
        this.updateProperty("bordered", Boolean.toString(this.bordered));

        if (tasksProperties.containsKey("tasks")) {
            this.tasks = tasksProperties.get("tasks");
        } else {
            this.tasks = "";
        }
        this.updateProperty("tasks", this.tasks);
    }

    @JsonCreator
    public TasksElement(@JsonProperty("properties") HashMap<String, String> properties) {
        super(Type.TASKS, properties, null);
    }

    public void setHeader(String header) {
        this.header = header;
        this.updateProperty("header", this.header);
    }

    public void setBordered(boolean bordered) {
        this.bordered = bordered;
        this.updateProperty("bordered", Boolean.toString(this.bordered));
    }

    public void setTasks(String tasks) {
        this.tasks = tasks;
        this.updateProperty("tasks", this.tasks);
    }

    @Override
    void updateAllProperties() {
        this.setHeader(this.getProperties().get("header"));
        this.setBordered(this.getProperties().get("bordered").equals("true"));
        this.setTasks(this.getProperties().get("tasks"));
    }

    @Override
    Region getBaseNode(EditModeControl editModeControl, LoginControl loginControl) {
        BorderPane rootPane = new BorderPane();

        SVGPath editButtonIcon = Icons.getIcon(Icon.SETTINGS, 13, 13);
        editButtonIcon.setFill(Color.WHITE);

        SimpleBooleanProperty editorAccessMode = new SimpleBooleanProperty(false);
        ObservableList<UserOfDashboard> users = FXCollections.observableArrayList();
        ObservableList<Task> tasksList = FXCollections.observableArrayList();

        System.out.println("tasks: " + this.tasks);

        if (this.tasks != null && !this.tasks.equals("")) {
            System.out.println("tasks is not null");
            try {
                ArrayList<Task> tasksListTemp = new ObjectMapper().readValue(tasks, new TypeReference<ArrayList<Task>>(){});
                tasksList.addAll(tasksListTemp);
                System.out.println("taskLIst: " + tasksList);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        VBox centerPane = new VBox();
        centerPane.setPadding(new Insets(5));
        centerPane.setSpacing(5);

        ScrollPane tasktaskListBox = new ScrollPane();
        tasktaskListBox.setFitToWidth(true);

        VBox taskListBoxInner = new VBox();
        taskListBoxInner.setSpacing(5);
        taskListBoxInner.setPadding(new Insets(3));

        for (Task task : tasksList) {
            Label taskLabel = new Label(task.getText());
            taskLabel.setWrapText(true);

            Label assignedUserLabel = new Label();
            if (task.getAssignedUser() != null) {
                assignedUserLabel.setText(task.getAssignedUser().getUsername());
            } else {
                assignedUserLabel.setText("everyone");
            }
            assignedUserLabel.setStyle("-fx-font-size: 12px;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            CheckBox doneCheckBox = new CheckBox();
            doneCheckBox.setSelected(task.getDone());

            doneCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                task.setDone(newValue);
                ArrayList<Task> tasksListTemp = new ArrayList<>();
                tasksListTemp.addAll(tasksList);
                tasksList.clear();
                tasksList.addAll(tasksListTemp);
                try {
                    this.setTasks(new ObjectMapper().writeValueAsString(tasksListTemp));
    
                    editModeControl.toggleEditMode();
                    editModeControl.toggleEditMode();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            HBox taskBox = new HBox();
            taskBox.setSpacing(5);
            taskBox.getChildren().addAll(taskLabel, assignedUserLabel, spacer, doneCheckBox);

            taskListBoxInner.getChildren().add(taskBox);
        }

        tasktaskListBox.setContent(taskListBoxInner);
        centerPane.getChildren().add(tasktaskListBox);

        Label headerLabel = new Label(this.header);
        headerLabel.setPadding(new Insets(10));
        headerLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        rootPane.setCenter(centerPane);
        rootPane.setTop(headerLabel);

        if (this.bordered) {
            rootPane.setStyle("-fx-border-color: rgb(211, 212, 213); -fx-border-width: 1; -fx-border-radius: 5;");
        } else {
            rootPane.setStyle("-fx-border-color: transparent; -fx-border-width: 1; -fx-border-radius: 5;");
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

        GridPane editorPane = new GridPane();
        editorPane.setHgap(5);
        editorPane.setVgap(5);

        editorAccessMode.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                rootPane.setCenter(editorPane);

                if (serverConnector != null) {
                    ServerConnector.GetDashboardUsersResult result = serverConnector.getDashboardUsers(dashboardModel.getOwnerUsername(), loginControl.getToken(), dashboardModel.getName());
                    if (result.success) {
                        users.clear();
                        users.addAll(result.users);
                    } else {
                        if (result.message.equals("Token expired")) {
                            loginControl.loginSuccessProperty().set(false);
                        }
                        notificationProvider.addNotification(new NotificationProvider.Notification("Error", result.message, Color.RED));
                    }
                }
            } else {
                rootPane.setCenter(centerPane);
            }
        });


        Callback<ListView<UserOfDashboard>, ListCell<UserOfDashboard>> factory = new Callback<ListView<UserOfDashboard>,ListCell<UserOfDashboard>>() {
            @Override
            public ListCell<UserOfDashboard> call(ListView<UserOfDashboard> p) {
                return new ListCell<UserOfDashboard>() {
                    @Override
                    protected void updateItem(UserOfDashboard item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty ? "" : item.getUsername());
                    }
                };
            }
        };

        ScrollPane taskListScrollPane = new ScrollPane();
        taskListScrollPane.setFitToWidth(true);
        VBox taskList = new VBox();
        taskList.setSpacing(5);
        taskList.setPadding(new Insets(3));

        Button saveChangesButton = new Button("Save changes");
        GridPane.setHalignment(saveChangesButton, HPos.RIGHT);
        
        for (Task task : tasksList) {
            SVGPath acceptIcon = Icons.getIcon(Icon.CONFIRM, 15, 15);
            acceptIcon.setFill(Color.WHITE);

            SVGPath deleteIcon = Icons.getIcon(Icon.DELETE, 15, 15);
            deleteIcon.setFill(Color.WHITE);
            
            TextField taskField = new TextField();
            taskField.setText(task.getText());
            
            ComboBox<UserOfDashboard> usernameCombobox = new ComboBox<UserOfDashboard>();
            usernameCombobox.itemsProperty().set(users);
            usernameCombobox.setStyle("-fx-min-width: none;");
            usernameCombobox.setCellFactory(factory);
            usernameCombobox.setButtonCell(factory.call(null));
            
            usernameCombobox.getSelectionModel().select(task.getAssignedUser());

            CheckBox doneCheckBox = new CheckBox();
            doneCheckBox.setSelected(task.getDone());

            Button confirmButton = new Button("");
            confirmButton.setGraphic(acceptIcon);
            confirmButton.setPadding(new Insets(5));

            Button deleteButton = new Button("");
            deleteButton.setGraphic(deleteIcon);
            deleteButton.setPadding(new Insets(2, 5, 2, 5));
            deleteButton.setId("danger");

            HBox taskRow = new HBox();
            taskRow.setSpacing(3);
            taskRow.getChildren().addAll(taskField, usernameCombobox, doneCheckBox, confirmButton, deleteButton);
            taskRow.setAlignment(Pos.CENTER);

            taskField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.isEmpty()) {
                    confirmButton.setDisable(false);
                } else {
                    confirmButton.setDisable(true);
                }
            });

            usernameCombobox.valueProperty().addListener((observable, oldValue, newValue) -> {
                confirmButton.setDisable(false);
            });

            doneCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                confirmButton.setDisable(false);
            });

            confirmButton.setOnAction(event -> {
                if (!taskField.getText().isEmpty()) {
                    task.setText(taskField.getText());
                    task.setAssignedUser(usernameCombobox.getSelectionModel().getSelectedItem());
                    task.setDone(doneCheckBox.isSelected());
                    confirmButton.setDisable(true);
                    saveChangesButton.setDisable(false);
                } else {
                    notificationProvider.addNotification(new NotificationProvider.Notification("Error", "Task text cannot be empty", Color.RED));
                }
            });

            deleteButton.setOnAction(event -> {
                tasksList.remove(task);
                saveChangesButton.setDisable(false);
            });

            confirmButton.setDisable(true);
            
            taskList.getChildren().add(taskRow);
        }

        tasksList.addListener((ListChangeListener.Change<? extends Task> c) -> {
            while (c.next()) {
                taskList.getChildren().clear();

                for (Task task : tasksList) {
                    SVGPath acceptIcon = Icons.getIcon(Icon.CONFIRM, 15, 15);
                    acceptIcon.setFill(Color.WHITE);
        
                    SVGPath deleteIcon = Icons.getIcon(Icon.DELETE, 15, 15);
                    deleteIcon.setFill(Color.WHITE);
                    
                    TextField taskField = new TextField();
                    taskField.setText(task.getText());
                    
                    ComboBox<UserOfDashboard> usernameCombobox = new ComboBox<UserOfDashboard>();
                    usernameCombobox.itemsProperty().set(users);
                    usernameCombobox.setStyle("-fx-min-width: none;");
                    usernameCombobox.setCellFactory(factory);
                    usernameCombobox.setButtonCell(factory.call(null));
                    
                    usernameCombobox.getSelectionModel().select(task.getAssignedUser());
        
                    CheckBox doneCheckBox = new CheckBox();
                    doneCheckBox.setSelected(task.getDone());
        
                    Button confirmButton = new Button("");
                    confirmButton.setGraphic(acceptIcon);
                    confirmButton.setPadding(new Insets(5));
        
                    Button deleteButton = new Button("");
                    deleteButton.setGraphic(deleteIcon);
                    deleteButton.setPadding(new Insets(2, 5, 2, 5));
                    deleteButton.setId("danger");
        
                    HBox taskRow = new HBox();
                    taskRow.setSpacing(3);
                    taskRow.getChildren().addAll(taskField, usernameCombobox, doneCheckBox, confirmButton, deleteButton);
                    taskRow.setAlignment(Pos.CENTER);

                    taskField.textProperty().addListener((observable, oldValue, newValue) -> {
                        if (!newValue.isEmpty()) {
                            confirmButton.setDisable(false);
                        } else {
                            confirmButton.setDisable(true);
                        }
                    });

                    usernameCombobox.valueProperty().addListener((observable, oldValue, newValue) -> {
                        confirmButton.setDisable(false);
                    });

                    doneCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                        confirmButton.setDisable(false);
                    });
        
                    confirmButton.setOnAction(event -> {
                        if (!taskField.getText().isEmpty()) {
                            task.setText(taskField.getText());
                            task.setAssignedUser(usernameCombobox.getSelectionModel().getSelectedItem());
                            task.setDone(doneCheckBox.isSelected());
                            confirmButton.setDisable(true);
                            saveChangesButton.setDisable(false);
                        } else {
                            notificationProvider.addNotification(new NotificationProvider.Notification("Error", "Task text cannot be empty", Color.RED));
                        }
                    });
        
                    deleteButton.setOnAction(event -> {
                        tasksList.remove(task);
                        saveChangesButton.setDisable(false);
                    });

                    confirmButton.setDisable(true);
                    
                    taskList.getChildren().add(taskRow);
                }

                for (Task task : tasksList) {
                    Label taskLabel = new Label(task.getText());
                    taskLabel.setWrapText(true);
        
                    Label assignedUserLabel = new Label();
                    if (task.getAssignedUser() != null) {
                        assignedUserLabel.setText(task.getAssignedUser().getUsername());
                    } else {
                        assignedUserLabel.setText("everyone");
                    }
                    assignedUserLabel.setStyle("-fx-font-size: 12px;");
        
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
        
                    CheckBox doneCheckBox = new CheckBox();
                    doneCheckBox.setSelected(task.getDone());
                    

                    doneCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                        task.setDone(newValue);
                        ArrayList<Task> tasksListTemp = new ArrayList<>();
                        tasksListTemp.addAll(tasksList);
                        tasksList.clear();
                        tasksList.addAll(tasksListTemp);
                        try {
                            this.setTasks(new ObjectMapper().writeValueAsString(tasksListTemp));
            
                            editModeControl.toggleEditMode();
                            editModeControl.toggleEditMode();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        
                    HBox taskBox = new HBox();
                    taskBox.setSpacing(5);
                    taskBox.getChildren().addAll(taskLabel, assignedUserLabel, spacer, doneCheckBox);
        
                    taskListBoxInner.getChildren().add(taskBox);
                }
            }
        });

        Button addTaskButton = new Button("Add Task");
        GridPane.setHalignment(addTaskButton, HPos.CENTER);
        addTaskButton.setOnAction(event -> {
            Task newTask = new Task("", false, null);
            tasksList.add(newTask);
        });

        saveChangesButton.setOnAction(event -> {
            ArrayList<Task> tasksListTemp = new ArrayList<>();
            tasksListTemp.addAll(tasksList);
            tasksList.clear();
            tasksList.addAll(tasksListTemp);
            try {
                this.setTasks(new ObjectMapper().writeValueAsString(tasksListTemp));

                editModeControl.toggleEditMode();
                editModeControl.toggleEditMode();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        saveChangesButton.setDisable(true);

        taskListScrollPane.setContent(taskList);

        editorPane.add(taskListScrollPane, 0, 0, 2, 1);
        editorPane.add(addTaskButton, 0, 1, 2, 1);
        editorPane.add(saveChangesButton, 1, 2, 1, 1);

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
