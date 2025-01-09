package org.dashboard.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.ArrayList;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import io.github.cdimascio.dotenv.Dotenv;

import org.dashboard.common.Passwords;
import org.dashboard.common.Request;
import org.dashboard.common.models.DashboardModel;

public class ServerConnector {
    private String host = "localhost";
    private int port = 3000;
    private SSLSocket socket;
    private SSLSession session;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ServerConnector() {
    }

    public ServerConnector(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private SSLContext createSSLContext() {
        Dotenv dotenv = Dotenv.load();
        String KEYSTORE_PATH = dotenv.get("KEYSTORE_PATH");
        String KEYSTORE_PASSWORD = dotenv.get("KEYSTORE_PASSWORD");
        String KEY_PASSWORD = dotenv.get("KEY_PASSWORD");
        
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(KEYSTORE_PATH), KEYSTORE_PASSWORD.toCharArray());

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, KEY_PASSWORD.toCharArray());
            KeyManager[] km = keyManagerFactory.getKeyManagers();

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keyStore);
            TrustManager[] tm = trustManagerFactory.getTrustManagers();

            SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
            sslContext.init(km, tm, null);

            return sslContext;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void connect() {
        SSLContext sslContext = createSSLContext();

        if (sslContext != null) {
            SSLSocketFactory ssf = (SSLSocketFactory)sslContext.getSocketFactory();

            try {
                SSLSocket socket = (SSLSocket) ssf.createSocket(this.host, this.port);
                socket.setEnabledCipherSuites(socket.getSupportedCipherSuites());

                socket.startHandshake();

                this.socket = socket;
                this.session = socket.getSession();

                System.out.println("SSL connection established with: " + session.getPeerHost());

                out = new ObjectOutputStream(this.socket.getOutputStream());
                in = new ObjectInputStream(this.socket.getInputStream());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void disconnect() {
        if (this.socket != null) {
            try {
                Request request = new Request("Disconnect", new HashMap<String, String>());

                out.writeObject(request);

                in.close();
                out.close();
                this.socket.close();

                System.out.println("Disconnected from server");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Request sendRequest(Request request) {
        try {
            out.writeObject(request);
            return (Request)in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public class LoginRequestResult {
        public boolean success;
        public String token;
        public String message;

        public LoginRequestResult() {
        }
    }

    public LoginRequestResult loginRequest(String username, String password) {
        HashMap<String, String> data = new HashMap<>();
        data.put("username", username);
        data.put("password", password);

        Request request = new Request("Login request", data);

        Request response = sendRequest(request);

        if (response != null) {
            LoginRequestResult result = new LoginRequestResult();
            
            if (response.getType().equals("Login success")) {
                result.success = true;
                result.message = response.getMessage().get("success");
                result.token = response.getMessage().get("token");
            } else {
                result.success = false;
                result.message = response.getMessage().get("error");
            }

            return result;
            
        }

        return null;

    }

    public class UserLookupResult {
        public boolean success;
        public String message;
        public boolean exists;

        public UserLookupResult() {
        }
    }

    public UserLookupResult userExists(String username) {
        HashMap<String, String> data = new HashMap<>();
        data.put("username", username);

        Request request = new Request("User exists", data);

        Request response = sendRequest(request);

        if (response != null) {
            UserLookupResult result = new UserLookupResult();
            
            if (response.getType().equals("User lookup success")) {
                result.success = true;
                result.message = response.getMessage().get("username");
                result.exists = Boolean.parseBoolean(response.getMessage().get("exists"));
            } else {
                result.success = false;
                result.message = response.getMessage().get("error");
            }

            return result;
            
        }

        return null;
    }

    public class UserCreateResult {
        public boolean success;
        public String message;
        public String username;

        public UserCreateResult() {
        }
    }

    public UserCreateResult createUser(String username, Passwords.Password password) {
        HashMap<String, String> data = new HashMap<>();
        Passwords.Password pass = password;
        data.put("username", username);
        

        Request request = new Request("Create user", data, pass);

        Request response = sendRequest(request);

        if (response != null) {
            UserCreateResult result = new UserCreateResult();
            
            if (response.getType().equals("Create user success")) {
                result.success = true;
                result.message = response.getMessage().get("success");
                result.username = response.getMessage().get("username");
            } else {
                result.success = false;
                result.message = response.getMessage().get("error");
            }

            return result;
            
        }

        return null;
    }

    public class UserDashboardsResult {
        public boolean success;
        public String message;
        public ArrayList<DashboardModel> dashboards;

        public UserDashboardsResult() {
        }
    }

    public UserDashboardsResult getUserDashboards(String username, String token) {
        HashMap<String, String> data = new HashMap<>();
        data.put("username", username);

        Request request = new Request("Get user dashboards", data, token);

        Request response = sendRequest(request);

        if (response != null) {
            UserDashboardsResult result = new UserDashboardsResult();
            
            if (response.getType().equals("Get user dashboards success")) {
                result.success = true;
                result.message = response.getMessage().get("success");
                result.dashboards = new ArrayList<DashboardModel>();
                ArrayList<DashboardModel> recievedDashboards = (ArrayList<DashboardModel>)response.getObject();
                recievedDashboards.forEach(dashboard -> {
                    dashboard.updatePropertiesFromJSON();
                    result.dashboards.add(dashboard);
                });
            } else {
                result.success = false;
                result.message = response.getMessage().get("error");
            }

            return result;
            
        }

        return null;
    }

    public class DashboardCreateResult {
        public boolean success;
        public String message;
        public String username;
        public String dashboardName;

        public DashboardCreateResult() {
        }
    }

    public DashboardCreateResult createDashboard(String username, String token, String dashboardName) {
        HashMap<String, String> data = new HashMap<>();
        data.put("username", username);
        data.put("dashboardName", dashboardName);

        Request request = new Request("Create dashboard", data, token);

        Request response = sendRequest(request);

        if (response != null) {
            DashboardCreateResult result = new DashboardCreateResult();
            
            if (response.getType().equals("Create dashboard success")) {
                result.success = true;
                result.message = response.getMessage().get("success");
                result.username = response.getMessage().get("username");
                result.dashboardName = response.getMessage().get("dashboardName");
            } else {
                result.success = false;
                result.message = response.getMessage().get("error");
                result.username = response.getMessage().get("username");
                result.dashboardName = response.getMessage().get("dashboardName");
            }

            return result;
            
        }

        return null;
    }

    public class DashboardUpdateResult {
        public boolean success;
        public String message;
        public String username;
        public String dashboardName;

        public DashboardUpdateResult() {
        }
        
    }

    public DashboardUpdateResult updateDashboard(String username, String token, DashboardModel dashboard) {
        HashMap<String, String> data = new HashMap<>();
        data.put("username", username);
        data.put("dashboardName", dashboard.getName());
        data.put("properties", dashboard.getProperties().toJSONString());
        DashboardModel dashboardModel = new DashboardModel(dashboard.getId(), dashboard.getOwnerId(), dashboard.getCreatedAt(), dashboard.getUpdatedAt(), dashboard.getName(), dashboard.getProperties());
        dashboardModel.clearProperties();

        Request request = new Request("Update dashboard", data, dashboardModel, token);

        Request response = sendRequest(request);

        if (response != null) {
            DashboardUpdateResult result = new DashboardUpdateResult();
            
            if (response.getType().equals("Update dashboard success")) {
                result.success = true;
                result.message = response.getMessage().get("success");
                result.username = response.getMessage().get("username");
                result.dashboardName = response.getMessage().get("dashboardName");
            } else {
                result.success = false;
                result.message = response.getMessage().get("error");
                result.username = response.getMessage().get("username");
                result.dashboardName = response.getMessage().get("dashboardName");
            }

            return result;
            
        }

        return null;
    }

    public class DashboardGetResult {
        public boolean success;
        public String message;
        public String username;
        public String dashboardName;
        public DashboardModel dashboard;

        public DashboardGetResult() {
        }
    }

    public DashboardGetResult getDashboard(String username, String token, String dashboardName) {
        HashMap<String, String> data = new HashMap<>();
        data.put("username", username);
        data.put("dashboardName", dashboardName);

        Request request = new Request("Get dashboard", data, token);

        Request response = sendRequest(request);

        if (response != null) {
            DashboardGetResult result = new DashboardGetResult();

            result.username = username;
            result.dashboardName = dashboardName;
            
            if (response.getType().equals("Get dashboard success")) {
                result.success = true;
                result.message = response.getMessage().get("success");
                result.dashboard = (DashboardModel)response.getObject();
                result.dashboard.updatePropertiesFromJSON();
            } else {
                result.success = false;
                result.message = response.getMessage().get("error");
            }

            return result;
        }

        return null;
    }

    public class LogoutResult {
        public boolean success;
        public String message;

        public LogoutResult() {
        }
    }

    public LogoutResult logout(String username, String token) {
        HashMap<String, String> data = new HashMap<>();
        data.put("username", username);

        Request request = new Request("Logout request", data, token);

        Request response = sendRequest(request);

        if (response != null) {
            LogoutResult result = new LogoutResult();
            
            if (response.getType().equals("Logout success")) {
                result.success = true;
                result.message = response.getMessage().get("success");
            } else {
                result.success = false;
                result.message = response.getMessage().get("error");
            }

            return result;
            
        }

        return null;
    }

    public class DashboardDeleteResult {
        public boolean success;
        public String message;
        public String username;
        public String dashboardName;

        public DashboardDeleteResult() {
        }
    }

    public DashboardDeleteResult deleteDashboard(String username, String token, String dashboardName) {
        HashMap<String, String> data = new HashMap<>();
        data.put("username", username);
        data.put("dashboardName", dashboardName);

        Request request = new Request("Delete dashboard", data, token);

        Request response = sendRequest(request);

        if (response != null) {
            DashboardDeleteResult result = new DashboardDeleteResult();
            
            if (response.getType().equals("Delete dashboard success")) {
                result.success = true;
                result.message = response.getMessage().get("success");
                result.username = response.getMessage().get("username");
                result.dashboardName = response.getMessage().get("dashboardName");
            } else {
                result.success = false;
                result.message = response.getMessage().get("error");
                result.username = response.getMessage().get("username");
                result.dashboardName = response.getMessage().get("dashboardName");
            }

            return result;
            
        }

        return null;
    }

    public class DashboardRenameResult {
        public boolean success;
        public String message;
        public String username;
        public String oldDashboardName;
        public String newDashboardName;

        public DashboardRenameResult() {
        }
    }

    public DashboardRenameResult renameDashboard(String username, String token, String oldDashboardName, String newDashboardName) {
        HashMap<String, String> data = new HashMap<>();
        data.put("username", username);
        data.put("dashboardName", oldDashboardName);
        data.put("newDashboardName", newDashboardName);

        Request request = new Request("Rename dashboard", data, token);

        Request response = sendRequest(request);

        if (response != null) {
            DashboardRenameResult result = new DashboardRenameResult();
            
            if (response.getType().equals("Rename dashboard success")) {
                result.success = true;
                result.message = response.getMessage().get("success");
                result.username = response.getMessage().get("username");
                result.oldDashboardName = response.getMessage().get("dashboardName");
                result.newDashboardName = response.getMessage().get("newDashboardName");
            } else {
                result.success = false;
                result.message = response.getMessage().get("error");
                result.username = response.getMessage().get("username");
                result.oldDashboardName = response.getMessage().get("dashboardName");
                result.newDashboardName = response.getMessage().get("newDashboardName");
            }

            return result;
            
        }

        return null;
    }
}
