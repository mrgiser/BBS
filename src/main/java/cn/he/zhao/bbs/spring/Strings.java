package cn.he.zhao.bbs.spring;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 描述:
 * Strings
 *
 * @Author HeFeng
 * @Create 2018-07-28 18:47
 */


public final class Strings {
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final int MAX_EMAIL_LENGTH_LOCAL = 64;
    private static final int MAX_EMAIL_LENGTH_DOMAIN = 255;
    private static final int MAX_EMAIL_LENGTH = 256;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

    private Strings() {
    }

    public static List<String> toLines(String string) throws IOException {
        if (null == string) {
            return null;
        } else {
            BufferedReader bufferedReader = new BufferedReader(new StringReader(string));
            ArrayList ret = new ArrayList();

            try {
                for(String line = bufferedReader.readLine(); null != line; line = bufferedReader.readLine()) {
                    ret.add(line);
                }
            } finally {
                bufferedReader.close();
            }

            return ret;
        }
    }

    public static boolean isNumeric(String string) {
        if (isEmptyOrNull(string)) {
            return false;
        } else {
            Pattern pattern = Pattern.compile("[0-9]*");
            Matcher matcher = pattern.matcher(string);
            return matcher.matches();
        }
    }

    public static boolean isEmail(String string) {
        if (isEmptyOrNull(string)) {
            return false;
        } else if (256 < string.length()) {
            return false;
        } else {
            String[] parts = string.split("@");
            if (2 != parts.length) {
                return false;
            } else {
                String local = parts[0];
                if (64 < local.length()) {
                    return false;
                } else {
                    String domain = parts[1];
                    return 255 < domain.length() ? false : EMAIL_PATTERN.matcher(string).matches();
                }
            }
        }
    }

    public static boolean isEmptyOrNull(String string) {
        return string == null || string.length() == 0;
    }

    public static String[] trimAll(String[] strings) {
        if (null == strings) {
            return null;
        } else {
            String[] ret = new String[strings.length];

            for(int i = 0; i < strings.length; ++i) {
                ret[i] = strings[i].trim();
            }

            return ret;
        }
    }

    public static boolean containsIgnoreCase(String string, String[] strings) {
        if (null == strings) {
            return false;
        } else {
            String[] var2 = strings;
            int var3 = strings.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                String str = var2[var4];
                if (null == str && null == string) {
                    return true;
                }

                if (null != string && null != str && string.equalsIgnoreCase(str)) {
                    return true;
                }
            }

            return false;
        }
    }

    public static boolean contains(String string, String[] strings) {
        if (null == strings) {
            return false;
        } else {
            String[] var2 = strings;
            int var3 = strings.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                String str = var2[var4];
                if (null == str && null == string) {
                    return true;
                }

                if (null != string && null != str && string.equals(str)) {
                    return true;
                }
            }

            return false;
        }
    }

    public static boolean isURL(String string) {
        try {
            new URL(string);
            return true;
        } catch (MalformedURLException var2) {
            return false;
        }
    }
}
