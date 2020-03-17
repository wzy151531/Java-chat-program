package socotra.jdbc;

import socotra.common.ConnectionData;
import socotra.util.Util;

/**
 * InsertClientChatDataThread is used to insert certain connectionData to the database.
 */

public class InsertClientChatDataThread extends Thread {

    /**
     * ConnectionData needs to be inserted.
     */
    private ConnectionData connectionData;

    /**
     * Constructor for InsertClientChatDataThread.
     *
     * @param connectionData The connectionData needs to be inserted.
     */
    InsertClientChatDataThread(ConnectionData connectionData) {
        this.connectionData = connectionData;
    }

    /**
     * Start inserting the connection data.
     */
    public void run() {
        try {
            int userId = JdbcUtil.queryUserId(connectionData.getUserSignature());
            String sessionName = Util.generateChatName(connectionData.getChatSession().getToUsernames());
            JdbcUtil.storeSession(userId, sessionName);
            JdbcUtil.storeChatHistory(connectionData.getUuid().toString(), connectionData.getTextData(), connectionData.getUserSignature(), sessionName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
