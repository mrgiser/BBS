/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2018, b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package cn.he.zhao.bbs.controller;

import cn.he.zhao.bbs.entityUtil.*;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.entityUtil.my.User;
import cn.he.zhao.bbs.exception.RequestProcessAdviceException;
import cn.he.zhao.bbs.spring.Common;
import cn.he.zhao.bbs.spring.Requests;
import cn.he.zhao.bbs.spring.Strings;
import cn.he.zhao.bbs.util.JsonUtil;
import cn.he.zhao.bbs.util.Languages;
import cn.he.zhao.bbs.util.Symphonys;
import cn.he.zhao.bbs.util.TimeZones;
import cn.he.zhao.bbs.validate.UpdateEmotionListValidation;
import cn.he.zhao.bbs.validate.UpdatePasswordValidation;
import cn.he.zhao.bbs.validate.UpdateProfilesValidation;
import cn.he.zhao.bbs.validate.UpdateSyncB3Validation;
import com.qiniu.util.Auth;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import cn.he.zhao.bbs.advice.*;
import cn.he.zhao.bbs.entity.*;
import cn.he.zhao.bbs.service.*;
import cn.he.zhao.bbs.service.interf.LangPropsService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Settings processor.
 * <ul>
 * <li>Shows settings (/settings), GET</li>
 * <li>Shows settings pages (/settings/*), GET</li>
 * <li>Updates profiles (/settings/profiles), POST</li>
 * <li>Updates user avatar (/settings/avatar), POST</li>
 * <li>Geo status (/settings/geo/status), POST</li>
 * <li>Sync (/settings/sync/b3), POST</li>
 * <li>Privacy (/settings/privacy), POST</li>
 * <li>Function (/settings/function), POST</li>
 * <li>Updates emotions (/settings/emotionList), POST</li>
 * <li>Password (/settings/password), POST</li>
 * <li>Updates i18n (/settings/i18n), POST</li>
 * <li>Sends email verify code (/settings/email/vc), POST</li>
 * <li>Updates email (/settings/email), POST</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.0, Jun 12, 2018
 * @since 2.4.0
 */
@Controller
public class SettingsProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsProcessor.class);

    /**
     * User management service.
     */
    @Autowired
    private UserMgmtService userMgmtService;

    /**
     * User query service.
     */
    @Autowired
    private UserQueryService userQueryService;

    /**
     * InvitecodeUtil query service.
     */
    @Autowired
    private InvitecodeQueryService invitecodeQueryService;

    /**
     * OptionUtil query service.
     */
    @Autowired
    private OptionQueryService optionQueryService;

    /**
     * Language service.
     */
    @Autowired
    private LangPropsService langPropsService;

    /**
     * EmotionUtil query service.
     */
    @Autowired
    private EmotionQueryService emotionQueryService;

    /**
     * EmotionUtil management service.
     */
    @Autowired
    private EmotionMgmtService emotionMgmtService;

    /**
     * Data entity service.
     */
    @Autowired
    private DataModelService dataModelService;

    /**
     * Avatar query service.
     */
    @Autowired
    private AvatarQueryService avatarQueryService;

    /**
     * RoleUtil query service.
     */
    @Autowired
    private RoleQueryService roleQueryService;

    /**
     * Verifycode query service.
     */
    @Autowired
    private VerifycodeQueryService verifycodeQueryService;

    /**
     * Verifycode management service.
     */
    @Autowired
    private VerifycodeMgmtService verifycodeMgmtService;


    /**
     * Sends email verify code.
     *
     * @param request           the specified request
     * @param requestJSONObject the specified request json object
     */
    @RequestMapping(value = "/settings/email/vc", method = RequestMethod.POST)
