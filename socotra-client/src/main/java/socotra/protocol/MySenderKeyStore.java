package socotra.protocol;

import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.groups.SenderKeyName;
import org.whispersystems.libsignal.groups.state.SenderKeyRecord;
import org.whispersystems.libsignal.groups.state.SenderKeyStore;
import socotra.common.ChatSession;
import socotra.common.User;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

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

    public void printSenderKeyNames() {
        System.out.println("senderKeyMap:");
        senderKeyMap.keySet().forEach(n -> {
            System.out.println("    " + n.getGroupId() + ", " + n.getSender());
        });
    }

    boolean containsExactSenderKey(SignalProtocolAddress userAddress) {
        Set<SenderKeyName> keySet = senderKeyMap.keySet();
        for (SenderKeyName skn : keySet) {
            if (skn.getSender().equals(userAddress)) {
                return true;
            }
        }
        return false;
    }

    Set<String> containsRelatedSenderKey(User user) {
//        System.out.println("Before:");
//        printSenderKeyNames();

        Set<String> result = new TreeSet<>();
        Set<SenderKeyName> keySet = senderKeyMap.keySet();
        Set<SenderKeyName> deletedGroupId = new HashSet<>();
        for (SenderKeyName skn : keySet) {
            if (skn.getSender().getName().equals(user.getUsername()) && skn.getSender().getDeviceId() != user.getDeviceId()) {
                ChatSession oldSession = new ChatSession(skn.getGroupId());
                TreeSet<User> copy = new TreeSet<>(oldSession.getMembers());
                User pre = new User(skn.getSender().getName(), skn.getSender().getDeviceId(), true);
                copy.remove(pre);
                copy.add(user);
                ChatSession newSession = new ChatSession(copy, false, true, oldSession.getSessionType());
                result.add(newSession.generateChatIdCSV());

                oldSession.getMembers().forEach(n -> {
                    deletedGroupId.add(new SenderKeyName(skn.getGroupId(), new SignalProtocolAddress(n.getUsername(), n.getDeviceId())));
                });
            }
        }
        deletedGroupId.forEach(n -> {
            senderKeyMap.remove(n);
        });

//        System.out.println("After:");
//        printSenderKeyNames();
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
