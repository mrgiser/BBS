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

import cn.he.zhao.bbs.entityUtil.PointtransferUtil;
import cn.he.zhao.bbs.entityUtil.UserExtUtil;
import cn.he.zhao.bbs.spring.Stopwatchs;
import cn.he.zhao.bbs.mapper.PointtransferMapper;
import cn.he.zhao.bbs.mapper.UserMapper;
import cn.he.zhao.bbs.entity.Pointtransfer;
import cn.he.zhao.bbs.entity.UserExt;
import cn.he.zhao.bbs.entityUtil.my.CollectionUtils;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.util.JsonUtil;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang.time.DateUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Activity query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.5.1.6, May 27, 2018
 * @since 1.3.0
 */
@Service
public class ActivityQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ActivityQueryService.class);

    /**
     * PointtransferUtil Mapper.
     */
    @Autowired
    private PointtransferMapper pointtransferMapper;

    /**
     * User Mapper.
     */
    @Autowired
    private UserMapper userMapper;

    /**
     * PointtransferUtil query service.
     */
    @Autowired
    private PointtransferQueryService pointtransferQueryService;

    /**
     * Avatar query service.
     */
    @Autowired
    private AvatarQueryService avatarQueryService;

    /**
     * Gets average point of activity eating snake of a user specified by the given user id.
     *
     * @param userId the given user id
     * @return average point, if the point small than {@code 1}, returns {@code pointActivityEatingSnake} which
     * configured in sym.properties
     */
    public int getEatingSnakeAvgPoint(final String userId) {
        return pointtransferMapper.getActivityEatingSnakeAvg(userId);
    }

    /**
     * Gets the top eating snake users (single game max) with the specified fetch size.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param fetchSize      the specified fetch size
     * @return users, returns an empty list if not found
     */
    public List<JSONObject> getTopEatingSnakeUsersMax(final int avatarViewMode, final int fetchSize) {
        final List<JSONObject> ret = new ArrayList<>();

        try {
            final List<UserExt> users = userMapper.getsTopEatingsnakeUsersMax(fetchSize);

            for (final UserExt user : users) {
                JSONObject json = new JSONObject(JsonUtil.objectToJson(user));
                avatarQueryService.fillUserAvatarURL(avatarViewMode, json);

                ret.add(json);
            }
        } catch (final Exception e) {
            LOGGER.error( "Gets top eating snake users error", e);
        }

        return ret;
    }

    /**
     * Gets the top eating snake users (sum) with the specified fetch size.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param fetchSize      the specified fetch size
     * @return users, returns an empty list if not found
     */
    public List<JSONObject> getTopEatingSnakeUsersSum(final int avatarViewMode, final int fetchSize) {
        final List<JSONObject> ret = new ArrayList<>();

        try {
            final List<UserExt> users = userMapper.getsTopEatingsnakeUsersSum(fetchSize);

            for (final UserExt user : users) {
                JSONObject json = new JSONObject(JsonUtil.objectToJson(user));
                avatarQueryService.fillUserAvatarURL(avatarViewMode, json);

                ret.add(json);
            }
        } catch (final Exception e) {
            LOGGER.error( "Gets top eating snake users error", e);
        }

        return ret;
    }

    /**
     * Gets the top checkin users with the specified fetch size.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param fetchSize      the specified fetch size
     * @return users, returns an empty list if not found
     */
    public List<JSONObject> getTopCheckinUsers(final int avatarViewMode, final int fetchSize) {
        final List<JSONObject> ret = new ArrayList<>();

//        final Query query = new Query().addSort(UserExtUtil.USER_LONGEST_CHECKIN_STREAK, SortDirection.DESCENDING).
//                addSort(UserExtUtil.USER_CURRENT_CHECKIN_STREAK, SortDirection.DESCENDING).
//                setCurrentPageNum(1).setPageSize(fetchSize);

        PageHelper.startPage(1,fetchSize,"userLongestCheckinStreak DESC, userCurrentCheckinStreak DESC");

        try {
            final List<UserExt> users = userMapper.getAll();
//            final List<JSONObject> users = CollectionUtils.jsonArrayToList(result.optJSONArray(Keys.RESULTS));

            for (final UserExt user : users) {
                if (UserExtUtil.USER_APP_ROLE_C_HACKER == user.getUserAppRole()) {
                    user.setUserPointHex( Integer.toHexString(user.getUserPoint()));
                } else {
                    user.setUserPointCC( UserExtUtil.toCCString(user.getUserPoint()));
                }

                JSONObject json = new JSONObject(JsonUtil.objectToJson(user));
                avatarQueryService.fillUserAvatarURL(avatarViewMode, json);

                ret.add(json);
            }
        } catch (final Exception e) {
            LOGGER.error( "Gets top checkin users error", e);
        }

        return ret;
    }

    /**
     * Does checkin today?
     *
     * @param userId the specified user id
     * @return {@code true} if checkin succeeded, returns {@code false} otherwise
     */
    public synchronized boolean isCheckedinToday(final String userId) {
        Stopwatchs.start("Checks checkin");
        try {
            final UserExt user = userMapper.get(userId);
            final long time = user.getUserCheckinTime();

            return DateUtils.isSameDay(new Date(), new Date(time));
        } catch (final Exception e) {
            LOGGER.error( "Checks checkin failed", e);

            return true;
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Does participate 1A0001 today?
     *
     * @param userId the specified user id
     * @return {@code true} if participated, returns {@code false} otherwise
     */
    public synchronized boolean is1A0001Today(final String userId) {
        final Date now = new Date();

        final List<Pointtransfer> records = pointtransferQueryService.getLatestPointtransfers(userId,
                PointtransferUtil.TRANSFER_TYPE_C_ACTIVITY_1A0001, 1);
        if (records.isEmpty()) {
            return false;
        }

        final Pointtransfer maybeToday = records.get(0);
        final long time = maybeToday.getTime();

        return DateUtils.isSameDay(now, new Date(time));
    }

    /**
     * Did collect 1A0001 today?
     *
     * @param userId the specified user id
     * @return {@code true} if collected, returns {@code false} otherwise
     */
    public synchronized boolean isCollected1A0001Today(final String userId) {
        final Date now = new Date();

        final List<Pointtransfer> records = pointtransferQueryService.getLatestPointtransfers(userId,
                PointtransferUtil.TRANSFER_TYPE_C_ACTIVITY_1A0001_COLLECT, 1);
        if (records.isEmpty()) {
            return false;
        }

        final Pointtransfer maybeToday = records.get(0);
        final long time = maybeToday.getTime();

        return DateUtils.isSameDay(now, new Date(time));
    }

    /**
     * Did collect yesterday's liveness reward?
     *
     * @param userId the specified user id
     * @return {@code true} if collected, returns {@code false} otherwise
     */
    public synchronized boolean isCollectedYesterdayLivenessReward(final String userId) {
        final Date now = new Date();

        final List<Pointtransfer> records = pointtransferQueryService.getLatestPointtransfers(userId,
                PointtransferUtil.TRANSFER_TYPE_C_ACTIVITY_YESTERDAY_LIVENESS_REWARD, 1);
        if (records.isEmpty()) {
            return false;
        }

        final Pointtransfer maybeToday = records.get(0);
        final long time = maybeToday.getTime();

        return DateUtils.isSameDay(now, new Date(time));
    }
}
