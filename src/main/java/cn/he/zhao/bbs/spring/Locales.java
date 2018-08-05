package cn.he.zhao.bbs.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * 描述:
 * Locales
 *
 * @Author HeFeng
 * @Create 2018-08-05 15:20
 */
public class Locales {
    private static final Logger LOGGER = LoggerFactory.getLogger(Locales.class);
    private static final ThreadLocal<Locale> LOCALE = new InheritableThreadLocal();
    private static final int LANG_START = 0;
    private static final int LANG_END = 2;
    private static final int COUNTRY_START = 3;
    private static final int COUNTRY_END = 5;

    private Locales() {
    }

    public static Locale getLocale(HttpServletRequest request) {
        Locale locale = null;
        HttpSession session = request.getSession(false);
        if (session != null) {
            locale = (Locale)session.getAttribute("locale");
        }

        if (null == locale) {
            String languageHeader = request.getHeader("Accept-Language");
            LOGGER.debug( "[Accept-Language={0}]", new Object[]{languageHeader});
            String language = "zh";
            String country = "CN";
            if (!Strings.isEmptyOrNull(languageHeader)) {
                language = getLanguage(languageHeader);
                country = getCountry(languageHeader);
            }

            locale = new Locale(language, country);
            if (!hasLocale(locale)) {
                locale = LocaleContextHolder.getLocale();
                LOGGER.debug( "Using the default locale[{0}]", new Object[]{locale.toString()});
            } else {
                LOGGER.debug( "Got locale[{0}] from request.", new Object[]{locale.toString()});
            }
        } else {
            LOGGER.debug( "Got locale[{0}] from session.", new Object[]{locale.toString()});
        }

        return locale;
    }

    public static boolean hasLocale(Locale locale) {
        try {
            ResourceBundle.getBundle("lang", locale);
            return true;
        } catch (MissingResourceException var2) {
            return false;
        }
    }

    public static void setLocale(HttpServletRequest request, Locale locale) {
        HttpSession session = request.getSession(false);
        if (null == session) {
            LOGGER.warn("Ignores set locale caused by no session");
        } else {
            session.setAttribute("locale", locale);
            LOGGER.debug( "Client[sessionId={0}] sets locale to [{1}]", new Object[]{session.getId(), locale.toString()});
        }
    }

    public static void setLocale(Locale locale) {
        LOCALE.set(locale);
    }

    public static Locale getLocale() {
        Locale ret = (Locale)LOCALE.get();
        return null == ret ? LocaleContextHolder.getLocale() : ret;
    }

    public static String getCountry(String localeString) {
        return localeString.length() >= 5 ? localeString.substring(3, 5) : "";
    }

    public static String getLanguage(String localeString) {
        return localeString.length() >= 2 ? localeString.substring(0, 2) : "";
    }

    public static Locale getLocale(String localeString) {
        String language = getLanguage(localeString);
        String country = getCountry(localeString);
        return new Locale(language, country);
    }
}