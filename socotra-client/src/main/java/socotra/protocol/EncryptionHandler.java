package socotra.protocol;

import org.whispersystems.libsignal.SessionCipher;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.UntrustedIdentityException;
import org.whispersystems.libsignal.protocol.CiphertextMessage;
import org.whispersystems.libsignal.protocol.PreKeySignalMessage;
import org.whispersystems.libsignal.protocol.SignalMessage;
import socotra.Client;
import socotra.common.ChatSession;
import socotra.common.ConnectionData;

import java.nio.charset.StandardCharsets;
import java.util.TreeSet;

public abstract class EncryptionHandler {

    public static ConnectionData encryptTextData(String text, ChatSession chatSession) throws UntrustedIdentityException {
        EncryptedClient encryptedClient = Client.getEncryptedClient();
        String receiverUsername = extractTheOtherName(chatSession);
        SessionCipher sessionCipher = new SessionCipher(encryptedClient.getSessionStore(), encryptedClient.getPreKeyStore(),
                encryptedClient.getSignedPreKeyStore(), encryptedClient.getIdentityKeyStore(),
                new SignalProtocolAddress(receiverUsername, 1));
        CiphertextMessage ciphertextMessage = sessionCipher.encrypt(text.getBytes(StandardCharsets.UTF_8));
        return new ConnectionData(ciphertextMessage.serialize(), Client.getClientThread().getUsername(), chatSession, ciphertextMessage.getType());
    }

    public static ConnectionData decryptTextData(ConnectionData connectionData) throws Exception {
        EncryptedClient encryptedClient = Client.getEncryptedClient();
        String senderUsername = extractTheOtherName(connectionData.getChatSession());
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
                throw new IllegalStateException("Bad encrypted text data.");
        }
        return new ConnectionData(plainText, connectionData.getUserSignature(), connectionData.getChatSession());
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
