package socotra.common;

import java.io.Serializable;

public class ConnectionData implements Serializable {

    private static final long serialVersionUID = 1L;
    private int type;
    private String username;
    private String password;
    private boolean validated;
    private byte[] audioData;
    private String textData;
    private String userSignature;

    public ConnectionData(boolean validated) {
        this.type = -1;
        this.validated = validated;
    }

    public ConnectionData(int type, String username, String password) {
        this.type = 0;
        this.username = username;
        this.password = password;
    }

    public ConnectionData(String textData, String userSignature) {
        this.type = 1;
        this.textData = textData;
        this.userSignature = userSignature;
    }

    public ConnectionData(byte[] audioData, String userSignature) {
        this.type = 2;
        this.audioData = audioData;
        this.userSignature = userSignature;
    }

    public int getType() {
        return type;
    }

    public String getUsername() {
        if (type != 0) {
            System.out.println("Cannot't get username.");
            // TODO
        }
        return username;
    }

    public String getPassword() {
        if (type != 0) {
            System.out.println("Cannot't get password.");
            // TODO
        }
        return password;
    }

    public boolean getValidated() {
        return validated;
    }

    public byte[] getAudioData() {
        if (type != 2) {
            System.out.println("Cannot't get audio.");
            // TODO
        }
        return audioData;
    }

    public String getTextData() {
        if (type != 1) {
            System.out.println("Cannot't get text.");
            // TODO
        }
        return textData;
    }

    public String getUserSignature() {
        return userSignature;
    }

}