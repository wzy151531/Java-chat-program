package socotra.protocol;

import org.whispersystems.libsignal.NoSessionException;
import org.whispersystems.libsignal.SessionCipher;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.UntrustedIdentityException;
import org.whispersystems.libsignal.groups.GroupCipher;
import org.whispersystems.libsignal.groups.SenderKeyName;
import org.whispersystems.libsignal.protocol.CiphertextMessage;
import org.whispersystems.libsignal.protocol.PreKeySignalMessage;
import org.whispersystems.libsignal.protocol.SignalMessage;
import org.whispersystems.libsignal.state.SessionStore;
import socotra.Client;
import socotra.common.ChatSession;
import socotra.common.ConnectionData;
import socotra.common.User;

import java.nio.charset.StandardCharsets;
import java.util.TreeSet;

public abstract class EncryptionHandler {

    public static ConnectionData encryptTextData(String text, ChatSession chatSession) throws UntrustedIdentityException, NoSessionException {
        return encryptData(text.getBytes(StandardCharsets.UTF_8), chatSession, ConnectionData.ENCRYPTED_TEXT);
    }

    public static ConnectionData encryptAudioData(byte[] audio, ChatSession chatSession) throws UntrustedIdentityException, NoSessionException {
        return encryptData(audio, chatSession, ConnectionData.ENCRYPTED_AUDIO);
    }

    static ConnectionData encryptSKDMData(byte[] senderKey, ChatSession chatSession, User receiver) throws UntrustedIdentityException {
        SessionCipher sessionCipher = Client.getEncryptedClient().getSessionCipher(receiver);
        CiphertextMessage cipherSenderKey = sessionCipher.encrypt(senderKey);
        return new ConnectionData(cipherSenderKey.serialize(), Client.getClientThread().getUser(), chatSession, receiver, cipherSenderKey.getType());
    }

    public static byte[] decryptSKDMData(ConnectionData connectionData) throws Exception {
        return decryptData(connectionData);
    }

    public static ConnectionData encryptBackUpData(byte[] data, ChatSession chatSession, int dataType, User receiver, User signature) throws UntrustedIdentityException, NoSessionException {
        EncryptedClient encryptedClient = Client.getEncryptedClient();
        SessionCipher sessionCipher = encryptedClient.getSessionCipher(receiver);
        CiphertextMessage ciphertextMessage = sessionCipher.encrypt(data);
        byte[] encryptedData = ciphertextMessage.serialize();
        int cipherTextMessageType = ciphertextMessage.getType();
        return new ConnectionData(encryptedData, signature, chatSession, cipherTextMessageType, dataType);
    }

    public static ConnectionData decryptBackUpData(ConnectionData connectionData, User sender) throws Exception {
        SessionCipher sessionCipher = Client.getEncryptedClient().getSessionCipher(sender);
        byte[] decryptedData;
        switch (connectionData.getCipherType()) {
            case 2:
                decryptedData = sessionCipher.decrypt(new SignalMessage(connectionData.getCipherData()));
                break;
            case 3:
                decryptedData = sessionCipher.decrypt(new PreKeySignalMessage(connectionData.getCipherData()));
                break;
            default:
                throw new IllegalStateException("Bad chatSession type.");
        }
        switch (connectionData.getDataType()) {
            case ConnectionData.ENCRYPTED_TEXT:
                return new ConnectionData(new String(decryptedData), connectionData.getUuid(), connectionData.getUserSignature(), connectionData.getChatSession());
            case ConnectionData.ENCRYPTED_AUDIO:
                return new ConnectionData(decryptedData, connectionData.getUserSignature(), connectionData.getChatSession());
            default:
                throw new IllegalStateException("Bad data type.");
        }
    }

