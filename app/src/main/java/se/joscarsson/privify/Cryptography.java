package se.joscarsson.privify;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class Cryptography {
    static String hash(String value) {
        MessageDigest digest;

        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        digest.reset();
        byte[] hashBytes = digest.digest(value.getBytes());

        return String.format("%0" + (hashBytes.length * 2) + "X", new BigInteger(1, hashBytes));
    }
}
