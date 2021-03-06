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
import cn.he.zhao.bbs.entityUtil.*;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.entityUtil.my.User;
import cn.he.zhao.bbs.exception.RequestProcessAdviceException;
import cn.he.zhao.bbs.entity.*;
import cn.he.zhao.bbs.service.*;
import cn.he.zhao.bbs.service.interf.LangPropsService;
import cn.he.zhao.bbs.spring.Common;
import cn.he.zhao.bbs.spring.Requests;
import cn.he.zhao.bbs.util.*;
import cn.he.zhao.bbs.validate.CommentAddValidation;
import cn.he.zhao.bbs.validate.CommentUpdateValidation;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Comment processor.
 * <ul>
 * <li>Adds a comment (/comment) <em>locally</em>, POST</li>
 * <li>Updates a comment (/comment/{id}) <em>locally</em>, PUT</li>
 * <li>Gets a comment's content (/comment/{id}/content), GET</li>
 * <li>Thanks a comment (/comment/thank), POST</li>
 * <li>Gets a comment's replies (/comment/replies), GET </li>
 * <li>Gets a comment's revisions (/commment/{id}/revisions), GET</li>
 * <li>Removes a comment (/comment/{id}/remove), POST</li>
 * <li>Accepts a comment (/comment/accept), POST</li>
 * </ul>
 * <p>
 * The '<em>locally</em>' means user post a comment on Symphony directly rather than receiving a comment from externally
 * (for example Solo).
 * </p>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.8.0.0, Jun 11, 2018
 * @since 0.2.0
 */
@Controller
public class CommentProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CommentProcessor.class);

    /**
     * RevisionUtil query service.
     */
    @Autowired
    private RevisionQueryService revisionQueryService;

    /**
     * User query service.
     */
    @Autowired
    private UserQueryService userQueryService;

    /**
     * Comment management service.
     */
    @Autowired
    private CommentMgmtService commentMgmtService;

    /**
     * Comment query service.
     */
    @Autowired
    private CommentQueryService commentQueryService;

    /**
     * Article query service.
     */
    @Autowired
    private ArticleQueryService articleQueryService;

    /**
     * Language service.
     */
    @Autowired
    private LangPropsService langPropsService;

    /**
     * RewardUtil query service.
     */
    @Autowired
    private RewardQueryService rewardQueryService;

    /**
     * Short link query service.
     */
    @Autowired
    private ShortLinkQueryService shortLinkQueryService;

    /**
     * FollowUtil management service.
     */
    @Autowired
    private FollowMgmtService followMgmtService;

    /**
     * Accepts a comment.
     *
     * @param request           the specified request
     * @param requestJSONObject the specified request json object, for example,
     *                          {
     *                          "commentId": ""
     *                          }
     */
    @RequestMapping(value = "/comment/accept", method = RequestMethod.POST)
