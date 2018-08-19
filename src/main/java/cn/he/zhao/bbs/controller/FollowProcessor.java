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
import cn.he.zhao.bbs.model.*;
import cn.he.zhao.bbs.model.my.*;
import cn.he.zhao.bbs.service.*;
import cn.he.zhao.bbs.service.interf.LangPropsService;
import cn.he.zhao.bbs.spring.Requests;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Follow processor.
 * <ul>
 * <li>Follows a user (/follow/user), POST</li>
 * <li>Unfollows a user (/follow/user), DELETE</li>
 * <li>Follows a tag (/follow/tag), POST</li>
 * <li>Unfollows a tag (/follow/tag), DELETE</li>
 * <li>Follows an article (/follow/article), POST</li>
 * <li>Unfollows an article (/follow/article), DELETE</li>
 * <li>Watches an article (/follow/article-watch), POST</li>
 * <li>Unwatches an article (/follow/article-watch), DELETE</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.3.0.4, Mar 7, 2017
 * @since 0.2.5
 */
@Controller
public class FollowProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FollowProcessor.class);
    /**
     * Holds follows.
     */
    private static final Set<String> FOLLOWS = new HashSet<>();
    /**
     * Follow management service.
     */
    @Autowired
    private FollowMgmtService followMgmtService;
    /**
     * Notification management service.
     */
    @Autowired
    private NotificationMgmtService notificationMgmtService;
    /**
     * Article query service.
     */
    @Autowired
    private ArticleQueryService articleQueryService;

    /**
     * Follows a user.
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "followingId": ""
     * }
     * </pre>
     * </p>
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/follow/user", method = RequestMethod.POST)
//    @Before(adviceClass = LoginCheck.class)
    @LoginCheckAnno
    public void followUser(Map<String, Object> dataModel, final HttpServletRequest request,
                           final HttpServletResponse response) throws Exception {
        dataModel.put(Keys.STATUS_CODE,false);

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, response);
        final String followingUserId = requestJSONObject.optString(Follow.FOLLOWING_ID);

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String followerUserId = currentUser.optString(Keys.OBJECT_ID);

        followMgmtService.followUser(followerUserId, followingUserId);

        if (!FOLLOWS.contains(followingUserId + followerUserId)) {
            final JSONObject notification = new JSONObject();
            notification.put(Notification.NOTIFICATION_USER_ID, followingUserId);
            notification.put(Notification.NOTIFICATION_DATA_ID, followerUserId);

            notificationMgmtService.addNewFollowerNotification(notification);
        }

        FOLLOWS.add(followingUserId + followerUserId);

        dataModel.put(Keys.STATUS_CODE,true);
    }

    /**
     * Unfollows a user.
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "followingId": ""
     * }
     * </pre>
     * </p>
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/follow/user", method = RequestMethod.DELETE)
//    @Before(adviceClass = LoginCheck.class)
    @LoginCheckAnno
    public void unfollowUser(Map<String, Object> dataModel, final HttpServletRequest request,
                             final HttpServletResponse response) throws Exception {
        dataModel.put(Keys.STATUS_CODE,false);

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, response);
        final String followingUserId = requestJSONObject.optString(Follow.FOLLOWING_ID);

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String followerUserId = currentUser.optString(Keys.OBJECT_ID);

        followMgmtService.unfollowUser(followerUserId, followingUserId);

        dataModel.put(Keys.STATUS_CODE,true);
    }

    /**
     * Follows a tag.
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "followingId": ""
     * }
     * </pre>
     * </p>
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/follow/tag", method = RequestMethod.POST)
//    @Before(adviceClass = LoginCheck.class)
    @LoginCheckAnno
    public void followTag(Map<String, Object> dataModel, final HttpServletRequest request,
                          final HttpServletResponse response) throws Exception {
        dataModel.put(Keys.STATUS_CODE,false);

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, response);
        final String followingTagId = requestJSONObject.optString(Follow.FOLLOWING_ID);

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String followerUserId = currentUser.optString(Keys.OBJECT_ID);

        followMgmtService.followTag(followerUserId, followingTagId);

        dataModel.put(Keys.STATUS_CODE,true);
    }

    /**
     * Unfollows a tag.
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "followingId": ""
     * }
     * </pre>
     * </p>
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/follow/tag", method = RequestMethod.DELETE)
//    @Before(adviceClass = LoginCheck.class)
    @LoginCheckAnno
    public void unfollowTag(Map<String, Object> dataModel, final HttpServletRequest request,
                            final HttpServletResponse response) throws Exception {
        dataModel.put(Keys.STATUS_CODE,false);

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, response);
        final String followingTagId = requestJSONObject.optString(Follow.FOLLOWING_ID);

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String followerUserId = currentUser.optString(Keys.OBJECT_ID);

        followMgmtService.unfollowTag(followerUserId, followingTagId);

        dataModel.put(Keys.STATUS_CODE,true);
    }

    /**
     * Follows an article.
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "followingId": ""
     * }
     * </pre>
     * </p>
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/follow/article", method = RequestMethod.POST)
//    @Before(adviceClass = {LoginCheck.class, PermissionCheck.class})
    @LoginCheckAnno
    @PermissionGrantAnno
    public void followArticle(Map<String, Object> dataModel, final HttpServletRequest request,
                              final HttpServletResponse response) throws Exception {
        dataModel.put(Keys.STATUS_CODE,false);

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, response);
        final String followingArticleId = requestJSONObject.optString(Follow.FOLLOWING_ID);

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String followerUserId = currentUser.optString(Keys.OBJECT_ID);

        followMgmtService.followArticle(followerUserId, followingArticleId);

        final JSONObject article = articleQueryService.getArticle(followingArticleId);
        final String articleAuthorId = article.optString(Article.ARTICLE_AUTHOR_ID);

        if (!FOLLOWS.contains(articleAuthorId + followingArticleId + "-" + followerUserId) &&
                !articleAuthorId.equals(followerUserId)) {
            final JSONObject notification = new JSONObject();
            notification.put(Notification.NOTIFICATION_USER_ID, articleAuthorId);
            notification.put(Notification.NOTIFICATION_DATA_ID, followingArticleId + "-" + followerUserId);

            notificationMgmtService.addArticleNewFollowerNotification(notification);
        }

        FOLLOWS.add(articleAuthorId + followingArticleId + "-" + followerUserId);

        dataModel.put(Keys.STATUS_CODE,true);
    }

    /**
     * Unfollows an article.
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "followingId": ""
     * }
     * </pre>
     * </p>
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/follow/article", method = RequestMethod.DELETE)
//    @Before(adviceClass = LoginCheck.class)
    @LoginCheckAnno
    public void unfollowArticle(Map<String, Object> dataModel, final HttpServletRequest request,
                                final HttpServletResponse response) throws Exception {
        dataModel.put(Keys.STATUS_CODE,false);

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, response);
        final String followingArticleId = requestJSONObject.optString(Follow.FOLLOWING_ID);

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String followerUserId = currentUser.optString(Keys.OBJECT_ID);

        followMgmtService.unfollowArticle(followerUserId, followingArticleId);

        dataModel.put(Keys.STATUS_CODE,true);
    }

    /**
     * Watches an article.
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "followingId": ""
     * }
     * </pre>
     * </p>
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/follow/article-watch", method = RequestMethod.POST)
//    @Before(adviceClass = {LoginCheck.class, PermissionCheck.class})
    @LoginCheckAnno
    @PermissionGrantAnno
    public void watchArticle(Map<String, Object> dataModel, final HttpServletRequest request,
                             final HttpServletResponse response) throws Exception {
        dataModel.put(Keys.STATUS_CODE,false);

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, response);
        final String followingArticleId = requestJSONObject.optString(Follow.FOLLOWING_ID);

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String followerUserId = currentUser.optString(Keys.OBJECT_ID);

        followMgmtService.watchArticle(followerUserId, followingArticleId);

        final JSONObject article = articleQueryService.getArticle(followingArticleId);
        final String articleAuthorId = article.optString(Article.ARTICLE_AUTHOR_ID);

        if (!FOLLOWS.contains(articleAuthorId + followingArticleId + "-" + followerUserId) &&
                !articleAuthorId.equals(followerUserId)) {
            final JSONObject notification = new JSONObject();
            notification.put(Notification.NOTIFICATION_USER_ID, articleAuthorId);
            notification.put(Notification.NOTIFICATION_DATA_ID, followingArticleId + "-" + followerUserId);

            notificationMgmtService.addArticleNewWatcherNotification(notification);
        }

        FOLLOWS.add(articleAuthorId + followingArticleId + "-" + followerUserId);

        dataModel.put(Keys.STATUS_CODE,true);
    }

    /**
     * Unwatches an article.
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "followingId": ""
     * }
     * </pre>
     * </p>
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/follow/article-watch", method = RequestMethod.DELETE)
//    @Before(adviceClass = LoginCheck.class)
    @LoginCheckAnno
    public void unwatchArticle(Map<String, Object> dataModel, final HttpServletRequest request,
                               final HttpServletResponse response) throws Exception {
        dataModel.put(Keys.STATUS_CODE,false);

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, response);
        final String followingArticleId = requestJSONObject.optString(Follow.FOLLOWING_ID);

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String followerUserId = currentUser.optString(Keys.OBJECT_ID);

        followMgmtService.unwatchArticle(followerUserId, followingArticleId);

        dataModel.put(Keys.STATUS_CODE,true);
    }
}
