package org.dashboard.client;

import java.util.Date;
import java.util.HashMap;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

import org.dashboard.common.Pair;
import org.dashboard.common.models.DashboardElementModel;
import org.dashboard.common.models.DashboardModel;

public class ObservableDashboardModel {
    private DashboardModel dashboardModel;

    private StringProperty name;
    private ObjectProperty<Date> createdAt;
    private ObjectProperty<Date> updatedAt;
    private ObjectProperty<DashboardModel.Properties> propertiesObservable;
    private ObservableMap<Pair<Integer, Integer>, DashboardElementModel> elements;
    private StringProperty ownerUsername;

    public ObservableDashboardModel(DashboardModel dashboardModel) {
        this.dashboardModel = dashboardModel;
        this.name = new SimpleStringProperty(dashboardModel.getName());
        this.createdAt = new SimpleObjectProperty<>(dashboardModel.getCreatedAt());
        this.updatedAt = new SimpleObjectProperty<>(dashboardModel.getUpdatedAt());
        this.propertiesObservable = new SimpleObjectProperty<>(dashboardModel.getProperties());
        this.ownerUsername = new SimpleStringProperty(dashboardModel.getOwnerUsername());

        this.elements = FXCollections.observableMap(new HashMap<>());

        if (dashboardModel.getProperties() != null) {
            dashboardModel.getProperties().getElements().forEach((key, value) -> {
                elements.put(key, value);
            });
        }

        this.name.addListener((observable, oldValue, newValue) -> {
            this.dashboardModel.setName(newValue);
        });

        this.elements.addListener((MapChangeListener<Pair<Integer, Integer>, DashboardElementModel>) change -> {
            if (change.wasAdded()) {
                dashboardModel.getProperties().setElement(
                    change.getKey().getKey(),
                    change.getKey().getValue(),
                    change.getValueAdded()
                );
                propertiesObservable.set(dashboardModel.getProperties());
            }
            if (change.wasRemoved()) {
                dashboardModel.getProperties().getElements().remove(change.getKey());
                propertiesObservable.set(dashboardModel.getProperties());
            }
        });
    }

    public StringProperty nameProperty() {
        return name;
    }

    public ObjectProperty<Date> createdAtProperty() {
        return createdAt;
    }

    public ObjectProperty<Date> updatedAtProperty() {
        return updatedAt;
    }

    public ObjectProperty<DashboardModel.Properties> propertiesProperty() {
        return propertiesObservable;
    }

    public ObservableMap<Pair<Integer, Integer>, DashboardElementModel> elementsProperty() {
        return elements;
    }

    public StringProperty ownerUsernameProperty() {
        return ownerUsername;
    }

    public String getName() {
        return name.get();
    }

    public Date getCreatedAt() {
        return createdAt.get();
    }

    public Date getUpdatedAt() {
        return updatedAt.get();
    }

    public String getOwnerUsername() {
        return ownerUsername.get();
    }

    public DashboardModel getDashboardModel() {
        return dashboardModel;
    }

    public DashboardModel.Properties getProperties() {
        return propertiesObservable.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public void setElement(int x, int y, DashboardElementModel element) {
        elements.put(new Pair<>(x, y), element);
    }

    public void removeElement(int x, int y) {
        elements.remove(new Pair<>(x, y));
    }

    public void removeElement(DashboardElementModel element) {
        System.out.println("remove element called");
        elements.entrySet().removeIf(entry -> entry.getValue().equals(element));
    }



}
