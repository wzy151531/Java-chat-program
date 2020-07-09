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
    private MySessionStore sessionStore;
    private MyPreKeyStore preKeyStore;
    private MySignedPreKeyStore signedPreKeyStore;
    private MyIdentityKeyStore identityKeyStore;
    private MySenderKeyStore senderKeyStore;

    public EncryptedClient() {
        Client.setEncryptedClient(this);
        try {
            create();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        init();
        register();
    }

    EncryptedClient(byte[] serializedIdentityKeyPair, int registrationId) {
        Client.setEncryptedClient(this);
        try {
            load(serializedIdentityKeyPair, registrationId);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        init();
    }

    private void load(byte[] serializedIdentityKeyPair, int registrationId) throws InvalidKeyException {
        this.identityKeyPair = new IdentityKeyPair(serializedIdentityKeyPair);
        this.registrationId = registrationId;
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
        this.senderKeyStore = new MySenderKeyStore();
    }

    private void register() {
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

    public void updateSignedPreKey() {
        signedPreKeyStore.removeSignedPreKey(this.signedPreKey.getId());
        try {
            this.signedPreKey = KeyHelper.generateSignedPreKey(this.identityKeyPair, this.registrationId);
            storeSignedPreKey();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
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

    public MySessionStore getSessionStore() {
        return this.sessionStore;
    }

    public MyPreKeyStore getPreKeyStore() {
        return this.preKeyStore;
    }

    public MySignedPreKeyStore getSignedPreKeyStore() {
        return this.signedPreKeyStore;
    }

    public MyIdentityKeyStore getIdentityKeyStore() {
        return this.identityKeyStore;
    }

    public MySenderKeyStore getSenderKeyStore() {
        return this.senderKeyStore;
    }

}
