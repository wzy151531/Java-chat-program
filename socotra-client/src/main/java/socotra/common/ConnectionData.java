package socotra.common;

import java.io.Serializable;
import java.util.*;

/**
 * This file defines the format of connection data used for communication between server and clients.
 */

public class ConnectionData implements Serializable {


    public static final int ENCRYPTED_TEXT = 1;
    public static final int ENCRYPTED_AUDIO = 2;
    /**
     * Serial version uid.
     */
    private static final long serialVersionUID = 1L;
    /**
     * The unique id.
     */
    private UUID uuid;
    /**
     * The type of the connection data.
     */
    private int type;
    /**
     * Indicates whether the connection data is sent.
     */
    private boolean isSent = false;
    /**
     * The username of the login connection data.
     */
    private String username;
    /**
     * The password of the login connection data.
     */
    private String password;
    /**
     * The online status of user.
     */
    private boolean isOnline;
    /**
     * The online users.
     */
    private TreeSet<String> onlineUsers;
    /**
     * The validation result of the login connection data.
     */
    private boolean validated;

    private boolean signUpSuccess;

    /**
     * The audio message of the audio connection data.
     */
    private byte[] audioData;
    /**
     * The text message of the text connection data.
     */
    private String textData;
    /**
     * The user signature of connection data.
     */
    private String userSignature;
    /**
     * The chat session that data send to.
     */
    private ChatSession chatSession;
    /**
     * The chat history data of certain user.
     */
    private HashMap<ChatSession, List<ConnectionData>> chatData;

    private String receiverUsername;
    private KeyBundle keyBundle;
    private byte[] cipherData;
    private int cipherType;
    private int dataType;

    private boolean needDistribute;
    private TreeSet<String> receiversUsername;
    private HashMap<String, KeyBundle> keyBundles;
    private HashMap<String, ConnectionData> senderKeysData;

    /**
     * If connection data is about the result of user's validation, the connection data's type is -1.
     *
     * @param validated The result of user's validation.
     */
    public ConnectionData(boolean validated) {
        this.type = -1;
        this.validated = validated;
    }

    /**
     * If connection data is about the information of user's online status, the connection data's type is -2.
     *
     * @param userSignature The online user's name.
     */
    public ConnectionData(String userSignature, boolean isOnline) {
        this.type = -2;
        this.userSignature = userSignature;
        this.isOnline = isOnline;
    }

    /**
     * If connection data is about current online users, the connection data's type is -3.
     *
     * @param onlineUsers The current online users' name.
     */
    public ConnectionData(TreeSet<String> onlineUsers) {
        this.type = -3;
        this.onlineUsers = onlineUsers;
    }

    /**
     * If connection data is about received hint, the connection data's type is -4.
     *
     * @param uuid             The received hint of connection data's uuid.
     * @param userSignature    The connection data sender's username.
     * @param chatSession      The chatSession this connection data belong.
     * @param receiverUsername The receiver's username.
     */
    public ConnectionData(UUID uuid, String userSignature, ChatSession chatSession, String receiverUsername) {
        this.uuid = uuid;
        this.type = -4;
        this.userSignature = userSignature;
        this.chatSession = chatSession;
        this.receiverUsername = receiverUsername;
    }

    public ConnectionData(int type, boolean signUpSuccess) {
        this.type = -5;
        this.signUpSuccess = signUpSuccess;
    }

    /**
     * If connection data is about login information, the connection data's type is 0.
     *
     * @param type     The connection data's type.
     * @param username The user's username.
     * @param password The user's password.
     */
    public ConnectionData(int type, String username, String password) {
        this.type = 0;
        this.username = username;
        this.password = password;
    }

    /**
     * If connection data is about text message, the connection data's type is 1.
     *
     * @param textData      The text message in connection data.
     * @param userSignature The connection data sender's username.
     * @param chatSession   The chat session the data send to.
     */
    public ConnectionData(String textData, String userSignature, ChatSession chatSession) {
        this.uuid = UUID.randomUUID();
        this.type = 1;
        this.textData = textData;
        this.userSignature = userSignature;
        this.chatSession = chatSession;
    }

    /**
     * The constructor for recreating the connectionData according to existing textData, userSignature, uuid,
     * chatSession.
     *
     * @param textData      The textData of connectionData.
     * @param uuid          The uuid of connectionData.
     * @param userSignature The userSignature of connectionData.
     * @param chatSession   The chatSession of connectionData.
     */
    public ConnectionData(String textData, UUID uuid, String userSignature, ChatSession chatSession) {
        this.uuid = uuid;
        this.type = 1;
        this.textData = textData;
        this.userSignature = userSignature;
        this.chatSession = chatSession;
        this.isSent = true;
    }

