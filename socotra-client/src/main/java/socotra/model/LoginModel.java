package socotra.model;

import socotra.Client;
import socotra.protocol.Loader;
import socotra.util.SetChatData;
import socotra.util.Util;

public class LoginModel {

    void loadData() {
        Loader loader = new Loader(Client.getClientThread().getUsername());
        loader.loadStores();

        SetChatData setChatData = new SetChatData(loader.loadChatData());
        Client.setSetChatData(setChatData);
        setChatData.start();
    }

    /**
     * Send login connectionData to inform server.
     *
     * @param serverName The server name needs to connect.
     * @param username   The username used to login.
     * @param password   The password used to login.
     * @return The errorType after login.
     */
    public void handleLogin(String serverName, String username, String password) {
        Client.setClientThread(new ClientThread(Util.isEmpty(serverName) ? "localhost" : serverName, username, password, 1));
        Client.getClientThread().start();
    }
}
