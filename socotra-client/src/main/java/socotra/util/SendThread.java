package socotra.util;

import socotra.Client;
import socotra.common.ConnectionData;

import java.io.ObjectOutputStream;

/**
 * SendThread is used to send some message to server.
 */

public class SendThread extends Thread {

    /**
     * ConnectionData the sendThread needs to send.
     */
    private ConnectionData connectionData;
    /**
     * If the connectionData is about logout.
     */
    private boolean logout = false;

    /**
     * The constructor for normal information.
     *
     * @param connectionData The normal information.
     */
    public SendThread(ConnectionData connectionData) {
        this.connectionData = connectionData;
    }

    /**
     * The constructor for information about logout.
     *
     * @param connectionData The logout connectionData.
     * @param logout         Indicates that the connectionData is about logout.
     */
    public SendThread(ConnectionData connectionData, boolean logout) {
        this.connectionData = connectionData;
        this.logout = logout;
    }

    /**
     * Start sending messages. If client wants to logout, exit the program.
     */
    public void run() {
        try {
            ObjectOutputStream toServer = Client.getClientThread().getToServer();
            toServer.writeObject(connectionData);
            if (logout) {
                toServer.writeObject(new ConnectionData(connectionData.getUserSignature(), false));
                Client.getClientThread().endConnection();
                System.exit(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
