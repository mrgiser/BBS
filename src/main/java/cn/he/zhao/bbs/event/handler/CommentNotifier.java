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

import cn.he.zhao.bbs.channel.ArticleChannel;
import cn.he.zhao.bbs.channel.ArticleListChannel;
import cn.he.zhao.bbs.event.AddCommentEvent;
import cn.he.zhao.bbs.mapper.CommentMapper;
import cn.he.zhao.bbs.mapper.UserMapper;
import cn.he.zhao.bbs.entity.*;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.entityUtil.my.Pagination;
import cn.he.zhao.bbs.entityUtil.my.User;
import cn.he.zhao.bbs.service.*;
import cn.he.zhao.bbs.service.interf.LangPropsService;
import cn.he.zhao.bbs.util.*;
import cn.he.zhao.bbs.validate.UserRegisterValidation;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class CommentNotifier implements ApplicationListener<AddCommentEvent> {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(CommentNotifier.class);

    /**
     * Comment Mapper.
     */
    @Autowired
    private CommentMapper commentMapper;

    /**
     * User Mapper.
     */
    @Autowired
    private UserMapper userMapper;

    /**
     * NotificationUtil management service.
     */
    @Autowired
    private NotificationMgmtService notificationMgmtService;

    /**
     * Article query service.
     */
    @Autowired
    private ArticleQueryService articleQueryService;

    /**
     * User query service.
     */
    @Autowired
    private UserQueryService userQueryService;

    /**
     * Avatar query service.
     */
    @Autowired
    private AvatarQueryService avatarQueryService;

    /**
     * Short link query service.
     */
    @Autowired
    private ShortLinkQueryService shortLinkQueryService;

    /**
     * Language service.
     */
    @Autowired
    private LangPropsService langPropsService;

    /**
     * PointtransferUtil management service.
     */
    @Autowired
    private PointtransferMgmtService pointtransferMgmtService;

    /**
     * Comment query service.
     */
    @Autowired
    private CommentQueryService commentQueryService;

    /**
     * FollowUtil query service.
     */
    @Autowired
    private FollowQueryService followQueryService;

    /**
     * RoleUtil query service.
     */
    @Autowired
    private RoleQueryService roleQueryService;

    @Override
    public void onApplicationEvent(AddCommentEvent event) {
        final JSONObject data = event.event;
        LOGGER.trace( "Processing an event [type={0}, data={1}]", data);

        try {
            final JSONObject originalArticle = data.getJSONObject(Article.ARTICLE);
            final JSONObject originalComment = data.getJSONObject(Comment.COMMENT);
            final boolean fromClient = data.optBoolean(Common.FROM_CLIENT);
            final int commentViewMode = data.optInt(UserExt.USER_COMMENT_VIEW_MODE);

            final String articleId = originalArticle.optString(Keys.OBJECT_ID);
            final String commentId = originalComment.optString(Keys.OBJECT_ID);
            final String originalCmtId = originalComment.optString(Comment.COMMENT_ORIGINAL_COMMENT_ID);
            final String commenterId = originalComment.optString(Comment.COMMENT_AUTHOR_ID);

            final String commentContent = originalComment.optString(Comment.COMMENT_CONTENT);
            final JSONObject commenter = userQueryService.getUser(commenterId);
            final String commenterName = commenter.optString(User.USER_NAME);

            // 0. Data channel (WebSocket)
            final JSONObject chData = JSONs.clone(originalComment);
            chData.put(Comment.COMMENT_T_COMMENTER, commenter);
            chData.put(Keys.OBJECT_ID, commentId);
            chData.put(Article.ARTICLE_T_ID, articleId);
            chData.put(Article.ARTICLE, originalArticle);
            chData.put(Comment.COMMENT_T_ID, commentId);
            chData.put(Comment.COMMENT_ORIGINAL_COMMENT_ID, originalCmtId);

            String originalCmtAuthorId = null;
            if (StringUtils.isNotBlank(originalCmtId)) {
                final Query numQuery = new Query()
                        .setPageSize(Integer.MAX_VALUE).setCurrentPageNum(1).setPageCount(1);

                switch (commentViewMode) {
                    case UserExt.USER_COMMENT_VIEW_MODE_C_TRADITIONAL:
                        numQuery.setFilter(CompositeFilterOperator.and(
                                new PropertyFilter(Comment.COMMENT_ON_ARTICLE_ID, FilterOperator.EQUAL, articleId),
                                new PropertyFilter(Keys.OBJECT_ID, FilterOperator.LESS_THAN_OR_EQUAL, originalCmtId)
                        )).addSort(Keys.OBJECT_ID, SortDirection.ASCENDING);

                        break;
                    case UserExt.USER_COMMENT_VIEW_MODE_C_REALTIME:
                        numQuery.setFilter(CompositeFilterOperator.and(
                                new PropertyFilter(Comment.COMMENT_ON_ARTICLE_ID, FilterOperator.EQUAL, articleId),
                                new PropertyFilter(Keys.OBJECT_ID, FilterOperator.GREATER_THAN_OR_EQUAL, originalCmtId)
                        )).addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);

                        break;
                }

                final long num = commentMapper.count(numQuery);
                final int page = (int) ((num / Symphonys.getInt("articleCommentsPageSize")) + 1);
                chData.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, page);

                final JSONObject originalCmt = commentMapper.get(originalCmtId);
                originalCmtAuthorId = originalCmt.optString(Comment.COMMENT_AUTHOR_ID);
                final JSONObject originalCmtAuthor = userMapper.get(originalCmtAuthorId);

                if (Comment.COMMENT_ANONYMOUS_C_PUBLIC == originalCmt.optInt(Comment.COMMENT_ANONYMOUS)) {
                    chData.put(Comment.COMMENT_T_ORIGINAL_AUTHOR_THUMBNAIL_URL,
                            avatarQueryService.getAvatarURLByUser(
                                    UserExt.USER_AVATAR_VIEW_MODE_C_ORIGINAL, originalCmtAuthor, "20"));
                } else {
                    chData.put(Comment.COMMENT_T_ORIGINAL_AUTHOR_THUMBNAIL_URL,
                            avatarQueryService.getDefaultAvatarURL("20"));
                }
            }

            if (Comment.COMMENT_ANONYMOUS_C_PUBLIC == originalComment.optInt(Comment.COMMENT_ANONYMOUS)) {
                chData.put(Comment.COMMENT_T_AUTHOR_NAME, commenterName);
                chData.put(Comment.COMMENT_T_AUTHOR_THUMBNAIL_URL, avatarQueryService.getAvatarURLByUser(
                        UserExt.USER_AVATAR_VIEW_MODE_C_ORIGINAL, commenter, "48"));
            } else {
                chData.put(Comment.COMMENT_T_AUTHOR_NAME, UserExt.ANONYMOUS_USER_NAME);
                chData.put(Comment.COMMENT_T_AUTHOR_THUMBNAIL_URL, avatarQueryService.getDefaultAvatarURL("48"));
            }

            chData.put(Common.THUMBNAIL_UPDATE_TIME, commenter.optLong(UserExt.USER_UPDATE_TIME));
            chData.put(Common.TIME_AGO, langPropsService.get("justNowLabel"));
            chData.put(Comment.COMMENT_CREATE_TIME_STR, DateFormatUtils.format(chData.optLong(Comment.COMMENT_CREATE_TIME), "yyyy-MM-dd HH:mm:ss"));
            String thankTemplate = langPropsService.get("thankConfirmLabel");
            thankTemplate = thankTemplate.replace("{point}", String.valueOf(Symphonys.getInt("pointThankComment")))
                    .replace("{user}", commenterName);
            chData.put(Comment.COMMENT_T_THANK_LABEL, thankTemplate);
            String cc = shortLinkQueryService.linkArticle(commentContent);
            cc = shortLinkQueryService.linkTag(cc);
            cc = Emotions.toAliases(cc);
            cc = Emotions.convert(cc);
            cc = Markdowns.toHTML(cc);
            cc = Markdowns.clean(cc, "");
            cc = MP3Players.render(cc);
            cc = VideoPlayers.render(cc);

            if (fromClient) {
                // "<i class='ft-small'>by 88250</i>"
                String syncCommenterName = StringUtils.substringAfter(cc, "<i class=\"ft-small\">by ");
                syncCommenterName = StringUtils.substringBefore(syncCommenterName, "</i>");

                if (UserRegisterValidation.invalidUserName(syncCommenterName)) {
                    syncCommenterName = UserExt.ANONYMOUS_USER_NAME;
                }

                cc = cc.replaceAll("<i class=\"ft-small\">by .*</i>", "");

                chData.put(Comment.COMMENT_T_AUTHOR_NAME, syncCommenterName);
            }

            chData.put(Comment.COMMENT_CONTENT, cc);
            chData.put(Comment.COMMENT_UA, originalComment.optString(Comment.COMMENT_UA));
            chData.put(Common.FROM_CLIENT, fromClient);

            ArticleChannel.notifyComment(chData);

            // + Article Heat
            final JSONObject articleHeat = new JSONObject();
            articleHeat.put(Article.ARTICLE_T_ID, articleId);
            articleHeat.put(Common.OPERATION, "+");

            ArticleListChannel.notifyHeat(articleHeat);
            ArticleChannel.notifyHeat(articleHeat);

            final boolean isDiscussion = originalArticle.optInt(Article.ARTICLE_TYPE) == Article.ARTICLE_TYPE_C_DISCUSSION;
            final String articleAuthorId = originalArticle.optString(Article.ARTICLE_AUTHOR_ID);
            final boolean commenterIsArticleAuthor = articleAuthorId.equals(commenterId);

            final Set<String> requisiteAtParticipantsPermissions = new HashSet<>();
            requisiteAtParticipantsPermissions.add(Permission.PERMISSION_ID_C_COMMON_AT_PARTICIPANTS);
            final boolean hasAtParticipantPerm = roleQueryService.userHasPermissions(commenterId, requisiteAtParticipantsPermissions);

            if (hasAtParticipantPerm) {
                // 1. '@participants' NotificationUtil
                if (commentContent.contains("@participants ")) {
                    final List<JSONObject> participants = articleQueryService.getArticleLatestParticipants(
                            UserExt.USER_AVATAR_VIEW_MODE_C_ORIGINAL, articleId, Integer.MAX_VALUE);
                    int count = participants.size();
                    if (count < 1) {
                        return;
                    }

                    count = 0;
                    for (final JSONObject participant : participants) {
                        final String participantId = participant.optString(Keys.OBJECT_ID);
                        if (participantId.equals(commenterId)) {
                            continue;
                        }

                        count++;

                        final JSONObject requestJSONObject = new JSONObject();
                        requestJSONObject.put(Notification.NOTIFICATION_USER_ID, participantId);
                        requestJSONObject.put(Notification.NOTIFICATION_DATA_ID, commentId);

                        notificationMgmtService.addAtNotification(requestJSONObject);
                    }

                    final int sum = count * Pointtransfer.TRANSFER_SUM_C_AT_PARTICIPANTS;
                    if (sum > 0) {
                        pointtransferMgmtService.transfer(commenterId, Pointtransfer.ID_C_SYS,
                                Pointtransfer.TRANSFER_TYPE_C_AT_PARTICIPANTS, sum, commentId, System.currentTimeMillis());
                    }

                    return;
                }
            }

            final Set<String> atUserNames = userQueryService.getUserNames(commentContent);
            atUserNames.remove(commenterName);

            final Set<String> watcherIds = new HashSet<>();
            final JSONObject followerUsersResult =
                    followQueryService.getArticleWatchers(UserExt.USER_AVATAR_VIEW_MODE_C_ORIGINAL,
                            articleId, 1, Integer.MAX_VALUE);

            final List<JSONObject> watcherUsers = (List<JSONObject>) followerUsersResult.opt(Keys.RESULTS);
            for (final JSONObject watcherUser : watcherUsers) {
                final String watcherUserId = watcherUser.optString(Keys.OBJECT_ID);

                watcherIds.add(watcherUserId);
            }
            watcherIds.remove(articleAuthorId);

            if (commenterIsArticleAuthor && atUserNames.isEmpty() && watcherIds.isEmpty() && StringUtils.isBlank(originalCmtId)) {
                return;
            }

            // 2. 'Commented' NotificationUtil
            if (!commenterIsArticleAuthor) {
                final JSONObject requestJSONObject = new JSONObject();
                requestJSONObject.put(Notification.NOTIFICATION_USER_ID, articleAuthorId);
                requestJSONObject.put(Notification.NOTIFICATION_DATA_ID, commentId);

                notificationMgmtService.addCommentedNotification(requestJSONObject);
            }

            // 3. 'Reply' NotificationUtil
            final Set<String> repliedIds = new HashSet<>();
            if (StringUtils.isNotBlank(originalCmtId)) {
                if (!articleAuthorId.equals(originalCmtAuthorId)) {
                    final JSONObject requestJSONObject = new JSONObject();
                    requestJSONObject.put(Notification.NOTIFICATION_USER_ID, originalCmtAuthorId);
                    requestJSONObject.put(Notification.NOTIFICATION_DATA_ID, commentId);

                    notificationMgmtService.addReplyNotification(requestJSONObject);

                    repliedIds.add(originalCmtAuthorId);
                }
            }

            final String articleContent = originalArticle.optString(Article.ARTICLE_CONTENT);
            final Set<String> articleContentAtUserNames = userQueryService.getUserNames(articleContent);

            final Set<String> requisiteAtUserPermissions = new HashSet<>();
            requisiteAtUserPermissions.add(Permission.PERMISSION_ID_C_COMMON_AT_USER);
            final boolean hasAtUserPerm = roleQueryService.userHasPermissions(commenterId, requisiteAtUserPermissions);

            final Set<String> atIds = new HashSet<>();
            if (hasAtUserPerm) {
                // 4. 'At' NotificationUtil
                for (final String userName : atUserNames) {
                    if (isDiscussion && !articleContentAtUserNames.contains(userName)) {
                        continue;
                    }

                    final JSONObject user = userQueryService.getUserByName(userName);

                    if (null == user) {
                        LOGGER.warn( "Not found user by name [{0}]", userName);

                        continue;
                    }

                    if (user.optString(Keys.OBJECT_ID).equals(articleAuthorId)) {
                        continue; // Has notified in step 2
                    }

                    final String userId = user.optString(Keys.OBJECT_ID);
                    if (repliedIds.contains(userId)) {
                        continue;
                    }

                    final JSONObject requestJSONObject = new JSONObject();
                    requestJSONObject.put(Notification.NOTIFICATION_USER_ID, userId);
                    requestJSONObject.put(Notification.NOTIFICATION_DATA_ID, commentId);

                    notificationMgmtService.addAtNotification(requestJSONObject);

                    atIds.add(userId);
                }
            }

            // 5. 'following - article comment' NotificationUtil
            for (final String userId : watcherIds) {
                final JSONObject watcher = userMapper.get(userId);
                final String watcherName = watcher.optString(User.USER_NAME);

                if ((isDiscussion && !articleContentAtUserNames.contains(watcherName)) || commenterName.equals(watcherName)
                        || repliedIds.contains(userId) || atIds.contains(userId)) {
                    continue;
                }

                final JSONObject requestJSONObject = new JSONObject();
                requestJSONObject.put(Notification.NOTIFICATION_USER_ID, userId);
                requestJSONObject.put(Notification.NOTIFICATION_DATA_ID, commentId);

                notificationMgmtService.addFollowingArticleCommentNotification(requestJSONObject);
            }
        } catch (final Exception e) {
            LOGGER.error("Sends the comment notification failed", e);
        }
    }
}
