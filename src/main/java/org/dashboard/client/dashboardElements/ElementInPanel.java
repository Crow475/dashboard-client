package org.dashboard.client.dashboardElements;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import org.dashboard.client.Icons;

public class ElementInPanel extends HBox {
    private AbstractElement dashboardElement;
    private CornerRadii cornerRadii = new CornerRadii(10);
    private Background normalBackground = new Background(new BackgroundFill(Color.LIGHTGRAY, cornerRadii, null));
    private Background dragBackground = new Background(new BackgroundFill(Color.GRAY, cornerRadii, null));

    public ElementInPanel(AbstractElement dashboardElement) {
        this.dashboardElement = dashboardElement;
        this.setBackground(normalBackground);
        this.setPadding(new Insets(5));
        
        this.setCursor(Cursor.OPEN_HAND);
        
        this.setOnDragDetected(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                Dragboard db = ElementInPanel.this.startDragAndDrop(TransferMode.COPY);

                db.setDragView(ElementInPanel.this.snapshot(null, null));

                ClipboardContent content = new ClipboardContent();
                content.put(AbstractElement.DATA_FORMAT, ElementInPanel.this.dashboardElement);
                
                db.setContent(content);
                ElementInPanel.this.setBackground(dragBackground);
                event.consume();
            }
        });
        
        this.setOnDragDone(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                ElementInPanel.this.setBackground(normalBackground);
                event.consume();
            }
        });
        
    }
    
    public ElementInPanel(AbstractElement dashboardElement, String label) {
        this(dashboardElement);
        this.setAlignment(Pos.CENTER_LEFT);
        this.setPrefSize(150, 30);
        this.setSpacing(5);

        Label elementLabel = new Label(label);
        elementLabel.setTextFill(Color.BLACK);

        SVGPath dragIcon = Icons.getIcon(Icons.Icon.DRAG, 12, 12);

        this.getChildren().addAll(dragIcon, elementLabel);
    }
}