//    @Before(adviceClass = {LoginCheck.class})
    @LoginCheckAnno
    public void sendEmailVC(Map<String, Object> dataModel, final HttpServletRequest request, final JSONObject requestJSONObject) {
        dataModel.put(Keys.STATUS_CODE,false);

        final String email = StringUtils.lowerCase(StringUtils.trim(requestJSONObject.optString(User.USER_EMAIL)));
        if (!Strings.isEmail(email)) {
            final String msg = langPropsService.get("sendFailedLabel") + " - " + langPropsService.get("invalidEmailLabel");
            dataModel.put("msg",msg);

            return;
        }

        final String captcha = requestJSONObject.optString(CaptchaProcessor.CAPTCHA);
        if (CaptchaProcessor.invalidCaptcha(captcha)) {
            final String msg = langPropsService.get("sendFailedLabel") + " - " + langPropsService.get("captchaErrorLabel");
            dataModel.put("msg",msg);

            return;
        }

        final JSONObject user = (JSONObject) request.getAttribute(User.USER);
        if (email.equalsIgnoreCase(user.optString(User.USER_EMAIL))) {
            final String msg = langPropsService.get("sendFailedLabel") + " - " + langPropsService.get("bindedLabel");
            dataModel.put("msg",msg);

            return;
        }

        final String userId = user.optString(Keys.OBJECT_ID);
        try {
            Verifycode verifycode1 = verifycodeQueryService.getVerifycodeByUserId(VerifycodeUtil.TYPE_C_EMAIL, VerifycodeUtil.BIZ_TYPE_C_BIND_EMAIL, userId);
            if (null != verifycode1) {
//                context.renderTrueResult().renderMsg(langPropsService.get("vcSentLabel"));
                dataModel.put(Keys.STATUS_CODE,false);
                dataModel.put(Keys.MSG,langPropsService.get("vcSentLabel"));

                return;
            }

            if (null != userQueryService.getUserByEmail(email)) {
                dataModel.put(Keys.MSG ,langPropsService.get("duplicatedEmailLabel"));

                return;
            }

            final String code = RandomStringUtils.randomNumeric(6);
            Verifycode verifycode = new Verifycode();
            verifycode.setUserId(userId);
            verifycode.setBizType(VerifycodeUtil.BIZ_TYPE_C_BIND_EMAIL);
            verifycode.setType(VerifycodeUtil.TYPE_C_EMAIL);
            verifycode.setCode(code);
            verifycode.setStatus( VerifycodeUtil.STATUS_C_UNSENT);
            verifycode.setExpired(DateUtils.addMinutes(new Date(), 10).getTime());
            verifycode.setReceiver( email);
            verifycodeMgmtService.addVerifycode(verifycode);

//            context.renderTrueResult().renderMsg(langPropsService.get("verifycodeSentLabel"));
            dataModel.put(Keys.STATUS_CODE,true);
            dataModel.put(Keys.MSG,langPropsService.get("verifycodeSentLabel"));
        } catch ( final Exception e) {
            dataModel.put(Keys.MSG ,e.getMessage());
        }
    }

    /**
     * Updates email.
     *
     * @param request           the specified request
     * @param requestJSONObject the specified request json object
     */
    @RequestMapping(value = "/settings/email", method = RequestMethod.POST)
//    @Before(adviceClass = {LoginCheck.class})
    @LoginCheckAnno
    public void updateEmail(Map<String, Object> dataModel, final HttpServletRequest request, final JSONObject requestJSONObject) {
        dataModel.put(Keys.STATUS_CODE,false);

        final String captcha = requestJSONObject.optString(CaptchaProcessor.CAPTCHA);
        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String userId = currentUser.optString(Keys.OBJECT_ID);
        try {
            final Verifycode verifycode = verifycodeQueryService.getVerifycodeByUserId(VerifycodeUtil.TYPE_C_EMAIL, VerifycodeUtil.BIZ_TYPE_C_BIND_EMAIL, userId);
            if (null == verifycode) {
                final String msg = langPropsService.get("updateFailLabel") + " - " + langPropsService.get("captchaErrorLabel");
                dataModel.put("msg",msg);
                dataModel.put(Common.CODE, 2);

                return;
            }

            if (!StringUtils.equals(verifycode.getCode(), captcha)) {
                final String msg = langPropsService.get("updateFailLabel") + " - " + langPropsService.get("captchaErrorLabel");
                dataModel.put("msg",msg);
                dataModel.put(Common.CODE, 2);

                return;
            }

            final UserExt user = userQueryService.getUser(userId);
            final String email = verifycode.getReceiver();
            user.setUserEmail(email);
            JSONObject jsonObject = new JSONObject(JsonUtil.objectToJson(user));
            userMgmtService.updateUserEmail(userId, jsonObject);
            verifycodeMgmtService.removeByCode(captcha);

            dataModel.put(Keys.STATUS_CODE,true);
        } catch ( final Exception e) {
            dataModel.put(Keys.MSG ,e.getMessage());
        }
    }

    /**
     * Updates user i18n.
     *

     * @param request  the specified request
     * @param response the specified response
     */
    @RequestMapping(value = "/settings/i18n", method = RequestMethod.POST)
