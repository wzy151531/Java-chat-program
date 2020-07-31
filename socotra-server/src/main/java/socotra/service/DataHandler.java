package socotra.service;

import socotra.Server;
import socotra.common.ConnectionData;
import socotra.common.KeyBundle;
import socotra.common.User;
import socotra.jdbc.JdbcUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

class DataHandler {

    private static final int LOGIN = 1;
    private static final int SIGNUP = 2;
    private final ServerThread serverThread;


    DataHandler(ServerThread serverThread) {
        this.serverThread = serverThread;
    }

    private void processOnline(int type) {
        serverThread.appendClient();
        System.out.println("Validated user. Current online users: " + Server.getClients().keySet());
        User user = serverThread.getUser();
//        HashMap<ChatSession, List<ConnectionData>> chatData = JdbcUtil.getCertainChatData(user);
//        if (chatData != null) {
//            Sender.privateSend(new ConnectionData(chatData, "server"), user);
//        }
        // user wants to login normally.
        if (user.isActive()) {
            try {
                if (type == LOGIN) {
                    TreeSet<User> onlineUsers = new TreeSet<>(Server.getClients().keySet());
                    onlineUsers.remove(user);
                    ArrayList<ConnectionData> pairwiseData = Server.loadPairwiseData(user);
                    ArrayList<ConnectionData> senderKeyData = Server.loadSenderKeyData(user);
                    ArrayList<ConnectionData> groupData = Server.loadGroupData(user);
                    serverThread.inform(new ConnectionData(onlineUsers, pairwiseData, senderKeyData, groupData));
                }

                // Inform the new client current online users and inform other clients that the new client is online.
                Sender.broadcast(new ConnectionData(user, true), user);
                boolean isActive = JdbcUtil.isActive(user);
                boolean isFresh = JdbcUtil.isFresh(user);
                if (!isActive && !isFresh) {
                    processSwitchDevice(user);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void processSwitchDevice(User user) {
        System.out.println("Switch device: " + user);
        Sender.broadcast(new ConnectionData(user), user, new ConnectionData(14, true));
    }

    private boolean processLogin(User user, String password) {
        serverThread.setUser(user);
        try {
            if (!JdbcUtil.validateUser(user, password)) {
                System.out.println("Invalidated user.");
                serverThread.inform(new ConnectionData(false));
                return false;
            } else {
                processOnline(LOGIN);
//                processDepositData(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

//    private void processDepositData(User user) {
//        ArrayList<ConnectionData> pairwiseData = Server.loadPairwiseData(user);
//        ArrayList<ConnectionData> senderKeyData = Server.loadSenderKeyData(user);
//        ArrayList<ConnectionData> groupData = Server.loadGroupData(user);
//        try {
//            serverThread.inform(new ConnectionData(pairwiseData, senderKeyData, groupData));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private boolean processLogout() {
        User user = serverThread.getUser();
        Sender.broadcast(new ConnectionData(user, false), user);
        Server.removeClient(user);
        System.out.println("User log out. Current online users: " + Server.getClients().keySet());
        return false;
    }

    boolean handle(ConnectionData connectionData) {
        switch (connectionData.getType()) {
            // If connection data is about login.
            case 0:
                if (!processLogin(connectionData.getUser(), connectionData.getPassword())) {
                    return false;
                }
                break;
            // If connection data is about logout.
            case -2:
                return processLogout();
            // If connection data is about received hint.
            case -4:
                Sender.privateSend(connectionData, connectionData.getReceiverUsername());
                break;
            // If connection data is about normal chat message.
//            case 1:
//                connectionData.setIsSent(true);
//                // Once received the text data, use a new thread to insert it into database.
//                JdbcUtil.insertClientChatData(connectionData);
            case 2:
            case 7:
                connectionData.setIsSent(true);
                System.out.println("Rece data to: " + connectionData.getChatSession().getMembers());
                Sender.groupSend(connectionData, connectionData.getChatSession().getMembers());
                break;
            // If connection data is about store chat history.
            case 3:
//                        JdbcUtil.updateClientsChatData(connectionData.getUserSignature(), connectionData.getChatData());
                break;
            case 4:
                if (!serverThread.processSignUp(connectionData)) {
                    return false;
                }
                serverThread.setUser(connectionData.getUser());
                processOnline(SIGNUP);
                break;
            case 5:
                System.out.println("receiver query keyBundle request.");
                try {
                    KeyBundle keyBundle = JdbcUtil.queryKeyBundle(connectionData.getReceiverUsername());
                    Sender.privateSend(new ConnectionData(keyBundle, connectionData.getReceiverUsername(), connectionData.isReInit()), connectionData.getUserSignature());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            case 9:
                processQueryKeyBundles(connectionData);
                break;
            case 11:
                processDistributeSenderKey(connectionData);
                break;
            default:
                System.out.println("Unknown data.");
        }
        return true;
    }

    private void processDistributeSenderKey(ConnectionData connectionData) {
        HashMap<User, ConnectionData> senderKeysData = connectionData.getSenderKeysData();
        senderKeysData.forEach((k, v) -> {
            if (Server.getClients().containsKey(k)) {
                Sender.privateSend(v, k);
            } else {
                Server.storeSenderKeyData(k, v);
            }
        });
    }

    private void processQueryKeyBundles(ConnectionData connectionData) {
        HashMap<User, KeyBundle> result = new HashMap<>();
        TreeSet<User> receiversUsername = connectionData.getReceiversUsername();
        receiversUsername.forEach(n -> {
            try {
                KeyBundle keyBundle = JdbcUtil.queryKeyBundle(n);
                result.put(n, keyBundle);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        Sender.privateSend(new ConnectionData(result, connectionData.getChatSession(), connectionData.isInit()), connectionData.getUserSignature());
    }

}
