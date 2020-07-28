package socotra.protocol;

import org.whispersystems.libsignal.NoSessionException;
import org.whispersystems.libsignal.SessionCipher;
import org.whispersystems.libsignal.UntrustedIdentityException;
import org.whispersystems.libsignal.groups.GroupCipher;
import org.whispersystems.libsignal.protocol.CiphertextMessage;
import org.whispersystems.libsignal.protocol.PreKeySignalMessage;
import org.whispersystems.libsignal.protocol.SignalMessage;
import socotra.Client;
import socotra.common.ChatSession;
import socotra.common.ConnectionData;

import java.nio.charset.StandardCharsets;
import java.util.TreeSet;

public abstract class EncryptionHandler {

    public static ConnectionData encryptTextData(String text, ChatSession chatSession) throws UntrustedIdentityException, NoSessionException {
        return encryptData(text.getBytes(StandardCharsets.UTF_8), chatSession, ConnectionData.ENCRYPTED_TEXT);
    }

    public static ConnectionData encryptAudioData(byte[] audio, ChatSession chatSession) throws UntrustedIdentityException, NoSessionException {
        return encryptData(audio, chatSession, ConnectionData.ENCRYPTED_AUDIO);
    }

    static ConnectionData encryptSKDMData(byte[] senderKey, ChatSession chatSession, String receiverName) throws UntrustedIdentityException {
        SessionCipher sessionCipher = Client.getEncryptedClient().getSessionCipher(receiverName);
        CiphertextMessage cipherSenderKey = sessionCipher.encrypt(senderKey);
        return new ConnectionData(cipherSenderKey.serialize(), Client.getClientThread().getUsername(), chatSession, receiverName, cipherSenderKey.getType());
    }

    public static byte[] decryptSKDMData(ConnectionData connectionData) throws Exception {
        return decryptData(connectionData);
    }

    private static ConnectionData encryptData(byte[] data, ChatSession chatSession, int dataType) throws UntrustedIdentityException, NoSessionException {
        EncryptedClient encryptedClient = Client.getEncryptedClient();
        byte[] encryptedData;
        int cipherTextMessageType = 0;
        switch (chatSession.getSessionType()) {
            case ChatSession.PAIRWISE:
                String receiverName = extractTheOtherName(chatSession);
                SessionCipher sessionCipher = encryptedClient.getSessionCipher(receiverName);
                CiphertextMessage ciphertextMessage = sessionCipher.encrypt(data);
                encryptedData = ciphertextMessage.serialize();
                cipherTextMessageType = ciphertextMessage.getType();
                break;
            case ChatSession.GROUP:
                GroupCipher groupCipher = encryptedClient.getGroupCipher(Client.getClientThread().getUsername(), chatSession);
                encryptedData = groupCipher.encrypt(data);
                break;
            default:
                throw new IllegalStateException("Bad chatSession type.");
        }
        return new ConnectionData(encryptedData, Client.getClientThread().getUsername(), chatSession, cipherTextMessageType, dataType);

    }

    private static byte[] decryptData(ConnectionData connectionData) throws Exception {
        SessionCipher sessionCipher = Client.getEncryptedClient().getSessionCipher(connectionData.getUserSignature());
        switch (connectionData.getCipherType()) {
            case 2:
                return sessionCipher.decrypt(new SignalMessage(connectionData.getCipherData()));
            case 3:
                return sessionCipher.decrypt(new PreKeySignalMessage(connectionData.getCipherData()));
            default:
                throw new IllegalStateException("Bad encrypted data.");
        }
    }

    private static byte[] decryptGroupData(ConnectionData connectionData) throws Exception {
        System.out.println("decrypt group data");
        GroupCipher groupCipher = Client.getEncryptedClient().getGroupCipher(connectionData.getUserSignature(),
                connectionData.getChatSession());
        return groupCipher.decrypt(connectionData.getCipherData());
    }

    public static ConnectionData decryptTextData(ConnectionData connectionData) throws Exception {
        String plainText = new String(connectionData.getChatSession().getSessionType() == ChatSession.PAIRWISE ?
                decryptData(connectionData) : decryptGroupData(connectionData));
        System.out.println("Get plainText: " + plainText);
        return new ConnectionData(plainText, connectionData.getUuid(), connectionData.getUserSignature(), connectionData.getChatSession());
    }

    public static ConnectionData decryptAudioData(ConnectionData connectionData) throws Exception {
        return new ConnectionData(connectionData.getChatSession().getSessionType() == ChatSession.PAIRWISE ?
                decryptData(connectionData) : decryptGroupData(connectionData), connectionData.getUserSignature(), connectionData.getChatSession());
    }

    private static String extractTheOtherName(ChatSession chatSession) throws IllegalStateException {
        TreeSet<String> toUsernames = chatSession.getToUsernames();
        if (toUsernames.size() != 2 || !toUsernames.contains(Client.getClientThread().getUsername())) {
            throw new IllegalStateException("Bad chat session.");
        }
        for (String username : toUsernames) {
            if (!username.equals(Client.getClientThread().getUsername())) {
                return username;
            }
        }
        throw new IllegalStateException("Bad current chat session.");
    }

}
