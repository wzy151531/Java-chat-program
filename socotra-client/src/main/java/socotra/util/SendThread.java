package socotra.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import socotra.Client;
import socotra.common.ConnectionData;
import socotra.model.ClientThread;
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

    private boolean backUp = false;

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

    public SendThread(ConnectionData connectionData, boolean backUp, int type) {
        this.connectionData = connectionData;
        this.backUp = backUp;
    }

    /**
     * Start sending messages. If client wants to logout, exit the program.
     */
    public void run() {
//            ObjectOutputStream toServer = Client.getClientThread().getToServer();
        ClientThread clientThread = Client.getClientThread();
        if (logout) {
            Platform.runLater(() -> {
                Alert warningAlert = Util.generateAlert(Alert.AlertType.WARNING, "Log out", "Log out confirmation.", "Confirm to log out.");
                warningAlert.setResultConverter(dialogButton -> {
                    if (dialogButton == ButtonType.OK) {
                        Client.processLogout(connectionData);
                    }
                    return null;
                });
                warningAlert.getButtonTypes().add(ButtonType.CANCEL);
                warningAlert.show();
            });
        } else {
            if (connectionData.getType() == 7) {
                System.out.println("Send data to: " + connectionData.getChatSession().getMembers());
            }
            clientThread.sendData(connectionData);
            if (backUp) {
                Platform.runLater(() -> {
                    Util.generateAlert(Alert.AlertType.INFORMATION, "Back up", "Back up successfully.", "Switch account to see the back up messages.").show();
                });
            }
        }
    }
}
