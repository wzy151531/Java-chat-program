package socotra.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import socotra.Client;
import socotra.common.ChatSession;
import socotra.common.ConnectionData;
import socotra.util.SetChatData;
import socotra.util.SetOnlineUsers;
import socotra.util.UtilTest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

public class HomeModelTest {

    private static ChatSession mockChatSession1;
    private static ChatSession mockChatSession2;
    private static ConnectionData connectionData1;
    private static ConnectionData connectionData2;
    private static ConnectionData connectionData3;
    private static ConnectionData connectionData4;

    @BeforeAll
    public static void init() throws Exception {
        mockChatSession1 = new ChatSession(UtilTest.generateTreeSet("admin", "admin1"), true);
        mockChatSession2 = new ChatSession(UtilTest.generateTreeSet("admin", "admin2"), true);
        connectionData1 = new ConnectionData("test", "admin", mockChatSession1);
        connectionData2 = new ConnectionData("test1", "admin", mockChatSession1);
        connectionData3 = new ConnectionData("test2", "admin1", mockChatSession2);
        connectionData4 = new ConnectionData("test3", "admin1", mockChatSession2);

        new ClientThread().start();
        Thread.sleep(1000);
        Client.setHomeModel(new HomeModel());
    }

    @Test
    public void testSetOnlineUsers() throws Exception {
        TreeSet<String> onlineUsers = UtilTest.generateTreeSet("admin", "admin1");
        new SetOnlineUsers(onlineUsers).start();
        Thread.sleep(500);
        ArrayList<String> temp = new ArrayList<>();
        temp.add("all");
        temp.add("admin");
        temp.add("admin1");
        ObservableList<String> expected = FXCollections.observableArrayList(temp);
        ObservableList<String> actual = Client.getHomeModel().getClientsList();
        assertEquals(expected, actual);
    }

    @Test
    public void testSetChatDataAndChatSessionExist() throws Exception {
        HashMap<ChatSession, List<ConnectionData>> chatData = new HashMap<>();
        List<ConnectionData> mockList1 = new ArrayList<>();
        mockList1.add(connectionData1);
        mockList1.add(connectionData2);
        List<ConnectionData> mockList2 = new ArrayList<>();
        mockList2.add(connectionData3);
        mockList2.add(connectionData4);
        chatData.put(mockChatSession1, mockList1);
        chatData.put(mockChatSession2, mockList2);

        new SetChatData(chatData).start();
        Thread.sleep(500);

        HashMap<ChatSession, ObservableList<ConnectionData>> expected = new HashMap<>();
        ObservableList<ConnectionData> mockList3 = FXCollections.observableArrayList(new ArrayList<>());
        mockList3.add(connectionData1);
        mockList3.add(connectionData2);
        ObservableList<ConnectionData> mockList4 = FXCollections.observableArrayList(new ArrayList<>());
        mockList4.add(connectionData3);
        mockList4.add(connectionData4);
        expected.put(mockChatSession1, mockList3);
        expected.put(mockChatSession2, mockList4);
        HashMap<ChatSession, ObservableList<ConnectionData>> actual = Client.getHomeModel().getChatData();
        assertEquals(expected, actual);

        boolean actual1 = Client.getHomeModel().chatSessionExist(UtilTest.generateTreeSet("admin", "admin1"));
        assertTrue(actual1);
        actual1 = Client.getHomeModel().chatSessionExist(UtilTest.generateTreeSet("admin", "admin2"));
        assertTrue(actual1);
        actual1 = Client.getHomeModel().chatSessionExist(UtilTest.generateTreeSet("admin1", "admin2"));
        assertFalse(actual1);
    }

    static class ClientThread extends Thread {
        public void run() {
            Client.main(new String[]{});
        }
    }

}
