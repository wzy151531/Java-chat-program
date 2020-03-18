package socotra.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;

/**
 * This file defines the format of connection data used for communication between server and clients.
 */

public class ConnectionData implements Serializable {

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
     * @param uuid          The received hint of connection data's uuid.
     * @param userSignature The connection data sender's username.
     * @param chatSession
     */
    public ConnectionData(UUID uuid, String userSignature, ChatSession chatSession) {
        this.uuid = uuid;
        this.type = -4;
        this.userSignature = userSignature;
        this.chatSession = chatSession;
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

    /**
     * If connection data is about chatData, the connectino data's type is 3.
     *
     * @param chatData      The chat history data of user.
     * @param userSignature The user want to store the chatData.
     */
    public ConnectionData(HashMap<ChatSession, List<ConnectionData>> chatData, String userSignature) {
        this.type = 3;
        this.chatData = chatData;
        this.userSignature = userSignature;
    }

    /**
     * Getter for uuid.
     *
     * @return The uuid of connection data.
     */
    public UUID getUuid() {
        if (type != 1 && type != 2 && type != -4) {
            throw new IllegalStateException("Type isn't 1 or 2 or 4, cannot get uuid");
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
        if (type != 1 && type != 2) {
            throw new IllegalStateException("Type isn't 1 or 2, cannot get isSent");
        }
        return isSent;
    }

    /**
     * Setter for isSent.
     *
     * @param isSent Indicates whether the connection data is sent.
     */
    public void setIsSent(boolean isSent) {
        if (type != 1 && type != 2) {
            throw new IllegalStateException("Type isn't 1 or 2, cannot set isSent");
        }
        this.isSent = isSent;
    }

    /**
     * Getter for username.
     *
     * @return The username of the login connection data.
     */
    public String getUsername() {
        if (type != 0) {
            throw new IllegalStateException("Type isn't 0, cannot get username.");
        }
        return username;
    }

    /**
     * Getter for password.
     *
     * @return The password of the login connection data.
     */
    public String getPassword() {
        if (type != 0) {
            throw new IllegalStateException("Type isn't 0, cannot get password.");
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
        if (type != -2 && type != -4 && type != 1 && type != 2 && type != 3) {
            throw new IllegalStateException("Type isn't -2 or -4 or 1 or 2 or 3, cannot get text data.");
        }
        return userSignature;
    }

    /**
     * Getter for chatSession.
     *
     * @return The chat session that connection data sent to.
     */
    public ChatSession getChatSession() {
        if (type != 1 && type != 2 && type != -4) {
            throw new IllegalStateException("Type isn't 1 or 2, cannot get chatSession");
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