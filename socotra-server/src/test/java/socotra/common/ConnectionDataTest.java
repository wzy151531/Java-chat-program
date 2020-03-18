package socotra.common;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import socotra.util.UtilTest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public class ConnectionDataTest {

    private static ConnectionData typeNegativeOne;
    private static ConnectionData typeNegativeTwo;
    private static ConnectionData typeNegativeThree;
    private static ConnectionData typeNegativeFour;
    private static ConnectionData typeZero;
    private static ConnectionData typeOne1;
    private static ConnectionData typeOne2;
    private static ConnectionData typeTwo;
    private static ConnectionData typeThree;
    private static UUID uuid;
    private static TreeSet<String> onlineUsers;
    private static ChatSession chatSession;
    private static byte[] audioData;
    private static HashMap<ChatSession, List<ConnectionData>> chatData;

    @BeforeAll
    public static void init() {
        uuid = UUID.randomUUID();
        onlineUsers = UtilTest.generateTreeSet("admin", "admin1", "admin2");
        chatSession = new ChatSession(UtilTest.generateTreeSet("admin", "admin1"), true);
        audioData = "audio".getBytes();
        chatData = generateMockChatData();
        typeNegativeOne = new ConnectionData(true);
        typeNegativeTwo = new ConnectionData("admin", true);
        typeNegativeThree = new ConnectionData(onlineUsers);
        typeNegativeFour = new ConnectionData(uuid, "admin", chatSession);
        typeZero = new ConnectionData(0, "admin", "admin");
        typeOne1 = new ConnectionData("test", "admin", chatSession);
        typeOne2 = new ConnectionData("test", uuid, "admin", chatSession);
        typeTwo = new ConnectionData(audioData, "admin", chatSession);
        typeThree = new ConnectionData(chatData, "admin");
    }

    private static HashMap<ChatSession, List<ConnectionData>> generateMockChatData() {
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

    @Test
    public void testGetType() {
        int actual = typeNegativeOne.getType();
        assertEquals(actual, -1);
        actual = typeNegativeTwo.getType();
        assertEquals(actual, -2);
        actual = typeNegativeThree.getType();
        assertEquals(actual, -3);
        actual = typeNegativeFour.getType();
        assertEquals(actual, -4);
        actual = typeZero.getType();
        assertEquals(actual, 0);
        actual = typeOne1.getType();
        assertEquals(actual, 1);
        actual = typeTwo.getType();
        assertEquals(actual, 2);
        actual = typeThree.getType();
        assertEquals(actual, 3);
    }

    @Test
    public void testGetUuid() {
        UUID expected = uuid;
        UUID actual = typeNegativeFour.getUuid();
        assertEquals(expected, actual);
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeOne.getUuid();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeTwo.getUuid();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeThree.getUuid();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeZero.getUuid();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeThree.getUuid();
        });
    }

    @Test
    public void testGetIsSent() {
        typeOne1.setIsSent(false);
        boolean actual = typeOne1.getIsSent();
        assertFalse(actual);
        typeTwo.setIsSent(true);
        actual = typeTwo.getIsSent();
        assertTrue(actual);
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeOne.getIsSent();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeTwo.getIsSent();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeThree.getIsSent();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeFour.getIsSent();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeZero.getIsSent();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeThree.getIsSent();
        });
    }

    @Test
    public void testSetIsSent() {
        typeOne1.setIsSent(false);
        boolean actual = typeOne1.getIsSent();
        assertFalse(actual);
        typeTwo.setIsSent(true);
        actual = typeTwo.getIsSent();
        assertTrue(actual);
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeOne.setIsSent(true);
        });
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeTwo.setIsSent(true);
        });
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeThree.setIsSent(true);
        });
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeFour.setIsSent(true);
        });
        assertThrows(IllegalStateException.class, () -> {
            typeZero.setIsSent(true);
        });
        assertThrows(IllegalStateException.class, () -> {
            typeThree.setIsSent(true);
        });
    }

    @Test
    public void testGetUsername() {
        String expected = "admin";
        String actual = typeZero.getUsername();
        assertEquals(expected, actual);
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeOne.getUsername();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeTwo.getUsername();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeThree.getUsername();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeFour.getUsername();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeOne1.getUsername();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeTwo.getUsername();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeThree.getUsername();
        });
    }

    @Test
    public void testGetPassword() {
        String expected = "admin";
        String actual = typeZero.getPassword();
        assertEquals(expected, actual);
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeOne.getPassword();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeTwo.getPassword();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeThree.getPassword();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeFour.getPassword();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeOne1.getPassword();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeTwo.getPassword();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeThree.getPassword();
        });
    }

    @Test
    public void testGetIsOnline() {
        boolean actual = typeNegativeTwo.getIsOnline();
        assertTrue(actual);
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeOne.getIsOnline();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeThree.getIsOnline();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeFour.getIsOnline();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeZero.getIsOnline();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeOne1.getIsOnline();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeTwo.getIsOnline();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeThree.getIsOnline();
        });
    }

    @Test
    public void testGetOnlineUsers() {
        TreeSet<String> expected = UtilTest.generateTreeSet("admin", "admin1", "admin2");
        TreeSet<String> actual = typeNegativeThree.getOnlineUsers();
        assertEquals(expected, actual);
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeOne.getOnlineUsers();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeTwo.getOnlineUsers();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeFour.getOnlineUsers();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeZero.getOnlineUsers();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeOne1.getOnlineUsers();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeTwo.getOnlineUsers();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeThree.getOnlineUsers();
        });
    }

    @Test
    public void testGetValidated() {
        boolean actual = typeNegativeOne.getValidated();
        assertTrue(actual);
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeTwo.getValidated();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeThree.getValidated();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeFour.getValidated();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeZero.getValidated();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeOne1.getValidated();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeTwo.getValidated();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeThree.getValidated();
        });
    }

    @Test
    public void testGetAudioData() {
        byte[] expected = audioData;
        byte[] actual = typeTwo.getAudioData();
        assertEquals(expected, actual);
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeOne.getAudioData();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeTwo.getAudioData();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeThree.getAudioData();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeFour.getAudioData();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeZero.getAudioData();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeOne1.getAudioData();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeThree.getAudioData();
        });
    }

    @Test
    public void testGetTextData() {
        String expected = "test";
        String actual = typeOne1.getTextData();
        assertEquals(expected, actual);
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeOne.getTextData();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeTwo.getTextData();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeThree.getTextData();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeFour.getTextData();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeZero.getTextData();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeTwo.getTextData();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeThree.getTextData();
        });
    }

    @Test
    public void testGetUserSignature() {
        String expected = "admin";
        String actual = typeNegativeTwo.getUserSignature();
        assertEquals(expected, actual);
        actual = typeNegativeFour.getUserSignature();
        assertEquals(expected, actual);
        actual = typeOne1.getUserSignature();
        assertEquals(expected, actual);
        actual = typeTwo.getUserSignature();
        assertEquals(expected, actual);
        actual = typeThree.getUserSignature();
        assertEquals(expected, actual);
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeOne.getUserSignature();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeThree.getUserSignature();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeZero.getUserSignature();
        });
    }

    @Test
    public void testGetChatSession() {
        ChatSession expected = chatSession;
        ChatSession actual = typeOne1.getChatSession();
        assertEquals(expected, actual);
        actual = typeTwo.getChatSession();
        assertEquals(expected, actual);
        actual = typeNegativeFour.getChatSession();
        assertEquals(expected, actual);
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeOne.getChatSession();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeTwo.getChatSession();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeThree.getChatSession();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeZero.getChatSession();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeThree.getChatSession();
        });
    }

    @Test
    public void testGetChatData() {
        HashMap<ChatSession, List<ConnectionData>> expected = chatData;
        HashMap<ChatSession, List<ConnectionData>> actual = typeThree.getChatData();
        assertEquals(expected, actual);
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeOne.getChatData();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeTwo.getChatData();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeThree.getChatData();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeFour.getChatData();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeZero.getChatData();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeOne1.getChatData();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeTwo.getChatData();
        });
    }

}
