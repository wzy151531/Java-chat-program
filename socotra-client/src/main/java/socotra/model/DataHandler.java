package socotra.model;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import socotra.Client;
import socotra.common.ChatSession;
import socotra.common.ConnectionData;
import socotra.common.KeyBundle;
import socotra.controller.ControllerUtil;
import socotra.protocol.EncryptedClient;
import socotra.protocol.EncryptionHandler;
import socotra.util.SendThread;
import socotra.util.SetOnlineUsers;
import socotra.util.Util;

import java.util.ArrayList;
import java.util.HashMap;

class DataHandler {

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
                    Client.getLoginModel().loadData();
                    ControllerUtil controllerUtil = new ControllerUtil();
                    Platform.runLater(() -> {
                        Client.closeWaitingAlert();
                        controllerUtil.loadHomePage();
                        Client.showInitClientAlert();
                    });
                }
                break;
            // If connectionData is about users online information.
            case -2:
                System.out.println(connectionData.getUserSignature() + " is " + (connectionData.getIsOnline() ? "online" : "offline"));
                String clientName = connectionData.getUserSignature();
                if (connectionData.getIsOnline() && !Client.getHomeModel().clientsContains(clientName)) {
                    Client.getHomeModel().appendClientsList(clientName);
                }
//                } else if (!connectionData.getIsOnline() && Client.getHomeModel().clientsContains(clientName)) {
//                    Client.getHomeModel().removeClientsList(clientName);
//                }
                break;
            // If connectionData is about set online users.
            case -3:
                System.out.println(connectionData.getOnlineUsers());
                SetOnlineUsers setOnlineUsers = new SetOnlineUsers(connectionData.getOnlineUsers());
                Client.setSetOnlineUsers(setOnlineUsers);
                setOnlineUsers.start();
                break;
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
                    String receiverName = connectionData.getReceiverUsername();
                    encryptedClient.initPairwiseChat(connectionData.getKeyBundle(), receiverName);
                    encryptedClient.finishInitPairwiseChat(receiverName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 8:
                processReceSenderKey(connectionData);
                break;
            case 10:
                processReceKeyBundles(connectionData);
                break;
            case 12:
                processDepositData(connectionData);
                break;
            default:
                System.out.println("Unknown data.");
        }
        return true;
    }

    private void processDepositData(ConnectionData connectionData) {
        Platform.runLater(() -> {
            processPairwiseData(connectionData.getDepositPairwiseData());
            processSenderKeyData(connectionData.getDepositSenderKeyData());
            processGroupData(connectionData.getDepositGroupData());
            Client.closeInitClientAlert();
        });
    }

    private void processPairwiseData(ArrayList<ConnectionData> pairwiseData) {
        System.out.println("process pairwise data.");
        if (pairwiseData == null) return;
        pairwiseData.forEach(n -> {
            handleChatMessage(n);
        });
    }

    private void processSenderKeyData(ArrayList<ConnectionData> senderKeyData) {
        System.out.println("process senderKey data.");
        if (senderKeyData == null) return;
        senderKeyData.forEach(n -> {
            processReceSenderKey(n);
        });
    }

    private void processGroupData(ArrayList<ConnectionData> groupData) {
        System.out.println("process group data.");
        if (groupData == null) return;
        groupData.forEach(n -> {
            handleChatMessage(n);
        });
    }

    private void processReceSenderKey(ConnectionData connectionData) {
        try {
            byte[] senderKey = EncryptionHandler.decryptSKDMData(connectionData);
            EncryptedClient encryptedClient = Client.getEncryptedClient();
            encryptedClient.processReceivedSenderKey(senderKey, connectionData.getChatSession(), connectionData.getNeedDistribute(), connectionData.getUserSignature());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processReceKeyBundles(ConnectionData connectionData) {
        EncryptedClient encryptedClient = Client.getEncryptedClient();
        HashMap<String, KeyBundle> keyBundles = connectionData.getKeyBundles();
        keyBundles.forEach((k, v) -> {
            try {
                encryptedClient.initPairwiseChat(v, k);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        String caller = Client.getClientThread().getUsername();
        ChatSession chatSession = connectionData.getChatSession();
        encryptedClient.distributeSenderKey(chatSession.getOthers(caller), chatSession, connectionData.getNeedDistribute());
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
        new SendThread(new ConnectionData(connectionData.getUuid(), Client.getClientThread().getUsername(), connectionData.getChatSession(), connectionData.getUserSignature())).start();
    }

}
