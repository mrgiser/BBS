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

import cn.he.zhao.bbs.advice.*;
import cn.he.zhao.bbs.exception.RequestProcessAdviceException;
import cn.he.zhao.bbs.entity.*;
import cn.he.zhao.bbs.entity.my.*;
import cn.he.zhao.bbs.service.*;
import cn.he.zhao.bbs.service.interf.LangPropsService;
import cn.he.zhao.bbs.spring.Locales;
import cn.he.zhao.bbs.spring.Requests;
import cn.he.zhao.bbs.spring.SpringUtil;
import cn.he.zhao.bbs.spring.Strings;
import cn.he.zhao.bbs.util.Sessions;
import cn.he.zhao.bbs.util.Symphonys;
import cn.he.zhao.bbs.validate.UserForgetPwdValidation;
import cn.he.zhao.bbs.validate.UserRegister2Validation;
import cn.he.zhao.bbs.validate.UserRegisterValidation;
import com.qiniu.util.Auth;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Login/Register processor.
 * <ul>
 * <li>Registration (/register), GET/POST</li>
 * <li>Login (/login), GET/POST</li>
 * <li>Logout (/logout), GET</li>
 * <li>Reset password (/reset-pwd), GET/POST</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @version 1.13.12.3, Jun 27, 2018
 * @since 0.2.0
 */
@Controller
public class LoginProcessor {

    /**
     * Wrong password tries.
     * <p>
     * &lt;userId, {"wrongCount": int, "captcha": ""}&gt;
     * </p>
     */
    public static final Map<String, JSONObject> WRONG_PWD_TRIES = new ConcurrentHashMap<>();

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginProcessor.class);

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
     * Language service.
     */
    @Autowired
    private LangPropsService langPropsService;

    /**
     * Pointtransfer management service.
     */
    @Autowired
    private PointtransferMgmtService pointtransferMgmtService;

    /**
     * Data entity service.
     */
    @Autowired
    private DataModelService dataModelService;

    /**
     * Verifycode management service.
     */
    @Autowired
    private VerifycodeMgmtService verifycodeMgmtService;

    /**
     * Verifycode query service.
     */
    @Autowired
    private VerifycodeQueryService verifycodeQueryService;

    /**
     * Option query service.
     */
    @Autowired
    private OptionQueryService optionQueryService;

    /**
     * Invitecode query service.
     */
    @Autowired
    private InvitecodeQueryService invitecodeQueryService;

    /**
     * Invitecode management service.
     */
    @Autowired
    private InvitecodeMgmtService invitecodeMgmtService;

    /**
     * Invitecode management service.
     */
    @Autowired
    private NotificationMgmtService notificationMgmtService;

    /**
     * Role query service.
     */
    @Autowired
    private RoleQueryService roleQueryService;

    /**
     * Tag query service.
     */
    @Autowired
    private TagQueryService tagQueryService;

    /**
     * Next guide step.
     *
     * @param request  the specified request
     * @param response the specified response
     */
    @RequestMapping(value = "/guide/next", method = RequestMethod.POST)
    @LoginCheckAnno
    public void nextGuideStep(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response) {
        dataModel.put(Keys.STATUS_CODE,false);

        JSONObject requestJSONObject;
        try {
            requestJSONObject = Requests.parseRequestJSONObject(request, response);
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage());

            return;
        }

        JSONObject user = (JSONObject) request.getAttribute(User.USER);
        final String userId = user.optString(Keys.OBJECT_ID);

        int step = requestJSONObject.optInt(UserExt.USER_GUIDE_STEP);

        if (UserExt.USER_GUIDE_STEP_STAR_PROJECT < step || UserExt.USER_GUIDE_STEP_FIN >= step) {
            step = UserExt.USER_GUIDE_STEP_FIN;
        }

        try {
            user = userQueryService.getUser(userId);
            user.put(UserExt.USER_GUIDE_STEP, step);
            userMgmtService.updateUser(userId, user);
        } catch (final Exception e) {
            LOGGER.error( "Guide next step [" + step + "] failed", e);

            return;
        }

        dataModel.put(Keys.STATUS_CODE,true);
    }

    /**
     * Shows guide page.
     *
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/guide", method = RequestMethod.GET)
    @StopWatchStartAnno
    @LoginCheckAnno
    @CSRFTokenAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showGuide( final HttpServletRequest request, final HttpServletResponse response, Map<String, Object> dataModel)
            throws Exception {
        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final int step = currentUser.optInt(UserExt.USER_GUIDE_STEP);
        if (UserExt.USER_GUIDE_STEP_FIN == step) {
//            return "redirect:" +(SpringUtil.getServerPath());

            return "redirect:/";
        }

//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("/verify/guide.ftl");

//        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModel.put(Common.CURRENT_USER, currentUser);

        final List<JSONObject> tags = tagQueryService.getTags(32);
        dataModel.put(Tag.TAGS, tags);

        final List<JSONObject> users = userQueryService.getNiceUsers(6);
        final Iterator<JSONObject> iterator = users.iterator();
        while (iterator.hasNext()) {
            final JSONObject user = iterator.next();
            if (user.optString(Keys.OBJECT_ID).equals(currentUser.optString(Keys.OBJECT_ID))) {
                iterator.remove();

                break;
            }
        }
        dataModel.put(User.USERS, users);

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

        return "/verify/guide.ftl";
    }

    /**
     * Shows login page.
     *
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    @StopWatchStartAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showLogin(final HttpServletRequest request, final HttpServletResponse response, Map<String, Object> dataModel)
            throws Exception {
        if (null != userQueryService.getCurrentUser(request)
                || userMgmtService.tryLogInWithCookie(request, response)) {
//            return "redirect:" +(SpringUtil.getServerPath());

            return "redirect:/";
        }

//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);

        String referer = request.getParameter(Common.GOTO);
        if (StringUtils.isBlank(referer)) {
            referer = request.getHeader("referer");
        }

        if (!StringUtils.startsWith(referer, SpringUtil.getServerPath())) {
            referer = SpringUtil.getServerPath();
        }

//        renderer.setTemplateName("/verify/login.ftl");

//        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModel.put(Common.GOTO, referer);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        return "/verify/login.ftl";
    }

    /**
     * Shows forget password page.
     *
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/forget-pwd", method = RequestMethod.GET)
    @StopWatchStartAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showForgetPwd(final HttpServletRequest request, final HttpServletResponse response, Map<String, Object> dataModel)
            throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        final Map<String, Object> dataModel = renderer.getDataModel();

//        renderer.setTemplateName("verify/forget-pwd.ftl");

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        return "verify/forget-pwd.ftl";
    }

    /**
     * Forget password.
     *
     * @param request the specified request
     */
    @RequestMapping(value = "/forget-pwd", method = RequestMethod.POST)
