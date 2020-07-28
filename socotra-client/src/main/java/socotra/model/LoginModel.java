package socotra.model;

import socotra.Client;
import socotra.common.User;
import socotra.protocol.Loader;
import socotra.util.Util;

public class LoginModel {

    private Loader loader;

    void loadStores() {
        Loader loader = new Loader(Client.getClientThread().getUser());
        this.loader = loader;
        loader.loadStores();
    }

    void loadChatData() {
        System.out.println("loadChatData.");
        Client.getHomeModel().setChatData(loader.loadChatData());
    }

    /**
     * Send login connectionData to inform server.
     *
     * @param serverName The server name needs to connect.
     * @param user   The user used to login.
     * @param password   The password used to login.
     * @return The errorType after login.
     */
    public void handleLogin(String serverName, User user, String password) {
        Client.setClientThread(new ClientThread(Util.isEmpty(serverName) ? "localhost" : serverName, user, password, 1));
        Client.getClientThread().start();
    }
}
