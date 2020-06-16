package socotra.util;

import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.SessionBuilder;
import org.whispersystems.libsignal.SessionCipher;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.ecc.Curve;
import org.whispersystems.libsignal.ecc.ECPublicKey;
import org.whispersystems.libsignal.protocol.CiphertextMessage;
import org.whispersystems.libsignal.protocol.PreKeySignalMessage;
import org.whispersystems.libsignal.protocol.SignalMessage;
import org.whispersystems.libsignal.state.PreKeyBundle;
import org.whispersystems.libsignal.state.PreKeyRecord;
import socotra.common.ConnectionData;
import socotra.protocol.TestClient;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TestProtocol {
    public static byte[] identityKey;
    public static byte[] preKey;
    public static TestClient tc1;
    public static TestClient tc2;
    public static SignalProtocolAddress signalProtocolAddress1 = new SignalProtocolAddress("Wang", 1);
    public static SignalProtocolAddress signalProtocolAddress2 = new SignalProtocolAddress("Yin", 1);

    static {
        try {
            tc1 = new TestClient(1, "Wang");
            tc2 = new TestClient(2, "Yin");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void init() {
        List<PreKeyRecord> preKeys = tc2.getPreKeys();
        List<byte[]> preKeysList = new ArrayList<>();
        for (PreKeyRecord pk : preKeys) {
            preKeysList.add(pk.getKeyPair().getPublicKey().serialize());
        }
        new SendThread(new ConnectionData(tc2.getIdentityKeyPair().getPublicKey().serialize(), preKeysList, 2)).start();
    }

    public static void query() {
        new SendThread(new ConnectionData(2)).start();
    }

    public static void test() throws Exception {

//        System.out.println("local: " + new String(tc2.getIdentityKeyPair().getPublicKey().serialize()));

        SessionBuilder sessionBuilder1To2 = new SessionBuilder(tc1.getSessionStore(), tc1.getPreKeyStore(), tc1.getSignedPreKeyStore(),
                tc1.getIdentityKeyStore(), signalProtocolAddress2);

        ECPublicKey preKeyTemp = Curve.decodePoint(preKey, 0);
        System.out.println(new String(preKeyTemp.serialize()));

        PreKeyBundle preKeyBundle2_1 = new PreKeyBundle(tc2.getRegistrationId(), 1, tc2.getPreKeys().get(0).getId(), preKeyTemp,
                tc2.getSignedPreKey().getId(), tc2.getSignedPreKey().getKeyPair().getPublicKey(), tc2.getSignedPreKey().getSignature(),
                new IdentityKey(identityKey, 0));
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
        byte[] result2To1_1 = sessionCipher1To2.decrypt(new SignalMessage(message2To1_1.serialize()));
        System.out.println(new String(result2To1_1));

        CiphertextMessage message1To2_3 = sessionCipher1To2.encrypt("Great".getBytes(StandardCharsets.UTF_8));
        byte[] result1To2_3 = sessionCipher2To1.decrypt(new SignalMessage(message1To2_3.serialize()));
        System.out.println(new String(result1To2_3));

        tc1.getSessionStore().deleteSession(signalProtocolAddress2);
        PreKeyBundle preKeyBundle2_2 = new PreKeyBundle(tc2.getRegistrationId(), 1, tc2.getPreKeys().get(1).getId(), tc2.getPreKeys().get(1).getKeyPair().getPublicKey(),
                tc2.getSignedPreKey().getId(), tc2.getSignedPreKey().getKeyPair().getPublicKey(), tc2.getSignedPreKey().getSignature(),
                tc2.getIdentityKeyPair().getPublicKey());
        sessionBuilder1To2.process(preKeyBundle2_2);
        CiphertextMessage message1To2_4 = sessionCipher1To2.encrypt("Hi again".getBytes(StandardCharsets.UTF_8));
        byte[] result1To2_4 = sessionCipher2To1.decrypt(new PreKeySignalMessage(message1To2_4.serialize()));
        System.out.println(new String(result1To2_4));
    }
}
