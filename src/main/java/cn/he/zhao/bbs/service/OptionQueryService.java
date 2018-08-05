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

import cn.he.zhao.bbs.model.my.*;
import cn.he.zhao.bbs.util.Symphonys;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.model.*;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.websocket.Session;
import java.util.List;
import java.util.Set;

/**
 * Option query service.
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
     * Option Mapper.
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
    public int getOnlineVisitorCount() {
        final int ret = ArticleChannel.SESSIONS.size() + ArticleListChannel.SESSIONS.size() + ChatRoomChannel.SESSIONS.size() + getOnlineMemberCount();

        try {
            final JSONObject maxOnlineMemberCntRecord = optionMapper.get(Option.ID_C_STATISTIC_MAX_ONLINE_VISITOR_COUNT);
            final int maxOnlineVisitorCnt = maxOnlineMemberCntRecord.optInt(Option.OPTION_VALUE);

            if (maxOnlineVisitorCnt < ret) {
                // Updates the max online visitor count

                final Transaction transaction = optionMapper.beginTransaction();

                try {
                    maxOnlineMemberCntRecord.put(Option.OPTION_VALUE, String.valueOf(ret));
                    optionMapper.update(maxOnlineMemberCntRecord.optString(Keys.OBJECT_ID), maxOnlineMemberCntRecord);

                    transaction.commit();
                } catch (final MapperException e) {
                    if (transaction.isActive()) {
                        transaction.rollback();
                    }

                    LOGGER.error( "Updates the max online visitor count failed", e);
                }
            }
        } catch (final MapperException ex) {
            LOGGER.error( "Gets online visitor count failed", ex);
        }

        return ret;
    }

    /**
     * Gets the statistic.
     *
     * @return statistic
     * @throws ServiceException service exception
     */
    public JSONObject getStatistic() throws ServiceException {
        final JSONObject ret = new JSONObject();

        final Query query = new Query().
                setFilter(new PropertyFilter(Option.OPTION_CATEGORY, FilterOperator.EQUAL, Option.CATEGORY_C_STATISTIC))
                .setPageCount(1);
        try {
            final JSONObject result = optionMapper.get(query);
            final JSONArray options = result.optJSONArray(Keys.RESULTS);

            for (int i = 0; i < options.length(); i++) {
                final JSONObject option = options.optJSONObject(i);
                ret.put(option.optString(Keys.OBJECT_ID), option.optInt(Option.OPTION_VALUE));
            }

            return ret;
        } catch (final MapperException e) {
            LOGGER.error( "Gets statistic failed", e);
            throw new ServiceException(e);
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
                if (content.contains(reservedWord.optString(Option.OPTION_VALUE))) {
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
     * @throws ServiceException service exception
     */
    public List<JSONObject> getReservedWords() throws ServiceException {
        final Query query = new Query().
                setFilter(new PropertyFilter(Option.OPTION_CATEGORY, FilterOperator.EQUAL, Option.CATEGORY_C_RESERVED_WORDS));
        try {
            final JSONObject result = optionMapper.get(query);
            final JSONArray options = result.optJSONArray(Keys.RESULTS);

            return CollectionUtils.jsonArrayToList(options);
        } catch (final MapperException e) {
            LOGGER.error( "Gets reserved words failed", e);

            throw new ServiceException(e);
        }
    }

    /**
     * Checks whether the specified word is a reserved word.
     *
     * @param word the specified word
     * @return {@code true} if it is a reserved word, returns {@code false} otherwise
     */
    public boolean isReservedWord(final String word) {
        final Query query = new Query().
                setFilter(CompositeFilterOperator.and(
                        new PropertyFilter(Option.OPTION_VALUE, FilterOperator.EQUAL, word),
                        new PropertyFilter(Option.OPTION_CATEGORY, FilterOperator.EQUAL, Option.CATEGORY_C_RESERVED_WORDS)
                ));
        try {
            return optionMapper.count(query) > 0;
        } catch (final MapperException e) {
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
            final JSONObject result = optionMapper.get(Option.ID_C_MISC_ALLOW_REGISTER);

            return result.optString(Option.OPTION_VALUE);
        } catch (final MapperException e) {
            LOGGER.error( "Gets option [allow register] value failed", e);

            return null;
        }
    }

    /**
     * Gets the miscellaneous.
     *
     * @return misc
     * @throws ServiceException service exception
     */
    public List<JSONObject> getMisc() throws ServiceException {
        final Query query = new Query().
                setFilter(new PropertyFilter(Option.OPTION_CATEGORY, FilterOperator.EQUAL, Option.CATEGORY_C_MISC));
        try {
            final JSONObject result = optionMapper.get(query);
            final JSONArray options = result.optJSONArray(Keys.RESULTS);

            for (int i = 0; i < options.length(); i++) {
                final JSONObject option = options.optJSONObject(i);

                option.put("label", langPropsService.get(option.optString(Keys.OBJECT_ID) + "Label"));
            }

            return CollectionUtils.jsonArrayToList(options);
        } catch (final MapperException e) {
            LOGGER.error( "Gets misc failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets an option by the specified id.
     *
     * @param optionId the specified id
     * @return option, return {@code null} if not found
     */
    public JSONObject getOption(final String optionId) {
        try {
            final JSONObject ret = optionMapper.get(optionId);

            if (null == ret) {
                return null;
            }

            return ret;
        } catch (final MapperException e) {
            LOGGER.error( "Gets an option [optionId=" + optionId + "] failed", e);

            return null;
        }
    }
}
