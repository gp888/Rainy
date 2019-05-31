package com.gp.rainy.encypt.Encrypt;

public class Encrypt {

    private static String publicKey = "aefb1aca";

    //加密
    public static String encrypt(String value) {
        String encryptValue = "";
        try {
            encryptValue = EncrypDESC.encryptDES(value, publicKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptValue;
    }

    //加密
    public static String encrypt(String value, String publicKey) {
        String encryptValue = "";
        try {
            encryptValue = EncrypDESC.encryptDES(value, publicKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptValue;
    }

    //解密
    public static String decrypt(String value) {
        String encryptValue = "";
        try {
            encryptValue = EncrypDESC.decryptDES(value, publicKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptValue;
    }

    //解密
    public static String decrypt(String value, String publicKey) {
        String encryptValue = "";
        try {
            encryptValue = EncrypDESC.decryptDES(value, publicKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptValue;
    }

}
