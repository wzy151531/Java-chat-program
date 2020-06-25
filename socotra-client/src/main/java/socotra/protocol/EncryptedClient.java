package socotra.protocol;

import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.state.*;
import org.whispersystems.libsignal.util.KeyHelper;
import socotra.Client;

import java.util.ArrayList;
import java.util.List;

public class EncryptedClient {

    private IdentityKeyPair identityKeyPair;
    private int registrationId;
    private List<PreKeyRecord> preKeys;
    private SignedPreKeyRecord signedPreKey;
    private SessionStore sessionStore;
    private PreKeyStore preKeyStore;
    private SignedPreKeyStore signedPreKeyStore;
    private IdentityKeyStore identityKeyStore;

    public EncryptedClient() {
        Client.setEncryptedClient(this);
        try {
            create();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        init();
    }

    public EncryptedClient(String username) {
        Client.setEncryptedClient(this);
        // load();
        init();
    }

    private void create() throws InvalidKeyException {
        this.identityKeyPair = KeyHelper.generateIdentityKeyPair();
        this.registrationId = KeyHelper.generateRegistrationId(false);
        this.preKeys = KeyHelper.generatePreKeys(0, 10);
        this.signedPreKey = KeyHelper.generateSignedPreKey(this.identityKeyPair, this.registrationId);
    }

    private void init() {
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
        signedPreKeyStore.storeSignedPreKey(this.signedPreKey.getId(), this.signedPreKey);
    }

    public IdentityKeyPair getIdentityKeyPair() {
        return this.identityKeyPair;
    }

    public List<PreKeyRecord> getPreKeys() {
        return this.preKeys;
    }

    public List<byte[]> getFlattenedPreKeys() {
        List<byte[]> result = new ArrayList<>();
        for (PreKeyRecord pk : this.preKeys) {
            result.add(pk.getKeyPair().getPublicKey().serialize());
        }
        return result;
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
