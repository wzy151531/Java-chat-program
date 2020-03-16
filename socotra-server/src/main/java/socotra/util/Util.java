package socotra.util;

import socotra.Server;
import socotra.common.ChatSession;
import socotra.common.ConnectionData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

public abstract class Util {

    public synchronized static void broadcast(ConnectionData connectionData) {
        Server.getClients().forEach((k, v) -> {
            try {
                v.writeObject(connectionData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public synchronized static void broadcast(ConnectionData connectionData, String username, ConnectionData specialConnectionData) {
        Server.getClients().forEach((k, v) -> {
            try {
                if (!k.equals(username)) {
                    v.writeObject(connectionData);
                } else {
                    v.writeObject(specialConnectionData);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public synchronized static void broadcast(ConnectionData connectionData, String username) {
        Server.getClients().forEach((k, v) -> {
            try {
                if (!k.equals(username)) {
                    v.writeObject(connectionData);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void privateSend(ConnectionData connectionData, String toUsername) {
        try {
            Server.getClients().get(toUsername).writeObject(connectionData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void groupSend(ConnectionData connectionData, TreeSet<String> toUsernames) {
        ArrayList<String> clients = new ArrayList<>(toUsernames);
        clients.remove(connectionData.getUserSignature());
//        System.out.println(clients);
        for (String toUsername : clients) {
            Server.getClients().forEach((k, v) -> {
                try {
                    if (k.equals(toUsername)) {
                        v.writeObject(connectionData);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public static boolean isAnyOnline(TreeSet<String> toUsernames) {
        for (String username : toUsernames) {
            if (Server.getClients().keySet().contains(username)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Generate chat session name according to the toUsernames in chatSession.
     *
     * @param toUsernames toUsernames in chatSession.
     * @return The String represent the chatSession.
     */
    public static String generateChatName(TreeSet<String> toUsernames) {
        String result = toUsernames.toString().replace(" ", "");
        return result.substring(1, result.length() - 1);
    }

    public static void printClientsChatData(HashMap<String, HashMap<ChatSession, List<ConnectionData>>> clientsChatData) {
        clientsChatData.forEach((k, v) -> {
            System.out.println("============" + k);
            v.forEach((k1, v1) -> {
                System.out.println(k1.getToUsernames());
                v1.forEach(n -> {
                    System.out.println("    " + n.getTextData());
                });
            });
        });
    }

}
