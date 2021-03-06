package socotra.protocol;

import socotra.common.ChatSession;
import socotra.common.ConnectionData;
import socotra.common.User;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Saver {

    private final String userDirPath = "src/main/resources/userData";
    private final User user;
    private final EncryptedClient encryptedClient;
    private final FileEncrypter fileEncrypter;

    public Saver(User user, EncryptedClient encryptedClient) {
        this.user = user;
        this.encryptedClient = encryptedClient;
        File userDir = new File(userDirPath, user.toString());
        if (!userDir.exists()) {
            userDir.mkdir();
        }
        this.fileEncrypter = new FileEncrypter(user);
    }

    public void saveStores() {
        try {
            saveIdentityKeyStore();
            saveSignedPreKeyStore();
            savePreKeyStore();
            saveSessionStore();
            saveSenderKeyStore();
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

    private void writeStore(HashMap<String, byte[]> hashMap, String fileName) throws IOException {
        File file = new File(userDirPath + "/" + user.toString(), fileName);
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
        try {
            fileEncrypter.encrypt(fileName, fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeChatData(HashMap<ChatSession, ArrayList<ConnectionData>> chatData) throws IOException {
        File file = new File(userDirPath + "/" + user.toString(), "chatData");
        if (!file.exists()) {
            file.createNewFile();
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        chatData.forEach((k, v) -> {
            if (v.size() == 0) {
                try {
                    bw.write("null," + k.generateChatIdCSV() + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                v.forEach(n -> {
                    try {
                        bw.write(n.getUuid() + "," + n.getType() + "," +
                                (n.getType() == 1 ? n.getTextData() : byteArrayToHexStr(n.getAudioData())) + "," +
                                n.getUserSignature() + "," + k.generateChatIdCSV() + "\n"
                        );
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
        bw.close();
        try {
            fileEncrypter.encrypt("chatData", "chatData");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveIdentityKeyStore() throws IOException {
        writeStore(encryptedClient.getIdentityKeyStore().getFormattedIdentityKeyMap(), "identityKeyStore.csv");
    }

    private void saveSignedPreKeyStore() throws IOException {
        writeStore(encryptedClient.getSignedPreKeyStore().getFormattedSignedPreKeyMap(), "signedPreKeyStore.csv");
    }

    private void savePreKeyStore() throws IOException {
        writeStore(encryptedClient.getPreKeyStore().getFormattedPreKeyMap(), "preKeyStore.csv");
    }

    private void saveSessionStore() throws IOException {
        writeStore(encryptedClient.getSessionStore().getFormattedSessionMap(), "sessionStore.csv");
    }

    private void saveSenderKeyStore() throws IOException {
        writeStore(encryptedClient.getSenderKeyStore().getFormattedSenderKeyMap(), "senderKeyStore.csv");
    }

    public void saveChatData(HashMap<ChatSession, ArrayList<ConnectionData>> chatData) {
        try {
            writeChatData(chatData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
