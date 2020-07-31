package socotra.service;

import socotra.Server;
import socotra.common.ChatSession;
import socotra.common.ConnectionData;
import socotra.common.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

public abstract class Sender {

    /**
     * Broadcast connectionData to all connected clients.
     *
     * @param connectionData The connectionData needs to broadcast.
     */
    static void broadcast(ConnectionData connectionData) {
        Server.getClients().forEach((k, v) -> {
            v.sendMsg(connectionData);
        });
    }

    /**
     * Send special connectionData to certain user and broadcast other same connectionData to others.
     *
     * @param connectionData        The connectionData needs to broadcast to others.
     * @param user                  The certain user needs to send the special connectionData.
     * @param specialConnectionData The special connectionData needs to be sent to the certain user.
     */
    static void broadcast(ConnectionData connectionData, User user, ConnectionData specialConnectionData) {
        Server.getClients().forEach((k, v) -> {
            if (!k.equals(user)) {
                v.sendMsg(connectionData);
            } else {
                v.sendMsg(specialConnectionData);
            }
        });
    }

    /**
     * Broadcast connectionData to all online clients except the certain user.
     *
     * @param connectionData The connectionData needs to broadcast.
     * @param user           The certain user that will not receive the broadcast connectionData.
     */
    static void broadcast(ConnectionData connectionData, User user) {
        Server.getClients().forEach((k, v) -> {
            if (!k.equals(user)) {
                v.sendMsg(connectionData);
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
    static void privateSend(ConnectionData connectionData, User receiver) {
        OutputHandler oh = Server.getClients().get(receiver);
        if (oh != null) {
            oh.sendMsg(connectionData);
        }
    }

    /**
     * Send connectionData to a group.
     *
     * @param connectionData The connectionData needs to be sent.
     * @param members        The users in the group.
     */
    static void groupSend(ConnectionData connectionData, TreeSet<User> members) {
        ArrayList<User> receivers = new ArrayList<>(members);
        receivers.remove(connectionData.getUserSignature());
        for (User receiver : receivers) {
            HashMap<User, OutputHandler> allClients = Server.getClients();
            OutputHandler oh = allClients.get(receiver);
            if (oh != null) {
                oh.sendMsg(connectionData);
            } else {
                ChatSession chatSession = connectionData.getChatSession();
                System.out.println(receiver + " is not online currently.");
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

}