//    @Before(adviceClass = {LoginCheck.class, CSRFCheck.class})
    @LoginCheckAnno
    @CSRFCheckAnno
    public void updateI18n(Map<String, Object> dataModel,
                           final HttpServletRequest request, final HttpServletResponse response) {
        dataModel.put(Keys.STATUS_CODE,false);

        JSONObject requestJSONObject;
        try {
            requestJSONObject = Requests.parseRequestJSONObject(request, response);
            request.setAttribute(Keys.REQUEST, requestJSONObject);
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage());

            requestJSONObject = new JSONObject();
        }

        String userLanguage = requestJSONObject.optString(UserExtUtil.USER_LANGUAGE, Locale.SIMPLIFIED_CHINESE.toString());
        if (!Languages.getAvailableLanguages().contains(userLanguage)) {
            userLanguage = Locale.US.toString();
        }

        String userTimezone = requestJSONObject.optString(UserExtUtil.USER_TIMEZONE, TimeZone.getDefault().getID());
        if (!Arrays.asList(TimeZone.getAvailableIDs()).contains(userTimezone)) {
            userTimezone = TimeZone.getDefault().getID();
        }

        try {
            final UserExt user = userQueryService.getCurrentUser(request);
            user.setUserLanguage(userLanguage);
            user.setUserTimezone(userTimezone);

            userMgmtService.updateUser(user.getOid(), user);

            dataModel.put(Keys.STATUS_CODE,true);
        } catch ( final Exception e) {
            dataModel.put(Keys.MSG ,e.getMessage());
        }
    }

    /**
     * Shows settings pages.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = {"/settings", "/settings/*"}, method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
    @StopWatchStartAnno
    @LoginCheckAnno
//    @After(adviceClass = {CSRFToken.class, PermissionGrant.class, StopwatchEndAdvice.class})
    @CSRFTokenAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public void showSettings(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
        final String requestURI = request.getRequestURI();
        String page = StringUtils.substringAfter(requestURI, "/settings/");
        if (StringUtils.isBlank(page)) {
            page = "profile";
        }
        page += ".ftl";
//        renderer.setTemplateName("/home/settings/" + page);
        String url = "/home/settings/" + page;
//        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject user = (JSONObject) request.getAttribute(User.USER);
        user.put(UserExtUtil.USER_T_CREATE_TIME, new Date(user.getLong(Keys.OBJECT_ID)));
        UserProcessor.fillHomeUser(dataModel, user, roleQueryService);

        final int avatarViewMode = (int) request.getAttribute(UserExtUtil.USER_AVATAR_VIEW_MODE);
        avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

        final String userId = user.optString(Keys.OBJECT_ID);

        final int invitedUserCount = userQueryService.getInvitedUserCount(userId);
        dataModel.put(Common.INVITED_USER_COUNT, invitedUserCount);

        // Qiniu file upload authenticate
        final Auth auth = Auth.create(Symphonys.get("qiniu.accessKey"), Symphonys.get("qiniu.secretKey"));
        final String uploadToken = auth.uploadToken(Symphonys.get("qiniu.bucket"));
        dataModel.put("qiniuUploadToken", uploadToken);
        dataModel.put("qiniuDomain", Symphonys.get("qiniu.domain"));

        if (!Symphonys.getBoolean("qiniu.enabled")) {
            dataModel.put("qiniuUploadToken", "");
        }

        final long imgMaxSize = Symphonys.getLong("upload.img.maxSize");
        dataModel.put("imgMaxSize", imgMaxSize);
        final long fileMaxSize = Symphonys.getLong("upload.file.maxSize");
        dataModel.put("fileMaxSize", fileMaxSize);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        String inviteTipLabel = (String) dataModel.get("inviteTipLabel");
        inviteTipLabel = inviteTipLabel.replace("{point}", String.valueOf(PointtransferUtil.TRANSFER_SUM_C_INVITE_REGISTER));
        dataModel.put("inviteTipLabel", inviteTipLabel);

        String pointTransferTipLabel = (String) dataModel.get("pointTransferTipLabel");
        pointTransferTipLabel = pointTransferTipLabel.replace("{point}", Symphonys.get("pointTransferMin"));
        dataModel.put("pointTransferTipLabel", pointTransferTipLabel);

        String dataExportTipLabel = (String) dataModel.get("dataExportTipLabel");
        dataExportTipLabel = dataExportTipLabel.replace("{point}",
                String.valueOf(PointtransferUtil.TRANSFER_SUM_C_DATA_EXPORT));
        dataModel.put("dataExportTipLabel", dataExportTipLabel);

        final String allowRegister = optionQueryService.getAllowRegister();
        dataModel.put("allowRegister", allowRegister);

        String buyInvitecodeLabel = langPropsService.get("buyInvitecodeLabel");
        buyInvitecodeLabel = buyInvitecodeLabel.replace("${point}",
                String.valueOf(PointtransferUtil.TRANSFER_SUM_C_BUY_INVITECODE));
        buyInvitecodeLabel = buyInvitecodeLabel.replace("${point2}",
                String.valueOf(PointtransferUtil.TRANSFER_SUM_C_INVITECODE_USED));
        dataModel.put("buyInvitecodeLabel", buyInvitecodeLabel);

        final List<Invitecode> invitecodes = invitecodeQueryService.getValidInvitecodes(userId);
        for (final Invitecode invitecode : invitecodes) {
            String msg = langPropsService.get("expireTipLabel");
            msg = msg.replace("${time}", DateFormatUtils.format(Long.parseLong(invitecode.getOid()
                    + Symphonys.getLong("invitecode.expired")), "yyyy-MM-dd HH:mm"));
            invitecode.setMemo( msg);
        }

        dataModel.put(InvitecodeUtil.INVITECODES, (Object) invitecodes);

        if (requestURI.contains("function")) {
            dataModel.put(EmotionUtil.EMOTIONS, emotionQueryService.getEmojis(userId));
            dataModel.put(EmotionUtil.SHORT_T_LIST, emojiLists);
        }

        if (requestURI.contains("i18n")) {
            dataModel.put(Common.LANGUAGES, Languages.getAvailableLanguages());

            final List<JSONObject> timezones = new ArrayList<>();
            final List<TimeZones.TimeZoneWithDisplayNames> timeZones = TimeZones.getInstance().getTimeZones();
            for (final TimeZones.TimeZoneWithDisplayNames timeZone : timeZones) {
                final JSONObject timezone = new JSONObject();

                timezone.put(Common.ID, timeZone.getTimeZone().getID());
                timezone.put(Common.NAME, timeZone.getDisplayName());

                timezones.add(timezone);
            }
            dataModel.put(Common.TIMEZONES, timezones);
        }

        dataModel.put(Common.TYPE, "settings");
    }

    /**
     * Updates user geo status.
     *

     * @param request  the specified request
     * @param response the specified response
     */
    @RequestMapping(value = "/settings/geo/status", method = RequestMethod.POST)
