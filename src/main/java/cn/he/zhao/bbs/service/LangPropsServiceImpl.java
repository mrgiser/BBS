package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.service.interf.LangPropsService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 描述:
 * LangPropsServiceImpl
 *
 * @Author HeFeng
 * @Create 2018-08-01 15:26
 */
public class LangPropsServiceImpl implements LangPropsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LangPropsServiceImpl.class);
    private static final Map<Locale, Map<String, String>> LANGS = new HashMap();

    public LangPropsServiceImpl() {
    }

    public Map<String, String> getAll(Locale locale) {
        Map<String, String> ret = (Map)LANGS.get(locale);
        if (null == ret) {
            ret = new HashMap();

            ResourceBundle langBundle;
            try {
                langBundle = ResourceBundle.getBundle("lang", locale);
            } catch (MissingResourceException var8) {
                LOGGER.warn("{0}, using default locale[{1}] instead", new Object[]{var8.getMessage(), Latkes.getLocale()});

                try {
                    langBundle = ResourceBundle.getBundle("lang", Latkes.getLocale());
                } catch (MissingResourceException var7) {
                    LOGGER.warn("{0}, using default lang.properties instead", new Object[]{var8.getMessage()});
                    langBundle = ResourceBundle.getBundle("lang");
                }
            }

            Enumeration keys = langBundle.getKeys();

            while(keys.hasMoreElements()) {
                String key = (String)keys.nextElement();
                String value = this.replaceVars(langBundle.getString(key));
                ((Map)ret).put(key, value);
            }

            LANGS.put(locale, ret);
        }

        return (Map)ret;
    }

    public String get(String key) {
        return this.get("lang", key, Locales.getLocale());
    }

    public String get(String key, Locale locale) {
        return this.get("lang", key, locale);
    }

    private String get(String baseName, String key, Locale locale) {
        if (!"lang".equals(baseName)) {
            RuntimeException e = new RuntimeException("i18n resource[baseName=" + baseName + "] not found");
            LOGGER.error( e.getMessage(), e);
            throw e;
        } else {
            try {
                return this.replaceVars(ResourceBundle.getBundle(baseName, locale).getString(key));
            } catch (MissingResourceException var5) {
                LOGGER.error("{0}, get it from default locale[{1}]", new Object[]{var5.getMessage(), Latkes.getLocale()});
                return ResourceBundle.getBundle(baseName, Latkes.getLocale()).getString(key);
            }
        }
    }

    private String replaceVars(String langValue) {
        String ret = StringUtils.replace(langValue, "${servePath}", Latkes.getServePath());
        ret = StringUtils.replace(ret, "${staticServePath}", Latkes.getStaticServePath());
        return ret;
    }
}