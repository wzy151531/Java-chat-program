package socotra.protocol;

import org.whispersystems.libsignal.InvalidKeyIdException;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.PreKeyStore;

import java.io.IOException;
import java.util.HashMap;

public class MyPreKeyStore implements PreKeyStore {

    private final HashMap<Integer, PreKeyRecord> preKeyMap;

    MyPreKeyStore() {
        this.preKeyMap = new HashMap<>();
    }

    HashMap<String, byte[]> getFormattedPreKeyMap() {
        HashMap<String, byte[]> result = new HashMap<>();
        preKeyMap.forEach((k, v) -> {
            result.put(k.toString(), v.serialize());
        });
        return result;
    }

    @Override
    public PreKeyRecord loadPreKey(int preKeyId) throws InvalidKeyIdException {
        try {
            if (!this.preKeyMap.containsKey(preKeyId)) {
                throw new InvalidKeyIdException("Unknown preKey: " + preKeyId);
            }
            return new PreKeyRecord(this.preKeyMap.get(preKeyId).serialize());
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public void storePreKey(int preKeyId, PreKeyRecord record) {
        this.preKeyMap.put(preKeyId, record);
    }

    @Override
    public boolean containsPreKey(int preKeyId) {
        return this.preKeyMap.containsKey(preKeyId);
    }

    @Override
    public void removePreKey(int preKeyId) {
        this.preKeyMap.remove(preKeyId);
    }

}
