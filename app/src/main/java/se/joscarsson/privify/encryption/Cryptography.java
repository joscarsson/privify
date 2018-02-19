package se.joscarsson.privify.encryption;

import android.util.Pair;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Cryptography {
    private static final int ITERATION_MULTIPLIER = 10;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = KEY_LENGTH / 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    public static Pair<String, String> hash(String value, String hexSalt)
    {
        try {
            byte[] salt = new byte[16];

            if (hexSalt == null) {
                RANDOM.nextBytes(salt);
            } else {
                salt = toBytes(hexSalt);
            }

            KeySpec keySpecification = new PBEKeySpec(value.toCharArray(), salt, ITERATION_MULTIPLIER * 1000, KEY_LENGTH);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hashBytes = keyFactory.generateSecret(keySpecification).getEncoded();
            return new Pair<>(toHex(hashBytes), toHex(salt));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static Pair<Cipher, byte[]> newCipher(String passphrase) {
        ByteArrayOutputStream header = new ByteArrayOutputStream();

        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);

        KeySpec keySpecification = new PBEKeySpec(passphrase.toCharArray(), salt, ITERATION_MULTIPLIER * 1000, KEY_LENGTH);

        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] keyBytes = keyFactory.generateSecret(keySpecification).getEncoded();
            SecretKey key = new SecretKeySpec(keyBytes, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] iv = new byte[cipher.getBlockSize()];
            RANDOM.nextBytes(iv);
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivParams);

            header.write(new byte[] {ITERATION_MULTIPLIER});
            header.write(new byte[] {SALT_LENGTH});
            header.write(salt);
            header.write(new byte[] {(byte)iv.length});
            header.write(iv);

            return new Pair<>(cipher, header.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static Cipher getCipher(String passphrase, InputStream inputStream) {
        try {
            int iterationMultipler = inputStream.read();

            int saltLength = inputStream.read();
            byte[] salt = new byte[saltLength];
            for (int i = 0; i < salt.length; i++) {
                salt[i] = (byte)inputStream.read();
            }

            int ivLength = inputStream.read();
            byte[] iv = new byte[ivLength];
            for (int i = 0; i < iv.length; i++) {
                iv[i] = (byte)inputStream.read();
            }

            int iterationCount = iterationMultipler * 1000;
            KeySpec keySpecification = new PBEKeySpec(passphrase.toCharArray(), salt, iterationCount, KEY_LENGTH);

            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] keyBytes = keyFactory.generateSecret(keySpecification).getEncoded();
            SecretKey key = new SecretKeySpec(keyBytes, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, key, ivParams);
            return cipher;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String toHex(byte[] values) {
        return String.format("%0" + (values.length * 2) + "X", new BigInteger(1, values));
    }

    private static byte[] toBytes(String hex) {
        byte[] data = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            data[i / 2] = (byte)(Character.digit(hex.charAt(i), 16) * 16 + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }
}
