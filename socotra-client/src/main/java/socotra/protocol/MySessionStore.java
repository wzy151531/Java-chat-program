package socotra.protocol;

import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.state.SessionRecord;
import org.whispersystems.libsignal.state.SessionStore;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class MySessionStore implements SessionStore {

    private final HashMap<SignalProtocolAddress, SessionRecord> sessionMap;

    MySessionStore() {
        this.sessionMap = new HashMap<>();
    }

    public HashMap<String, byte[]> getFormattedSessionMap() {
        HashMap<String, byte[]> result = new HashMap<>();
        sessionMap.forEach((k, v) -> {
            result.put(k.getName(), v.serialize());
        });
        return result;
    }

    @Override
    public synchronized SessionRecord loadSession(SignalProtocolAddress address) {
        if (containsSession(address)) {
            try {
                return new SessionRecord(this.sessionMap.get(address).serialize());
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
        return new SessionRecord();
    }

    @Override
    public synchronized List<Integer> getSubDeviceSessions(String name) {
        List<Integer> result = new LinkedList<>();
        for (SignalProtocolAddress key : this.sessionMap.keySet()) {
            if (key.getName().equals(name) && key.getDeviceId() != 1) {
                result.add(key.getDeviceId());
            }
        }
        return result;
    }

    @Override
    public synchronized void storeSession(SignalProtocolAddress address, SessionRecord record) {
        this.sessionMap.put(address, record);
    }

    @Override
    public synchronized boolean containsSession(SignalProtocolAddress address) {
        return this.sessionMap.containsKey(address);
    }

    synchronized boolean containsRelatedSession(String username) {
        Set<SignalProtocolAddress> keySet = sessionMap.keySet();
        for (SignalProtocolAddress addr : keySet) {
            if (addr.getName().equals(username)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized void deleteSession(SignalProtocolAddress address) {
        this.sessionMap.remove(address);
    }

    @Override
    public synchronized void deleteAllSessions(String name) {
        for (SignalProtocolAddress key : this.sessionMap.keySet()) {
            if (key.getName().equals(name)) {
                this.sessionMap.remove(key);
            }
        }
    }

}
