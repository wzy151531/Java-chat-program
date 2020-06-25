package socotra.common;

import java.io.Serializable;
import java.util.List;

public class KeyBundle implements Serializable {

    private static final long serialVersionUID = 1L;
    private int type;
    private int registrationId;
    private byte[] identityKey;
    private List<byte[]> preKeys;
    private int preKeyId;
    private byte[] preKey;
    private int signedPreKeyId;
    private byte[] signedPreKey;
    private byte[] signedPreKeySignature;

    public KeyBundle(int registrationId, byte[] identityKey, List<byte[]> preKeys, int signedPreKeyId, byte[] signedPreKey, byte[] signedPreKeySignature) {
        this.type = 1;
        this.registrationId = registrationId;
        this.identityKey = identityKey;
        this.preKeys = preKeys;
        this.signedPreKeyId = signedPreKeyId;
        this.signedPreKey = signedPreKey;
        this.signedPreKeySignature = signedPreKeySignature;
    }

    public KeyBundle(int registrationId, byte[] identityKey, int preKeyId, byte[] preKey, int signedPreKeyId, byte[] signedPreKey, byte[] signedPreKeySignature) {
        this.type = 2;
        this.registrationId = registrationId;
        this.identityKey = identityKey;
        this.preKeyId = preKeyId;
        this.preKey = preKey;
        this.signedPreKeyId = signedPreKeyId;
        this.signedPreKey = signedPreKey;
        this.signedPreKeySignature = signedPreKeySignature;
    }

    public int getRegistrationId() {
        return registrationId;
    }

    public byte[] getIdentityKey() {
        return identityKey;
    }

    public List<byte[]> getPreKeys() {
        if (type != 1) {
            throw new IllegalStateException("Type isn't 1, cannot get preKeys.");
        }
        return preKeys;
    }

    public int getPreKeyId() {
        if (type != 2) {
            throw new IllegalStateException("Type isn't 2, cannot get preKeyId.");
        }
        return preKeyId;
    }

    public byte[] getPreKey() {
        if (type != 2) {
            throw new IllegalStateException("Type isn't 2, cannot get preKey.");
        }
        return preKey;
    }

    public int getSignedPreKeyId() {
        return signedPreKeyId;
    }

    public byte[] getSignedPreKey() {
        return signedPreKey;
    }

    public byte[] getSignedPreKeySignature() {
        return signedPreKeySignature;
    }
}
