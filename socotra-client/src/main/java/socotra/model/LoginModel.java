package socotra.model;

import socotra.Client;
import socotra.common.ChatSession;
import socotra.common.ConnectionData;
import socotra.protocol.EncryptedClient;
import socotra.protocol.Loader;
import socotra.protocol.MySessionStore;
import socotra.util.SetChatData;
import socotra.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

public class LoginModel {

    /**
     * The error type of the connection.
     */
    private int errorType = 0;

    /**
     * Setter for error type.
     *
     * @param errorType The error type needs to be set.
     */
    void setErrorType(int errorType) {
        this.errorType = errorType;
    }

    private HashMap<ChatSession, List<ConnectionData>> generateChatData(EncryptedClient encryptedClient) {
        HashMap<ChatSession, List<ConnectionData>> result = new HashMap<>();
        MySessionStore mySessionStore = encryptedClient.getSessionStore();
        mySessionStore.getFormattedSessionMap().forEach((k, v) -> {
            TreeSet<String> users = new TreeSet<>();
            users.add(k);
            users.add(Client.getClientThread().getUsername());
            result.put(new ChatSession(users, true, true), new ArrayList<>());
        });
        return result;
    }

    /**
     * Send login connectionData to inform server.
     *
     * @param serverName The server name needs to connect.
     * @param username   The username used to login.
     * @param password   The password used to login.
     * @return The errorType after login.
     */
    public int handleLogin(String serverName, String username, String password) {
        Client.setClientThread(new ClientThread(Util.isEmpty(serverName) ? "localhost" : serverName, username, password, 1));
        Client.getClientThread().start();
        synchronized (this) {
            try {
                this.wait();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println(errorType);
        if (errorType == 0) {
            Loader loader = new Loader(username);
            loader.loadStores();
            SetChatData setChatData = new SetChatData(generateChatData(Client.getEncryptedClient()));
            Client.setSetChatData(setChatData);
            setChatData.start();
        }
        return errorType;
    }

}
