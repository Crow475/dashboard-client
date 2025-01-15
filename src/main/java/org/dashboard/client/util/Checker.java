package org.dashboard.client.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class Checker {
    public static boolean checkURL(String url) {
        try {
            URL u = URI.create(url).toURL();
            return true;
        } catch (MalformedURLException | IllegalArgumentException e) {
            return false;
        }
    }
}