    /**
     * If connection data is about audio message, the connection data's type is 2.
     *
     * @param audioData     The audio message in connection data.
     * @param userSignature The connection data sender's username.
     * @param chatSession   The chat session the data send to.
     */
    public ConnectionData(byte[] audioData, String userSignature, ChatSession chatSession) {
        this.uuid = UUID.randomUUID();
        this.type = 2;
        this.audioData = audioData;
        this.userSignature = userSignature;
        this.chatSession = chatSession;
    }

    public ConnectionData(byte[] audioData, UUID uuid, String userSignature, ChatSession chatSession) {
        this.uuid = uuid;
        this.type = 2;
        this.audioData = audioData;
        this.userSignature = userSignature;
        this.chatSession = chatSession;
    }

    /**
     * If connection data is about chatData, the connection data's type is 3.
     *
     * @param chatData      The chat history data of user.
     * @param userSignature The user want to store the chatData.
     */
    public ConnectionData(HashMap<ChatSession, List<ConnectionData>> chatData, String userSignature) {
        this.type = 3;
        this.chatData = chatData;
        this.userSignature = userSignature;
    }

    public ConnectionData(String username, String password, KeyBundle keyBundle) {
        this.type = 4;
        this.username = username;
        this.password = password;
        this.keyBundle = keyBundle;
    }

    public ConnectionData(String receiverUsername, String userSignature) {
        this.type = 5;
        this.receiverUsername = receiverUsername;
        this.userSignature = userSignature;
    }

    public ConnectionData(KeyBundle keyBundle, String receiverUsername) {
        this.type = 6;
        this.keyBundle = keyBundle;
        this.receiverUsername = receiverUsername;
    }

    public ConnectionData(byte[] cipherData, String userSignature, ChatSession chatSession, int cipherType, int dataType) {
        this.uuid = UUID.randomUUID();
        this.type = 7;
        this.cipherData = cipherData;
        this.userSignature = userSignature;
        this.chatSession = chatSession;
        this.cipherType = cipherType;
        this.dataType = dataType;
    }

    /**
     * If the connection data is about senderKeyDistributionMessage for group chat, the connection data's type is 8.
     *
     * @param cipherData     The sender's senderKey.
     * @param userSignature  The sender's username.
     * @param chatSession    The group chat session.
     * @param needDistribute If receiver's sender key needs to distribute to others in this group.
     */
    // TODO: integrate all senderKey package to one package.
    public ConnectionData(byte[] cipherData, String userSignature, ChatSession chatSession, boolean needDistribute, String receiverUsername, int cipherType) {
        this.type = 8;
        this.cipherData = cipherData;
        this.userSignature = userSignature;
        this.chatSession = chatSession;
        this.needDistribute = needDistribute;
        this.receiverUsername = receiverUsername;
        this.cipherType = cipherType;
    }

    public ConnectionData(TreeSet<String> receiversUsername, ChatSession chatSession, String userSignature, boolean needDistribute) {
        this.type = 9;
        this.receiversUsername = receiversUsername;
        this.chatSession = chatSession;
        this.userSignature = userSignature;
        this.needDistribute = needDistribute;
    }

    public ConnectionData(HashMap<String, KeyBundle> keyBundles, ChatSession chatSession, boolean needDistribute) {
        this.type = 10;
        this.keyBundles = keyBundles;
        this.chatSession = chatSession;
        this.needDistribute = needDistribute;
    }

    public ConnectionData(HashMap<String, ConnectionData> senderKeysData) {
        this.type = 11;
        this.senderKeysData = senderKeysData;
    }

    public HashMap<String, ConnectionData> getSenderKeysData() {
        if (type != 11) {
            throw new IllegalStateException("Type isn't 11, cannot get senderKeysData");
        }
        return this.senderKeysData;
    }

    public HashMap<String, KeyBundle> getKeyBundles() {
        if (type != 10) {
            throw new IllegalStateException("Type isn't 10, cannot get keyBundles");
        }
        return this.keyBundles;
    }

    public TreeSet<String> getReceiversUsername() {
        if (type != 9) {
            throw new IllegalStateException("Type isn't 9, cannot get receiversUsername");
        }
        return this.receiversUsername;
    }

    public boolean getNeedDistribute() {
        if (type != 8 && type != 9 && type != 10) {
            throw new IllegalStateException("Type isn't 8 or 9 or 10, cannot get needDistribute");
        }
        return this.needDistribute;
    }

    public KeyBundle getKeyBundle() {
        if (type != 4 && type != 6) {
            throw new IllegalStateException("Type isn't 4 or 6, cannot get keyBundle");
        }
        return this.keyBundle;
    }

    public byte[] getCipherData() {
        if (type != 7 && type != 8) {
            throw new IllegalStateException("Type isn't 7 or 8, cannot get cipherData");
        }
        return this.cipherData;
    }

