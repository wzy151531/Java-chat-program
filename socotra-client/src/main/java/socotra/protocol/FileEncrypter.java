package socotra.protocol;

import socotra.common.User;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;

public class FileEncrypter {

    private static final int ENCRYPT = 0;
    private static final int DECRYPT = 1;
    private static final String key = "aaaabbbbccccdddd";
    private static final String ivParameter = "AAAABBBBCCCCDDDD";
    private static final String userDirPath = "src/main/resources/userData";
    private final User user;

    public FileEncrypter(User user) {
        this.user = user;
    }

    private void handleFile(int mode, String src, String des) throws Exception {
        File srcFile = new File(userDirPath + "/" + user.toString(), src);
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(
                srcFile));
        byte[] bytIn = new byte[(int) srcFile.length()];
        bis.read(bytIn);
        bis.close();
        byte[] raw = key.getBytes("ASCII");
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());
        if (mode == FileEncrypter.ENCRYPT) {
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        } else if (mode == FileEncrypter.DECRYPT) {
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
        } else {
            throw new IllegalStateException("Bad mode.");
        }
        byte[] bytOut = cipher.doFinal(bytIn);
        File desFile = new File(userDirPath + "/" + user.toString(), des);
        BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(desFile));
        bos.write(bytOut);
        bos.close();
    }

    public void encrypt(String src, String des) throws Exception {
        handleFile(ENCRYPT, src, des);
    }

    public void decrypt(String src, String des) throws Exception {
        handleFile(DECRYPT, src, des);
    }

}
