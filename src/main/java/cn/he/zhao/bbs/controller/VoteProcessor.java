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
import cn.he.zhao.bbs.entity.*;
import cn.he.zhao.bbs.entityUtil.ArticleUtil;
import cn.he.zhao.bbs.entityUtil.NotificationUtil;
import cn.he.zhao.bbs.entityUtil.RoleUtil;
import cn.he.zhao.bbs.entityUtil.VoteUtil;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.entityUtil.my.User;
import cn.he.zhao.bbs.service.*;
import cn.he.zhao.bbs.service.interf.LangPropsService;
import cn.he.zhao.bbs.spring.Common;
import cn.he.zhao.bbs.spring.Requests;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Vote processor.
 * <ul>
 * <li>Votes up an article (/vote/up/article), POST</li>
 * <li>Votes down an article (/vote/down/article), POST</li>
 * <li>Votes up a comment (/vote/up/comment), POST</li>
 * <li>Votes down a comment (/vote/down/comment), POST</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.3.0.6, Jun 27, 2018
 * @since 1.3.0
 */
@Controller
public class VoteProcessor {

    /**
     * Holds votes.
     */
    private static final Set<String> VOTES = new HashSet<>();

    /**
     * Vote management service.
     */
    @Autowired
    private VoteMgmtService voteMgmtService;

    /**
     * Vote query service.
     */
    @Autowired
    private VoteQueryService voteQueryService;

    /**
     * Language service.
     */
    @Autowired
    private LangPropsService langPropsService;

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
     * NotificationUtil management service.
     */
    @Autowired
    private NotificationMgmtService notificationMgmtService;

    /**
     * Votes up a comment.
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "dataId": ""
     * }
     * </pre>
     * </p>
     *

     * @param request  the specified request
     * @throws Exception exception
     */
    @RequestMapping(value = "/vote/up/comment", method = RequestMethod.POST)
//    @Before(adviceClass = {LoginCheck.class, PermissionCheck.class})
    @LoginCheckAnno
    @PermissionCheckAnno
    public void voteUpComment(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        dataModel.put(Keys.STATUS_CODE,false);

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, response);
        final String dataId = requestJSONObject.optString(Common.DATA_ID);

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String userId = currentUser.optString(Keys.OBJECT_ID);

        if (!RoleUtil.ROLE_ID_C_ADMIN.equals(currentUser.optString(User.USER_ROLE))
                && voteQueryService.isOwn(userId, dataId, VoteUtil.DATA_TYPE_C_COMMENT)) {
//            context.renderFalseResult().renderMsg(langPropsService.get("cantVoteSelfLabel"));
            dataModel.put(Keys.STATUS_CODE,false);
            dataModel.put(Keys.MSG,langPropsService.get("cantVoteSelfLabel"));

            return;
        }

