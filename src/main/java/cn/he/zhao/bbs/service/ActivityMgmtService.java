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

import cn.he.zhao.bbs.entityUtil.LivenessUtil;
import cn.he.zhao.bbs.entityUtil.PointtransferUtil;
import cn.he.zhao.bbs.entityUtil.UserExtUtil;
import cn.he.zhao.bbs.spring.Strings;
import cn.he.zhao.bbs.mapper.CharacterMapper;
import cn.he.zhao.bbs.mapper.PointtransferMapper;
import cn.he.zhao.bbs.entity.Liveness;
import cn.he.zhao.bbs.entity.Pointtransfer;
import cn.he.zhao.bbs.entity.UserExt;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.service.interf.LangPropsService;
import cn.he.zhao.bbs.util.Results;
import cn.he.zhao.bbs.util.Symphonys;
import cn.he.zhao.bbs.util.Tesseracts;
import jodd.util.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Activity management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://zephyr.b3log.org">Zephyr</a>
 * @version 1.6.10.1, Jan 30, 2018
 * @since 1.3.0
 */
@Service
public class ActivityMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ActivityMgmtService.class);

    /**
     * Character Mapper.
     */
    @Autowired
    private CharacterMapper characterMapper;

    /**
     * PointtransferUtil Mapper.
     */
    @Autowired
    private PointtransferMapper pointtransferMapper;

    /**
     * PointtransferUtil query service.
     */
    @Autowired
    private PointtransferQueryService pointtransferQueryService;

    /**
     * PointtransferUtil management service.
     */
    @Autowired
    private PointtransferMgmtService pointtransferMgmtService;

    /**
     * Activity query service.
     */
    @Autowired
    private ActivityQueryService activityQueryService;

    /**
     * User management service.
     */
    @Autowired
    private UserMgmtService userMgmtService;

    /**
     * User query service.
     */
    @Autowired
    private UserQueryService userQueryService;

    /**
     * Language service.
     */
    @Autowired
    private LangPropsService langPropsService;

    /**
     * LivenessUtil management service.
     */
    @Autowired
    private LivenessMgmtService livenessMgmtService;

    /**
     * LivenessUtil query service.
     */
    @Autowired
    private LivenessQueryService livenessQueryService;

    /**
     * Starts eating snake.
     *
     * @param userId the specified user id
     * @return result
     */
    public synchronized JSONObject startEatingSnake(final String userId) {
        final JSONObject ret = Results.falseResult();

        final int startPoint = pointtransferMapper.getActivityEatingSnakeAvg(userId);

        final boolean succ = null != pointtransferMgmtService.transfer(userId, PointtransferUtil.ID_C_SYS,
                PointtransferUtil.TRANSFER_TYPE_C_ACTIVITY_EATINGSNAKE,
                startPoint, "", System.currentTimeMillis());

        ret.put(Keys.STATUS_CODE, succ);

        final String msg = succ ? "started" : langPropsService.get("activityStartEatingSnakeFailLabel");
        ret.put(Keys.MSG, msg);

        livenessMgmtService.incLiveness(userId, LivenessUtil.LIVENESS_ACTIVITY);

        return ret;
    }

    /**
     * Collects eating snake.
     *
     * @param userId the specified user id
     * @param score  the specified score
     * @return result
     */
    public synchronized JSONObject collectEatingSnake(final String userId, final int score) {
        final JSONObject ret = Results.falseResult();

        if (score < 1) {
            ret.put(Keys.STATUS_CODE, true);

            return ret;
        }

        final int max = Symphonys.getInt("pointActivityEatingSnakeCollectMax");
        final int amout = score > max ? max : score;

        final boolean succ = null != pointtransferMgmtService.transfer(PointtransferUtil.ID_C_SYS, userId,
                PointtransferUtil.TRANSFER_TYPE_C_ACTIVITY_EATINGSNAKE_COLLECT, amout,
                "", System.currentTimeMillis());

        if (!succ) {
            ret.put(Keys.MSG, "Sorry, transfer point failed, please contact admin");
        }

        ret.put(Keys.STATUS_CODE, succ);

        return ret;
    }

    /**
     * Submits the specified character to recognize.
     *
     * @param userId       the specified user id
     * @param characterImg the specified character image encoded by Base64
     * @param character    the specified character
     * @return recognition result
     */
    @Transactional
    public synchronized JSONObject submitCharacter(final String userId, final String characterImg, final String character) {
        String recongnizeFailedMsg = langPropsService.get("activityCharacterRecognizeFailedLabel");

        final JSONObject ret = new JSONObject();
        ret.put(Keys.STATUS_CODE, false);
        ret.put(Keys.MSG, recongnizeFailedMsg);

        if (StringUtils.isBlank(characterImg) || StringUtils.isBlank(character)) {
            ret.put(Keys.STATUS_CODE, false);
            ret.put(Keys.MSG, recongnizeFailedMsg);

            return ret;
        }

        final byte[] data = Base64.decode(characterImg);
        OutputStream stream = null;
        final String tmpDir = System.getProperty("java.io.tmpdir");
        final String imagePath = tmpDir + "/" + userId + "-character.png";

        try {
            stream = new FileOutputStream(imagePath);
            stream.write(data);
            stream.flush();
            stream.close();
        } catch (final IOException e) {
            LOGGER.error( "Submits character failed", e);

            return ret;
        } finally {
            if (null != stream) {
                try {
                    stream.close();
                } catch (final IOException ex) {
                    LOGGER.error( "Closes stream failed", ex);
                }
            }
        }

        final String recognizedCharacter = Tesseracts.recognizeCharacter(imagePath);
        LOGGER.info("Character [" + character + "], recognized [" + recognizedCharacter + "], image path [" + imagePath
                + "]");
        if (StringUtils.equals(character, recognizedCharacter)) {
            final Query query = new Query();
            query.setFilter(CompositeFilterOperator.and(
                    new PropertyFilter(cn.he.zhao.bbs.entityUtil.CharacterUtil.CHARACTER_USER_ID, FilterOperator.EQUAL, userId),
                    new PropertyFilter(cn.he.zhao.bbs.entityUtil.CharacterUtil.CHARACTER_CONTENT, FilterOperator.EQUAL, character)
            ));

            try {
                if (characterMapper.count(query) > 0) {
                    return ret;
                }
            } catch (final Exception e) {
                LOGGER.error( "Count characters failed [userId=" + userId + ", character=" + character + "]", e);

                return ret;
            }

            final JSONObject record = new JSONObject();
            record.put(cn.he.zhao.bbs.entityUtil.CharacterUtil.CHARACTER_CONTENT, character);
            record.put(cn.he.zhao.bbs.entityUtil.CharacterUtil.CHARACTER_IMG, characterImg);
            record.put(cn.he.zhao.bbs.entityUtil.CharacterUtil.CHARACTER_USER_ID, userId);

            String characterId = "";
//            final Transaction transaction = characterMapper.beginTransaction();
//            try {
                characterId = characterMapper.add(record);

//                transaction.commit();
//            } catch (final MapperException e) {
//                LOGGER.error( "Submits character failed", e);

//                if (null != transaction) {
//                    transaction.rollback();
//                }

                return ret;
//            }

            pointtransferMgmtService.transfer(PointtransferUtil.ID_C_SYS, userId,
                    PointtransferUtil.TRANSFER_TYPE_C_ACTIVITY_CHARACTER, PointtransferUtil.TRANSFER_SUM_C_ACTIVITY_CHARACTER,
                    characterId, System.currentTimeMillis());

            ret.put(Keys.STATUS_CODE, true);
            ret.put(Keys.MSG, langPropsService.get("activityCharacterRecognizeSuccLabel"));
        } else {
            recongnizeFailedMsg = recongnizeFailedMsg.replace("{一}", recognizedCharacter);
            ret.put(Keys.STATUS_CODE, false);
            ret.put(Keys.MSG, recongnizeFailedMsg);
        }

        return ret;
    }

    /**
     * Daily checkin.
     *
     * @param userId the specified user id
     * @return {@code Random int} if checkin succeeded, returns {@code Integer.MIN_VALUE} otherwise
     */
    public synchronized int dailyCheckin(final String userId) {
        if (activityQueryService.isCheckedinToday(userId)) {
            return Integer.MIN_VALUE;
        }

        final Random random = new Random();
        final int sum = random.nextInt(PointtransferUtil.TRANSFER_SUM_C_ACTIVITY_CHECKIN_MAX)
                % (PointtransferUtil.TRANSFER_SUM_C_ACTIVITY_CHECKIN_MAX - PointtransferUtil.TRANSFER_SUM_C_ACTIVITY_CHECKIN_MIN + 1)
                + PointtransferUtil.TRANSFER_SUM_C_ACTIVITY_CHECKIN_MIN;
        final boolean succ = null != pointtransferMgmtService.transfer(PointtransferUtil.ID_C_SYS, userId,
                PointtransferUtil.TRANSFER_TYPE_C_ACTIVITY_CHECKIN, sum, userId, System.currentTimeMillis());
        if (!succ) {
            return Integer.MIN_VALUE;
        }

        try {
            final UserExt user = userQueryService.getUser(userId);

            int currentStreakStart = user.getUserCurrentCheckinStreakStart();
            int currentStreakEnd = user.getUserCurrentCheckinStreakEnd();

            final Date today = new Date();
            user.setUserCheckinTime( today.getTime());

            final String todayStr = DateFormatUtils.format(today, "yyyyMMdd");
            final int todayInt = Integer.valueOf(todayStr);

            if (0 == currentStreakStart) {
                user.setUserCurrentCheckinStreakStart( todayInt);
                user.setUserCurrentCheckinStreakEnd( todayInt);
                user.setUserLongestCheckinStreakStart( todayInt);
                user.setUserLongestCheckinStreakEnd( todayInt);
                user.setUserCurrentCheckinStreak( 1);
                user.setUserLongestCheckinStreak( 1);

                userMgmtService.updateUser(userId, user);

                return sum;
            }

            final Date endDate = DateUtils.parseDate(String.valueOf(currentStreakEnd), new String[]{"yyyyMMdd"});
            final Date nextDate = DateUtils.addDays(endDate, 1);

            if (DateUtils.isSameDay(nextDate, today)) {
                user.setUserLongestCheckinStreakEnd( todayInt);
            } else {
                user.setUserCurrentCheckinStreakStart( todayInt);
                user.setUserCurrentCheckinStreakEnd( todayInt);
            }

            currentStreakStart = user.getUserCurrentCheckinStreakStart();
            currentStreakEnd = user.getUserCurrentCheckinStreakEnd();
            final int longestStreakStart = user.getUserLongestCheckinStreakStart();
            final int longestStreakEnd = user.getUserLongestCheckinStreakEnd();

            final Date currentStreakStartDate
                    = DateUtils.parseDate(String.valueOf(currentStreakStart), new String[]{"yyyyMMdd"});
            final Date currentStreakEndDate
                    = DateUtils.parseDate(String.valueOf(currentStreakEnd), new String[]{"yyyyMMdd"});
            final Date longestStreakStartDate
                    = DateUtils.parseDate(String.valueOf(longestStreakStart), new String[]{"yyyyMMdd"});
            final Date longestStreakEndDate
                    = DateUtils.parseDate(String.valueOf(longestStreakEnd), new String[]{"yyyyMMdd"});

            final int currentStreakDays
                    = (int) ((currentStreakEndDate.getTime() - currentStreakStartDate.getTime()) / 86400000) + 1;
            final int longestStreakDays
                    = (int) ((longestStreakEndDate.getTime() - longestStreakStartDate.getTime()) / 86400000) + 1;

            user.setUserCurrentCheckinStreak( currentStreakDays);
            user.setUserLongestCheckinStreak( longestStreakDays);

            if (longestStreakDays < currentStreakDays) {
                user.setUserLongestCheckinStreakStart( currentStreakStart);
                user.setUserLongestCheckinStreakEnd( currentStreakEnd);

                user.setUserLongestCheckinStreak( currentStreakDays);
            }

            userMgmtService.updateUser(userId, user);

            if (currentStreakDays > 0 && 0 == currentStreakDays % 10) {
                // Additional Point
                pointtransferMgmtService.transfer(PointtransferUtil.ID_C_SYS, userId,
                        PointtransferUtil.TRANSFER_TYPE_C_ACTIVITY_CHECKIN_STREAK,
                        PointtransferUtil.TRANSFER_SUM_C_ACTIVITY_CHECKINT_STREAK, userId, System.currentTimeMillis());
            }

            livenessMgmtService.incLiveness(userId, LivenessUtil.LIVENESS_ACTIVITY);

            return sum;
        } catch (final Exception e) {
            LOGGER.error( "Checkin streak error", e);

            return Integer.MIN_VALUE;
        }
    }

    /**
     * Bets 1A0001.
     *
     * @param userId       the specified user id
     * @param amount       the specified amount
     * @param smallOrLarge the specified small or large
     * @return result
     */
    public synchronized JSONObject bet1A0001(final String userId, final int amount, final int smallOrLarge) {
        final JSONObject ret = Results.falseResult();

        if (activityQueryService.is1A0001Today(userId)) {
            ret.put(Keys.MSG, langPropsService.get("activityParticipatedLabel"));

            return ret;
        }

        final String date = DateFormatUtils.format(new Date(), "yyyyMMdd");

        final boolean succ = null != pointtransferMgmtService.transfer(userId, PointtransferUtil.ID_C_SYS,
                PointtransferUtil.TRANSFER_TYPE_C_ACTIVITY_1A0001, amount, date + "-" + smallOrLarge, System.currentTimeMillis());

        ret.put(Keys.STATUS_CODE, succ);

        final String msg = succ
                ? langPropsService.get("activityBetSuccLabel") : langPropsService.get("activityBetFailLabel");
        ret.put(Keys.MSG, msg);

        livenessMgmtService.incLiveness(userId, LivenessUtil.LIVENESS_ACTIVITY);

        return ret;
    }

    /**
     * Collects 1A0001.
     *
     * @param userId the specified user id
     * @return result
     */
    public synchronized JSONObject collect1A0001(final String userId) {
        final JSONObject ret = Results.falseResult();

        if (!activityQueryService.is1A0001Today(userId)) {
            ret.put(Keys.MSG, langPropsService.get("activityNotParticipatedLabel"));

            return ret;
        }

        if (activityQueryService.isCollected1A0001Today(userId)) {
            ret.put(Keys.MSG, langPropsService.get("activityParticipatedLabel"));

            return ret;
        }

        final List<JSONObject> records = pointtransferQueryService.getLatestPointtransfers(userId,
                PointtransferUtil.TRANSFER_TYPE_C_ACTIVITY_1A0001, 1);
        final JSONObject pointtransfer = records.get(0);
        final String data = pointtransfer.optString(PointtransferUtil.DATA_ID);
        final String smallOrLarge = data.split("-")[1];
        final int sum = pointtransfer.optInt(PointtransferUtil.SUM);

        String smallOrLargeResult = null;
        try {
            final Document doc = Jsoup.parse(new URL("http://stockpage.10jqka.com.cn/1A0001/quote/header/"), 5000);
            final JSONObject result = new JSONObject(doc.text());
            final String price = result.optJSONObject("data").optJSONObject("1A0001").optString("10");

            if (!price.contains(".")) {
                smallOrLargeResult = "0";
            } else {
                int endInt = 0;
                if (price.split("\\.")[1].length() > 1) {
                    final String end = price.substring(price.length() - 1);
                    endInt = Integer.valueOf(end);
                }

                if (0 <= endInt && endInt <= 4) {
                    smallOrLargeResult = "0";
                } else if (5 <= endInt && endInt <= 9) {
                    smallOrLargeResult = "1";
                } else {
                    LOGGER.error("Activity 1A0001 collect result [" + endInt + "]");
                }
            }
        } catch (final Exception e) {
            LOGGER.error( "Collect 1A0001 failed", e);

            ret.put(Keys.MSG, langPropsService.get("activity1A0001CollectFailLabel"));

            return ret;
        }

        if (Strings.isEmptyOrNull(smallOrLarge)) {
            ret.put(Keys.MSG, langPropsService.get("activity1A0001CollectFailLabel"));

            return ret;
        }

        ret.put(Keys.STATUS_CODE, true);
        if (StringUtils.equals(smallOrLarge, smallOrLargeResult)) {
            final int amount = sum * 2;

            final boolean succ = null != pointtransferMgmtService.transfer(PointtransferUtil.ID_C_SYS, userId,
                    PointtransferUtil.TRANSFER_TYPE_C_ACTIVITY_1A0001_COLLECT, amount,
                    DateFormatUtils.format(new Date(), "yyyyMMdd") + "-" + smallOrLargeResult, System.currentTimeMillis());

            if (succ) {
                String msg = langPropsService.get("activity1A0001CollectSucc1Label");
                msg = msg.replace("{point}", String.valueOf(amount));

                ret.put(Keys.MSG, msg);
            } else {
                ret.put(Keys.MSG, langPropsService.get("activity1A0001CollectFailLabel"));
            }
        } else {
            ret.put(Keys.MSG, langPropsService.get("activity1A0001CollectSucc0Label"));
        }

        return ret;
    }

    /**
     * Collects yesterday's liveness reward.
     *
     * @param userId the specified user id
     */
    public synchronized void yesterdayLivenessReward(final String userId) {
        if (activityQueryService.isCollectedYesterdayLivenessReward(userId)) {
            return;
        }

        final JSONObject yesterdayLiveness = livenessQueryService.getYesterdayLiveness(userId);
        if (null == yesterdayLiveness) {
            return;
        }

        final int sum = LivenessUtil.calcPoint(yesterdayLiveness);

        if (0 == sum) {
            return;
        }

        boolean succ = null != pointtransferMgmtService.transfer(PointtransferUtil.ID_C_SYS, userId,
                PointtransferUtil.TRANSFER_TYPE_C_ACTIVITY_YESTERDAY_LIVENESS_REWARD, sum, userId, System.currentTimeMillis());
        if (!succ) {
            return;
        }

        // Today liveness (activity)
        livenessMgmtService.incLiveness(userId, LivenessUtil.LIVENESS_ACTIVITY);
    }

    /**
     * Starts Gobang.
     *
     * @param userId the specified user id
     * @return result
     */
    public synchronized JSONObject startGobang(final String userId) {
        final JSONObject ret = Results.falseResult();

        final int startPoint = PointtransferUtil.TRANSFER_SUM_C_ACTIVITY_GOBANG_START;

        final boolean succ = null != pointtransferMgmtService.transfer(userId, PointtransferUtil.ID_C_SYS,
                PointtransferUtil.TRANSFER_TYPE_C_ACTIVITY_GOBANG,
                startPoint, "", System.currentTimeMillis());

        ret.put(Keys.STATUS_CODE, succ);

        final String msg = succ ? "started" : langPropsService.get("activityStartGobangFailLabel");
        ret.put(Keys.MSG, msg);

        livenessMgmtService.incLiveness(userId, LivenessUtil.LIVENESS_ACTIVITY);

        return ret;
    }

    /**
     * Collects Gobang.
     *
     * @param userId the specified user id
     * @param score  the specified score
     * @return result
     */
    public synchronized JSONObject collectGobang(final String userId, final int score) {
        final JSONObject ret = Results.falseResult();

        final boolean succ = null != pointtransferMgmtService.transfer(PointtransferUtil.ID_C_SYS, userId,
                PointtransferUtil.TRANSFER_TYPE_C_ACTIVITY_GOBANG_COLLECT, score,
                "", System.currentTimeMillis());

        if (!succ) {
            ret.put(Keys.MSG, "Sorry, transfer point failed, please contact admin");
        }

        ret.put(Keys.STATUS_CODE, succ);

        return ret;
    }
}
