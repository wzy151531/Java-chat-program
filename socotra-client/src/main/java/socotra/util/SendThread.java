package socotra.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import socotra.Client;
import socotra.common.ConnectionData;
import socotra.protocol.Saver;

import java.io.IOException;
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
            if (logout) {
                Platform.runLater(() -> {
                    Alert warningAlert = Util.generateAlert(Alert.AlertType.WARNING, "Log out", "Log out confirmation.", "Confirm to log out.");
                    warningAlert.setResultConverter(dialogButton -> {
                        if (dialogButton == ButtonType.OK) {
                            try {
                                Saver saver = new Saver(Client.getClientThread().getUsername(), Client.getEncryptedClient());
                                saver.saveStores();
                                saver.saveChatData(Client.getHomeModel().getChatDataCopy());
                                toServer.writeObject(connectionData);
                                Client.getClientThread().endConnection();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            System.exit(0);
                        }
                        return null;
                    });
                    warningAlert.getButtonTypes().add(ButtonType.CANCEL);
                    warningAlert.show();
                });

            } else {
                toServer.writeObject(connectionData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
