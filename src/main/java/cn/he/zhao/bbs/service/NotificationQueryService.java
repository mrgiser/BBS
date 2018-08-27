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
package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.util.Symphonys;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Notification query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.14.0.2, Jul 15, 2018
 * @since 0.2.5
 */
@Service
public class NotificationQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationQueryService.class);

    /**
     * Notification Mapper.
     */
    @Autowired
    private NotificationMapper notificationMapper;

    /**
     * Article Mapper.
     */
    @Autowired
    private ArticleMapper articleMapper;

    /**
     * Comment query service.
     */
    @Autowired
    private CommentQueryService commentQueryService;

    /**
     * User Mapper.
     */
    @Autowired
    private UserMapper userMapper;

    /**
     * Comment Mapper.
     */
    @Autowired
    private CommentMapper commentMapper;

    /**
     * Reward Mapper.
     */
    @Autowired
    private RewardMapper rewardMapper;

    /**
     * Tag Mapper.
     */
    @Autowired
    private TagMapper tagMapper;

    /**
     * Pointtransfer Mapper.
     */
    @Autowired
    private PointtransferMapper pointtransferMapper;

    /**
     * Avatar query service.
     */
    @Autowired
    private AvatarQueryService avatarQueryService;

    /**
     * Language service.
     */
    @Autowired
    private LangPropsService langPropsService;

    /**
     * Role query service.
     */
    @Autowired
    private RoleQueryService roleQueryService;

    /**
     * Gets a notification by the specified id.
     *
     * @param notificationId the specified id
     * @return notification, returns {@code null} if not found
     */
    public JSONObject getNotification(final String notificationId) {
        try {
            return notificationMapper.get(notificationId);
        } catch (final Exception e) {
            LOGGER.error( "Gets a notification [id=" + notificationId + "] failed", e);

            return null;
        }
    }

    /**
     * Gets the count of unread 'following' notifications of a user specified with the given user id.
     *
     * @param userId the given user id
     * @return count of unread notifications, returns {@code 0} if occurs exception
     */
    public int getUnreadFollowingNotificationCount(final String userId) {
        final List<Filter> filters = new ArrayList<>();
        filters.add(new PropertyFilter(Notification.NOTIFICATION_USER_ID, FilterOperator.EQUAL, userId));
        filters.add(new PropertyFilter(Notification.NOTIFICATION_HAS_READ, FilterOperator.EQUAL, false));

        final List<Filter> subFilters = new ArrayList<>();
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
                Notification.DATA_TYPE_C_FOLLOWING_ARTICLE_UPDATE));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
                Notification.DATA_TYPE_C_FOLLOWING_ARTICLE_COMMENT));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
                Notification.DATA_TYPE_C_FOLLOWING_USER));

        filters.add(new CompositeFilter(CompositeFilterOperator.OR, subFilters));

        final Query query = new Query().setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));

        try {
            final JSONObject result = notificationMapper.get(query);

            return result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_RECORD_COUNT);
        } catch (final MapperException e) {
            LOGGER.error( "Gets [following] notification count failed [userId=" + userId + "]", e);

            return 0;
        }
    }

    /**
     * Gets the count of unread 'sys announce' notifications of a user specified with the given user id.
     *
     * @param userId the given user id
     * @return count of unread notifications, returns {@code 0} if occurs exception
     */
    public int getUnreadSysAnnounceNotificationCount(final String userId) {
        final List<Filter> filters = new ArrayList<>();
        filters.add(new PropertyFilter(Notification.NOTIFICATION_USER_ID, FilterOperator.EQUAL, userId));
        filters.add(new PropertyFilter(Notification.NOTIFICATION_HAS_READ, FilterOperator.EQUAL, false));

        final List<Filter> subFilters = new ArrayList<>();
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
                Notification.DATA_TYPE_C_SYS_ANNOUNCE_ARTICLE));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
                Notification.DATA_TYPE_C_SYS_ANNOUNCE_NEW_USER));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
                Notification.DATA_TYPE_C_SYS_ANNOUNCE_ROLE_CHANGED));

        filters.add(new CompositeFilter(CompositeFilterOperator.OR, subFilters));

        final Query query = new Query().setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));

        try {
            final JSONObject result = notificationMapper.get(query);

            return result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_RECORD_COUNT);
        } catch (final MapperException e) {
            LOGGER.error( "Gets [sys_announce] notification count failed [userId=" + userId + "]", e);

            return 0;
        }
    }

    /**
     * Gets 'sys announce' type notifications with the specified user id, current page number and page size.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param userId         the specified user id
     * @param currentPageNum the specified page number
     * @param pageSize       the specified page size
     * @return result json object, for example,      <pre>
     * {
     *     "paginationRecordCount": int,
     *     "rslts": java.util.List[{
     *         "oId": "", // notification record id
     *         "description": "",
     *         "hasRead": boolean
     *     }, ....]
     * }
     * </pre>
     * @throws ServiceException service exception
     */
    public JSONObject getSysAnnounceNotifications(final int avatarViewMode,
                                                  final String userId, final int currentPageNum, final int pageSize) throws ServiceException {
        final JSONObject ret = new JSONObject();
        final List<JSONObject> rslts = new ArrayList<>();

        ret.put(Keys.RESULTS, (Object) rslts);

        final List<Filter> filters = new ArrayList<>();
        filters.add(new PropertyFilter(Notification.NOTIFICATION_USER_ID, FilterOperator.EQUAL, userId));

        final List<Filter> subFilters = new ArrayList<>();
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
                Notification.DATA_TYPE_C_SYS_ANNOUNCE_ARTICLE));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
                Notification.DATA_TYPE_C_SYS_ANNOUNCE_NEW_USER));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
                Notification.DATA_TYPE_C_SYS_ANNOUNCE_ROLE_CHANGED));

        filters.add(new CompositeFilter(CompositeFilterOperator.OR, subFilters));

        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize).
                setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters)).
                addSort(Notification.NOTIFICATION_HAS_READ, SortDirection.ASCENDING).
                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);

        try {
            final JSONObject queryResult = notificationMapper.get(query);
            final JSONArray results = queryResult.optJSONArray(Keys.RESULTS);

            ret.put(Pagination.PAGINATION_RECORD_COUNT,
                    queryResult.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_RECORD_COUNT));

            for (int i = 0; i < results.length(); i++) {
                final JSONObject notification = results.optJSONObject(i);
                final String dataId = notification.optString(Notification.NOTIFICATION_DATA_ID);
                final int dataType = notification.optInt(Notification.NOTIFICATION_DATA_TYPE);
                String desTemplate = "";

                switch (dataType) {
                    case Notification.DATA_TYPE_C_SYS_ANNOUNCE_NEW_USER:
                        desTemplate = langPropsService.get("notificationSysNewUser1Label");

                        break;
                    case Notification.DATA_TYPE_C_SYS_ANNOUNCE_ARTICLE:
                        desTemplate = langPropsService.get("notificationSysArticleLabel");

                        final JSONObject article15 = articleMapper.get(dataId);
                        if (null == article15) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String articleLink15 = "<a href=\""
                                +  SpringUtil.getServerPath() + article15.optString(Article.ARTICLE_PERMALINK) + "\">"
                                + article15.optString(Article.ARTICLE_TITLE) + "</a>";
                        desTemplate = desTemplate.replace("{article}", articleLink15);

                        break;
                    case Notification.DATA_TYPE_C_SYS_ANNOUNCE_ROLE_CHANGED:
                        desTemplate = langPropsService.get("notificationSysRoleChangedLabel");

                        final String oldRoleId = dataId.split("-")[0];
                        final String newRoleId = dataId.split("-")[1];
                        final JSONObject oldRole = roleQueryService.getRole(oldRoleId);
                        final JSONObject newRole = roleQueryService.getRole(newRoleId);

                        desTemplate = desTemplate.replace("{oldRole}", oldRole.optString(Role.ROLE_NAME));
                        desTemplate = desTemplate.replace("{newRole}", newRole.optString(Role.ROLE_NAME));

                        break;
                    default:
                        throw new AssertionError();
                }

                notification.put(Common.DESCRIPTION, desTemplate);
                notification.put(Common.CREATE_TIME, new Date(notification.optLong(Keys.OBJECT_ID)));

                rslts.add(notification);
            }

            return ret;
        } catch (final MapperException e) {
            LOGGER.error( "Gets [sys_announce] notifications failed", e);

            throw new ServiceException(e);
        }
    }

    /**
     * Gets the count of unread notifications of a user specified with the given user id.
     *
     * @param userId the given user id
     * @return count of unread notifications, returns {@code 0} if occurs exception
     */
    public int getUnreadNotificationCount(final String userId) {
        Stopwatchs.start("Gets unread notification count");
        try {
            final Query query = new Query();
            query.setFilter(CompositeFilterOperator.and(
                    new PropertyFilter(Notification.NOTIFICATION_USER_ID, FilterOperator.EQUAL, userId),
                    new PropertyFilter(Notification.NOTIFICATION_HAS_READ, FilterOperator.EQUAL, false)
            ));

            try {
                return (int) notificationMapper.count(query);
            } catch (final MapperException e) {
                LOGGER.error( "Gets unread notification count failed [userId=" + userId + "]", e);

                return 0;
            }
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Gets the count of unread notifications of a user specified with the given user id and data type.
     *
     * @param userId               the given user id
     * @param notificationDataType the specified notification data type
     * @return count of unread notifications, returns {@code 0} if occurs exception
     */
    public int getUnreadNotificationCountByType(final String userId, final int notificationDataType) {
        final List<Filter> filters = new ArrayList<>();

        filters.add(new PropertyFilter(Notification.NOTIFICATION_USER_ID, FilterOperator.EQUAL, userId));
        filters.add(new PropertyFilter(Notification.NOTIFICATION_HAS_READ, FilterOperator.EQUAL, false));
        filters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, notificationDataType));

        final Query query = new Query();
        query.setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters)).addProjection(Keys.OBJECT_ID, String.class);

        try {
            final JSONObject result = notificationMapper.get(query);

            return result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_RECORD_COUNT);
        } catch (final MapperException e) {
            LOGGER.error( "Gets [" + notificationDataType + "] notification count failed [userId=" + userId + "]", e);

            return 0;
        }
    }

    /**
     * Gets the count of unread 'point' notifications of a user specified with the given user id.
     *
     * @param userId the given user id
     * @return count of unread notifications, returns {@code 0} if occurs exception
     */
    public int getUnreadPointNotificationCount(final String userId) {
        final List<Filter> filters = new ArrayList<>();
        filters.add(new PropertyFilter(Notification.NOTIFICATION_USER_ID, FilterOperator.EQUAL, userId));
        filters.add(new PropertyFilter(Notification.NOTIFICATION_HAS_READ, FilterOperator.EQUAL, false));

        final List<Filter> subFilters = new ArrayList<>();
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, Notification.DATA_TYPE_C_POINT_ARTICLE_REWARD));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, Notification.DATA_TYPE_C_POINT_ARTICLE_THANK));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, Notification.DATA_TYPE_C_POINT_CHARGE));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, Notification.DATA_TYPE_C_POINT_EXCHANGE));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, Notification.DATA_TYPE_C_ABUSE_POINT_DEDUCT));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, Notification.DATA_TYPE_C_POINT_COMMENT_THANK));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, Notification.DATA_TYPE_C_POINT_COMMENT_ACCEPT));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, Notification.DATA_TYPE_C_POINT_TRANSFER));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, Notification.DATA_TYPE_C_INVITECODE_USED));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, Notification.DATA_TYPE_C_INVITATION_LINK_USED));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, Notification.DATA_TYPE_C_POINT_PERFECT_ARTICLE));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, Notification.DATA_TYPE_C_POINT_REPORT_HANDLED));

        filters.add(new CompositeFilter(CompositeFilterOperator.OR, subFilters));

        final Query query = new Query().setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));
        try {
            final JSONObject result = notificationMapper.get(query);

            return result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_RECORD_COUNT);
        } catch (final MapperException e) {
            LOGGER.error( "Gets [point] notification count failed [userId=" + userId + "]", e);

            return 0;
        }
    }

    /**
     * Gets 'point' type notifications with the specified user id, current page number and page size.
     *
     * @param userId         the specified user id
     * @param currentPageNum the specified page number
     * @param pageSize       the specified page size
     * @return result json object, for example,      <pre>
     * {
     *     "paginationRecordCount": int,
     *     "rslts": java.util.List[{
     *         "oId": "", // notification record id
     *         "description": "",
     *         "hasRead": boolean
     *     }, ....]
     * }
     * </pre>
     * @throws ServiceException service exception
     */
    public JSONObject getPointNotifications(final String userId, final int currentPageNum, final int pageSize)
            throws ServiceException {
        final JSONObject ret = new JSONObject();
        final List<JSONObject> rslts = new ArrayList<>();

        ret.put(Keys.RESULTS, (Object) rslts);

        final List<Filter> filters = new ArrayList<>();
        filters.add(new PropertyFilter(Notification.NOTIFICATION_USER_ID, FilterOperator.EQUAL, userId));

        final List<Filter> subFilters = new ArrayList<>();
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, Notification.DATA_TYPE_C_POINT_ARTICLE_REWARD));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, Notification.DATA_TYPE_C_POINT_ARTICLE_THANK));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, Notification.DATA_TYPE_C_POINT_CHARGE));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, Notification.DATA_TYPE_C_POINT_EXCHANGE));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, Notification.DATA_TYPE_C_ABUSE_POINT_DEDUCT));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, Notification.DATA_TYPE_C_POINT_COMMENT_THANK));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, Notification.DATA_TYPE_C_POINT_COMMENT_ACCEPT));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, Notification.DATA_TYPE_C_POINT_TRANSFER));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, Notification.DATA_TYPE_C_INVITECODE_USED));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, Notification.DATA_TYPE_C_INVITATION_LINK_USED));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, Notification.DATA_TYPE_C_POINT_PERFECT_ARTICLE));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, Notification.DATA_TYPE_C_POINT_REPORT_HANDLED));

        filters.add(new CompositeFilter(CompositeFilterOperator.OR, subFilters));

        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize).
                setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters)).
                addSort(Notification.NOTIFICATION_HAS_READ, SortDirection.ASCENDING).
                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);
        try {
            final JSONObject queryResult = notificationMapper.get(query);
            final JSONArray results = queryResult.optJSONArray(Keys.RESULTS);

            ret.put(Pagination.PAGINATION_RECORD_COUNT,
                    queryResult.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_RECORD_COUNT));

            for (int i = 0; i < results.length(); i++) {
                final JSONObject notification = results.optJSONObject(i);
                final String dataId = notification.optString(Notification.NOTIFICATION_DATA_ID);
                final int dataType = notification.optInt(Notification.NOTIFICATION_DATA_TYPE);
                String desTemplate = "";

                switch (dataType) {
                    case Notification.DATA_TYPE_C_POINT_ARTICLE_THANK:
                        desTemplate = langPropsService.get("notificationArticleThankLabel");

                        final JSONObject reward12 = rewardMapper.get(dataId);
                        final String senderId12 = reward12.optString(Reward.SENDER_ID);
                        final JSONObject user12 = userMapper.get(senderId12);
                        final String articleId12 = reward12.optString(Reward.DATA_ID);
                        final JSONObject article12 = articleMapper.get(articleId12);
                        if (null == article12) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String userLink12 = UserExt.getUserLink(user12);
                        desTemplate = desTemplate.replace("{user}", userLink12);

                        final String articleLink12 = "<a href=\""
                                +  SpringUtil.getServerPath() + article12.optString(Article.ARTICLE_PERMALINK) + "\">"
                                + article12.optString(Article.ARTICLE_TITLE) + "</a>";
                        desTemplate = desTemplate.replace("{article}", articleLink12);

                        break;
                    case Notification.DATA_TYPE_C_POINT_ARTICLE_REWARD:
                        desTemplate = langPropsService.get("notificationArticleRewardLabel");

                        final JSONObject reward7 = rewardMapper.get(dataId);
                        final String senderId7 = reward7.optString(Reward.SENDER_ID);
                        final JSONObject user7 = userMapper.get(senderId7);
                        final String articleId7 = reward7.optString(Reward.DATA_ID);
                        final JSONObject article7 = articleMapper.get(articleId7);
                        if (null == article7) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String userLink7 = UserExt.getUserLink(user7);
                        desTemplate = desTemplate.replace("{user}", userLink7);

                        final String articleLink7 = "<a href=\""
                                +  SpringUtil.getServerPath() + article7.optString(Article.ARTICLE_PERMALINK) + "\">"
                                + article7.optString(Article.ARTICLE_TITLE) + "</a>";
                        desTemplate = desTemplate.replace("{article}", articleLink7);

                        break;
                    case Notification.DATA_TYPE_C_POINT_CHARGE:
                        desTemplate = langPropsService.get("notificationPointChargeLabel");

                        final JSONObject transfer5 = pointtransferMapper.get(dataId);
                        final int sum5 = transfer5.optInt(Pointtransfer.SUM);
                        final String memo5 = transfer5.optString(Pointtransfer.DATA_ID);
                        final String yuan = memo5.split("-")[0];

                        desTemplate = desTemplate.replace("{yuan}", yuan);
                        desTemplate = desTemplate.replace("{point}", String.valueOf(sum5));

                        break;
                    case Notification.DATA_TYPE_C_POINT_EXCHANGE:
                        desTemplate = langPropsService.get("notificationPointExchangeLabel");

                        final JSONObject transfer6 = pointtransferMapper.get(dataId);
                        final int sum6 = transfer6.optInt(Pointtransfer.SUM);
                        final String yuan6 = transfer6.optString(Pointtransfer.DATA_ID);

                        desTemplate = desTemplate.replace("{yuan}", yuan6);
                        desTemplate = desTemplate.replace("{point}", String.valueOf(sum6));

                        break;
                    case Notification.DATA_TYPE_C_ABUSE_POINT_DEDUCT:
                        desTemplate = langPropsService.get("notificationAbusePointDeductLabel");

                        final JSONObject transfer7 = pointtransferMapper.get(dataId);
                        final int sum7 = transfer7.optInt(Pointtransfer.SUM);
                        final String memo7 = transfer7.optString(Pointtransfer.DATA_ID);

                        desTemplate = desTemplate.replace("{action}", memo7);
                        desTemplate = desTemplate.replace("{point}", String.valueOf(sum7));

                        break;
                    case Notification.DATA_TYPE_C_POINT_COMMENT_THANK:
                        desTemplate = langPropsService.get("notificationCmtThankLabel");

                        final JSONObject reward8 = rewardMapper.get(dataId);
                        final String senderId8 = reward8.optString(Reward.SENDER_ID);
                        final JSONObject user8 = userMapper.get(senderId8);
                        final JSONObject comment8 = commentMapper.get(reward8.optString(Reward.DATA_ID));
                        final String articleId8 = comment8.optString(Comment.COMMENT_ON_ARTICLE_ID);
                        final JSONObject article8 = articleMapper.get(articleId8);
                        if (null == article8) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String userLink8 = UserExt.getUserLink(user8);
                        desTemplate = desTemplate.replace("{user}", userLink8);

                        final String articleLink8 = "<a href=\""
                                +  SpringUtil.getServerPath() + article8.optString(Article.ARTICLE_PERMALINK) + "\">"
                                + article8.optString(Article.ARTICLE_TITLE) + "</a>";
                        desTemplate = desTemplate.replace("{article}", articleLink8);

                        break;
                    case Notification.DATA_TYPE_C_POINT_COMMENT_ACCEPT:
                        desTemplate = langPropsService.get("notificationCmtAcceptLabel");

                        final JSONObject reward33 = rewardMapper.get(dataId);
                        final String articleId33 = reward33.optString(Reward.DATA_ID);
                        final JSONObject article33 = articleMapper.get(articleId33);
                        if (null == article33) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String articleAuthorId = article33.optString(Article.ARTICLE_AUTHOR_ID);
                        final JSONObject user33 = userMapper.get(articleAuthorId);
                        final String userLink33 = UserExt.getUserLink(user33);
                        desTemplate = desTemplate.replace("{user}", userLink33);

                        final String articleLink33 = "<a href=\""
                                +  SpringUtil.getServerPath() + article33.optString(Article.ARTICLE_PERMALINK) + "\">"
                                + article33.optString(Article.ARTICLE_TITLE) + "</a>";
                        desTemplate = desTemplate.replace("{article}", Emotions.convert(articleLink33));

                        break;
                    case Notification.DATA_TYPE_C_POINT_TRANSFER:
                        desTemplate = langPropsService.get("notificationPointTransferLabel");

                        final JSONObject transfer101 = pointtransferMapper.get(dataId);
                        final String fromId101 = transfer101.optString(Pointtransfer.FROM_ID);
                        final JSONObject user101 = userMapper.get(fromId101);
                        final int sum101 = transfer101.optInt(Pointtransfer.SUM);

                        final String userLink101 = UserExt.getUserLink(user101);
                        desTemplate = desTemplate.replace("{user}", userLink101);
                        desTemplate = desTemplate.replace("{amount}", String.valueOf(sum101));

                        break;
                    case Notification.DATA_TYPE_C_INVITECODE_USED:
                        desTemplate = langPropsService.get("notificationInvitecodeUsedLabel");

                        final JSONObject invitedUser = userMapper.get(dataId);
                        final String invitedUserLink = UserExt.getUserLink(invitedUser);

                        desTemplate = desTemplate.replace("{user}", invitedUserLink);

                        break;
                    case Notification.DATA_TYPE_C_INVITATION_LINK_USED:
                        desTemplate = langPropsService.get("notificationInvitationLinkUsedLabel");

                        final JSONObject invitedUser18 = userMapper.get(dataId);
                        final String invitedUserLink18 = UserExt.getUserLink(invitedUser18);

                        desTemplate = desTemplate.replace("{user}", invitedUserLink18);

                        break;
                    case Notification.DATA_TYPE_C_POINT_PERFECT_ARTICLE:
                        desTemplate = langPropsService.get("notificationPointPerfectArticleLabel");

                        final JSONObject article22 = articleMapper.get(dataId);
                        if (null == article22) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String articleLink22 = "<a href=\""
                                +  SpringUtil.getServerPath() + article22.optString(Article.ARTICLE_PERMALINK) + "\">"
                                + article22.optString(Article.ARTICLE_TITLE) + "</a>";
                        desTemplate = desTemplate.replace("{article}", articleLink22);

                        break;
                    case Notification.DATA_TYPE_C_POINT_REPORT_HANDLED:
                        desTemplate = langPropsService.get("notification36Label");

                        break;
                    default:
                        throw new AssertionError();
                }

                notification.put(Common.DESCRIPTION, desTemplate);
                notification.put(Common.CREATE_TIME, new Date(notification.optLong(Keys.OBJECT_ID)));

                rslts.add(notification);
            }

            return ret;
        } catch (final MapperException e) {
            LOGGER.error( "Gets [point] notifications failed", e);

            throw new ServiceException(e);
        }
    }

    /**
     * Gets 'commented' type notifications with the specified user id, current page number and page size.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param userId         the specified user id
     * @param currentPageNum the specified page number
     * @param pageSize       the specified page size
     * @return result json object, for example,      <pre>
     * {
     *     "paginationRecordCount": int,
     *     "rslts": java.util.List[{
     *         "oId": "", // notification record id
     *         "commentAuthorName": "",
     *         "commentContent": "",
     *         "commentAuthorThumbnailURL": "",
     *         "commentArticleTitle": "",
     *         "commentArticleType": int,
     *         "commentSharpURL": "",
     *         "commentCreateTime": java.util.Date,
     *         "hasRead": boolean
     *     }, ....]
     * }
     * </pre>
     * @throws ServiceException service exception
     */
    public JSONObject getCommentedNotifications(final int avatarViewMode,
                                                final String userId, final int currentPageNum, final int pageSize) throws ServiceException {
        final JSONObject ret = new JSONObject();
        final List<JSONObject> rslts = new ArrayList<>();

        ret.put(Keys.RESULTS, (Object) rslts);

        final List<Filter> filters = new ArrayList<>();
        filters.add(new PropertyFilter(Notification.NOTIFICATION_USER_ID, FilterOperator.EQUAL, userId));
        filters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, Notification.DATA_TYPE_C_COMMENTED));

        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize).
                setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters)).
                addSort(Notification.NOTIFICATION_HAS_READ, SortDirection.ASCENDING).
                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);

        try {
            final JSONObject user = userMapper.get(userId);
            final int cmtViewMode = user.optInt(UserExt.USER_COMMENT_VIEW_MODE);

            final JSONObject queryResult = notificationMapper.get(query);
            final JSONArray results = queryResult.optJSONArray(Keys.RESULTS);

            ret.put(Pagination.PAGINATION_RECORD_COUNT,
                    queryResult.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_RECORD_COUNT));

            for (int i = 0; i < results.length(); i++) {
                final JSONObject notification = results.optJSONObject(i);
                final String commentId = notification.optString(Notification.NOTIFICATION_DATA_ID);

                final JSONObject comment = commentQueryService.getCommentById(avatarViewMode, commentId);

                final Query q = new Query().setPageCount(1).
                        addProjection(Article.ARTICLE_PERFECT, Integer.class).
                        addProjection(Article.ARTICLE_TITLE, String.class).
                        addProjection(Article.ARTICLE_TYPE, Integer.class).
                        setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.EQUAL,
                                comment.optString(Comment.COMMENT_ON_ARTICLE_ID)));
                final JSONArray rlts = articleMapper.get(q).optJSONArray(Keys.RESULTS);
                final JSONObject article = rlts.optJSONObject(0);
                final String articleTitle = article.optString(Article.ARTICLE_TITLE);
                final int articleType = article.optInt(Article.ARTICLE_TYPE);
                final int articlePerfect = article.optInt(Article.ARTICLE_PERFECT);

                final JSONObject commentedNotification = new JSONObject();
                commentedNotification.put(Keys.OBJECT_ID, notification.optString(Keys.OBJECT_ID));
                commentedNotification.put(Comment.COMMENT_T_AUTHOR_NAME, comment.optString(Comment.COMMENT_T_AUTHOR_NAME));
                commentedNotification.put(Comment.COMMENT_CONTENT, comment.optString(Comment.COMMENT_CONTENT));
                commentedNotification.put(Comment.COMMENT_T_AUTHOR_THUMBNAIL_URL,
                        comment.optString(Comment.COMMENT_T_AUTHOR_THUMBNAIL_URL));
                commentedNotification.put(Common.THUMBNAIL_UPDATE_TIME, comment.optJSONObject(Comment.COMMENT_T_COMMENTER).
                        optLong(UserExt.USER_UPDATE_TIME));
                commentedNotification.put(Comment.COMMENT_T_ARTICLE_TITLE, Emotions.convert(articleTitle));
                commentedNotification.put(Comment.COMMENT_T_ARTICLE_TYPE, articleType);
                commentedNotification.put(Comment.COMMENT_CREATE_TIME, comment.opt(Comment.COMMENT_CREATE_TIME));
                commentedNotification.put(Notification.NOTIFICATION_HAS_READ, notification.optBoolean(Notification.NOTIFICATION_HAS_READ));
                commentedNotification.put(Comment.COMMENT_T_ARTICLE_PERFECT, articlePerfect);
                final String articleId = comment.optString(Comment.COMMENT_ON_ARTICLE_ID);
                final int cmtPage = commentQueryService.getCommentPage(articleId, commentId, cmtViewMode, Symphonys.getInt("articleCommentsPageSize"));
                commentedNotification.put(Comment.COMMENT_SHARP_URL, "/article/" + articleId + "?p=" + cmtPage
                        + "&m=" + cmtViewMode + "#" + commentId);

                rslts.add(commentedNotification);
            }

            return ret;
        } catch (final MapperException e) {
            LOGGER.error( "Gets [commented] notifications", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets 'reply' type notifications with the specified user id, current page number and page size.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param userId         the specified user id
     * @param currentPageNum the specified page number
     * @param pageSize       the specified page size
     * @return result json object, for example,      <pre>
     * {
     *     "paginationRecordCount": int,
     *     "rslts": java.util.List[{
     *         "oId": "", // notification record id
     *         "commentAuthorName": "",
     *         "commentContent": "",
     *         "commentAuthorThumbnailURL": "",
     *         "commentArticleTitle": "",
     *         "commentArticleType": int,
     *         "commentSharpURL": "",
     *         "commentCreateTime": java.util.Date,
     *         "hasRead": boolean
     *     }, ....]
     * }
     * </pre>
     * @throws ServiceException service exception
     */
    public JSONObject getReplyNotifications(final int avatarViewMode,
                                            final String userId, final int currentPageNum, final int pageSize) throws ServiceException {
        final JSONObject ret = new JSONObject();
        final List<JSONObject> rslts = new ArrayList<>();

        ret.put(Keys.RESULTS, (Object) rslts);

        final List<Filter> filters = new ArrayList<>();
        filters.add(new PropertyFilter(Notification.NOTIFICATION_USER_ID, FilterOperator.EQUAL, userId));
        filters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, Notification.DATA_TYPE_C_REPLY));

        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize).
                setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters)).
                addSort(Notification.NOTIFICATION_HAS_READ, SortDirection.ASCENDING).
                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);

        try {
            final JSONObject queryResult = notificationMapper.get(query);
            final JSONArray results = queryResult.optJSONArray(Keys.RESULTS);

            ret.put(Pagination.PAGINATION_RECORD_COUNT,
                    queryResult.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_RECORD_COUNT));

            for (int i = 0; i < results.length(); i++) {
                final JSONObject notification = results.optJSONObject(i);
                final String commentId = notification.optString(Notification.NOTIFICATION_DATA_ID);

                final JSONObject comment = commentQueryService.getCommentById(avatarViewMode, commentId);

                final Query q = new Query().setPageCount(1).
                        addProjection(Article.ARTICLE_PERFECT, Integer.class).
                        addProjection(Article.ARTICLE_TITLE, String.class).
                        addProjection(Article.ARTICLE_TYPE, Integer.class).
                        setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.EQUAL,
                                comment.optString(Comment.COMMENT_ON_ARTICLE_ID)));
                final JSONArray rlts = articleMapper.get(q).optJSONArray(Keys.RESULTS);
                final JSONObject article = rlts.optJSONObject(0);
                final String articleTitle = article.optString(Article.ARTICLE_TITLE);
                final int articleType = article.optInt(Article.ARTICLE_TYPE);
                final int articlePerfect = article.optInt(Article.ARTICLE_PERFECT);

                final JSONObject replyNotification = new JSONObject();
                replyNotification.put(Keys.OBJECT_ID, notification.optString(Keys.OBJECT_ID));
                replyNotification.put(Comment.COMMENT_T_AUTHOR_NAME, comment.optString(Comment.COMMENT_T_AUTHOR_NAME));
                replyNotification.put(Comment.COMMENT_CONTENT, comment.optString(Comment.COMMENT_CONTENT));
                replyNotification.put(Comment.COMMENT_T_AUTHOR_THUMBNAIL_URL, comment.optString(Comment.COMMENT_T_AUTHOR_THUMBNAIL_URL));
                replyNotification.put(Common.THUMBNAIL_UPDATE_TIME, comment.optJSONObject(Comment.COMMENT_T_COMMENTER).optLong(UserExt.USER_UPDATE_TIME));
                replyNotification.put(Comment.COMMENT_T_ARTICLE_TITLE, Emotions.convert(articleTitle));
                replyNotification.put(Comment.COMMENT_T_ARTICLE_TYPE, articleType);
                replyNotification.put(Comment.COMMENT_SHARP_URL, comment.optString(Comment.COMMENT_SHARP_URL));
                replyNotification.put(Comment.COMMENT_CREATE_TIME, comment.opt(Comment.COMMENT_CREATE_TIME));
                replyNotification.put(Notification.NOTIFICATION_HAS_READ, notification.optBoolean(Notification.NOTIFICATION_HAS_READ));
                replyNotification.put(Comment.COMMENT_T_ARTICLE_PERFECT, articlePerfect);
                replyNotification.put(Notification.NOTIFICATION_DATA_TYPE, Notification.DATA_TYPE_C_REPLY);
                final String articleId = comment.optString(Comment.COMMENT_ON_ARTICLE_ID);
                replyNotification.put(Article.ARTICLE_T_ID, articleId);
                replyNotification.put(Comment.COMMENT_T_ID, comment.optString(Keys.OBJECT_ID));

                rslts.add(replyNotification);
            }

            return ret;
        } catch (final MapperException e) {
            LOGGER.error( "Gets [reply] notifications", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets 'at' type notifications with the specified user id, current page number and page size.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param userId         the specified user id
     * @param currentPageNum the specified page number
     * @param pageSize       the specified page size
     * @return result json object, for example,      <pre>
     * {
     *     "paginationRecordCount": int,
     *     "rslts": java.util.List[{
     *         "oId": "", // notification record id
     *         "authorName": "",
     *         "content": "",
     *         "thumbnailURL": "",
     *         "articleTitle": "",
     *         "articleType": int,
     *         "url": "",
     *         "createTime": java.util.Date,
     *         "hasRead": boolean,
     *         "atInArticle": boolean,
     *         "isAt": boolean,
     *         "articleTags": "", // if atInArticle is true
     *         "articleTagObjs": [{}, ....], // if atInArticle is true
     *         "articleCommentCnt": int // if atInArticle is true
     *     }, ....]
     * }
     * </pre>
     * @throws ServiceException service exception
     */
    public JSONObject getAtNotifications(final int avatarViewMode,
                                         final String userId, final int currentPageNum, final int pageSize) throws ServiceException {
        final JSONObject ret = new JSONObject();
        final List<JSONObject> rslts = new ArrayList<>();

        ret.put(Keys.RESULTS, (Object) rslts);

        final List<Filter> filters = new ArrayList<>();
        filters.add(new PropertyFilter(Notification.NOTIFICATION_USER_ID, FilterOperator.EQUAL, userId));

        final List<Filter> subFilters = new ArrayList<>();
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
                Notification.DATA_TYPE_C_AT));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
                Notification.DATA_TYPE_C_ARTICLE_NEW_FOLLOWER));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
                Notification.DATA_TYPE_C_ARTICLE_NEW_WATCHER));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
                Notification.DATA_TYPE_C_COMMENT_VOTE_UP));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
                Notification.DATA_TYPE_C_COMMENT_VOTE_DOWN));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
                Notification.DATA_TYPE_C_ARTICLE_VOTE_UP));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
                Notification.DATA_TYPE_C_ARTICLE_VOTE_DOWN));

        filters.add(new CompositeFilter(CompositeFilterOperator.OR, subFilters));

        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize).
                setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters)).
                addSort(Notification.NOTIFICATION_HAS_READ, SortDirection.ASCENDING).
                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);

        try {
            final JSONObject queryResult = notificationMapper.get(query);
            final JSONArray results = queryResult.optJSONArray(Keys.RESULTS);

            ret.put(Pagination.PAGINATION_RECORD_COUNT,
                    queryResult.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_RECORD_COUNT));

            for (int i = 0; i < results.length(); i++) {
                final JSONObject notification = results.optJSONObject(i);
                final int dataType = notification.optInt(Notification.NOTIFICATION_DATA_TYPE);
                final String dataId = notification.optString(Notification.NOTIFICATION_DATA_ID);

                final JSONObject atNotification = new JSONObject();
                atNotification.put(Notification.NOTIFICATION_DATA_TYPE, dataType);
                String description = "";

                atNotification.put(Keys.OBJECT_ID, notification.optString(Keys.OBJECT_ID));
                atNotification.put(Notification.NOTIFICATION_HAS_READ, notification.optBoolean(Notification.NOTIFICATION_HAS_READ));
                atNotification.put(Common.CREATE_TIME, new Date(notification.optLong(Keys.OBJECT_ID)));

                switch (dataType) {
                    case Notification.DATA_TYPE_C_AT:
                        final JSONObject comment = commentQueryService.getCommentById(avatarViewMode, dataId);
                        if (null != comment) {
                            final Query q = new Query().setPageCount(1).
                                    addProjection(Article.ARTICLE_PERFECT, Integer.class).
                                    addProjection(Article.ARTICLE_TITLE, String.class).
                                    addProjection(Article.ARTICLE_TYPE, Integer.class).
                                    setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.EQUAL,
                                            comment.optString(Comment.COMMENT_ON_ARTICLE_ID)));
                            final JSONArray rlts = articleMapper.get(q).optJSONArray(Keys.RESULTS);
                            final JSONObject article = rlts.optJSONObject(0);
                            final String articleTitle = article.optString(Article.ARTICLE_TITLE);
                            final int articleType = article.optInt(Article.ARTICLE_TYPE);
                            final int articlePerfect = article.optInt(Article.ARTICLE_PERFECT);

                            atNotification.put(Common.AUTHOR_NAME, comment.optString(Comment.COMMENT_T_AUTHOR_NAME));
                            atNotification.put(Common.CONTENT, comment.optString(Comment.COMMENT_CONTENT));
                            atNotification.put(Common.THUMBNAIL_URL,
                                    comment.optString(Comment.COMMENT_T_AUTHOR_THUMBNAIL_URL));
                            atNotification.put(Common.THUMBNAIL_UPDATE_TIME, comment.optJSONObject(Comment.COMMENT_T_COMMENTER).
                                    optLong(UserExt.USER_UPDATE_TIME));
                            atNotification.put(Article.ARTICLE_TITLE, Emotions.convert(articleTitle));
                            atNotification.put(Article.ARTICLE_TYPE, articleType);
                            atNotification.put(Common.URL, comment.optString(Comment.COMMENT_SHARP_URL));
                            atNotification.put(Common.CREATE_TIME, comment.opt(Comment.COMMENT_CREATE_TIME));
                            atNotification.put(Notification.NOTIFICATION_T_AT_IN_ARTICLE, false);
                            atNotification.put(Article.ARTICLE_PERFECT, articlePerfect);
                            atNotification.put(Article.ARTICLE_T_ID, comment.optString(Comment.COMMENT_ON_ARTICLE_ID));
                            atNotification.put(Comment.COMMENT_T_ID, comment.optString(Keys.OBJECT_ID));

                            rslts.add(atNotification);
                        } else { // The 'at' in article content
                            final JSONObject article = articleMapper.get(dataId);

                            final String articleAuthorId = article.optString(Article.ARTICLE_AUTHOR_ID);
                            final JSONObject articleAuthor = userMapper.get(articleAuthorId);

                            atNotification.put(Common.AUTHOR_NAME, articleAuthor.optString(User.USER_NAME));
                            atNotification.put(Common.CONTENT, "");
                            final String thumbnailURL = avatarQueryService.getAvatarURLByUser(avatarViewMode, articleAuthor, "48");
                            atNotification.put(Common.THUMBNAIL_URL, thumbnailURL);
                            atNotification.put(Common.THUMBNAIL_UPDATE_TIME, articleAuthor.optLong(UserExt.USER_UPDATE_TIME));
                            atNotification.put(Article.ARTICLE_TITLE, Emotions.convert(article.optString(Article.ARTICLE_TITLE)));
                            atNotification.put(Article.ARTICLE_TYPE, article.optInt(Article.ARTICLE_TYPE));
                            atNotification.put(Common.URL,  SpringUtil.getServerPath() + article.optString(Article.ARTICLE_PERMALINK));
                            atNotification.put(Common.CREATE_TIME, new Date(article.optLong(Article.ARTICLE_CREATE_TIME)));
                            atNotification.put(Notification.NOTIFICATION_T_AT_IN_ARTICLE, true);

                            final String tagsStr = article.optString(Article.ARTICLE_TAGS);
                            atNotification.put(Article.ARTICLE_TAGS, tagsStr);
                            final List<JSONObject> tags = buildTagObjs(tagsStr);
                            atNotification.put(Article.ARTICLE_T_TAG_OBJS, (Object) tags);

                            atNotification.put(Article.ARTICLE_COMMENT_CNT, article.optInt(Article.ARTICLE_COMMENT_CNT));
                            atNotification.put(Article.ARTICLE_PERFECT, article.optInt(Article.ARTICLE_PERFECT));
                            atNotification.put(Article.ARTICLE_T_ID, article.optString(Keys.OBJECT_ID));

                            rslts.add(atNotification);
                        }

                        break;
                    case Notification.DATA_TYPE_C_ARTICLE_NEW_FOLLOWER:
                    case Notification.DATA_TYPE_C_ARTICLE_NEW_WATCHER:
                        final String articleId = dataId.split("-")[0];
                        final String followerUserId = dataId.split("-")[1];

                        final JSONObject article = articleMapper.get(articleId);
                        if (null == article) {
                            description = langPropsService.get("removedLabel");
                            atNotification.put(Common.DESCRIPTION, description);

                            rslts.add(atNotification);

                            continue;
                        }

                        if (Notification.DATA_TYPE_C_ARTICLE_NEW_FOLLOWER == dataType) {
                            description = langPropsService.get("notificationArticleNewFollowerLabel");
                        } else if (Notification.DATA_TYPE_C_ARTICLE_NEW_WATCHER == dataType) {
                            description = langPropsService.get("notificationArticleNewWatcherLabel");
                        }

                        final JSONObject followerUser = userMapper.get(followerUserId);
                        final String followerUserName = followerUser.optString(User.USER_NAME);
                        atNotification.put(User.USER_NAME, followerUserName);

                        final String thumbnailURL = avatarQueryService.getAvatarURLByUser(avatarViewMode, followerUser, "48");
                        atNotification.put(Common.THUMBNAIL_URL, thumbnailURL);
                        atNotification.put(Common.THUMBNAIL_UPDATE_TIME, followerUser.optLong(UserExt.USER_UPDATE_TIME));

                        final String userLink = UserExt.getUserLink(followerUserName);
                        description = description.replace("{user}", userLink);

                        final String articleLink = " <a href=\"" +  SpringUtil.getServerPath() + article.optString(Article.ARTICLE_PERMALINK) + "\">"
                                + Emotions.convert(article.optString(Article.ARTICLE_TITLE)) + "</a>";
                        description = description.replace("{article}", articleLink);

                        atNotification.put(Common.DESCRIPTION, description);

                        rslts.add(atNotification);

                        break;
                    case Notification.DATA_TYPE_C_COMMENT_VOTE_UP:
                    case Notification.DATA_TYPE_C_COMMENT_VOTE_DOWN:
                        final JSONObject user = userMapper.get(userId);
                        final int cmtViewMode = user.optInt(UserExt.USER_COMMENT_VIEW_MODE);
                        final String commentId = dataId.split("-")[0];
                        final String cmtVoterId = dataId.split("-")[1];
                        final JSONObject cmtVoter = userMapper.get(cmtVoterId);
                        String voterUserName = cmtVoter.optString(User.USER_NAME);
                        atNotification.put(User.USER_NAME, voterUserName);
                        String thumbnailURLVote = avatarQueryService.getAvatarURLByUser(avatarViewMode, cmtVoter, "48");
                        atNotification.put(Common.THUMBNAIL_URL, thumbnailURLVote);
                        atNotification.put(Common.THUMBNAIL_UPDATE_TIME, cmtVoter.optLong(UserExt.USER_UPDATE_TIME));

                        JSONObject articleVote = null;
                        if (Notification.DATA_TYPE_C_COMMENT_VOTE_UP == dataType) {
                            description = langPropsService.get("notificationCommentVoteUpLabel");
                            articleVote = commentMapper.get(commentId);
                            if (null == articleVote) {
                                description = langPropsService.get("removedLabel");
                                atNotification.put(Common.DESCRIPTION, description);
                                rslts.add(atNotification);

                                continue;
                            }

                            articleVote = articleMapper.get(articleVote.optString(Comment.COMMENT_ON_ARTICLE_ID));
                        } else if (Notification.DATA_TYPE_C_COMMENT_VOTE_DOWN == dataType) {
                            description = langPropsService.get("notificationCommentVoteDownLabel");
                            articleVote = commentMapper.get(commentId);
                            if (null == articleVote) {
                                description = langPropsService.get("removedLabel");
                                atNotification.put(Common.DESCRIPTION, description);
                                rslts.add(atNotification);

                                continue;
                            }

                            articleVote = articleMapper.get(articleVote.optString(Comment.COMMENT_ON_ARTICLE_ID));
                        }
                        if (null == articleVote) {
                            description = langPropsService.get("removedLabel");
                            atNotification.put(Common.DESCRIPTION, description);
                            rslts.add(atNotification);

                            continue;
                        }

                        String userLinkVote = UserExt.getUserLink(voterUserName);
                        description = description.replace("{user}", userLinkVote);
                        final String cmtVoteURL = commentQueryService.getCommentURL(commentId, cmtViewMode, Symphonys.getInt("articleCommentsPageSize"));
                        atNotification.put(Common.DESCRIPTION, description.replace("{article}", Emotions.convert(cmtVoteURL)));
                        rslts.add(atNotification);

                        break;
                    case Notification.DATA_TYPE_C_ARTICLE_VOTE_UP:
                    case Notification.DATA_TYPE_C_ARTICLE_VOTE_DOWN:
                        final String voteArticleId = dataId.split("-")[0];
                        final String voterId = dataId.split("-")[1];
                        final JSONObject voter = userMapper.get(voterId);
                        voterUserName = voter.optString(User.USER_NAME);
                        atNotification.put(User.USER_NAME, voterUserName);
                        thumbnailURLVote = avatarQueryService.getAvatarURLByUser(avatarViewMode, voter, "48");
                        atNotification.put(Common.THUMBNAIL_URL, thumbnailURLVote);
                        atNotification.put(Common.THUMBNAIL_UPDATE_TIME, voter.optLong(UserExt.USER_UPDATE_TIME));

                        JSONObject voteArticle = null;
                        if (Notification.DATA_TYPE_C_ARTICLE_VOTE_UP == dataType) {
                            description = langPropsService.get("notificationArticleVoteUpLabel");
                            voteArticle = articleMapper.get(voteArticleId);
                        } else if (Notification.DATA_TYPE_C_ARTICLE_VOTE_DOWN == dataType) {
                            description = langPropsService.get("notificationArticleVoteDownLabel");
                            voteArticle = articleMapper.get(voteArticleId);
                        }

                        if (null == voteArticle) {
                            description = langPropsService.get("removedLabel");
                            atNotification.put(Common.DESCRIPTION, description);
                            rslts.add(atNotification);

                            continue;
                        }

                        userLinkVote = UserExt.getUserLink(voterUserName);
                        description = description.replace("{user}", userLinkVote);
                        final String articleLinkVote = " <a href=\"" +  SpringUtil.getServerPath() + voteArticle.optString(Article.ARTICLE_PERMALINK) + "\">"
                                + Emotions.convert(voteArticle.optString(Article.ARTICLE_TITLE)) + "</a>";
                        description = description.replace("{article}", articleLinkVote);
                        atNotification.put(Common.DESCRIPTION, description);
                        rslts.add(atNotification);

                        break;
                }
            }

            return ret;
        } catch (final MapperException e) {
            LOGGER.error( "Gets [at] notifications", e);

            throw new ServiceException(e);
        }
    }

    /**
     * Gets 'following' type notifications with the specified user id, current page number and page size.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param userId         the specified user id
     * @param currentPageNum the specified page number
     * @param pageSize       the specified page size
     * @return result json object, for example,      <pre>
     * {
     *     "paginationRecordCount": int,
     *     "rslts": java.util.List[{
     *         "oId": "", // notification record id
     *         "authorName": "",
     *         "content": "",
     *         "thumbnailURL": "",
     *         "articleTitle": "",
     *         "articleType": int,
     *         "url": "",
     *         "createTime": java.util.Date,
     *         "hasRead": boolean,
     *         "type": "", // article/comment
     *         "articleTags": "", // if atInArticle is true
     *         "articleTagObjs": [{}, ....], // if atInArticle is true
     *         "articleCommentCnt": int // if atInArticle is true
     *     }, ....]
     * }
     * </pre>
     * @throws ServiceException service exception
     */
    public JSONObject getFollowingNotifications(final int avatarViewMode,
                                                final String userId, final int currentPageNum, final int pageSize) throws ServiceException {
        final JSONObject ret = new JSONObject();
        final List<JSONObject> rslts = new ArrayList<>();

        ret.put(Keys.RESULTS, (Object) rslts);

        final List<Filter> filters = new ArrayList<>();
        filters.add(new PropertyFilter(Notification.NOTIFICATION_USER_ID, FilterOperator.EQUAL, userId));

        final List<Filter> subFilters = new ArrayList<>();
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
                Notification.DATA_TYPE_C_FOLLOWING_USER));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
                Notification.DATA_TYPE_C_FOLLOWING_ARTICLE_UPDATE));
        subFilters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
                Notification.DATA_TYPE_C_FOLLOWING_ARTICLE_COMMENT));

        filters.add(new CompositeFilter(CompositeFilterOperator.OR, subFilters));

        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize).
                setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters)).
                addSort(Notification.NOTIFICATION_HAS_READ, SortDirection.ASCENDING).
                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);

        try {
            final JSONObject queryResult = notificationMapper.get(query);
            final JSONArray results = queryResult.optJSONArray(Keys.RESULTS);

            ret.put(Pagination.PAGINATION_RECORD_COUNT,
                    queryResult.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_RECORD_COUNT));

            for (int i = 0; i < results.length(); i++) {
                final JSONObject notification = results.optJSONObject(i);
                final String commentId = notification.optString(Notification.NOTIFICATION_DATA_ID);
                final int dataType = notification.optInt(Notification.NOTIFICATION_DATA_TYPE);
                final JSONObject followingNotification = new JSONObject();
                followingNotification.put(Notification.NOTIFICATION_DATA_TYPE, dataType);

                switch (dataType) {
                    case Notification.DATA_TYPE_C_FOLLOWING_ARTICLE_COMMENT:
                        final JSONObject comment = commentQueryService.getCommentById(avatarViewMode, commentId);
                        final Query q = new Query().setPageCount(1).
                                addProjection(Article.ARTICLE_PERFECT, Integer.class).
                                addProjection(Article.ARTICLE_TITLE, String.class).
                                addProjection(Article.ARTICLE_TYPE, Integer.class).
                                setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.EQUAL,
                                        comment.optString(Comment.COMMENT_ON_ARTICLE_ID)));
                        final JSONArray rlts = articleMapper.get(q).optJSONArray(Keys.RESULTS);
                        JSONObject article = rlts.optJSONObject(0);
                        final String articleTitle = article.optString(Article.ARTICLE_TITLE);
                        final int articleType = article.optInt(Article.ARTICLE_TYPE);
                        final int articlePerfect = article.optInt(Article.ARTICLE_PERFECT);

                        followingNotification.put(Keys.OBJECT_ID, notification.optString(Keys.OBJECT_ID));
                        followingNotification.put(Common.AUTHOR_NAME, comment.optString(Comment.COMMENT_T_AUTHOR_NAME));
                        followingNotification.put(Common.CONTENT, comment.optString(Comment.COMMENT_CONTENT));
                        followingNotification.put(Common.THUMBNAIL_URL,
                                comment.optString(Comment.COMMENT_T_AUTHOR_THUMBNAIL_URL));
                        followingNotification.put(Common.THUMBNAIL_UPDATE_TIME, comment.optJSONObject(Comment.COMMENT_T_COMMENTER).
                                optLong(UserExt.USER_UPDATE_TIME));
                        followingNotification.put(Article.ARTICLE_TITLE, Emotions.convert(articleTitle));
                        followingNotification.put(Article.ARTICLE_TYPE, articleType);
                        followingNotification.put(Common.URL, comment.optString(Comment.COMMENT_SHARP_URL));
                        followingNotification.put(Common.CREATE_TIME, comment.opt(Comment.COMMENT_CREATE_TIME));
                        followingNotification.put(Notification.NOTIFICATION_HAS_READ, notification.optBoolean(Notification.NOTIFICATION_HAS_READ));
                        followingNotification.put(Notification.NOTIFICATION_T_IS_COMMENT, true);
                        followingNotification.put(Article.ARTICLE_PERFECT, articlePerfect);

                        rslts.add(followingNotification);

                        break;
                    case Notification.DATA_TYPE_C_FOLLOWING_USER:
                    case Notification.DATA_TYPE_C_FOLLOWING_ARTICLE_UPDATE:
                        article = articleMapper.get(commentId);

                        final String articleAuthorId = article.optString(Article.ARTICLE_AUTHOR_ID);
                        final JSONObject articleAuthor = userMapper.get(articleAuthorId);

                        followingNotification.put(Keys.OBJECT_ID, notification.optString(Keys.OBJECT_ID));
                        followingNotification.put(Common.AUTHOR_NAME, articleAuthor.optString(User.USER_NAME));
                        followingNotification.put(Common.CONTENT, "");
                        final String thumbnailURL = avatarQueryService.getAvatarURLByUser(avatarViewMode, articleAuthor, "48");
                        followingNotification.put(Common.THUMBNAIL_URL, thumbnailURL);
                        followingNotification.put(Common.THUMBNAIL_UPDATE_TIME, articleAuthor.optLong(UserExt.USER_UPDATE_TIME));
                        followingNotification.put(Article.ARTICLE_TITLE, Emotions.convert(article.optString(Article.ARTICLE_TITLE)));
                        followingNotification.put(Article.ARTICLE_TYPE, article.optInt(Article.ARTICLE_TYPE));
                        followingNotification.put(Common.URL,  SpringUtil.getServerPath() + article.optString(Article.ARTICLE_PERMALINK));
                        followingNotification.put(Common.CREATE_TIME, new Date(article.optLong(Article.ARTICLE_CREATE_TIME)));
                        followingNotification.put(Notification.NOTIFICATION_HAS_READ, notification.optBoolean(Notification.NOTIFICATION_HAS_READ));
                        followingNotification.put(Notification.NOTIFICATION_T_IS_COMMENT, false);

                        final String tagsStr = article.optString(Article.ARTICLE_TAGS);
                        followingNotification.put(Article.ARTICLE_TAGS, tagsStr);
                        final List<JSONObject> tags = buildTagObjs(tagsStr);
                        followingNotification.put(Article.ARTICLE_T_TAG_OBJS, (Object) tags);

                        followingNotification.put(Article.ARTICLE_COMMENT_CNT, article.optInt(Article.ARTICLE_COMMENT_CNT));
                        followingNotification.put(Article.ARTICLE_PERFECT, article.optInt(Article.ARTICLE_PERFECT));

                        rslts.add(followingNotification);

                        break;
                }
            }

            return ret;
        } catch (final MapperException e) {
            LOGGER.error( "Gets [following] notifications", e);

            throw new ServiceException(e);
        }
    }

    /**
     * Gets 'broadcast' type notifications with the specified user id, current page number and page size.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param userId         the specified user id
     * @param currentPageNum the specified page number
     * @param pageSize       the specified page size
     * @return result json object, for example,      <pre>
     * {
     *     "paginationRecordCount": int,
     *     "rslts": java.util.List[{
     *         "oId": "", // notification record id
     *         "authorName": "",
     *         "content": "",
     *         "thumbnailURL": "",
     *         "articleTitle": "",
     *         "articleType": int,
     *         "articleTags": "",
     *         "articleTagObjs": [{}, ....],
     *         "articleCommentCnt": int,
     *         "url": "",
     *         "createTime": java.util.Date,
     *         "hasRead": boolean,
     *         "type": "", // article/comment
     *     }, ....]
     * }
     * </pre>
     * @throws ServiceException service exception
     */
    public JSONObject getBroadcastNotifications(final int avatarViewMode,
                                                final String userId, final int currentPageNum, final int pageSize) throws ServiceException {
        final JSONObject ret = new JSONObject();
        final List<JSONObject> rslts = new ArrayList<>();

        ret.put(Keys.RESULTS, (Object) rslts);

        final List<Filter> filters = new ArrayList<>();
        filters.add(new PropertyFilter(Notification.NOTIFICATION_USER_ID, FilterOperator.EQUAL, userId));
        filters.add(new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, Notification.DATA_TYPE_C_BROADCAST));

        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize).
                setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters)).
                addSort(Notification.NOTIFICATION_HAS_READ, SortDirection.ASCENDING).
                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);

        try {
            final JSONObject queryResult = notificationMapper.get(query);
            final JSONArray results = queryResult.optJSONArray(Keys.RESULTS);

            ret.put(Pagination.PAGINATION_RECORD_COUNT,
                    queryResult.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_RECORD_COUNT));

            for (int i = 0; i < results.length(); i++) {
                final JSONObject notification = results.optJSONObject(i);
                final String articleId = notification.optString(Notification.NOTIFICATION_DATA_ID);

                final Query q = new Query().setPageCount(1).
                        addProjection(Article.ARTICLE_TITLE, String.class).
                        addProjection(Article.ARTICLE_TYPE, Integer.class).
                        addProjection(Article.ARTICLE_AUTHOR_ID, String.class).
                        addProjection(Article.ARTICLE_PERMALINK, String.class).
                        addProjection(Article.ARTICLE_CREATE_TIME, Long.class).
                        addProjection(Article.ARTICLE_TAGS, String.class).
                        addProjection(Article.ARTICLE_COMMENT_CNT, Integer.class).
                        addProjection(Article.ARTICLE_PERFECT, Integer.class).
                        setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.EQUAL, articleId));
                final JSONArray rlts = articleMapper.get(q).optJSONArray(Keys.RESULTS);
                final JSONObject article = rlts.optJSONObject(0);

                if (null == article) {
                    LOGGER.warn("Not found article[id=" + articleId + ']');

                    continue;
                }

                final String articleTitle = article.optString(Article.ARTICLE_TITLE);
                final String articleAuthorId = article.optString(Article.ARTICLE_AUTHOR_ID);
                final JSONObject author = userMapper.get(articleAuthorId);

                if (null == author) {
                    LOGGER.warn("Not found user[id=" + articleAuthorId + ']');

                    continue;
                }

                final JSONObject broadcastNotification = new JSONObject();
                broadcastNotification.put(Keys.OBJECT_ID, notification.optString(Keys.OBJECT_ID));
                broadcastNotification.put(Common.AUTHOR_NAME, author.optString(User.USER_NAME));
                broadcastNotification.put(Common.CONTENT, "");
                broadcastNotification.put(Common.THUMBNAIL_URL,
                        avatarQueryService.getAvatarURLByUser(avatarViewMode, author, "48"));
                broadcastNotification.put(Common.THUMBNAIL_UPDATE_TIME, author.optLong(UserExt.USER_UPDATE_TIME));
                broadcastNotification.put(Article.ARTICLE_TITLE, Emotions.convert(articleTitle));
                broadcastNotification.put(Common.URL,  SpringUtil.getServerPath() + article.optString(Article.ARTICLE_PERMALINK));
                broadcastNotification.put(Common.CREATE_TIME, new Date(article.optLong(Article.ARTICLE_CREATE_TIME)));
                broadcastNotification.put(Notification.NOTIFICATION_HAS_READ,
                        notification.optBoolean(Notification.NOTIFICATION_HAS_READ));
                broadcastNotification.put(Common.TYPE, Article.ARTICLE);
                broadcastNotification.put(Article.ARTICLE_TYPE, article.optInt(Article.ARTICLE_TYPE));

                final String tagsStr = article.optString(Article.ARTICLE_TAGS);
                broadcastNotification.put(Article.ARTICLE_TAGS, tagsStr);
                final List<JSONObject> tags = buildTagObjs(tagsStr);
                broadcastNotification.put(Article.ARTICLE_T_TAG_OBJS, (Object) tags);

                broadcastNotification.put(Article.ARTICLE_COMMENT_CNT, article.optInt(Article.ARTICLE_COMMENT_CNT));
                broadcastNotification.put(Article.ARTICLE_PERFECT, article.optInt(Article.ARTICLE_PERFECT));

                rslts.add(broadcastNotification);
            }

            return ret;
        } catch (final MapperException e) {
            LOGGER.error( "Gets [broadcast] notifications", e);

            throw new ServiceException(e);
        }
    }

    /**
     * Builds tag objects with the specified tags string.
     *
     * @param tagsStr the specified tags string
     * @return tag objects
     */
    private List<JSONObject> buildTagObjs(final String tagsStr) {
        final List<JSONObject> ret = new ArrayList<>();

        final String[] tagTitles = tagsStr.split(",");
        for (final String tagTitle : tagTitles) {
            final JSONObject tag = new JSONObject();
            tag.put(Tag.TAG_TITLE, tagTitle);

            final String uri = tagMapper.getURIByTitle(tagTitle);
            if (null != uri) {
                tag.put(Tag.TAG_URI, uri);
            } else {
                tag.put(Tag.TAG_URI, tagTitle);

                tagMapper.getURIByTitle(tagTitle);
            }

            ret.add(tag);
        }

        return ret;
    }
}
