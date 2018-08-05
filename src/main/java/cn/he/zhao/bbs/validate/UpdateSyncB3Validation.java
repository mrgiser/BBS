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

import cn.he.zhao.bbs.exception.RequestProcessAdviceException;
import cn.he.zhao.bbs.spring.Requests;
import cn.he.zhao.bbs.spring.Strings;
import cn.he.zhao.bbs.model.UserExt;
import cn.he.zhao.bbs.model.my.Keys;
import cn.he.zhao.bbs.service.interf.LangPropsService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class UpdateSyncB3Validation {

    /**
     * Language service.
     */
    @Autowired
    private LangPropsService langPropsService;

    /**
     * Max user B3 key length.
     */
    public static final int MAX_USER_B3_KEY_LENGTH = 20;

    /**
     * Max user B3 client interface URL length.
     */
    public static final int MAX_USER_B3_CLIENT_URL = 150;

    @Autowired
    private HttpServletResponse response;

    public void doAdvice(final HttpServletRequest request, final Map<String, Object> args) throws RequestProcessAdviceException {

        JSONObject requestJSONObject;
        try {
            requestJSONObject = Requests.parseRequestJSONObject(request, response);
            request.setAttribute(Keys.REQUEST, requestJSONObject);
        } catch (final Exception e) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, e.getMessage()));
        }

        final String b3Key = requestJSONObject.optString(UserExt.USER_B3_KEY);
        if (!Strings.isEmptyOrNull(b3Key) && b3Key.length() > MAX_USER_B3_KEY_LENGTH) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("invalidUserB3KeyLabel")));
        }

        final String clientAddArticleURL = requestJSONObject.optString(UserExt.USER_B3_CLIENT_ADD_ARTICLE_URL);
        if (!Strings.isEmptyOrNull(clientAddArticleURL) && clientAddArticleURL.length() > MAX_USER_B3_CLIENT_URL) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("invalidUserB3ClientURLLabel")));
        }

        final String clientAddCommentURL = requestJSONObject.optString(UserExt.USER_B3_CLIENT_ADD_COMMENT_URL);
        if (!Strings.isEmptyOrNull(clientAddCommentURL) && clientAddCommentURL.length() > MAX_USER_B3_CLIENT_URL) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("invalidUserB3ClientURLLabel")));
        }
    }
}
