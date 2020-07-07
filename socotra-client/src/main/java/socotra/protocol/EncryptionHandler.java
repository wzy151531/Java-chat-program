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
        return encryptData(text.getBytes(StandardCharsets.UTF_8), chatSession, ConnectionData.ENCRYPTED_TEXT);
    }

    public static ConnectionData encryptAudioData(byte[] audio, ChatSession chatSession) throws UntrustedIdentityException {
        return encryptData(audio, chatSession, ConnectionData.ENCRYPTED_AUDIO);
    }

    private static ConnectionData encryptData(byte[] data, ChatSession chatSession, int dataType) throws UntrustedIdentityException {
        SessionCipher sessionCipher = generateSessionCipher(chatSession);
        CiphertextMessage ciphertextMessage = sessionCipher.encrypt(data);
        return new ConnectionData(ciphertextMessage.serialize(), Client.getClientThread().getUsername(), chatSession, ciphertextMessage.getType(), dataType);
    }

    private static byte[] decryptData(ConnectionData connectionData) throws Exception {
        SessionCipher sessionCipher = generateSessionCipher(connectionData.getChatSession());
        byte[] result;
        switch (connectionData.getCipherType()) {
            case 2:
                result = sessionCipher.decrypt(new SignalMessage(connectionData.getCipherData()));
                break;
            case 3:
                result = sessionCipher.decrypt(new PreKeySignalMessage(connectionData.getCipherData()));
                break;
            default:
                throw new IllegalStateException("Bad encrypted data.");
        }
        return result;
    }

    public static ConnectionData decryptTextData(ConnectionData connectionData) throws Exception {
        String plainText = new String(decryptData(connectionData));
        return new ConnectionData(plainText, connectionData.getUuid(), connectionData.getUserSignature(), connectionData.getChatSession());
    }

    public static ConnectionData decryptAudioData(ConnectionData connectionData) throws Exception {
        return new ConnectionData(decryptData(connectionData), connectionData.getUserSignature(), connectionData.getChatSession());
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

    private static SessionCipher generateSessionCipher(ChatSession chatSession) {
        EncryptedClient encryptedClient = Client.getEncryptedClient();
        String theOtherName = extractTheOtherName(chatSession);
        SignalProtocolAddress theOtherAddress = new SignalProtocolAddress(theOtherName, 1);
        return new SessionCipher(encryptedClient.getSessionStore(), encryptedClient.getPreKeyStore(),
                encryptedClient.getSignedPreKeyStore(), encryptedClient.getIdentityKeyStore(), theOtherAddress);
    }

}
