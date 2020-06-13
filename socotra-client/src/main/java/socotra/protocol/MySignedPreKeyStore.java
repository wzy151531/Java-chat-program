package socotra.protocol;

import org.whispersystems.libsignal.InvalidKeyIdException;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import org.whispersystems.libsignal.state.SignedPreKeyStore;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class MySignedPreKeyStore implements SignedPreKeyStore {

    private final HashMap<Integer, SignedPreKeyRecord> signedPreKeyMap;

    public MySignedPreKeyStore() {
        this.signedPreKeyMap = new HashMap<>();
    }

    @Override
    public SignedPreKeyRecord loadSignedPreKey(int signedPreKeyId) throws InvalidKeyIdException {
        try {
            if (!this.signedPreKeyMap.containsKey(signedPreKeyId)) {
                System.out.println(this.signedPreKeyMap.keySet());
                throw new InvalidKeyIdException("Unknown signedPreKey: " + signedPreKeyId);
            }
            return new SignedPreKeyRecord(this.signedPreKeyMap.get(signedPreKeyId).serialize());
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public List<SignedPreKeyRecord> loadSignedPreKeys() {
        try {
            List<SignedPreKeyRecord> results = new LinkedList<>();
            for (SignedPreKeyRecord signedPreKeyRecord : this.signedPreKeyMap.values()) {
                results.add(new SignedPreKeyRecord(signedPreKeyRecord.serialize()));
            }
            return results;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public void storeSignedPreKey(int signedPreKeyId, SignedPreKeyRecord record) {
        this.signedPreKeyMap.put(signedPreKeyId, record);
    }

    @Override
    public boolean containsSignedPreKey(int signedPreKeyId) {
        return this.signedPreKeyMap.containsKey(signedPreKeyId);
    }

    @Override
    public void removeSignedPreKey(int signedPreKeyId) {
        this.signedPreKeyMap.remove(signedPreKeyId);
    }

}