//    @Before(adviceClass = {LoginCheck.class, CSRFCheck.class, PermissionCheck.class})
    @LoginCheckAnno
    @CSRFCheckAnno
    @PermissionCheckAnno
    public void acceptComment(Map<String, Object> dataModel, final HttpServletRequest request, final JSONObject requestJSONObject) {
        dataModel.put(Keys.STATUS_CODE,false);

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String userId = currentUser.optString(Keys.OBJECT_ID);
        final String commentId = requestJSONObject.optString(CommentUtil.COMMENT_T_ID);

        try {
            final Comment comment = commentQueryService.getComment(commentId);
            if (null == comment) {
//                context.renderFalseResult().renderMsg("Not found comment to accept");
                dataModel.put(Keys.STATUS_CODE,false);
                dataModel.put(Keys.MSG, "Not found comment to accept");

                return;
            }
            final String commentAuthorId = comment.getCommentAuthorId();
            if (StringUtils.equals(userId, commentAuthorId)) {
//                context.renderFalseResult().renderMsg(langPropsService.get("thankSelfLabel"));

                dataModel.put(Keys.STATUS_CODE,false);
                dataModel.put(Keys.MSG, langPropsService.get("thankSelfLabel"));
                return;
            }

            final String articleId = comment.getCommentOnArticleId();
            final Article article = articleQueryService.getArticle(articleId);
            if (!StringUtils.equals(userId, article.getArticleAuthorId())) {
//                context.renderFalseResult().renderMsg(langPropsService.get("sc403Label"));

                dataModel.put(Keys.STATUS_CODE,false);
                dataModel.put(Keys.MSG, langPropsService.get("sc403Label"));
                return;
            }

            commentMgmtService.acceptComment(commentId);

            dataModel.put(Keys.STATUS_CODE,true);
        } catch ( final Exception e) {
            dataModel.put(Keys.MSG ,e.getMessage());
        }
    }

    /**
     * Removes a comment.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/comment/{id}/remove", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class, PermissionCheck.class})
//    @After(adviceClass = {StopwatchEndAdvice.class})
    @LoginCheckAnno
    @CSRFCheckAnno
    @PermissionCheckAnno
    @StopWatchEndAnno
    public void removeComment(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response,
                              final String id) throws Exception {
        if (StringUtils.isBlank(id)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String currentUserId = currentUser.optString(Keys.OBJECT_ID);
        final Comment comment = commentQueryService.getComment(id);
        if (null == comment) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        final String authorId = comment.getCommentAuthorId();
        if (!authorId.equals(currentUserId)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        dataModel.put(Keys.STATUS_CODE,false);
        try {
            commentMgmtService.removeComment(id);

            dataModel.put(Keys.STATUS_CODE, StatusCodes.SUCC);
            dataModel.put(CommentUtil.COMMENT_T_ID, id);
        } catch ( final Exception e) {
            final String msg = e.getMessage();

            dataModel.put("msg",msg);
            dataModel.put(Keys.STATUS_CODE, StatusCodes.ERR);
        }
    }

    /**
     * Gets a comment's revisions.
     *
     * @param id      the specified comment id
     */
    @RequestMapping(value = "/comment/{id}/revisions", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class, PermissionCheck.class})
//    @After(adviceClass = {StopwatchEndAdvice.class})
    @LoginCheckAnno
    @CSRFCheckAnno
    @PermissionCheckAnno
    @StopWatchEndAnno
    public void getCommentRevisions(Map<String, Object> dataModel, final String id) {
        final List<Revision> revisions = revisionQueryService.getCommentRevisions(id);
        final JSONObject ret = new JSONObject();
        dataModel.put(Keys.STATUS_CODE, true);
        dataModel.put(RevisionUtil.REVISIONS, (Object) revisions);

//        context.renderJSON(ret);
    }

    /**
     * Gets a comment's content.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws IOException io exception
     */
    @RequestMapping(value = "/comment/{id}/content", method = RequestMethod.GET)
