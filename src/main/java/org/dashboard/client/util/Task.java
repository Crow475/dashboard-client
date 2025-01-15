package org.dashboard.client.util;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.dashboard.common.models.UserOfDashboard;

public class Task implements Serializable {
    private String text;
    private boolean done;
    private UserOfDashboard assignedUser;

    @JsonCreator
    public Task(@JsonProperty("text") String text, @JsonProperty("done") boolean done, @JsonProperty("assignedUser") UserOfDashboard assignedUser) {
        this.text = text;
        this.done = done;
        this.assignedUser = assignedUser;
    }

    public String getText() {
        return this.text;
    }

    public boolean getDone() {
        return this.done;
    }

    public UserOfDashboard getAssignedUser() {
        return this.assignedUser;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public void setAssignedUser(UserOfDashboard assignedUser) {
        this.assignedUser = assignedUser;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Task) {
            Task other = (Task) obj;
            if (this.assignedUser == null && other.assignedUser == null) {
                return this.text.equals(other.text) && this.done == other.done;
            } else if (this.assignedUser == null || other.assignedUser == null) {
                return false;
            }
            return this.text.equals(other.text) && this.done == other.done && this.assignedUser.equals(other.assignedUser);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        if (this.assignedUser == null) {
            return this.text.hashCode() + (this.done ? 1 : 0);
        }
        return this.text.hashCode() + (this.done ? 1 : 0) + this.assignedUser.hashCode();
    }
}
