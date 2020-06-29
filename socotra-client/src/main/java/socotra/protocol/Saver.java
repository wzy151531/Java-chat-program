package socotra.protocol;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class Saver {

    private final String userDirPath = "src/main/resources/userData";
    private final String username;
    private final EncryptedClient encryptedClient;

    public Saver(String username, EncryptedClient encryptedClient) {
        this.username = username;
        this.encryptedClient = encryptedClient;
        File userDir = new File(userDirPath, username);
        if (!userDir.exists()) {
            userDir.mkdir();
        }
    }

    public void saveStores() {
        try {
            saveIdentityKeyStore();
            saveSignedPreKeyStore();
            savePreKeyStore();
            saveSessionStore();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String byteArrayToHexStr(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[byteArray.length * 2];
        for (int j = 0; j < byteArray.length; j++) {
            int v = byteArray[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private void writeFile(HashMap<String, byte[]> hashMap, String fileName) throws IOException {
        File file = new File(userDirPath + "/" + username, fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        if (fileName.equals("identityKeyStore.csv")) {
            bw.write(byteArrayToHexStr(encryptedClient.getIdentityKeyStore().getIdentityKeyPair().serialize()) + "," + encryptedClient.getIdentityKeyStore().getLocalRegistrationId() + "\n");
        }
        hashMap.forEach((k, v) -> {
            try {
                bw.write(k + "," + byteArrayToHexStr(v) + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        bw.close();
    }

    private void saveIdentityKeyStore() throws IOException {
        writeFile(encryptedClient.getIdentityKeyStore().getFormattedIdentityKeyMap(), "identityKeyStore.csv");
    }

    private void saveSignedPreKeyStore() throws IOException {
        writeFile(encryptedClient.getSignedPreKeyStore().getFormattedSignedPreKeyMap(), "signedPreKeyStore.csv");
    }

    private void savePreKeyStore() throws IOException {
        writeFile(encryptedClient.getPreKeyStore().getFormattedPreKeyMap(), "preKeyStore.csv");
    }

    private void saveSessionStore() throws IOException {
        writeFile(encryptedClient.getSessionStore().getFormattedSessionMap(), "sessionStore.csv");
    }

}
