package socotra.model;

import org.whispersystems.libsignal.*;
import org.whispersystems.libsignal.ecc.Curve;
import org.whispersystems.libsignal.protocol.PreKeySignalMessage;
import org.whispersystems.libsignal.protocol.SignalMessage;
import org.whispersystems.libsignal.state.PreKeyBundle;
import socotra.Client;
import socotra.common.ChatSession;
import socotra.common.ConnectionData;
import socotra.common.KeyBundle;
import socotra.protocol.EncryptedClient;
import socotra.util.SendThread;
import socotra.util.SetOnlineUsers;

import java.util.TreeSet;

public class DataHandler {

    private final LoginModel loginModel = Client.getLoginModel();
    private final SignUpModel signUpModel = Client.getSignUpModel();

//    DataHandler(LoginModel loginModel, SignUpModel signUpModel) {
//        this.loginModel = loginModel;
//        this.signUpModel = signUpModel;
//    }

    private void initSession(KeyBundle keyBundle, String receiverName) throws InvalidKeyException, UntrustedIdentityException {
        EncryptedClient encryptedClient = Client.getEncryptedClient();
        SessionBuilder sessionBuilder = new SessionBuilder(encryptedClient.getSessionStore(), encryptedClient.getPreKeyStore(),
                encryptedClient.getSignedPreKeyStore(), encryptedClient.getIdentityKeyStore(),
                new SignalProtocolAddress(receiverName, 1));
        PreKeyBundle preKeyBundle = new PreKeyBundle(keyBundle.getRegistrationId(), 1, keyBundle.getPreKeyId(),
                Curve.decodePoint(keyBundle.getPreKey(), 0), keyBundle.getSignedPreKeyId(),
                Curve.decodePoint(keyBundle.getSignedPreKey(), 0), keyBundle.getSignedPreKeySignature(),
                new IdentityKey(keyBundle.getIdentityKey(), 0));
        sessionBuilder.process(preKeyBundle);
    }

    private String generateSenderUsername(ChatSession chatSession) throws IllegalStateException {
        TreeSet<String> toUsernames = chatSession.getToUsernames();
        if (toUsernames.size() != 2 || !toUsernames.contains(Client.getClientThread().getUsername())) {
            throw new IllegalStateException("Bad current chat session.");
        }
        for (String username : toUsernames) {
            if (!username.equals(Client.getClientThread().getUsername())) {
                return username;
            }
        }
        throw new IllegalStateException("Bad current chat session.");
    }

    private ConnectionData decryptTextData(ConnectionData connectionData) throws Exception {
        EncryptedClient encryptedClient = Client.getEncryptedClient();
        String senderUsername = generateSenderUsername(connectionData.getChatSession());
        SignalProtocolAddress senderAddress = new SignalProtocolAddress(senderUsername, 1);
        SessionCipher sessionCipher = new SessionCipher(encryptedClient.getSessionStore(), encryptedClient.getPreKeyStore(),
                encryptedClient.getSignedPreKeyStore(), encryptedClient.getIdentityKeyStore(), senderAddress);
        String plainText;
        switch (connectionData.getCipherType()) {
            case 2:
                plainText = new String(sessionCipher.decrypt(new SignalMessage(connectionData.getCipherTextData())));
                break;
            case 3:
                plainText = new String(sessionCipher.decrypt(new PreKeySignalMessage(connectionData.getCipherTextData())));
                break;
            default:
                throw new IllegalStateException("Bad encrypted text.");
        }
        return new ConnectionData(plainText, connectionData.getUserSignature(), connectionData.getChatSession());
    }

    boolean handle(ConnectionData connectionData) {
        switch (connectionData.getType()) {
            // If connectionData is about the result of user's validation.
            case -1:
                if (!connectionData.getValidated()) {
                    loginModel.setErrorType(2);
                    return false;
                }
                synchronized (loginModel) {
                    loginModel.notify();
                }
//                        TestProtocol.init();
                break;
            // If connectionData is about users online information.
            case -2:
                System.out.println(connectionData.getUserSignature() + " is " + (connectionData.getIsOnline() ? "online" : "offline"));
                if (connectionData.getIsOnline()) {
                    Client.getHomeModel().appendClientsList(connectionData.getUserSignature());
                } else {
                    Client.getHomeModel().removeClientsList(connectionData.getUserSignature());
                }
                break;
            // If connectionData is about set online users.
            case -3:
                System.out.println(connectionData.getOnlineUsers());
                SetOnlineUsers setOnlineUsers = new SetOnlineUsers(connectionData.getOnlineUsers());
                Client.setSetOnlineUsers(setOnlineUsers);
                setOnlineUsers.start();
//                        TestProtocol.query();
                break;
            // If connectionData is about received hint.
            case -4:
                Client.getHomeModel().updateChatData(connectionData.getUuid(), connectionData.getChatSession());
                break;
            // If connectionData is about sign up result.
            case -5:
                if (!connectionData.getSignUpSuccess()) {
                    signUpModel.setErrorType(2);
                    return false;
                }
                synchronized (signUpModel) {
                    signUpModel.notify();
                }
                break;
            // If connectionData is about normal chat messages.
            case 1:
            case 2:
            case 7:
                if (connectionData.getType() == 7) {
                    try {
                        Client.getHomeModel().appendChatData(decryptTextData(connectionData));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Client.getHomeModel().appendChatData(connectionData);
                }
                new SendThread(new ConnectionData(connectionData.getUuid(), Client.getClientThread().getUsername(), connectionData.getChatSession())).start();
                break;
            // If connectionData is about chat history data.
//            case 3:
//                SetChatData setChatData = new SetChatData(connectionData.getChatData());
//                Client.setSetChatData(setChatData);
//                setChatData.start();
//                break;
            case 6:
//                System.out.println("remote: " + new String(connectionData.getKeyBundle().getIdentityKey()));
                TreeSet<String> clients = new TreeSet<>();
                clients.add(connectionData.getReceiverUsername());
                clients.add(Client.getClientThread().getUsername());
                try {
                    initSession(connectionData.getKeyBundle(), connectionData.getReceiverUsername());
                    ChatSession chatSession = new ChatSession(clients, true, true);
                    Client.getHomeModel().appendChatSessionList(chatSession);
                } catch (Exception e) {
                    e.printStackTrace();
                }

//                        TestProtocol.identityKey = connectionData.getKeyBundle().getIdentityKey();
//                        TestProtocol.preKey = connectionData.getKeyBundle().getPreKey();
//                        TestProtocol.test();
                break;
            default:
                System.out.println("Unknown data.");
        }
        return true;
    }

}