package socotra.util;

import socotra.Client;
import socotra.common.ChatSession;
import socotra.common.ConnectionData;

import java.util.HashMap;
import java.util.List;

public class SetChatData extends Thread {

    private HashMap<ChatSession, List<ConnectionData>> chatData;

    public SetChatData(HashMap<ChatSession, List<ConnectionData>> chatData) {
        this.chatData = chatData;
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
        Client.getHomeModel().setChatData(chatData);
    }

}