//    @Before(adviceClass = {LoginCheck.class, CSRFCheck.class})
    @LoginCheckAnno
    @CSRFCheckAnno
    public void updateGeoStatus(Map<String, Object> dataModel,
                                final HttpServletRequest request, final HttpServletResponse response) {
        dataModel.put(Keys.STATUS_CODE,false);

        JSONObject requestJSONObject;
        try {
            requestJSONObject = Requests.parseRequestJSONObject(request, response);
            request.setAttribute(Keys.REQUEST, requestJSONObject);
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage());

            requestJSONObject = new JSONObject();
        }

        int geoStatus = requestJSONObject.optInt(UserExtUtil.USER_GEO_STATUS);
        if (UserExtUtil.USER_GEO_STATUS_C_PRIVATE != geoStatus && UserExtUtil.USER_GEO_STATUS_C_PUBLIC != geoStatus) {
            geoStatus = UserExtUtil.USER_GEO_STATUS_C_PUBLIC;
        }

        try {
            final UserExt user = userQueryService.getCurrentUser(request);
            user.setUserGeoStatus(geoStatus);

            userMgmtService.updateUser(user.getOid(), user);

            dataModel.put(Keys.STATUS_CODE,true);
        } catch ( final Exception e) {
            dataModel.put(Keys.MSG ,e.getMessage());
        }
    }

    /**
     * Updates user privacy.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/settings/privacy", method = RequestMethod.POST)
//    @Before(adviceClass = {LoginCheck.class, CSRFCheck.class})
    @LoginCheckAnno
    @CSRFCheckAnno
    public void updatePrivacy(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        dataModel.put(Keys.STATUS_CODE,false);

        JSONObject requestJSONObject;
        try {
            requestJSONObject = Requests.parseRequestJSONObject(request, response);
            request.setAttribute(Keys.REQUEST, requestJSONObject);
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage());

            requestJSONObject = new JSONObject();
        }

        final boolean articleStatus = requestJSONObject.optBoolean(UserExtUtil.USER_ARTICLE_STATUS);
        final boolean commentStatus = requestJSONObject.optBoolean(UserExtUtil.USER_COMMENT_STATUS);
        final boolean followingUserStatus = requestJSONObject.optBoolean(UserExtUtil.USER_FOLLOWING_USER_STATUS);
        final boolean followingTagStatus = requestJSONObject.optBoolean(UserExtUtil.USER_FOLLOWING_TAG_STATUS);
        final boolean followingArticleStatus = requestJSONObject.optBoolean(UserExtUtil.USER_FOLLOWING_ARTICLE_STATUS);
        final boolean watchingArticleStatus = requestJSONObject.optBoolean(UserExtUtil.USER_WATCHING_ARTICLE_STATUS);
        final boolean followerStatus = requestJSONObject.optBoolean(UserExtUtil.USER_FOLLOWER_STATUS);
        final boolean breezemoonStatus = requestJSONObject.optBoolean(UserExtUtil.USER_BREEZEMOON_STATUS);
        final boolean pointStatus = requestJSONObject.optBoolean(UserExtUtil.USER_POINT_STATUS);
        final boolean onlineStatus = requestJSONObject.optBoolean(UserExtUtil.USER_ONLINE_STATUS);
        final boolean timelineStatus = requestJSONObject.optBoolean(UserExtUtil.USER_TIMELINE_STATUS);
        final boolean uaStatus = requestJSONObject.optBoolean(UserExtUtil.USER_UA_STATUS);
        final boolean userForgeLinkStatus = requestJSONObject.optBoolean(UserExtUtil.USER_FORGE_LINK_STATUS);
        final boolean userJoinPointRank = requestJSONObject.optBoolean(UserExtUtil.USER_JOIN_POINT_RANK);
        final boolean userJoinUsedPointRank = requestJSONObject.optBoolean(UserExtUtil.USER_JOIN_USED_POINT_RANK);

        final UserExt user = userQueryService.getCurrentUser(request);

        user.setUserOnlineStatus(onlineStatus
                ? UserExtUtil.USER_XXX_STATUS_C_PUBLIC : UserExtUtil.USER_XXX_STATUS_C_PRIVATE);
        user.setUserArticleStatus(articleStatus
                ? UserExtUtil.USER_XXX_STATUS_C_PUBLIC : UserExtUtil.USER_XXX_STATUS_C_PRIVATE);
        user.setUserCommentStatus( commentStatus
                ? UserExtUtil.USER_XXX_STATUS_C_PUBLIC : UserExtUtil.USER_XXX_STATUS_C_PRIVATE);
        user.setUserFollowingUserStatus(followingUserStatus
                ? UserExtUtil.USER_XXX_STATUS_C_PUBLIC : UserExtUtil.USER_XXX_STATUS_C_PRIVATE);
        user.setUserFollowingTagStatus(followingTagStatus
                ? UserExtUtil.USER_XXX_STATUS_C_PUBLIC : UserExtUtil.USER_XXX_STATUS_C_PRIVATE);
        user.setUserFollowingArticleStatus(followingArticleStatus
                ? UserExtUtil.USER_XXX_STATUS_C_PUBLIC : UserExtUtil.USER_XXX_STATUS_C_PRIVATE);
        user.setUserWatchingArticleStatus(watchingArticleStatus
                ? UserExtUtil.USER_XXX_STATUS_C_PUBLIC : UserExtUtil.USER_XXX_STATUS_C_PRIVATE);
        user.setUserFollowerStatus( followerStatus
                ? UserExtUtil.USER_XXX_STATUS_C_PUBLIC : UserExtUtil.USER_XXX_STATUS_C_PRIVATE);
        user.setUserBreezemoonStatus(breezemoonStatus
                ? UserExtUtil.USER_XXX_STATUS_C_PUBLIC : UserExtUtil.USER_XXX_STATUS_C_PRIVATE);
        user.setUserPointStatus(pointStatus
                ? UserExtUtil.USER_XXX_STATUS_C_PUBLIC : UserExtUtil.USER_XXX_STATUS_C_PRIVATE);
        user.setUserTimelineStatus(timelineStatus
                ? UserExtUtil.USER_XXX_STATUS_C_PUBLIC : UserExtUtil.USER_XXX_STATUS_C_PRIVATE);
        user.setUserUAStatus(uaStatus
                ? UserExtUtil.USER_XXX_STATUS_C_PUBLIC : UserExtUtil.USER_XXX_STATUS_C_PRIVATE);
        user.setUserJoinPointRank(userJoinPointRank
                ? UserExtUtil.USER_JOIN_POINT_RANK_C_JOIN : UserExtUtil.USER_JOIN_POINT_RANK_C_NOT_JOIN);
        user.setUserJoinUsedPointRank(userJoinUsedPointRank
                ? UserExtUtil.USER_JOIN_USED_POINT_RANK_C_JOIN : UserExtUtil.USER_JOIN_USED_POINT_RANK_C_NOT_JOIN);
        user.setUserForgeLinkStatus( userForgeLinkStatus
                ? UserExtUtil.USER_XXX_STATUS_C_PUBLIC : UserExtUtil.USER_XXX_STATUS_C_PRIVATE);

        try {
            userMgmtService.updateUser(user.getOid(), user);

            dataModel.put(Keys.STATUS_CODE,true);
        } catch ( final Exception e) {
            dataModel.put(Keys.MSG ,e.getMessage());
        }
    }

    /**
     * Updates user function.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/settings/function", method = RequestMethod.POST)
//    @Before(adviceClass = {LoginCheck.class, CSRFCheck.class})
    @LoginCheckAnno
    @CSRFCheckAnno
    public void updateFunction(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        dataModel.put(Keys.STATUS_CODE,false);

        JSONObject requestJSONObject;
        try {
            requestJSONObject = Requests.parseRequestJSONObject(request, response);
            request.setAttribute(Keys.REQUEST, requestJSONObject);
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage());

            requestJSONObject = new JSONObject();
        }

        String userListPageSizeStr = requestJSONObject.optString(UserExtUtil.USER_LIST_PAGE_SIZE);
        final int userCommentViewMode = requestJSONObject.optInt(UserExtUtil.USER_COMMENT_VIEW_MODE);
        final int userAvatarViewMode = requestJSONObject.optInt(UserExtUtil.USER_AVATAR_VIEW_MODE);
        final int userListViewMode = requestJSONObject.optInt(UserExtUtil.USER_LIST_VIEW_MODE);
        final boolean notifyStatus = requestJSONObject.optBoolean(UserExtUtil.USER_NOTIFY_STATUS);
        final boolean subMailStatus = requestJSONObject.optBoolean(UserExtUtil.USER_SUB_MAIL_STATUS);
        final boolean keyboardShortcutsStatus = requestJSONObject.optBoolean(UserExtUtil.USER_KEYBOARD_SHORTCUTS_STATUS);
        final boolean userReplyWatchArticleStatus = requestJSONObject.optBoolean(UserExtUtil.USER_REPLY_WATCH_ARTICLE_STATUS);

        int userListPageSize;
        try {
            userListPageSize = Integer.valueOf(userListPageSizeStr);

            if (10 > userListPageSize) {
                userListPageSize = 10;
            }

            if (userListPageSize > 60) {
                userListPageSize = 60;
            }
        } catch (final Exception e) {
            userListPageSize = Symphonys.getInt("indexArticlesCnt");
        }

        final UserExt user = userQueryService.getCurrentUser(request);

        user.setUserListPageSize(userListPageSize);
        user.setUserCommentViewMode(userCommentViewMode);
        user.setUserAvatarViewMode(userAvatarViewMode);
        user.setUserListViewMode(userListViewMode);
        user.setUserNotifyStatus(notifyStatus
                ? UserExtUtil.USER_XXX_STATUS_C_ENABLED : UserExtUtil.USER_XXX_STATUS_C_DISABLED);
        user.setUserSubMailStatus(subMailStatus
                ? UserExtUtil.USER_XXX_STATUS_C_ENABLED : UserExtUtil.USER_XXX_STATUS_C_DISABLED);
        user.setUserKeyboardShortcutsStatus(keyboardShortcutsStatus
                ? UserExtUtil.USER_XXX_STATUS_C_ENABLED : UserExtUtil.USER_XXX_STATUS_C_DISABLED);
        user.setUserWatchingArticleStatus(userReplyWatchArticleStatus
                ? UserExtUtil.USER_XXX_STATUS_C_ENABLED : UserExtUtil.USER_XXX_STATUS_C_DISABLED);

        try {
            userMgmtService.updateUser(user.getOid(), user);

            dataModel.put(Keys.STATUS_CODE,true);
        } catch ( final Exception e) {
            dataModel.put(Keys.MSG ,e.getMessage());
        }
    }

    /**
     * Updates user profiles.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/settings/profiles", method = RequestMethod.POST)
//    @Before(adviceClass = {LoginCheck.class, CSRFCheck.class, UpdateProfilesValidation.class})
    @LoginCheckAnno
    @CSRFCheckAnno
    public void updateProfiles(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        dataModel.put(Keys.STATUS_CODE,false);

        try {
            UpdateProfilesValidation.doAdvice(request);
        } catch (RequestProcessAdviceException e){
            dataModel.put(Keys.MSG,e.getJsonObject().get(Keys.MSG));
        }

        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);

        final String userTags = requestJSONObject.optString(UserExtUtil.USER_TAGS);
        final String userURL = requestJSONObject.optString(User.USER_URL);
        final String userQQ = requestJSONObject.optString(UserExtUtil.USER_QQ);
        final String userIntro = requestJSONObject.optString(UserExtUtil.USER_INTRO);
        final String userNickname = requestJSONObject.optString(UserExtUtil.USER_NICKNAME);

        final UserExt user = userQueryService.getCurrentUser(request);

        user.setUserTags(userTags);
        user.setUserURL(userURL);
        user.setUserQQ(userQQ);
        user.setUserIntro( userIntro);
        user.setUserNickname( userNickname);
        user.setUserAvatarType(UserExtUtil.USER_AVATAR_TYPE_C_UPLOAD);

        try {
            JSONObject jsonObject = new JSONObject(JsonUtil.objectToJson(user));
            userMgmtService.updateProfiles(jsonObject);

            dataModel.put(Keys.STATUS_CODE,true);
        } catch ( final Exception e) {
            dataModel.put(Keys.MSG ,e.getMessage());
        }
    }

    /**
     * Updates user avatar.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/settings/avatar", method = RequestMethod.POST)
//    @Before(adviceClass = {LoginCheck.class, CSRFCheck.class, UpdateProfilesValidation.class})
    @LoginCheckAnno
    @CSRFCheckAnno
    public void updateAvatar(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        dataModel.put(Keys.STATUS_CODE,false);

        try {
            UpdateProfilesValidation.doAdvice(request);
        } catch (RequestProcessAdviceException e){
            dataModel.put(Keys.MSG,e.getJsonObject().get(Keys.MSG));
        }

        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);
        final String userAvatarURL = requestJSONObject.optString(UserExtUtil.USER_AVATAR_URL);

        final UserExt user = userQueryService.getCurrentUser(request);

        user.setUserAvatarType(UserExtUtil.USER_AVATAR_TYPE_C_UPLOAD);
        user.setUserUpdateTime(System.currentTimeMillis());

        if (Strings.contains(userAvatarURL, new String[]{"<", ">", "\"", "'"})) {
            user.setUserAvatarURL(Symphonys.get("defaultThumbnailURL"));
        } else {
            if (Symphonys.getBoolean("qiniu.enabled")) {
                final String qiniuDomain = Symphonys.get("qiniu.domain");

                if (!StringUtils.startsWith(userAvatarURL, qiniuDomain)) {
                    user.setUserAvatarURL(Symphonys.get("defaultThumbnailURL"));
                } else {
                    user.setUserAvatarURL(userAvatarURL);
                }
            } else {
                user.setUserAvatarURL(userAvatarURL);
            }
        }

        try {
            userMgmtService.updateUser(user.getOid(), user);

            dataModel.put(Keys.STATUS_CODE,true);
        } catch ( final Exception e) {
            dataModel.put(Keys.MSG ,e.getMessage());
        }
    }

    /**
     * Updates user B3log sync.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/settings/sync/b3", method = RequestMethod.POST)
//    @Before(adviceClass = {LoginCheck.class, CSRFCheck.class, UpdateSyncB3Validation.class})
    @LoginCheckAnno
    @CSRFCheckAnno
    public void updateSyncB3(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        dataModel.put(Keys.STATUS_CODE,false);
        try {
            UpdateSyncB3Validation.doAdvice(request);
        } catch (RequestProcessAdviceException e){
            dataModel.put(Keys.MSG,e.getJsonObject().get(Keys.MSG));
        }

        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);

        final String b3Key = requestJSONObject.optString(UserExtUtil.USER_B3_KEY);
        final String addArticleURL = requestJSONObject.optString(UserExtUtil.USER_B3_CLIENT_ADD_ARTICLE_URL);
        final String updateArticleURL = requestJSONObject.optString(UserExtUtil.USER_B3_CLIENT_UPDATE_ARTICLE_URL);
        final String addCommentURL = requestJSONObject.optString(UserExtUtil.USER_B3_CLIENT_ADD_COMMENT_URL);
        final boolean syncWithSymphonyClient = requestJSONObject.optBoolean(UserExtUtil.SYNC_TO_CLIENT, false);

        final UserExt user = userQueryService.getCurrentUser(request);
        user.setUserB3Key(b3Key);
        user.setUserB3ClientAddArticleURL(addArticleURL);
        user.setUserB3ClientUpdateArticleURL(updateArticleURL);
        user.setUserB3ClientAddCommentURL(addCommentURL);
        user.setSyncWithSymphonyClient(String.valueOf(syncWithSymphonyClient));

        try {
            JSONObject jsonObject = new JSONObject(JsonUtil.objectToJson(user));
            userMgmtService.updateSyncB3(jsonObject);

            dataModel.put(Keys.STATUS_CODE,true);
        } catch ( final Exception e) {
            final String msg = langPropsService.get("updateFailLabel") + " - " + e.getMessage();
            LOGGER.error( msg, e);

            dataModel.put("msg",msg);
        }
    }

    /**
     * Updates user password.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/settings/password", method = RequestMethod.POST)
//    @Before(adviceClass = {LoginCheck.class, CSRFCheck.class, UpdatePasswordValidation.class})
    @LoginCheckAnno
    @CSRFCheckAnno
    public void updatePassword(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        dataModel.put(Keys.STATUS_CODE,false);

        try {
            UpdatePasswordValidation.doAdvice(request);
        } catch (RequestProcessAdviceException e){
            dataModel.put(Keys.MSG,e.getJsonObject().get(Keys.MSG));
        }

        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);

        final String password = requestJSONObject.optString(User.USER_PASSWORD);
        final String newPassword = requestJSONObject.optString(User.USER_NEW_PASSWORD);

        final UserExt user = userQueryService.getCurrentUser(request);

        if (!password.equals(user.getUserPassword())) {
            dataModel.put(Keys.MSG ,langPropsService.get("invalidOldPwdLabel"));

            return;
        }

        user.setUserPassword(newPassword);

        try {
            JSONObject jsonObject = new JSONObject(JsonUtil.objectToJson(user));
            userMgmtService.updatePassword(jsonObject);
            dataModel.put(Keys.STATUS_CODE,true);
        } catch ( final Exception e) {
            final String msg = langPropsService.get("updateFailLabel") + " - " + e.getMessage();
            LOGGER.error( msg, e);

            dataModel.put("msg",msg);
        }
    }

    /**
     * Updates user emotions.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/settings/emotionList", method = RequestMethod.POST)
//    @Before(adviceClass = {LoginCheck.class, CSRFCheck.class, UpdateEmotionListValidation.class})
    @LoginCheckAnno
    @CSRFCheckAnno
    public void updateEmoji(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        dataModel.put(Keys.STATUS_CODE,false);

        try {
            UpdateEmotionListValidation.doAdvice(request);
        } catch (RequestProcessAdviceException e){
            dataModel.put(Keys.MSG,e.getJsonObject().get(Keys.MSG));
        }

        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);
        final String emotionList = requestJSONObject.optString(EmotionUtil.EMOTIONS);

        final UserExt user = userQueryService.getCurrentUser(request);

        try {
            emotionMgmtService.setEmotionList(user.getOid(), emotionList);

            dataModel.put(Keys.STATUS_CODE,true);
        } catch ( final Exception e) {
            final String msg = langPropsService.get("updateFailLabel") + " - " + e.getMessage();
            LOGGER.error( msg, e);

            dataModel.put("msg",msg);
        }
    }

    private static final String[][] emojiLists = {{
            "smile",
            "laughing",
            "smirk",
            "heart_eyes",
            "kissing_heart",
            "flushed",
            "grin",
            "stuck_out_tongue_closed_eyes",
            "kissing",
            "sleeping",
            "anguished",
            "open_mouth",
            "expressionless",
            "unamused",
            "sweat_smile",
            "weary",
            "sob",
            "joy",
            "astonished",
            "scream"
    }, {
            "tired_face",
            "rage",
            "triumph",
            "yum",
            "mask",
            "sunglasses",
            "dizzy_face",
            "imp",
            "smiling_imp",
            "innocent",
            "alien",
            "yellow_heart",
            "blue_heart",
            "purple_heart",
            "heart",
            "green_heart",
            "broken_heart",
            "dizzy",
            "anger",
            "exclamation"
    }, {
            "question",
            "zzz",
            "notes",
            "poop",
            "+1",
            "-1",
            "ok_hand",
            "punch",
            "v",
            "hand",
            "point_up",
            "point_down",
            "pray",
            "clap",
            "muscle",
            "ok_woman",
            "no_good",
            "raising_hand",
            "massage",
            "haircut"
    }, {
            "nail_care",
            "see_no_evil",
            "feet",
            "kiss",
            "eyes",
            "trollface",
            "snowman",
            "zap",
            "cat",
            "dog",
            "mouse",
            "hamster",
            "rabbit",
            "frog",
            "koala",
            "pig",
            "monkey",
            "racehorse",
            "camel",
            "sheep"
    }, {
            "elephant",
            "panda_face",
            "snake",
            "hatched_chick",
            "hatching_chick",
            "turtle",
            "bug",
            "honeybee",
            "beetle",
            "snail",
            "octopus",
            "whale",
            "dolphin",
            "dragon",
            "goat",
            "paw_prints",
            "tulip",
            "four_leaf_clover",
            "rose",
            "mushroom"
    }, {
            "seedling",
            "shell",
            "crescent_moon",
            "partly_sunny",
            "octocat",
            "jack_o_lantern",
            "ghost",
            "santa",
            "tada",
            "camera",
            "loudspeaker",
            "hourglass",
            "lock",
            "key",
            "bulb",
            "hammer",
            "moneybag",
            "smoking",
            "bomb",
            "gun"
    }, {
            "hocho",
            "pill",
            "syringe",
            "scissors",
            "swimmer",
            "black_joker",
            "coffee",
            "tea",
            "sake",
            "beer",
            "wine_glass",
            "pizza",
            "hamburger",
            "poultry_leg",
            "meat_on_bone",
            "dango",
            "doughnut",
            "icecream",
            "shaved_ice",
            "cake"
    }, {
            "cookie",
            "lollipop",
            "apple",
            "green_apple",
            "tangerine",
            "lemon",
            "cherries",
            "grapes",
            "watermelon",
            "strawberry",
            "peach",
            "melon",
            "banana",
            "pear",
            "pineapple",
            "sweet_potato",
            "eggplant",
            "tomato",
            EmotionUtil.EOF_EMOJI // 标记结束以便在function.ftl中处理
    }};
}
