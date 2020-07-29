package socotra.protocol;

import javafx.application.Platform;
import org.whispersystems.libsignal.*;
import org.whispersystems.libsignal.ecc.Curve;
import org.whispersystems.libsignal.groups.GroupCipher;
import org.whispersystems.libsignal.groups.GroupSessionBuilder;
import org.whispersystems.libsignal.groups.SenderKeyName;
import org.whispersystems.libsignal.groups.state.SenderKeyRecord;
import org.whispersystems.libsignal.protocol.SenderKeyDistributionMessage;
import org.whispersystems.libsignal.state.*;
import org.whispersystems.libsignal.util.KeyHelper;
import socotra.Client;
import socotra.common.ChatSession;
import socotra.common.ConnectionData;
import socotra.common.KeyBundle;
import socotra.common.User;
import socotra.model.DataHandler;
import socotra.util.SendThread;

import java.nio.charset.StandardCharsets;
import java.util.*;

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

    private SenderKeyDistributionMessage SKDM;

    public EncryptedClient(User user) {
        this.signalProtocolAddress = new SignalProtocolAddress(user.getUsername(), user.getDeviceId());
        Client.setEncryptedClient(this);
        try {
            create();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        init();
        register();
    }

    EncryptedClient(byte[] serializedIdentityKeyPair, int registrationId, User user) {
        this.signalProtocolAddress = new SignalProtocolAddress(user.getUsername(), user.getDeviceId());
        Client.setEncryptedClient(this);
        try {
            load(serializedIdentityKeyPair, registrationId);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        init();
    }

    public SenderKeyDistributionMessage getSKDM() {
        if (SKDM == null) {
            throw new IllegalStateException("SKDM is null.");
        }
        return this.SKDM;
    }

    public User getUser() {
        return new User(signalProtocolAddress.getName(), signalProtocolAddress.getDeviceId(), true);
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
     * @param receiver The user information of receiver.
     */
    public void requestKeyBundle(User receiver, boolean reInit) {
        new SendThread(new ConnectionData(receiver, Client.getClientThread().getUser(), reInit)).start();
    }

    public void initPairwiseChat(KeyBundle keyBundle, User receiver) throws InvalidKeyException, UntrustedIdentityException {
        SessionBuilder sessionBuilder = new SessionBuilder(sessionStore, preKeyStore, signedPreKeyStore, identityKeyStore,
                new SignalProtocolAddress(receiver.getUsername(), receiver.getDeviceId()));
        PreKeyBundle preKeyBundle = new PreKeyBundle(keyBundle.getRegistrationId(), 1, keyBundle.getPreKeyId(),
                keyBundle.getPreKey() == null ? null : Curve.decodePoint(keyBundle.getPreKey(), 0), keyBundle.getSignedPreKeyId(),
                Curve.decodePoint(keyBundle.getSignedPreKey(), 0), keyBundle.getSignedPreKeySignature(),
                new IdentityKey(keyBundle.getIdentityKey(), 0));
        sessionBuilder.process(preKeyBundle);
    }

    public void finishInitPairwiseChat(User receiver) {
        TreeSet<User> members = new TreeSet<>();
        members.add(receiver);
        members.add(getUser());
        ChatSession chatSession = new ChatSession(members, true, true, ChatSession.PAIRWISE);
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
    public void initGroupChat(ChatSession chatSession, SenderKeyDistributionMessage SKDM, boolean init) {
        Platform.runLater(() -> {
            Client.showInitGroupChatAlert();
            TreeSet<User> others = chatSession.getOthers(getUser());
            TreeSet<User> unknownOthers = getUnknownOthers(others);
            if (unknownOthers.isEmpty()) {
                distributeSenderKey(others, chatSession, SKDM);
                finishInitGroupChat(chatSession, init);
            } else {
                this.SKDM = SKDM;
                requestKeyBundles(unknownOthers, chatSession, init);
            }
        });
    }

    public void distributeSenderKey(TreeSet<User> others, ChatSession chatSession, SenderKeyDistributionMessage SKDM) {
        HashMap<User, ConnectionData> senderKeysData = new HashMap<>();
        others.forEach(n -> {
            try {
                senderKeysData.put(n, EncryptionHandler.encryptSKDMData(SKDM.serialize(), chatSession, n));
            } catch (UntrustedIdentityException e) {
                e.printStackTrace();
            }
        });
        new SendThread(new ConnectionData(senderKeysData)).start();
    }

    public void processReceivedSenderKey(byte[] senderKey, ChatSession chatSession, User sender, boolean init) {
        try {
            storeReceivedSenderKey(senderKey, chatSession.generateChatIdCSV(), sender);

            SenderKeyName senderKeyName = new SenderKeyName(chatSession.generateChatIdCSV(), signalProtocolAddress);
            SenderKeyRecord senderKeyRecord = senderKeyStore.loadSenderKey(senderKeyName);
            boolean isFresh = senderKeyRecord.isEmpty();

            DataHandler dataHandler = Client.getDataHandler();
            if (isFresh) {
                SenderKeyDistributionMessage SKDM = createSenderKey(chatSession.generateChatIdCSV());
                initGroupChat(chatSession, SKDM, init);
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

    public void finishInitGroupChat(ChatSession chatSession, boolean init) {
        DataHandler dataHandler = Client.getDataHandler();
        if (init && !dataHandler.isGroupDataNull()) {
            dataHandler.processGroupData();
        } else {
            Client.getHomeModel().appendChatData(chatSession);
        }
        Client.closeInitGroupChatAlert();
    }

    private TreeSet<User> getUnknownOthers(TreeSet<User> others) {
        TreeSet<User> result = new TreeSet<>();
        others.forEach(n -> {
            if (!sessionStore.containsSession(new SignalProtocolAddress(n.getUsername(), n.getDeviceId()))) {
                result.add(n);
            }
        });
        return result;
    }

    private void requestKeyBundles(TreeSet<User> unknownOthers, ChatSession chatSession, boolean init) {
        new SendThread(new ConnectionData(unknownOthers, chatSession, getUser(), init)).start();
    }

    SessionCipher getSessionCipher(User theOther) {
        SignalProtocolAddress theOtherAddress = new SignalProtocolAddress(theOther.getUsername(), theOther.getDeviceId());
        return new SessionCipher(sessionStore, preKeyStore,
                signedPreKeyStore, identityKeyStore, theOtherAddress);
    }

    GroupCipher getGroupCipher(User sender, ChatSession chatSession) {
        String groupId = chatSession.generateChatIdCSV();
        SignalProtocolAddress signalProtocolAddress = new SignalProtocolAddress(sender.getUsername(), sender.getDeviceId());
        return new GroupCipher(senderKeyStore, new SenderKeyName(groupId, signalProtocolAddress));
    }

    public SenderKeyDistributionMessage createSenderKey(String groupId) {
        GroupSessionBuilder groupSessionBuilder = new GroupSessionBuilder(senderKeyStore);
        SenderKeyName senderKeyName = new SenderKeyName(groupId, signalProtocolAddress);
        SenderKeyRecord senderKeyRecord = senderKeyStore.loadSenderKey(senderKeyName);
        if (!senderKeyRecord.isEmpty()) {
            throw new IllegalStateException("Sender key already exists.");
        }
        return groupSessionBuilder.create(senderKeyName);
    }

    private SenderKeyDistributionMessage updateSenderKey(String groupId) {
        SenderKeyName senderKeyName = new SenderKeyName(groupId, signalProtocolAddress);
        SenderKeyRecord senderKeyRecord = senderKeyStore.loadSenderKey(senderKeyName);
        if (!senderKeyRecord.isEmpty()) {
            senderKeyStore.storeSenderKey(senderKeyName, new SenderKeyRecord());
        }
        return createSenderKey(groupId);
    }

    private void storeReceivedSenderKey(byte[] senderKey, String groupId, User sender) {
        try {
            SenderKeyDistributionMessage SKDM = new SenderKeyDistributionMessage(senderKey);
            GroupSessionBuilder groupSessionBuilder = new GroupSessionBuilder(senderKeyStore);
            groupSessionBuilder.process(new SenderKeyName(groupId,
                    new SignalProtocolAddress(sender.getUsername(), sender.getDeviceId())), SKDM);
        } catch (InvalidMessageException | LegacyMessageException e) {
            System.out.println("Bad senderKey.");
        }
    }

    public void checkPairwiseCipher(User user) {
        System.out.println("check pairwise cipher");
        SignalProtocolAddress userAddress = new SignalProtocolAddress(user.getUsername(), user.getDeviceId());
        if (!sessionStore.containsSession(userAddress) &&
                sessionStore.containsRelatedSession(user.getUsername())) {
            System.out.println("ReInit pairwise chat.");
            EncryptedClient encryptedClient = Client.getEncryptedClient();
            encryptedClient.requestKeyBundle(user, true);
        } else {
            Client.closeReInitChatAlert();
        }
    }

    public void checkGroupCipher(User user) {
        System.out.println("check group cipher");
        Set<String> relatedGroups = senderKeyStore.containsRelatedSenderKey(user);
        if (!senderKeyStore.containsExactSenderKey(
                new SignalProtocolAddress(user.getUsername(), user.getDeviceId())) &&
                !relatedGroups.isEmpty()) {
            System.out.println("ReInit group chat.");
            relatedGroups.forEach(n -> {
                System.out.println("Update sender key for: " + n);
                SenderKeyDistributionMessage SKDM = updateSenderKey(n);
                ChatSession chatSession = new ChatSession(n);
                distributeSenderKey(chatSession.getOthers(
                        new User(signalProtocolAddress.getName(), signalProtocolAddress.getDeviceId(), true))
                        , chatSession, SKDM);
            });
        }
    }

}
