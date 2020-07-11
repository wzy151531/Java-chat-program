package socotra.model;

import socotra.Client;
import socotra.protocol.EncryptedClient;
import socotra.protocol.Saver;

public class SignUpModel {

    void saveStores() {
        Saver saver = new Saver(Client.getClientThread().getUsername(), Client.getEncryptedClient());
        saver.saveStores();
    }

    /**
     * Send login connectionData to inform server.
     *
     * @param username The username used to login.
     * @param password The password used to login.
     * @return The errorType after login.
     */
    public void handleSignUp(String username, String password) {
        EncryptedClient encryptedClient = new EncryptedClient(username);
        Client.setClientThread(new ClientThread("localhost", username, password, 2));
        Client.getClientThread().start();
    }

}
