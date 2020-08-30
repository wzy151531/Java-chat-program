package socotra.util;

import socotra.common.User;
import socotra.service.OutputHandler;

import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.TreeSet;

public class SenderTest {

    public static TreeSet<User> generateTreeSet(String... toUsernames) {
        TreeSet<User> result = new TreeSet<>();
        for (String toUsername : toUsernames) {
            result.add(new User(toUsername, 1, true));
        }
        return result;
    }

    public static HashMap<User, OutputHandler> generateHashMap(String... clients) {
        HashMap<User, OutputHandler> result = new HashMap<>();
        for (String client : clients) {
            result.put(new User(client, 1, true), null);
        }
        return result;
    }

}
