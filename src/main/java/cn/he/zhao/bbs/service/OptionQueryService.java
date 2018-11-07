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

import cn.he.zhao.bbs.channel.ArticleChannel;
import cn.he.zhao.bbs.channel.ArticleListChannel;
import cn.he.zhao.bbs.channel.ChatRoomChannel;
import cn.he.zhao.bbs.channel.UserChannel;
import cn.he.zhao.bbs.entityUtil.OptionUtil;
import cn.he.zhao.bbs.entityUtil.my.CollectionUtils;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;

import cn.he.zhao.bbs.service.interf.LangPropsService;
import cn.he.zhao.bbs.util.JsonUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.transaction.annotation.Transactional;

import javax.websocket.Session;
import java.util.List;
import java.util.Set;

/**
 * OptionUtil query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.4.0.12, Jan 30, 2018
 * @since 0.2.0
 */
@Service
public class OptionQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OptionQueryService.class);

    /**
     * OptionUtil Mapper.
     */
    @Autowired
    private OptionMapper optionMapper;

    /**
     * Language service.
     */
    @Autowired
    private LangPropsService langPropsService;

    /**
     * Gets the online member count.
     *
     * @return online member count
     */
    public int getOnlineMemberCount() {
        int ret = 0;
        for (final Set<Session> value : UserChannel.SESSIONS.values()) {
            ret += value.size();
        }

        return ret;
    }

    /**
     * Gets the online visitor count.
     *
     * @return online visitor count
     */
    @Transactional
    public int getOnlineVisitorCount() {
        final int ret = ArticleChannel.SESSIONS.size() + ArticleListChannel.SESSIONS.size() + ChatRoomChannel.SESSIONS.size() + getOnlineMemberCount();

        try {
            final Option maxOnlineMemberCntRecord = optionMapper.get(OptionUtil.ID_C_STATISTIC_MAX_ONLINE_VISITOR_COUNT);
            final int maxOnlineVisitorCnt = Integer.parseInt(maxOnlineMemberCntRecord.getOptionValue());

            if (maxOnlineVisitorCnt < ret) {
                // Updates the max online visitor count

//                final Transaction transaction = optionMapper.beginTransaction();

                try {
                    maxOnlineMemberCntRecord.setOptionValue( String.valueOf(ret));
                    optionMapper.update(maxOnlineMemberCntRecord.getOid(), maxOnlineMemberCntRecord);

//                    transaction.commit();
                } catch (final Exception e) {
//                    if (transaction.isActive()) {
//                        transaction.rollback();
//                    }

                    LOGGER.error( "Updates the max online visitor count failed", e);
                }
            }
        } catch (final Exception ex) {
            LOGGER.error( "Gets online visitor count failed", ex);
        }

        return ret;
    }

    /**
     * Gets the statistic.
     *
     * @return statistic
     * @throws Exception service exception
     */
    public JSONObject getStatistic() throws Exception {
        final JSONObject ret = new JSONObject();

//        final Query query = new Query().
//                setFilter(new PropertyFilter(OptionUtil.OPTION_CATEGORY, FilterOperator.EQUAL, OptionUtil.CATEGORY_C_STATISTIC))
//                .setPageCount(1);
        try {
            final List<Option> result = optionMapper.getByOptionCategory(OptionUtil.CATEGORY_C_STATISTIC);
            final JSONArray options = JsonUtil.listToJSONArray(result);

            for (int i = 0; i < options.length(); i++) {
                final JSONObject option = options.optJSONObject(i);
                ret.put(option.optString(Keys.OBJECT_ID), option.optInt(OptionUtil.OPTION_VALUE));
            }

            return ret;
        } catch (final Exception e) {
            LOGGER.error( "Gets statistic failed", e);
            throw new Exception(e);
        }
    }

    /**
     * Checks whether the specified content contains reserved words.
     *
     * @param content the specified content
     * @return {@code true} if it contains reserved words, returns {@code false} otherwise
     */
    public boolean containReservedWord(final String content) {
        if (StringUtils.isBlank(content)) {
            return false;
        }

        try {
            final List<JSONObject> reservedWords = getReservedWords();

            for (final JSONObject reservedWord : reservedWords) {
                if (content.contains(reservedWord.optString(OptionUtil.OPTION_VALUE))) {
                    return true;
                }
            }

            return false;
        } catch (final Exception e) {
            return true;
        }
    }

    /**
     * Gets the reserved words.
     *
     * @return reserved words
     * @throws Exception service exception
     */
    public List<JSONObject> getReservedWords() throws Exception {
//        final Query query = new Query().
//                setFilter(new PropertyFilter(OptionUtil.OPTION_CATEGORY, FilterOperator.EQUAL, OptionUtil.CATEGORY_C_RESERVED_WORDS));
        try {
            final List<Option> result = optionMapper.getByOptionCategory(OptionUtil.CATEGORY_C_RESERVED_WORDS);
            final JSONArray options = JsonUtil.listToJSONArray(result);

            return CollectionUtils.jsonArrayToList(options);
        } catch (final Exception e) {
            LOGGER.error( "Gets reserved words failed", e);

            throw new Exception(e);
        }
    }

    /**
     * Checks whether the specified word is a reserved word.
     *
     * @param word the specified word
     * @return {@code true} if it is a reserved word, returns {@code false} otherwise
     */
    public boolean isReservedWord(final String word) {
//        final Query query = new Query().
//                setFilter(CompositeFilterOperator.and(
//                        new PropertyFilter(OptionUtil.OPTION_VALUE, FilterOperator.EQUAL, word),
//                        new PropertyFilter(OptionUtil.OPTION_CATEGORY, FilterOperator.EQUAL, OptionUtil.CATEGORY_C_RESERVED_WORDS)
//                ));
        try {
            return optionMapper.getByOptionCategoryAndValue(OptionUtil.CATEGORY_C_RESERVED_WORDS, word) > 0;
        } catch (final Exception e) {
            LOGGER.error( "Checks reserved word failed", e);

            return true;
        }
    }

    /**
     * Gets allow register option value.
     *
     * @return allow register option value, return {@code null} if not found
     */
    public String getAllowRegister() {
        try {
            final Option result = optionMapper.get(OptionUtil.ID_C_MISC_ALLOW_REGISTER);

            return result.getOptionValue();
        } catch (final Exception e) {
            LOGGER.error( "Gets option [allow register] value failed", e);

            return null;
        }
    }

    /**
     * Gets the miscellaneous.
     *
     * @return misc
     * @throws Exception service exception
     */
    public List<JSONObject> getMisc() throws Exception {
//        final Query query = new Query().
//                setFilter(new PropertyFilter(OptionUtil.OPTION_CATEGORY, FilterOperator.EQUAL, OptionUtil.CATEGORY_C_MISC));
        try {
            final List<Option> result = optionMapper.getByOptionCategory(OptionUtil.CATEGORY_C_MISC);
            final JSONArray options = JsonUtil.listToJSONArray(result);

            for (int i = 0; i < options.length(); i++) {
                final JSONObject option = options.optJSONObject(i);

                option.put("label", langPropsService.get(option.optString(Keys.OBJECT_ID) + "Label"));
            }

            return CollectionUtils.jsonArrayToList(options);
        } catch (final Exception e) {
            LOGGER.error( "Gets misc failed", e);
            throw new Exception(e);
        }
    }

    /**
     * Gets an option by the specified id.
     *
     * @param optionId the specified id
     * @return option, return {@code null} if not found
     */
    public Option getOption(final String optionId) {
        try {
            final Option ret = optionMapper.get(optionId);

            if (null == ret) {
                return null;
            }

            return ret;
        } catch (final Exception e) {
            LOGGER.error( "Gets an option [optionId=" + optionId + "] failed", e);

            return null;
        }
    }
}
