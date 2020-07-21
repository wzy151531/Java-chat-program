package socotra.protocol;

import org.whispersystems.libsignal.groups.SenderKeyName;
import org.whispersystems.libsignal.groups.state.SenderKeyRecord;
import org.whispersystems.libsignal.groups.state.SenderKeyStore;

import java.io.IOException;
import java.util.HashMap;

public class MySenderKeyStore implements SenderKeyStore {

    private final HashMap<SenderKeyName, SenderKeyRecord> senderKeyMap;

    MySenderKeyStore() {
        this.senderKeyMap = new HashMap<>();
    }

    HashMap<String, byte[]> getFormattedSenderKeyMap() {
        HashMap<String, byte[]> result = new HashMap<>();
        senderKeyMap.forEach((k, v) -> {
            result.put(k.serialize(), v.serialize());
        });
        return result;
    }

    @Override
    public void storeSenderKey(SenderKeyName senderKeyName, SenderKeyRecord record) {
        this.senderKeyMap.put(senderKeyName, record);
    }

    @Override
    public SenderKeyRecord loadSenderKey(SenderKeyName senderKeyName) {
        try {
            if (!this.senderKeyMap.containsKey(senderKeyName)) {
                return new SenderKeyRecord();
            }
            return new SenderKeyRecord(this.senderKeyMap.get(senderKeyName).serialize());
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

}
