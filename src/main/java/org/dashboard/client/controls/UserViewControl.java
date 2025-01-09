package org.dashboard.client.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import org.dashboard.common.models.DashboardModel;

public class UserViewControl {
    public enum mode {
        DASHBOARD,
        DASHBOARDLIST,
        SETTINGS,
        DFEFAULT;
    }
    
    private final SimpleObjectProperty<mode> viewMode = new SimpleObjectProperty<>();
    private DashboardModel currentDashboard = null;

    public UserViewControl() {}

    public ObjectProperty<mode> viewModeProperty() {
        return viewMode;
    }

    public void goToDashboard(DashboardModel dashboard) {
        currentDashboard = dashboard;
        viewMode.set(mode.DASHBOARD);
    }

    public void goToDashboardList() {
        viewMode.set(mode.DASHBOARDLIST);
    }

    public void goToSettings() {
        viewMode.set(mode.SETTINGS);
    }

    public DashboardModel getCurrentDashboard() {
        return currentDashboard;
    }

    public void resetView() {
        viewMode.set(mode.DFEFAULT);
    }
}

