package it.unifi.desdecryption;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

public class DesEncrypter {
    Cipher ecipher;
    Cipher dcipher;

    DesEncrypter() throws Exception {
        ecipher = Cipher.getInstance("DES");
        dcipher = Cipher.getInstance("DES");
    }

    public void initEcipher(SecretKey key) throws Exception {
        ecipher.init(Cipher.ENCRYPT_MODE, key);
    }

    public String encrypt(String str) throws Exception {
        // Encode the string into bytes using utf-8
        byte[] utf8 = str.getBytes("UTF-8");

        // Encrypt
        byte[] enc = ecipher.doFinal(utf8);

        // Encode bytes to base64 to get a string
        return new sun.misc.BASE64Encoder().encode(enc);
    }
}
