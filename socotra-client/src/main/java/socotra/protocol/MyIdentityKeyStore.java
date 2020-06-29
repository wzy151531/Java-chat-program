package socotra.protocol;

import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.state.IdentityKeyStore;

import java.io.Serializable;
import java.util.HashMap;

public class MyIdentityKeyStore implements IdentityKeyStore, Serializable {

    private final IdentityKeyPair identityKeyPair;
    private final int localRegistrationId;
    private final HashMap<SignalProtocolAddress, IdentityKey> identityKeyMap;

    MyIdentityKeyStore(IdentityKeyPair identityKeyPair, int localRegistrationId) {
        this.identityKeyPair = identityKeyPair;
        this.localRegistrationId = localRegistrationId;
        this.identityKeyMap = new HashMap<>();
    }

    HashMap<String, byte[]> getFormattedIdentityKeyMap() {
        HashMap<String, byte[]> result = new HashMap<>();
        identityKeyMap.forEach((k, v) -> {
            result.put(k.getName(), v.serialize());
        });
        return result;
    }

    @Override
    public IdentityKeyPair getIdentityKeyPair() {
        return this.identityKeyPair;
    }

    @Override
    public int getLocalRegistrationId() {
        return this.localRegistrationId;
    }

    @Override
    public boolean saveIdentity(SignalProtocolAddress address, IdentityKey identityKey) {
        IdentityKey preIdentityKey = this.identityKeyMap.get(address);
        if (preIdentityKey == null) {
            this.identityKeyMap.put(address, identityKey);
            return false;
        }
        this.identityKeyMap.put(address, identityKey);
        return true;
    }

    @Override
    public boolean isTrustedIdentity(SignalProtocolAddress address, IdentityKey identityKey, Direction direction) {
        IdentityKey trustedIdentityKey = this.identityKeyMap.get(address);
        return trustedIdentityKey == null || trustedIdentityKey.equals(identityKey);
    }

    @Override
    public IdentityKey getIdentity(SignalProtocolAddress address) {
        return this.identityKeyMap.get(address);
    }

}
