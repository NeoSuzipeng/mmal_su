package com.mmall.util;

import java.security.MessageDigest;

/**
 * Created by 10353 on 2018/1/4.
 */
public class MD5Util {

    private static final String hexDigits[] = {"0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    private static String byteArrayToHexString(byte b[]) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++)
            resultSb.append(byteToHexString(b[i]));

        return resultSb.toString();
    }

    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
            n += 256;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    private static String MD5Encode(String str, String encodeCharacter){
        //todo
        String result = null;
        try{
            result = new String(str);
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            if(encodeCharacter == null || "".equals(encodeCharacter))
                result = byteArrayToHexString(messageDigest.digest(result.getBytes()));
            else
                result = byteArrayToHexString(messageDigest.digest(result.getBytes(encodeCharacter)));
        }catch (Exception e){
        }
        return result.toUpperCase();
    }

    public static String MD5EncodeUtf8(String str){
        return MD5Encode(str, "utf-8");
    }

}
