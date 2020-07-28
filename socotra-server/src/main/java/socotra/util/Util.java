package socotra.util;

import socotra.Server;
import socotra.common.ChatSession;
import socotra.common.ConnectionData;
import socotra.common.User;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

public abstract class Util {

    /**
     * Broadcast connectionData to all connected clients.
     *
     * @param connectionData The connectionData needs to broadcast.
     */
    public synchronized static void broadcast(ConnectionData connectionData) {
        Server.getClients().forEach((k, v) -> {
            try {
                v.writeObject(connectionData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Send special connectionData to certain user and broadcast other same connectionData to others.
     *
     * @param connectionData        The connectionData needs to broadcast to others.
     * @param user                  The certain user needs to send the special connectionData.
     * @param specialConnectionData The special connectionData needs to be sent to the certain user.
     */
    public synchronized static void broadcast(ConnectionData connectionData, User user, ConnectionData specialConnectionData) {
        Server.getClients().forEach((k, v) -> {
            try {
                if (!k.equals(user)) {
                    v.writeObject(connectionData);
                } else {
                    v.writeObject(specialConnectionData);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Broadcast connectionData to all online clients except the certain user.
     *
     * @param connectionData The connectionData needs to broadcast.
     * @param user           The certain user that will not receive the broadcast connectionData.
     */
    public synchronized static void broadcast(ConnectionData connectionData, User user) {
        Server.getClients().forEach((k, v) -> {
            try {
                if (!k.equals(user)) {
                    v.writeObject(connectionData);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Send private connectionData to certain user.
     * <p>
     * Add synchronized to make this function invoked once at same time.
     *
     * @param connectionData The private connectionData.
     * @param receiver       The certain user.
     */
    public synchronized static void privateSend(ConnectionData connectionData, User receiver) {
        try {
            ObjectOutputStream oos = Server.getClients().get(receiver);
            if (oos != null) {
                oos.writeObject(connectionData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send connectionData to a group.
     *
     * @param connectionData The connectionData needs to be sent.
     * @param members        The users in the group.
     */
    public synchronized static void groupSend(ConnectionData connectionData, TreeSet<User> members) {
        ArrayList<User> receivers = new ArrayList<>(members);
        receivers.remove(connectionData.getUserSignature());
        for (User receiver : receivers) {
            HashMap<User, ObjectOutputStream> allClients = Server.getClients();
            ObjectOutputStream oos = allClients.get(receiver);
            if (oos != null) {
                try {
                    oos.writeObject(connectionData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                ChatSession chatSession = connectionData.getChatSession();
                switch (chatSession.getSessionType()) {
                    case ChatSession.PAIRWISE:
                        Server.storePairwiseData(receiver, connectionData);
                        break;
                    case ChatSession.GROUP:
                        Server.storeGroupData(receiver, connectionData);
                        break;
                    default:
                        throw new IllegalStateException("Bad chatSession type.");
                }
            }
        }
    }

    /**
     * Check if any of the given users is online.
     *
     * @param toUsernames The given user names.
     * @return The boolean indicates whether any of the given users is online.
     */
//    public static boolean isAnyOnline(TreeSet<String> toUsernames) {
//        for (String username : toUsernames) {
//            if (Server.getClients().keySet().contains(username)) {
//                return true;
//            }
//        }
//        return false;
//    }

    /**
     * Generate chat session name according to the toUsernames in chatSession.
     *
     * @param toUsernames toUsernames in chatSession.
     * @return The String represent the chatSession.
     */
//    public static String generateChatName(TreeSet<String> toUsernames) {
//        String result = toUsernames.toString().replace(" ", "");
//        return result.substring(1, result.length() - 1);
//    }

    /**
     * Print clientsChatData.
     *
     * @param clientsChatData The chat history data of all clients.
     */
    public static void printClientsChatData(HashMap<String, HashMap<ChatSession, List<ConnectionData>>> clientsChatData) {
        clientsChatData.forEach((k, v) -> {
            System.out.println("============" + k);
            v.forEach((k1, v1) -> {
                System.out.println(k1.getMembers());
                v1.forEach(n -> {
                    System.out.println("    " + n.getTextData());
                });
            });
        });
    }

}
