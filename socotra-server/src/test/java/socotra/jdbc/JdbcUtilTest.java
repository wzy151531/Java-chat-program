package socotra.jdbc;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import socotra.util.Util;
import socotra.util.UtilTest;

import java.util.TreeSet;

public class JdbcUtilTest {

    @Before
    public void init() throws Exception {
        JdbcUtil.init();
        JdbcUtil.connect();
    }

    @After
    public void finalHandle() throws Exception {
        JdbcUtil.end();
    }

    @Test
    public void testValidateUser1() throws Exception {
        boolean actual = JdbcUtil.validateUser("admin", "admin");
        Assert.assertTrue(actual);
    }

    @Test
    public void testValidateUser2() throws Exception {
        boolean actual = JdbcUtil.validateUser("admin1", "admin2");
        Assert.assertFalse(actual);
    }

    @Test
    public void testGenerateSessionMembers1() {
        TreeSet<String> expected = UtilTest.generateTreeSet("admin", "admin1");
        TreeSet<String> actual = JdbcUtil.generateSessionMembers("admin", "admin1");
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGenerateSessionMembers2() {
        TreeSet<String> expected = UtilTest.generateTreeSet("admin", "admin1", "admin2");
        TreeSet<String> actual1 = JdbcUtil.generateSessionMembers("admin,admin1", "admin2");
        TreeSet<String> actual2 = JdbcUtil.generateSessionMembers("admin,admin2", "admin1");
        TreeSet<String> actual3 = JdbcUtil.generateSessionMembers("admin1,admin2", "admin");
        Assert.assertEquals(expected, actual1);
        Assert.assertEquals(expected, actual2);
        Assert.assertEquals(expected, actual3);
    }

    @Test
    public void testGenerateSessionMembers3() {
        TreeSet<String> expected = UtilTest.generateTreeSet("all");
        TreeSet<String> actual = JdbcUtil.generateSessionMembers("all", "admin");
        Assert.assertEquals(expected, actual);
    }

}
