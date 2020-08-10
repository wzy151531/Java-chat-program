package socotra.util;

import org.whispersystems.libsignal.protocol.SenderKeyDistributionMessage;
import socotra.Client;
import socotra.common.ChatSession;
import socotra.protocol.EncryptedClient;

public class UpdateSenderKey extends Thread {

    private void processUpdateSenderKey() {
        System.out.println("Process update senderKey.");
        EncryptedClient encryptedClient = Client.getEncryptedClient();
        Client.getHomeModel().getChatSessionList().forEach(n -> {
            if (n.getSessionType() == ChatSession.GROUP) {
                SenderKeyDistributionMessage SKDM = encryptedClient.updateSenderKey(n.generateChatIdCSV());
                encryptedClient.distributeSenderKey(n.getOthers(Client.getClientThread().getUser()), n, SKDM);
            }
        });
    }

    public void run() {
        if (Client.getHomeModel() == null) {
            synchronized (this) {
                try {
                    this.wait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        processUpdateSenderKey();
    }

}