    public int getCipherType() {
        if (type != 7 && type != 8) {
            throw new IllegalStateException("Type isn't 7 or 8, cannot get cipherType");
        }
        return this.cipherType;
    }

    public int getDataType() {
        if (type != 7) {
            throw new IllegalStateException("Type isn't 7, cannot get dataType");
        }
        return this.dataType;
    }

    public String getReceiverUsername() {
        if (type != -4 && type != 5 && type != 6 && type != 8) {
            throw new IllegalStateException("Type isn't 5 or 6 or 8, cannot get receiverUsername");
        }
        return this.receiverUsername;
    }

    /**
     * Getter for uuid.
     *
     * @return The uuid of connection data.
     */
    public UUID getUuid() {
        if (type != 1 && type != 2 && type != -4 && type != 7) {
            throw new IllegalStateException("Type isn't 1 or 2 or 4 or 7, cannot get uuid");
        }
        return uuid;
    }

    /**
     * Getter for type.
     *
     * @return The type of the connection data.
     */
    public synchronized int getType() {
        return type;
    }

    /**
     * Getter for isSent.
     *
     * @return Indicates whether the connection data is sent.
     */
    public boolean getIsSent() {
        if (type != 1 && type != 2 && type != 7) {
            throw new IllegalStateException("Type isn't 1 or 2 or 7, cannot get isSent");
        }
        return isSent;
    }

    /**
     * Setter for isSent.
     *
     * @param isSent Indicates whether the connection data is sent.
     */
    public void setIsSent(boolean isSent) {
        if (type != 1 && type != 2 && type != 7) {
            throw new IllegalStateException("Type isn't 1 or 2 or 7, cannot set isSent");
        }
        this.isSent = isSent;
    }

    /**
     * Getter for username.
     *
     * @return The username of the login connection data.
     */
    public String getUsername() {
        if (type != 0 && type != 4) {
            throw new IllegalStateException("Type isn't 0 or 4, cannot get username.");
        }
        return username;
    }

    /**
     * Getter for password.
     *
     * @return The password of the login connection data.
     */
    public String getPassword() {
        if (type != 0 && type != 4) {
            throw new IllegalStateException("Type isn't 0 or 4, cannot get password.");
        }
        return password;
    }

    /**
     * Getter for isOnline.
     *
     * @return The online status of user.
     */
    public boolean getIsOnline() {
        if (type != -2) {
            throw new IllegalStateException("Type isn't -2, cannot get isOnline.");
        }
        return isOnline;
    }

    /**
     * Getter for onlineUsers.
     *
     * @return The online users.
     */
    public TreeSet<String> getOnlineUsers() {
        if (type != -3) {
            throw new IllegalStateException("Type isn't -3, connot get onlineUsers.");
        }
        return onlineUsers;
    }

    /**
     * Getter for validated.
     *
     * @return The validation result of the login connection data.
     */
    public boolean getValidated() {
        if (type != -1) {
            throw new IllegalStateException("Type isn't -1, cannot get validated.");
        }
        return validated;
    }

    public boolean getSignUpSuccess() {
        if (type != -5) {
            throw new IllegalStateException("Type isn't -5, cannot get signUpSuccess.");
        }
        return signUpSuccess;
    }

    /**
     * Getter for audio message.
     *
     * @return The audio message of the audio connection data.
     */
    public byte[] getAudioData() {
        if (type != 2) {
            throw new IllegalStateException("Type isn't 2, cannot get audio data.");
        }
        return audioData;
    }

    /**
     * Getter for text message.
     *
     * @return The text message of the text connection data.
     */
    public String getTextData() {
        if (type != 1) {
            throw new IllegalStateException("Type isn't 1, cannot get text data.");
        }
        return textData;
    }

    /**
     * Getter for userSignature.
     *
     * @return The userSignature of the connection data.
     */
    public String getUserSignature() {
        if (type != -2 && type != -4 && type != 1 && type != 2 && type != 3 && type != 5 && type != 7 && type != 8 && type != 9) {
            throw new IllegalStateException("Type isn't -2 or -4 or 1 or 2 or 3 or 5 or 7 or 8 or 9, cannot get text data.");
        }
        return userSignature;
    }

    /**
     * Getter for chatSession.
     *
     * @return The chat session that connection data sent to.
     */
    public ChatSession getChatSession() {
        if (type != 1 && type != 2 && type != -4 && type != 7 && type != 8 && type != 9 && type != 10) {
            throw new IllegalStateException("Type isn't 1 or 2 or 4 or 7 or 8 or 9 or 10, cannot get chatSession");
        }
        return chatSession;
    }

    /**
     * Getter for chatData.
     *
     * @return The chat history data of the user connection data sends to.
     */
    public HashMap<ChatSession, List<ConnectionData>> getChatData() {
        if (type != 3) {
            throw new IllegalStateException("Type isn't 3, cannot get chatData.");
        }
        return chatData;
    }

}