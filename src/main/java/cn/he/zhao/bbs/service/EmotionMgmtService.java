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

import cn.he.zhao.bbs.mapper.EmotionMapper;
import cn.he.zhao.bbs.model.Emotion;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Emotion management service.
 *
 * @author Zephyr
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.2.0, Aug 19, 2016
 * @since 1.5.0
 */
@Service
public class EmotionMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EmotionMgmtService.class);

    /**
     * Emotion Mapper.
     */
    @Autowired
    private EmotionMapper emotionMapper;

    /**
     * Sets a user's emotions.
     *
     * @param userId the specified user id
     * @param emotionList the specified emotions
     * @throws ServiceException service exception
     */
    public void setEmotionList(final String userId, final String emotionList) throws ServiceException {
        final Transaction transaction = emotionMapper.beginTransaction();

        try {
            // clears the user all emotions
            emotionMapper.removeUserEmotions(userId);

            final Set<String> emotionSet = new HashSet<>(); // for deduplication
            final String[] emotionArray = emotionList.split(",");
            for (int i = 0, sort = 0; i < emotionArray.length; i++) {
                final String content = emotionArray[i];
                if (StringUtils.isBlank(content) || emotionSet.contains(content) || !Emotions.isEmoji(content)) {
                    continue;
                }

                final JSONObject userEmotion = new JSONObject();
                userEmotion.put(Emotion.EMOTION_USER_ID, userId);
                userEmotion.put(Emotion.EMOTION_CONTENT, content);
                userEmotion.put(Emotion.EMOTION_SORT, sort++);
                userEmotion.put(Emotion.EMOTION_TYPE, Emotion.EMOTION_TYPE_C_EMOJI);

                emotionMapper.add(userEmotion);

                emotionSet.add(content);
            }

            transaction.commit();
        } catch (final MapperException e) {
            LOGGER.error( "Set user emotion list failed [id=" + userId + "]", e);
            if (null != transaction && transaction.isActive()) {
                transaction.rollback();
            }

            throw new ServiceException(e);
        }
    }
}
