package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.entityUtil.VerifycodeUtil;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.entityUtil.my.User;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;

import cn.he.zhao.bbs.service.interf.LangPropsService;
import cn.he.zhao.bbs.spring.Common;
import cn.he.zhao.bbs.spring.Locales;
import cn.he.zhao.bbs.spring.SpringUtil;
import cn.he.zhao.bbs.util.Mails;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
//        final Query query = new Query().setFilter(new PropertyFilter(Verifycode.CODE, FilterOperator.EQUAL, code));
        try {
            final List<Verifycode> results = verifycodeMapper.getByCode(code);
            if (1 > results.size()) {
                return;
            }

            verifycodeMapper.deleteByPrimaryKey(results.get(0).getOid());
        } catch (final Exception e) {
            LOGGER.error( "Removes by code [" + code + "] failed", e);
        }
    }

    /**
     * Adds a verifycode with the specified request json object.
     *
     * @param verifycode the specified request json object, for example,
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
     * @throws Exception service exception
     */
    @Transactional
    public String addVerifycode(final Verifycode verifycode) throws Exception {
        try {
            return verifycodeMapper.add(verifycode);
        } catch (final Exception e) {
            final String msg = "Adds verifycode failed";
            LOGGER.error( msg, e);

            throw new Exception(msg);
        }
    }

    /**
     * Removes expired verifycodes.
     */
    @Transactional
    public void removeExpiredVerifycodes() {
//        final Query query = new Query().setFilter(new PropertyFilter(Verifycode.EXPIRED,
//                FilterOperator.LESS_THAN, new Date().getTime()));

        try {
            final List<Verifycode> results = verifycodeMapper.getExpiredVerifycodes(new Date().getTime());

            int size = results.size();
            for (int i = 0; i < size; i++) {
                final String id = results.get(i).getOid();
                verifycodeMapper.deleteByPrimaryKey(id);
            }
        } catch (final Exception e) {
            LOGGER.error( "Expires verifycodes failed", e);
        }
    }

    /**
     * Sends email verifycode.
     */
    @Transactional
    public void sendEmailVerifycode() {
//        final List<Filter> filters = new ArrayList<>();
//        filters.add(new PropertyFilter(Verifycode.TYPE, FilterOperator.EQUAL, VerifycodeUtil.TYPE_C_EMAIL));
//        filters.add(new PropertyFilter(Verifycode.STATUS, FilterOperator.EQUAL, VerifycodeUtil.STATUS_C_UNSENT));
//        final Query query = new Query().setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));

        try {
            final List<Verifycode> results = verifycodeMapper.getByTypeAndStatus(VerifycodeUtil.TYPE_C_EMAIL,VerifycodeUtil.STATUS_C_UNSENT);

            int size = results.size();
            for (int i = 0; i < size; i++) {
                final Verifycode verifycode = results.get(i);

                final String userId = verifycode.getUserId();
                final UserExt user = userMapper.get(userId);
                if (null == user) {
                    continue;
                }

                final Map<String, Object> dataModel = new HashMap<>();

                final String userName = user.getUserName();
                dataModel.put(User.USER_NAME, userName);

                final String toMail = verifycode.getReceiver();
                final String code = verifycode.getCode();
                String subject;

                final int bizType = verifycode.getBizType();
                switch (bizType) {
                    case VerifycodeUtil.BIZ_TYPE_C_REGISTER:
                        dataModel.put(Common.URL,  SpringUtil.getServerPath() + "/register?code=" + code);
                        subject = langPropsService.get("registerEmailSubjectLabel", Locales.getLocale());

                        break;
                    case VerifycodeUtil.BIZ_TYPE_C_RESET_PWD:
                        dataModel.put(Common.URL,  SpringUtil.getServerPath() + "/reset-pwd?code=" + code);
                        subject = langPropsService.get("forgetEmailSubjectLabel", Locales.getLocale());

                        break;
                    case VerifycodeUtil.BIZ_TYPE_C_BIND_EMAIL:
                        dataModel.put(Common.CODE, code);
                        subject = langPropsService.get("bindEmailSubjectLabel", Locales.getLocale());

                        break;
                    default:
                        LOGGER.warn("Send email verify code failed with wrong biz type [" + bizType + "]");

                        continue;
                }

                verifycode.setStatus(VerifycodeUtil.STATUS_C_SENT);
                verifycodeMapper.updateByPrimaryKey(verifycode);

                final String fromName = langPropsService.get("symphonyEnLabel")
                        + " " + langPropsService.get("verifycodeEmailFromNameLabel", Locales.getLocale());
                Mails.sendHTML(fromName, subject, toMail, Mails.TEMPLATE_NAME_VERIFYCODE, dataModel);
            }
        } catch (final Exception e) {
            LOGGER.error( "Sends verifycode failed", e);
        }
    }
}
