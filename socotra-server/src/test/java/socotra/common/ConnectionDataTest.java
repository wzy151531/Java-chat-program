package socotra.common;

import org.junit.Before;
import org.junit.Test;
import socotra.util.UtilTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ConnectionDataTest {

    private HashMap<ChatSession, List<ConnectionData>> generateMockChatData() {
        HashMap<ChatSession, List<ConnectionData>> result = new HashMap<>();
        ChatSession mockChatSession1 = new ChatSession(UtilTest.generateTreeSet("admin", "admin1"), true);
        List<ConnectionData> mockList1 = new ArrayList<>();
        mockList1.add(new ConnectionData("test", "admin", mockChatSession1));
        mockList1.add(new ConnectionData("test1", "admin", mockChatSession1));
        ChatSession mockChatSession2 = new ChatSession(UtilTest.generateTreeSet("admin", "admin1"), true);
        List<ConnectionData> mockList2 = new ArrayList<>();
        mockList2.add(new ConnectionData("test2", "admin1", mockChatSession2));
        mockList2.add(new ConnectionData("test3", "admin1", mockChatSession2));
        result.put(mockChatSession2, mockList2);
        return result;
    }

    @Before
    public void init() {
        UUID uuid = UUID.randomUUID();
        ChatSession chatSession = new ChatSession(UtilTest.generateTreeSet("admin", "admin1"), true);
        byte[] audioData = "audio".getBytes();
        HashMap<ChatSession, List<ConnectionData>> chatData = generateMockChatData();
        ConnectionData typeNegativeOne = new ConnectionData(true);
        ConnectionData typeNegativeTwo = new ConnectionData("admin", true);
        ConnectionData typeNegativeThree = new ConnectionData(UtilTest.generateTreeSet("admin", "admin1", "admin2"));
        ConnectionData typeNegativeFour = new ConnectionData(uuid, "admin", chatSession);
        ConnectionData typeZero = new ConnectionData(0, "admin", "admin");
        ConnectionData typeOne1 = new ConnectionData("test", "admin", chatSession);
        ConnectionData typeOne2 = new ConnectionData("test", uuid, "admin", chatSession);
        ConnectionData typeTwo = new ConnectionData(audioData, "admin", chatSession);
        ConnectionData typeThree = new ConnectionData(chatData, "admin");
    }

}
