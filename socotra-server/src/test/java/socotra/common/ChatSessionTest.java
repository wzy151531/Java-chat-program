package socotra.common;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import socotra.util.SenderTest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.TreeSet;

public class ChatSessionTest {

    private static ChatSession chatSession1;
    private static ChatSession chatSession2;

    @BeforeAll
    public static void init() {
        chatSession1 = new ChatSession(null, true);
    }

    @Test
    public void testGetToUsernamesAndSetToUsernames() {
        chatSession1.setMembers(SenderTest.generateTreeSet("admin"));
        TreeSet<String> expected = SenderTest.generateTreeSet("admin");
        TreeSet<String> actual = chatSession1.getMembers();
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
        chatSession1 = new ChatSession(SenderTest.generateTreeSet("admin", "admin1"), true);
        chatSession2 = new ChatSession(SenderTest.generateTreeSet("admin", "admin1"), false);
        boolean actual = chatSession1.equals(chatSession2);
        assertTrue(actual);

        chatSession1 = new ChatSession(SenderTest.generateTreeSet("admin", "admin1"), true);
        chatSession2 = new ChatSession(SenderTest.generateTreeSet("admin1", "admin"), true);
        actual = chatSession1.equals(chatSession2);
        assertTrue(actual);

        chatSession1 = new ChatSession(SenderTest.generateTreeSet("admin", "admin1"), true);
        chatSession2 = new ChatSession(SenderTest.generateTreeSet("admin2", "admin"), true);
        actual = chatSession1.equals(chatSession2);
        assertFalse(actual);
    }

}
