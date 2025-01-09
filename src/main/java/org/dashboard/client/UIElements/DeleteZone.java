package org.dashboard.client.UIElements;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import org.dashboard.client.Icons;
import org.dashboard.client.dashboardElements.AbstractElement;

public class DeleteZone extends VBox {
    public DeleteZone() {
        Border regularBorder = new Border(new BorderStroke(Color.RED, BorderStrokeStyle.DASHED, new CornerRadii(5), new BorderWidths(2)));
        Border draggedOverBorder = new Border(new BorderStroke(Color.DARKRED, BorderStrokeStyle.DASHED, new CornerRadii(5), new BorderWidths(2)));

        SVGPath deleteIcon = Icons.getIcon(Icons.Icon.DELETE, 24, 24);
        deleteIcon.setFill(Color.RED);
        
        Label deleteLabel = new Label("Remove");
        deleteLabel.setTextFill(Color.RED);
        
        this.setBorder(regularBorder);
        this.setAlignment(Pos.CENTER);
        this.setVisible(false);
        this.setPadding(new Insets(5));
        this.getChildren().addAll(deleteIcon, deleteLabel);

        this.setOnDragOver(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                if (event.getGestureSource() != DeleteZone.this) {
                    if (event.getDragboard().hasContent(AbstractElement.DATA_FORMAT)) {
                        event.acceptTransferModes(javafx.scene.input.TransferMode.MOVE);
                        DeleteZone.this.setBorder(draggedOverBorder);
                        deleteLabel.setTextFill(Color.DARKRED);
                        deleteIcon.setFill(Color.DARKRED);
                    }
                }
                event.consume();
            }
        });

        this.setOnDragExited(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                DeleteZone.this.setBorder(regularBorder);
                deleteLabel.setTextFill(Color.RED);
                deleteIcon.setFill(Color.RED);

                event.consume();
            }
        });

        this.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                event.setDropCompleted(true);
                event.consume();
            }
        });
    }
    
}