//    @Before(adviceClass = {LoginCheck.class})
    @LoginCheckAnno
    public void getCommentContent(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response,
                                  final String id) throws IOException {
//        context.renderJSON().renderJSONValue(Keys.STATUS_CODE, StatusCodes.ERR);
        dataModel.put(Keys.STATUS_CODE, StatusCodes.ERR);

        try {
            final Comment comment = commentQueryService.getComment(id);
            if (null == comment) {
                LOGGER.warn("Not found comment [id=" + id + "] to update");

                return;
            }

            final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
            if (!currentUser.optString(Keys.OBJECT_ID).equals(comment.getCommentAuthorId())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);

                return;
            }

            dataModel.put(CommentUtil.COMMENT_CONTENT, comment.getCommentContent());
            dataModel.put(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch ( final Exception e) {
            dataModel.put(Keys.MSG ,e.getMessage());
        }
    }

    /**
     * Updates a comment locally.
     * <p>
     * The request json object:
     * <pre>
     * {
     *     "commentContent": ""
     * }
     * </pre>
     * </p>
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws IOException io exception
     */
    @RequestMapping(value = "/comment/{id}", method = RequestMethod.PUT)
//    @Before(adviceClass = {CSRFCheck.class, LoginCheck.class, CommentUpdateValidation.class, PermissionCheck.class})
    @CSRFCheckAnno
    @LoginCheckAnno
    @PermissionCheckAnno
    public void updateComment(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response,
                              final String id) throws IOException {
//        context.renderJSON().renderJSONValue(Keys.STATUS_CODE, StatusCodes.ERR);
        dataModel.put(Keys.STATUS_CODE, StatusCodes.ERR);

        try {
            CommentUpdateValidation.doAdvice(request);
        } catch (RequestProcessAdviceException e) {
            dataModel.put(Keys.MSG,e.getJsonObject().get(Keys.MSG));
            dataModel.put(Keys.STATUS_CODE, e.getJsonObject().get(Keys.STATUS_CODE));
        }

        try {
            final Comment comment = commentQueryService.getComment(id);
            if (null == comment) {
                LOGGER.warn("Not found comment [id=" + id + "] to update");

                return;
            }

            final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
            if (!currentUser.optString(Keys.OBJECT_ID).equals(comment.getCommentAuthorId())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);

                return;
            }

            final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);

            String commentContent = requestJSONObject.optString(CommentUtil.COMMENT_CONTENT);
            final String ip = Requests.getRemoteAddr(request);
            final String ua = Headers.getHeader(request, Common.USER_AGENT);

            comment.setCommentContent(commentContent);
            comment.setCommentIP( "");
            if (StringUtils.isNotBlank(ip)) {
                comment.setCommentIP( ip);
            }
            comment.setCommentUA("");
            if (StringUtils.isNotBlank(ua)) {
                comment.setCommentUA( ua);
            }

            JSONObject jsonObject = new JSONObject(JsonUtil.objectToJson(comment));
            commentMgmtService.updateComment(comment.getOid(), jsonObject);

            commentContent = jsonObject.optString(CommentUtil.COMMENT_CONTENT);
            commentContent = shortLinkQueryService.linkArticle(commentContent);
            commentContent = shortLinkQueryService.linkTag(commentContent);
            commentContent = Emotions.toAliases(commentContent);
            commentContent = Emotions.convert(commentContent);
            commentContent = Markdowns.toHTML(commentContent);
            commentContent = Markdowns.clean(commentContent, "");
            commentContent = MP3Players.render(commentContent);
            commentContent = VideoPlayers.render(commentContent);

            dataModel.put(Keys.STATUS_CODE, StatusCodes.SUCC);
            dataModel.put(CommentUtil.COMMENT_CONTENT, commentContent);
        } catch ( final Exception e) {
            dataModel.put(Keys.MSG ,e.getMessage());
        }
    }

    /**
     * Gets a comment's original comment.
     *
     * @param request the specified request
     * @throws Exception exception
     */
    @RequestMapping(value = "/comment/original", method = RequestMethod.POST)
    public void getOriginalComment(Map<String, Object> dataModel, final HttpServletRequest request,final HttpServletResponse response) throws Exception {
        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, response);
        final String commentId = requestJSONObject.optString(CommentUtil.COMMENT_T_ID);
        int commentViewMode = requestJSONObject.optInt(UserExtUtil.USER_COMMENT_VIEW_MODE);
        int avatarViewMode = UserExtUtil.USER_AVATAR_VIEW_MODE_C_ORIGINAL;
        final UserExt currentUser = userQueryService.getCurrentUser(request);
        if (null != currentUser) {
            avatarViewMode = currentUser.getUserAvatarViewMode();
        }

        final JSONObject originalCmt = commentQueryService.getOriginalComment(avatarViewMode, commentViewMode, commentId);

        // Fill thank
        final String originalCmtId = originalCmt.optString(Keys.OBJECT_ID);

        if (null != currentUser) {
            originalCmt.put(Common.REWARDED,
                    rewardQueryService.isRewarded(currentUser.getOid(),
                            originalCmtId, RewardUtil.TYPE_C_COMMENT));
        }

        originalCmt.put(Common.REWARED_COUNT, rewardQueryService.rewardedCount(originalCmtId, RewardUtil.TYPE_C_COMMENT));

