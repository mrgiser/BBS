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

import cn.he.zhao.bbs.entityUtil.*;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.entityUtil.my.Pagination;
import cn.he.zhao.bbs.entityUtil.my.User;
import cn.he.zhao.bbs.service.interf.LangPropsService;
import cn.he.zhao.bbs.spring.Common;
import cn.he.zhao.bbs.spring.SpringUtil;
import cn.he.zhao.bbs.spring.Stopwatchs;
import cn.he.zhao.bbs.util.Emotions;
import cn.he.zhao.bbs.util.JsonUtil;
import cn.he.zhao.bbs.util.Symphonys;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.aspectj.weaver.ast.Not;
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
 * NotificationUtil query service.
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
     * NotificationUtil Mapper.
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
     * RewardUtil Mapper.
     */
    @Autowired
    private RewardMapper rewardMapper;

    /**
     * Tag Mapper.
     */
    @Autowired
    private TagMapper tagMapper;

    /**
     * PointtransferUtil Mapper.
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
     * RoleUtil query service.
     */
    @Autowired
    private RoleQueryService roleQueryService;

    /**
     * Gets a notification by the specified id.
     *
     * @param notificationId the specified id
     * @return notification, returns {@code null} if not found
     */
    public Notification getNotification(final String notificationId) {
        try {
            return notificationMapper.getByOId(notificationId);
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
//        final List<Filter> filters = new ArrayList<>();
//        filters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_USER_ID, FilterOperator.EQUAL, userId));
//        filters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_HAS_READ, FilterOperator.EQUAL, false));
//
//        final List<Filter> subFilters = new ArrayList<>();
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
//                NotificationUtil.DATA_TYPE_C_FOLLOWING_ARTICLE_UPDATE));
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
//                NotificationUtil.DATA_TYPE_C_FOLLOWING_ARTICLE_COMMENT));
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
//                NotificationUtil.DATA_TYPE_C_FOLLOWING_USER));
//
//        filters.add(new CompositeFilter(CompositeFilterOperator.OR, subFilters));
//
//        final Query query = new Query().setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));

        try {
            final Integer result = notificationMapper.countByReadAndUserIdAndDataTypes(userId,false,
                    NotificationUtil.DATA_TYPE_C_FOLLOWING_ARTICLE_UPDATE,
                    NotificationUtil.DATA_TYPE_C_FOLLOWING_ARTICLE_COMMENT,
                    NotificationUtil.DATA_TYPE_C_FOLLOWING_USER);

            return result;
//                    .optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_RECORD_COUNT);
        } catch (final Exception e) {
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
//        final List<Filter> filters = new ArrayList<>();
//        filters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_USER_ID, FilterOperator.EQUAL, userId));
//        filters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_HAS_READ, FilterOperator.EQUAL, false));
//
//        final List<Filter> subFilters = new ArrayList<>();
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
//                NotificationUtil.DATA_TYPE_C_SYS_ANNOUNCE_ARTICLE));
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
//                NotificationUtil.DATA_TYPE_C_SYS_ANNOUNCE_NEW_USER));
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
//                NotificationUtil.DATA_TYPE_C_SYS_ANNOUNCE_ROLE_CHANGED));
//
//        filters.add(new CompositeFilter(CompositeFilterOperator.OR, subFilters));
//
//        final Query query = new Query().setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));

        try {
            final Integer result = notificationMapper.countByReadAndUserIdAndDataTypes(userId,false,
                    NotificationUtil.DATA_TYPE_C_SYS_ANNOUNCE_ARTICLE,
                    NotificationUtil.DATA_TYPE_C_SYS_ANNOUNCE_NEW_USER,
                    NotificationUtil.DATA_TYPE_C_SYS_ANNOUNCE_ROLE_CHANGED);

            return result;
//                    .optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_RECORD_COUNT);
        } catch (final Exception e) {
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
     * @throws Exception service exception
     */
    public JSONObject getSysAnnounceNotifications(final int avatarViewMode,
                                                  final String userId, final int currentPageNum, final int pageSize) throws Exception {
        final JSONObject ret = new JSONObject();
        final List<JSONObject> rslts = new ArrayList<>();

        ret.put(Keys.RESULTS, (Object) rslts);

//        final List<Filter> filters = new ArrayList<>();
//        filters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_USER_ID, FilterOperator.EQUAL, userId));
//
//        final List<Filter> subFilters = new ArrayList<>();
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
//                NotificationUtil.DATA_TYPE_C_SYS_ANNOUNCE_ARTICLE));
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
//                NotificationUtil.DATA_TYPE_C_SYS_ANNOUNCE_NEW_USER));
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
//                NotificationUtil.DATA_TYPE_C_SYS_ANNOUNCE_ROLE_CHANGED));
//
//        filters.add(new CompositeFilter(CompositeFilterOperator.OR, subFilters));
//
//        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize).
//                setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters)).
//                addSort(NotificationUtil.NOTIFICATION_HAS_READ, SortDirection.ASCENDING).
//                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);

        PageHelper.startPage(currentPageNum,pageSize,"hasRead ASCE, oId DESC");

        try {
            final PageInfo<Notification> queryResult = new PageInfo<>(notificationMapper.getByReadAndUserIdAndDataTypes(userId,
                    NotificationUtil.DATA_TYPE_C_SYS_ANNOUNCE_ARTICLE,
                    NotificationUtil.DATA_TYPE_C_SYS_ANNOUNCE_NEW_USER,
                    NotificationUtil.DATA_TYPE_C_SYS_ANNOUNCE_ROLE_CHANGED));

            final List<JSONObject> results = JsonUtil.listToJSONList(queryResult.getList());

            ret.put(Pagination.PAGINATION_RECORD_COUNT, queryResult.getTotal());

            for (int i = 0; i < results.size(); i++) {
                final JSONObject notification = results.get(i);
                final String dataId = notification.optString(NotificationUtil.NOTIFICATION_DATA_ID);
                final int dataType = notification.optInt(NotificationUtil.NOTIFICATION_DATA_TYPE);
                String desTemplate = "";

                switch (dataType) {
                    case NotificationUtil.DATA_TYPE_C_SYS_ANNOUNCE_NEW_USER:
                        desTemplate = langPropsService.get("notificationSysNewUser1Label");

                        break;
                    case NotificationUtil.DATA_TYPE_C_SYS_ANNOUNCE_ARTICLE:
                        desTemplate = langPropsService.get("notificationSysArticleLabel");

                        final Article article15 = articleMapper.get(dataId);
                        if (null == article15) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String articleLink15 = "<a href=\""
                                +  SpringUtil.getServerPath() + article15.getArticlePermalink() + "\">"
                                + article15.getArticleTitle() + "</a>";
                        desTemplate = desTemplate.replace("{article}", articleLink15);

                        break;
                    case NotificationUtil.DATA_TYPE_C_SYS_ANNOUNCE_ROLE_CHANGED:
                        desTemplate = langPropsService.get("notificationSysRoleChangedLabel");

                        final String oldRoleId = dataId.split("-")[0];
                        final String newRoleId = dataId.split("-")[1];
                        final Role oldRole = roleQueryService.getRole(oldRoleId);
                        final Role newRole = roleQueryService.getRole(newRoleId);

                        desTemplate = desTemplate.replace("{oldRole}", oldRole.getRoleName());
                        desTemplate = desTemplate.replace("{newRole}", newRole.getRoleName());

                        break;
                    default:
                        throw new AssertionError();
                }

                notification.put(Common.DESCRIPTION, desTemplate);
                notification.put(Common.CREATE_TIME, new Date(notification.optLong(Keys.OBJECT_ID)));

                rslts.add(notification);
            }

            return ret;
        } catch (final Exception e) {
            LOGGER.error( "Gets [sys_announce] notifications failed", e);

            throw new Exception(e);
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
//            final Query query = new Query();
//            query.setFilter(CompositeFilterOperator.and(
//                    new PropertyFilter(NotificationUtil.NOTIFICATION_USER_ID, FilterOperator.EQUAL, userId),
//                    new PropertyFilter(NotificationUtil.NOTIFICATION_HAS_READ, FilterOperator.EQUAL, false)
//            ));

            try {
                return (int) notificationMapper.countByReadAndUserId(userId, false);
            } catch (final Exception e) {
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
//        final List<Filter> filters = new ArrayList<>();
//
//        filters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_USER_ID, FilterOperator.EQUAL, userId));
//        filters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_HAS_READ, FilterOperator.EQUAL, false));
//        filters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, notificationDataType));
//
//        final Query query = new Query();
//        query.setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters)).addProjection(Keys.OBJECT_ID, String.class);

        try {
            final int result = notificationMapper.countByReadAndUserIdAndDataType(userId, false,notificationDataType);

//            return result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_RECORD_COUNT);
            return result;
        } catch (final Exception e) {
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
//        final List<Filter> filters = new ArrayList<>();
//        filters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_USER_ID, FilterOperator.EQUAL, userId));
//        filters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_HAS_READ, FilterOperator.EQUAL, false));
//
//        final List<Filter> subFilters = new ArrayList<>();
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, NotificationUtil.DATA_TYPE_C_POINT_ARTICLE_REWARD));
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, ));
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, NotificationUtil.));
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, NotificationUtil.));
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, NotificationUtil.));
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, NotificationUtil.));
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, NotificationUtil.));
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, NotificationUtil.));
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, NotificationUtil.));
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, NotificationUtil.));
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, NotificationUtil.));
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, NotificationUtil.));
//
//        filters.add(new CompositeFilter(CompositeFilterOperator.OR, subFilters));
//
//        final Query query = new Query().setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));
        List<Integer> list = new ArrayList<>();
        list.add(NotificationUtil.DATA_TYPE_C_POINT_ARTICLE_REWARD);
        list.add(NotificationUtil.DATA_TYPE_C_POINT_ARTICLE_THANK);
        list.add(NotificationUtil.DATA_TYPE_C_POINT_CHARGE);
        list.add(NotificationUtil.DATA_TYPE_C_POINT_EXCHANGE);
        list.add(NotificationUtil.DATA_TYPE_C_ABUSE_POINT_DEDUCT);
        list.add(NotificationUtil.DATA_TYPE_C_POINT_COMMENT_THANK);
        list.add(NotificationUtil.DATA_TYPE_C_POINT_COMMENT_ACCEPT);
        list.add(NotificationUtil.DATA_TYPE_C_POINT_TRANSFER);
        list.add(NotificationUtil.DATA_TYPE_C_INVITECODE_USED);
        list.add(NotificationUtil.DATA_TYPE_C_INVITATION_LINK_USED);
        list.add(NotificationUtil.DATA_TYPE_C_POINT_PERFECT_ARTICLE);
        list.add(NotificationUtil.DATA_TYPE_C_POINT_REPORT_HANDLED);
        try {
            final Integer result = notificationMapper.countUnReadAndUserIdAndDataTypes(userId,false,list);

            return result;
//                    .optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_RECORD_COUNT);
        } catch (final Exception e) {
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
     * @throws Exception service exception
     */
    public JSONObject getPointNotifications(final String userId, final int currentPageNum, final int pageSize)
            throws Exception {
        final JSONObject ret = new JSONObject();
        final List<JSONObject> rslts = new ArrayList<>();

        ret.put(Keys.RESULTS, (Object) rslts);

//        final List<Filter> filters = new ArrayList<>();
//        filters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_USER_ID, FilterOperator.EQUAL, userId));
//
//        final List<Filter> subFilters = new ArrayList<>();
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, NotificationUtil.DATA_TYPE_C_POINT_ARTICLE_REWARD));
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, NotificationUtil.DATA_TYPE_C_POINT_ARTICLE_THANK));
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, NotificationUtil.DATA_TYPE_C_POINT_CHARGE));
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, NotificationUtil.DATA_TYPE_C_POINT_EXCHANGE));
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, NotificationUtil.DATA_TYPE_C_ABUSE_POINT_DEDUCT));
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, NotificationUtil.DATA_TYPE_C_POINT_COMMENT_THANK));
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, NotificationUtil.DATA_TYPE_C_POINT_COMMENT_ACCEPT));
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, NotificationUtil.DATA_TYPE_C_POINT_TRANSFER));
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, NotificationUtil.DATA_TYPE_C_INVITECODE_USED));
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, NotificationUtil.DATA_TYPE_C_INVITATION_LINK_USED));
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, NotificationUtil.DATA_TYPE_C_POINT_PERFECT_ARTICLE));
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, NotificationUtil.DATA_TYPE_C_POINT_REPORT_HANDLED));
//
//        filters.add(new CompositeFilter(CompositeFilterOperator.OR, subFilters));

        PageHelper.startPage(currentPageNum,pageSize, "hasRead ASCE, oId DESC");
