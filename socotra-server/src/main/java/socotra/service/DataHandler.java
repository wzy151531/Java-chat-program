package socotra.service;

import socotra.Server;
import socotra.common.ChatSession;
import socotra.common.ConnectionData;
import socotra.common.KeyBundle;
import socotra.jdbc.JdbcUtil;
import socotra.util.Util;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

class DataHandler {

    private final ServerThread serverThread;

    DataHandler(ServerThread serverThread) {
        this.serverThread = serverThread;
    }

    private void processOnline() {
        serverThread.appendClient();
        System.out.println("Validated user. Current online users: " + Server.getClients().keySet());
        HashMap<ChatSession, List<ConnectionData>> chatData = JdbcUtil.getCertainChatData(serverThread.getUsername());
        if (chatData != null) {
            Util.privateSend(new ConnectionData(chatData, "server"), serverThread.getUsername());
        }
        TreeSet<String> allClientsName = new TreeSet<>(Server.getClients().keySet());
        allClientsName.remove(serverThread.getUsername());
        // Inform the new client current online users and inform other clients that the new client is online.
        Util.broadcast(new ConnectionData(serverThread.getUsername(), true), serverThread.getUsername(), new ConnectionData(allClientsName));
    }

    private boolean processLogin(String username, String password) {
        serverThread.setUsername(username);
        try {
            if (!JdbcUtil.validateUser(username, password)) {
                System.out.println("Invalidated user.");
                serverThread.inform(new ConnectionData(false));
                return false;
            } else {
                serverThread.inform(new ConnectionData(true));
                processOnline();
                processDepositData(username);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private void processDepositData(String username) {
        ArrayList<ConnectionData> pairwiseData = Server.loadPairwiseData(username);
        ArrayList<ConnectionData> senderKeyData = Server.loadSenderKeyData(username);
        ArrayList<ConnectionData> groupData = Server.loadGroupData(username);
        try {
            serverThread.inform(new ConnectionData(pairwiseData, senderKeyData, groupData));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean processLogout() {
        Util.broadcast(new ConnectionData(serverThread.getUsername(), false), serverThread.getUsername());
        Server.removeClient(serverThread.getUsername());
        System.out.println("User log out. Current online users: " + Server.getClients().keySet());
        return false;
    }

    boolean handle(ConnectionData connectionData) {
        switch (connectionData.getType()) {
            // If connection data is about login.
            case 0:
                if (!processLogin(connectionData.getUsername(), connectionData.getPassword())) {
                    return false;
                }
                break;
            // If connection data is about logout.
            case -2:
                return processLogout();
            // If connection data is about received hint.
            case -4:
                Util.privateSend(connectionData, connectionData.getReceiverUsername());
                break;
            // If connection data is about normal chat message.
            case 1:
                connectionData.setIsSent(true);
                // Once received the text data, use a new thread to insert it into database.
                JdbcUtil.insertClientChatData(connectionData);
            case 2:
            case 7:
                connectionData.setIsSent(true);
                Util.groupSend(connectionData, connectionData.getChatSession().getToUsernames());
                break;
            // If connection data is about store chat history.
            case 3:
//                        JdbcUtil.updateClientsChatData(connectionData.getUserSignature(), connectionData.getChatData());
                break;
            case 4:
                if (!serverThread.processSignUp(connectionData)) {
                    return false;
                }
                serverThread.setUsername(connectionData.getUsername());
                processOnline();
                break;
            case 5:
                try {
                    KeyBundle keyBundle = JdbcUtil.queryKeyBundle(connectionData.getReceiverUsername());
                    Util.privateSend(new ConnectionData(keyBundle, connectionData.getReceiverUsername()), connectionData.getUserSignature());
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
        HashMap<String, ConnectionData> senderKeysData = connectionData.getSenderKeysData();
        senderKeysData.forEach((k, v) -> {
            if (Server.getClients().containsKey(k)) {
                Util.privateSend(v, k);
            } else {
                Server.storeSenderKeyData(k, v);
            }
        });
    }

    private void processQueryKeyBundles(ConnectionData connectionData) {
        HashMap<String, KeyBundle> result = new HashMap<>();
        TreeSet<String> receiversUsername = connectionData.getReceiversUsername();
        receiversUsername.forEach(n -> {
            try {
                KeyBundle keyBundle = JdbcUtil.queryKeyBundle(n);
                result.put(n, keyBundle);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        Util.privateSend(new ConnectionData(result, connectionData.getChatSession(), connectionData.getNeedDistribute(), connectionData.isInit()), connectionData.getUserSignature());
    }

}
