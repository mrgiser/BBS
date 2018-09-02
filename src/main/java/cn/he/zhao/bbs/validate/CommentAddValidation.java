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
import cn.he.zhao.bbs.spring.SpringUtil;
import cn.he.zhao.bbs.spring.Strings;
import cn.he.zhao.bbs.entity.Article;
import cn.he.zhao.bbs.entity.Comment;
import cn.he.zhao.bbs.entity.UserExt;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.service.ArticleQueryService;
import cn.he.zhao.bbs.service.CommentQueryService;
import cn.he.zhao.bbs.service.OptionQueryService;
import cn.he.zhao.bbs.service.interf.LangPropsService;
import cn.he.zhao.bbs.util.StatusCodes;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class CommentAddValidation {

    /**
     * Max comment content length.
     */
    public static final int MAX_COMMENT_CONTENT_LENGTH = 2000;
    /**
     * Language service.
     */
    @Autowired
    private static LangPropsService langPropsService;
    /**
     * Comment query service.
     */
    @Autowired
    private static CommentQueryService commentQueryService;
    /**
     * Article query service.
     */
    @Autowired
    private static ArticleQueryService articleQueryService;
    /**
     * OptionUtil query service.
     */
    @Autowired
    private static OptionQueryService optionQueryService;

    /**
     * Validates comment fields.
     *
     * @param requestJSONObject the specified request object
     * @throws RequestProcessAdviceException if validate failed
     */
    public static void validateCommentFields(final JSONObject requestJSONObject) throws RequestProcessAdviceException {

        final JSONObject exception = new JSONObject();
        exception.put(Keys.STATUS_CODE, StatusCodes.ERR);

        final String commentContent = StringUtils.trim(requestJSONObject.optString(Comment.COMMENT_CONTENT));
        if (Strings.isEmptyOrNull(commentContent) || commentContent.length() > MAX_COMMENT_CONTENT_LENGTH) {
            throw new RequestProcessAdviceException(exception.put(Keys.MSG, langPropsService.get("commentErrorLabel")));
        }

        if (optionQueryService.containReservedWord(commentContent)) {
            throw new RequestProcessAdviceException(exception.put(Keys.MSG, langPropsService.get("contentContainReservedWordLabel")));
        }

        try {
            final String articleId = requestJSONObject.optString(Article.ARTICLE_T_ID);
            if (Strings.isEmptyOrNull(articleId)) {
                throw new RequestProcessAdviceException(exception.put(Keys.MSG, langPropsService.get("commentArticleErrorLabel")));
            }

            final JSONObject article = articleQueryService.getArticleById(UserExt.USER_AVATAR_VIEW_MODE_C_ORIGINAL, articleId);
            if (null == article) {
                throw new RequestProcessAdviceException(exception.put(Keys.MSG, langPropsService.get("commentArticleErrorLabel")));
            }

            if (!article.optBoolean(Article.ARTICLE_COMMENTABLE)) {
                throw new RequestProcessAdviceException(exception.put(Keys.MSG, langPropsService.get("notAllowCmtLabel")));
            }

            final String originalCommentId = requestJSONObject.optString(Comment.COMMENT_ORIGINAL_COMMENT_ID);
            if (StringUtils.isNotBlank(originalCommentId)) {
                final JSONObject originalCmt = commentQueryService.getComment(originalCommentId);
                if (null == originalCmt) {
                    throw new RequestProcessAdviceException(exception.put(Keys.MSG, langPropsService.get("commentArticleErrorLabel")));
                }
            }
        } catch ( final Exception e) {
            throw new RequestProcessAdviceException(exception.put(Keys.MSG, "Unknown Error"));
        }
    }

    public static void doAdvice(final HttpServletRequest request) throws RequestProcessAdviceException {

        JSONObject requestJSONObject;
        try {
            HttpServletResponse response = SpringUtil.getCurrentResponse();
            requestJSONObject = Requests.parseRequestJSONObject(request, response);
            request.setAttribute(Keys.REQUEST, requestJSONObject);
        } catch (final Exception e) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, e.getMessage()).
                    put(Keys.STATUS_CODE, StatusCodes.ERR));
        }

        validateCommentFields(requestJSONObject);
    }
}
