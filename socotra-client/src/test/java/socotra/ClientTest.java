package socotra;

import org.junit.jupiter.api.Test;
import socotra.common.ChatSession;
import socotra.common.ConnectionData;
import socotra.util.SetChatData;
import socotra.util.SetOnlineUsers;
import socotra.util.UtilTest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

public class ClientTest {

    @Test
    public void testSetHomeModel() throws Exception {
        Client.setHomeModel(null);
        HashMap<ChatSession, List<ConnectionData>> chatData = new HashMap<>();
        SetChatData setChatDataThread = new SetChatData(chatData);
        setChatDataThread.start();
        Thread.sleep(1000);
        Thread.State expected = Thread.State.WAITING;
        Thread.State actual = setChatDataThread.getState();
        assertEquals(expected, actual);
        TreeSet<String> onlineUsers = UtilTest.generateTreeSet("admin");
        SetOnlineUsers setOnlineUsersThread = new SetOnlineUsers(onlineUsers);
        setOnlineUsersThread.start();
        Thread.sleep(1000);
        expected = Thread.State.WAITING;
        actual = setOnlineUsersThread.getState();
        assertEquals(expected, actual);
    }

}
