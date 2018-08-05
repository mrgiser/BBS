package cn.he.zhao.bbs.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 描述:
 * MD5
 *
 * @Author HeFeng
 * @Create 2018-08-05 16:40
 */
public class MD5 {
    private static final Logger LOGGER = LoggerFactory.getLogger(MD5.class);
    private static MessageDigest messageDigest;
    private static final int LOW_8_BITS_1 = 255;
    private static final int APPEND_SIZE = 16;

    private MD5() {
    }

    public static String hash(String string) {
        char[] charArray = string.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        for(int i = 0; i < charArray.length; ++i) {
            byteArray[i] = (byte)charArray[i];
        }

        byte[] bytes = messageDigest.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();

        for(int i = 0; i < bytes.length; ++i) {
            int val = bytes[i] & 255;
            if (val < 16) {
                hexValue.append("0");
            }

            hexValue.append(Integer.toHexString(val));
        }

        return hexValue.toString();
    }

    static {
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException var1) {
            LOGGER.error( var1.getMessage(), var1);
            throw new RuntimeException(var1);
        }
    }
}