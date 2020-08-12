package socotra.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import socotra.Client;
import socotra.common.ChatSession;
import socotra.common.ConnectionData;
import socotra.common.KeyBundle;
import socotra.common.User;
import socotra.model.HomeModel;
import socotra.protocol.EncryptedClient;
import socotra.protocol.EncryptionHandler;
import socotra.util.SendThread;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

public class BackUpController {

    @FXML
    private Button logoutButton;

    @FXML
    private Button confirmButton;

    @FXML
    private Label receiverLabel;

    @FXML
    private void initialize() {
        Client.setHomeModel(new HomeModel());
        if (Client.backUpReceiver != null) {
            receiverLabel.setText(Client.backUpReceiver.toString());
        } else {
            receiverLabel.setText("");
            confirmButton.setDisable(true);
        }
    }

    @FXML
    public void confirm(ActionEvent event) {
        processBackUp(Client.backUpReceiver, Client.backUpKeyBundle);
    }

    @FXML
    public void logout(ActionEvent event) {
        Client.getHomeModel().handleLogout();
    }

    private void processBackUp(User receiver, KeyBundle keyBundle) {
        try {
            HashMap<ChatSession, ArrayList<ConnectionData>> chatDataCopy = Client.getHomeModel().getChatDataCopy();
            EncryptedClient encryptedClient = Client.getEncryptedClient();
            encryptedClient.initPairwiseChat(keyBundle, receiver);
            HashMap<ChatSession, ArrayList<ConnectionData>> result = new HashMap<>();
            chatDataCopy.forEach((k, v) -> {
                System.out.println(k.generateChatIdCSV() + ": ");
                TreeSet<User> members = new TreeSet<>(k.getMembers());
                User oldUser = k.relatedUser(receiver);
                members.remove(oldUser);
                members.add(receiver);
                ChatSession newSession = new ChatSession(members, false, true, k.getSessionType());
                ArrayList<ConnectionData> records = new ArrayList<>();
                v.forEach(n -> {
                    try {
                        System.out.println("    content: " + (n.getType() == 1 ? n.getTextData() : "audio"));
                        ConnectionData connectionData;
                        User signature = n.getUserSignature().getUsername().equals(receiver.getUsername()) ? receiver : n.getUserSignature();
                        switch (n.getType()) {
                            case 1:
                                connectionData = EncryptionHandler.encryptBackUpData(
                                        n.getTextData().getBytes(StandardCharsets.UTF_8), newSession, ConnectionData.ENCRYPTED_TEXT, receiver, signature);
                                break;
                            case 2:
                                connectionData = EncryptionHandler.encryptBackUpData(
                                        n.getAudioData(), newSession, ConnectionData.ENCRYPTED_AUDIO, receiver, signature);
                                break;
                            default:
                                throw new IllegalStateException("Bad type.");
                        }
                        records.add(connectionData);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                result.put(k, records);
            });
            new SendThread(new ConnectionData(receiver, result, Client.getClientThread().getUser()), true, 1).start();
            System.out.println("Back up success.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
