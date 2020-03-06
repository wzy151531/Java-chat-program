package socotra.common;

import java.io.Serializable;

public class ConnectionData implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * The type of the connection data.
     */
    private int type;
    /**
     * The username of the login connection data.
     */
    private String username;
    /**
     * The password of the login connection data.
     */
    private String password;
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
     * If connection data is about the result of user's validation, the connection data's type is -1.
     *
     * @param validated The result of user's validation.
     */
    public ConnectionData(boolean validated) {
        this.type = -1;
        this.validated = validated;
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
     */
    public ConnectionData(String textData, String userSignature) {
        this.type = 1;
        this.textData = textData;
        this.userSignature = userSignature;
    }

    /**
     * If connection data is about audio message, the connection data's type is 2.
     *
     * @param audioData     The audio message in connection data.
     * @param userSignature The connection data sender's username.
     */
    public ConnectionData(byte[] audioData, String userSignature) {
        this.type = 2;
        this.audioData = audioData;
        this.userSignature = userSignature;
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
        return userSignature;
    }

}