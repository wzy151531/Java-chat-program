package socotra.util;

import org.junit.Test;
import org.junit.Assert;
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
    public void testGenerateChatName1() {
        TreeSet<String> toUsernames = generateTreeSet("admin");
        String expected = "admin";
        String actual = Util.generateChatName(toUsernames);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGenerateChatName2() {
        TreeSet<String> toUsernames = generateTreeSet("admin", "admin1", "admin2");
        String expected = "admin,admin1,admin2";
        String actual = Util.generateChatName(toUsernames);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testIsAnyOnline1() {
        HashMap<String, ObjectOutputStream> onlineUsers = generateHashMap("admin", "admin1", "admin2");
        Server.getClients().clear();
        onlineUsers.forEach((k, v) -> {
            Server.addClient(k, v);
        });
        TreeSet<String> users = generateTreeSet("admin");
        boolean actual = Util.isAnyOnline(users);
        Assert.assertTrue(actual);
    }

    @Test
    public void testIsAnyOnline2() {
        HashMap<String, ObjectOutputStream> onlineUsers = generateHashMap("admin", "admin1");
        Server.getClients().clear();
        onlineUsers.forEach((k, v) -> {
            Server.addClient(k, v);
        });
        TreeSet<String> users = generateTreeSet("admin1", "admin2");
        boolean actual = Util.isAnyOnline(users);
        Assert.assertTrue(actual);
    }

    @Test
    public void testIsAnyOnline3() {
        HashMap<String, ObjectOutputStream> onlineUsers = generateHashMap("admin", "admin1");
        Server.getClients().clear();
        onlineUsers.forEach((k, v) -> {
            Server.addClient(k, v);
        });
        TreeSet<String> users = generateTreeSet("admin2", "admin3");
        boolean actual = Util.isAnyOnline(users);
        Assert.assertFalse(actual);
    }

}
