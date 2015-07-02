package com.lukaszgajos.keepassmob.core;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * Created by pedros on 07.06.15.
 */
public class Crypter {

    private String strKey;
    private DESKeySpec keySpec;
    private SecretKeyFactory keyFactory;
    private SecretKey key;

    public Crypter(String strKey){
        this.strKey = strKey;

        try {
            keySpec = new DESKeySpec(strKey.getBytes("UTF8"));
            keyFactory = SecretKeyFactory.getInstance("DES");
            key = keyFactory.generateSecret(keySpec);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public String encrypt(String strIn){

        Cipher cipher = null; // cipher is not thread safe
        String encrypedPwd = "";
        try {
            byte[] cleartext = strIn.getBytes("UTF8");
            cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            cipher.doFinal(cleartext);

            encrypedPwd = Base64.encodeToString(cipher.doFinal(cleartext), Base64.DEFAULT);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return encrypedPwd;
    }

    public String decrypt(String strIn){
        byte[] encrypedPwdBytes = Base64.decode(strIn, Base64.DEFAULT);

        Cipher cipher = null;// cipher is not thread safe
        byte[] plainTextPwdBytes = null;
        try {
            cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            plainTextPwdBytes = (cipher.doFinal(encrypedPwdBytes));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        return  new String(plainTextPwdBytes);
    }


}
