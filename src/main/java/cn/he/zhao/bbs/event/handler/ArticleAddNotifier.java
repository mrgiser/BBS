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
package cn.he.zhao.bbs.event.handler;

import cn.he.zhao.bbs.event.AddArticleEvent;
import cn.he.zhao.bbs.entity.*;
import cn.he.zhao.bbs.entity.my.Keys;
import cn.he.zhao.bbs.entity.my.Pagination;
import cn.he.zhao.bbs.entity.my.User;
import cn.he.zhao.bbs.service.FollowQueryService;
import cn.he.zhao.bbs.service.NotificationMgmtService;
import cn.he.zhao.bbs.service.RoleQueryService;
import cn.he.zhao.bbs.service.UserQueryService;
import cn.he.zhao.bbs.util.Escapes;
import cn.he.zhao.bbs.util.Symphonys;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class ArticleAddNotifier implements ApplicationListener<AddArticleEvent> {

    public static final Logger LOGGER = LoggerFactory.getLogger(ArticleAddNotifier.class);

    /**
     * Notification management service.
     */
    @Autowired
    private NotificationMgmtService notificationMgmtService;

    /**
     * Follow query service.
     */
    @Autowired
    private FollowQueryService followQueryService;

    /**
     * User query service.
     */
    @Autowired
    private UserQueryService userQueryService;


    /**
     * Role query service.
     */
    @Autowired
    private RoleQueryService roleQueryService;


    @Override
    public void onApplicationEvent(AddArticleEvent event) {
        final JSONObject data = event.event;
        LOGGER.trace("Processing an event [type={0}, data={1}]", data);

        try {
            final JSONObject originalArticle = data.getJSONObject(Article.ARTICLE);
            final String articleId = originalArticle.optString(Keys.OBJECT_ID);
            final String articleAuthorId = originalArticle.optString(Article.ARTICLE_AUTHOR_ID);
            final JSONObject articleAuthor = userQueryService.getUser(articleAuthorId);
            final String articleAuthorName = articleAuthor.optString(User.USER_NAME);

            final Set<String> requisiteAtUserPermissions = new HashSet<>();
            requisiteAtUserPermissions.add(Permission.PERMISSION_ID_C_COMMON_AT_USER);
            final boolean hasAtUserPerm = roleQueryService.userHasPermissions(articleAuthorId, requisiteAtUserPermissions);
            final Set<String> atedUserIds = new HashSet<>();

            if (hasAtUserPerm) {
                // 'At' Notification
                final String articleContent = originalArticle.optString(Article.ARTICLE_CONTENT);
                final Set<String> atUserNames = userQueryService.getUserNames(articleContent);
                atUserNames.remove(articleAuthorName); // Do not notify the author itself

                for (final String userName : atUserNames) {
                    final JSONObject user = userQueryService.getUserByName(userName);

                    if (null == user) {
                        LOGGER.warn("Not found user by name [{0}]", userName);

                        continue;
                    }

                    final JSONObject requestJSONObject = new JSONObject();
                    final String atedUserId = user.optString(Keys.OBJECT_ID);
                    requestJSONObject.put(Notification.NOTIFICATION_USER_ID, atedUserId);
                    requestJSONObject.put(Notification.NOTIFICATION_DATA_ID, articleId);

                    notificationMgmtService.addAtNotification(requestJSONObject);

                    atedUserIds.add(atedUserId);
                }
            }

            final String tags = originalArticle.optString(Article.ARTICLE_TAGS);

            // 'following - user' Notification
            if (Article.ARTICLE_TYPE_C_DISCUSSION != originalArticle.optInt(Article.ARTICLE_TYPE)
                    && Article.ARTICLE_ANONYMOUS_C_PUBLIC == originalArticle.optInt(Article.ARTICLE_ANONYMOUS)
                    && !Tag.TAG_TITLE_C_SANDBOX.equals(tags)
                    && !StringUtils.containsIgnoreCase(tags, Symphonys.get("systemAnnounce"))) {
                final JSONObject followerUsersResult = followQueryService.getFollowerUsers(
                        UserExt.USER_AVATAR_VIEW_MODE_C_ORIGINAL, articleAuthorId, 1, Integer.MAX_VALUE);

                final List<JSONObject> followerUsers = (List<JSONObject>) followerUsersResult.opt(Keys.RESULTS);
                for (final JSONObject followerUser : followerUsers) {
                    final JSONObject requestJSONObject = new JSONObject();
                    final String followerUserId = followerUser.optString(Keys.OBJECT_ID);

                    if (atedUserIds.contains(followerUserId)) {
                        continue;
                    }

                    requestJSONObject.put(Notification.NOTIFICATION_USER_ID, followerUserId);
                    requestJSONObject.put(Notification.NOTIFICATION_DATA_ID, articleId);

                    notificationMgmtService.addFollowingUserNotification(requestJSONObject);
                }
            }

            final String articleTitle = Escapes.escapeHTML(originalArticle.optString(Article.ARTICLE_TITLE));

            // 'Broadcast' Notification
            if (Article.ARTICLE_TYPE_C_CITY_BROADCAST == originalArticle.optInt(Article.ARTICLE_TYPE)) {
                final String city = originalArticle.optString(Article.ARTICLE_CITY);

                if (StringUtils.isNotBlank(city)) {
                    final JSONObject requestJSONObject = new JSONObject();
                    requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, 1);
                    requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, Integer.MAX_VALUE);
                    requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, Integer.MAX_VALUE);

                    final long latestLoginTime = DateUtils.addDays(new Date(), -15).getTime();
                    requestJSONObject.put(UserExt.USER_LATEST_LOGIN_TIME, latestLoginTime);
                    requestJSONObject.put(UserExt.USER_CITY, city);

                    final JSONObject result = userQueryService.getUsersByCity(requestJSONObject);
                    final JSONArray users = result.optJSONArray(User.USERS);

                    for (int i = 0; i < users.length(); i++) {
                        final String userId = users.optJSONObject(i).optString(Keys.OBJECT_ID);

                        if (userId.equals(articleAuthorId)) {
                            continue;
                        }

                        final JSONObject notification = new JSONObject();
                        notification.put(Notification.NOTIFICATION_USER_ID, userId);
                        notification.put(Notification.NOTIFICATION_DATA_ID, articleId);

                        notificationMgmtService.addBroadcastNotification(notification);
                    }

                    LOGGER.info("City [" + city + "] broadcast [users=" + users.length() + "]");
                }
            }

            // 'Sys Announce' Notification
            if (StringUtils.containsIgnoreCase(tags, Symphonys.get("systemAnnounce"))) {
                final long latestLoginTime = DateUtils.addDays(new Date(), -15).getTime();

                final JSONObject result = userQueryService.getLatestLoggedInUsers(
                        latestLoginTime, 1, Integer.MAX_VALUE, Integer.MAX_VALUE);
                final JSONArray users = result.optJSONArray(User.USERS);

                for (int i = 0; i < users.length(); i++) {
                    final String userId = users.optJSONObject(i).optString(Keys.OBJECT_ID);

                    final JSONObject notification = new JSONObject();
                    notification.put(Notification.NOTIFICATION_USER_ID, userId);
                    notification.put(Notification.NOTIFICATION_DATA_ID, articleId);

                    notificationMgmtService.addSysAnnounceArticleNotification(notification);
                }

                LOGGER.info("System announcement [" + articleTitle + "] broadcast [users=" + users.length() + "]");
            }
        } catch (final Exception e) {
            LOGGER.error( "Sends the article add notification failed", e);
        }
    }
}
