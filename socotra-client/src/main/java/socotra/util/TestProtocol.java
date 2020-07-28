package socotra.util;

import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.SessionBuilder;
import org.whispersystems.libsignal.SessionCipher;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.ecc.ECPublicKey;
import org.whispersystems.libsignal.fingerprint.Fingerprint;
import org.whispersystems.libsignal.fingerprint.NumericFingerprintGenerator;
import org.whispersystems.libsignal.groups.GroupCipher;
import org.whispersystems.libsignal.groups.GroupSessionBuilder;
import org.whispersystems.libsignal.groups.SenderKeyName;
import org.whispersystems.libsignal.groups.state.SenderKeyRecord;
import org.whispersystems.libsignal.protocol.CiphertextMessage;
import org.whispersystems.libsignal.protocol.PreKeySignalMessage;
import org.whispersystems.libsignal.protocol.SenderKeyDistributionMessage;
import org.whispersystems.libsignal.protocol.SignalMessage;
import org.whispersystems.libsignal.state.PreKeyBundle;
import socotra.common.User;
import socotra.protocol.EncryptedClient;
import socotra.protocol.FileEncrypter;

import java.nio.charset.StandardCharsets;

public class TestProtocol {
    private static EncryptedClient tc1;
    private static EncryptedClient tc2;
    private static EncryptedClient tc3;
    private static EncryptedClient tc4;
    private static SignalProtocolAddress signalProtocolAddress1 = new SignalProtocolAddress("tc1", 1);
    private static SignalProtocolAddress signalProtocolAddress2 = new SignalProtocolAddress("tc2", 1);
    private static SignalProtocolAddress signalProtocolAddress3 = new SignalProtocolAddress("tc3", 1);
    private static SignalProtocolAddress signalProtocolAddress4 = new SignalProtocolAddress("tc4", 1);
    private static String groupId = "G42";

