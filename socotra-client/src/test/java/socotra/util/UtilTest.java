package socotra.util;

import org.junit.jupiter.api.Test;
import socotra.common.User;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.TreeSet;

public class UtilTest {

    public static TreeSet<User> generateTreeSet(String... toUsernames) {
        TreeSet<User> result = new TreeSet<>();
        for (String toUsername : toUsernames) {
            result.add(new User(toUsername, 1, true));
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

}
