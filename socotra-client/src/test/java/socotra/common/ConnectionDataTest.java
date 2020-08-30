package socotra.common;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import socotra.util.UtilTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ConnectionDataTest {

    private static ConnectionData typeNegativeOne;
    private static ConnectionData typeNegativeTwo;
    private static ConnectionData typeNegativeFour;
    private static ConnectionData typeZero;
    private static ConnectionData typeOne1;
    private static ConnectionData typeOne2;
    private static ConnectionData typeTwo;
    private static UUID uuid;
    private static TreeSet<User> onlineUsers;
    private static ChatSession chatSession;
    private static byte[] audioData;
    private static User user1, user2;

    @BeforeAll
    public static void init() {
        user1 = new User("admin", 1, true);
        user2 = new User("admin1", 1, true);
        uuid = UUID.randomUUID();
        onlineUsers = UtilTest.generateTreeSet("admin", "admin1", "admin2");
        chatSession = new ChatSession(UtilTest.generateTreeSet("admin", "admin1"), true, true, ChatSession.PAIRWISE);
        audioData = "audio".getBytes();
        typeNegativeOne = new ConnectionData(onlineUsers, null, null, null, null, null, user1);
        typeNegativeTwo = new ConnectionData(user1, true);
        typeNegativeFour = new ConnectionData(uuid, user1, chatSession, user2);
        typeZero = new ConnectionData(0, user1, "admin");
        typeOne1 = new ConnectionData("test", user1, chatSession);
        typeOne2 = new ConnectionData("test", uuid, user1, chatSession);
        typeTwo = new ConnectionData(audioData, user1, chatSession);
    }

    @Test
    public void testGetType() {
        int actual = typeNegativeOne.getType();
        assertEquals(actual, -1);
        actual = typeNegativeTwo.getType();
        assertEquals(actual, -2);
        actual = typeNegativeFour.getType();
        assertEquals(actual, -4);
        actual = typeZero.getType();
        assertEquals(actual, 0);
        actual = typeOne1.getType();
        assertEquals(actual, 1);
        actual = typeTwo.getType();
        assertEquals(actual, 2);
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
            typeZero.getUuid();
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
            typeNegativeFour.getIsSent();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeZero.getIsSent();
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
            typeNegativeFour.setIsSent(true);
        });
        assertThrows(IllegalStateException.class, () -> {
            typeZero.setIsSent(true);
        });
    }

    @Test
    public void testGetUser() {
        User expected = new User("admin", 1, true);
        User actual = typeZero.getUser();
        assertEquals(expected, actual);
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeOne.getUser();
        });
        actual = typeNegativeTwo.getUser();
        assertEquals(expected, actual);
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeFour.getUser();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeOne1.getUser();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeTwo.getUser();
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
            typeNegativeFour.getPassword();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeOne1.getPassword();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeTwo.getPassword();
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
    }

    @Test
    public void testGetOnlineUsers() {
        TreeSet<User> expected = new TreeSet<>();
        expected.add(new User("admin", 1, true));
        expected.add(new User("admin1", 1, true));
        expected.add(new User("admin2", 1, true));
        TreeSet<User> actual = typeNegativeOne.getOnlineUsers();
        assertEquals(expected, actual);

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
    }

    @Test
    public void testGetValidated() {
        boolean actual = typeNegativeOne.getValidated();
        assertTrue(actual);
        assertThrows(IllegalStateException.class, () -> {
            typeNegativeTwo.getValidated();
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
            typeNegativeFour.getAudioData();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeZero.getAudioData();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeOne1.getAudioData();
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
            typeNegativeFour.getTextData();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeZero.getTextData();
        });
        assertThrows(IllegalStateException.class, () -> {
            typeTwo.getTextData();
        });
    }

    @Test
    public void testGetUserSignature() {
        User expected = new User("admin", 1, true);
        User actual = typeNegativeFour.getUserSignature();
        assertEquals(expected, actual);
        actual = typeOne1.getUserSignature();
        assertEquals(expected, actual);
        actual = typeTwo.getUserSignature();
        assertEquals(expected, actual);
        actual = typeNegativeOne.getUserSignature();
        assertEquals(expected, actual);
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
            typeZero.getChatSession();
        });
    }

}
