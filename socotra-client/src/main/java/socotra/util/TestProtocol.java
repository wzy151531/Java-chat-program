package socotra.util;

import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.SessionBuilder;
import org.whispersystems.libsignal.SessionCipher;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.ecc.ECPublicKey;
import org.whispersystems.libsignal.groups.GroupCipher;
import org.whispersystems.libsignal.groups.GroupSessionBuilder;
import org.whispersystems.libsignal.groups.SenderKeyName;
import org.whispersystems.libsignal.protocol.CiphertextMessage;
import org.whispersystems.libsignal.protocol.PreKeySignalMessage;
import org.whispersystems.libsignal.protocol.SenderKeyDistributionMessage;
import org.whispersystems.libsignal.protocol.SignalMessage;
import org.whispersystems.libsignal.state.PreKeyBundle;
import org.whispersystems.libsignal.state.PreKeyRecord;
import socotra.protocol.EncryptedClient;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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
            tc1 = new EncryptedClient(); // tc1
            tc2 = new EncryptedClient(); // tc2
            tc3 = new EncryptedClient(); // tc3
            tc4 = new EncryptedClient(); // tc4
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

        PreKeyBundle preKeyBundle2_1 = new PreKeyBundle(tc2.getRegistrationId(), 1, preKeyIdTemp, preKeyTemp,
                tc2.getSignedPreKey().getId(), tc2.getSignedPreKey().getKeyPair().getPublicKey(), tc2.getSignedPreKey().getSignature(),
                identityKeyTemp);
        sessionBuilder1To2.process(preKeyBundle2_1);

        SessionCipher sessionCipher1To2 = new SessionCipher(tc1.getSessionStore(), tc1.getPreKeyStore(), tc1.getSignedPreKeyStore(), tc1.getIdentityKeyStore(), signalProtocolAddress2);

        CiphertextMessage message1To2_1 = sessionCipher1To2.encrypt("Hello world!".getBytes(StandardCharsets.UTF_8));
        CiphertextMessage message1To2_2 = sessionCipher1To2.encrypt("Perfect".getBytes(StandardCharsets.UTF_8));

        SessionCipher sessionCipher2To1 = new SessionCipher(tc2.getSessionStore(), tc2.getPreKeyStore(), tc2.getSignedPreKeyStore(),
                tc2.getIdentityKeyStore(), signalProtocolAddress1);
        byte[] result1To2_1 = sessionCipher2To1.decrypt(new PreKeySignalMessage(message1To2_1.serialize()));
        System.out.println(new String(result1To2_1));

        byte[] result1To2_2 = sessionCipher2To1.decrypt(new PreKeySignalMessage(message1To2_2.serialize()));
        System.out.println(new String(result1To2_2));

        CiphertextMessage message2To1_1 = sessionCipher2To1.encrypt("Yes".getBytes(StandardCharsets.UTF_8));
        CiphertextMessage message1To2_3 = sessionCipher1To2.encrypt("Great".getBytes(StandardCharsets.UTF_8));

        byte[] result2To1_1 = sessionCipher1To2.decrypt(new SignalMessage(message2To1_1.serialize()));
        System.out.println(new String(result2To1_1));

        byte[] result1To2_3 = sessionCipher2To1.decrypt(new PreKeySignalMessage(message1To2_3.serialize()));
        System.out.println(new String(result1To2_3));

        // TODO tc2 update signed pre key and tc1 create a new sessionCipher with him.
        tc2.updateSignedPreKey();
        PreKeyBundle preKeyBundle2_1_2 = new PreKeyBundle(tc2.getRegistrationId(), 1, tc2.getPreKeys().get(1).getId(), tc2.getPreKeys().get(1).getKeyPair().getPublicKey(),
                tc2.getSignedPreKey().getId(), tc2.getSignedPreKey().getKeyPair().getPublicKey(), tc2.getSignedPreKey().getSignature(),
                identityKeyTemp);
        sessionBuilder1To2.process(preKeyBundle2_1_2);


        // The order does not matter.
        CiphertextMessage message1To2_4 = sessionCipher1To2.encrypt("4".getBytes(StandardCharsets.UTF_8));

//        CiphertextMessage message1To2_5 = sessionCipher1To2.encrypt("5".getBytes(StandardCharsets.UTF_8));
//
//        byte[] result1To2_5 = sessionCipher2To1.decrypt(new SignalMessage(message1To2_5.serialize()));
//        System.out.println(new String(result1To2_5));

        byte[] result1To2_4 = sessionCipher2To1.decrypt(new PreKeySignalMessage(message1To2_4.serialize()));
        System.out.println(new String(result1To2_4));


        // TODO the preKey can be null, and whilst the preKeyId can be random.
        PreKeyBundle preKeyBundle2_2 = new PreKeyBundle(tc2.getRegistrationId(), 1, 0, null,
                tc2.getSignedPreKey().getId(), tc2.getSignedPreKey().getKeyPair().getPublicKey(), tc2.getSignedPreKey().getSignature(),
                tc2.getIdentityKeyPair().getPublicKey());
        sessionBuilder1To2.process(preKeyBundle2_2);
        CiphertextMessage message1To2_6 = sessionCipher1To2.encrypt("Hi again".getBytes(StandardCharsets.UTF_8));
        byte[] result1To2_6 = sessionCipher2To1.decrypt(new PreKeySignalMessage(message1To2_6.serialize()));
        System.out.println(new String(result1To2_6));

    }

    public static void testGroup() throws Exception {
        GroupSessionBuilder groupSessionBuilder_1 = new GroupSessionBuilder(tc1.getSenderKeyStore());
        GroupSessionBuilder groupSessionBuilder_2 = new GroupSessionBuilder(tc2.getSenderKeyStore());
        SenderKeyDistributionMessage senderKeyDistributionMessage_1 = groupSessionBuilder_1.create(new SenderKeyName(groupId, signalProtocolAddress1));
        SenderKeyDistributionMessage senderKeyDistributionMessage_2 = groupSessionBuilder_2.create(new SenderKeyName(groupId, signalProtocolAddress2));
        groupSessionBuilder_1.process(new SenderKeyName(groupId, signalProtocolAddress2), senderKeyDistributionMessage_2);
        groupSessionBuilder_2.process(new SenderKeyName(groupId, signalProtocolAddress1), senderKeyDistributionMessage_1);

        GroupCipher groupCipher_1_send = new GroupCipher(tc1.getSenderKeyStore(), new SenderKeyName(groupId, signalProtocolAddress1));
        byte[] message1_1 = groupCipher_1_send.encrypt("Hello guys".getBytes(StandardCharsets.UTF_8));

        GroupCipher groupCipher_2_rece_1 = new GroupCipher(tc2.getSenderKeyStore(), new SenderKeyName(groupId, signalProtocolAddress1));
        byte[] result1_1 = groupCipher_2_rece_1.decrypt(message1_1);
        System.out.println(new String(result1_1));

        GroupCipher groupCipher_2_send = new GroupCipher(tc2.getSenderKeyStore(), new SenderKeyName(groupId, signalProtocolAddress2));
        byte[] message2_1 = groupCipher_2_send.encrypt("Alright".getBytes(StandardCharsets.UTF_8));

        GroupCipher groupCipher_1_rece_2 = new GroupCipher(tc1.getSenderKeyStore(), new SenderKeyName(groupId, signalProtocolAddress2));
        byte[] result2_1 = groupCipher_1_rece_2.decrypt(message2_1);
        System.out.println(new String(result2_1));
    }
}
