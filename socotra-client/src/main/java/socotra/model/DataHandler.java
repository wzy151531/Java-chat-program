package socotra.model;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.UntrustedIdentityException;
import org.whispersystems.libsignal.protocol.SenderKeyDistributionMessage;
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
import socotra.util.UpdateSenderKey;
import socotra.util.Util;

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

public class DataHandler {

    private ArrayList<ConnectionData> pairwiseData;
    private ArrayList<ConnectionData> senderKeysData;
    private ArrayList<ConnectionData> groupData;
    private ArrayList<ConnectionData> switchData;

    DataHandler() {
        Client.setDataHandler(this);
    }

    private void initClient(ConnectionData connectionData) {
        Client.showInitClientAlert();
        ControllerUtil controllerUtil = new ControllerUtil();
        if (!connectionData.isBackUp()) {
            controllerUtil.loadHomePage();
            Client.getLoginModel().loadChatData();

            processBackUpMessages(connectionData.getBackUpMessages(), connectionData.getUserSignature());

            processDepositData(connectionData);
            processOnlineUsers(connectionData.getOnlineUsers());
            processPairwiseData();
            processSenderKeyData();
        } else {
            Client.backUpReceiver = connectionData.getReceiverUsername();
            Client.backUpKeyBundle = connectionData.getKeyBundle();
            controllerUtil.loadBackUpPage();
            Client.getLoginModel().loadChatData();
            Client.closeInitClientAlert();
        }
    }

    private void processBackUpMessages(HashMap<ChatSession, ArrayList<ConnectionData>> backUpMessages, User sender) {
        if (backUpMessages != null) {
            backUpMessages.forEach((k, v) -> {
                v.forEach(n -> {
                    handleBackUpMessage(n, sender);
                });
            });
        }
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
//                System.out.println(user + " is " + (connectionData.getIsOnline() ? "online" : "offline"));
                if (connectionData.getIsOnline() && !Client.getHomeModel().clientsContains(user)) {
                    HomeModel homeModel = Client.getHomeModel();
                    homeModel.appendClientsList(user);
                } else if (!connectionData.getIsOnline() && Client.getHomeModel().clientsContains(user)) {
                    Client.getHomeModel().removeClientsList(user);
                }
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
                        Util.generateAlert(Alert.AlertType.ERROR, "Validation Error", connectionData.getErrorMsg(), "Try again.").show();
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
            // If connectionData is about receiver's key bundle.
            case 6:
                EncryptedClient encryptedClient = Client.getEncryptedClient();
                User receiver = connectionData.getReceiverUsername();
                KeyBundle keyBundle = connectionData.getKeyBundle();
                try {
                    if (connectionData.isReInit()) {
                        encryptedClient.initPairwiseChat(keyBundle, receiver);
                        encryptedClient.checkGroupCipher(receiver);
                        Platform.runLater(() -> {
                            Client.getHomeModel().updateRelatedSession(receiver);
                            Client.closeReInitChatAlert();
                        });
                    } else {
                        encryptedClient.initPairwiseChat(keyBundle, receiver);
                        encryptedClient.finishInitPairwiseChat(receiver);
                    }
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
            case 13:
                processSwitchInfo(connectionData.getUserSignature());
                break;
            case 14:
                new UpdateSenderKey().start();
                break;
            default:
                System.out.println("Unknown data.");
        }
        return true;
    }

    private void processSwitchInfo(User user) {
        User me = Client.getClientThread().getUser();
        if (me.getUsername().equals(user.getUsername())) {
            Platform.runLater(() -> {
                Alert warningAlert = Util.generateAlert(Alert.AlertType.WARNING, "Switch Device", "Account logs in on another device.", "If it's not your operation, please change your password soon.");
                warningAlert.setResultConverter(dialogButton -> {
                    if (dialogButton == ButtonType.OK) {
                        Client.processLogout(new ConnectionData(Client.getClientThread().getUser(), false));
                    }
                    return null;
                });
                warningAlert.show();
            });
        } else {
            Platform.runLater(() -> {
                Client.showReInitChatAlert();
                EncryptedClient encryptedClient = Client.getEncryptedClient();
                encryptedClient.checkPairwiseCipher(user);
            });
        }
    }

    private void processDepositData(ConnectionData connectionData) {
        System.out.println("receive deposit data.");
        this.pairwiseData = connectionData.getDepositPairwiseData();
        this.senderKeysData = connectionData.getDepositSenderKeyData();
        this.groupData = connectionData.getDepositGroupData();
        this.switchData = connectionData.getDepositSwitchData();
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
        if (groupData == null) {
            processSwitchData();
            Platform.runLater(() -> {
                Client.closeInitClientAlert();
            });
            return;
        }
        groupData.forEach(n -> {
            handleChatMessage(n);
        });
        groupData = null;
        processSwitchData();
        Platform.runLater(() -> {
            Client.closeInitClientAlert();
        });
    }

    private void processSwitchData() {
        if (switchData == null) {
            System.out.println("Switch data is null");

            return;
        }
        switchData.forEach(n -> {
            processSwitchInfo(n.getUserSignature());
        });
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
        encryptedClient.distributeSenderKey(chatSession.getOthers(caller), chatSession, encryptedClient.getSKDM());
        Platform.runLater(() -> {
            encryptedClient.finishInitGroupChat(chatSession, connectionData.isInit());
        });
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

    private void handleBackUpMessage(ConnectionData connectionData, User sender) {
        try {
            Client.getHomeModel().appendChatData(EncryptionHandler.decryptBackUpData(connectionData, sender));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
