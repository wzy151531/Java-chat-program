package socotra.model;

import socotra.Client;
import socotra.protocol.EncryptedClient;
import socotra.protocol.Saver;

public class SignUpModel {

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
     * @param username The username used to login.
     * @param password The password used to login.
     * @return The errorType after login.
     */
    public int handleSignUp(String username, String password) {
        EncryptedClient encryptedClient = new EncryptedClient();
        Client.setClientThread(new ClientThread("localhost", username, password, 2));
        Client.getClientThread().start();
        synchronized (this) {
            try {
                this.wait();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // TODO store key_bundle.
        if (errorType == 0) {
            Saver saver = new Saver(username, encryptedClient);
            saver.saveStores();
        }
        return errorType;
    }

}
