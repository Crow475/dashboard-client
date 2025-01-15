package org.dashboard.client.util;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LinkWithLabel implements Serializable {
    private String label;
    private String link;

    public LinkWithLabel(@JsonProperty("label") String label, @JsonProperty("link") String link) {
        this.label = label;
        this.link = link;
    }

    public String getLabel() {
        return label;
    }

    public String getLink() {
        return link;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof LinkWithLabel) {
            return ((LinkWithLabel)obj).getLabel().equals(this.label) && ((LinkWithLabel)obj).getLink().equals(this.link);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return label.hashCode() + link.hashCode();
    }
}