//        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize).
//                setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters)).
//                addSort(NotificationUtil.NOTIFICATION_HAS_READ, SortDirection.ASCENDING).
//                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);
        List<Integer> list = new ArrayList<>();
        list.add(NotificationUtil.DATA_TYPE_C_POINT_ARTICLE_REWARD);
        list.add(NotificationUtil.DATA_TYPE_C_POINT_ARTICLE_THANK);
        list.add(NotificationUtil.DATA_TYPE_C_POINT_CHARGE);
        list.add(NotificationUtil.DATA_TYPE_C_POINT_EXCHANGE);
        list.add(NotificationUtil.DATA_TYPE_C_ABUSE_POINT_DEDUCT);
        list.add(NotificationUtil.DATA_TYPE_C_POINT_COMMENT_THANK);
        list.add(NotificationUtil.DATA_TYPE_C_POINT_COMMENT_ACCEPT);
        list.add(NotificationUtil.DATA_TYPE_C_POINT_TRANSFER);
        list.add(NotificationUtil.DATA_TYPE_C_INVITECODE_USED);
        list.add(NotificationUtil.DATA_TYPE_C_INVITATION_LINK_USED);
        list.add(NotificationUtil.DATA_TYPE_C_POINT_PERFECT_ARTICLE);
        list.add(NotificationUtil.DATA_TYPE_C_POINT_REPORT_HANDLED);
        try {
            final PageInfo<Notification> queryResult = new PageInfo<>(notificationMapper.getByUserIdAndDataTypes(userId, list));
            final List<JSONObject> results = JsonUtil.listToJSONList(queryResult.getList());

            ret.put(Pagination.PAGINATION_RECORD_COUNT,
                    queryResult.getTotal());

            for (int i = 0; i < results.size(); i++) {
                final JSONObject notification = results.get(i);
                final String dataId = notification.optString(NotificationUtil.NOTIFICATION_DATA_ID);
                final int dataType = notification.optInt(NotificationUtil.NOTIFICATION_DATA_TYPE);
                String desTemplate = "";

                switch (dataType) {
                    case NotificationUtil.DATA_TYPE_C_POINT_ARTICLE_THANK:
                        desTemplate = langPropsService.get("notificationArticleThankLabel");

                        final Reward reward12 = rewardMapper.get(dataId);
                        final String senderId12 = reward12.getSenderId();
                        final UserExt user12 = userMapper.get(senderId12);
                        final String articleId12 = reward12.getDataId();
                        final Article article12 = articleMapper.get(articleId12);
                        if (null == article12) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String userLink12 = UserExtUtil.getUserLink(user12);
                        desTemplate = desTemplate.replace("{user}", userLink12);

                        final String articleLink12 = "<a href=\""
                                +  SpringUtil.getServerPath() + article12.getArticlePermalink() + "\">"
                                + article12.getArticleTitle() + "</a>";
                        desTemplate = desTemplate.replace("{article}", articleLink12);

                        break;
                    case NotificationUtil.DATA_TYPE_C_POINT_ARTICLE_REWARD:
                        desTemplate = langPropsService.get("notificationArticleRewardLabel");

                        final Reward reward7 = rewardMapper.get(dataId);
                        final String senderId7 = reward7.getSenderId();
                        final UserExt user7 = userMapper.get(senderId7);
                        final String articleId7 = reward7.getDataId();
                        final Article article7 = articleMapper.get(articleId7);
                        if (null == article7) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String userLink7 = UserExtUtil.getUserLink(user7);
                        desTemplate = desTemplate.replace("{user}", userLink7);

                        final String articleLink7 = "<a href=\""
                                +  SpringUtil.getServerPath() + article7.getArticlePermalink() + "\">"
                                + article7.getArticleTitle() + "</a>";
                        desTemplate = desTemplate.replace("{article}", articleLink7);

                        break;
                    case NotificationUtil.DATA_TYPE_C_POINT_CHARGE:
                        desTemplate = langPropsService.get("notificationPointChargeLabel");

                        final Pointtransfer transfer5 = pointtransferMapper.get(dataId);
                        final int sum5 = transfer5.getSum();
                        final String memo5 = transfer5.getDataId();
                        final String yuan = memo5.split("-")[0];

                        desTemplate = desTemplate.replace("{yuan}", yuan);
                        desTemplate = desTemplate.replace("{point}", String.valueOf(sum5));

                        break;
                    case NotificationUtil.DATA_TYPE_C_POINT_EXCHANGE:
                        desTemplate = langPropsService.get("notificationPointExchangeLabel");

                        final Pointtransfer transfer6 = pointtransferMapper.get(dataId);
                        final int sum6 = transfer6.getSum();
                        final String yuan6 = transfer6.getDataId();

                        desTemplate = desTemplate.replace("{yuan}", yuan6);
                        desTemplate = desTemplate.replace("{point}", String.valueOf(sum6));

                        break;
                    case NotificationUtil.DATA_TYPE_C_ABUSE_POINT_DEDUCT:
                        desTemplate = langPropsService.get("notificationAbusePointDeductLabel");

                        final Pointtransfer transfer7 = pointtransferMapper.get(dataId);
                        final int sum7 = transfer7.getSum();
                        final String memo7 = transfer7.getDataId();

                        desTemplate = desTemplate.replace("{action}", memo7);
                        desTemplate = desTemplate.replace("{point}", String.valueOf(sum7));

                        break;
                    case NotificationUtil.DATA_TYPE_C_POINT_COMMENT_THANK:
                        desTemplate = langPropsService.get("notificationCmtThankLabel");

                        final Reward reward8 = rewardMapper.get(dataId);
                        final String senderId8 = reward8.getSenderId();
                        final UserExt user8 = userMapper.get(senderId8);
                        final Comment comment8 = commentMapper.get(reward8.getDataId());
                        final String articleId8 = comment8.getCommentOnArticleId();
                        final Article article8 = articleMapper.get(articleId8);
                        if (null == article8) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String userLink8 = UserExtUtil.getUserLink(user8);
                        desTemplate = desTemplate.replace("{user}", userLink8);

                        final String articleLink8 = "<a href=\""
                                +  SpringUtil.getServerPath() + article8.getArticlePermalink() + "\">"
                                + article8.getArticleTitle() + "</a>";
                        desTemplate = desTemplate.replace("{article}", articleLink8);

                        break;
                    case NotificationUtil.DATA_TYPE_C_POINT_COMMENT_ACCEPT:
                        desTemplate = langPropsService.get("notificationCmtAcceptLabel");

                        final Reward reward33 = rewardMapper.get(dataId);
                        final String articleId33 = reward33.getDataId();
                        final Article article33 = articleMapper.get(articleId33);
                        if (null == article33) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String articleAuthorId = article33.getArticleAuthorId();
                        final UserExt user33 = userMapper.get(articleAuthorId);
                        final String userLink33 = UserExtUtil.getUserLink(user33);
                        desTemplate = desTemplate.replace("{user}", userLink33);

                        final String articleLink33 = "<a href=\""
                                +  SpringUtil.getServerPath() + article33.getArticlePermalink() + "\">"
                                + article33.getArticleTitle() + "</a>";
                        desTemplate = desTemplate.replace("{article}", Emotions.convert(articleLink33));

                        break;
                    case NotificationUtil.DATA_TYPE_C_POINT_TRANSFER:
                        desTemplate = langPropsService.get("notificationPointTransferLabel");

                        final Pointtransfer transfer101 = pointtransferMapper.get(dataId);
                        final String fromId101 = transfer101.getFromId();
                        final UserExt user101 = userMapper.get(fromId101);
                        final int sum101 = transfer101.getSum();

                        final String userLink101 = UserExtUtil.getUserLink(user101);
                        desTemplate = desTemplate.replace("{user}", userLink101);
                        desTemplate = desTemplate.replace("{amount}", String.valueOf(sum101));

                        break;
                    case NotificationUtil.DATA_TYPE_C_INVITECODE_USED:
                        desTemplate = langPropsService.get("notificationInvitecodeUsedLabel");

                        final UserExt invitedUser = userMapper.get(dataId);
                        final String invitedUserLink = UserExtUtil.getUserLink(invitedUser);

                        desTemplate = desTemplate.replace("{user}", invitedUserLink);

                        break;
                    case NotificationUtil.DATA_TYPE_C_INVITATION_LINK_USED:
                        desTemplate = langPropsService.get("notificationInvitationLinkUsedLabel");

                        final UserExt invitedUser18 = userMapper.get(dataId);
                        final String invitedUserLink18 = UserExtUtil.getUserLink(invitedUser18);

                        desTemplate = desTemplate.replace("{user}", invitedUserLink18);

                        break;
                    case NotificationUtil.DATA_TYPE_C_POINT_PERFECT_ARTICLE:
                        desTemplate = langPropsService.get("notificationPointPerfectArticleLabel");

                        final Article article22 = articleMapper.get(dataId);
                        if (null == article22) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String articleLink22 = "<a href=\""
                                +  SpringUtil.getServerPath() + article22.getArticlePermalink() + "\">"
                                + article22.getArticleTitle() + "</a>";
                        desTemplate = desTemplate.replace("{article}", articleLink22);

                        break;
                    case NotificationUtil.DATA_TYPE_C_POINT_REPORT_HANDLED:
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
        } catch (final Exception e) {
            LOGGER.error( "Gets [point] notifications failed", e);

            throw new Exception(e);
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
     * @throws Exception service exception
     */
    public JSONObject getCommentedNotifications(final int avatarViewMode,
                                                final String userId, final int currentPageNum, final int pageSize) throws Exception {
        final JSONObject ret = new JSONObject();
        final List<JSONObject> rslts = new ArrayList<>();

        ret.put(Keys.RESULTS, (Object) rslts);

//        final List<Filter> filters = new ArrayList<>();
//        filters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_USER_ID, FilterOperator.EQUAL, userId));
//        filters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, NotificationUtil.DATA_TYPE_C_COMMENTED));

        PageHelper.startPage(currentPageNum,pageSize,"hasRead ASEC, oId DESC");
//        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize).
//                setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters)).
//                addSort(NotificationUtil.NOTIFICATION_HAS_READ, SortDirection.ASCENDING).
//                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);

        try {
            final UserExt user = userMapper.get(userId);
            final int cmtViewMode = user.getUserCommentViewMode();

            final PageInfo<Notification> queryResult = new PageInfo<>(
                    notificationMapper.getByUserIdAndDataType(userId, NotificationUtil.DATA_TYPE_C_COMMENTED));
            final List<JSONObject> results = JsonUtil.listToJSONList(queryResult.getList());

            ret.put(Pagination.PAGINATION_RECORD_COUNT, queryResult.getTotal());

            for (int i = 0; i < results.size(); i++) {
                final JSONObject notification = results.get(i);
                final String commentId = notification.optString(NotificationUtil.NOTIFICATION_DATA_ID);

                final Comment comment = commentQueryService.getCommentById(avatarViewMode, commentId);

//                final Query q = new Query().setPageCount(1).
//                        addProjection(Article.ARTICLE_PERFECT, Integer.class).
//                        addProjection(Article.ARTICLE_TITLE, String.class).
//                        addProjection(Article.ARTICLE_TYPE, Integer.class).
//                        setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.EQUAL,
//                                comment.optString(Comment.COMMENT_ON_ARTICLE_ID)));
                final Article article = articleMapper.get(comment.getCommentOnArticleId());
//                final JSONObject article = rlts.optJSONObject(0);
                final String articleTitle = article.getArticleTitle();
                final int articleType = article.getArticleType();
                final int articlePerfect = article.getArticlePerfect();

                final JSONObject commentedNotification = new JSONObject();
                commentedNotification.put(Keys.OBJECT_ID, notification.optString(Keys.OBJECT_ID));
                commentedNotification.put(CommentUtil.COMMENT_T_AUTHOR_NAME, comment.getCommentAuthorName());
                commentedNotification.put(CommentUtil.COMMENT_CONTENT, comment.getCommentContent());
                commentedNotification.put(CommentUtil.COMMENT_T_AUTHOR_THUMBNAIL_URL, comment.getCommentAuthorThumbnailURL());
                commentedNotification.put(Common.THUMBNAIL_UPDATE_TIME, ((UserExt)comment.getCommenter()).getUserUpdateTime());
                commentedNotification.put(CommentUtil.COMMENT_T_ARTICLE_TITLE, Emotions.convert(articleTitle));
                commentedNotification.put(CommentUtil.COMMENT_T_ARTICLE_TYPE, articleType);
                commentedNotification.put(CommentUtil.COMMENT_CREATE_TIME, comment.getCommentCreateTime());
                commentedNotification.put(NotificationUtil.NOTIFICATION_HAS_READ, notification.optBoolean(NotificationUtil.NOTIFICATION_HAS_READ));
                commentedNotification.put(CommentUtil.COMMENT_T_ARTICLE_PERFECT, articlePerfect);
                final String articleId = comment.getCommentOnArticleId();
                final int cmtPage = commentQueryService.getCommentPage(articleId, commentId, cmtViewMode, Symphonys.getInt("articleCommentsPageSize"));
                commentedNotification.put(CommentUtil.COMMENT_SHARP_URL, "/article/" + articleId + "?p=" + cmtPage
                        + "&m=" + cmtViewMode + "#" + commentId);

                rslts.add(commentedNotification);
            }

            return ret;
        } catch (final Exception e) {
            LOGGER.error( "Gets [commented] notifications", e);
            throw new Exception(e);
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
     * @throws Exception service exception
     */
    public JSONObject getReplyNotifications(final int avatarViewMode,
                                            final String userId, final int currentPageNum, final int pageSize) throws Exception {
        final JSONObject ret = new JSONObject();
        final List<JSONObject> rslts = new ArrayList<>();

        ret.put(Keys.RESULTS, (Object) rslts);

//        final List<Filter> filters = new ArrayList<>();
//        filters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_USER_ID, FilterOperator.EQUAL, userId));
//        filters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, NotificationUtil.DATA_TYPE_C_REPLY));

//        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize).
//                setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters)).
//                addSort(NotificationUtil.NOTIFICATION_HAS_READ, SortDirection.ASCENDING).
//                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);

        PageHelper.startPage(currentPageNum,pageSize,"hasRead ASEC, oId DESC");

        try {
            final PageInfo<Notification> queryResult = new PageInfo<>(
                    notificationMapper.getByUserIdAndDataType(userId,NotificationUtil.DATA_TYPE_C_REPLY));
            final List<JSONObject> results = JsonUtil.listToJSONList(queryResult.getList());

            ret.put(Pagination.PAGINATION_RECORD_COUNT, queryResult.getTotal());

            for (int i = 0; i < results.size(); i++) {
                final JSONObject notification = results.get(i);
                final String commentId = notification.optString(NotificationUtil.NOTIFICATION_DATA_ID);

                final Comment comment = commentQueryService.getCommentById(avatarViewMode, commentId);

//                final Query q = new Query().setPageCount(1).
//                        addProjection(Article.ARTICLE_PERFECT, Integer.class).
//                        addProjection(Article.ARTICLE_TITLE, String.class).
//                        addProjection(Article.ARTICLE_TYPE, Integer.class).
//                        setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.EQUAL,
//                                comment.optString(Comment.COMMENT_ON_ARTICLE_ID)));
                final Article article = articleMapper.get(comment.getCommentOnArticleId());
//                final JSONObject article = rlts.optJSONObject(0);
                final String articleTitle = article.getArticleTitle();
                final int articleType = article.getArticleType();
                final int articlePerfect = article.getArticlePerfect();

                final JSONObject replyNotification = new JSONObject();
                replyNotification.put(Keys.OBJECT_ID, notification.optString(Keys.OBJECT_ID));
                replyNotification.put(CommentUtil.COMMENT_T_AUTHOR_NAME, comment.getCommentAuthorName());
                replyNotification.put(CommentUtil.COMMENT_CONTENT, comment.getCommentContent());
                replyNotification.put(CommentUtil.COMMENT_T_AUTHOR_THUMBNAIL_URL, comment.getCommentAuthorThumbnailURL());
                replyNotification.put(Common.THUMBNAIL_UPDATE_TIME, ((UserExt)comment.getCommenter()).getUserUpdateTime());
                replyNotification.put(CommentUtil.COMMENT_T_ARTICLE_TITLE, Emotions.convert(articleTitle));
                replyNotification.put(CommentUtil.COMMENT_T_ARTICLE_TYPE, articleType);
                replyNotification.put(CommentUtil.COMMENT_SHARP_URL, comment.getCommentSharpURL());
                replyNotification.put(CommentUtil.COMMENT_CREATE_TIME, comment.getCommentCreateTime());
                replyNotification.put(NotificationUtil.NOTIFICATION_HAS_READ, notification.optBoolean(NotificationUtil.NOTIFICATION_HAS_READ));
                replyNotification.put(CommentUtil.COMMENT_T_ARTICLE_PERFECT, articlePerfect);
                replyNotification.put(NotificationUtil.NOTIFICATION_DATA_TYPE, NotificationUtil.DATA_TYPE_C_REPLY);
                final String articleId = comment.getCommentOnArticleId();
                replyNotification.put(ArticleUtil.ARTICLE_T_ID, articleId);
                replyNotification.put(CommentUtil.COMMENT_T_ID, comment.getOid());

                rslts.add(replyNotification);
            }

            return ret;
        } catch (final Exception e) {
            LOGGER.error( "Gets [reply] notifications", e);
            throw new Exception(e);
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
     * @throws Exception service exception
     */
    public JSONObject getAtNotifications(final int avatarViewMode,
                                         final String userId, final int currentPageNum, final int pageSize) throws Exception {
        final JSONObject ret = new JSONObject();
        final List<JSONObject> rslts = new ArrayList<>();

        ret.put(Keys.RESULTS, (Object) rslts);

//        final List<Filter> filters = new ArrayList<>();
//        filters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_USER_ID, FilterOperator.EQUAL, userId));
//
//        final List<Filter> subFilters = new ArrayList<>();
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
//                NotificationUtil.DATA_TYPE_C_AT));
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
//                NotificationUtil.));
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
//                NotificationUtil.));
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
//                NotificationUtil.));
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
//                NotificationUtil.));
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
//                NotificationUtil.));
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
//                NotificationUtil.));
//
//        filters.add(new CompositeFilter(CompositeFilterOperator.OR, subFilters));

        List<Integer> list = new ArrayList<>();
        list.add(NotificationUtil.DATA_TYPE_C_AT);
        list.add(NotificationUtil.DATA_TYPE_C_ARTICLE_NEW_FOLLOWER);
        list.add(NotificationUtil.DATA_TYPE_C_ARTICLE_NEW_WATCHER);
        list.add(NotificationUtil.DATA_TYPE_C_COMMENT_VOTE_UP);
        list.add(NotificationUtil.DATA_TYPE_C_COMMENT_VOTE_DOWN);
        list.add(NotificationUtil.DATA_TYPE_C_ARTICLE_VOTE_UP);
        list.add(NotificationUtil.DATA_TYPE_C_ARTICLE_VOTE_DOWN);

//        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize).
//                setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters)).
//                addSort(NotificationUtil.NOTIFICATION_HAS_READ, SortDirection.ASCENDING).
//                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);

        PageHelper.startPage(currentPageNum,pageSize,"hasRead ASCE, oId DESC");

        try {
            final PageInfo<Notification> queryResult = new PageInfo<>(notificationMapper.getByUserIdAndDataTypes(userId,list));
            final List<JSONObject> results = JsonUtil.listToJSONList(queryResult.getList());

            ret.put(Pagination.PAGINATION_RECORD_COUNT, queryResult.getTotal());

            for (int i = 0; i < results.size(); i++) {
                final JSONObject notification = results.get(i);
                final int dataType = notification.optInt(NotificationUtil.NOTIFICATION_DATA_TYPE);
                final String dataId = notification.optString(NotificationUtil.NOTIFICATION_DATA_ID);

                final JSONObject atNotification = new JSONObject();
                atNotification.put(NotificationUtil.NOTIFICATION_DATA_TYPE, dataType);
                String description = "";

                atNotification.put(Keys.OBJECT_ID, notification.optString(Keys.OBJECT_ID));
                atNotification.put(NotificationUtil.NOTIFICATION_HAS_READ, notification.optBoolean(NotificationUtil.NOTIFICATION_HAS_READ));
                atNotification.put(Common.CREATE_TIME, new Date(notification.optLong(Keys.OBJECT_ID)));

                switch (dataType) {
                    case NotificationUtil.DATA_TYPE_C_AT:
                        final Comment comment = commentQueryService.getCommentById(avatarViewMode, dataId);
                        if (null != comment) {
//                            final Query q = new Query().setPageCount(1).
//                                    addProjection(Article.ARTICLE_PERFECT, Integer.class).
//                                    addProjection(Article.ARTICLE_TITLE, String.class).
//                                    addProjection(Article.ARTICLE_TYPE, Integer.class).
//                                    setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.EQUAL,
//                                            comment.optString(Comment.COMMENT_ON_ARTICLE_ID)));
                            final Article article = articleMapper.get(comment.getCommentOnArticleId());
//                            final JSONObject article = rlts.optJSONObject(0);
                            final String articleTitle = article.getArticleTitle();
                            final int articleType = article.getArticleType();
                            final int articlePerfect = article.getArticlePerfect();

                            atNotification.put(Common.AUTHOR_NAME, comment.getCommentAuthorName());
                            atNotification.put(Common.CONTENT, comment.getCommentContent());
                            atNotification.put(Common.THUMBNAIL_URL, comment.getCommentAuthorThumbnailURL());
                            atNotification.put(Common.THUMBNAIL_UPDATE_TIME, ((UserExt)comment.getCommenter()).
                                    getUserUpdateTime());
                            atNotification.put(ArticleUtil.ARTICLE_TITLE, Emotions.convert(articleTitle));
                            atNotification.put(ArticleUtil.ARTICLE_TYPE, articleType);
                            atNotification.put(Common.URL, comment.getCommentSharpURL());
                            atNotification.put(Common.CREATE_TIME, comment.getCommentCreateTime());
                            atNotification.put(NotificationUtil.NOTIFICATION_T_AT_IN_ARTICLE, false);
                            atNotification.put(ArticleUtil.ARTICLE_PERFECT, articlePerfect);
                            atNotification.put(ArticleUtil.ARTICLE_T_ID, comment.getCommentOnArticleId());
                            atNotification.put(CommentUtil.COMMENT_T_ID, comment.getOid());

                            rslts.add(atNotification);
                        } else { // The 'at' in article content
                            final Article article = articleMapper.get(dataId);

                            final String articleAuthorId = article.getArticleAuthorId();
                            final UserExt articleAuthor = userMapper.get(articleAuthorId);

                            atNotification.put(Common.AUTHOR_NAME, articleAuthor.getUserName());
                            atNotification.put(Common.CONTENT, "");
                            JSONObject jsonObject = new JSONObject(JsonUtil.objectToJson(articleAuthor));
                            final String thumbnailURL = avatarQueryService.getAvatarURLByUser(avatarViewMode, jsonObject, "48");
                            atNotification.put(Common.THUMBNAIL_URL, thumbnailURL);
                            atNotification.put(Common.THUMBNAIL_UPDATE_TIME, articleAuthor.getUserUpdateTime());
                            atNotification.put(ArticleUtil.ARTICLE_TITLE, Emotions.convert(article.getArticleTitle()));
                            atNotification.put(ArticleUtil.ARTICLE_TYPE, article.getArticleType());
                            atNotification.put(Common.URL,  SpringUtil.getServerPath() + article.getArticlePermalink());
                            atNotification.put(Common.CREATE_TIME, new Date(article.getArticleCreateTime()));
                            atNotification.put(NotificationUtil.NOTIFICATION_T_AT_IN_ARTICLE, true);

                            final String tagsStr = article.getArticleTags();
                            atNotification.put(ArticleUtil.ARTICLE_TAGS, tagsStr);
                            final List<JSONObject> tags = buildTagObjs(tagsStr);
                            atNotification.put(ArticleUtil.ARTICLE_T_TAG_OBJS, (Object) tags);

                            atNotification.put(ArticleUtil.ARTICLE_COMMENT_CNT, article.getArticleCommentCount());
                            atNotification.put(ArticleUtil.ARTICLE_PERFECT, article.getArticlePerfect());
                            atNotification.put(ArticleUtil.ARTICLE_T_ID, article.getOid());

                            rslts.add(atNotification);
                        }

                        break;
                    case NotificationUtil.DATA_TYPE_C_ARTICLE_NEW_FOLLOWER:
                    case NotificationUtil.DATA_TYPE_C_ARTICLE_NEW_WATCHER:
                        final String articleId = dataId.split("-")[0];
                        final String followerUserId = dataId.split("-")[1];

                        final Article article = articleMapper.get(articleId);
                        if (null == article) {
                            description = langPropsService.get("removedLabel");
                            atNotification.put(Common.DESCRIPTION, description);

                            rslts.add(atNotification);

                            continue;
                        }

                        if (NotificationUtil.DATA_TYPE_C_ARTICLE_NEW_FOLLOWER == dataType) {
                            description = langPropsService.get("notificationArticleNewFollowerLabel");
                        } else if (NotificationUtil.DATA_TYPE_C_ARTICLE_NEW_WATCHER == dataType) {
                            description = langPropsService.get("notificationArticleNewWatcherLabel");
                        }

                        final UserExt followerUser = userMapper.get(followerUserId);
                        final String followerUserName = followerUser.getUserName();
                        atNotification.put(User.USER_NAME, followerUserName);

                        JSONObject jsonObject = new JSONObject(JsonUtil.objectToJson(followerUser));
                        final String thumbnailURL = avatarQueryService.getAvatarURLByUser(avatarViewMode, jsonObject, "48");
                        atNotification.put(Common.THUMBNAIL_URL, thumbnailURL);
                        atNotification.put(Common.THUMBNAIL_UPDATE_TIME, followerUser.getUserUpdateTime());

                        final String userLink = UserExtUtil.getUserLink(followerUserName);
                        description = description.replace("{user}", userLink);

                        final String articleLink = " <a href=\"" +  SpringUtil.getServerPath() + article.getArticlePermalink() + "\">"
                                + Emotions.convert(article.getArticleTitle()) + "</a>";
                        description = description.replace("{article}", articleLink);

                        atNotification.put(Common.DESCRIPTION, description);

                        rslts.add(atNotification);

                        break;
                    case NotificationUtil.DATA_TYPE_C_COMMENT_VOTE_UP:
                    case NotificationUtil.DATA_TYPE_C_COMMENT_VOTE_DOWN:
                        final UserExt user = userMapper.get(userId);
                        final int cmtViewMode = user.getUserCommentViewMode();
                        final String commentId = dataId.split("-")[0];
                        final String cmtVoterId = dataId.split("-")[1];
                        final UserExt cmtVoter = userMapper.get(cmtVoterId);
                        String voterUserName = cmtVoter.getUserName();
                        atNotification.put(User.USER_NAME, voterUserName);
                        JSONObject jsonObject1 = new JSONObject(JsonUtil.objectToJson(cmtVoter));
                        String thumbnailURLVote = avatarQueryService.getAvatarURLByUser(avatarViewMode, jsonObject1, "48");
                        atNotification.put(Common.THUMBNAIL_URL, thumbnailURLVote);
                        atNotification.put(Common.THUMBNAIL_UPDATE_TIME, cmtVoter.getUserUpdateTime());

                        Article articleVote = null;
                        Comment jsonComment = null;
                        if (NotificationUtil.DATA_TYPE_C_COMMENT_VOTE_UP == dataType) {
                            description = langPropsService.get("notificationCommentVoteUpLabel");
                            jsonComment = commentMapper.get(commentId);
                            if (null == articleVote) {
                                description = langPropsService.get("removedLabel");
                                atNotification.put(Common.DESCRIPTION, description);
                                rslts.add(atNotification);

                                continue;
                            }

                            articleVote = articleMapper.get(jsonComment.getCommentOnArticleId());
                        } else if (NotificationUtil.DATA_TYPE_C_COMMENT_VOTE_DOWN == dataType) {
                            description = langPropsService.get("notificationCommentVoteDownLabel");
                            jsonComment = commentMapper.get(commentId);
                            if (null == articleVote) {
                                description = langPropsService.get("removedLabel");
                                atNotification.put(Common.DESCRIPTION, description);
                                rslts.add(atNotification);

                                continue;
                            }

                            articleVote = articleMapper.get(jsonComment.getCommentOnArticleId());
                        }
                        if (null == articleVote) {
                            description = langPropsService.get("removedLabel");
                            atNotification.put(Common.DESCRIPTION, description);
                            rslts.add(atNotification);

                            continue;
                        }

                        String userLinkVote = UserExtUtil.getUserLink(voterUserName);
                        description = description.replace("{user}", userLinkVote);
                        final String cmtVoteURL = commentQueryService.getCommentURL(commentId, cmtViewMode, Symphonys.getInt("articleCommentsPageSize"));
                        atNotification.put(Common.DESCRIPTION, description.replace("{article}", Emotions.convert(cmtVoteURL)));
                        rslts.add(atNotification);

                        break;
                    case NotificationUtil.DATA_TYPE_C_ARTICLE_VOTE_UP:
                    case NotificationUtil.DATA_TYPE_C_ARTICLE_VOTE_DOWN:
                        final String voteArticleId = dataId.split("-")[0];
                        final String voterId = dataId.split("-")[1];
                        final UserExt voter = userMapper.get(voterId);
                        voterUserName = voter.getUserName();
                        atNotification.put(User.USER_NAME, voterUserName);
                        JSONObject jsonObject2 = new JSONObject(JsonUtil.objectToJson(voter));
                        thumbnailURLVote = avatarQueryService.getAvatarURLByUser(avatarViewMode, jsonObject2, "48");
                        atNotification.put(Common.THUMBNAIL_URL, thumbnailURLVote);
                        atNotification.put(Common.THUMBNAIL_UPDATE_TIME, voter.getUserUpdateTime());

                        Article voteArticle = null;
                        if (NotificationUtil.DATA_TYPE_C_ARTICLE_VOTE_UP == dataType) {
                            description = langPropsService.get("notificationArticleVoteUpLabel");
                            voteArticle = articleMapper.get(voteArticleId);
                        } else if (NotificationUtil.DATA_TYPE_C_ARTICLE_VOTE_DOWN == dataType) {
                            description = langPropsService.get("notificationArticleVoteDownLabel");
                            voteArticle = articleMapper.get(voteArticleId);
                        }

                        if (null == voteArticle) {
                            description = langPropsService.get("removedLabel");
                            atNotification.put(Common.DESCRIPTION, description);
                            rslts.add(atNotification);

                            continue;
                        }

                        userLinkVote = UserExtUtil.getUserLink(voterUserName);
                        description = description.replace("{user}", userLinkVote);
                        final String articleLinkVote = " <a href=\"" +  SpringUtil.getServerPath() + voteArticle.getArticlePermalink() + "\">"
                                + Emotions.convert(voteArticle.getArticleTitle()) + "</a>";
                        description = description.replace("{article}", articleLinkVote);
                        atNotification.put(Common.DESCRIPTION, description);
                        rslts.add(atNotification);

                        break;
                }
            }

            return ret;
        } catch (final Exception e) {
            LOGGER.error( "Gets [at] notifications", e);

            throw new Exception(e);
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
     * @throws Exception service exception
     */
    public JSONObject getFollowingNotifications(final int avatarViewMode,
                                                final String userId, final int currentPageNum, final int pageSize) throws Exception {
        final JSONObject ret = new JSONObject();
        final List<JSONObject> rslts = new ArrayList<>();

        ret.put(Keys.RESULTS, (Object) rslts);

//        final List<Filter> filters = new ArrayList<>();
//        filters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_USER_ID, FilterOperator.EQUAL, userId));
//
//        final List<Filter> subFilters = new ArrayList<>();
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
//                NotificationUtil.DATA_TYPE_C_FOLLOWING_USER));
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
//                NotificationUtil.DATA_TYPE_C_FOLLOWING_ARTICLE_UPDATE));
//        subFilters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL,
//                NotificationUtil.DATA_TYPE_C_FOLLOWING_ARTICLE_COMMENT));

        List<Integer> list = new ArrayList<>();
        list.add(NotificationUtil.DATA_TYPE_C_FOLLOWING_USER);
        list.add(NotificationUtil.DATA_TYPE_C_FOLLOWING_ARTICLE_UPDATE);
        list.add(NotificationUtil.DATA_TYPE_C_FOLLOWING_ARTICLE_COMMENT);

//        filters.add(new CompositeFilter(CompositeFilterOperator.OR, subFilters));

//        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize).
//                setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters)).
//                addSort(NotificationUtil.NOTIFICATION_HAS_READ, SortDirection.ASCENDING).
//                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);

        PageHelper.startPage(currentPageNum,pageSize,"hasRead ASCE, Oid DESC");

        try {
            final PageInfo<Notification> queryResult = new PageInfo<>(notificationMapper.getByUserIdAndDataTypes(userId,list));
            final List<JSONObject> results = JsonUtil.listToJSONList(queryResult.getList());

            ret.put(Pagination.PAGINATION_RECORD_COUNT, queryResult.getTotal());

            for (int i = 0; i < results.size(); i++) {
                final JSONObject notification = results.get(i);
                final String commentId = notification.optString(NotificationUtil.NOTIFICATION_DATA_ID);
                final int dataType = notification.optInt(NotificationUtil.NOTIFICATION_DATA_TYPE);
                final JSONObject followingNotification = new JSONObject();
                followingNotification.put(NotificationUtil.NOTIFICATION_DATA_TYPE, dataType);

                switch (dataType) {
                    case NotificationUtil.DATA_TYPE_C_FOLLOWING_ARTICLE_COMMENT:
                        final Comment comment = commentQueryService.getCommentById(avatarViewMode, commentId);
//                        final Query q = new Query().setPageCount(1).
//                                addProjection(Article.ARTICLE_PERFECT, Integer.class).
//                                addProjection(Article.ARTICLE_TITLE, String.class).
//                                addProjection(Article.ARTICLE_TYPE, Integer.class).
//                                setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.EQUAL,
//                                        comment.optString(Comment.COMMENT_ON_ARTICLE_ID)));
                        Article article = articleMapper.get(comment.getCommentOnArticleId());
//                        JSONObject article = rlts.optJSONObject(0);
                        final String articleTitle = article.getArticleTitle();
                        final int articleType = article.getArticleType();
                        final int articlePerfect = article.getArticlePerfect();

                        followingNotification.put(Keys.OBJECT_ID, notification.optString(Keys.OBJECT_ID));
                        followingNotification.put(Common.AUTHOR_NAME, comment.getCommentAuthorName());
                        followingNotification.put(Common.CONTENT, comment.getCommentContent());
                        followingNotification.put(Common.THUMBNAIL_URL, comment.getCommentAuthorThumbnailURL());
                        followingNotification.put(Common.THUMBNAIL_UPDATE_TIME, ((UserExt)comment.getCommenter()).
                                getUserUpdateTime());
                        followingNotification.put(ArticleUtil.ARTICLE_TITLE, Emotions.convert(articleTitle));
                        followingNotification.put(ArticleUtil.ARTICLE_TYPE, articleType);
                        followingNotification.put(Common.URL, comment.getCommentSharpURL());
                        followingNotification.put(Common.CREATE_TIME, comment.getCommentCreateTime());
                        followingNotification.put(NotificationUtil.NOTIFICATION_HAS_READ, notification.optBoolean(NotificationUtil.NOTIFICATION_HAS_READ));
                        followingNotification.put(NotificationUtil.NOTIFICATION_T_IS_COMMENT, true);
                        followingNotification.put(ArticleUtil.ARTICLE_PERFECT, articlePerfect);

                        rslts.add(followingNotification);

                        break;
                    case NotificationUtil.DATA_TYPE_C_FOLLOWING_USER:
                    case NotificationUtil.DATA_TYPE_C_FOLLOWING_ARTICLE_UPDATE:
                        article = articleMapper.get(commentId);

                        final String articleAuthorId = article.getArticleAuthorId();
                        final UserExt articleAuthor = userMapper.get(articleAuthorId);

                        followingNotification.put(Keys.OBJECT_ID, notification.optString(Keys.OBJECT_ID));
                        followingNotification.put(Common.AUTHOR_NAME, articleAuthor.getUserName());
                        followingNotification.put(Common.CONTENT, "");
                        JSONObject jsonObject = new JSONObject(JsonUtil.objectToJson(articleAuthor));
                        final String thumbnailURL = avatarQueryService.getAvatarURLByUser(avatarViewMode, jsonObject, "48");
                        followingNotification.put(Common.THUMBNAIL_URL, thumbnailURL);
                        followingNotification.put(Common.THUMBNAIL_UPDATE_TIME, articleAuthor.getUserUpdateTime());
                        followingNotification.put(ArticleUtil.ARTICLE_TITLE, Emotions.convert(article.getArticleTitle()));
                        followingNotification.put(ArticleUtil.ARTICLE_TYPE, article.getArticleType());
                        followingNotification.put(Common.URL,  SpringUtil.getServerPath() + article.getArticlePermalink());
                        followingNotification.put(Common.CREATE_TIME, new Date(article.getArticleCreateTime()));
                        followingNotification.put(NotificationUtil.NOTIFICATION_HAS_READ, notification.optBoolean(NotificationUtil.NOTIFICATION_HAS_READ));
                        followingNotification.put(NotificationUtil.NOTIFICATION_T_IS_COMMENT, false);

                        final String tagsStr = article.getArticleTags();
                        followingNotification.put(ArticleUtil.ARTICLE_TAGS, tagsStr);
                        final List<JSONObject> tags = buildTagObjs(tagsStr);
                        followingNotification.put(ArticleUtil.ARTICLE_T_TAG_OBJS, (Object) tags);

                        followingNotification.put(ArticleUtil.ARTICLE_COMMENT_CNT, article.getArticleCommentCount());
                        followingNotification.put(ArticleUtil.ARTICLE_PERFECT, article.getArticlePerfect());

                        rslts.add(followingNotification);

                        break;
                }
            }

            return ret;
        } catch (final Exception e) {
            LOGGER.error( "Gets [following] notifications", e);

            throw new Exception(e);
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
     * @throws Exception service exception
     */
    public JSONObject getBroadcastNotifications(final int avatarViewMode,
                                                final String userId, final int currentPageNum, final int pageSize) throws Exception {
        final JSONObject ret = new JSONObject();
        final List<JSONObject> rslts = new ArrayList<>();

        ret.put(Keys.RESULTS, (Object) rslts);

//        final List<Filter> filters = new ArrayList<>();
//        filters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_USER_ID, FilterOperator.EQUAL, userId));
//        filters.add(new PropertyFilter(NotificationUtil.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, NotificationUtil.DATA_TYPE_C_BROADCAST));
//
//        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize).
//                setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters)).
//                addSort(NotificationUtil.NOTIFICATION_HAS_READ, SortDirection.ASCENDING).
//                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);

        PageHelper.startPage(currentPageNum,pageSize,"hasRead ASCE, oId DESC");

        try {
            final PageInfo<Notification> queryResult = new PageInfo<>(notificationMapper.getByUserIdAndDataType(userId,NotificationUtil.DATA_TYPE_C_BROADCAST));
            final List<JSONObject> results = JsonUtil.listToJSONList(queryResult.getList());

            ret.put(Pagination.PAGINATION_RECORD_COUNT,
                    queryResult.getTotal());

            for (int i = 0; i < results.size(); i++) {
                final JSONObject notification = results.get(i);
                final String articleId = notification.optString(NotificationUtil.NOTIFICATION_DATA_ID);

//                final Query q = new Query().setPageCount(1).
//                        addProjection(Article.ARTICLE_TITLE, String.class).
//                        addProjection(Article.ARTICLE_TYPE, Integer.class).
//                        addProjection(Article.ARTICLE_AUTHOR_ID, String.class).
//                        addProjection(Article.ARTICLE_PERMALINK, String.class).
//                        addProjection(Article.ARTICLE_CREATE_TIME, Long.class).
//                        addProjection(Article.ARTICLE_TAGS, String.class).
//                        addProjection(Article.ARTICLE_COMMENT_CNT, Integer.class).
//                        addProjection(Article.ARTICLE_PERFECT, Integer.class).
//                        setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.EQUAL, articleId));
                final Article article = articleMapper.get(articleId);
//                final JSONObject article = rlts.optJSONObject(0);

                if (null == article) {
                    LOGGER.warn("Not found article[id=" + articleId + ']');

                    continue;
                }

                final String articleTitle = article.getArticleTitle();
                final String articleAuthorId = article.getArticleAuthorId();
                final UserExt author = userMapper.get(articleAuthorId);

                if (null == author) {
                    LOGGER.warn("Not found user[id=" + articleAuthorId + ']');

                    continue;
                }

                final JSONObject broadcastNotification = new JSONObject();
                broadcastNotification.put(Keys.OBJECT_ID, notification.optString(Keys.OBJECT_ID));
                broadcastNotification.put(Common.AUTHOR_NAME, author.getUserName());
                broadcastNotification.put(Common.CONTENT, "");
                JSONObject jsonObject = new JSONObject(JsonUtil.objectToJson(author));
                broadcastNotification.put(Common.THUMBNAIL_URL,
                        avatarQueryService.getAvatarURLByUser(avatarViewMode, jsonObject, "48"));
                broadcastNotification.put(Common.THUMBNAIL_UPDATE_TIME, author.getUserUpdateTime());
                broadcastNotification.put(ArticleUtil.ARTICLE_TITLE, Emotions.convert(articleTitle));
                broadcastNotification.put(Common.URL,  SpringUtil.getServerPath() + article.getArticlePermalink());
                broadcastNotification.put(Common.CREATE_TIME, new Date(article.getArticleCreateTime()));
                broadcastNotification.put(NotificationUtil.NOTIFICATION_HAS_READ,
                        notification.optBoolean(NotificationUtil.NOTIFICATION_HAS_READ));
                broadcastNotification.put(Common.TYPE, ArticleUtil.ARTICLE);
                broadcastNotification.put(ArticleUtil.ARTICLE_TYPE, article.getArticleType());

                final String tagsStr = article.getArticleTags();
                broadcastNotification.put(ArticleUtil.ARTICLE_TAGS, tagsStr);
                final List<JSONObject> tags = buildTagObjs(tagsStr);
                broadcastNotification.put(ArticleUtil.ARTICLE_T_TAG_OBJS, (Object) tags);

                broadcastNotification.put(ArticleUtil.ARTICLE_COMMENT_CNT, article.getArticleCommentCount());
                broadcastNotification.put(ArticleUtil.ARTICLE_PERFECT, article.getArticlePerfect());

                rslts.add(broadcastNotification);
            }

            return ret;
        } catch (final Exception e) {
            LOGGER.error( "Gets [broadcast] notifications", e);

            throw new Exception(e);
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
            tag.put(TagUtil.TAG_TITLE, tagTitle);

            final String uri = tagMapper.getURIByTitle(tagTitle);
            if (null != uri) {
                tag.put(TagUtil.TAG_URI, uri);
            } else {
                tag.put(TagUtil.TAG_URI, tagTitle);

                tagMapper.getURIByTitle(tagTitle);
            }

            ret.add(tag);
        }

        return ret;
    }
}
