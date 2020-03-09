package socotra.util;

import socotra.Client;
import socotra.common.ConnectionData;

import java.io.ObjectOutputStream;

public class SendThread extends Thread {
    private ConnectionData connectionData;
    private boolean logout = false;

    public SendThread(ConnectionData connectionData) {
        this.connectionData = connectionData;
    }

    public SendThread(ConnectionData connectionData, boolean logout) {
        this.connectionData = connectionData;
        this.logout = logout;
    }

    public void run() {
        try {
            ObjectOutputStream toServer = Client.getClientThread().getToServer();
            toServer.writeObject(connectionData);
            if (logout) {
                Client.getClientThread().endConnection();
                System.exit(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
