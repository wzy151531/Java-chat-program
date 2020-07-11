package socotra.common;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import socotra.util.UtilTest;

import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

public class ChatSessionTest {

    private static ChatSession chatSession1;
    private static ChatSession chatSession2;

    @BeforeAll
    public static void init() {
        chatSession1 = new ChatSession(null, true, true);
    }

    @Test
    public void testGetToUsernamesAndSetToUsernames() {
        chatSession1.setToUsernames(UtilTest.generateTreeSet("admin"));
        TreeSet<String> expected = UtilTest.generateTreeSet("admin");
        TreeSet<String> actual = chatSession1.getToUsernames();
        assertEquals(expected, actual);
    }

    @Test
    public void testIsHintAndSetHint() {
        chatSession1.setHint(false);
        boolean actual = chatSession1.isHint();
        assertFalse(actual);
    }

    @Test
    public void testEquals() {
        chatSession1 = new ChatSession(UtilTest.generateTreeSet("admin", "admin1"), true, true);
        chatSession2 = new ChatSession(UtilTest.generateTreeSet("admin", "admin1"), false, true);
        boolean actual = chatSession1.equals(chatSession2);
        assertTrue(actual);

        chatSession1 = new ChatSession(UtilTest.generateTreeSet("admin", "admin1"), true, true);
        chatSession2 = new ChatSession(UtilTest.generateTreeSet("admin1", "admin"), true, true);
        actual = chatSession1.equals(chatSession2);
        assertTrue(actual);

        chatSession1 = new ChatSession(UtilTest.generateTreeSet("admin", "admin1"), true, true);
        chatSession2 = new ChatSession(UtilTest.generateTreeSet("admin2", "admin"), true, true);
        actual = chatSession1.equals(chatSession2);
        assertFalse(actual);
    }

}
