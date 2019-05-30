package pt.ulisboa.tecnico.cmu.utils;

import com.google.api.client.util.Base64;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class CryptoUtils {

    private static final String ASYM_CIPHER = "RSA/ECB/PKCS1Padding";

    public static String asymCipher(String text, Key key) {
        try {
            Cipher cipher = Cipher.getInstance(ASYM_CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] cipherBytes = cipher.doFinal(text.getBytes());
            return Base64.encodeBase64String(cipherBytes);
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
        return "[]";
    }

    public static String asymDecipher(String text, Key key) {
        try {
            Cipher  cipher = Cipher.getInstance(ASYM_CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] plainBytes = cipher.doFinal(Base64.decodeBase64(text));
            return new String(plainBytes);
        } catch (RuntimeException | NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return "[]";
    }

}