//        context.renderJSON(true).renderJSONValue(Comment.COMMENT_T_REPLIES, (Object) originalCmt);
        dataModel.put(Keys.STATUS_CODE,true);
        dataModel.put(CommentUtil.COMMENT_T_REPLIES, (Object) originalCmt);
    }

    /**
     * Gets a comment's replies.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/comment/replies", method = RequestMethod.POST)
    public void getReplies(Map<String, Object> dataModel,
                           final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, response);
        final String commentId = requestJSONObject.optString(CommentUtil.COMMENT_T_ID);
        int commentViewMode = requestJSONObject.optInt(UserExtUtil.USER_COMMENT_VIEW_MODE);
        int avatarViewMode = UserExtUtil.USER_AVATAR_VIEW_MODE_C_ORIGINAL;
        final UserExt currentUser = userQueryService.getCurrentUser(request);
        if (null != currentUser) {
            avatarViewMode = currentUser.getUserAvatarViewMode();
        }

        if (StringUtils.isBlank(commentId)) {
//            context.renderJSON(true).renderJSONValue(Comment.COMMENT_T_REPLIES, (Object) Collections.emptyList());
            dataModel.put(Keys.STATUS_CODE,true);
            dataModel.put(CommentUtil.COMMENT_T_REPLIES, (Object) Collections.emptyList());
            return;
        }

        final List<JSONObject> replies = commentQueryService.getReplies(avatarViewMode, commentViewMode, commentId);

        // Fill reply thank
        for (final JSONObject reply : replies) {
            final String replyId = reply.optString(Keys.OBJECT_ID);

            if (null != currentUser) {
                reply.put(Common.REWARDED,
                        rewardQueryService.isRewarded(currentUser.getOid(),
                                replyId, RewardUtil.TYPE_C_COMMENT));
            }

            reply.put(Common.REWARED_COUNT, rewardQueryService.rewardedCount(replyId, RewardUtil.TYPE_C_COMMENT));
        }

//        context.renderJSON(true).renderJSONValue(Comment.COMMENT_T_REPLIES, (Object) replies);
        dataModel.put(Keys.STATUS_CODE,true);
        dataModel.put(CommentUtil.COMMENT_T_REPLIES, (Object) replies);
    }

    /**
     * Adds a comment locally.
     * <p>
     * The request json object (a comment):
     * <pre>
     * {
     *     "articleId": "",
     *     "commentContent": "",
     *     "commentAnonymous": boolean,
     *     "commentOriginalCommentId": "", // optional
     *     "userCommentViewMode": int
     * }
     * </pre>
     * </p>
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws IOException      io exception
     * @throws ServletException servlet exception
     */
    @RequestMapping(value = "/comment", method = RequestMethod.POST)
