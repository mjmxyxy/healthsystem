package com.healthy.util;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class AESUtil {
    private static final String ALGO = "AES/GCM/NoPadding";

    public static String encrypt(String plaintext, String key) throws Exception {
        byte[] keyBytes = key.getBytes();
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance(ALGO);
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);
        byte[] cipherText = cipher.doFinal(plaintext.getBytes());
        byte[] combined = new byte[iv.length + cipherText.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);
        return Base64.getEncoder().encodeToString(combined);
    }

    public static String decrypt(String cipherTextBase64, String key) throws Exception {
        byte[] combined = Base64.getDecoder().decode(cipherTextBase64);
        byte[] iv = new byte[12];
        System.arraycopy(combined, 0, iv, 0, 12);
        byte[] cipherText = new byte[combined.length - 12];
        System.arraycopy(combined, 12, cipherText, 0, cipherText.length);
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance(ALGO);
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);
        byte[] plain = cipher.doFinal(cipherText);
        return new String(plain);
    }
}
