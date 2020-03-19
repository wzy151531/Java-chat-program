package socotra.jdbc;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import socotra.util.UtilTest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.TreeSet;

public class JdbcUtilTest {

    @BeforeAll
    public static void init() throws Exception {
        JdbcUtil.init();
        JdbcUtil.connect();
    }

    @AfterAll
    public static void finalHandle() throws Exception {
        JdbcUtil.end();
    }

    private void deleteFromTestUser(String username) throws Exception {
        JdbcUtil.insert("delete from test_user where username='" + username + "'");
    }

    @Test
    public void testValidateUser() throws Exception {
        boolean actual = JdbcUtil.validateUser("admin", "admin");
        assertTrue(actual);

        actual = JdbcUtil.validateUser("admin1", "admin2");
        assertFalse(actual);
    }

    @Test
    public void testGenerateSessionMembers() {
        TreeSet<String> expected = UtilTest.generateTreeSet("admin", "admin1");
        TreeSet<String> actual = JdbcUtil.generateSessionMembers("admin", "admin1");
        assertEquals(expected, actual);

        expected = UtilTest.generateTreeSet("admin", "admin1", "admin2");
        actual = JdbcUtil.generateSessionMembers("admin,admin1", "admin2");
        assertEquals(expected, actual);

        actual = JdbcUtil.generateSessionMembers("admin,admin2", "admin1");
        assertEquals(expected, actual);

        actual = JdbcUtil.generateSessionMembers("admin1,admin2", "admin");
        assertEquals(expected, actual);

        expected = UtilTest.generateTreeSet("all");
        actual = JdbcUtil.generateSessionMembers("all", "admin");
        assertEquals(expected, actual);
    }

    @Test
    public void testInsert() throws Exception {
        boolean actual = JdbcUtil.validateUser("testInsert", "testInsert");
        assertFalse(actual);
        JdbcUtil.insert("insert into test_user(username, password) values ('testInsert', 'testInsert')");
        actual = JdbcUtil.validateUser("testInsert", "testInsert");
        assertTrue(actual);
        deleteFromTestUser("testInsert");
    }

}