//    @Before(adviceClass = UserForgetPwdValidation.class)
    public void forgetPwd(Map<String, Object> dataModel, final HttpServletRequest request) {
//        dataModel.put(Keys.STATUS_CODE,false);

        dataModel.put(Keys.STATUS_CODE,false);

        try {
            UserForgetPwdValidation.doAdvice(request);
        } catch (RequestProcessAdviceException e) {
            dataModel.put(Keys.MSG, e.getJsonObject().get(Keys.MSG));
            return;
        }

        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);
        final String email = requestJSONObject.optString(User.USER_EMAIL);

        try {
            final JSONObject user = userQueryService.getUserByEmail(email);
            if (null == user) {
//                context.renderFalseResult().renderMsg(langPropsService.get("notFoundUserLabel"));
                dataModel.put("msg",langPropsService.get("notFoundUserLabel"));
                return;
            }

            final String userId = user.optString(Keys.OBJECT_ID);

            final JSONObject verifycode = new JSONObject();
            verifycode.put(Verifycode.BIZ_TYPE, Verifycode.BIZ_TYPE_C_RESET_PWD);
            final String code = RandomStringUtils.randomAlphanumeric(6);
            verifycode.put(Verifycode.CODE, code);
            verifycode.put(Verifycode.EXPIRED, DateUtils.addDays(new Date(), 1).getTime());
            verifycode.put(Verifycode.RECEIVER, email);
            verifycode.put(Verifycode.STATUS, Verifycode.STATUS_C_UNSENT);
            verifycode.put(Verifycode.TYPE, Verifycode.TYPE_C_EMAIL);
            verifycode.put(Verifycode.USER_ID, userId);
            verifycodeMgmtService.addVerifycode(verifycode);

//            context.renderTrueResult().renderMsg(langPropsService.get("verifycodeSentLabel"));
            dataModel.put("msg",langPropsService.get("verifycodeSentLabel"));
        } catch (final Exception e) {
            final String msg = langPropsService.get("resetPwdLabel") + " - " + e.getMessage();
            LOGGER.error( msg + "[email=" + email + "]");

//            dataModel.put("msg",msg);
            dataModel.put("msg", msg);
        }
    }

    /**
     * Shows reset password page.
     *
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/reset-pwd", method = RequestMethod.GET)
    @StopWatchStartAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showResetPwd(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        final Map<String, Object> dataModel = renderer.getDataModel();

        String result = null;

        final String code = request.getParameter("code");
        final JSONObject verifycode = verifycodeQueryService.getVerifycode(code);
        if (null == verifycode) {
            dataModel.put(Keys.MSG, langPropsService.get("verifycodeExpiredLabel"));
//            renderer.setTemplateName("/error/custom.ftl");
            result = "/error/custom.ftl";
        } else {
//            renderer.setTemplateName("verify/reset-pwd.ftl");
            result = "verify/reset-pwd.ftl";

            final String userId = verifycode.optString(Verifycode.USER_ID);
            final JSONObject user = userQueryService.getUser(userId);
            dataModel.put(User.USER, user);
            dataModel.put(Common.CODE, code);
        }

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        return result;
    }

    /**
     * Resets password.
     *
     * @param request  the specified request
     * @param response the specified response
     */
    @RequestMapping(value = "/reset-pwd", method = RequestMethod.POST)
    public void resetPwd(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response) {
//        dataModel.put(Keys.STATUS_CODE,false);
        dataModel.put(Keys.STATUS_CODE,false);

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, response);
        final String password = requestJSONObject.optString(User.USER_PASSWORD); // Hashed
        final String userId = requestJSONObject.optString(Common.USER_ID);
        final String code = requestJSONObject.optString(Common.CODE);
        final JSONObject verifycode = verifycodeQueryService.getVerifycode(code);
        if (null == verifycode || !verifycode.optString(Verifycode.USER_ID).equals(userId)) {
//            dataModel.put(Keys.MSG ,langPropsService.get("verifycodeExpiredLabel"));
            dataModel.put("msg", langPropsService.get("verifycodeExpiredLabel"));
            return;
        }

        String name = null;
        String email = null;
        try {
            final JSONObject user = userQueryService.getUser(userId);
            if (null == user) {
//                dataModel.put(Keys.MSG ,langPropsService.get("resetPwdLabel") + " - " + "User Not Found");
                dataModel.put("msg", langPropsService.get("resetPwdLabel") + " - " + "User Not Found");
                return;
            }

            user.put(User.USER_PASSWORD, password);
            userMgmtService.updatePassword(user);
            verifycodeMgmtService.removeByCode(code);
//            dataModel.put(Keys.STATUS_CODE,true);
            dataModel.put(Keys.STATUS_CODE,true);
            LOGGER.info("User [email=" + user.optString(User.USER_EMAIL) + "] reseted password");

            Sessions.login(request, response, user, true);
        } catch (final Exception e) {
            final String msg = langPropsService.get("resetPwdLabel") + " - " + e.getMessage();
            LOGGER.error( msg + "[name={0}, email={1}]", name, email);

//            dataModel.put("msg",msg);
            dataModel.put("msg",msg);
        }
    }

    /**
     * Shows registration page.
     *
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/register", method = RequestMethod.GET)
    @StopWatchStartAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showRegister(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        String result = null;

        if (null != userQueryService.getCurrentUser(request)
                || userMgmtService.tryLogInWithCookie(request, response)) {
//            return "redirect:" +(SpringUtil.getServerPath());

            return "redirect:/";
        }

//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//
//        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModel.put(Common.REFERRAL, "");

        boolean useInvitationLink = false;

        String referral = request.getParameter("r");
        if (!UserRegisterValidation.invalidUserName(referral)) {
            final JSONObject referralUser = userQueryService.getUserByName(referral);
            if (null != referralUser) {
                dataModel.put(Common.REFERRAL, referral);

                final Map<String, JSONObject> permissions =
                        roleQueryService.getUserPermissionsGrantMap(referralUser.optString(Keys.OBJECT_ID));
                final JSONObject useILPermission =
                        permissions.get(Permission.PERMISSION_ID_C_COMMON_USE_INVITATION_LINK);
                useInvitationLink = useILPermission.optBoolean(Permission.PERMISSION_T_GRANT);
            }
        }

        final String code = request.getParameter("code");
        if (Strings.isEmptyOrNull(code)) { // Register Step 1
//            renderer.setTemplateName("verify/register.ftl");
            result = "verify/register.ftl";
        } else { // Register Step 2
            final JSONObject verifycode = verifycodeQueryService.getVerifycode(code);
            if (null == verifycode) {
                dataModel.put(Keys.MSG, langPropsService.get("verifycodeExpiredLabel"));
//                renderer.setTemplateName("/error/custom.ftl");
                result = "/error/custom.ftl";
            } else {
//                renderer.setTemplateName("verify/register2.ftl");
                result = "verify/register2.ftl";

                final String userId = verifycode.optString(Verifycode.USER_ID);
                final JSONObject user = userQueryService.getUser(userId);
                dataModel.put(User.USER, user);

                if (UserExt.USER_STATUS_C_VALID == user.optInt(UserExt.USER_STATUS)
                        || UserExt.NULL_USER_NAME.equals(user.optString(User.USER_NAME))) {
                    dataModel.put(Keys.MSG, langPropsService.get("userExistLabel"));
//                    renderer.setTemplateName("/error/custom.ftl");
                    result = "/error/custom.ftl";
                } else {
                    referral = StringUtils.substringAfter(code, "r=");
                    if (!Strings.isEmptyOrNull(referral)) {
                        dataModel.put(Common.REFERRAL, referral);
                    }
                }
            }
        }

        final String allowRegister = optionQueryService.getAllowRegister();
        dataModel.put(Option.ID_C_MISC_ALLOW_REGISTER, allowRegister);
        if (useInvitationLink && "2".equals(allowRegister)) {
            dataModel.put(Option.ID_C_MISC_ALLOW_REGISTER, "1");
        }

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        return result;
    }

    /**
     * Register Step 1.
     *
     * @param request the specified request
     */
    @RequestMapping(value = "/register", method = RequestMethod.POST)
