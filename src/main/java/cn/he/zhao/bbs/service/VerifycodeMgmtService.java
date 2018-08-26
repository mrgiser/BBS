package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.entity.my.*;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Verifycode management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.2.0.1, Jun 12, 2018
 * @since 1.3.0
 */
@Service
public class VerifycodeMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(VerifycodeMgmtService.class);

    /**
     * Verifycode Mapper.
     */
    @Autowired
    private VerifycodeMapper verifycodeMapper;

    /**
     * User Mapper.
     */
    @Autowired
    private UserMapper userMapper;

    /**
     * Language service.
     */
    @Autowired
    private LangPropsService langPropsService;

    /**
     * Removes a verifycode with the specified code.
     *
     * @param code the specified code
     */
    @Transactional
    public void removeByCode(final String code) {
        final Query query = new Query().setFilter(new PropertyFilter(Verifycode.CODE, FilterOperator.EQUAL, code));
        try {
            final JSONArray results = verifycodeMapper.get(query).optJSONArray(Keys.RESULTS);
            if (1 > results.length()) {
                return;
            }

            verifycodeMapper.remove(results.optJSONObject(0).optString(Keys.OBJECT_ID));
        } catch (final Exception e) {
            LOGGER.error( "Removes by code [" + code + "] failed", e);
        }
    }

    /**
     * Adds a verifycode with the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,
     *                          {
     *                          "userId"; "",
     *                          "type": int,
     *                          "bizType": int,
     *                          "receiver": "",
     *                          "code": "",
     *                          "status": int,
     *                          "expired": long
     *                          }
     * @return verifycode id
     * @throws ServiceException service exception
     */
    @Transactional
    public String addVerifycode(final JSONObject requestJSONObject) throws ServiceException {
        try {
            return verifycodeMapper.add(requestJSONObject);
        } catch (final MapperException e) {
            final String msg = "Adds verifycode failed";
            LOGGER.error( msg, e);

            throw new ServiceException(msg);
        }
    }

    /**
     * Removes expired verifycodes.
     */
    @Transactional
    public void removeExpiredVerifycodes() {
        final Query query = new Query().setFilter(new PropertyFilter(Verifycode.EXPIRED,
                FilterOperator.LESS_THAN, new Date().getTime()));

        try {
            final JSONObject result = verifycodeMapper.get(query);
            final JSONArray verifycodes = result.optJSONArray(Keys.RESULTS);

            for (int i = 0; i < verifycodes.length(); i++) {
                final String id = verifycodes.optJSONObject(i).optString(Keys.OBJECT_ID);
                verifycodeMapper.remove(id);
            }
        } catch (final MapperException e) {
            LOGGER.error( "Expires verifycodes failed", e);
        }
    }

    /**
     * Sends email verifycode.
     */
    @Transactional
    public void sendEmailVerifycode() {
        final List<Filter> filters = new ArrayList<>();
        filters.add(new PropertyFilter(Verifycode.TYPE, FilterOperator.EQUAL, Verifycode.TYPE_C_EMAIL));
        filters.add(new PropertyFilter(Verifycode.STATUS, FilterOperator.EQUAL, Verifycode.STATUS_C_UNSENT));
        final Query query = new Query().setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));

        try {
            final JSONObject result = verifycodeMapper.get(query);
            final JSONArray verifycodes = result.optJSONArray(Keys.RESULTS);

            for (int i = 0; i < verifycodes.length(); i++) {
                final JSONObject verifycode = verifycodes.optJSONObject(i);

                final String userId = verifycode.optString(Verifycode.USER_ID);
                final JSONObject user = userMapper.get(userId);
                if (null == user) {
                    continue;
                }

                final Map<String, Object> dataModel = new HashMap<>();

                final String userName = user.optString(User.USER_NAME);
                dataModel.put(User.USER_NAME, userName);

                final String toMail = verifycode.optString(Verifycode.RECEIVER);
                final String code = verifycode.optString(Verifycode.CODE);
                String subject;

                final int bizType = verifycode.optInt(Verifycode.BIZ_TYPE);
                switch (bizType) {
                    case Verifycode.BIZ_TYPE_C_REGISTER:
                        dataModel.put(Common.URL,  SpringUtil.getServerPath() + "/register?code=" + code);
                        subject = langPropsService.get("registerEmailSubjectLabel", Locales.getLocale());

                        break;
                    case Verifycode.BIZ_TYPE_C_RESET_PWD:
                        dataModel.put(Common.URL,  SpringUtil.getServerPath() + "/reset-pwd?code=" + code);
                        subject = langPropsService.get("forgetEmailSubjectLabel", Locales.getLocale());

                        break;
                    case Verifycode.BIZ_TYPE_C_BIND_EMAIL:
                        dataModel.put(Common.CODE, code);
                        subject = langPropsService.get("bindEmailSubjectLabel", Locales.getLocale());

                        break;
                    default:
                        LOGGER.warn("Send email verify code failed with wrong biz type [" + bizType + "]");

                        continue;
                }

                verifycode.put(Verifycode.STATUS, Verifycode.STATUS_C_SENT);
                verifycodeMapper.update(verifycode.optString(Keys.OBJECT_ID), verifycode);

                final String fromName = langPropsService.get("symphonyEnLabel")
                        + " " + langPropsService.get("verifycodeEmailFromNameLabel", Locales.getLocale());
                Mails.sendHTML(fromName, subject, toMail, Mails.TEMPLATE_NAME_VERIFYCODE, dataModel);
            }
        } catch (final MapperException e) {
            LOGGER.error( "Sends verifycode failed", e);
        }
    }
}