//    @Before(adviceClass = {CSRFCheck.class, LoginCheck.class, CommentAddValidation.class, PermissionCheck.class})
    @CSRFCheckAnno
    @LoginCheckAnno
    @PermissionCheckAnno
    public void addComment(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws IOException, ServletException {
//        context.renderJSON().renderJSONValue(Keys.STATUS_CODE, StatusCodes.ERR);
        dataModel.put(Keys.STATUS_CODE,StatusCodes.ERR);

        try {
            CommentAddValidation.doAdvice(request);
        } catch (RequestProcessAdviceException e) {
            dataModel.put(Keys.STATUS_CODE,e.getJsonObject().get(Keys.STATUS_CODE));
            dataModel.put(Keys.MSG,e.getJsonObject().get(Keys.MSG));
        }

        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);

        final String articleId = requestJSONObject.optString(ArticleUtil.ARTICLE_T_ID);
        final String commentContent = requestJSONObject.optString(CommentUtil.COMMENT_CONTENT);
        final String commentOriginalCommentId = requestJSONObject.optString(CommentUtil.COMMENT_ORIGINAL_COMMENT_ID);
        final int commentViewMode = requestJSONObject.optInt(UserExtUtil.USER_COMMENT_VIEW_MODE);
        final String ip = Requests.getRemoteAddr(request);
        final String ua = Headers.getHeader(request, Common.USER_AGENT);

        final boolean isAnonymous = requestJSONObject.optBoolean(CommentUtil.COMMENT_ANONYMOUS, false);

        final JSONObject comment = new JSONObject();
        comment.put(CommentUtil.COMMENT_CONTENT, commentContent);
        comment.put(CommentUtil.COMMENT_ON_ARTICLE_ID, articleId);
        comment.put(UserExtUtil.USER_COMMENT_VIEW_MODE, commentViewMode);
        comment.put(CommentUtil.COMMENT_IP, "");
        if (StringUtils.isNotBlank(ip)) {
            comment.put(CommentUtil.COMMENT_IP, ip);
        }
        comment.put(CommentUtil.COMMENT_UA, "");
        if (StringUtils.isNotBlank(ua)) {
            comment.put(CommentUtil.COMMENT_UA, ua);
        }
        comment.put(CommentUtil.COMMENT_ORIGINAL_COMMENT_ID, commentOriginalCommentId);

        try {
            final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
            final String currentUserName = currentUser.optString(User.USER_NAME);
            final Article article = articleQueryService.getArticle(articleId);
            final String articleContent = article.getArticleContent();
            final String articleAuthorId = article.getArticleAuthorId();
            final UserExt articleAuthor = userQueryService.getUser(articleAuthorId);
            final String articleAuthorName = articleAuthor.getUserName();

            final Set<String> userNames = userQueryService.getUserNames(articleContent);
            if (ArticleUtil.ARTICLE_TYPE_C_DISCUSSION == article.getArticleType()
                    && !articleAuthorName.equals(currentUserName)) {
                boolean invited = false;
                for (final String userName : userNames) {
                    if (userName.equals(currentUserName)) {
                        invited = true;

                        break;
                    }
                }

                if (!invited) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);

                    return;
                }
            }

            final String commentAuthorId = currentUser.optString(Keys.OBJECT_ID);
            comment.put(CommentUtil.COMMENT_AUTHOR_ID, commentAuthorId);
            comment.put(CommentUtil.COMMENT_ANONYMOUS, isAnonymous
                    ? CommentUtil.COMMENT_ANONYMOUS_C_ANONYMOUS : CommentUtil.COMMENT_ANONYMOUS_C_PUBLIC);

            commentMgmtService.addComment(comment);

            if (!commentAuthorId.equals(articleAuthorId) &&
                    UserExtUtil.USER_XXX_STATUS_C_ENABLED == currentUser.optInt(UserExtUtil.USER_REPLY_WATCH_ARTICLE_STATUS)) {
                followMgmtService.watchArticle(commentAuthorId, articleId);
            }

            dataModel.put(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch ( final Exception e) {
            dataModel.put(Keys.MSG ,e.getMessage());
        }
    }

    /**
     * Thanks a comment.
     *
     * @param request           the specified request
     * @param requestJSONObject the specified request json object, for example,
     *                          {
     *                          "commentId": ""
     *                          }
     */
    @RequestMapping(value = "/comment/thank", method = RequestMethod.POST)
//    @Before(adviceClass = {LoginCheck.class, CSRFCheck.class, PermissionCheck.class})
    @CSRFCheckAnno
    @LoginCheckAnno
    @PermissionCheckAnno
    public void thankComment(Map<String, Object> dataModel, final HttpServletRequest request, final JSONObject requestJSONObject) {
        dataModel.put(Keys.STATUS_CODE,false);

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String commentId = requestJSONObject.optString(CommentUtil.COMMENT_T_ID);

        try {
            commentMgmtService.thankComment(commentId, currentUser.optString(Keys.OBJECT_ID));

//            context.renderTrueResult().renderMsg(langPropsService.get("thankSentLabel"));
            dataModel.put(Keys.STATUS_CODE,true);
            dataModel.put(Keys.MSG,langPropsService.get("thankSentLabel"));
        } catch ( final Exception e) {
            dataModel.put(Keys.MSG ,e.getMessage());
        }
    }
}