        final int vote = voteQueryService.isVoted(userId, dataId);
        if (VoteUtil.TYPE_C_UP == vote) {
            voteMgmtService.voteCancel(userId, dataId, VoteUtil.DATA_TYPE_C_COMMENT);
        } else {
            voteMgmtService.voteUp(userId, dataId, VoteUtil.DATA_TYPE_C_COMMENT);

            final Comment comment = commentQueryService.getComment(dataId);
            final String commenterId = comment.getCommentAuthorId();

            if (!VOTES.contains(userId + dataId) && !userId.equals(commenterId)) {
                final JSONObject notification = new JSONObject();
                notification.put(NotificationUtil.NOTIFICATION_USER_ID, commenterId);
                notification.put(NotificationUtil.NOTIFICATION_DATA_ID, dataId + "-" + userId);

                notificationMgmtService.addCommentVoteUpNotification(notification);
            }

            VOTES.add(userId + dataId);
        }

//        context.renderTrueResult().renderJSONValue(Vote.TYPE, vote);
        dataModel.put(Keys.STATUS_CODE,true);
        dataModel.put(VoteUtil.TYPE, vote);
    }

    /**
     * Votes down a comment.
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "dataId": ""
     * }
     * </pre>
     * </p>
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/vote/down/comment", method = RequestMethod.POST)
//    @Before(adviceClass = {LoginCheck.class, PermissionCheck.class})
    @LoginCheckAnno
    @PermissionCheckAnno
    public void voteDownComment(Map<String, Object> dataModel, final HttpServletRequest request,
                                final HttpServletResponse response) throws Exception {
        dataModel.put(Keys.STATUS_CODE,false);

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, response);
        final String dataId = requestJSONObject.optString(Common.DATA_ID);

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String userId = currentUser.optString(Keys.OBJECT_ID);

        if (!RoleUtil.ROLE_ID_C_ADMIN.equals(currentUser.optString(User.USER_ROLE))
                && voteQueryService.isOwn(userId, dataId, VoteUtil.DATA_TYPE_C_COMMENT)) {
//            context.renderFalseResult().renderMsg(langPropsService.get("cantVoteSelfLabel"));
            dataModel.put(Keys.STATUS_CODE,false);
            dataModel.put(Keys.MSG, langPropsService.get("cantVoteSelfLabel"));

            return;
        }

        final int vote = voteQueryService.isVoted(userId, dataId);
        if (VoteUtil.TYPE_C_DOWN == vote) {
            voteMgmtService.voteCancel(userId, dataId, VoteUtil.DATA_TYPE_C_COMMENT);
        } else {
            voteMgmtService.voteDown(userId, dataId, VoteUtil.DATA_TYPE_C_COMMENT);

            // https://github.com/b3log/symphony/issues/611
//            final JSONObject comment = commentQueryService.getComment(dataId);
//            final String commenterId = comment.optString(Comment.COMMENT_AUTHOR_ID);
//
//            if (!VOTES.contains(userId + dataId) && !userId.equals(commenterId)) {
//                final JSONObject notification = new JSONObject();
//                notification.put(NotificationUtil.NOTIFICATION_USER_ID, commenterId);
//                notification.put(NotificationUtil.NOTIFICATION_DATA_ID, dataId + "-" + userId);
//
//                notificationMgmtService.addCommentVoteDownNotification(notification);
//            }
//
//            VOTES.add(userId + dataId);
        }

//        context.renderTrueResult().renderJSONValue(Vote.TYPE, vote);
        dataModel.put(Keys.STATUS_CODE,true);
        dataModel.put(VoteUtil.TYPE, vote);
    }

    /**
     * Votes up an article.
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "dataId": ""
     * }
     * </pre>
     * </p>
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/vote/up/article", method = RequestMethod.POST)
//    @Before(adviceClass = {LoginCheck.class, PermissionCheck.class})
    @LoginCheckAnno
    @PermissionCheckAnno
    public void voteUpArticle(Map<String, Object> dataModel, final HttpServletRequest request,
                              final HttpServletResponse response) throws Exception {
        dataModel.put(Keys.STATUS_CODE,false);

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, response);
        final String dataId = requestJSONObject.optString(Common.DATA_ID);

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String userId = currentUser.optString(Keys.OBJECT_ID);

        if (!RoleUtil.ROLE_ID_C_ADMIN.equals(currentUser.optString(User.USER_ROLE))
                && voteQueryService.isOwn(userId, dataId, VoteUtil.DATA_TYPE_C_ARTICLE)) {
//            context.renderFalseResult().renderMsg(langPropsService.get("cantVoteSelfLabel"));

            dataModel.put(Keys.STATUS_CODE,false);
            dataModel.put(Keys.MSG, langPropsService.get("cantVoteSelfLabel"));
            return;
        }

        final int vote = voteQueryService.isVoted(userId, dataId);
        if (VoteUtil.TYPE_C_UP == vote) {
            voteMgmtService.voteCancel(userId, dataId, VoteUtil.DATA_TYPE_C_ARTICLE);
        } else {
            voteMgmtService.voteUp(userId, dataId, VoteUtil.DATA_TYPE_C_ARTICLE);

            final Article article = articleQueryService.getArticle(dataId);
            final String articleAuthorId = article.getArticleAuthorId();

            if (!VOTES.contains(userId + dataId) && !userId.equals(articleAuthorId)) {
                final JSONObject notification = new JSONObject();
                notification.put(NotificationUtil.NOTIFICATION_USER_ID, articleAuthorId);
                notification.put(NotificationUtil.NOTIFICATION_DATA_ID, dataId + "-" + userId);

                notificationMgmtService.addArticleVoteUpNotification(notification);
            }

            VOTES.add(userId + dataId);
        }

//        context.renderTrueResult().renderJSONValue(Vote.TYPE, vote);
        dataModel.put(Keys.STATUS_CODE,true);
        dataModel.put(VoteUtil.TYPE, vote);
    }

    /**
     * Votes down an article.
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "dataId": ""
     * }
     * </pre>
     * </p>
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/vote/down/article", method = RequestMethod.POST)
//    @Before(adviceClass = {LoginCheck.class, PermissionCheck.class})
    @LoginCheckAnno
    @PermissionCheckAnno
    public void voteDownArticle(Map<String, Object> dataModel, final HttpServletRequest request,
                                final HttpServletResponse response) throws Exception {
        dataModel.put(Keys.STATUS_CODE,false);

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, response);
        final String dataId = requestJSONObject.optString(Common.DATA_ID);

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String userId = currentUser.optString(Keys.OBJECT_ID);

        if (!RoleUtil.ROLE_ID_C_ADMIN.equals(currentUser.optString(User.USER_ROLE))
                && voteQueryService.isOwn(userId, dataId, VoteUtil.DATA_TYPE_C_ARTICLE)) {
//            context.renderFalseResult().renderMsg(langPropsService.get("cantVoteSelfLabel"));

            dataModel.put(Keys.STATUS_CODE,false);
            dataModel.put(Keys.MSG, langPropsService.get("cantVoteSelfLabel"));
            return;
        }

        final int vote = voteQueryService.isVoted(userId, dataId);
        if (VoteUtil.TYPE_C_DOWN == vote) {
            voteMgmtService.voteCancel(userId, dataId, VoteUtil.DATA_TYPE_C_ARTICLE);
        } else {
            voteMgmtService.voteDown(userId, dataId, VoteUtil.DATA_TYPE_C_ARTICLE);

            // https://github.com/b3log/symphony/issues/611
//            final JSONObject article = articleQueryService.getArticle(dataId);
//            final String articleAuthorId = article.optString(Article.ARTICLE_AUTHOR_ID);
//
//            if (!VOTES.contains(userId + dataId) && !userId.equals(articleAuthorId)) {
//                final JSONObject notification = new JSONObject();
//                notification.put(NotificationUtil.NOTIFICATION_USER_ID, articleAuthorId);
//                notification.put(NotificationUtil.NOTIFICATION_DATA_ID, dataId + "-" + userId);
//
//                notificationMgmtService.addArticleVoteDownNotification(notification);
//            }
//
//            VOTES.add(userId + dataId);
        }

//        context.renderTrueResult().renderJSONValue(Vote.TYPE, vote);
        dataModel.put(Keys.STATUS_CODE,true);
        dataModel.put(VoteUtil.TYPE, vote);
    }
}
