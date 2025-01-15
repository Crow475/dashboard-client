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
import org.dashboard.common.models.UserOfDashboard;

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
                result.message = response.getMessage().get("error") + " 'User exists'";
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
                result.message = response.getMessage().get("error") + " 'Create user'";
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
                result.message = response.getMessage().get("error") + " 'Get user dashboards'";
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
                result.message = response.getMessage().get("error") + " 'Create dashboard'";
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

        System.out.println("sent data: " + data);

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
                result.message = response.getMessage().get("error") + " 'Update dashboard'";
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
                result.message = response.getMessage().get("error") + " 'Get dashboard'";
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
                result.message = response.getMessage().get("error") + " 'Delete dashboard'";
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
                result.message = response.getMessage().get("error") + " 'Rename dashboard'";
                result.username = response.getMessage().get("username");
                result.oldDashboardName = response.getMessage().get("dashboardName");
                result.newDashboardName = response.getMessage().get("newDashboardName");
            }

            return result;
            
        }

        return null;
    }

    public class UserOfDashboardResult {
        public boolean success;
        public String message;
        public String username;
        public String dashboardName;
        public UserOfDashboard user;

        public UserOfDashboardResult() {
        }
    }

    public UserOfDashboardResult getUserOfDashboard(String subjectUser, String username, String token, String dashboardName) {
        HashMap<String, String> data = new HashMap<>();
        data.put("subjectUser", subjectUser);
        data.put("username", username);
        data.put("dashboardName", dashboardName);

        Request request = new Request("Get user of dashboard", data, token);

        Request response = sendRequest(request);

        if (response != null) {
            UserOfDashboardResult result = new UserOfDashboardResult();

            result.username = username;
            result.dashboardName = dashboardName;
            
            if (response.getType().equals("Get user of dashboard success")) {
                result.success = true;
                result.message = response.getMessage().get("success");
                result.user = (UserOfDashboard)response.getObject();
            } else {
                result.success = false;
                result.message = response.getMessage().get("error") + " 'Get user of dashboard'";
            }

            return result;
        }

        return null;
    }

    public class GetDashboardUsersResult {
        public boolean success;
        public String message;
        public String username;
        public String dashboardName;
        public ArrayList<UserOfDashboard> users;

        public GetDashboardUsersResult() {
        }
    }

    public GetDashboardUsersResult getDashboardUsers(String username, String token, String dashboardName) {
        HashMap<String, String> data = new HashMap<>();
        data.put("username", username);
        data.put("dashboardName", dashboardName);

        Request request = new Request("Get dashboard users", data, token);

        Request response = sendRequest(request);

        if (response != null) {
            GetDashboardUsersResult result = new GetDashboardUsersResult();

            result.username = username;
            result.dashboardName = dashboardName;
            
            if (response.getType().equals("Get dashboard users success")) {
                result.success = true;
                result.message = response.getMessage().get("success");
                result.users = new ArrayList<UserOfDashboard>();
                if (response.getObject() != null && response.getObject() instanceof ArrayList) {
                    ArrayList<UserOfDashboard> recievedUsers = (ArrayList<UserOfDashboard>)response.getObject();
                    recievedUsers.forEach(user -> {
                        System.out.println("in connector: user: " + user);
                        result.users.add(user);
                    });
                } else {
                    result.success = false;
                    result.message = "Error: No users found";
                }
            } else {
                result.success = false;
                result.message = response.getMessage().get("error") + " 'Get dashboard users'";
            }

            return result;
        }

        return null;
    }

    public class UpdateUserOfDashboardResult {
        public boolean success;
        public String message;
        public String username;
        public String dashboardName;
        public String subjectUser;

        public UpdateUserOfDashboardResult() {
        }
    }

    public UpdateUserOfDashboardResult updateUserOfDashboard(String username, String token, String dashboardName, String subjectUser, String role) {
        HashMap<String, String> data = new HashMap<>();
        data.put("username", username);
        data.put("dashboardName", dashboardName);
        data.put("subjectUser", subjectUser);
        data.put("newRole", role);

        Request request = new Request("Update user of dashboard", data, token);

        Request response = sendRequest(request);

        if (response != null) {
            UpdateUserOfDashboardResult result = new UpdateUserOfDashboardResult();

            result.username = username;
            result.dashboardName = dashboardName;
            result.subjectUser = subjectUser;
            
            if (response.getType().equals("Update user of dashboard success")) {
                result.success = true;
                result.message = response.getMessage().get("success");
            } else {
                result.success = false;
                result.message = response.getMessage().get("error") + " 'Update user of dashboard'";
            }

            return result;
        }

        return null;
    }

    public class SearchForUserResult {
        public boolean success;
        public String message;
        public ArrayList<String> users;

        public SearchForUserResult() {
        }
    }

    public SearchForUserResult searchForUser(String username, String token) {
        HashMap<String, String> data = new HashMap<>();
        data.put("username", username);

        Request request = new Request("Search for user", data, token);

        Request response = sendRequest(request);

        if (response != null) {
            SearchForUserResult result = new SearchForUserResult();
            
            if (response.getType().equals("Search for user success")) {
                result.success = true;
                result.message = response.getMessage().get("success");
                result.users = new ArrayList<String>();
                if (response.getObject() != null && response.getObject() instanceof ArrayList) {
                    ArrayList<String> recievedUsers = (ArrayList<String>)response.getObject();
                    recievedUsers.forEach(user -> {
                        result.users.add(user);
                    });
                }
            } else if (response.getMessage().get("error").equals("No users found")) {
                result.success = true;
                result.message = response.getMessage().get("error");
                result.users = null;
            } else {
                result.success = false;
                result.message = response.getMessage().get("error") + " 'Search for user'";
            }

            return result;
            
        }

        return null;
    }

    public class AddUserOfDashboardResult {
        public boolean success;
        public String message;
        public String username;
        public String dashboardName;
        public String subjectUser;

        public AddUserOfDashboardResult() {
        }
    }

    public AddUserOfDashboardResult addUserOfDashboard(String username, String token, String dashboardName, String subjectUser, String role) {
        HashMap<String, String> data = new HashMap<>();
        data.put("username", username);
        data.put("dashboardName", dashboardName);
        data.put("subjectUser", subjectUser);
        data.put("newRole", role);

        Request request = new Request("Add user of dashboard", data, token);

        Request response = sendRequest(request);

        if (response != null) {
            AddUserOfDashboardResult result = new AddUserOfDashboardResult();

            result.username = username;
            result.dashboardName = dashboardName;
            result.subjectUser = subjectUser;
            
            if (response.getType().equals("Add user of dashboard success")) {
                result.success = true;
                result.message = response.getMessage().get("success");
            } else {
                result.success = false;
                result.message = response.getMessage().get("error") + " 'Add user of dashboard'";
            }

            return result;
        }

        return null;
    }

    public class RemoveUserOfDashboardResult {
        public boolean success;
        public String message;
        public String username;
        public String dashboardName;
        public String subjectUser;

        public RemoveUserOfDashboardResult() {
        }
    }

    public RemoveUserOfDashboardResult removeUserOfDashboard(String username, String token, String dashboardName, String subjectUser, String role) {
        HashMap<String, String> data = new HashMap<>();
        data.put("username", username);
        data.put("dashboardName", dashboardName);
        data.put("subjectUser", subjectUser);
        data.put("role", role);

        Request request = new Request("Remove user of dashboard", data, token);

        Request response = sendRequest(request);

        if (response != null) {
            RemoveUserOfDashboardResult result = new RemoveUserOfDashboardResult();

            result.username = username;
            result.dashboardName = dashboardName;
            result.subjectUser = subjectUser;
            
            if (response.getType().equals("Remove user of dashboard success")) {
                result.success = true;
                result.message = response.getMessage().get("success");
            } else {
                result.success = false;
                result.message = response.getMessage().get("error") + " 'Remove user of dashboard'";
            }

            return result;
        }

        return null;
    }

    public class DeleteAccountResult {
        public boolean success;
        public String message;
        public String username;

        public DeleteAccountResult() {
        }
    }

    public DeleteAccountResult deleteAccount(String username, String token, String password) {
        HashMap<String, String> data = new HashMap<>();
        data.put("username", username);
        data.put("password", password);

        Request request = new Request("Delete user", data, token);

        Request response = sendRequest(request);

        if (response != null) {
            DeleteAccountResult result = new DeleteAccountResult();
            
            if (response.getType().equals("User delete success")) {
                result.success = true;
                result.message = response.getMessage().get("success");
            } else {
                result.success = false;
                result.message = response.getMessage().get("error") + " 'Delete account'";
            }

            return result;
            
        }

        return null;
    }
}
