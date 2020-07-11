package socotra.protocol;

import javafx.scene.control.Alert;
import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SessionRecord;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import socotra.util.Util;

import java.io.*;

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

    private void readFile(String fileName) throws IOException, IllegalStateException, InvalidKeyException {
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
                default:
                    throw new IllegalStateException("Bad file name.");
            }
        }
    }

    private void loadIdentityKeyStore() throws IOException, IllegalStateException, InvalidKeyException {
        readFile("identityKeyStore.csv");
    }

    private void loadSignedPreKeyStore() throws IOException, IllegalStateException, InvalidKeyException {
        readFile("signedPreKeyStore.csv");
    }

    private void loadPreKeyStore() throws IOException, IllegalStateException, InvalidKeyException {
        readFile("preKeyStore.csv");
    }

    private void loadSessionStore() throws IOException, IllegalStateException, InvalidKeyException {
        readFile("sessionStore.csv");
    }

}
