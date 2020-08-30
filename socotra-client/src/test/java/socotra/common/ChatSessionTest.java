package socotra.common;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import socotra.util.UtilTest;

import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

public class ChatSessionTest {

    private static ChatSession chatSession1;
    private static ChatSession chatSession2;

    @BeforeAll
    public static void init() {
        chatSession1 = new ChatSession(null, true, true, ChatSession.PAIRWISE);
    }

    @Test
    public void testConstructors() {
        chatSession1 = new ChatSession(UtilTest.generateTreeSet("admin", "admin1"), true, true, ChatSession.PAIRWISE);
        chatSession2 = new ChatSession("🔒/admin:1|admin1:1/1");
        boolean actual = chatSession1.equals(chatSession2);
        assertTrue(actual);

        assertThrows(IllegalArgumentException.class, () -> {
            new ChatSession("🔒/admin:1|admin1:1/1/x");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new ChatSession("🔒/admin:1|admin1:1");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new ChatSession("");
        });
    }

    @Test
    public void testGetToMembersAndSetMembers() {
        chatSession1.setMembers(UtilTest.generateTreeSet("admin"));
        TreeSet<User> expected = UtilTest.generateTreeSet("admin");
        TreeSet<User> actual = chatSession1.getMembers();
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
        chatSession1 = new ChatSession(UtilTest.generateTreeSet("admin", "admin1"), true, true, ChatSession.PAIRWISE);
        chatSession2 = new ChatSession(UtilTest.generateTreeSet("admin", "admin1"), false, true, ChatSession.PAIRWISE);
        boolean actual = chatSession1.equals(chatSession2);
        assertTrue(actual);

        chatSession1 = new ChatSession(UtilTest.generateTreeSet("admin", "admin1"), true, true, ChatSession.PAIRWISE);
        chatSession2 = new ChatSession(UtilTest.generateTreeSet("admin1", "admin"), true, true, ChatSession.PAIRWISE);
        actual = chatSession1.equals(chatSession2);
        assertTrue(actual);

        chatSession1 = new ChatSession(UtilTest.generateTreeSet("admin", "admin1"), true, true, ChatSession.PAIRWISE);
        chatSession2 = new ChatSession(UtilTest.generateTreeSet("admin1", "admin"), true, true, ChatSession.GROUP);
        actual = chatSession1.equals(chatSession2);
        assertFalse(actual);

        chatSession1 = new ChatSession(UtilTest.generateTreeSet("admin", "admin1"), true, true, ChatSession.PAIRWISE);
        chatSession2 = new ChatSession(UtilTest.generateTreeSet("admin1", "admin"), true, false, ChatSession.PAIRWISE);
        actual = chatSession1.equals(chatSession2);
        assertFalse(actual);

        chatSession1 = new ChatSession(UtilTest.generateTreeSet("admin", "admin1"), true, true, ChatSession.PAIRWISE);
        chatSession2 = new ChatSession(UtilTest.generateTreeSet("admin2", "admin"), true, true, ChatSession.PAIRWISE);
        actual = chatSession1.equals(chatSession2);
        assertFalse(actual);
    }

    @Test
    public void testRelatedUser() {
        TreeSet<User> members = new TreeSet<>();
        User user1 = new User("admin", 1, true);
        User user2 = new User("admin1", 1, true);
        members.add(user1);
        members.add(user2);
        chatSession1 = new ChatSession(members, true, true, ChatSession.PAIRWISE);
        User user3 = new User("admin", 2, true);
        User actual = chatSession1.relatedUser(user3);
        assertEquals(actual, user1);

        user3 = new User("admin", 1, true);
        actual = chatSession1.relatedUser(user3);
        assertNull(actual);

        user3 = new User("admin2", 1, true);
        actual = chatSession1.relatedUser(user3);
        assertNull(actual);
    }

    @Test
    public void testGetOthers() {
        chatSession1 = new ChatSession(UtilTest.generateTreeSet("admin", "admin1"), true, true, ChatSession.PAIRWISE);
        TreeSet<User> expected = new TreeSet<>();
        User user1 = new User("admin", 1, true);
        expected.add(user1);
        TreeSet<User> actual = chatSession1.getOthers(new User("admin1", 1, true));
        assertEquals(expected, actual);
    }

}
