package socotra.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import socotra.Server;

import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.TreeSet;

public class UtilTest {

    public static TreeSet<String> generateTreeSet(String... toUsernames) {
        TreeSet<String> result = new TreeSet<>();
        for (String toUsername : toUsernames) {
            result.add(toUsername);
        }
        return result;
    }

    public static HashMap<String, ObjectOutputStream> generateHashMap(String... clients) {
        HashMap<String, ObjectOutputStream> result = new HashMap<>();
        for (String client : clients) {
            result.put(client, null);
        }
        return result;
    }

    @Test
    public void testGenerateChatName() {
        TreeSet<String> toUsernames = generateTreeSet("admin");
        String expected = "admin";
        String actual = Util.generateChatName(toUsernames);
        assertEquals(expected, actual);

        toUsernames = generateTreeSet("admin", "admin1", "admin2");
        expected = "admin,admin1,admin2";
        actual = Util.generateChatName(toUsernames);
        assertEquals(expected, actual);
    }

    @Test
    public void testIsAnyOnline() {
        HashMap<String, ObjectOutputStream> onlineUsers = generateHashMap("admin", "admin1", "admin2");
        Server.getClients().clear();
        onlineUsers.forEach((k, v) -> {
            Server.addClient(k, v);
        });
        TreeSet<String> users = generateTreeSet("admin");
        boolean actual = Util.isAnyOnline(users);
        assertTrue(actual);

        onlineUsers = generateHashMap("admin", "admin1");
        Server.getClients().clear();
        onlineUsers.forEach((k, v) -> {
            Server.addClient(k, v);
        });
        users = generateTreeSet("admin1", "admin2");
        actual = Util.isAnyOnline(users);
        assertTrue(actual);

        onlineUsers = generateHashMap("admin", "admin1");
        Server.getClients().clear();
        onlineUsers.forEach((k, v) -> {
            Server.addClient(k, v);
        });
        users = generateTreeSet("admin2", "admin3");
        actual = Util.isAnyOnline(users);
        assertFalse(actual);
    }

}