    static {
        try {
            tc1 = new EncryptedClient(new User("tc1", 1, true)); // tc1
            tc2 = new EncryptedClient(new User("tc2", 1, true)); // tc2
            tc3 = new EncryptedClient(new User("tc3", 1, true)); // tc3
            tc4 = new EncryptedClient(new User("tc4", 1, true)); // tc4
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void test() throws Exception {

        SessionBuilder sessionBuilder1To2 = new SessionBuilder(tc1.getSessionStore(), tc1.getPreKeyStore(), tc1.getSignedPreKeyStore(),
                tc1.getIdentityKeyStore(), signalProtocolAddress2);

//        ECPublicKey preKeyTemp = Curve.decodePoint(preKey, 0);
        ECPublicKey preKeyTemp = tc2.getPreKeys().get(0).getKeyPair().getPublicKey();
        int preKeyIdTemp = tc2.getPreKeys().get(0).getId();

//        IdentityKey identityKeyTemp = new IdentityKey(identityKey, 0);
        IdentityKey identityKeyTemp = tc2.getIdentityKeyPair().getPublicKey();
        int signedPreKeyId = tc2.getSignedPreKey().getId();
        ECPublicKey signedPreKeyPub = tc2.getSignedPreKey().getKeyPair().getPublicKey();
        byte[] signedPreKeySig = tc2.getSignedPreKey().getSignature();

        PreKeyBundle preKeyBundle2_1 = new PreKeyBundle(tc2.getRegistrationId(), 1, preKeyIdTemp, preKeyTemp,
                signedPreKeyId, signedPreKeyPub, signedPreKeySig,
                identityKeyTemp);
        sessionBuilder1To2.process(preKeyBundle2_1);

        SessionCipher sessionCipher1To2 = new SessionCipher(tc1.getSessionStore(), tc1.getPreKeyStore(), tc1.getSignedPreKeyStore(),
                tc1.getIdentityKeyStore(), signalProtocolAddress2);
        SessionCipher sessionCipher2To1 = new SessionCipher(tc2.getSessionStore(), tc2.getPreKeyStore(), tc2.getSignedPreKeyStore(),
                tc2.getIdentityKeyStore(), signalProtocolAddress1);

        CiphertextMessage message1To2_1 = sessionCipher1To2.encrypt("Hello world!".getBytes(StandardCharsets.UTF_8));

        byte[] result1To2_1 = sessionCipher2To1.decrypt(new PreKeySignalMessage(message1To2_1.serialize()));
        System.out.println(new String(result1To2_1));

        CiphertextMessage message2To1_1 = sessionCipher2To1.encrypt("Hello world back.".getBytes(StandardCharsets.UTF_8));

        byte[] result2To1_1 = sessionCipher1To2.decrypt(new SignalMessage(message2To1_1.serialize()));
        System.out.println(new String(result2To1_1));

        // TODO tc2 update signed pre key and tc1 create a new sessionCipher with him.
        tc1.updateIdentityKey();
//        int signedPreKeyId_new = tc2.getSignedPreKey().getId();
//        ECPublicKey signedPreKeyPub_new = tc2.getSignedPreKey().getKeyPair().getPublicKey();
//        byte[] signedPreKeySig_new = tc2.getSignedPreKey().getSignature();
//        PreKeyBundle preKeyBundle2_1_2 = new PreKeyBundle(tc2.getRegistrationId(), 1, tc2.getPreKeys().get(1).getId(), tc2.getPreKeys().get(1).getKeyPair().getPublicKey(),
//                signedPreKeyId, signedPreKeyPub, signedPreKeySig,
//                identityKeyTemp);
//        sessionBuilder1To2.process(preKeyBundle2_1_2);

        CiphertextMessage message1To2_2 = sessionCipher1To2.encrypt("Updated".getBytes(StandardCharsets.UTF_8));

        byte[] result1To2_2 = sessionCipher2To1.decrypt(new SignalMessage(message1To2_2.serialize()));
        System.out.println(new String(result1To2_2));


        // TODO the preKey can be null, and whilst the preKeyId can be random.
//        PreKeyBundle preKeyBundle2_2 = new PreKeyBundle(tc2.getRegistrationId(), 1, 0, null,
//                tc2.getSignedPreKey().getId(), tc2.getSignedPreKey().getKeyPair().getPublicKey(), tc2.getSignedPreKey().getSignature(),
//                tc2.getIdentityKeyPair().getPublicKey());
//        sessionBuilder1To2.process(preKeyBundle2_2);
//        CiphertextMessage message1To2_6 = sessionCipher1To2.encrypt("Hi again".getBytes(StandardCharsets.UTF_8));
//        byte[] result1To2_6 = sessionCipher2To1.decrypt(new PreKeySignalMessage(message1To2_6.serialize()));
//        System.out.println(new String(result1To2_6));

    }

    public static void testGroup() throws Exception {
        GroupSessionBuilder groupSessionBuilder_1 = new GroupSessionBuilder(tc1.getSenderKeyStore());
        GroupSessionBuilder groupSessionBuilder_2 = new GroupSessionBuilder(tc2.getSenderKeyStore());
        SenderKeyName senderKeyName_1 = new SenderKeyName(groupId, signalProtocolAddress1);
        SenderKeyName senderKeyName_2 = new SenderKeyName(groupId, signalProtocolAddress2);
        SenderKeyDistributionMessage senderKeyDistributionMessage_1 = groupSessionBuilder_1.create(senderKeyName_1);
        SenderKeyDistributionMessage senderKeyDistributionMessage_2 = groupSessionBuilder_2.create(senderKeyName_2);

        groupSessionBuilder_1.process(senderKeyName_2, senderKeyDistributionMessage_2);
        groupSessionBuilder_2.process(senderKeyName_1, senderKeyDistributionMessage_1);

        GroupCipher groupCipher_1_send = new GroupCipher(tc1.getSenderKeyStore(), senderKeyName_1);
        byte[] message1_1 = groupCipher_1_send.encrypt("Hello guys".getBytes(StandardCharsets.UTF_8));

        // TODO: before create own new sender key, delete previous sender key.
        tc1.getSenderKeyStore().storeSenderKey(senderKeyName_1, new SenderKeyRecord());
        SenderKeyDistributionMessage senderKeyDistributionMessage_1_2 = groupSessionBuilder_1.create(senderKeyName_1);
        groupSessionBuilder_2.process(senderKeyName_1, senderKeyDistributionMessage_1_2);

        byte[] message1_2_1 = groupCipher_1_send.encrypt("New".getBytes(StandardCharsets.UTF_8));

        GroupCipher groupCipher_2_rece_1 = new GroupCipher(tc2.getSenderKeyStore(), senderKeyName_1);
        byte[] result1_1 = groupCipher_2_rece_1.decrypt(message1_1);
        System.out.println(new String(result1_1));
        byte[] result1_2_1 = groupCipher_2_rece_1.decrypt(message1_2_1);
        System.out.println(new String(result1_2_1));

        GroupCipher groupCipher_2_send = new GroupCipher(tc2.getSenderKeyStore(), senderKeyName_2);
        byte[] message2_1 = groupCipher_2_send.encrypt("Alright".getBytes(StandardCharsets.UTF_8));

        GroupCipher groupCipher_1_rece_2 = new GroupCipher(tc1.getSenderKeyStore(), senderKeyName_2);
        byte[] result2_1 = groupCipher_1_rece_2.decrypt(message2_1);
        System.out.println(new String(result2_1));
    }

    public static void testAES() {
        try {
            FileEncrypter fileEncrypter = new FileEncrypter(new User("1", 1, true));
            fileEncrypter.encrypt("identityKeyStore.csv", "identityKeyStore.csv");
            fileEncrypter.decrypt("identityKeyStore.csv", "identityKeyStore.csv");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void testFingerprint() throws Exception {
        test();
        NumericFingerprintGenerator nfg = new NumericFingerprintGenerator(5200);
        Fingerprint fp1_2 = nfg.createFor(1, tc1.getIdentifier(), tc1.getIdentityKeyPair().getPublicKey(), tc2.getIdentifier(), tc1.getIdentityKeyStore().getIdentity(signalProtocolAddress2));
        Fingerprint fp2_1 = nfg.createFor(1, tc2.getIdentifier(), tc2.getIdentityKeyPair().getPublicKey(), tc1.getIdentifier(), tc2.getIdentityKeyStore().getIdentity(signalProtocolAddress1));
        Fingerprint fp1_3 = nfg.createFor(1, tc1.getIdentifier(), tc1.getIdentityKeyPair().getPublicKey(), tc3.getIdentifier(), tc3.getIdentityKeyPair().getPublicKey());
        System.out.println(fp1_2.getDisplayableFingerprint().getDisplayText());
        System.out.println(fp2_1.getDisplayableFingerprint().getDisplayText());
        System.out.println(fp1_3.getDisplayableFingerprint().getDisplayText());
    }

}
