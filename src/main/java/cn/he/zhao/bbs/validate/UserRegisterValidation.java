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
package cn.he.zhao.bbs.validate;

import cn.he.zhao.bbs.controller.CaptchaProcessor;
import cn.he.zhao.bbs.exception.RequestProcessAdviceException;
import cn.he.zhao.bbs.spring.Requests;
import cn.he.zhao.bbs.spring.SpringUtil;
import cn.he.zhao.bbs.spring.Strings;
import cn.he.zhao.bbs.entity.*;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.entityUtil.my.User;
import cn.he.zhao.bbs.service.InvitecodeQueryService;
import cn.he.zhao.bbs.service.OptionQueryService;
import cn.he.zhao.bbs.service.RoleQueryService;
import cn.he.zhao.bbs.service.UserQueryService;
import cn.he.zhao.bbs.service.interf.LangPropsService;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class UserRegisterValidation {

    /**
     * Max user name length.
     */
    public static final int MAX_USER_NAME_LENGTH = 20;
    /**
     * Min user name length.
     */
    public static final int MIN_USER_NAME_LENGTH = 1;
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UserRegisterValidation.class);
    /**
     * Max password length.
     * <p>
     * MD5 32
     * </p>
     */
    private static final int MAX_PWD_LENGTH = 32;
    /**
     * Min password length.
     */
    private static final int MIN_PWD_LENGTH = 1;
    /**
     * Captcha length.
     */
    private static final int CAPTCHA_LENGTH = 4;
    /**
     * Invitecode length.
     */
    private static final int INVITECODE_LENGHT = 16;
    /**
     * Language service.
     */
    @Autowired
    private static LangPropsService langPropsService;
    /**
     * Option query service.
     */
    @Autowired
    private static  OptionQueryService optionQueryService;
    /**
     * Invitecode query service.
     */
    @Autowired
    private static InvitecodeQueryService invitecodeQueryService;
    /**
     * User query service.
     */
    @Autowired
    private static UserQueryService userQueryService;
    /**
     * Role query servicce.
     */
    @Autowired
    private static RoleQueryService roleQueryService;


    /**
     * Checks whether the specified name is invalid.
     * <p>
     * A valid user name:
     * <ul>
     * <li>length [1, 20]</li>
     * <li>content {a-z, A-Z, 0-9}</li>
     * </ul>
     * </p>
     *
     * @param name the specified name
     * @return {@code true} if it is invalid, returns {@code false} otherwise
     */
    public static boolean invalidUserName(final String name) {
        if (StringUtils.isBlank(name)) {
            return true;
        }

        if (UserExt.isReservedUserName(name)) {
            return true;
        }

        final int length = name.length();
        if (length < MIN_USER_NAME_LENGTH || length > MAX_USER_NAME_LENGTH) {
            return true;
        }

        char c;
        for (int i = 0; i < length; i++) {
            c = name.charAt(i);

            if (('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || '0' <= c && c <= '9') {
                continue;
            }

            return true;
        }

        return false;
    }

    /**
     * Checks password, length [1, 16].
     *
     * @param password the specific password
     * @return {@code true} if it is invalid, returns {@code false} otherwise
     */
    public static boolean invalidUserPassword(final String password) {
        return password.length() < MIN_PWD_LENGTH || password.length() > MAX_PWD_LENGTH;
    }

    public static void doAdvice(final HttpServletRequest request ) throws RequestProcessAdviceException {

        JSONObject requestJSONObject;
        HttpServletResponse response = SpringUtil.getCurrentResponse();
        try {
            requestJSONObject = Requests.parseRequestJSONObject(request, response);
            request.setAttribute(Keys.REQUEST, requestJSONObject);
        } catch (final Exception e) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, e.getMessage()));
        }

        final String referral = requestJSONObject.optString(Common.REFERRAL);

        // check if admin allow to register
        final JSONObject option = optionQueryService.getOption(Option.ID_C_MISC_ALLOW_REGISTER);
        if ("1".equals(option.optString(Option.OPTION_VALUE))) {
            checkField(true, "registerFailLabel", "notAllowRegisterLabel");
        }

        boolean useInvitationLink = false;

        if (!UserRegisterValidation.invalidUserName(referral)) {
            try {
                final JSONObject referralUser = userQueryService.getUserByName(referral);
                if (null != referralUser) {

                    final Map<String, JSONObject> permissions =
                            roleQueryService.getUserPermissionsGrantMap(referralUser.optString(Keys.OBJECT_ID));
                    final JSONObject useILPermission =
                            permissions.get(Permission.PERMISSION_ID_C_COMMON_USE_INVITATION_LINK);
                    useInvitationLink = useILPermission.optBoolean(Permission.PERMISSION_T_GRANT);
                }
            } catch (final Exception e) {
                LOGGER.warn( "Query user [name=" + referral + "] failed", e);
            }
        }

        // invitecode register
        if (!useInvitationLink && "2".equals(option.optString(Option.OPTION_VALUE))) {
            final String invitecode = requestJSONObject.optString(Invitecode.INVITECODE);

            if (Strings.isEmptyOrNull(invitecode) || INVITECODE_LENGHT != invitecode.length()) {
                checkField(true, "registerFailLabel", "invalidInvitecodeLabel");
            }

            final JSONObject ic = invitecodeQueryService.getInvitecode(invitecode);
            if (null == ic) {
                checkField(true, "registerFailLabel", "invalidInvitecodeLabel");
            }

            if (Invitecode.STATUS_C_UNUSED != ic.optInt(Invitecode.STATUS)) {
                checkField(true, "registerFailLabel", "usedInvitecodeLabel");
            }
        }

        // open register
        if (useInvitationLink || "0".equals(option.optString(Option.OPTION_VALUE))) {
            final String captcha = requestJSONObject.optString(CaptchaProcessor.CAPTCHA);
            checkField(CaptchaProcessor.invalidCaptcha(captcha), "registerFailLabel", "captchaErrorLabel");
        }

        final String name = requestJSONObject.optString(User.USER_NAME);
        final String email = requestJSONObject.optString(User.USER_EMAIL);
        final int appRole = requestJSONObject.optInt(UserExt.USER_APP_ROLE);
        //final String password = requestJSONObject.optString(User.USER_PASSWORD);

        if (UserExt.isReservedUserName(name)) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("registerFailLabel")
                    + " - " + langPropsService.get("reservedUserNameLabel")));
        }

        checkField(invalidUserName(name), "registerFailLabel", "invalidUserNameLabel");
        checkField(!Strings.isEmail(email), "registerFailLabel", "invalidEmailLabel");
        checkField(UserExt.USER_APP_ROLE_C_HACKER != appRole
                && UserExt.USER_APP_ROLE_C_PAINTER != appRole, "registerFailLabel", "invalidAppRoleLabel");
        //checkField(invalidUserPassword(password), "registerFailLabel", "invalidPasswordLabel");
    }

    /**
     * Checks field.
     *
     * @param invalid    the specified invalid flag
     * @param failLabel  the specified fail label
     * @param fieldLabel the specified field label
     * @throws RequestProcessAdviceException request process advice exception
     */
    private static void checkField(final boolean invalid, final String failLabel, final String fieldLabel)
            throws RequestProcessAdviceException {
        if (invalid) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get(failLabel)
                    + " - " + langPropsService.get(fieldLabel)));
        }
    }
}
