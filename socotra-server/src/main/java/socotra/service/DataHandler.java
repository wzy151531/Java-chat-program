package socotra.service;

import socotra.Server;
import socotra.common.ConnectionData;
import socotra.common.KeyBundle;
import socotra.common.User;
import socotra.jdbc.JdbcUtil;

import java.io.IOException;
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

    private void processOnline(int type, boolean isSwitch) {
        serverThread.appendClient();
        System.out.println("Validated user. Current online users: " + Server.getClients().keySet());
        User user = serverThread.getUser();
        // user wants to login normally.
        if (user.isActive()) {
            try {
                Sender.broadcast(new ConnectionData(user, true), user);
                if (type == LOGIN) {
                    TreeSet<User> onlineUsers = new TreeSet<>(Server.getClients().keySet());
                    onlineUsers.remove(user);
                    ArrayList<ConnectionData> pairwiseData = Server.loadPairwiseData(user);
                    ArrayList<ConnectionData> senderKeyData = Server.loadSenderKeyData(user);
                    ArrayList<ConnectionData> groupData = Server.loadGroupData(user);
                    serverThread.inform(new ConnectionData(onlineUsers, pairwiseData, senderKeyData, groupData));
                }

                if (isSwitch) {
                    processSwitchDevice(user);
                }
                // Inform the new client current online users and inform other clients that the new client is online.
//                boolean isActive = JdbcUtil.isActive(user);
//                boolean isFresh = JdbcUtil.isFresh(user);
//                if (!isActive && !isFresh) {
//                    processSwitchDevice(user);
//                }
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
            boolean isSwitch = JdbcUtil.validateUser(user, password);
            processOnline(LOGIN, isSwitch);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            try {
                serverThread.inform(new ConnectionData(false));
            } catch (IOException e1) {
                e.printStackTrace();
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

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
            case 2:
            case 7:
                connectionData.setIsSent(true);
                System.out.println("Rece data to: " + connectionData.getChatSession().getMembers());
                Sender.groupSend(connectionData, connectionData.getChatSession().getMembers());
                break;
            case 4:
                try {
                    boolean isFresh = serverThread.processSignUp(connectionData);
                    serverThread.setUser(connectionData.getUser());
                    processOnline(SIGNUP, !isFresh);
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                    return false;
                }

//                if (!serverThread.processSignUp(connectionData)) {
//                    return false;
//                }
//                serverThread.setUser(connectionData.getUser());
//                processOnline(SIGNUP);
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
