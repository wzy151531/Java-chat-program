package socotra.jdbc;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import socotra.common.User;
import socotra.util.SenderTest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.TreeSet;

public class JdbcSenderTest {

    private static User user1, user2;

    @BeforeAll
    public static void init() throws Exception {
        user1 = new User("admin", 1, true);
        user2 = new User("testInsert", 1, true);
        JdbcUtil.init();
        JdbcUtil.connect();
    }

    @AfterAll
    public static void finalHandle() throws Exception {
        JdbcUtil.end();
    }

    private void deleteFromTestUser(User user) throws Exception {
        JdbcUtil.insert("delete from users where username='" + user.getUsername() + "'");
    }

    @Test
    public void testValidateUser() throws Exception {
        JdbcUtil.insert("insert into users(username, password, deviceid, active) values ('admin', 'admin', 1, true)");

        boolean actual = JdbcUtil.validateUser(user1, "admin");
        assertFalse(actual);

        assertThrows(IllegalArgumentException.class, () -> {
            JdbcUtil.validateUser(user1, "admin2");
        });

        deleteFromTestUser(user1);
    }

    @Test
    public void testInsert() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            JdbcUtil.validateUser(user2, "testInsert");
        });
        JdbcUtil.insert("insert into users(username, password, deviceid, active) values ('testInsert', 'testInsert', 1, true)");
        boolean actual = JdbcUtil.validateUser(user2, "testInsert");
        assertFalse(actual);
        deleteFromTestUser(user2);
    }

}
