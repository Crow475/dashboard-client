package org.dashboard.client.UIElements;

import org.dashboard.client.ObservableDashboardModel;
import org.dashboard.client.ServerConnector;
import org.dashboard.client.controls.EditModeControl;
import org.dashboard.client.controls.LoginControl;
import org.dashboard.client.dashboardElements.AbstractElement;
import org.dashboard.client.providers.NotificationProvider;
import org.dashboard.common.models.UserOfDashboard;

import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class DashboardGrid extends ScrollPane {
    public DashboardGrid(ObservableDashboardModel dashboardModelProperty, EditModeControl editModeControl, LoginControl loginControl, NotificationProvider notificationProvider, ServerConnector serverConnector) {
        super();

        this.fitToWidthProperty().set(true);

        GridPane dashboardGrid = new GridPane();
        dashboardGrid.setHgap(5);
        dashboardGrid.setVgap(5);
        
        for (int i = 0; i < dashboardModelProperty.getProperties().getSizeX(); i++) {
            for (int j = 0; j < dashboardModelProperty.getProperties().getSizeY(); j++) {
                final int x = i;
                final int y = j;
                StackPane containerPane = new StackPane();
                containerPane.setPadding(new Insets(5));
                
                containerPane.prefWidthProperty().bind(dashboardGrid.widthProperty().divide(4));
                containerPane.prefHeightProperty().bind(containerPane.widthProperty().divide(1.2));
                // containerPane.prefHeightProperty().bind(dashboardGrid.heightProperty().divide(4));

                containerPane.setMinSize(80, 80);
                
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
                        containerPane.getChildren().add(dashboardElement.construct(editModeControl, loginControl, notificationProvider, serverConnector));
                        dashboardModelProperty.setElement(x, y, dashboardElement);
                        
                        event.setDropCompleted(true);
                        event.consume();
                    }
                });
                
                if (dashboardModelProperty.getProperties().getElement(i, j) != null) {
                    // System.out.println(dashboardModelProperty.getProperties().getElement(i, j).getProperties());
                    AbstractElement tempElement = (AbstractElement)dashboardModelProperty.getProperties().getElement(i, j);
                    tempElement.setDashboardModel(dashboardModelProperty);
                    containerPane.getChildren().add(tempElement.construct(editModeControl, loginControl, notificationProvider, serverConnector));
                }
                
                dashboardGrid.add(containerPane, i, j);
            }
        }
        
        this.setContent(dashboardGrid);
    }
}
