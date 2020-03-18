package socotra;

import org.junit.Assert;
import org.junit.Test;
import socotra.util.UtilTest;

import java.io.ObjectOutputStream;
import java.util.HashMap;

public class ServerTest {

    @Test
    public void testGetClient1() {
        HashMap<String, ObjectOutputStream> expected = UtilTest.generateHashMap("admin", "admin1", "admin2");
        Server.getClients().clear();
        expected.forEach((k, v) -> {
            Server.addClient(k, v);
        });
        HashMap<String, ObjectOutputStream> actual = Server.getClients();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetClient2() {
        HashMap<String, ObjectOutputStream> expected = UtilTest.generateHashMap("admin");
        Server.getClients().clear();
        expected.forEach((k, v) -> {
            Server.addClient(k, v);
        });
        HashMap<String, ObjectOutputStream> actual = Server.getClients();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testAddClient1() {
        HashMap<String, ObjectOutputStream> expected = UtilTest.generateHashMap("admin", "admin1");
        Server.getClients().clear();
        Server.getClients().put("admin", null);
        Server.addClient("admin1", null);
        HashMap<String, ObjectOutputStream> actual = Server.getClients();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testAddClient2() {
        HashMap<String, ObjectOutputStream> expected = UtilTest.generateHashMap("admin", "admin1");
        Server.getClients().clear();
        Server.getClients().put("admin", null);
        Server.getClients().put("admin1", null);
        Server.addClient("admin1", null);
        HashMap<String, ObjectOutputStream> actual = Server.getClients();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testRemoveClient1() {
        HashMap<String, ObjectOutputStream> expected = UtilTest.generateHashMap("admin", "admin1");
        Server.getClients().clear();
        expected.forEach((k, v) -> {
            Server.addClient(k, v);
        });
        Server.addClient("admin2", null);
        Server.removeClient("admin2");
        HashMap<String, ObjectOutputStream> actual = Server.getClients();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testRemoveClient2() {
        HashMap<String, ObjectOutputStream> expected = UtilTest.generateHashMap("admin");
        Server.getClients().clear();
        expected.forEach((k, v) -> {
            Server.addClient(k, v);
        });
        Server.removeClient("admin1");
        HashMap<String, ObjectOutputStream> actual = Server.getClients();
        Assert.assertEquals(expected, actual);
    }

}
