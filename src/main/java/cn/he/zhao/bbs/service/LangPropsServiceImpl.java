package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.service.interf.LangPropsService;
import cn.he.zhao.bbs.spring.Locales;
import cn.he.zhao.bbs.spring.SpringUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
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

    @Autowired
    private HttpServletRequest request;

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
                LOGGER.warn("{0}, using default locale[{1}] instead", new Object[]{var8.getMessage(), Locale.getDefault()});

                try {
                    langBundle = ResourceBundle.getBundle("lang", Locale.getDefault());
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
                LOGGER.error("{0}, get it from default locale[{1}]", new Object[]{var5.getMessage(), Locale.getDefault()});
                return ResourceBundle.getBundle(baseName, Locale.getDefault()).getString(key);
            }
        }
    }

    private String replaceVars(String langValue) {
        String ret = StringUtils.replace(langValue, "${servePath}",  SpringUtil.getServerPath());
        ret = StringUtils.replace(ret, "${staticServePath}", SpringUtil.getStaticServePath());
        return ret;
    }
}