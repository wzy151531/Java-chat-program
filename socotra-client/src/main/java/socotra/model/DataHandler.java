package socotra.model;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import socotra.Client;
import socotra.common.ChatSession;
import socotra.common.ConnectionData;
import socotra.common.KeyBundle;
import socotra.common.User;
import socotra.controller.ControllerUtil;
import socotra.protocol.EncryptedClient;
import socotra.protocol.EncryptionHandler;
import socotra.util.SendThread;
import socotra.util.SetOnlineUsers;
import socotra.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

public class DataHandler {

    private ArrayList<ConnectionData> pairwiseData;
    private ArrayList<ConnectionData> senderKeysData;
    private ArrayList<ConnectionData> groupData;

    DataHandler() {
        Client.setDataHandler(this);
    }

    private void initClient(ConnectionData connectionData) {
        Client.showInitClientAlert();
        ControllerUtil controllerUtil = new ControllerUtil();
        controllerUtil.loadHomePage();
        Client.getLoginModel().loadChatData();

        processDepositData(connectionData);
        processOnlineUsers(connectionData.getOnlineUsers());
        processPairwiseData();
        processSenderKeyData();
        Client.closeInitClientAlert();
    }

    private void processOnlineUsers(TreeSet<User> onlineUsers) {
        SetOnlineUsers setOnlineUsers = new SetOnlineUsers(onlineUsers);
        Client.setSetOnlineUsers(setOnlineUsers);
        setOnlineUsers.start();
    }

    boolean handle(ConnectionData connectionData) {
        switch (connectionData.getType()) {
            // If connectionData is about the result of user's validation.
            case -1:
                if (!connectionData.getValidated()) {
                    Platform.runLater(() -> {
                        Client.closeWaitingAlert();
                        Util.generateAlert(Alert.AlertType.ERROR, "Validation Error", "Invalidated user.", "Try again.").show();
                    });
                    return false;
                } else {
                    Client.getLoginModel().loadStores();
                    Platform.runLater(() -> {
                        Client.closeWaitingAlert();
                        initClient(connectionData);
                    });
                }
                break;
            // If connectionData is about users online information.
            case -2:
                User user = connectionData.getUser();
                System.out.println(user + " is " + (connectionData.getIsOnline() ? "online" : "offline"));
                if (connectionData.getIsOnline() && !Client.getHomeModel().clientsContains(user)) {
                    Client.getHomeModel().appendClientsList(user);
                }
//                } else if (!connectionData.getIsOnline() && Client.getHomeModel().clientsContains(clientName)) {
//                    Client.getHomeModel().removeClientsList(clientName);
//                }
                break;
            // If connectionData is about set online users.
//            case -3:
////                System.out.println(connectionData.getOnlineUsers());
//                SetOnlineUsers setOnlineUsers = new SetOnlineUsers(connectionData.getOnlineUsers());
//                Client.setSetOnlineUsers(setOnlineUsers);
//                setOnlineUsers.start();
//                break;
            // If connectionData is about received hint.
            case -4:
                Client.getHomeModel().updateChatData(connectionData.getUuid(), connectionData.getChatSession());
                break;
            // If connectionData is about sign up result.
            case -5:
                if (!connectionData.getSignUpSuccess()) {
                    Platform.runLater(() -> {
                        Client.closeWaitingAlert();
                        Util.generateAlert(Alert.AlertType.ERROR, "Validation Error", "User Already Exists.", "Try another username.").show();
                    });
                    return false;
                } else {
                    Client.getSignUpModel().saveStores();
                    ControllerUtil controllerUtil = new ControllerUtil();
                    Platform.runLater(() -> {
                        Client.closeWaitingAlert();
                        controllerUtil.loadHomePage();
                        processOnlineUsers(connectionData.getOnlineUsers());
                    });
                }
                break;
            // If connectionData is about normal chat messages.
            case 1:
            case 2:
            case 7:
                handleChatMessage(connectionData);
                break;
            // If connectionData is about chat history data.
//            case 3:
//                SetChatData setChatData = new SetChatData(connectionData.getChatData());
//                Client.setSetChatData(setChatData);
//                setChatData.start();
//                break;
            // If connectionData is about receiver's key bundle.
            case 6:
                try {
                    EncryptedClient encryptedClient = Client.getEncryptedClient();
                    User receiver = connectionData.getReceiverUsername();
                    encryptedClient.initPairwiseChat(connectionData.getKeyBundle(), receiver);
                    encryptedClient.finishInitPairwiseChat(receiver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 8:
                processReceSenderKey(connectionData, false);
                break;
            case 10:
                processReceKeyBundles(connectionData);
                break;
//            case 12:
//                processDepositData(connectionData);
//                break;
            default:
                System.out.println("Unknown data.");
        }
        return true;
    }

    private void processDepositData(ConnectionData connectionData) {
        System.out.println("receive deposit data.");
        this.pairwiseData = connectionData.getDepositPairwiseData();
        this.senderKeysData = connectionData.getDepositSenderKeyData();
        this.groupData = connectionData.getDepositGroupData();
    }

    private void processPairwiseData() {
        System.out.println("process pairwise data.");
        if (pairwiseData == null) return;
        pairwiseData.forEach(n -> {
            System.out.println("receive pairwise data: " + n.getType());
            handleChatMessage(n);
        });
    }

    private void processSenderKeyData() {
        System.out.println("process senderKey data.");
        if (senderKeysData == null) {
            processGroupData();
            return;
        }
        ;
        System.out.println("SenderKey isn't null");
        senderKeysData.forEach(n -> {
            processReceSenderKey(n, true);
        });
    }

    public void processGroupData() {
        System.out.println("process group data.");
        if (groupData == null) return;
        groupData.forEach(n -> {
            handleChatMessage(n);
        });
        groupData = null;
    }

    public boolean isGroupDataNull() {
        return groupData == null;
    }

    private void processReceSenderKey(ConnectionData connectionData, boolean init) {
        try {
            byte[] senderKey = EncryptionHandler.decryptSKDMData(connectionData);
            EncryptedClient encryptedClient = Client.getEncryptedClient();
            encryptedClient.processReceivedSenderKey(senderKey, connectionData.getChatSession(), connectionData.getUserSignature(), init);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processReceKeyBundles(ConnectionData connectionData) {
        EncryptedClient encryptedClient = Client.getEncryptedClient();
        HashMap<User, KeyBundle> keyBundles = connectionData.getKeyBundles();
        keyBundles.forEach((k, v) -> {
            try {
                encryptedClient.initPairwiseChat(v, k);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        User caller = Client.getClientThread().getUser();
        ChatSession chatSession = connectionData.getChatSession();
        encryptedClient.distributeSenderKey(chatSession.getOthers(caller), chatSession, encryptedClient.getSKDM(), connectionData.isInit());
    }

    private void handleChatMessage(ConnectionData connectionData) {
        if (connectionData.getType() == 7) {
            try {
                switch (connectionData.getDataType()) {
                    case ConnectionData.ENCRYPTED_TEXT:
                        Client.getHomeModel().appendChatData(EncryptionHandler.decryptTextData(connectionData));
                        break;
                    case ConnectionData.ENCRYPTED_AUDIO:
                        Client.getHomeModel().appendChatData(EncryptionHandler.decryptAudioData(connectionData));
                        break;
                    default:
                        throw new IllegalStateException("Bad data type.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Client.getHomeModel().appendChatData(connectionData);
        }
        new SendThread(new ConnectionData(connectionData.getUuid(), Client.getClientThread().getUser(), connectionData.getChatSession(), connectionData.getUserSignature())).start();
    }

}