//    @Before(adviceClass = UserRegisterValidation.class)
    public void register(Map<String, Object> dataModel, final HttpServletRequest request) {

        dataModel.put(Keys.STATUS_CODE,false);
        try {
            UserRegisterValidation.doAdvice(request);
        } catch (RequestProcessAdviceException e) {
            dataModel.put(Keys.MSG, e.getJsonObject().get(Keys.MSG));
            return;
        }

        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);
        final String name = requestJSONObject.optString(User.USER_NAME);
        final String email = requestJSONObject.optString(User.USER_EMAIL);
        final String invitecode = requestJSONObject.optString(Invitecode.INVITECODE);
        final String referral = requestJSONObject.optString(Common.REFERRAL);

        final JSONObject user = new JSONObject();
        user.put(User.USER_NAME, name);
        user.put(User.USER_EMAIL, email);
        user.put(User.USER_PASSWORD, "");
        final Locale locale = Locales.getLocale();
        user.put(UserExt.USER_LANGUAGE, locale.getLanguage() + "_" + locale.getCountry());

        try {
            final String newUserId = userMgmtService.addUser(user);

            final JSONObject verifycode = new JSONObject();
            verifycode.put(Verifycode.BIZ_TYPE, Verifycode.BIZ_TYPE_C_REGISTER);
            String code = RandomStringUtils.randomAlphanumeric(6);
            if (!Strings.isEmptyOrNull(referral)) {
                code += "r=" + referral;
            }
            verifycode.put(Verifycode.CODE, code);
            verifycode.put(Verifycode.EXPIRED, DateUtils.addDays(new Date(), 1).getTime());
            verifycode.put(Verifycode.RECEIVER, email);
            verifycode.put(Verifycode.STATUS, Verifycode.STATUS_C_UNSENT);
            verifycode.put(Verifycode.TYPE, Verifycode.TYPE_C_EMAIL);
            verifycode.put(Verifycode.USER_ID, newUserId);
            verifycodeMgmtService.addVerifycode(verifycode);

            final String allowRegister = optionQueryService.getAllowRegister();
            if ("2".equals(allowRegister) && StringUtils.isNotBlank(invitecode)) {
                final JSONObject ic = invitecodeQueryService.getInvitecode(invitecode);
                ic.put(Invitecode.USER_ID, newUserId);
                ic.put(Invitecode.USE_TIME, System.currentTimeMillis());
                final String icId = ic.optString(Keys.OBJECT_ID);

                invitecodeMgmtService.updateInvitecode(icId, ic);
            }

//            context.renderTrueResult().renderMsg(langPropsService.get("verifycodeSentLabel"));
            dataModel.put(Keys.STATUS_CODE,true);
            dataModel.put(Keys.MSG, langPropsService.get("verifycodeSentLabel"));
        } catch (final Exception e) {
            final String msg = langPropsService.get("registerFailLabel") + " - " + e.getMessage();
            LOGGER.error( msg + "[name={0}, email={1}]", name, email);

            dataModel.put("msg",msg);
        }
    }

    /**
     * Register Step 2.
     *
     * @param request  the specified request
     * @param response the specified response
     */
    @RequestMapping(value = "/register2", method = RequestMethod.POST)
