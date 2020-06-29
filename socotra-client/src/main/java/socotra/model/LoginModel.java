package socotra.model;

import socotra.Client;
import socotra.protocol.Loader;
import socotra.util.Util;

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
        }
        return errorType;
    }

}
