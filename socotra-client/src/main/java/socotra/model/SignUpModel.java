package socotra.model;

import socotra.Client;
import socotra.common.User;
import socotra.protocol.EncryptedClient;
import socotra.protocol.Saver;

public class SignUpModel {

    void saveStores() {
        Saver saver = new Saver(Client.getClientThread().getUser(), Client.getEncryptedClient());
        saver.saveStores();
    }

    /**
     * Send login connectionData to inform server.
     *
     * @param user     The user information used to login.
     * @param password The password used to login.
     * @return The errorType after login.
     */
    public void handleSignUp(User user, String password) {
        EncryptedClient encryptedClient = new EncryptedClient(user);
        Client.setClientThread(new ClientThread("localhost", user, password, 2));
        Client.getClientThread().start();
    }

}
