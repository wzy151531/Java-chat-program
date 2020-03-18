package socotra.util;

import socotra.Client;
import socotra.common.ChatSession;
import socotra.common.ConnectionData;

import java.util.HashMap;
import java.util.List;

/**
 * SetChatData is used to set chat data when user log in.
 */

public class SetChatData extends Thread {

    /**
     * The chat history data of user.
     */
    private HashMap<ChatSession, List<ConnectionData>> chatData;

    /**
     * Constructor for SetChatData.
     *
     * @param chatData The chat history data of user.
     */
    public SetChatData(HashMap<ChatSession, List<ConnectionData>> chatData) {
        this.chatData = chatData;
    }

    /**
     * Start setting chat data. If homeModel is not created, then this thread wait.
     */
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
        Client.getHomeModel().setChatData(chatData);
    }

}