//    @Before(adviceClass = UserRegister2Validation.class)
    public void register2(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response) {
//        dataModel.put(Keys.STATUS_CODE,false);
        dataModel.put(Keys.STATUS_CODE, false);

        try {
            UserRegister2Validation.doAdvice(request);
        } catch (RequestProcessAdviceException e) {
            dataModel.put(Keys.MSG,e.getJsonObject().get(Keys.MSG));
            return;
        }

        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);

        final String password = requestJSONObject.optString(User.USER_PASSWORD); // Hashed
        final int appRole = requestJSONObject.optInt(UserExt.USER_APP_ROLE);
        final String referral = requestJSONObject.optString(Common.REFERRAL);
        final String userId = requestJSONObject.optString(Common.USER_ID);

        String name = null;
        String email = null;
        try {
            final JSONObject user = userQueryService.getUser(userId);
            if (null == user) {
//                dataModel.put(Keys.MSG ,langPropsService.get("registerFailLabel") + " - " + "User Not Found");
                dataModel.put(Keys.MSG, langPropsService.get("registerFailLabel") + " - " + "User Not Found");
                return;
            }

            name = user.optString(User.USER_NAME);
            email = user.optString(User.USER_EMAIL);

            user.put(UserExt.USER_APP_ROLE, appRole);
            user.put(User.USER_PASSWORD, password);
            user.put(UserExt.USER_STATUS, UserExt.USER_STATUS_C_VALID);

            userMgmtService.addUser(user);

            Sessions.login(request, response, user, false);

            final String ip = Requests.getRemoteAddr(request);
            userMgmtService.updateOnlineStatus(user.optString(Keys.OBJECT_ID), ip, true);

            if (!Strings.isEmptyOrNull(referral) && !UserRegisterValidation.invalidUserName(referral)) {
                final JSONObject referralUser = userQueryService.getUserByName(referral);
                if (null != referralUser) {
                    final String referralId = referralUser.optString(Keys.OBJECT_ID);
                    // Point
                    pointtransferMgmtService.transfer(Pointtransfer.ID_C_SYS, userId,
                            Pointtransfer.TRANSFER_TYPE_C_INVITED_REGISTER,
                            Pointtransfer.TRANSFER_SUM_C_INVITE_REGISTER, referralId, System.currentTimeMillis());
                    pointtransferMgmtService.transfer(Pointtransfer.ID_C_SYS, referralId,
                            Pointtransfer.TRANSFER_TYPE_C_INVITE_REGISTER,
                            Pointtransfer.TRANSFER_SUM_C_INVITE_REGISTER, userId, System.currentTimeMillis());

                    final JSONObject notification = new JSONObject();
                    notification.put(Notification.NOTIFICATION_USER_ID, referralId);
                    notification.put(Notification.NOTIFICATION_DATA_ID, userId);

                    notificationMgmtService.addInvitationLinkUsedNotification(notification);
                }
            }

            final JSONObject ic = invitecodeQueryService.getInvitecodeByUserId(userId);
            if (null != ic && Invitecode.STATUS_C_UNUSED == ic.optInt(Invitecode.STATUS)) {
                ic.put(Invitecode.STATUS, Invitecode.STATUS_C_USED);
                ic.put(Invitecode.USER_ID, userId);
                ic.put(Invitecode.USE_TIME, System.currentTimeMillis());
                final String icId = ic.optString(Keys.OBJECT_ID);

                invitecodeMgmtService.updateInvitecode(icId, ic);

                final String icGeneratorId = ic.optString(Invitecode.GENERATOR_ID);
                if (StringUtils.isNotBlank(icGeneratorId) && !Pointtransfer.ID_C_SYS.equals(icGeneratorId)) {
                    pointtransferMgmtService.transfer(Pointtransfer.ID_C_SYS, icGeneratorId,
                            Pointtransfer.TRANSFER_TYPE_C_INVITECODE_USED,
                            Pointtransfer.TRANSFER_SUM_C_INVITECODE_USED, userId, System.currentTimeMillis());

                    final JSONObject notification = new JSONObject();
                    notification.put(Notification.NOTIFICATION_USER_ID, icGeneratorId);
                    notification.put(Notification.NOTIFICATION_DATA_ID, userId);

                    notificationMgmtService.addInvitecodeUsedNotification(notification);
                }
            }

//            dataModel.put(Keys.STATUS_CODE,true);
            dataModel.put(Keys.STATUS_CODE, true);

            LOGGER.info( "Registered a user [name={0}, email={1}]", name, email);
        } catch (final Exception e) {
            final String msg = langPropsService.get("registerFailLabel") + " - " + e.getMessage();
            LOGGER.error( msg + "[name={0}, email={1}]", name, email);

            dataModel.put("msg",msg);
        }
    }

    /**
     * Logins user.
     *
     * @param request  the specified request
     * @param response the specified response
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public void login(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response) {
//        context.renderJSON().renderMsg(langPropsService.get("loginFailLabel"));
        dataModel.put(Keys.STATUS_CODE,false);
        dataModel.put(Keys.MSG, langPropsService.get("loginFailLabel"));

        JSONObject requestJSONObject;
        try {
            requestJSONObject = Requests.parseRequestJSONObject(request, response);
        } catch (final Exception e) {
//            dataModel.put(Keys.MSG ,langPropsService.get("paramsParseFailedLabel"));
            dataModel.put(Keys.MSG, langPropsService.get("paramsParseFailedLabel"));

            return;
        }

        final String nameOrEmail = requestJSONObject.optString("nameOrEmail");

        try {
            JSONObject user = userQueryService.getUserByName(nameOrEmail);
            if (null == user) {
                user = userQueryService.getUserByEmail(nameOrEmail);
            }

            if (null == user) {
//                dataModel.put(Keys.MSG ,langPropsService.get("notFoundUserLabel"));
                dataModel.put(Keys.MSG, langPropsService.get("notFoundUserLabel"));
                return;
            }

            if (UserExt.USER_STATUS_C_INVALID == user.optInt(UserExt.USER_STATUS)) {
                userMgmtService.updateOnlineStatus(user.optString(Keys.OBJECT_ID), "", false);
//                dataModel.put(Keys.MSG ,langPropsService.get("userBlockLabel"));
                dataModel.put(Keys.MSG, langPropsService.get("userBlockLabel"));
                return;
            }

            if (UserExt.USER_STATUS_C_NOT_VERIFIED == user.optInt(UserExt.USER_STATUS)) {
                userMgmtService.updateOnlineStatus(user.optString(Keys.OBJECT_ID), "", false);
//                dataModel.put(Keys.MSG ,langPropsService.get("notVerifiedLabel"));
                dataModel.put(Keys.MSG, langPropsService.get("notVerifiedLabel"));

                return;
            }

            if (UserExt.USER_STATUS_C_INVALID_LOGIN == user.optInt(UserExt.USER_STATUS)) {
                userMgmtService.updateOnlineStatus(user.optString(Keys.OBJECT_ID), "", false);
//                dataModel.put(Keys.MSG ,langPropsService.get("invalidLoginLabel"));
                dataModel.put(Keys.MSG, langPropsService.get("invalidLoginLabel"));

                return;
            }

            final String userId = user.optString(Keys.OBJECT_ID);
            JSONObject wrong = WRONG_PWD_TRIES.get(userId);
            if (null == wrong) {
                wrong = new JSONObject();
            }

            final int wrongCount = wrong.optInt(Common.WRON_COUNT);
            if (wrongCount > 3) {
                final String captcha = requestJSONObject.optString(CaptchaProcessor.CAPTCHA);
                if (!StringUtils.equals(wrong.optString(CaptchaProcessor.CAPTCHA), captcha)) {
//                    dataModel.put(Keys.MSG ,langPropsService.get("captchaErrorLabel"));
//                    dataModel.put(Common.NEED_CAPTCHA, userId);
                    dataModel.put(Keys.MSG, langPropsService.get("captchaErrorLabel"));
                    dataModel.put(Common.NEED_CAPTCHA, userId);

                    return;
                }
            }

            final String userPassword = user.optString(User.USER_PASSWORD);
            if (userPassword.equals(requestJSONObject.optString(User.USER_PASSWORD))) {
                final String token = Sessions.login(request, response, user, requestJSONObject.optBoolean(Common.REMEMBER_LOGIN));

                final String ip = Requests.getRemoteAddr(request);
                userMgmtService.updateOnlineStatus(user.optString(Keys.OBJECT_ID), ip, true);

//                dataModel.put(Keys.MSG ,"").renderTrueResult();
//                dataModel.put(Keys.TOKEN, token);
                dataModel.put(Keys.MSG, "");
                dataModel.put(Keys.TOKEN, token);

                WRONG_PWD_TRIES.remove(userId);

                return;
            }

            if (wrongCount > 2) {
//                dataModel.put(Common.NEED_CAPTCHA, userId);
                dataModel.put(Common.NEED_CAPTCHA, userId);
            }

            wrong.put(Common.WRON_COUNT, wrongCount + 1);
            WRONG_PWD_TRIES.put(userId, wrong);

//            dataModel.put(Keys.MSG ,langPropsService.get("wrongPwdLabel"));
            dataModel.put(Keys.MSG, langPropsService.get("wrongPwdLabel"));
        } catch (final Exception e) {
//            dataModel.put(Keys.MSG ,langPropsService.get("loginFailLabel"));
            dataModel.put(Keys.MSG, langPropsService.get("loginFailLabel"));
        }
    }

    /**
     * Logout.
     *
     * @throws IOException io exception
     */
    @RequestMapping(value = {"/logout"}, method = RequestMethod.GET)
    public String logout( final HttpServletRequest request, final HttpServletResponse response) throws IOException {
//        final HttpServletRequest httpServletRequest = context.getRequest();

        Sessions.logout(request, response);

        String destinationURL = request.getParameter(Common.GOTO);
        if (!StringUtils.startsWith(destinationURL, SpringUtil.getServerPath())) {
            destinationURL = "/";
        }

//        return "redirect:" +(destinationURL);
        return "redirect:" + destinationURL;
    }

    /**
     * Expires invitecodes.
     *
     * @param request  the specified HTTP servlet request
     * @param response the specified HTTP servlet response
     * @throws Exception exception
     */
    @RequestMapping(value = "/cron/invitecode-expire", method = RequestMethod.GET)
    @StopWatchStartAnno
    @StopWatchEndAnno
    public void expireInvitecodes(final HttpServletRequest request, final HttpServletResponse response, Map<String, Object> dataModel)
            throws Exception {
        final String key = Symphonys.get("keyOfSymphony");
        if (!key.equals(request.getParameter("key"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        invitecodeMgmtService.expireInvitecodes();

//        dataModel.put(Keys.STATUS_CODE,true);
        dataModel.put(Keys.STATUS_CODE,true);
    }
}
