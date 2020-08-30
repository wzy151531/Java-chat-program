package socotra;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import socotra.common.ChatSession;
import socotra.common.User;
import socotra.service.OutputHandler;
import socotra.util.SenderTest;

import java.io.ObjectOutputStream;
import java.util.HashMap;

public class ServerTest {

    private static User user1, user2, user3;

    @BeforeAll
    public static void init() {
        user1 = new User("admin", 1, true);
        user2 = new User("admin1", 1, true);
        user3 = new User("admin2", 1, true);
    }

    @Test
    public void testGetClient() {
        HashMap<User, OutputHandler> expected = SenderTest.generateHashMap("admin", "admin1", "admin2");
        Server.getClients().clear();
        expected.forEach((k, v) -> {
            Server.addClient(k, v);
        });
        HashMap<User, OutputHandler> actual = Server.getClients();
        assertEquals(expected, actual);

        expected = SenderTest.generateHashMap("admin");
        Server.getClients().clear();
        expected.forEach((k, v) -> {
            Server.addClient(k, v);
        });
        actual = Server.getClients();
        assertEquals(expected, actual);
    }

    @Test
    public void testAddClient() {
        HashMap<User, OutputHandler> expected = SenderTest.generateHashMap("admin", "admin1");
        Server.getClients().clear();
        Server.getClients().put(user1, null);
        Server.addClient(user2, null);
        HashMap<User, OutputHandler> actual = Server.getClients();
        assertEquals(expected, actual);

        expected = SenderTest.generateHashMap("admin", "admin1");
        Server.getClients().clear();
        Server.getClients().put(user1, null);
        Server.getClients().put(user2, null);
        Server.addClient(user2, null);
        actual = Server.getClients();
        assertEquals(expected, actual);
    }

    @Test
    public void testRemoveClient() {
        HashMap<User, OutputHandler> expected = SenderTest.generateHashMap("admin", "admin1");
        Server.getClients().clear();
        expected.forEach((k, v) -> {
            Server.addClient(k, v);
        });
        Server.addClient(user3, null);
        Server.removeClient(user3);
        HashMap<User, OutputHandler> actual = Server.getClients();
        assertEquals(expected, actual);

        expected = SenderTest.generateHashMap("admin");
        Server.getClients().clear();
        expected.forEach((k, v) -> {
            Server.addClient(k, v);
        });
        Server.removeClient(user2);
        actual = Server.getClients();
        assertEquals(expected, actual);
    }

}