    private static ConnectionData encryptData(byte[] data, ChatSession chatSession, int dataType) throws UntrustedIdentityException, NoSessionException {
        EncryptedClient encryptedClient = Client.getEncryptedClient();
        byte[] encryptedData;
        int cipherTextMessageType = 0;
        SignalProtocolAddress signalProtocolAddress;
        switch (chatSession.getSessionType()) {
            case ChatSession.PAIRWISE:
                User receiver = extractTheOtherName(chatSession);
                SessionCipher sessionCipher = encryptedClient.getSessionCipher(receiver);
                CiphertextMessage ciphertextMessage = sessionCipher.encrypt(data);
                encryptedData = ciphertextMessage.serialize();
                cipherTextMessageType = ciphertextMessage.getType();

                if (dataType == ConnectionData.ENCRYPTED_TEXT) {
                    signalProtocolAddress = new SignalProtocolAddress(receiver.getUsername(), receiver.getDeviceId());
                    System.out.println("After Encrypt: " + new String(data) + ", the sending chain key's new message key is: " +
                            new String(encryptedClient.getSessionStore().loadSession(signalProtocolAddress).getSessionState().
                                    getSenderChainKey().getMessageKeys().getCipherKey().getEncoded()));
                }

                break;
            case ChatSession.GROUP:
                User sender = Client.getClientThread().getUser();
                GroupCipher groupCipher = encryptedClient.getGroupCipher(sender, chatSession);
                encryptedData = groupCipher.encrypt(data);

                if (dataType == ConnectionData.ENCRYPTED_TEXT) {
                    try {
                        signalProtocolAddress = new SignalProtocolAddress(sender.getUsername(), sender.getDeviceId());
                        SenderKeyName senderKeyName = new SenderKeyName(chatSession.generateChatIdCSV(), signalProtocolAddress);
                        System.out.println("After Encrypt group message: " + new String(data) + ", " + signalProtocolAddress + "'s sender key's new message key is: " +
                                new String(encryptedClient.getSenderKeyStore().loadSenderKey(senderKeyName).getSenderKeyState().
                                        getSenderChainKey().getSenderMessageKey().getCipherKey()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                break;
            default:
                throw new IllegalStateException("Bad chatSession type.");
        }
        return new ConnectionData(encryptedData, Client.getClientThread().getUser(), chatSession, cipherTextMessageType, dataType);
    }

    private static byte[] decryptData(ConnectionData connectionData) throws Exception {
        User sender = connectionData.getUserSignature();
        SessionCipher sessionCipher = Client.getEncryptedClient().getSessionCipher(sender);
        EncryptedClient encryptedClient = Client.getEncryptedClient();
        SignalProtocolAddress signalProtocolAddress = new SignalProtocolAddress(sender.getUsername(), sender.getDeviceId());
        byte[] result;
        switch (connectionData.getCipherType()) {
            case 2:
                SignalMessage signalMessage = new SignalMessage(connectionData.getCipherData());
                result = sessionCipher.decrypt(signalMessage);

                if (connectionData.getType() == 7 && connectionData.getDataType() == ConnectionData.ENCRYPTED_TEXT) {
                    System.out.println("After Decrypt: " + new String(result) + ", the receiving chain key's new message key is: " +
                            new String(encryptedClient.getSessionStore().loadSession(signalProtocolAddress).getSessionState().
                                    getReceiverChainKey(signalMessage.getSenderRatchetKey()).getMessageKeys().getCipherKey().getEncoded()));
                }

                return result;
            case 3:
                PreKeySignalMessage preKeySignalMessage = new PreKeySignalMessage(connectionData.getCipherData());
                result = sessionCipher.decrypt(preKeySignalMessage);

                if (connectionData.getType() == 7 && connectionData.getDataType() == ConnectionData.ENCRYPTED_TEXT) {
                    System.out.println("After Decrypt: " + new String(result) + ", the receiving chain key's new message key is: " +
                            new String(encryptedClient.getSessionStore().loadSession(signalProtocolAddress).getSessionState().
                                    getReceiverChainKey(preKeySignalMessage.getWhisperMessage().getSenderRatchetKey()).getMessageKeys().getCipherKey().getEncoded()));
                }

                return result;
            default:
                throw new IllegalStateException("Bad encrypted data.");
        }
    }

    private static byte[] decryptGroupData(ConnectionData connectionData) throws Exception {
        EncryptedClient encryptedClient = Client.getEncryptedClient();
        User sender = connectionData.getUserSignature();
        ChatSession chatSession = connectionData.getChatSession();
        GroupCipher groupCipher = encryptedClient.getGroupCipher(sender, chatSession);
        byte[] result = groupCipher.decrypt(connectionData.getCipherData());

        SignalProtocolAddress signalProtocolAddress = new SignalProtocolAddress(sender.getUsername(), sender.getDeviceId());
        SenderKeyName senderKeyName = new SenderKeyName(chatSession.generateChatIdCSV(), signalProtocolAddress);
        System.out.println("After Decrypt group message: " + new String(result) + ", " + signalProtocolAddress + "'s sender key's new message key is: " +
                new String(encryptedClient.getSenderKeyStore().loadSenderKey(senderKeyName).getSenderKeyState().
                        getSenderChainKey().getSenderMessageKey().getCipherKey()));

        return result;
    }

    public static ConnectionData decryptTextData(ConnectionData connectionData) throws Exception {
        String plainText = new String(connectionData.getChatSession().getSessionType() == ChatSession.PAIRWISE ?
                decryptData(connectionData) : decryptGroupData(connectionData));
//        System.out.println("Get plainText: " + plainText);
        return new ConnectionData(plainText, connectionData.getUuid(), connectionData.getUserSignature(), connectionData.getChatSession());
    }

    public static ConnectionData decryptAudioData(ConnectionData connectionData) throws Exception {
        return new ConnectionData(connectionData.getChatSession().getSessionType() == ChatSession.PAIRWISE ?
                decryptData(connectionData) : decryptGroupData(connectionData), connectionData.getUserSignature(), connectionData.getChatSession());
    }

    public static User extractTheOtherName(ChatSession chatSession) throws IllegalStateException {
        TreeSet<User> members = chatSession.getMembers();
        User caller = Client.getClientThread().getUser();
        if (members.size() != 2 || !members.contains(caller)) {
            throw new IllegalStateException("Bad chat session.");
        }
        for (User member : members) {
            if (!member.equals(caller)) {
                return member;
            }
        }
        throw new IllegalStateException("Bad current chat session.");
    }

}
