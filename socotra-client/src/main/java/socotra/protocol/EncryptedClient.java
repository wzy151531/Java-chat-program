package socotra.protocol;

import javafx.application.Platform;
import org.whispersystems.libsignal.*;
import org.whispersystems.libsignal.ecc.Curve;
import org.whispersystems.libsignal.groups.GroupCipher;
import org.whispersystems.libsignal.groups.GroupSessionBuilder;
import org.whispersystems.libsignal.groups.SenderKeyName;
import org.whispersystems.libsignal.protocol.SenderKeyDistributionMessage;
import org.whispersystems.libsignal.state.*;
import org.whispersystems.libsignal.util.KeyHelper;
import socotra.Client;
import socotra.common.ChatSession;
import socotra.common.ConnectionData;
import socotra.common.KeyBundle;
import socotra.model.DataHandler;
import socotra.util.SendThread;
import sun.misc.Signal;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

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
    private SignalProtocolAddress signalProtocolAddress;

    public EncryptedClient(String username) {
        this.signalProtocolAddress = new SignalProtocolAddress(username, 1);
        Client.setEncryptedClient(this);
        try {
            create();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        init();
        register();
    }

    EncryptedClient(byte[] serializedIdentityKeyPair, int registrationId, String username) {
        this.signalProtocolAddress = new SignalProtocolAddress(username, 1);
        Client.setEncryptedClient(this);
        try {
            load(serializedIdentityKeyPair, registrationId);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        init();
    }

    public SignalProtocolAddress getSignalProtocolAddress() {
        return signalProtocolAddress;
    }

    public byte[] getIdentifier() {
        return signalProtocolAddress.toString().getBytes(StandardCharsets.UTF_8);
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

    public void updateIdentityKey() {
        this.identityKeyPair = KeyHelper.generateIdentityKeyPair();
        this.registrationId = KeyHelper.generateRegistrationId(false);
        this.identityKeyStore = new MyIdentityKeyStore(this.identityKeyPair, registrationId, this.identityKeyStore.getIdentityKeyMap());
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

    /**
     * Request receiver's key bundle from server.
     *
     * @param receiverName The username of receiver.
     */
    public void requestKeyBundle(String receiverName) {
        new SendThread(new ConnectionData(receiverName, Client.getClientThread().getUsername())).start();
    }

    public void initPairwiseChat(KeyBundle keyBundle, String receiverName) throws InvalidKeyException, UntrustedIdentityException {
        SessionBuilder sessionBuilder = new SessionBuilder(sessionStore, preKeyStore, signedPreKeyStore, identityKeyStore,
                new SignalProtocolAddress(receiverName, 1));
        PreKeyBundle preKeyBundle = new PreKeyBundle(keyBundle.getRegistrationId(), 1, keyBundle.getPreKeyId(),
                keyBundle.getPreKey() == null ? null : Curve.decodePoint(keyBundle.getPreKey(), 0), keyBundle.getSignedPreKeyId(),
                Curve.decodePoint(keyBundle.getSignedPreKey(), 0), keyBundle.getSignedPreKeySignature(),
                new IdentityKey(keyBundle.getIdentityKey(), 0));
        sessionBuilder.process(preKeyBundle);
    }

    public void finishInitPairwiseChat(String receiverName) {
        TreeSet<String> users = new TreeSet<>();
        users.add(receiverName);
        users.add(signalProtocolAddress.getName());
        ChatSession chatSession = new ChatSession(users, true, true, ChatSession.PAIRWISE);
        Client.getHomeModel().appendChatSessionList(chatSession);
        Platform.runLater(() -> {
            Client.closeInitPairwiseChatAlert();
        });
    }

    /**
     * Initialize group chat session.
     *
     * @param chatSession The group chat session needs to be initialized.
     */
    public void initGroupChat(ChatSession chatSession, boolean needDistribute, boolean init) {
        String caller = signalProtocolAddress.getName();
        TreeSet<String> others = chatSession.getOthers(caller);
        TreeSet<String> unknownOthers = getUnknownOthers(others);
        if (unknownOthers.isEmpty()) {
            distributeSenderKey(others, chatSession, needDistribute, init);
        } else {
            requestKeyBundles(unknownOthers, chatSession, needDistribute, init);
        }
    }

    public void distributeSenderKey(TreeSet<String> others, ChatSession chatSession, boolean needDistribute, boolean init) {
        GroupSessionBuilder groupSessionBuilder = new GroupSessionBuilder(senderKeyStore);
        SenderKeyDistributionMessage SKDM = groupSessionBuilder.create(
                new SenderKeyName(chatSession.generateChatIdCSV(),
                        signalProtocolAddress));
        HashMap<String, ConnectionData> senderKeysData = new HashMap<>();
        others.forEach(n -> {
            try {
                senderKeysData.put(n, EncryptionHandler.encryptSKDMData(SKDM.serialize(), chatSession, n, needDistribute));
            } catch (UntrustedIdentityException e) {
                e.printStackTrace();
            }
        });
        new SendThread(new ConnectionData(senderKeysData)).start();
        finishInitGroupChat(chatSession, init);
    }

    public void processReceivedSenderKey(byte[] senderKey, ChatSession chatSession, boolean needDistribute, String senderName, boolean init) {
        try {
            SenderKeyDistributionMessage SKDM = new SenderKeyDistributionMessage(senderKey);
            GroupSessionBuilder groupSessionBuilder = new GroupSessionBuilder(senderKeyStore);
            groupSessionBuilder.process(new SenderKeyName(chatSession.generateChatIdCSV(),
                    new SignalProtocolAddress(senderName, 1)), SKDM);
            DataHandler dataHandler = Client.getDataHandler();
            if (needDistribute) {
                initGroupChat(chatSession, false, init);
            } else if (init && !dataHandler.isGroupDataNull()) {
                Platform.runLater(() -> {
                    dataHandler.processGroupData();
                    Client.closeInitClientAlert();
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void finishInitGroupChat(ChatSession chatSession, boolean init) {
        Platform.runLater(() -> {
            if (init) {
                DataHandler dataHandler = Client.getDataHandler();
                dataHandler.processGroupData();
                Client.closeInitClientAlert();
            } else {
                Client.getHomeModel().appendChatData(chatSession);
                Client.closeInitGroupChatAlert();
            }
        });
    }

    private TreeSet<String> getUnknownOthers(TreeSet<String> others) {
        TreeSet<String> result = new TreeSet<>();
        others.forEach(n -> {
            if (!sessionStore.containsSession(new SignalProtocolAddress(n, 1))) {
                result.add(n);
            }
        });
        return result;
    }

    private void requestKeyBundles(TreeSet<String> unknownOthers, ChatSession chatSession, boolean needDistribute, boolean init) {
        new SendThread(new ConnectionData(unknownOthers, chatSession, signalProtocolAddress.getName(), needDistribute, init)).start();
    }

    SessionCipher getSessionCipher(String theOther) {
        SignalProtocolAddress theOtherAddress = new SignalProtocolAddress(theOther, 1);
        return new SessionCipher(sessionStore, preKeyStore,
                signedPreKeyStore, identityKeyStore, theOtherAddress);
    }

    GroupCipher getGroupCipher(String senderName, ChatSession chatSession) {
        String groupId = chatSession.generateChatIdCSV();
        SignalProtocolAddress signalProtocolAddress = new SignalProtocolAddress(senderName, 1);
        return new GroupCipher(senderKeyStore, new SenderKeyName(groupId, signalProtocolAddress));
    }

}
