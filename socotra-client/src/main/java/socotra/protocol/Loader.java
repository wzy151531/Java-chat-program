package socotra.protocol;

import javafx.scene.control.Alert;
import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.groups.SenderKeyName;
import org.whispersystems.libsignal.groups.state.SenderKeyRecord;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SessionRecord;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import socotra.common.ChatSession;
import socotra.common.ConnectionData;
import socotra.util.Util;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Loader {

    private final String userDirPath = "src/main/resources/userData";
    private final String username;
    private EncryptedClient encryptedClient;

    public Loader(String username) {
        this.username = username;
        File userDir = new File(userDirPath, username);
        if (!userDir.exists()) {
            userDir.mkdir();
        }
    }

    public void loadStores() {
        try {
            loadIdentityKeyStore();
            loadSignedPreKeyStore();
            loadPreKeyStore();
            loadSessionStore();
            loadSenderKeyStore();
        } catch (FileNotFoundException e) {
            Util.generateAlert(Alert.AlertType.ERROR, "Error", "File not found.", "User identityKeyStore.csv not found.").show();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            System.out.println("Invalid key.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] hexStrToByteArray(String str) {
        if (str == null) {
            return null;
        }
        if (str.length() == 0) {
            return new byte[0];
        }
        byte[] byteArray = new byte[str.length() / 2];
        for (int i = 0; i < byteArray.length; i++) {
            String subStr = str.substring(2 * i, 2 * i + 2);
            byteArray[i] = ((byte) Integer.parseInt(subStr, 16));
        }
        return byteArray;
    }

    private void readStore(String fileName) throws IOException, IllegalStateException, InvalidKeyException {
        File file = new File(userDirPath + "/" + username, fileName);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        if (fileName.equals("identityKeyStore.csv")) {
            line = br.readLine();
            String[] firstLine = line.split(",");
            encryptedClient = new EncryptedClient(hexStrToByteArray(firstLine[0]), Integer.parseInt(firstLine[1]), username);
        }
        while ((line = br.readLine()) != null) {
            String[] record = line.split(",");
            switch (fileName) {
                case "identityKeyStore.csv":
                    MyIdentityKeyStore myIdentityKeyStore = encryptedClient.getIdentityKeyStore();
                    myIdentityKeyStore.saveIdentity(new SignalProtocolAddress(record[0], 1), new IdentityKey(hexStrToByteArray(record[1]), 0));
                    break;
                case "signedPreKeyStore.csv":
                    MySignedPreKeyStore mySignedPreKeyStore = encryptedClient.getSignedPreKeyStore();
                    mySignedPreKeyStore.storeSignedPreKey(Integer.parseInt(record[0]), new SignedPreKeyRecord(hexStrToByteArray(record[1])));
                    break;
                case "preKeyStore.csv":
                    MyPreKeyStore myPreKeyStore = encryptedClient.getPreKeyStore();
                    myPreKeyStore.storePreKey(Integer.parseInt(record[0]), new PreKeyRecord(hexStrToByteArray(record[1])));
                    break;
                case "sessionStore.csv":
                    MySessionStore mySessionStore = encryptedClient.getSessionStore();
                    mySessionStore.storeSession(new SignalProtocolAddress(record[0], 1), new SessionRecord(hexStrToByteArray(record[1])));
                    break;
                case "senderKeyStore.csv":
                    MySenderKeyStore mySenderKeyStore = encryptedClient.getSenderKeyStore();
                    String senderKeyNamePart = record[0];
                    String[] parts = senderKeyNamePart.split("::");
                    mySenderKeyStore.storeSenderKey(new SenderKeyName(parts[0], new SignalProtocolAddress(parts[1], Integer.parseInt(parts[2]))),
                            new SenderKeyRecord(hexStrToByteArray(record[1])));
                    break;
                default:
                    throw new IllegalStateException("Bad file name.");
            }
        }
    }

    private HashMap<ChatSession, List<ConnectionData>> readChatData() throws IOException {
        File file = new File(userDirPath + "/" + username, "chatData");
        if (!file.exists()) {
            return new HashMap<>();
        }
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        HashMap<ChatSession, List<ConnectionData>> result = new HashMap<>();
        while ((line = br.readLine()) != null) {
            String[] records = line.split(",");
            if (records[0].equals("null")) {
                result.put(new ChatSession(records[1]), new ArrayList<>());
            } else {
                ChatSession chatSession = new ChatSession(records[4]);
                UUID uuid = UUID.fromString(records[0]);
                int type = Integer.parseInt(records[1]);
                String data = records[2];
                String userSignature = records[3];
                List<ConnectionData> temp = result.getOrDefault(chatSession, new ArrayList<>());
                if (type == 1) {
                    temp.add(new ConnectionData(data, uuid, userSignature, chatSession));
                } else if (type == 2) {
                    temp.add(new ConnectionData(hexStrToByteArray(data), uuid, userSignature, chatSession));
                } else {
                    throw new IllegalStateException("Bad connectionData type.");
                }
                result.put(chatSession, temp);
            }
        }
        return result;
    }

    private void loadIdentityKeyStore() throws IOException, IllegalStateException, InvalidKeyException {
        readStore("identityKeyStore.csv");
    }

    private void loadSignedPreKeyStore() throws IOException, IllegalStateException, InvalidKeyException {
        readStore("signedPreKeyStore.csv");
    }

    private void loadPreKeyStore() throws IOException, IllegalStateException, InvalidKeyException {
        readStore("preKeyStore.csv");
    }

    private void loadSessionStore() throws IOException, IllegalStateException, InvalidKeyException {
        readStore("sessionStore.csv");
    }

    private void loadSenderKeyStore() throws IOException, IllegalStateException, InvalidKeyException {
        readStore("senderKeyStore.csv");
    }

    public HashMap<ChatSession, List<ConnectionData>> loadChatData() {
        try {
            return readChatData();
        } catch (IOException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

}
