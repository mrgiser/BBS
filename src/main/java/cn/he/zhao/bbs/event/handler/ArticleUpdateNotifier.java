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

import cn.he.zhao.bbs.event.UpdateArticleEvent;
import cn.he.zhao.bbs.entity.Article;
import cn.he.zhao.bbs.entity.Notification;
import cn.he.zhao.bbs.entity.UserExt;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.entityUtil.my.User;
import cn.he.zhao.bbs.service.FollowQueryService;
import cn.he.zhao.bbs.service.NotificationMgmtService;
import cn.he.zhao.bbs.service.UserQueryService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class ArticleUpdateNotifier implements ApplicationListener<UpdateArticleEvent> {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(ArticleUpdateNotifier.class);

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

    @Override
    public void onApplicationEvent(UpdateArticleEvent event) {
        final JSONObject data = event.event;
        LOGGER.trace("Processing an event [type={0}, data={1}]", data);

        try {
            final JSONObject originalArticle = data.getJSONObject(Article.ARTICLE);
            final String articleId = originalArticle.optString(Keys.OBJECT_ID);

            final String articleAuthorId = originalArticle.optString(Article.ARTICLE_AUTHOR_ID);
            final JSONObject articleAuthor = userQueryService.getUser(articleAuthorId);
            final String articleAuthorName = articleAuthor.optString(User.USER_NAME);
            final boolean isDiscussion = originalArticle.optInt(Article.ARTICLE_TYPE) == Article.ARTICLE_TYPE_C_DISCUSSION;

            final String articleContent = originalArticle.optString(Article.ARTICLE_CONTENT);
            final Set<String> atUserNames = userQueryService.getUserNames(articleContent);
            atUserNames.remove(articleAuthorName); // Do not notify the author itself

            final String tags = originalArticle.optString(Article.ARTICLE_TAGS);

            // 'following - article update' Notification
            final JSONObject followerUsersResult =
                    followQueryService.getArticleWatchers(UserExt.USER_AVATAR_VIEW_MODE_C_ORIGINAL,
                            articleId, 1, Integer.MAX_VALUE);

            final List<JSONObject> watcherUsers = (List<JSONObject>) followerUsersResult.opt(Keys.RESULTS);
            for (final JSONObject watcherUser : watcherUsers) {
                final String watcherName = watcherUser.optString(User.USER_NAME);
                if ((isDiscussion && !atUserNames.contains(watcherName)) || articleAuthorName.equals(watcherName)) {
                    continue;
                }

                final JSONObject requestJSONObject = new JSONObject();
                final String watcherUserId = watcherUser.optString(Keys.OBJECT_ID);

                requestJSONObject.put(Notification.NOTIFICATION_USER_ID, watcherUserId);
                requestJSONObject.put(Notification.NOTIFICATION_DATA_ID, articleId);

                notificationMgmtService.addFollowingArticleUpdateNotification(requestJSONObject);
            }
        } catch (final Exception e) {
            LOGGER.error( "Sends the article update notification failed", e);
        }
    }
}
