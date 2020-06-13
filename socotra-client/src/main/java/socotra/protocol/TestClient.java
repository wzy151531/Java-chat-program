package socotra.protocol;

import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.state.*;
import org.whispersystems.libsignal.util.KeyHelper;

import java.util.List;

public class TestClient {

    private int userId;
    private String username;
    private IdentityKeyPair identityKeyPair;
    private int registrationId;
    private List<PreKeyRecord> preKeys;
    private SignedPreKeyRecord signedPreKey;
    private SessionStore sessionStore;
    private PreKeyStore preKeyStore;
    private SignedPreKeyStore signedPreKeyStore;
    private IdentityKeyStore identityKeyStore;

    public TestClient(int userId, String username) throws InvalidKeyException {
        this.userId = userId;
        this.username = username;
        this.identityKeyPair = KeyHelper.generateIdentityKeyPair();
        this.registrationId = KeyHelper.generateRegistrationId(false);
        this.preKeys = KeyHelper.generatePreKeys(0, 100);
        this.signedPreKey = KeyHelper.generateSignedPreKey(this.identityKeyPair, userId);
        this.sessionStore = new MySessionStore();
        this.preKeyStore = new MyPreKeyStore();
        this.signedPreKeyStore = new MySignedPreKeyStore();
        this.identityKeyStore = new MyIdentityKeyStore(this.identityKeyPair, this.registrationId);
        storePreKeys();
        storeSignedPreKey();
    }

    private void storePreKeys() {
        for (PreKeyRecord preKeyRecord : this.preKeys) {
            preKeyStore.storePreKey(preKeyRecord.getId(), preKeyRecord);
        }
    }

    private void storeSignedPreKey() {
        signedPreKeyStore.storeSignedPreKey(this.userId, this.signedPreKey);
    }

    public int getUserId() {
        return this.userId;
    }

    public String getUsername() {
        return this.username;
    }

    public IdentityKeyPair getIdentityKeyPair() {
        return this.identityKeyPair;
    }

    public List<PreKeyRecord> getPreKeys() {
        return this.preKeys;
    }

    public SignedPreKeyRecord getSignedPreKey() {
        return this.signedPreKey;
    }

    public int getRegistrationId() {
        return this.registrationId;
    }

    public SessionStore getSessionStore() {
        return this.sessionStore;
    }

    public PreKeyStore getPreKeyStore() {
        return this.preKeyStore;
    }

    public SignedPreKeyStore getSignedPreKeyStore() {
        return this.signedPreKeyStore;
    }

    public IdentityKeyStore getIdentityKeyStore() {
        return this.identityKeyStore;
    }

}
