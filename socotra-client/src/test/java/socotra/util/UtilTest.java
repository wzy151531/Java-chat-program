package socotra.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
    public void testIsEmpty() {
        boolean actual = Util.isEmpty("");
        assertTrue(actual);

        actual = Util.isEmpty(null);
        assertTrue(actual);

        actual = Util.isEmpty("  ");
        assertTrue(actual);

        actual = Util.isEmpty(" 2 ");
        assertFalse(actual);
    }

    @Test
    public void testGenerateChatName() {
        TreeSet<String> toUsernames = generateTreeSet("admin");
        String expected = "admin";
        String actual = Util.generateChatName(toUsernames);
        assertEquals(expected, actual);

        toUsernames = generateTreeSet("admin", "admin1", "admin2");
        expected = "admin, admin1, admin2";
        actual = Util.generateChatName(toUsernames);
        assertEquals(expected, actual);
    }

}
